package org.tribetalk.fln.pipeline

import android.content.Context
import android.graphics.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import org.tribetalk.fln.image.rememberFlnImage
import org.tribetalk.ui.theme.*

/**
 * Scene Layout & Composition Engine.
 * Takes an ActivityIR and dynamically composes reusable SVG primitives into:
 * 1. Interactive Compose Views (Ten-frame grids, equations, touch manipulatives)
 * 2. Vector PDF Canvas drawings (Printable A4 worksheets)
 */
object SceneComposer {

    /**
     * Resolves the asset relative path from the SVG corpus or fallback flashcard assets.
     */
    fun resolveAssetPath(objectKey: String): String {
        val meta = SvgCorpusRegistry.get(objectKey)
        return meta?.relativeFilePath ?: "fln_svg_corpus/plants/mango.svg"
    }

    /**
     * Composes and draws an ActivityIR onto an Android Canvas for A4 Vector PDF rendering.
     */
    fun drawActivityOnPdfCanvas(
        canvas: Canvas,
        paint: Paint,
        ir: ActivityIR,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        context: Context
    ) {
        val assetPath = resolveAssetPath(ir.primaryObjectKey)
        val bitmap = loadBitmapFromAsset(context, assetPath)

        // Ink-friendly desaturation paint filter
        val monoFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        val printPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = monoFilter
        }

