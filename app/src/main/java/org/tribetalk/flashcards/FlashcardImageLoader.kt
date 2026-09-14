package org.tribetalk.flashcards

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.ui.components.FlnVectorGraphic
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Unified, memory-safe image loading and visual hierarchy engine for TribeTalk Flashcards.
 *
 * Enforces the strict 4-tier visual hierarchy:
 * 1. Real bundled image (drawable resource in APK)
 * 2. Valid teacher-selected image URI (internal persisted file or valid content URI)
 * 3. Existing emoji fallback (word-specific emoji)
 * 4. Existing generic fallback (FlnVectorGraphic or generic card)
 */
object FlashcardImageLoader {

    /**
     * Safely copies a teacher-selected photo from a transient content:// URI into
     * permanent app-internal storage, ensuring read access never expires.
     *
     * @return Absolute file path to the saved local image, or null on error.
     */
    fun saveTeacherPhoto(context: Context, sourceUri: Uri): String? {
        return try {
            val storageDir = File(context.filesDir, "flashcards/images").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(storageDir, "teacher_photo_${System.currentTimeMillis()}.jpg")

            // Downsample during copy if necessary to avoid excessive disk/memory usage
            val bounds = decodeBounds(context, sourceUri)
            val sampleSize = calculateInSampleSize(bounds.first, bounds.second, 1024, 1024)

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            if (bitmap != null) {
                FileOutputStream(destinationFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                bitmap.recycle()
                destinationFile.absolutePath
            } else {
                // Fallback direct stream copy
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (destinationFile.exists() && destinationFile.length() > 0) {
                    destinationFile.absolutePath
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Resolves an Android drawable resource ID if the reference points to a packaged resource.
     *
     * Supports:
     * - "android.resource://[package]/drawable/[name]"
     * - "@drawable/[name]"
     * - "res:[name]"
     * - Direct resource name (e.g. "ic_fln_dog")
     * - Mapping from iconType (e.g. "dog" -> "ic_fln_dog")
     */
    fun resolveDrawableResId(
        context: Context,
        imageUri: String?,
        iconType: String?,
        numeralValue: Int? = null
    ): Int? {
        val packageName = context.packageName

        // 1. Direct imageUri resource resolution
        if (!imageUri.isNullOrBlank()) {
            val cleanRef = when {
                imageUri.startsWith("android.resource://") -> {
                    imageUri.substringAfterLast("/")
                }
                imageUri.startsWith("@drawable/") -> {
                    imageUri.removePrefix("@drawable/")
                }
                imageUri.startsWith("res:") -> {
                    imageUri.removePrefix("res:")
                }
                imageUri.startsWith("ic_fln_") -> {
                    imageUri
                }
                else -> null
            }

            if (!cleanRef.isNullOrBlank()) {
                val id = context.resources.getIdentifier(cleanRef, "drawable", packageName)
                if (id != 0) return id
            }
        }

        // 2. IconType resource mapping
        if (!iconType.isNullOrBlank()) {
            val directIconId = context.resources.getIdentifier("ic_fln_$iconType", "drawable", packageName)
            if (directIconId != 0) return directIconId

            if (iconType == "number_counter" && numeralValue != null) {
                val numId = context.resources.getIdentifier("ic_fln_num$numeralValue", "drawable", packageName)
                if (numId != 0) return numId
            }
        }

        return null
    }

    /**
     * Safely loads and decodes a Bitmap from file path or content URI with memory-bounded downsampling.
     */
    fun loadBitmapSafe(context: Context, uriString: String, maxDimPx: Int = 512): Bitmap? {
        if (uriString.isBlank()) return null
        return try {
            if (uriString.startsWith("content://")) {
                val uri = Uri.parse(uriString)
                val bounds = decodeBounds(context, uri)
                if (bounds.first <= 0 || bounds.second <= 0) return null
                val sampleSize = calculateInSampleSize(bounds.first, bounds.second, maxDimPx, maxDimPx)
                val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
            } else {
                val cleanPath = if (uriString.startsWith("file://")) {
                    Uri.parse(uriString).path ?: uriString.removePrefix("file://")
                } else {
                    uriString
                }
                val file = File(cleanPath)
                if (!file.exists() || !file.canRead()) return null

                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, bounds)
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

                val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimPx, maxDimPx)
                val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                BitmapFactory.decodeFile(file.absolutePath, options)
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun decodeBounds(context: Context, uri: Uri): Pair<Int, Int> {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
            Pair(options.outWidth, options.outHeight)
        } catch (_: Exception) {
            Pair(-1, -1)
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }
}

/**
 * Reusable Flashcard Visual prompt obeying the strict 4-tier visual hierarchy.
 */
@Composable
fun FlashcardVisual(
    imageUri: String?,
    iconType: String?,
    imageEmoji: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxImageSize: Dp = 100.dp,
    emojiFontSize: TextUnit = 64.sp,
    vectorGraphicSize: Dp = 80.dp,
    numeralValue: Int? = null
) {
    val context = LocalContext.current

    // Tier 1: Real bundled image in APK drawable resources
    val bundledResId = remember(imageUri, iconType, numeralValue) {
        FlashcardImageLoader.resolveDrawableResId(context, imageUri, iconType, numeralValue)
    }

    // Tier 2: Valid teacher-selected image URI
    val teacherBitmap = remember(imageUri, bundledResId) {
        if (bundledResId == null && !imageUri.isNullOrBlank()) {
            FlashcardImageLoader.loadBitmapSafe(context, imageUri, maxDimPx = 512)
        } else null
    }

    when {
        bundledResId != null -> {
            Image(
                painter = painterResource(id = bundledResId),
                contentDescription = contentDescription,
                modifier = modifier
                    .sizeIn(maxWidth = maxImageSize, maxHeight = maxImageSize)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Fit
            )
        }
        teacherBitmap != null -> {
            Image(
                bitmap = teacherBitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier
                    .sizeIn(maxWidth = maxImageSize, maxHeight = maxImageSize)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
        }
        // Tier 3: Existing emoji fallback (exclude generic placeholder "🎴" and "??")
        !imageEmoji.isNullOrBlank() && imageEmoji != "🎴" && imageEmoji != "??" -> {
            Text(
                text = imageEmoji,
                fontSize = emojiFontSize,
                textAlign = TextAlign.Center
            )
        }
        // Tier 4: Existing generic fallback (FlnVectorGraphic or generic card)
        !iconType.isNullOrBlank() -> {
            FlnVectorGraphic(
                iconType = iconType,
                size = vectorGraphicSize,
                tint = MaterialTheme.colorScheme.primary,
                numeralValue = numeralValue
            )
        }
        else -> {
            Text(
                text = "🎴",
                fontSize = emojiFontSize,
                textAlign = TextAlign.Center
            )
        }
    }
}
