package org.tribetalk.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.flashcards.Flashcard
import org.tribetalk.flashcards.FlashcardVisual
import org.tribetalk.ui.theme.*

/**
 * Picture-First Bilingual Flashcard Component.
 *
 * Sequence:
 * 1. Visual Prompt (Image -> Fallback Emoji -> Vector Graphic)
 * 2. Prominent Hindi text
 * 3. Interactive Reveal (Tap card or button to display Santali Ol Chiki + Latin + Pronunciation)
 * 4. Card-level "Hear Santali" audio with rapid-tap protection
 */
@Composable
fun PictureFirstCardView(
    card: Flashcard,
    isRevealed: Boolean,
    isPlayingAudio: Boolean,
    onRevealToggle: () -> Unit,
    onPlayAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var lastAudioTapTime by remember { mutableLongStateOf(0L) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clickable { onRevealToggle() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Category Pill & Domain/Grade Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = EduPrimaryLight,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (card.topic.isNotBlank()) card.topic else "FLN Flashcard",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EduPrimaryDark,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    color = EduIndigoLight,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = "NIPUN",
                            modifier = Modifier.size(14.dp),
                            tint = EduIndigo
                        )
                        Text(
                            text = if (card.grade.isNotBlank()) card.grade else "FLN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EduIndigo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Picture-First Visual Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                EduPrimary.copy(alpha = 0.08f),
                                EduPrimary.copy(alpha = 0.18f)
                            )
                        )
                    )
                    .border(1.dp, EduPrimary.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                FlashcardVisual(
                    imageUri = card.imageUri,
                    iconType = card.iconType,
                    imageEmoji = card.imageEmoji,
                    contentDescription = card.hindiText,
                    maxImageSize = 96.dp,
                    emojiFontSize = 72.sp,
                    vectorGraphicSize = 80.dp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Hindi Text (Prompt)
            Text(
                text = card.hindiText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            if (card.englishGloss.isNotBlank()) {
                Text(
                    text = card.englishGloss,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Interactive Reveal Area
            if (!isRevealed) {
                Button(
                    onClick = onRevealToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EduPrimaryLight,
                        contentColor = EduPrimaryDark
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Visibility,
                        contentDescription = "Reveal",
                        modifier = Modifier.size(18.dp),
                        tint = EduPrimaryDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tap to Reveal Santali (ᱥᱟᱱᱛᱟᱲᱤ)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = EduPrimaryDark
                    )
                }
            } else {
                Surface(
                    color = EduPrimaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, EduPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Ol Chiki script
                        val olChiki = card.santaliOlChiki
                        if (!olChiki.isNullOrBlank()) {
                            Text(
                                text = olChiki,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = EduPrimaryDark,
                                textAlign = TextAlign.Center,
                                letterSpacing = 1.sp
                            )
                        }

                        // Latin Santali
                        if (card.santaliText.isNotBlank()) {
                            Text(
                                text = card.santaliText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Teacher Phonetic Guide Pill
                        if (!card.phoneticGuide.isNullOrBlank()) {
                            Surface(
                                color = EduAmberLight,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, EduAmber.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.RecordVoiceOver,
                                        contentDescription = "Pronounce",
                                        modifier = Modifier.size(14.dp),
                                        tint = EduAmber
                                    )
                                    Text(
                                        text = "उच्चारण: ${card.phoneticGuide}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EduAmber
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Dedicated "🔊 Hear Santali" Audio Button
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    if (!isPlayingAudio && now - lastAudioTapTime > 600) {
                        lastAudioTapTime = now
                        onPlayAudio()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlayingAudio) EduPrimaryDark else EduPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = "Hear Santali",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPlayingAudio) "Speaking Santali..." else "🔊 Hear Santali",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