        when (ir.actionType) {
            ActivityActionType.COUNT_AND_SELECT,
            ActivityActionType.COUNT_AND_MATCH -> {
                // Draw counting tokens in a balanced row/grid below prompt text
                val tokenSize = 20f
                val count = ir.quantity.coerceAtMost(12)
                for (i in 0 until count) {
                    val row = i / 6
                    val col = i % 6
                    val tx = x + 30 + (col * 24)
                    val ty = y + 36 + (row * 22)

                    if (bitmap != null) {
                        canvas.drawBitmap(bitmap, null, RectF(tx, ty, tx + tokenSize, ty + tokenSize), printPaint)
                    } else {
                        paint.style = Paint.Style.FILL
                        paint.color = android.graphics.Color.rgb(30, 41, 59)
                        canvas.drawCircle(tx + tokenSize / 2, ty + tokenSize / 2, 6.5f, paint)
                    }
                }

                // Answer box on right
                paint.style = Paint.Style.STROKE
                paint.color = android.graphics.Color.rgb(180, 195, 215)
                paint.strokeWidth = 1.2f
                val box = RectF(x + w - 85, y + 36, x + w - 20, y + 66)
                canvas.drawRoundRect(box, 4f, 4f, paint)

                paint.style = Paint.Style.FILL
                paint.color = android.graphics.Color.rgb(30, 41, 59)
                paint.textSize = 10.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Count: [   ]", x + w - 80, y + 54, paint)
            }

            ActivityActionType.ADDITION_CONCRETE -> {
                val tokenSize = 18f
                // Group A
                for (i in 0 until ir.quantity.coerceAtMost(5)) {
                    val tx = x + 24 + (i * 20)
                    if (bitmap != null) {
                        canvas.drawBitmap(bitmap, null, RectF(tx, y + 38, tx + tokenSize, y + 38 + tokenSize), printPaint)
                    } else {
                        paint.style = Paint.Style.FILL
                        paint.color = android.graphics.Color.rgb(30, 41, 59)
                        canvas.drawCircle(tx + tokenSize / 2, y + 38 + tokenSize / 2, 5.5f, paint)
                    }
                }

                // Plus symbol
                val plusX = x + 24 + (ir.quantity.coerceAtMost(5) * 20) + 6
                paint.style = Paint.Style.FILL
                paint.textSize = 15f
                paint.color = android.graphics.Color.rgb(30, 41, 59)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("+", plusX, y + 52, paint)

                // Group B
                val startB = plusX + 18
                for (i in 0 until ir.secondaryQuantity.coerceAtMost(5)) {
                    val tx = startB + (i * 20)
                    if (bitmap != null) {
                        canvas.drawBitmap(bitmap, null, RectF(tx, y + 38, tx + tokenSize, y + 38 + tokenSize), printPaint)
                    } else {
                        paint.style = Paint.Style.FILL
                        paint.color = android.graphics.Color.rgb(30, 41, 59)
                        canvas.drawCircle(tx + tokenSize / 2, y + 38 + tokenSize / 2, 5.5f, paint)
                    }
                }

                // Equals and Answer Box
                val eqX = startB + (ir.secondaryQuantity.coerceAtMost(5) * 20) + 8
                canvas.drawText("=", eqX, y + 52, paint)

                paint.style = Paint.Style.STROKE
                val ansBox = RectF(eqX + 16, y + 36, eqX + 54, y + 64)
                canvas.drawRoundRect(ansBox, 4f, 4f, paint)
            }

            ActivityActionType.SUBTRACTION_CONCRETE -> {
                val tokenSize = 18f
                val total = ir.quantity.coerceAtMost(8)
                val subtractCount = ir.secondaryQuantity.coerceAtMost(total)
                for (i in 0 until total) {
                    val tx = x + 24 + (i * 20)
                    val ty = y + 38
                    if (bitmap != null) {
                        canvas.drawBitmap(bitmap, null, RectF(tx, ty, tx + tokenSize, ty + tokenSize), printPaint)
                    } else {
                        paint.style = Paint.Style.FILL
                        paint.color = android.graphics.Color.rgb(30, 41, 59)
                        canvas.drawCircle(tx + tokenSize / 2, ty + tokenSize / 2, 6f, paint)
                    }
                    if (i >= total - subtractCount) {
                        paint.style = Paint.Style.STROKE
                        paint.color = android.graphics.Color.rgb(220, 38, 38)
                        paint.strokeWidth = 1.8f
                        canvas.drawLine(tx, ty + tokenSize, tx + tokenSize, ty, paint)
                    }
                }
                val eqX = x + 24 + (total * 20) + 12
                paint.style = Paint.Style.FILL
                paint.textSize = 13f
                paint.color = android.graphics.Color.rgb(30, 41, 59)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("- $subtractCount = [   ]", eqX, y + 52, paint)
            }

            ActivityActionType.SEQUENCE_TRAIN -> {
                val base = ir.quantity.coerceAtLeast(1)
                val seq = listOf(base, base + 1, base + 2, base + 3)
                val boxW = 34f
                seq.forEachIndexed { sIdx, num ->
                    val bx = x + 30 + (sIdx * 42)
                    val isMissing = sIdx == 2
                    paint.style = Paint.Style.FILL
                    paint.color = if (isMissing) android.graphics.Color.rgb(240, 245, 255) else android.graphics.Color.rgb(248, 250, 253)
                    val rect = RectF(bx, y + 36, bx + boxW, y + 64)
                    canvas.drawRoundRect(rect, 4f, 4f, paint)
                    paint.style = Paint.Style.STROKE
                    paint.color = android.graphics.Color.rgb(180, 195, 215)
                    paint.strokeWidth = 1f
                    canvas.drawRoundRect(rect, 4f, 4f, paint)
                    paint.style = Paint.Style.FILL
                    paint.color = android.graphics.Color.rgb(30, 41, 59)
                    paint.textSize = 12f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText(if (isMissing) "?" else num.toString(), bx + boxW / 2, y + 53, paint)
                    paint.textAlign = Paint.Align.LEFT
                }
            }

            ActivityActionType.COMPARE_QUANTITY -> {
                val tokenSize = 16f
                for (i in 0 until ir.quantity.coerceAtMost(4)) {
                    val tx = x + 24 + (i * 18)
                    if (bitmap != null) canvas.drawBitmap(bitmap, null, RectF(tx, y + 38, tx + tokenSize, y + 38 + tokenSize), printPaint)
                }
                val midX = x + 110
                paint.style = Paint.Style.STROKE
                paint.color = android.graphics.Color.rgb(180, 195, 215)
                canvas.drawCircle(midX, y + 46, 11f, paint)
                paint.style = Paint.Style.FILL
                paint.textSize = 8.5f
                paint.color = android.graphics.Color.rgb(100, 115, 135)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("< = >", midX, y + 49, paint)
                paint.textAlign = Paint.Align.LEFT

                for (i in 0 until ir.secondaryQuantity.coerceAtLeast(1).coerceAtMost(4)) {
                    val tx = midX + 24 + (i * 18)
                    if (bitmap != null) canvas.drawBitmap(bitmap, null, RectF(tx, y + 38, tx + tokenSize, y + 38 + tokenSize), printPaint)
                }
            }

            ActivityActionType.OBJECT_CLASSIFY -> {
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, null, RectF(x + 24, y + 34, x + 56, y + 64), printPaint)
                }
                paint.style = Paint.Style.FILL
                paint.textSize = 11f
                paint.color = android.graphics.Color.rgb(30, 41, 59)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(ir.santaliWord, x + 66, y + 50, paint)

