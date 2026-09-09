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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.tribetalk.core.TranslationExchange
import org.tribetalk.ui.theme.DarkBorder
import org.tribetalk.ui.theme.DarkBorderGreen
import org.tribetalk.ui.theme.DarkCard
import org.tribetalk.ui.theme.EmeraldContainerDark
import org.tribetalk.ui.theme.EmeraldGreen
import org.tribetalk.ui.theme.EmeraldMint
import org.tribetalk.ui.theme.PureBlack
import org.tribetalk.ui.theme.PureWhite
import org.tribetalk.ui.theme.WhiteSecondary

/**
 * Message card rendering a single speech translation exchange.
 * High-contrast Green, White, and Black design.
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
                color = DarkBorderGreen,
                shape = RoundedCornerShape(20.dp)
            ),
        color = DarkCard,
        tonalElevation = 4.dp
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
                    fontWeight = FontWeight.Medium,
                    color = WhiteSecondary
                )
                if (exchange.totalLatencyMs > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldContainerDark
                    ) {
                        Text(
                            text = "${exchange.totalLatencyMs.toInt()} ms",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldMint,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = exchange.sourceText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = PureWhite,
                fontSize = 18.sp
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 2.dp),
                color = DarkBorder
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
                    color = EmeraldGreen
                )
            }

            Text(
                text = exchange.targetText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = EmeraldGreen,
                fontSize = 21.sp,
                lineHeight = 28.sp
            )

            // Teacher Phonetic Pronunciation Guide for Santali Ol Chiki output
            if (exchange.targetLanguage == "sat" && exchange.targetText.isNotEmpty()) {
                val phonetics = org.tribetalk.core.TribeTalkTranslator.olChikiToSpeechPhonetics(exchange.targetText)
                if (phonetics.isNotBlank() && phonetics != exchange.targetText) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PureBlack.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkBorderGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Teacher Guide:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldMint
                            )
                            Text(
                                text = "[ $phonetics ]",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = PureWhite
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
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldContainerDark
                ) {
                    Text(
                        text = "100% Offline ONNX",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldMint,
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
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Audio Playback
                    if (exchange.targetText.isNotEmpty()) {
                        IconButton(
                            onClick = onPlayAudio,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Play Pronunciation",
                                tint = PureBlack,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
