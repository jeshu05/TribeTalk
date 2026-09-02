package com.alchemists.tribetalk.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alchemists.tribetalk.ui.viewmodel.AssessmentViewModel
import com.alchemists.tribetalk.voice.VoiceTranslationBridge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAssessmentScreen(
    viewModel: AssessmentViewModel,
    voiceTranslationBridge: VoiceTranslationBridge,
    onFinishedAssessment: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val assessment = uiState.assessment

    if (uiState.isCompleted) {
        LaunchedEffect(Unit) {
            onFinishedAssessment()
        }
    }

    if (assessment == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentQ = assessment.questions.getOrNull(uiState.currentQuestionIndex) ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = assessment.titleSantali,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = "Question ${uiState.currentQuestionIndex + 1} of ${assessment.questions.size}",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stepper Progress Line
            LinearProgressIndicator(
                progress = { (uiState.currentQuestionIndex + 1).toFloat() / assessment.questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            // Santali Prompts Header Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentQ.promptSantali,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        // 🔊 Play Santali Audio Button
                        IconButton(
                            onClick = {
                                viewModel.playSantaliAudio(
                                    promptHindi = currentQ.promptHindi,
                                    promptSantali = currentQ.promptSantali,
                                    voiceTranslationBridge = voiceTranslationBridge
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Santali Audio",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Text(
                        text = "Hindi: ${currentQ.promptHindi}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "HUD Guide: ${currentQ.promptPhonetic}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Visual Stimulus Box (Child-Friendly Rendering)
            if (currentQ.stimulus != null) {
                val stim = currentQ.stimulus
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val emojiList = List(stim.count) { stim.iconEmoji }
                        Text(
                            text = emojiList.joinToString("  "),
                            fontSize = 38.sp
                        )
                    }
                }
            }

            // Large Touch Option Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                currentQ.options.forEachIndexed { optIdx, option ->
                    val isSelected = uiState.selectedOptionIndex == optIdx

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clickable { viewModel.selectOption(optIdx) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.selectOption(optIdx) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (option.iconEmoji != null) {
                                    Text(text = option.iconEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = "${option.textSantali} (${option.textHindi})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Submit Answer Button
            Button(
                onClick = { viewModel.submitAnswer(context) },
                enabled = uiState.selectedOptionIndex != null && !uiState.isSubmitted,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Submit Answer", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            // Immediate Feedback Box
            if (uiState.isSubmitted && uiState.selectedOptionIndex != null) {
                val isCorrect = uiState.selectedOptionIndex == currentQ.correctAnswerIndex

                Surface(
                    color = if (isCorrect) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isCorrect) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isCorrect) "✓ ᱴᱷᱤᱠ ᱜᱮᱭᱟ! (Correct!)" else "🙂 ᱟᱨᱦᱚᱸ ᱪᱮᱥᱴᱟᱭ ᱢᱮ (Try Again 🙂)",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isCorrect) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = currentQ.explanationHindi,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Button(
                            onClick = { viewModel.nextQuestion(context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCorrect) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (uiState.currentQuestionIndex < assessment.questions.size - 1) "Next Question" else "View Results",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                            }
                        }
                    }
                }
            }
        }
    }
}
