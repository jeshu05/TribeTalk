package org.tribetalk.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.flashcards.Flashcard
import org.tribetalk.flashcards.FlashcardImageLoader
import org.tribetalk.flashcards.FlashcardVisual
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import org.tribetalk.fln.generator.ProceduralCurriculumGenerator
import org.tribetalk.fln.model.FlnCard
import org.tribetalk.fln.model.FlnDomain
import org.tribetalk.fln.repository.FlnCurriculumRepository
import org.tribetalk.ui.components.FlnVectorGraphic
import org.tribetalk.ui.components.MatchingGameView
import org.tribetalk.ui.components.PictureFirstCardView
import org.tribetalk.ui.theme.*
import java.io.File

/**
 * Interactive NIPUN Bharat bilingual flashcards studio and FLN experience.
 * Features:
 * - Picture-First Card with progressive reveal (Image -> Hindi -> Santali Ol Chiki + Latin + Pronunciation)
 * - Card-level Santali speech audio ("Hear Santali")
 * - 3 Dedicated Modes: Study, Quiz (immediate feedback & score), Match (dual-column pairs)
 * - Teacher Card Creator with strict manual Santali override preservation
 * - Deck Builder for customized Grade, Category, Skill, and Card Limit sessions
 * - 100% Offline with zero cloud dependencies
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    flnViewModel: FlnViewModel,
    modifier: Modifier = Modifier
) {
    val categories by flnViewModel.categories.collectAsState()
    val selectedCategory by flnViewModel.selectedCategory.collectAsState()
    val cards by flnViewModel.cards.collectAsState()
    val flashcards by flnViewModel.flashcards.collectAsState()
    val currentIndex by flnViewModel.currentCardIndex.collectAsState()
    val activeMode by flnViewModel.activeMode.collectAsState()
    val isRevealed by flnViewModel.isCardRevealed.collectAsState()
    val isPlayingAudio by flnViewModel.isPlayingAudio.collectAsState()
    val activeDeckTitle by flnViewModel.activeDeckTitle.collectAsState()

    val quizScore by flnViewModel.quizScore.collectAsState()
    val quizAnsweredCount by flnViewModel.quizAnsweredCount.collectAsState()
    val isQuizCompleted by flnViewModel.isQuizCompleted.collectAsState()
    val quizOptions by flnViewModel.quizOptions.collectAsState()
    val quizCorrectIndex by flnViewModel.quizCorrectIndex.collectAsState()
    val selectedQuizOption by flnViewModel.selectedQuizOption.collectAsState()
    val isQuizAnswerChecked by flnViewModel.isQuizAnswerChecked.collectAsState()
    val customCardSuccessMessage by flnViewModel.customCardSuccessMessage.collectAsState()
    val slmStatusMessage by flnViewModel.slmStatusMessage.collectAsState()

    var showTeacherCreateDialog by remember { mutableStateOf(false) }
    var showDeckBuilderDialog by remember { mutableStateOf(false) }
    var showSlmCreateDialog by remember { mutableStateOf(false) }
    var customPromptText by remember { mutableStateOf("") }

    var isCustomGeneratorActive by remember { mutableStateOf(false) }
    var activeGeneratedSet by remember { mutableStateOf<org.tribetalk.flashcards.FlashcardSet?>(null) }
    var activeGeneratedWorksheetFromDeck by remember { mutableStateOf<org.tribetalk.worksheet.Worksheet?>(null) }

    if (activeGeneratedWorksheetFromDeck != null) {
        WorksheetPreviewScreen(
            initialWorksheet = activeGeneratedWorksheetFromDeck!!,
            onBack = { activeGeneratedWorksheetFromDeck = null },
            modifier = modifier
        )
        return
    }

    if (activeGeneratedSet != null) {
        FlashcardPreviewScreen(
            initialSet = activeGeneratedSet!!,
            onBack = { activeGeneratedSet = null },
            onCreateWorksheet = { ws -> activeGeneratedWorksheetFromDeck = ws },
            modifier = modifier
        )
        return
    }

    if (isCustomGeneratorActive) {
        FlashcardGeneratorScreen(
            onFlashcardSetGenerated = { set ->
                activeGeneratedSet = set
                isCustomGeneratorActive = false
            },
            onBack = { isCustomGeneratorActive = false },
            modifier = modifier
        )
        return
    }

    val currentCard = cards.getOrNull(currentIndex)
    val currentFlashcard = flashcards.getOrNull(currentIndex)
    val progress = if (cards.isNotEmpty()) (currentIndex + 1).toFloat() / cards.size.toFloat() else 0f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NIPUN Flashcards",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hindi • Santali (Ol Chiki) • FLN Classroom",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Deck Builder
                    FilledTonalIconButton(
                        onClick = { showDeckBuilderDialog = true },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Deck Builder",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // Teacher Add Card
                    FilledTonalIconButton(
                        onClick = { showTeacherCreateDialog = true },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = EduPrimaryLight,
                            contentColor = EduPrimaryDark
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Card",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // Visual Studio / Generator
                    FilledTonalIconButton(
                        onClick = { isCustomGeneratorActive = true },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = EduIndigoLight,
                            contentColor = EduIndigo
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddPhotoAlternate,
                            contentDescription = "Visual Studio",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // Shuffle
                    IconButton(onClick = { flnViewModel.shuffleCards() }) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle Deck",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Feedback Banner
            (customCardSuccessMessage ?: slmStatusMessage)?.let { msg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    color = EduPrimaryLight,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, EduPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Success",
                                tint = EduPrimaryDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = msg,
                                color = EduPrimaryDark,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(
                            onClick = {
                                flnViewModel.clearCustomCardMessage()
                                flnViewModel.clearSlmStatusMessage()
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Rounded.Close, "Dismiss", tint = EduPrimaryDark)
                        }
                    }
                }
            }

            // Category Filter Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { flnViewModel.selectCategory(category) },
                        shape = RoundedCornerShape(20.dp),
                        label = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            // Clean Mode Selector Bar: [ Study ] [ Quiz ] [ Match ] + [ + Create Card ]
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Segmented Mode Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ModeSegmentButton(
                            title = "Study",
                            icon = Icons.Rounded.Book,
                            isSelected = activeMode == FlashcardMode.STUDY,
                            onClick = { flnViewModel.setMode(FlashcardMode.STUDY) }
                        )
                        ModeSegmentButton(
                            title = "Quiz",
                            icon = Icons.Rounded.Quiz,
                            isSelected = activeMode == FlashcardMode.QUIZ,
                            onClick = { flnViewModel.setMode(FlashcardMode.QUIZ) }
                        )
                        ModeSegmentButton(
                            title = "Match",
                            icon = Icons.Rounded.Extension,
                            isSelected = activeMode == FlashcardMode.MATCH,
                            onClick = { flnViewModel.setMode(FlashcardMode.MATCH) }
                        )
                    }

                    // Card Count / Deck Info Pill
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "${cards.size} cards",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Main Content Area based on Active Mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                when (activeMode) {
                    FlashcardMode.STUDY -> {
                        if (currentFlashcard != null) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Study Progress Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${currentIndex + 1} / ${flashcards.size}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 16.dp)
                                            .height(6.dp)
                                            .clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                    Text(
                                        text = activeDeckTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Picture-First Card View
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PictureFirstCardView(
                                        card = currentFlashcard,
                                        isRevealed = isRevealed,
                                        isPlayingAudio = isPlayingAudio,
                                        onRevealToggle = { flnViewModel.toggleCardReveal() },
                                        onPlayAudio = { flnViewModel.playSantaliAudio(currentFlashcard) }
                                    )
                                }

                                // Study Navigation Carousel Controls
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Previous Card Button
                                    FilledTonalIconButton(
                                        onClick = { flnViewModel.prevCard() },
                                        modifier = Modifier.size(50.dp),
                                        shape = CircleShape,
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                            contentDescription = "Previous Card",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // Tap to Reveal / Hide Pill
                                    Surface(
                                        onClick = { flnViewModel.toggleCardReveal() },
                                        shape = RoundedCornerShape(24.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.height(44.dp),
                                        shadowElevation = 1.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 18.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.TouchApp,
                                                contentDescription = "Reveal",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = if (isRevealed) "Hide Ol Chiki" else "Reveal Santali",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    // Next Card Button
                                    FilledTonalIconButton(
                                        onClick = { flnViewModel.nextCard() },
                                        modifier = Modifier.size(50.dp),
                                        shape = CircleShape,
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                            contentDescription = "Next Card",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No cards available in this category.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FlashcardMode.QUIZ -> {
                        if (isQuizCompleted) {
                            // Quiz Completed State
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.5.dp, EduPrimary.copy(alpha = 0.5f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text("🎉", fontSize = 48.sp)
                                    Text(
                                        text = "Quiz Completed!",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$quizScore / $quizAnsweredCount",
                                        style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    val feedbackText = if (quizScore == quizAnsweredCount && quizAnsweredCount > 0) {
                                        "Excellent!"
                                    } else if (quizScore >= quizAnsweredCount * 0.7) {
                                        "Great Job!"
                                    } else {
                                        "Keep Practicing!"
                                    }
                                    Text(
                                        text = feedbackText,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (quizScore == quizAnsweredCount) EduPrimaryDark else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedButton(
                                            onClick = { flnViewModel.setMode(FlashcardMode.STUDY) },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Study Mode")
                                        }
                                        Button(
                                            onClick = { flnViewModel.resetQuiz() },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Retry Quiz", color = Color.White)
                                        }
                                    }
                                }
                            }
                        } else if (currentFlashcard != null) {
                            InteractiveQuizCard(
                                card = currentFlashcard,
                                score = quizScore,
                                answeredCount = quizAnsweredCount,
                                options = quizOptions,
                                correctIndex = quizCorrectIndex,
                                selectedIndex = selectedQuizOption,
                                isChecked = isQuizAnswerChecked,
                                onOptionSelected = { flnViewModel.selectQuizOption(it) },
                                onNextQuestion = { flnViewModel.onQuizNextQuestion() },
                                onResetQuiz = { flnViewModel.resetQuiz() }
                            )
                        } else {
                            Text(
                                text = "No cards available for quiz.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FlashcardMode.MATCH -> {
                        MatchingGameView(
                            cards = flashcards,
                            onMatchSuccess = { cardId ->
                                flnViewModel.recordMatchSuccess(cardId)
                            },
                            onResetGame = {
                                flnViewModel.selectCategory(selectedCategory)
                            }
                        )
                    }
                }
            }
        }
    }

    // Teacher Card Creation Dialog (Feature 8)
    if (showTeacherCreateDialog) {
        TeacherCreateCardDialog(
            categories = categories.filter { it != FlnCurriculumRepository.CATEGORY_ALL },
            onDismiss = { showTeacherCreateDialog = false },
            onSaveCard = { newCard ->
                flnViewModel.saveTeacherCard(newCard)
                showTeacherCreateDialog = false
            }
        )
    }

    // Deck Builder Dialog (Feature 9)
    if (showDeckBuilderDialog) {
        DeckBuilderDialog(
            categories = categories,
            selectedCategory = selectedCategory,
            onDismiss = { showDeckBuilderDialog = false },
            onApplyDeck = { cat, gr, sk, lim, mode ->
                flnViewModel.applyCustomDeck(cat, gr, sk, lim, mode)
                showDeckBuilderDialog = false
            }
        )
    }

    // AI Flashcard Synthesizer Dialog
    if (showSlmCreateDialog) {
        AlertDialog(
            onDismissRequest = { showSlmCreateDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("AI Flashcard Creator", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Enter a Hindi word or topic for on-device synthesis:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = customPromptText,
                        onValueChange = { customPromptText = it },
                        label = { Text("Word / Topic") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (customPromptText.isNotBlank()) {
                        flnViewModel.createCustomFlashcard(customPromptText)
                        customPromptText = ""
                        showSlmCreateDialog = false
                    }
                }) {
                    Text("Synthesize")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSlmCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ModeSegmentButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = if (isSelected) null else BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Interactive Quiz Card supporting Picture-First prompts, randomized distractors,
 * immediate validation, and running score.
 */
