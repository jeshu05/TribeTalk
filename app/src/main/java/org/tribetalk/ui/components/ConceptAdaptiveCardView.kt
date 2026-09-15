package org.tribetalk.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import org.tribetalk.fln.image.rememberFlnImage
import org.tribetalk.fln.model.*
import org.tribetalk.fln.pipeline.*
import org.tribetalk.fln.repository.FlnCurriculumRepository
import org.tribetalk.ui.theme.*

/**
 * World-class Concept-Adaptive Bilingual Flashcard Component.
 * Completely free of emoji characters.
 * Supports:
 * 1. 3D Perspective Card Flip (Front: Ol Chiki, Illustration, Dual Voice; Back: Devanagari Hindi, Sentence, Worksheet Action)
 * 2. Hands-On Digital Manipulative (Interactive Activity IR Scene Composer, Ten-Frames, Object Clusters)
 * 3. Challenge Quiz Card (4-choice questions with child-friendly feedback)
 */
@Composable
fun ConceptAdaptiveCardView(
    card: FlnCard,
    playMode: FlnPlayMode,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSpeak: () -> Unit,
    onSpeakHindi: (() -> Unit)? = null,
    onGenerateWorksheet: ((String) -> Unit)? = null,
    activityIR: ActivityIR? = null,
    onAnswerSelected: ((String) -> Unit)? = null,
    onNextPhraseRequested: (() -> Unit)? = null,
    onSpeakSantaliWord: ((String) -> Unit)? = null,
    onSpeakHindiWord: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (playMode) {
            FlnPlayMode.EXPLORE -> {
                FlnFlipCard(
                    card = card,
                    isFlipped = isFlipped,
                    onFlip = onFlip,
                    onSpeakSantali = onSpeak,
                    onSpeakHindi = onSpeakHindi,
                    onGenerateWorksheet = onGenerateWorksheet
                )
            }
            FlnPlayMode.HANDS_ON -> {
                val effectiveIR = remember(card.id, activityIR?.id) {
                    activityIR ?: run {
                        val objectKey = card.vectorIconType.ifBlank {
                            card.imageAssetPath?.substringAfterLast("/")?.substringBeforeLast(".")
                                ?: SvgCorpusRegistry.findMatchingKey(card.englishGloss)
                                ?: "mango"
                        }
                        val isNum = card.numeralValue != null
                        val targetNum = card.numeralValue ?: 1
                        val targetStr = if (isNum) "${FlnCurriculumRepository.toOlChikiDigits(targetNum)} ($targetNum)" else card.santaliOlChiki
                        val distList = if (isNum) {
                            val cands = mutableListOf<Int>()
                            for (offset in listOf(1, -1, 2, -2, 3, -3)) {
                                val c = targetNum + offset
                                if (c in 1..20 && c != targetNum && !cands.contains(c)) cands.add(c)
                                if (cands.size >= 3) break
                            }
                            cands.map { "${FlnCurriculumRepository.toOlChikiDigits(it)} ($it)" }
                        } else {
                            FlnCurriculumRepository.generateDistractorsForCard(card, 3)
                        }
                        ActivityIR(
                            id = "card_${card.id}",
                            nipunCompetencyCode = card.nipunCode,
                            actionType = if (isNum) ActivityActionType.COUNT_AND_SELECT else ActivityActionType.PICTURE_WORD_MATCH,
                            grade = card.grade,
                            primaryObjectKey = objectKey,
                            quantity = targetNum,
                            correctValue = targetStr,
                            distractorOptions = (listOf(targetStr) + distList).distinct().shuffled(),
                            santaliWord = card.santaliOlChiki,
                            hindiWord = card.hindiText,
                            englishWord = card.englishGloss,
                            bilingualPhraseSantali = card.exampleSentenceSantali,
                            bilingualPhraseHindi = card.exampleSentenceHindi,
                            instructionSantali = if (isNum) "ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱥᱟᱹᱦᱤ ᱞᱮᱠᱷᱟ ᱵᱟᱪᱷᱟᱣ ᱢᱮ:" else "ᱥᱟᱹᱦᱤ ᱟᱹᱲᱟᱹ ᱵᱟᱪᱷᱟᱣ ᱢᱮ:",
                            instructionHindi = if (isNum) "गिनें और सही संख्या चुनें:" else "सही शब्द चुनें:"
                        )
                    }
                }
                ComposedActivityScene(
                    activityIR = effectiveIR,
                    onAnswerSelected = onAnswerSelected ?: {},
                    onNextPhraseRequested = onNextPhraseRequested,
                    onSpeakSantali = onSpeak,
                    onSpeakHindi = onSpeakHindi,
                    onSpeakSantaliWord = onSpeakSantaliWord,
                    onSpeakHindiWord = onSpeakHindiWord
                )
            }
            FlnPlayMode.QUIZ -> {
                FlnFlipCard(
                    card = card,
                    isFlipped = isFlipped,
                    onFlip = onFlip,
                    onSpeakSantali = onSpeak,
                    onSpeakHindi = onSpeakHindi,
                    onGenerateWorksheet = onGenerateWorksheet
                )
            }
        }
    }
}

