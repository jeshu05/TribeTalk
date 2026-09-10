package org.tribetalk.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.fln.model.FlnCard
import org.tribetalk.ui.components.FlnCardView
import org.tribetalk.ui.components.FlnVectorGraphic
import org.tribetalk.ui.theme.*

/**
 * Interactive NIPUN Bharat bilingual flashcards studio.
 * Clean, delightful educational design inspired by Quizlet and Duolingo.
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
    val currentIndex by flnViewModel.currentCardIndex.collectAsState()
    val isQuizMode by flnViewModel.isQuizMode.collectAsState()
    val isRevealed by flnViewModel.isCardRevealed.collectAsState()
    val isPlayingAudio by flnViewModel.isPlayingAudio.collectAsState()

    val quizScore by flnViewModel.quizScore.collectAsState()
    val quizAnsweredCount by flnViewModel.quizAnsweredCount.collectAsState()
    val quizOptions by flnViewModel.quizOptions.collectAsState()
    val quizCorrectIndex by flnViewModel.quizCorrectIndex.collectAsState()
    val selectedQuizOption by flnViewModel.selectedQuizOption.collectAsState()
    val isQuizAnswerChecked by flnViewModel.isQuizAnswerChecked.collectAsState()
    val customCardSuccessMessage by flnViewModel.customCardSuccessMessage.collectAsState()
    val isSlmGenerating by flnViewModel.isSlmGenerating.collectAsState()
    val slmStatusMessage by flnViewModel.slmStatusMessage.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var customPromptText by remember { mutableStateOf("") }

    val currentCard = cards.getOrNull(currentIndex)
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
                            text = "Hindi • Santali (Ol Chiki) • Balvatika to Grade 2",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = { showCreateDialog = true },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = EduPrimaryLight,
                            contentColor = EduPrimaryDark
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Synthesize",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    .padding(vertical = 10.dp),
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

            // Gamified Star Trail & Card Progress Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Star Trail Indicator
                Surface(
                    color = EduAmberLight,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.8.dp, EduAmber.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "⭐",
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (cards.isNotEmpty()) "${currentIndex + 1} / ${cards.size} Stars" else "0 Stars",
                            style = MaterialTheme.typography.labelMedium,
                            color = EduAmber,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )

                // Explore vs Quiz Switcher
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isQuizMode) "Quiz" else "Cards",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isQuizMode) EduIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = isQuizMode,
                        onCheckedChange = { flnViewModel.toggleQuizMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EduIndigo,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            // Main Display: Responsive Adaptive Flashcard / Quiz View
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                val screenWidth = maxWidth
                val screenHeight = maxHeight
                val isTablet = screenWidth >= 600.dp
                val cardMaxWidth = if (isTablet) 520.dp else 420.dp
                val illustrationSize = when {
                    screenHeight < 460.dp -> 90.dp
                    isTablet -> 160.dp
                    screenHeight < 660.dp -> 115.dp
                    else -> 135.dp
                }

                Box(
                    modifier = Modifier
                        .widthIn(max = cardMaxWidth)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentCard != null) {
                        if (isQuizMode) {
                            InteractiveQuizCard(
                                card = currentCard,
                                score = quizScore,
                                answeredCount = quizAnsweredCount,
                                options = quizOptions,
                                correctIndex = quizCorrectIndex,
                                selectedIndex = selectedQuizOption,
                                isChecked = isQuizAnswerChecked,
                                onOptionSelected = { flnViewModel.selectQuizOption(it) },
                                onNextQuestion = { flnViewModel.nextCard() },
                                onResetQuiz = { flnViewModel.resetQuiz() }
                            )
                        } else {
                            FlnCardView(
                                card = currentCard,
                                isQuizMode = false,
                                isRevealed = isRevealed,
                                isPlayingAudio = isPlayingAudio,
                                onRevealToggle = { flnViewModel.toggleCardReveal() },
                                onPlayAudio = { flnViewModel.playSantaliAudio(currentCard) },
                                illustrationSize = illustrationSize
                            )
                        }
                    } else {
                        Text(
                            text = "No cards available in this category.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Bottom Carousel Controls (Explore Mode)
            if (!isQuizMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Card Button
                    FilledTonalIconButton(
                        onClick = { flnViewModel.prevCard() },
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Previous Card",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Flip Prompt Pill
                    Surface(
                        onClick = { flnViewModel.toggleCardReveal() },
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.height(48.dp),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 22.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isRevealed) "🖼️ चित्र देखें" else "🔄 संथाली देखें",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Next Card Button
                    FilledTonalIconButton(
                        onClick = { flnViewModel.nextCard() },
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Next Card",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    // Synthesize Custom Card / SLM Deck Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EduPrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "AI",
                            tint = EduPrimaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        "AI Flashcard Creator",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Enter a Hindi word or classroom topic. Create a single card or let the on-device AI synthesize a full 5-card deck with Ol Chiki and teacher pronunciations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Quick topic chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val sampleTopics = listOf("🍎 फल", "🐮 जानवर", "🌳 प्रकृति", "🏫 स्कूल", "💰 बाज़ार")
                        items(sampleTopics) { topic ->
                            Surface(
                                onClick = { customPromptText = topic.substringAfter(" ") },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(
                                    text = topic,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customPromptText,
                        onValueChange = { customPromptText = it },
                        label = { Text("Topic or Word in Hindi") },
                        placeholder = { Text("उदा. नदी, आम, जंगल, स्कूल...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = {
                            if (customPromptText.isNotBlank()) {
                                flnViewModel.generateFlashcardsWithSlm(customPromptText)
                                customPromptText = ""
                                showCreateDialog = false
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = EduIndigoLight,
                            contentColor = EduIndigo
                        )
                    ) {
                        Text("AI 5-Card Deck", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (customPromptText.isNotBlank()) {
                                flnViewModel.createCustomFlashcard(customPromptText)
                                customPromptText = ""
                                showCreateDialog = false
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Single Card", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun InteractiveQuizCard(
    card: FlnCard,
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
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    Text("Reset", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }

            // Question Visual Prompt
            FlnVectorGraphic(iconType = card.iconType, size = 60.dp, tint = MaterialTheme.colorScheme.primary)

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
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
                        .height(48.dp),
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
