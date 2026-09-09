package org.tribetalk.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.fln.model.FlnCard
import org.tribetalk.ui.theme.DarkBorderGreen
import org.tribetalk.ui.theme.DarkCard
import org.tribetalk.ui.theme.DarkSurface
import org.tribetalk.ui.theme.EmeraldContainerDark
import org.tribetalk.ui.theme.EmeraldGreen
import org.tribetalk.ui.theme.EmeraldMint
import org.tribetalk.ui.theme.PureBlack
import org.tribetalk.ui.theme.PureWhite
import org.tribetalk.ui.theme.WhiteSecondary

/**
 * Interactive bilingual flashcard view aligned with NIPUN Bharat.
 * High-contrast Green, White, and Black styling with electric emerald Ol Chiki script.
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
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        ),
        border = BorderStroke(
            1.5.dp,
            DarkBorderGreen
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Category & NIPUN Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = EmeraldContainerDark,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = card.category,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldMint,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    color = PureBlack,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkBorderGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = "NIPUN",
                            modifier = Modifier.size(14.dp),
                            tint = EmeraldGreen
                        )
                        Text(
                            text = card.nipunCode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Central Graphic Box
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .border(
                        2.dp,
                        EmeraldGreen.copy(alpha = 0.4f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                FlnVectorGraphic(
                    iconType = card.iconType,
                    size = 85.dp,
                    numeralValue = card.numeralValue,
                    tint = EmeraldGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Standard Hindi Representation (Crisp Pure White)
            Text(
                text = card.hindiText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                textAlign = TextAlign.Center
            )

            Text(
                text = card.englishGloss,
                style = MaterialTheme.typography.bodySmall,
                color = WhiteSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quiz Hidden Mode vs Revealed
            if (isQuizMode && !isRevealed) {
                Button(
                    onClick = onRevealToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Visibility,
                        contentDescription = "Reveal",
                        modifier = Modifier.size(18.dp),
                        tint = PureBlack
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tap to Reveal Santali Translation",
                        color = PureBlack,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                // Santali Ol Chiki Script (Vibrant Electric Green)
                Surface(
                    color = PureBlack,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DarkBorderGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = card.santaliOlChiki,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreen,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Teacher Phonetic Guide (Crucial for non-native Hindi teachers)
                        Surface(
                            color = EmeraldContainerDark,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RecordVoiceOver,
                                    contentDescription = "Pronunciation",
                                    modifier = Modifier.size(14.dp),
                                    tint = EmeraldMint
                                )
                                Text(
                                    text = "Teacher's Guide: ${card.teacherPhoneticGuide}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldMint
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Playback Button (Electric Green on Black)
            Button(
                onClick = onPlayAudio,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlayingAudio) EmeraldMint else EmeraldGreen
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = "Speak Santali",
                    modifier = Modifier.size(20.dp),
                    tint = PureBlack
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPlayingAudio) "Pronouncing..." else "Listen Santali Pronunciation",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = PureBlack
                )
            }
        }
    }
}