/**
 * Direct Activity IR variant of ConceptAdaptiveCardView.
 */
@Composable
fun ConceptAdaptiveCardView(
    activityIR: ActivityIR,
    onAnswerSelected: (String) -> Unit,
    onNextPhraseRequested: (() -> Unit)? = null,
    onSpeakSantali: (() -> Unit)? = null,
    onSpeakHindi: (() -> Unit)? = null,
    onSpeakSantaliWord: ((String) -> Unit)? = null,
    onSpeakHindiWord: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ComposedActivityScene(
        activityIR = activityIR,
        onAnswerSelected = onAnswerSelected,
        onNextPhraseRequested = onNextPhraseRequested,
        onSpeakSantali = onSpeakSantali,
        onSpeakHindi = onSpeakHindi,
        onSpeakSantaliWord = onSpeakSantaliWord,
        onSpeakHindiWord = onSpeakHindiWord,
        modifier = modifier
    )
}

// =============================================================================
// 1. 3D Flip Card Component (Zero Emojis)
// =============================================================================

@Composable
fun FlnFlipCard(
    card: FlnCard,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSpeakSantali: () -> Unit,
    onSpeakHindi: (() -> Unit)?,
    onGenerateWorksheet: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cardFlip"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 440.dp)
            .clickable { onFlip() }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (rotation <= 90f) {
                // FRONT FACE (Mother Tongue L1 Focus)
                FrontCardFace(
                    card = card,
                    onSpeakSantali = onSpeakSantali,
                    onSpeakHindi = onSpeakHindi
                )
            } else {
                // BACK FACE (Hindi L2 Bridge)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                ) {
                    BackCardFace(
                        card = card,
                        onSpeakSantali = onSpeakSantali,
                        onSpeakHindi = onSpeakHindi,
                        onGenerateWorksheet = onGenerateWorksheet
                    )
                }
            }
        }
    }
}