                paint.style = Paint.Style.STROKE
                paint.color = android.graphics.Color.rgb(180, 195, 215)
                val catBox1 = RectF(x + w - 160, y + 36, x + w - 90, y + 62)
                canvas.drawRoundRect(catBox1, 4f, 4f, paint)
                val catBox2 = RectF(x + w - 80, y + 36, x + w - 10, y + 62)
                canvas.drawRoundRect(catBox2, 4f, 4f, paint)
                paint.style = Paint.Style.FILL
                paint.textSize = 9f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("[ ] ᱫᱟᱨᱮ-ᱱᱟᱹᱲᱤ", x + w - 125, y + 52, paint)
                canvas.drawText("[ ] ᱡᱤᱭᱟᱹᱞᱤ", x + w - 45, y + 52, paint)
                paint.textAlign = Paint.Align.LEFT
            }

            ActivityActionType.AKSHAR_PHONICS_TRACE -> {
                val akshar = ir.targetAkshar.ifBlank { ir.santaliWord.take(1).ifBlank { "ᱚ" } }
                paint.style = Paint.Style.FILL
                paint.color = android.graphics.Color.rgb(20, 70, 140)
                paint.textSize = 24f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(akshar, x + 30, y + 56, paint)

                paint.color = android.graphics.Color.rgb(150, 165, 185)
                paint.textSize = 11f
                paint.typeface = Typeface.DEFAULT
                canvas.drawText("Practice: . . .   . . .   . . .   . . .", x + 80, y + 52, paint)

                paint.color = android.graphics.Color.rgb(80, 95, 120)
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("[ ${ir.santaliWord} - ${ir.hindiWord} ]", x + w - 140, y + 52, paint)
            }

            ActivityActionType.PICTURE_WORD_MATCH -> {
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, null, RectF(x + 24, y + 34, x + 56, y + 64), printPaint)
                }
                paint.style = Paint.Style.FILL
                paint.textSize = 11.5f
                paint.color = android.graphics.Color.rgb(30, 41, 59)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(ir.hindiWord, x + 66, y + 50, paint)

                paint.color = android.graphics.Color.rgb(180, 195, 215)
                paint.strokeWidth = 1f
                canvas.drawLine(x + 150, y + 46, x + w - 140, y + 46, paint)

                paint.color = android.graphics.Color.rgb(20, 70, 140)
                paint.textSize = 12.5f
                canvas.drawText(ir.santaliWord, x + w - 120, y + 50, paint)
            }

            ActivityActionType.FILL_MISSING_AKSHAR -> {
                val fullWord = ir.santaliWord.ifBlank { "ᱩᱞ" }
                val displayWord = if (fullWord.length > 1) "${fullWord.first()} [ _ ] ${fullWord.drop(2)}" else "[ _ ]"
                paint.style = Paint.Style.FILL
                paint.textSize = 13f
                paint.color = android.graphics.Color.rgb(20, 70, 140)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Word: $displayWord", x + 30, y + 52, paint)

                val options = ir.distractorOptions.ifEmpty { listOf("ᱲ", "ᱫ", "ᱛ") }
                paint.textSize = 10f
                paint.color = android.graphics.Color.rgb(80, 95, 120)
                canvas.drawText("Options: [ ${options.joinToString("   ")} ]", x + 160, y + 52, paint)
            }

            ActivityActionType.CIRCLE_THE_ANSWER -> {
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, null, RectF(x + 24, y + 34, x + 56, y + 64), printPaint)
                }
                val opts = ir.distractorOptions.ifEmpty { listOf(ir.correctValue, "ᱫᱟᱨᱮ", "ᱜᱟᱰᱟ") }
                opts.take(3).forEachIndexed { oIdx, opt ->
                    val ox = x + 70 + (oIdx * 80)
                    paint.style = Paint.Style.STROKE
                    paint.color = android.graphics.Color.rgb(180, 195, 215)
                    val oRect = RectF(ox, y + 36, ox + 72, y + 62)
                    canvas.drawRoundRect(oRect, 12f, 12f, paint)
                    paint.style = Paint.Style.FILL
                    paint.textSize = 10f
                    paint.color = android.graphics.Color.rgb(30, 41, 59)
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText(opt, ox + 36, y + 52, paint)
                    paint.textAlign = Paint.Align.LEFT
                }
            }

            ActivityActionType.PATTERN_RECOGNITION -> {
                val pTokenSize = 18f
                for (i in 0 until 4) {
                    val px = x + 30 + (i * 36)
                    val isAlt = i % 2 == 1
                    if (isAlt) {
                        paint.style = Paint.Style.STROKE
                        paint.color = android.graphics.Color.rgb(30, 41, 59)
                        canvas.drawCircle(px + 9, y + 48, 9f, paint)
                    } else if (bitmap != null) {
                        canvas.drawBitmap(bitmap, null, RectF(px, y + 38, px + pTokenSize, y + 38 + pTokenSize), printPaint)
                    }
                }
                val qBox = RectF(x + 30 + (4 * 36), y + 36, x + 30 + (4 * 36) + 30, y + 62)
                paint.style = Paint.Style.STROKE
                paint.color = android.graphics.Color.rgb(20, 70, 140)
                canvas.drawRoundRect(qBox, 4f, 4f, paint)
                paint.style = Paint.Style.FILL
                paint.textSize = 11f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("?", qBox.centerX(), y + 52, paint)
                paint.textAlign = Paint.Align.LEFT
            }

            ActivityActionType.MULTIPLICATION_GROUPS -> {
                val groups = ir.quantity.coerceIn(2, 5)
                val perGroup = ir.secondaryQuantity.coerceIn(1, 4)
                val groupWidth = (w - 120) / groups.toFloat()
                for (g in 0 until groups) {
                    val gx = x + 24 + (g * groupWidth)
                    paint.style = Paint.Style.STROKE
                    paint.color = android.graphics.Color.rgb(180, 195, 215)
                    paint.strokeWidth = 1f
                    val gRect = RectF(gx, y + 36, gx + groupWidth - 6, y + 66)
                    canvas.drawRoundRect(gRect, 6f, 6f, paint)

                    for (item in 0 until perGroup) {
                        val ix = gx + 4 + (item * 14)
                        if (bitmap != null) {
                            canvas.drawBitmap(bitmap, null, RectF(ix, y + 42, ix + 12f, y + 54f), printPaint)
                        } else {
                            paint.style = Paint.Style.FILL
                            paint.color = android.graphics.Color.rgb(30, 41, 59)
                            canvas.drawCircle(ix + 6f, y + 48f, 4f, paint)
                        }
                    }
                }
                val eqX = x + 24 + (groups * groupWidth) + 4
                paint.style = Paint.Style.FILL
                paint.textSize = 12f
                paint.color = android.graphics.Color.rgb(30, 41, 59)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("$groups × $perGroup = [   ]", eqX, y + 52, paint)
            }

            ActivityActionType.MONEY_CALCULATION -> {
                // First coin
                paint.style = Paint.Style.FILL
                paint.color = android.graphics.Color.rgb(235, 240, 250)
                canvas.drawCircle(x + 44, y + 48, 13f, paint)
                paint.style = Paint.Style.STROKE
                paint.color = android.graphics.Color.rgb(140, 155, 175)
                paint.strokeWidth = 1.5f
                canvas.drawCircle(x + 44, y + 48, 13f, paint)
                paint.style = Paint.Style.FILL
                paint.color = android.graphics.Color.rgb(30, 41, 59)
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("₹${ir.quantity}", x + 44, y + 52, paint)

                // Plus
                paint.textSize = 14f
                canvas.drawText("+", x + 72, y + 52, paint)

                // Second coin
                paint.style = Paint.Style.FILL
                paint.color = android.graphics.Color.rgb(235, 240, 250)
                canvas.drawCircle(x + 100, y + 48, 13f, paint)
                paint.style = Paint.Style.STROKE
                paint.color = android.graphics.Color.rgb(140, 155, 175)
                paint.strokeWidth = 1.5f
                canvas.drawCircle(x + 100, y + 48, 13f, paint)
                paint.style = Paint.Style.FILL
                paint.color = android.graphics.Color.rgb(30, 41, 59)
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("₹${ir.secondaryQuantity}", x + 100, y + 52, paint)

                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 12.5f
                canvas.drawText("=  [ ₹ _____ ]", x + 126, y + 52, paint)
            }

            ActivityActionType.SHAPE_RECOGNITION -> {
                val shapeKey = ir.primaryObjectKey
                paint.style = Paint.Style.STROKE
                paint.color = android.graphics.Color.rgb(20, 70, 140)
                paint.strokeWidth = 2f
                val cx = x + 46
                val cy = y + 48
                when {
                    shapeKey.contains("circle") || shapeKey.contains("sun") -> canvas.drawCircle(cx, cy, 13f, paint)
                    shapeKey.contains("triangle") -> {
                        val path = Path().apply {
                            moveTo(cx, cy - 13)
                            lineTo(cx + 13, cy + 13)
                            lineTo(cx - 13, cy + 13)
                            close()
                        }
                        canvas.drawPath(path, paint)
                    }
                    else -> canvas.drawRect(cx - 13, cy - 13, cx + 13, cy + 13, paint)
                }
                paint.style = Paint.Style.FILL
                paint.textSize = 11f
                paint.color = android.graphics.Color.rgb(30, 41, 59)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val opts = ir.distractorOptions.ifEmpty { listOf("ᱜᱩᱞᱟᱹᱭ (Circle)", "ᱪᱟᱹᱣᱠᱟᱹ (Square)", "ᱛᱮᱠᱷᱩᱬ (Triangle)") }
                canvas.drawText("Shape: [ ${opts.take(3).joinToString("  /  ")} ]", x + 76, y + 52, paint)
            }

            else -> {
                // Standard visual with label / Phrase
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, null, RectF(x + 24, y + 34, x + 58, y + 64), printPaint)
                }
                val phrase = ir.bilingualPhraseSantali.ifBlank { "${ir.santaliWord} (${ir.hindiWord})" }
                paint.style = Paint.Style.FILL
                paint.textSize = 12.5f
                paint.color = android.graphics.Color.rgb(20, 70, 140)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(phrase, x + 70, y + 46, paint)

                paint.textSize = 9.5f
                paint.color = android.graphics.Color.rgb(100, 115, 135)
                val sub = ir.bilingualPhraseHindi.ifBlank { ir.englishWord }
                canvas.drawText(sub, x + 70, y + 60, paint)
            }
        }
    }

    private fun loadBitmapFromAsset(context: Context, assetPath: String): Bitmap? {
        return org.tribetalk.fln.image.FlnImageLoader.loadAndroidBitmap(context, assetPath)
    }
}