@Composable
private fun InteractiveQuizCard(
    card: Flashcard,
    score: Int,
    answeredCount: Int,
    options: List<String>,
    correctIndex: Int,
    selectedIndex: Int?,
    isChecked: Boolean,
    onOptionSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onResetQuiz: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Score Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = EduAmberLight,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Score: $score / $answeredCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EduAmber,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                TextButton(onClick = onResetQuiz) {
                    Text("Reset Quiz", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }

            // Question Visual Prompt (Picture-First)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                FlashcardVisual(
                    imageUri = card.imageUri,
                    iconType = card.iconType,
                    imageEmoji = card.imageEmoji,
                    contentDescription = card.hindiText,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    maxImageSize = 72.dp,
                    emojiFontSize = 42.sp,
                    vectorGraphicSize = 52.dp
                )
            }

            Text(
                text = "What is '${card.hindiText}' in Santali (Ol Chiki)?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // 4 Option Buttons
            options.forEachIndexed { optIdx, optText ->
                val isSelected = selectedIndex == optIdx
                val isCorrect = optIdx == correctIndex

                val btnBgColor = when {
                    isChecked && isCorrect -> EduPrimaryLight
                    isChecked && isSelected && !isCorrect -> Color(0xFFFEE2E2)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                val btnBorderColor = when {
                    isChecked && isCorrect -> MaterialTheme.colorScheme.primary
                    isChecked && isSelected && !isCorrect -> Color(0xFFEF4444)
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                }

                val btnTextColor = when {
                    isChecked && isCorrect -> MaterialTheme.colorScheme.primary
                    isChecked && isSelected && !isCorrect -> Color(0xFFDC2626)
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = btnBgColor,
                    border = BorderStroke(1.dp, btnBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isChecked) { onOptionSelected(optIdx) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = optText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected || (isChecked && isCorrect)) FontWeight.Bold else FontWeight.Medium,
                            color = btnTextColor
                        )

                        if (isChecked && isCorrect) {
                            Icon(Icons.Rounded.CheckCircle, "Correct", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        } else if (isChecked && isSelected && !isCorrect) {
                            Icon(Icons.Rounded.Cancel, "Incorrect", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Next Question Button
            if (isChecked) {
                Button(
                    onClick = onNextQuestion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Next Question", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, "Next", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/**
 * Teacher Card Creator Dialog (Feature 8)
 * Allows teachers to customize and add new cards with Hindi, Santali Ol Chiki,
 * Latin Santali, Pronunciation, Image/Emoji, Category, Grade, and Skill.
 *
 * CRITICAL RULE:
 * Preserves manual Santali overrides! "Translate to Santali" only generates a draft
 * for empty fields and NEVER overwrites manual edits made by the teacher.
 */
@Composable
private fun TeacherCreateCardDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onSaveCard: (Flashcard) -> Unit
) {
    var hindiText by remember { mutableStateOf("") }
    var santaliOlChiki by remember { mutableStateOf("") }
    var latinSantali by remember { mutableStateOf("") }
    var pronunciationGuide by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf("") }
    var imageEmoji by remember { mutableStateOf("📖") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "Classroom") }
    var selectedGrade by remember { mutableStateOf("Grade 1") }
    var selectedSkill by remember { mutableStateOf("Vocabulary") }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            val savedPath = FlashcardImageLoader.saveTeacherPhoto(context, uri)
            if (savedPath != null) {
                imageUri = savedPath
            }
        }
    }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.AddBox, contentDescription = "Add Card", tint = MaterialTheme.colorScheme.primary)
                Text("Teacher: Create Card", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Add a card for your classroom deck. Manual Santali edits are always preserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Hindi Input
                OutlinedTextField(
                    value = hindiText,
                    onValueChange = { hindiText = it },
                    label = { Text("Hindi Word / Term *") },
                    placeholder = { Text("उदा. किताब, हाथी, बादल...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Translate Draft Button
                OutlinedButton(
                    onClick = {
                        if (hindiText.isNotBlank()) {
                            val draft = ProceduralCurriculumGenerator.synthesizeCard(hindiText)
                            // Manual override preservation: only set if fields are empty
                            if (santaliOlChiki.isBlank()) santaliOlChiki = draft.santaliOlChiki
                            if (latinSantali.isBlank()) latinSantali = draft.englishGloss
                            if (pronunciationGuide.isBlank()) pronunciationGuide = draft.teacherPhoneticGuide
                            if (imageEmoji.isBlank() || imageEmoji == "📖") draft.imageEmoji?.let { imageEmoji = it }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Rounded.AutoAwesome, "Draft", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Translate to Santali (First Draft)")
                }

                // Santali Ol Chiki Input
                OutlinedTextField(
                    value = santaliOlChiki,
                    onValueChange = { santaliOlChiki = it },
                    label = { Text("Santali (Ol Chiki) *") },
                    placeholder = { Text("ᱚᱞ ᱪᱤᱠᱤ ᱞᱤᱯᱤ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Latin Santali Input
                OutlinedTextField(
                    value = latinSantali,
                    onValueChange = { latinSantali = it },
                    label = { Text("Latin Santali (Roman script)") },
                    placeholder = { Text("e.g. potob, seti...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Pronunciation Guide Input
                OutlinedTextField(
                    value = pronunciationGuide,
                    onValueChange = { pronunciationGuide = it },
                    label = { Text("Devanagari Pronunciation Guide") },
                    placeholder = { Text("उदा. पोतोब, सेता...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Emoji and Image Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = imageEmoji,
                        onValueChange = { imageEmoji = it },
                        label = { Text("Emoji") },
                        modifier = Modifier.width(90.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = imageUri,
                        onValueChange = { imageUri = it },
                        label = { Text("Image File Path (Optional)") },
                        placeholder = { Text("/sdcard/...") },
                        trailingIcon = {
                            IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                                Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = "Pick Photo")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Category Selection
                Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                // Grade & Skill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = selectedGrade,
                        onValueChange = { selectedGrade = it },
                        label = { Text("Grade") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = selectedSkill,
                        onValueChange = { selectedSkill = it },
                        label = { Text("Skill") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (hindiText.isNotBlank()) {
                        val card = Flashcard(
                            id = "teacher_${System.currentTimeMillis()}",
                            topic = selectedCategory,
                            skill = selectedSkill,
                            hindiText = hindiText.trim(),
                            santaliText = latinSantali.trim(),
                            santaliOlChiki = if (santaliOlChiki.isNotBlank()) santaliOlChiki.trim() else hindiText.trim(),
                            phoneticGuide = pronunciationGuide.trim(),
                            imageUri = if (imageUri.isNotBlank()) imageUri.trim() else null,
                            imageEmoji = if (imageEmoji.isNotBlank()) imageEmoji.trim() else "📖",
                            iconType = "book",
                            domain = "Literacy",
                            grade = selectedGrade.trim(),
                            englishGloss = latinSantali.trim()
                        )
                        onSaveCard(card)
                    }
                },
                enabled = hindiText.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save Card", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

/**
 * Deck Builder Dialog (Feature 9)
 * Allows teachers to assemble custom decks from category, grade, skill, and card count.
 */
@Composable
private fun DeckBuilderDialog(
    categories: List<String>,
    selectedCategory: String,
    onDismiss: () -> Unit,
    onApplyDeck: (category: String, grade: String, skill: String, limit: Int, mode: FlashcardMode) -> Unit
) {
    var category by remember { mutableStateOf(selectedCategory) }
    var grade by remember { mutableStateOf("Grade 1") }
    var skill by remember { mutableStateOf("Vocabulary") }
    var cardLimit by remember { mutableIntStateOf(5) }
    var targetMode by remember { mutableStateOf(FlashcardMode.STUDY) }

    val gradeOptions = listOf("Balvatika", "Grade 1", "Grade 2", "Grade 3")
    val skillOptions = listOf("Vocabulary", "Numeracy", "Phonics", "Comprehension")
    val limitOptions = listOf(5, 10, 15, 20)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Tune, contentDescription = "Deck Builder", tint = MaterialTheme.colorScheme.primary)
                Text("Deck Builder", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Customize your classroom deck for focused FLN instruction:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Category Selection
                Text("Category:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                // Grade Selection
                Text("Grade Level:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(gradeOptions) { gr ->
                        FilterChip(
                            selected = grade == gr,
                            onClick = { grade = gr },
                            label = { Text(gr) }
                        )
                    }
                }

                // Skill Selection
                Text("Target Skill:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(skillOptions) { sk ->
                        FilterChip(
                            selected = skill == sk,
                            onClick = { skill = sk },
                            label = { Text(sk) }
                        )
                    }
                }

                // Card Limit
                Text("Deck Size:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    limitOptions.forEach { lim ->
                        FilterChip(
                            selected = cardLimit == lim,
                            onClick = { cardLimit = lim },
                            label = { Text("$lim cards") }
                        )
                    }
                }

                // Starting Activity Mode
                Text("Start In Mode:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = targetMode == FlashcardMode.STUDY,
                        onClick = { targetMode = FlashcardMode.STUDY },
                        label = { Text("Study") }
                    )
                    FilterChip(
                        selected = targetMode == FlashcardMode.QUIZ,
                        onClick = { targetMode = FlashcardMode.QUIZ },
                        label = { Text("Quiz") }
                    )
                    FilterChip(
                        selected = targetMode == FlashcardMode.MATCH,
                        onClick = { targetMode = FlashcardMode.MATCH },
                        label = { Text("Match") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApplyDeck(category, grade, skill, cardLimit, targetMode) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Start Deck Session", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
