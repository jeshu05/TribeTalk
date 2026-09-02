package com.alchemists.tribetalk.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alchemists.tribetalk.curriculum.generator.FLNFlashcardGenerator
import com.alchemists.tribetalk.curriculum.repository.FLNCurriculumRepository
import com.alchemists.tribetalk.translation.Language
import com.alchemists.tribetalk.voice.VoiceTranslationBridge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    voiceTranslationBridge: VoiceTranslationBridge,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLessonIndex by remember { mutableIntStateOf(0) }
    var currentCardIndex by remember { mutableIntStateOf(0) }
    var randomSeed by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val currentLesson = FLNCurriculumRepository.lessons[selectedLessonIndex]
    val currentOutcome = currentLesson.learningOutcome

    val flashcards = remember(selectedLessonIndex, randomSeed) {
        FLNFlashcardGenerator.generateFlashcards(
            outcome = currentOutcome,
            count = 6,
            seed = randomSeed
        )
    }

    val activeCard = flashcards.getOrNull(currentCardIndex.coerceIn(0, flashcards.size - 1)) ?: flashcards.first()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Visual Flashcards Deck",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 22.sp
                            )
                        )
                        Text(
                            "NIPUN Bharat • Ol Chiki & Devanagari Guide",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        randomSeed = System.currentTimeMillis()
                        currentCardIndex = 0
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Generate New Deck",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.shadow(1.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Outcome Header Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = currentOutcome.nipunCode,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "Card ${currentCardIndex + 1} of ${flashcards.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Central Visual Flashcard Component
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Large Visual Emoji Stimulus
                    Text(
                        text = activeCard.symbolOrWord,
                        fontSize = 84.sp
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = activeCard.textSantali,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeCard.textHindi,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "HUD Guide: ${activeCard.phoneticDevanagari}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Play Santali Audio Button
                    Button(
                        onClick = {
                            voiceTranslationBridge.translateAndSpeak(
                                recognizedText = activeCard.textHindi,
                                sourceLanguage = Language.HINDI,
                                targetLanguage = Language.SANTALI,
                                isVoiceBridgeEnabled = true,
                                onStateChange = { _, _ -> },
                                onResult = {}
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Santali Audio",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play Santali Audio", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Deck Navigation Stepper & Regenerate Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (currentCardIndex > 0) currentCardIndex-- },
                    enabled = currentCardIndex > 0
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Card")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Prev")
                }

                Button(
                    onClick = {
                        randomSeed = System.currentTimeMillis()
                        currentCardIndex = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "New Deck")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Deck")
                }

                Button(
                    onClick = { if (currentCardIndex < flashcards.size - 1) currentCardIndex++ },
                    enabled = currentCardIndex < flashcards.size - 1
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Card")
                }
            }
        }
    }
}
