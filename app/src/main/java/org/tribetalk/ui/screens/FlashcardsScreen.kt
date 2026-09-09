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
import org.tribetalk.ui.components.FlnCardView
import org.tribetalk.ui.components.FlnVectorGraphic
import org.tribetalk.ui.theme.*

/**
 * Interactive NIPUN Bharat bilingual flashcards screen.
 * Supports Explore Mode, Interactive 4-Choice Quiz Mode, and On-the-Fly Card Synthesis.
 * High-contrast Green, White, and Black design.
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PureBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NIPUN Flashcards",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite
                        )
                        Text(
                            text = "Hindi <-> Santali (Ol Chiki) • Balvatika to Grade 2",
                            style = MaterialTheme.typography.bodySmall,
                            color = WhiteSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.AddCircleOutline,
                            contentDescription = "Add Custom Card",
                            tint = EmeraldGreen
                        )
                    }
                    IconButton(onClick = { flnViewModel.shuffleCards() }) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle Deck",
                            tint = EmeraldGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureBlack
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PureBlack),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Success Toast Banner
            (customCardSuccessMessage ?: slmStatusMessage)?.let { msg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    color = EmeraldGreen,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = msg, color = PureBlack, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {
                                flnViewModel.clearCustomCardMessage()
                                flnViewModel.clearSlmStatusMessage()
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Rounded.Close, "Dismiss", tint = PureBlack)
                        }
                    }
                }
            }

            // Category Filter Chips
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
                        label = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = PureBlack,
                            containerColor = DarkCard,
                            labelColor = PureWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) EmeraldGreen else DarkBorder,
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            // Mode Toggle Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (cards.isNotEmpty()) "Card ${currentIndex + 1} of ${cards.size}" else "0 cards",
                    style = MaterialTheme.typography.labelLarge,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold
                )

                // Quiz Mode Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isQuizMode) "Quiz Mode" else "Explore",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isQuizMode) EmeraldMint else PureWhite
                    )
                    Switch(
                        checked = isQuizMode,
                        onCheckedChange = { flnViewModel.toggleQuizMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureBlack,
                            checkedTrackColor = EmeraldGreen,
                            uncheckedThumbColor = WhiteSecondary,
                            uncheckedTrackColor = DarkCard
                        )
                    )
                }
            }

            // Main Display (Quiz Mode vs Card View)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (currentCard != null) {
                    if (isQuizMode) {
                        // Interactive Quiz View
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
                        // Standard Flip Flashcard View
                        FlnCardView(
                            card = currentCard,
                            isQuizMode = false,
                            isRevealed = isRevealed,
                            isPlayingAudio = isPlayingAudio,
                            onRevealToggle = { flnViewModel.toggleCardReveal() },
                            onPlayAudio = { flnViewModel.playSantaliAudio(currentCard) }
                        )
                    }
                } else {
                    Text(
                        text = "No cards available in this category.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhiteSecondary
                    )
                }
            }

            // Bottom Carousel Navigation Buttons (Visible in Explore Mode)
            if (!isQuizMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = { flnViewModel.prevCard() },
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = DarkCard,
                            contentColor = PureWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Previous Card"
                        )
                    }

                    // Flip Prompt Hint
                    Surface(
                        onClick = { flnViewModel.toggleCardReveal() },
                        shape = RoundedCornerShape(24.dp),
                        color = DarkSurface,
                        border = BorderStroke(1.dp, DarkBorderGreen),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TouchApp,
                                contentDescription = "Tap to flip",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isRevealed) "Hide Ol Chiki" else "Reveal Ol Chiki",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                    }

                    FilledTonalIconButton(
                        onClick = { flnViewModel.nextCard() },
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = EmeraldGreen,
                            contentColor = PureBlack
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Next Card"
                        )
                    }
                }
            }
        }
    }

    // Custom Card & SLM Deck Creator Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = DarkCard,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "AI",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Synthesize Flashcard / Deck",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter any Hindi word or classroom topic. Synthesize a single card or let the on-device SLM dream a complete 5-card deck with Ol Chiki and pronunciation guides.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WhiteSecondary
                    )

                    // Quick topic chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val sampleTopics = listOf("🍎 फल", "🐮 जानवर", "🌳 प्रकृति", "🏫 स्कूल", "💰 बाज़ार")
                        items(sampleTopics) { topic ->
                            Surface(
                                onClick = { customPromptText = topic.substringAfter(" ") },
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurface,
                                border = BorderStroke(0.8.dp, DarkBorder)
                            ) {
                                Text(
                                    text = topic,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PureWhite
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customPromptText,
                        onValueChange = { customPromptText = it },
                        label = { Text("Topic / Concept in Hindi") },
                        placeholder = { Text("उदा. नदी, आम, जंगल, स्कूल...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
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
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DarkSurface,
                            contentColor = EmeraldMint
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
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = PureBlack)
                    ) {
                        Text("Single Card", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = WhiteSecondary)
                }
            }
        )
    }
}

@Composable
private fun InteractiveQuizCard(
    card: org.tribetalk.fln.model.FlnCard,
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
            .padding(vertical = 10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.5.dp, DarkBorderGreen)
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
                Text(
                    text = "Score: $score / $answeredCount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldMint
                )
                TextButton(onClick = onResetQuiz) {
                    Text("Reset Score", color = WhiteSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }

            // Question Visual Prompt
            FlnVectorGraphic(iconType = card.iconType, size = 64.dp, tint = EmeraldGreen)

            Text(
                text = "What is '${card.hindiText}' in Santali (Ol Chiki)?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                textAlign = TextAlign.Center
            )

            // 4 Option Buttons
            options.forEachIndexed { optIdx, optText ->
                val isSelected = selectedIndex == optIdx
                val isCorrect = optIdx == correctIndex

                val btnBgColor = when {
                    isChecked && isCorrect -> EmeraldGreen.copy(alpha = 0.25f)
                    isChecked && isSelected && !isCorrect -> Color(0xFFEF4444).copy(alpha = 0.25f)
                    isSelected -> EmeraldGreen.copy(alpha = 0.15f)
                    else -> DarkSurface
                }

                val btnBorderColor = when {
                    isChecked && isCorrect -> EmeraldGreen
                    isChecked && isSelected && !isCorrect -> Color(0xFFEF4444)
                    isSelected -> EmeraldGreen
                    else -> DarkBorder
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(btnBgColor)
                        .border(1.2.dp, btnBorderColor, RoundedCornerShape(12.dp))
                        .clickable(enabled = !isChecked) { onOptionSelected(optIdx) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = optText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        if (isChecked && isCorrect) {
                            Icon(Icons.Rounded.CheckCircle, "Correct", tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                        } else if (isChecked && isSelected && !isCorrect) {
                            Icon(Icons.Rounded.Cancel, "Incorrect", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Teacher Phonetic Reveal on Check
            if (isChecked) {
                Text(
                    text = "Teacher Pronunciation: बोलें - ${card.teacherPhoneticGuide}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldMint,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = onNextQuestion,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = PureBlack)
                ) {
                    Text("Next Question", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
