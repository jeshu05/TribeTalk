package org.tribetalk.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.fln.model.*
import org.tribetalk.fln.pipeline.*
import org.tribetalk.fln.repository.FlnCurriculumRepository
import org.tribetalk.ui.components.ConceptAdaptiveCardView
import org.tribetalk.ui.components.KidStarQuizCard
import org.tribetalk.ui.theme.*

/**
 * World-class NIPUN Bharat Bilingual Flashcards Screen.
 * Completely free of emoji strings.
 * Fully adaptable across compact phones, landscape phones, and classroom tablets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    flnViewModel: FlnViewModel,
    onNavigateToWorksheets: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCard by flnViewModel.activeCard.collectAsState()
    val filteredCards by flnViewModel.filteredCards.collectAsState()
    val activeIndex by flnViewModel.activeCardIndex.collectAsState()
    val isFlipped by flnViewModel.isCardFlipped.collectAsState()
    val playMode by flnViewModel.playMode.collectAsState()
    val selectedDomain by flnViewModel.selectedDomain.collectAsState()
    val selectedCategory by flnViewModel.selectedCategory.collectAsState()

    val quizQuestion by flnViewModel.quizQuestion.collectAsState()
    val selectedQuizOption by flnViewModel.selectedQuizOption.collectAsState()
    val quizScore by flnViewModel.quizScore.collectAsState()
    val quizTotal by flnViewModel.quizTotal.collectAsState()
    val currentActivityIR by flnViewModel.currentActivityIR.collectAsState()

    val windowSizeInfo = rememberWindowSizeInfo()
    val isLandscape = windowSizeInfo.isLandscape
    val isCompactHeight = windowSizeInfo.isCompactHeight

    LaunchedEffect(activeCard) {
        activeCard?.let { card ->
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

            flnViewModel.setActivityIR(
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
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TribeTalk FLN ᱯᱟᱲᱦᱟᱣ",
                            style = if (isCompactHeight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (!isCompactHeight) {
                            Text(
                                text = "ᱥᱟᱱᱛᱟᱲᱤ ᱟᱨ ᱦᱤᱱᱫᱤ • खेल-खेल में सीखें",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { flnViewModel.openStudio() }) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "AI Content Studio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Star Score Pill (Zero Emojis)
                    Surface(
                        color = EduAmberLight,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = "Stars",
                                tint = EduAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$quizScore Stars",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = EduAmber
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline)
            )
        }
    ) { paddingValues ->
        if (isLandscape) {
            // LANDSCAPE TWO-COLUMN CONSOLE LAYOUT
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left: Hero Stage
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    if (playMode == FlnPlayMode.QUIZ && quizQuestion != null) {
                        KidStarQuizCard(
                            question = quizQuestion!!,
                            selectedOptionIndex = selectedQuizOption,
                            onSelectOption = { flnViewModel.submitQuizAnswer(it) },
                            onPlayAudio = { flnViewModel.playQuizAudio() }
                        )
                    } else if (activeCard != null) {
                        ConceptAdaptiveCardView(
                            card = activeCard!!,
                            playMode = playMode,
                            isFlipped = isFlipped,
                            onFlip = { flnViewModel.toggleCardFlip() },
                            onSpeak = { flnViewModel.playActiveCardAudio() },
                            onSpeakHindi = { flnViewModel.playActiveCardHindiAudio() },
                            activityIR = currentActivityIR,
                            onAnswerSelected = { flnViewModel.submitActivityAnswer(it) },
                            onNextPhraseRequested = {
                                val key = currentActivityIR?.primaryObjectKey ?: "mango"
                                flnViewModel.advanceToNextPhrase(key)
                            },
                            onSpeakSantaliWord = { flnViewModel.playSantaliText(it) },
                            onSpeakHindiWord = { flnViewModel.playHindiText(it) },
                            onGenerateWorksheet = { topic ->
                                flnViewModel.generateWorksheetFromTopic(topic)
                                onNavigateToWorksheets()
                            }
                        )
                    }
                }

                // Right: Console Controls
                Column(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PlayModeSelector(
                        currentMode = playMode,
                        onModeSelected = { flnViewModel.setPlayMode(it) }
                    )

                    if (playMode == FlnPlayMode.QUIZ) {
                        Button(
                            onClick = { flnViewModel.nextQuizQuestion() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Next Question", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Next",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        CategoryFilterChips(
                            selectedCategory = selectedCategory,
                            onCategorySelected = { flnViewModel.setCategory(it) }
                        )

                        Text(
                            text = "Card ${activeIndex + 1} of ${filteredCards.size.coerceAtLeast(1)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )

                        CardNavigationDock(
                            onPrevious = { flnViewModel.previousCard() },
                            onNext = { flnViewModel.nextCard() },
                            onFlip = { flnViewModel.toggleCardFlip() },
                            onSpeakSantali = { flnViewModel.playActiveCardAudio() },
                            onSpeakHindi = { flnViewModel.playActiveCardHindiAudio() }
                        )
                    }
                }
            }
        } else {
            // PORTRAIT FLOW (Phones & Portrait Tablets)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PlayModeSelector(
                    currentMode = playMode,
                    onModeSelected = { flnViewModel.setPlayMode(it) },
                    modifier = Modifier.widthIn(max = 520.dp)
                )

                if (playMode != FlnPlayMode.QUIZ) {
                    CategoryFilterChips(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { flnViewModel.setCategory(it) }
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 520.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (playMode == FlnPlayMode.QUIZ && quizQuestion != null) {
                        KidStarQuizCard(
                            question = quizQuestion!!,
                            selectedOptionIndex = selectedQuizOption,
                            onSelectOption = { flnViewModel.submitQuizAnswer(it) },
                            onPlayAudio = { flnViewModel.playQuizAudio() }
                        )
                    } else if (activeCard != null) {
                        ConceptAdaptiveCardView(
                            card = activeCard!!,
                            playMode = playMode,
                            isFlipped = isFlipped,
                            onFlip = { flnViewModel.toggleCardFlip() },
                            onSpeak = { flnViewModel.playActiveCardAudio() },
                            onSpeakHindi = { flnViewModel.playActiveCardHindiAudio() },
                            activityIR = currentActivityIR,
                            onAnswerSelected = { flnViewModel.submitActivityAnswer(it) },
                            onNextPhraseRequested = {
                                val key = currentActivityIR?.primaryObjectKey ?: "mango"
                                flnViewModel.advanceToNextPhrase(key)
                            },
                            onSpeakSantaliWord = { flnViewModel.playSantaliText(it) },
                            onSpeakHindiWord = { flnViewModel.playHindiText(it) },
                            onGenerateWorksheet = { topic ->
                                flnViewModel.generateWorksheetFromTopic(topic)
                                onNavigateToWorksheets()
                            }
                        )
                    }
                }

                if (playMode == FlnPlayMode.QUIZ) {
                    Button(
                        onClick = { flnViewModel.nextQuizQuestion() },
                        modifier = Modifier
                            .widthIn(max = 520.dp)
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Next Question", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Next",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.widthIn(max = 520.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Card ${activeIndex + 1} of ${filteredCards.size.coerceAtLeast(1)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        CardNavigationDock(
                            onPrevious = { flnViewModel.previousCard() },
                            onNext = { flnViewModel.nextCard() },
                            onFlip = { flnViewModel.toggleCardFlip() },
                            onSpeakSantali = { flnViewModel.playActiveCardAudio() },
                            onSpeakHindi = { flnViewModel.playActiveCardHindiAudio() }
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Helper Components (Zero Emojis - Pure Material Icons)
// =============================================================================

@Composable
private fun PlayModeSelector(
    currentMode: FlnPlayMode,
    onModeSelected: (FlnPlayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        FlnPlayMode.entries.forEach { mode ->
            val isSelected = currentMode == mode
            val modeIcon: ImageVector = when (mode) {
                FlnPlayMode.EXPLORE -> Icons.Rounded.Style
                FlnPlayMode.HANDS_ON -> Icons.Rounded.TouchApp
                FlnPlayMode.QUIZ -> Icons.Rounded.Quiz
            }

            val modeLabel = when (mode) {
                FlnPlayMode.EXPLORE -> "ᱧᱮᱞ (सीखें)"
                FlnPlayMode.HANDS_ON -> "ᱠᱷᱮᱞ (खेलें)"
                FlnPlayMode.QUIZ -> "ᱵᱤᱰᱟᱹᱣ (क्विज़)"
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = modeIcon,
                        contentDescription = modeLabel,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = modeLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterChips(
    selectedCategory: FlnCategory,
    onCategorySelected: (FlnCategory) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(FlnCategory.entries) { category ->
            val isSelected = selectedCategory == category
            val categoryIcon: ImageVector = when (category) {
                FlnCategory.ALL -> Icons.Rounded.Widgets
                FlnCategory.AKSHAR -> Icons.Rounded.Abc
                FlnCategory.NUMBERS -> Icons.Rounded.Pin
                FlnCategory.ARITHMETIC -> Icons.Rounded.Calculate
                FlnCategory.MONEY -> Icons.Rounded.Payments
                FlnCategory.ANIMALS -> Icons.Rounded.Pets
                FlnCategory.FRUITS -> Icons.Rounded.Restaurant
                FlnCategory.NATURE -> Icons.Rounded.Forest
                FlnCategory.SCHOOL -> Icons.Rounded.School
                FlnCategory.SPATIAL -> Icons.Rounded.Category
            }

            val labelText = "${category.santaliName} (${category.hindiName.substringBefore(" ")})"

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = labelText,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = {
                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
private fun CardNavigationDock(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFlip: () -> Unit,
    onSpeakSantali: () -> Unit,
    onSpeakHindi: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Previous Card",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Santali Speak Button
            FilledIconButton(
                onClick = onSpeakSantali,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = EduPrimaryLight)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = "Speak Santali",
                    tint = EduPrimaryDark,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Hindi Speak Button
            FilledIconButton(
                onClick = onSpeakHindi,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = EduIndigoLight)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Translate,
                    contentDescription = "Speak Hindi",
                    tint = EduIndigo,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        OutlinedButton(
            onClick = onFlip,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Icon(
                imageVector = Icons.Rounded.Sync,
                contentDescription = "Flip",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Flip", style = MaterialTheme.typography.labelMedium)
        }

        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Next Card",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
