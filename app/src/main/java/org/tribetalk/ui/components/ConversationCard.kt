package org.tribetalk.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.core.content.ContextCompat
import org.tribetalk.core.TranslationExchange
import org.tribetalk.ui.theme.*

/**
 * Message card rendering a single speech translation exchange.
 * Clean, friendly educational design.
 */
@Composable
fun ConversationCard(
    exchange: TranslationExchange,
    onPlayAudio: () -> Unit,
    isPlaying: Boolean,
    onMakeFlashcard: ((String) -> Unit)? = null,
    onGenerateWorksheet: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(18.dp)
            ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    fontWeight = FontWeight.Medium,
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
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = exchange.sourceText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 17.sp
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 2.dp),
                color = MaterialTheme.colorScheme.outlineVariant
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = exchange.targetText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                lineHeight = 28.sp
            )

            // Teacher Phonetic Pronunciation Guide for Santali Ol Chiki output
            if (exchange.targetLanguage == "sat" && exchange.targetText.isNotEmpty()) {
                val phonetics = org.tribetalk.core.TribeTalkTranslator.olChikiToSpeechPhonetics(exchange.targetText)
                if (phonetics.isNotBlank() && phonetics != exchange.targetText) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EduAmberLight,
                        border = BorderStroke(0.8.dp, EduAmber.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "बोलें:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EduAmber
                            )
                            Text(
                                text = "[ $phonetics ]",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = EduAmber
                            )
                        }
                    }
                }
            }

            // Bottom Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Offline verification badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EduPrimaryLight
                ) {
                    Text(
                        text = "100% Offline ONNX",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EduPrimaryDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy Translation",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Audio replay button
                    IconButton(
                        onClick = onPlayAudio,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) EduPrimaryLight else MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Replay Audio",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // FLN Classroom Action Bar: Instant Flashcard or Matching Worksheet from spoken instruction
            if (onMakeFlashcard != null || onGenerateWorksheet != null) {
                val instructionText = if (exchange.sourceLanguage == "hi") exchange.sourceText else exchange.targetText
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onMakeFlashcard != null) {
                        FilledTonalButton(
                            onClick = { onMakeFlashcard(instructionText) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = EduAmberLight,
                                contentColor = EduAmber
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Style,
                                contentDescription = "Flashcard",
                                modifier = Modifier.size(16.dp),
                                tint = EduAmber
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "फ़्लैशकार्ड",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = EduAmber
                            )
                        }
                    }

                    if (onGenerateWorksheet != null) {
                        FilledTonalButton(
                            onClick = { onGenerateWorksheet(instructionText) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = EduIndigoLight,
                                contentColor = EduIndigo
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Description,
                                contentDescription = "Worksheet",
                                modifier = Modifier.size(16.dp),
                                tint = EduIndigo
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "वर्कशीट बनाएं",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = EduIndigo
                            )
                        }
                    }
                }
            }
        }
    }
}
