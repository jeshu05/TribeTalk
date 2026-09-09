package org.tribetalk.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.fln.model.FlnCard
import org.tribetalk.ui.theme.*

/**
 * Interactive bilingual flashcard view aligned with NIPUN Bharat.
 * Clean, modern educational card inspired by Quizlet and Duolingo.
 */
@Composable
fun FlnCardView(
    card: FlnCard,
    isQuizMode: Boolean,
    isRevealed: Boolean,
    isPlayingAudio: Boolean,
    onRevealToggle: () -> Unit,
    onPlayAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Category Pill & NIPUN Target Code
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
                        text = card.category,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EduPrimaryDark,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    color = EduIndigoLight,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = "NIPUN",
                            modifier = Modifier.size(15.dp),
                            tint = EduIndigo
                        )
                        Text(
                            text = card.nipunCode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EduIndigo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Central Graphic Box with gentle soft circular backdrop
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .clip(CircleShape)
                    .background(EduPrimaryContainer)
                    .border(
                        1.5.dp,
                        EduPrimary.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                FlnVectorGraphic(
                    iconType = card.iconType,
                    size = 80.dp,
                    numeralValue = card.numeralValue,
                    tint = EduPrimary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Standard Hindi Representation (Deep Slate, crystal clear)
            Text(
                text = card.hindiText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = card.englishGloss,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quiz Hidden Mode vs Revealed
            if (isQuizMode && !isRevealed) {
                Button(
                    onClick = onRevealToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EduPrimaryLight,
                        contentColor = EduPrimaryDark
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Visibility,
                        contentDescription = "Reveal",
                        modifier = Modifier.size(18.dp),
                        tint = EduPrimaryDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tap to Reveal Santali Translation",
                        color = EduPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                // Santali Ol Chiki Script Card
                Surface(
                    color = EduPrimaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, EduPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = card.santaliOlChiki,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = EduPrimaryDark,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Teacher Phonetic Guide (Warm Amber Pill)
                        Surface(
                            color = EduAmberLight,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, EduAmber.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RecordVoiceOver,
                                    contentDescription = "Pronunciation",
                                    modifier = Modifier.size(15.dp),
                                    tint = EduAmber
                                )
                                Text(
                                    text = "कक्षा में बोलें: ${card.teacherPhoneticGuide}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EduAmber
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Audio Playback Button
            Button(
                onClick = onPlayAudio,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlayingAudio) EduPrimaryDark else EduPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = "Speak Santali",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPlayingAudio) "Pronouncing..." else "Listen Santali Pronunciation",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