/**
 * Interactive Jetpack Compose Composed Scene.
 * Dynamically arranges SVG primitives for counting, addition, and linguistic phrase progression.
 */
@Composable
fun ComposedActivityScene(
    activityIR: ActivityIR,
    onAnswerSelected: (String) -> Unit,
    onNextPhraseRequested: (() -> Unit)? = null,
    onSpeakSantali: (() -> Unit)? = null,
    onSpeakHindi: (() -> Unit)? = null,
    onSpeakSantaliWord: ((String) -> Unit)? = null,
    onSpeakHindiWord: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val metadata = remember(activityIR.primaryObjectKey) {
        SvgCorpusRegistry.get(activityIR.primaryObjectKey)
    }

    val relativePath = metadata?.relativeFilePath ?: "fln_svg_corpus/plants/mango.svg"
    var tappedCount by remember(activityIR.id) { mutableIntStateOf(0) }
    var selectedAnswer by remember(activityIR.id) { mutableStateOf<String?>(null) }
    val isSolved = selectedAnswer == activityIR.correctValue

    var showPhraseDialog by remember(activityIR.id) { mutableStateOf(false) }

    if (showPhraseDialog) {
        PhraseDiscoveryDialog(
            activityIR = activityIR,
            onAdvanceToSentence = {
                onNextPhraseRequested?.invoke()
                showPhraseDialog = false
            },
            onSpeakSantali = { text -> onSpeakSantaliWord?.invoke(text) ?: onSpeakSantali?.invoke() },
            onSpeakHindi = { text -> onSpeakHindiWord?.invoke(text) ?: onSpeakHindi?.invoke() },
            onDismiss = { showPhraseDialog = false }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, if (isSolved) EduPrimary else MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: NIPUN Competency & Linguistic Tier Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EduPrimaryLight
                ) {
                    Text(
                        text = "${activityIR.nipunCompetencyCode} • ${activityIR.actionType.displayName}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = EduPrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EduAmberLight
                ) {
                    Text(
                        text = activityIR.linguisticTier.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = EduAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bilingual Teacher Instruction & Dual Voice Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activityIR.instructionSantali.ifBlank { metadata?.santaliName ?: "" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = activityIR.instructionHindi.ifBlank { metadata?.hindiName ?: "" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (onSpeakSantali != null) {
                        FilledIconButton(
                            onClick = onSpeakSantali,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = EduPrimaryLight)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Hear Santali",
                                tint = EduPrimaryDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (onSpeakHindi != null) {
                        FilledIconButton(
                            onClick = onSpeakHindi,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = EduIndigoLight)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Hear Hindi",
                                tint = EduIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Visual Composition Arena: Duplicated SVG primitives in Ten-Frame / Clusters
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                when (activityIR.actionType) {
                    ActivityActionType.COUNT_AND_SELECT,
                    ActivityActionType.COUNT_AND_MATCH -> {
                        // Interactive Tap-To-Count Ten-Frame Manipulative Grid with Live Progress
                        val total = activityIR.quantity.coerceIn(1, 20)
                        val columns = if (total <= 5) total else 5
                        val tokenSize = when {
                            total <= 5 -> 54.dp
                            total <= 10 -> 44.dp
                            else -> 36.dp
                        }
                        val imageSize = when {
                            total <= 5 -> 38.dp
                            total <= 10 -> 30.dp
                            else -> 24.dp
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Live Count Progress Pill with Tap Counter & Reset
                            Surface(
                                color = if (tappedCount == total) EduPrimaryLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (tappedCount == total) EduPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (tappedCount == total) Icons.Rounded.CheckCircle else Icons.Rounded.TouchApp,
                                        contentDescription = null,
                                        tint = if (tappedCount == total) EduPrimaryDark else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "ᱞᱮᱠᱷᱟ: $tappedCount / $total  •  गिना गया: $tappedCount / $total",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tappedCount == total) EduPrimaryDark else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (tappedCount > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "↺ ᱫᱚᱦᱲᱟ",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { tappedCount = 0 }
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(columns),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.wrapContentSize()
                            ) {
                                items(total) { index ->
                                    val isTapped = index < tappedCount
                                    val scale by animateFloatAsState(
                                        targetValue = if (isTapped) 1.12f else 1.0f,
                                        animationSpec = spring(),
                                        label = "scale"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(tokenSize)
                                            .scale(scale)
                                            .clip(CircleShape)
                                            .background(if (isTapped) EduPrimaryLight else Color.White)
                                            .border(
                                                1.5.dp,
                                                if (isTapped) EduPrimary else MaterialTheme.colorScheme.outline,
                                                CircleShape
                                            )
                                            .clickable {
                                                tappedCount = if (index == tappedCount - 1) {
                                                    index
                                                } else {
                                                    index + 1
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data("file:///android_asset/$relativePath")
                                                .decoderFactory(SvgDecoder.Factory())
                                                .build(),
                                            contentDescription = metadata?.englishName,
                                            modifier = Modifier.size(imageSize)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ActivityActionType.ADDITION_CONCRETE -> {
                        // Visual Addition Equation
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Group A
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(activityIR.quantity.coerceAtMost(5)) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data("file:///android_asset/$relativePath")
                                            .decoderFactory(SvgDecoder.Factory())
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Text(text = "+", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = EduPrimaryDark)

                            // Group B
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(activityIR.secondaryQuantity.coerceAtMost(5)) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data("file:///android_asset/$relativePath")
                                            .decoderFactory(SvgDecoder.Factory())
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Text(text = "=", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = EduPrimaryDark)

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.5.dp, EduPrimary),
                                color = Color.White,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = selectedAnswer ?: "?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = if (isSolved) EduPrimaryDark else Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    ActivityActionType.SUBTRACTION_CONCRETE -> {
                        // Visual Subtraction with visual crossed-out badges
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val total = activityIR.quantity.coerceIn(1, 8)
                            val sub = activityIR.secondaryQuantity.coerceIn(1, total)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(total) { idx ->
                                    val isSubtracted = idx >= total - sub
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSubtracted) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data("file:///android_asset/$relativePath")
                                                .decoderFactory(SvgDecoder.Factory())
                                                .build(),
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp),
                                            alpha = if (isSubtracted) 0.4f else 1.0f
                                        )
                                        if (isSubtracted) {
                                            Text("✕", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                        }
                                    }
                                }
                            }

                            Text(text = "- $sub =", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = EduPrimaryDark)

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.5.dp, EduPrimary),
                                color = Color.White,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = selectedAnswer ?: "?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = if (isSolved) EduPrimaryDark else Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    ActivityActionType.MULTIPLICATION_GROUPS -> {
                        // Visual Group Array
                        val groups = activityIR.quantity.coerceIn(2, 4)
                        val perGroup = activityIR.secondaryQuantity.coerceIn(1, 4)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeat(groups) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, EduPrimary.copy(alpha = 0.5f)),
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        repeat(perGroup) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data("file:///android_asset/$relativePath")
                                                    .decoderFactory(SvgDecoder.Factory())
                                                    .build(),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Text(text = "= [ ? ]", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EduPrimaryDark)
                        }
                    }

                    ActivityActionType.MONEY_CALCULATION -> {
                        // Coin Addition Display
                        val c1Key = activityIR.primaryObjectKey.ifBlank { "coin_1" }
                        val c2Key = activityIR.secondaryObjectKey?.ifBlank { "coin_2" } ?: "coin_2"
                        val c1Path = SvgCorpusRegistry.get(c1Key)?.relativeFilePath ?: "fln_svg_corpus/math/$c1Key.svg"
                        val c2Path = SvgCorpusRegistry.get(c2Key)?.relativeFilePath ?: "fln_svg_corpus/math/$c2Key.svg"

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data("file:///android_asset/$c1Path")
                                    .decoderFactory(SvgDecoder.Factory())
                                    .build(),
                                contentDescription = c1Key,
                                modifier = Modifier.size(46.dp)
                            )

                            Text(text = "+", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = EduPrimaryDark)

                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data("file:///android_asset/$c2Path")
                                    .decoderFactory(SvgDecoder.Factory())
                                    .build(),
                                contentDescription = c2Key,
                                modifier = Modifier.size(46.dp)
                            )

                            Text(text = "=", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = EduPrimaryDark)

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.5.dp, EduPrimary),
                                color = Color.White,
                                modifier = Modifier.height(44.dp).padding(horizontal = 8.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                                    Text(
                                        text = if (selectedAnswer != null) "₹$selectedAnswer" else "₹ [ ? ]",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (isSolved) EduPrimaryDark else Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    ActivityActionType.SHAPE_RECOGNITION -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data("file:///android_asset/$relativePath")
                                .decoderFactory(SvgDecoder.Factory())
                                .build(),
                            contentDescription = metadata?.englishName,
                            modifier = Modifier.size(100.dp)
                        )
                    }

                    else -> {
                        // Single Hero Vector Display
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data("file:///android_asset/$relativePath")
                                .decoderFactory(SvgDecoder.Factory())
                                .build(),
                            contentDescription = metadata?.englishName,
                            modifier = Modifier.size(110.dp)
                        )
                    }
                }
            }

            // Distractor Option Buttons (Spacious 2x2 Grid preventing squeezing and text truncation)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activityIR.distractorOptions.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowOptions.forEach { opt ->
                            val isSelected = selectedAnswer == opt
                            val isCorrect = opt == activityIR.correctValue
                            val btnColor = when {
                                isSelected && isCorrect -> EduPrimary
                                isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            FilledTonalButton(
                                onClick = {
                                    selectedAnswer = opt
                                    onAnswerSelected(opt)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = btnColor,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Progression Banner: On success, unlock "Next Phrase Challenge"!
            if (isSolved && onNextPhraseRequested != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showPhraseDialog = true },
                    color = EduPrimaryLight,
                    border = BorderStroke(1.dp, EduPrimary)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = EduPrimaryDark,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Very Good! (ᱟᱹᱰᱤ ᱱᱟᱯᱟᱭ)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EduPrimaryDark
                                )
                                Text(
                                    text = "Tap to explore Next Phrase: ${activityIR.bilingualPhraseSantali.ifBlank { "Phrase Challenge" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = EduAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Phrase Discovery Dialog.
 * Breaks down phrases into individual clickable word tokens and bridges to full fluency sentences.
 */
@Composable
fun PhraseDiscoveryDialog(
    activityIR: ActivityIR,
    onAdvanceToSentence: () -> Unit,
    onSpeakSantali: (String) -> Unit,
    onSpeakHindi: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val metadata = remember(activityIR.primaryObjectKey) {
        SvgCorpusRegistry.get(activityIR.primaryObjectKey)
    }
    val relativePath = metadata?.relativeFilePath ?: "fln_svg_corpus/plants/mango.svg"

    val progression = remember(activityIR.primaryObjectKey) {
        LearnerProgressionEngine.getContent(activityIR.primaryObjectKey)
    }

    val phraseSantali = progression?.phraseSantali ?: activityIR.bilingualPhraseSantali.ifBlank { activityIR.santaliWord }
    val phraseHindi = progression?.phraseHindi ?: activityIR.bilingualPhraseHindi.ifBlank { activityIR.hindiWord }
    val phraseEnglish = progression?.phraseEnglish ?: activityIR.phraseEnglishGloss.ifBlank { activityIR.englishWord }
    val sentenceSantali = progression?.sentenceSantali ?: activityIR.sentenceSantali

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, EduPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tier Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EduAmberLight
                ) {
                    Text(
                        text = "PHRASE DISCOVERY • ᱟᱹᱲᱟᱹ ᱡᱚᱲ",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EduAmber
                    )
                }

                // Hero Illustration
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data("file:///android_asset/$relativePath")
                        .decoderFactory(SvgDecoder.Factory())
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )

                // Hero Phrase in Bold Ol Chiki
                Text(
                    text = phraseSantali,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // Interactive Tokenized Word Chips (Tap to learn each word)
                val words = phraseSantali.split(" ").filter { it.isNotBlank() }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    words.forEach { word ->
                        FilledTonalButton(
                            onClick = { onSpeakSantali(word) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = EduPrimaryLight)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = null,
                                tint = EduPrimaryDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = word, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = EduPrimaryDark)
                        }
                    }
                }

                // Bilingual Meaning
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = phraseHindi,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = EduIndigo
                    )
                    Text(
                        text = phraseEnglish,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Dual Voice Bar for Full Phrase
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onSpeakSantali(phraseSantali) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EduPrimary)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Santali", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { onSpeakHindi(phraseHindi) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hindi", fontSize = 12.sp)
                    }
                }

                // Sentence Step-Up Ladder Button
                if (sentenceSantali.isNotBlank()) {
                    Button(
                        onClick = {
                            onAdvanceToSentence()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EduAmber)
                    ) {
                        Icon(imageVector = Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Step Up to Sentence", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