@Composable
private fun FrontCardFace(
    card: FlnCard,
    onSpeakSantali: () -> Unit,
    onSpeakHindi: (() -> Unit)?
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val availableH = maxHeight
        val illustrationHeight = when {
            availableH <= 380.dp -> 96.dp
            availableH <= 460.dp -> 126.dp
            else -> 150.dp
        }
        val heroTextSize = when {
            availableH <= 380.dp -> 40.sp
            availableH <= 460.dp -> 50.sp
            else -> 60.sp
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Bilingual category pill + Dual Large Tactile Voice Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = EduPrimaryLight,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            tint = EduPrimaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${card.category.santaliName} • ${card.category.hindiName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EduPrimaryDark
                        )
                    }
                }

                // Dual Voice Button Pair (Santali & Hindi)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Santali Voice Button
                    FilledTonalButton(
                        onClick = onSpeakSantali,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = EduPrimaryLight)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Santali Audio",
                            tint = EduPrimaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ᱥᱟᱱᱛᱟᱲᱤ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EduPrimaryDark
                        )
                    }

                    // Hindi Voice Button
                    if (onSpeakHindi != null) {
                        FilledTonalButton(
                            onClick = onSpeakHindi,
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = EduIndigoLight)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Hindi Audio",
                                tint = EduIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "हिन्दी",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EduIndigo
                            )
                        }
                    }
                }
            }

            // Hero Script Area: Giant Ol Chiki
            Text(
                text = card.santaliOlChiki,
                fontSize = heroTextSize,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                lineHeight = heroTextSize * 1.15f
            )

            // Hero Illustration (Scalable Vector SVG)
            FlnCardIllustration(
                card = card,
                size = illustrationHeight
            )

            // Warm Companion Hindi Text & Gloss
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = card.hindiText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (card.exemplarWordSantali.isNotBlank() && card.exemplarWordSantali != card.santaliOlChiki) {
                    Text(
                        text = "${card.exemplarWordSantali} [ ${card.exemplarWordHindi} ]",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bottom Tap Prompt (Bilingual)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TouchApp,
                        contentDescription = "Tap",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "ᱚᱛᱟᱭ ᱢᱮ ᱩᱞᱴᱟᱹᱣ ᱞᱟᱹᱜᱤᱫ • पलटने के लिए छुएं",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BackCardFace(
    card: FlnCard,
    onSpeakSantali: () -> Unit,
    onSpeakHindi: (() -> Unit)?,
    onGenerateWorksheet: ((String) -> Unit)?
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Bar: Flip back note + Audio buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = EduAmberLight,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = "Meaning",
                        tint = EduAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MEANING & PHONICS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EduAmber
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledIconButton(
                    onClick = onSpeakSantali,
                    modifier = Modifier.size(38.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = EduAmberLight)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Pronounce Santali",
                        tint = EduAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (onSpeakHindi != null) {
                    FilledIconButton(
                        onClick = onSpeakHindi,
                        modifier = Modifier.size(38.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = EduIndigoLight)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Translate,
                            contentDescription = "Pronounce Hindi",
                            tint = EduIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Translation & Gloss
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = card.hindiText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = card.englishGloss,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Example Sentence Card
        if (card.exampleSentenceSantali.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Sentence / ᱟᱹᱭᱟᱹᱛ:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = card.exampleSentenceSantali,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = card.exampleSentenceHindi,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Phonics breakdown
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Phonics / ᱟᱲᱟᱝ ᱥᱮᱪᱮᱫ:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EduIndigo
                    )
                    Text(
                        text = card.teacherPhoneticGuide,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (card.fingerTracingGuide.isNotBlank()) {
                        Text(
                            text = card.fingerTracingGuide,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Bridge Button: Generate Worksheet for this Concept (No Emojis)
        Button(
            onClick = {
                val topic = card.exemplarWordHindi.ifBlank { card.hindiText }
                onGenerateWorksheet?.invoke(topic)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Description,
                contentDescription = "Worksheet",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Generate Worksheet for this Lesson",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// =============================================================================
// 2. Hands-On Digital Manipulative Card (Zero Emojis)
// =============================================================================

@Composable
fun HandsOnManipulativeCard(
    card: FlnCard,
    onSpeakSantali: () -> Unit,
    onSpeakHindi: (() -> Unit)?,
    onGenerateWorksheet: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = EduPrimaryLight,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TouchApp,
                            contentDescription = "Hands-On",
                            tint = EduPrimaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HANDS-ON PRACTICE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EduPrimaryDark
                        )
                    }
                }

                FilledIconButton(
                    onClick = onSpeakSantali,
                    modifier = Modifier.size(38.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = EduPrimaryLight)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Pronounce",
                        tint = EduPrimaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Interactive Manipulative Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (card.category == FlnCategory.NUMBERS && card.numeralValue != null) {
                    InteractiveTapCounter(targetCount = card.numeralValue)
                } else {
                    AlphabetTracingGuide(card = card)
                }
            }

            // Bottom Action
            Button(
                onClick = {
                    val topic = card.exemplarWordHindi.ifBlank { card.hindiText }
                    onGenerateWorksheet?.invoke(topic)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Description,
                    contentDescription = "Worksheet",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Practice on Printable Worksheet",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InteractiveTapCounter(targetCount: Int) {
    var tappedCount by remember(targetCount) { mutableStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Tap each token to count to $targetCount!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Progress Bar
        LinearProgressIndicator(
            progress = { (tappedCount.toFloat() / targetCount).coerceIn(0f, 1f) },
            modifier = Modifier
                .width(220.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Text(
            text = "Counted: $tappedCount / $targetCount",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (tappedCount >= targetCount) EduPrimaryDark else MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Tokens to Tap (Zero Emojis - Uses Material Icons)
        val displayCount = targetCount.coerceAtMost(12)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (i in 1..displayCount) {
                val isTapped = i <= tappedCount
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isTapped) EduAmberLight else MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = if (isTapped) 2.dp else 1.dp,
                            color = if (isTapped) EduAmber else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable {
                            if (tappedCount < targetCount) {
                                tappedCount = (tappedCount + 1).coerceAtMost(targetCount)
                            } else {
                                tappedCount = 0
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isTapped) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "Counted",
                            tint = EduAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Circle,
                            contentDescription = "Token",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlphabetTracingGuide(card: FlnCard) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = card.santaliOlChiki,
            fontSize = 72.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Create,
                        contentDescription = "Trace Guide",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Directional Tracing Guide:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.fingerTracingGuide.ifBlank { "Trace with your finger following the letter strokes." },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// =============================================================================
// 3. Child-Friendly Quiz Card (Zero Emojis)
// =============================================================================

@Composable
fun KidStarQuizCard(
    question: QuizQuestion,
    selectedOptionIndex: Int?,
    onSelectOption: (Int) -> Unit,
    onPlayAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 420.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = EduPrimaryLight,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Quiz,
                            contentDescription = "Quiz",
                            tint = EduPrimaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NIPUN QUIZ CHALLENGE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EduPrimaryDark
                        )
                    }
                }

                FilledIconButton(
                    onClick = onPlayAudio,
                    modifier = Modifier.size(38.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = EduPrimaryLight)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Hear Sound",
                        tint = EduPrimaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Question Prompt
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = question.promptSantali,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = question.promptHindi,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Visual Question Anchor (SVG illustration or Ol Chiki hero)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FlnCardIllustration(
                        card = question.card,
                        size = 100.dp
                    )
                }
            }

            // Options Grid (strictly 4 options in 2x2 grid)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                question.options.chunked(2).forEachIndexed { rowIdx, rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowOptions.forEachIndexed { colIdx, optionText ->
                            val optionIndex = rowIdx * 2 + colIdx
                            val isSelected = selectedOptionIndex == optionIndex
                            val isCorrect = optionIndex == question.correctOptionIndex

                            val backgroundColor = when {
                                selectedOptionIndex == null -> MaterialTheme.colorScheme.surface
                                isCorrect -> EduPrimaryLight
                                isSelected -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surface
                            }
                            val borderColor = when {
                                selectedOptionIndex == null -> MaterialTheme.colorScheme.outline
                                isCorrect -> MaterialTheme.colorScheme.primary
                                isSelected -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(enabled = selectedOptionIndex == null) {
                                        onSelectOption(optionIndex)
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = backgroundColor,
                                border = BorderStroke(1.5.dp, borderColor)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Feedback Message (No Emojis)
            AnimatedVisibility(
                visible = selectedOptionIndex != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val correct = selectedOptionIndex == question.correctOptionIndex
                Surface(
                    color = if (correct) EduPrimaryLight else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (correct) Icons.Rounded.CheckCircle else Icons.Rounded.Info,
                            contentDescription = if (correct) "Correct" else "Hint",
                            tint = if (correct) EduPrimaryDark else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (correct) "Shabash! Bahut Badhiya! (ᱟᱹᱰᱤ ᱱᱟᱯᱟᱭ!)" else "Prayas achha tha! ${question.hintHindi}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (correct) EduPrimaryDark else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// 4. Illustration Helper (WebP Bitmap + Crisp Glyph Fallback - Zero Emojis)
// =============================================================================

// =============================================================================
// 4. Illustration Helper & Ten-Frame Quantity Clusters (Zero Emojis)
// =============================================================================

@Composable
fun FlnCardIllustration(
    card: FlnCard,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val quantity = card.numeralValue ?: card.countingQuantity
    if (card.category == FlnCategory.NUMBERS || (card.numeralValue != null && card.numeralValue!! > 0)) {
        FlnQuantityCluster(
            card = card,
            quantity = quantity.coerceIn(1, 20),
            maxHeight = size,
            modifier = modifier
        )
    } else {
        FlnSingleCardIllustration(
            card = card,
            size = size,
            modifier = modifier
        )
    }
}

/**
 * Visual Quantity Cluster / Ten-Frame renderer for Number Flashcards and Counting Manipulatives.
 * Accurately displays 1 to 20 scalable SVG objects arranged pedagogically:
 * - 1 item: Giant hero icon (up to 96dp)
 * - 2..5 items: Balanced row of clean token chips
 * - 6..10 items: 2 rows of up to 5 tokens (authentic NIPUN ten-frame)
 * - 11..20 items: Compact double ten-frame grid fitting within available card height
 */
@Composable
fun FlnQuantityCluster(
    card: FlnCard,
    quantity: Int,
    maxHeight: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val svgPath = remember(card.id, card.vectorIconType, card.imageAssetPath) {
        if (card.imageAssetPath != null && card.imageAssetPath.endsWith(".svg")) {
            card.imageAssetPath.removePrefix("assets/")
        } else {
            val key = SvgCorpusRegistry.findMatchingKey(card.vectorIconType)
                ?: SvgCorpusRegistry.findMatchingKey(card.englishGloss)
                ?: SvgCorpusRegistry.findMatchingKey(card.exemplarWordSantali)
                ?: SvgCorpusRegistry.findMatchingKey(card.santaliOlChiki)
                ?: SvgCorpusRegistry.findMatchingKey(card.hindiText)
            if (key != null) {
                SvgCorpusRegistry.get(key)?.relativeFilePath?.removePrefix("assets/")
            } else null
        }
    }

    val bitmap = rememberFlnImage(card.imageAssetPath)
    val q = quantity.coerceIn(1, 20)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp, max = maxHeight),
        contentAlignment = Alignment.Center
    ) {
        when {
            q == 1 -> {
                val heroSize = if (maxHeight <= 100.dp) 72.dp else 92.dp
                FlnTokenChip(
                    svgPath = svgPath,
                    bitmap = bitmap,
                    label = card.englishGloss,
                    size = heroSize
                )
            }

            q in 2..5 -> {
                val chipSize = when {
                    maxHeight <= 90.dp -> 32.dp
                    maxHeight <= 120.dp -> 40.dp
                    else -> 48.dp
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(q) { idx ->
                        FlnTokenChip(
                            svgPath = svgPath,
                            bitmap = bitmap,
                            label = "${card.englishGloss} ${idx + 1}",
                            size = chipSize
                        )
                    }
                }
            }

            q in 6..10 -> {
                val chipSize = when {
                    maxHeight <= 90.dp -> 26.dp
                    maxHeight <= 130.dp -> 32.dp
                    else -> 38.dp
                }
                val row1Count = 5
                val row2Count = q - 5

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row 1 (5 items)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(row1Count) { idx ->
                            FlnTokenChip(
                                svgPath = svgPath,
                                bitmap = bitmap,
                                label = "${card.englishGloss} ${idx + 1}",
                                size = chipSize
                            )
                        }
                    }
                    // Row 2 (remainder)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(row2Count) { idx ->
                            FlnTokenChip(
                                svgPath = svgPath,
                                bitmap = bitmap,
                                label = "${card.englishGloss} ${idx + 6}",
                                size = chipSize
                            )
                        }
                    }
                }
            }

            else -> {
                // 11..20 items: Ten-frame double row/quad grid
                val chipSize = when {
                    maxHeight <= 110.dp -> 20.dp
                    maxHeight <= 140.dp -> 24.dp
                    else -> 28.dp
                }
                val itemsPerRow = if (q > 15) 5 else 6
                val rows = (0 until q).chunked(itemsPerRow)

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    rows.forEach { rowIndices ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowIndices.forEach { idx ->
                                FlnTokenChip(
                                    svgPath = svgPath,
                                    bitmap = bitmap,
                                    label = "${card.englishGloss} ${idx + 1}",
                                    size = chipSize
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tactile circular token chip representing one concrete object unit.
 */
@Composable
private fun FlnTokenChip(
    svgPath: String?,
    bitmap: androidx.compose.ui.graphics.ImageBitmap?,
    label: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = EduPrimaryLight.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, EduPrimary.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(size * 0.12f),
            contentAlignment = Alignment.Center
        ) {
            if (svgPath != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data("file:///android_asset/$svgPath")
                        .decoderFactory(SvgDecoder.Factory())
                        .crossfade(true)
                        .build(),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = "●",
                    fontSize = (size.value * 0.5f).sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FlnSingleCardIllustration(
    card: FlnCard,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val svgPath = remember(card.id, card.vectorIconType, card.imageAssetPath) {
        if (card.imageAssetPath != null && card.imageAssetPath.endsWith(".svg")) {
            card.imageAssetPath.removePrefix("assets/")
        } else {
            val key = SvgCorpusRegistry.findMatchingKey(card.vectorIconType)
                ?: SvgCorpusRegistry.findMatchingKey(card.englishGloss)
                ?: SvgCorpusRegistry.findMatchingKey(card.exemplarWordSantali)
                ?: SvgCorpusRegistry.findMatchingKey(card.santaliOlChiki)
                ?: SvgCorpusRegistry.findMatchingKey(card.hindiText)
            if (key != null) {
                SvgCorpusRegistry.get(key)?.relativeFilePath?.removePrefix("assets/")
            } else null
        }
    }

    val bitmap = rememberFlnImage(card.imageAssetPath)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (svgPath != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/$svgPath")
                    .decoderFactory(SvgDecoder.Factory())
                    .crossfade(true)
                    .build(),
                contentDescription = card.englishGloss,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
        } else if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = card.englishGloss,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            // Elegant Ol Chiki Character Hero Block
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = card.santaliOlChiki.take(4),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
