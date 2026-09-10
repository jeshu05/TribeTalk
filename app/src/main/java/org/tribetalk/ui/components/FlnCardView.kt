package org.tribetalk.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.fln.model.FlnCard
import org.tribetalk.ui.theme.*

/**
 * Kid-Centric 3D interactive flashcard view aligned with NIPUN Bharat.
 * Features a real 3D card-flip mechanism, spring touch physics, themed pastel toy card palettes,
 * and charming procedural cartoon vector art designed for children.
 */
@Composable
fun FlnCardView(
    card: FlnCard,
    isQuizMode: Boolean,
    isRevealed: Boolean,
    isPlayingAudio: Boolean,
    onRevealToggle: () -> Unit,
    onPlayAudio: () -> Unit,
    modifier: Modifier = Modifier,
    illustrationSize: Dp = 130.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile Bouncy Scale Physics
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_touch_scale"
    )

    // True 3D Flip Rotation (0 deg Front -> 180 deg Back)
    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "3d_card_flip"
    )

    val isBackFace = rotation > 90f
    val density = LocalDensity.current.density

    // Determine themed playful pastel palette based on category
    val (cardBg, cardBorder, accentTint, bottomBorderColor) = when (card.category.lowercase()) {
        "animals" -> CardPalette(
            bg = Color(0xFFFFFBEB), // Sunny Warm Apricot
            border = Color(0xFFFDE68A),
            accent = Color(0xFFD97706),
            bottomBorder = Color(0xFFF59E0B)
        )
        "nature" -> CardPalette(
            bg = Color(0xFFF0FDF4), // Fresh Garden Mint
            border = Color(0xFFBBF7D0),
            accent = Color(0xFF16A34A),
            bottomBorder = Color(0xFF4ADE80)
        )
        "numbers" -> CardPalette(
            bg = Color(0xFFF0F9FF), // Cheerful Sky Blue
            border = Color(0xFFBAE6FD),
            accent = Color(0xFF0284C7),
            bottomBorder = Color(0xFF38BDF8)
        )
        "school" -> CardPalette(
            bg = Color(0xFFFAF5FF), // Playful Lavender
            border = Color(0xFFE9D5FF),
            accent = Color(0xFF7C3AED),
            bottomBorder = Color(0xFFA855F7)
        )
        else -> CardPalette(
            bg = Color(0xFFF8FAFC),
            border = Color(0xFFE2E8F0),
            accent = EduPrimary,
            bottomBorder = EduPrimaryDark
        )
    }

    Card(
        modifier = modifier
            .scale(cardScale)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onRevealToggle
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(2.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // Duolingo-style 3D solid shadow bottom border indicator
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp) // Bottom pressable toy edge
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        // Flip the content when viewing the back face so text isn't mirrored
                        if (isBackFace) {
                            rotationY = 180f
                        }
                    }
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                if (!isBackFace) {
                    // =========================================================
                    // FRONT FACE: Illustration + Hindi + Tap invitation
                    // =========================================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = accentTint.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = card.category,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = accentTint,
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
                                        text = card.nipunCode,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EduIndigo
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Big Cute Cartoon Illustration Container with Soft Radial Glow
                        Box(
                            modifier = Modifier
                                .size(illustrationSize + 24.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White,
                                            accentTint.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                                .border(2.dp, accentTint.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            FlnVectorGraphic(
                                iconType = card.iconType,
                                size = illustrationSize,
                                numeralValue = card.numeralValue,
                                tint = accentTint
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Playful Hindi Title
                        Text(
                            text = card.hindiText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        // English Gloss
                        Text(
                            text = card.englishGloss,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Kid-friendly Bouncy Tap Call-to-Action Pill
                        Surface(
                            color = accentTint.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, accentTint.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TouchApp,
                                    contentDescription = "Tap",
                                    modifier = Modifier.size(18.dp),
                                    tint = accentTint
                                )
                                Text(
                                    text = "टैप करो संथाली देखने के लिए! 👆",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = accentTint
                                )
                            }
                        }
                    }
                } else {
                    // =========================================================
                    // BACK FACE: Ol Chiki Discovery + Phonetics + Audio Button
                    // =========================================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Header Pill
                        Surface(
                            color = EduPrimaryLight,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = EduPrimaryDark
                                )
                                Text(
                                    text = "ᱥᱟᱱᱛᱟᱲᱤ ᱪᱤᱠᱤ (Santali)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EduPrimaryDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Giant Authentic Ol Chiki Characters
                        Text(
                            text = card.santaliOlChiki,
                            style = MaterialTheme.typography.headlineLarge,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EduPrimary,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Hindi Reference Subtitle
                        Text(
                            text = "${card.hindiText} (${card.englishGloss})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Teacher Phonics Speech Bubble (Warm Amber)
                        Surface(
                            color = EduAmberLight,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, EduAmber.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RecordVoiceOver,
                                    contentDescription = "Pronunciation",
                                    modifier = Modifier.size(20.dp),
                                    tint = EduAmber
                                )
                                Column {
                                    Text(
                                        text = "ऐसे बोलें (Pronunciation):",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EduAmber.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = card.teacherPhoneticGuide,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EduAmber
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Big Tactile Soundwave Button (Listen & Speak)
                        Button(
                            onClick = onPlayAudio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlayingAudio) EduPrimaryDark else EduPrimary,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 1.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Speak Santali",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isPlayingAudio) "बोल रहा है... 🔊" else "सुनो और बोलो! 🔊",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tap to Flip Back Return Prompt
                        Text(
                            text = "🔄 टैप करके वापस पलटें",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private data class CardPalette(
    val bg: Color,
    val border: Color,
    val accent: Color,
    val bottomBorder: Color
)
