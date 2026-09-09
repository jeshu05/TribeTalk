package org.tribetalk.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.tribetalk.core.TranslationExchange

/**
 * Message card rendering a single speech translation exchange.
 * Features vernacular typography, audio playback trigger, and latency metrics.
 * Strictly uses Material vector icons without any unicode emojis.
 */
@Composable
fun ConversationCard(
    exchange: TranslationExchange,
    onPlayAudio: () -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Source Text Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (exchange.sourceLanguage == "hi") "हिन्दी (Hindi)" else "ᱥᱟᱱᱛᱟᱲᱤ (Santali)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (exchange.totalLatencyMs > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${exchange.totalLatencyMs.toInt()} ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = exchange.sourceText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 17.sp
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 2.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )

            // Target Translated Text Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (exchange.targetLanguage == "sat") "ᱥᱟᱱᱛᱟᱲᱤ (Santali Translation)" else "हिन्दी (Hindi Translation)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Text(
                text = exchange.targetText,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 20.sp,
                lineHeight = 28.sp
            )

            // Bottom Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Offline verification badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Offline ONNX",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Playback & Copy Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Copy translated text
                    IconButton(
                        onClick = {
                            val clipboard = ContextCompat.getSystemService(context, ClipboardManager::class.java)
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Translation", exchange.targetText))
                            Toast.makeText(context, "Translation copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy text",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Audio Playback
                    if (exchange.targetText.isNotEmpty()) {
                        IconButton(
                            onClick = onPlayAudio,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPlaying) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.secondaryContainer
                                )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.GraphicEq else Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Play synthesized speech",
                                tint = if (isPlaying) MaterialTheme.colorScheme.onSecondary
                                       else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
