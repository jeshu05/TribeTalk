package com.alchemists.tribetalk.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alchemists.tribetalk.curriculum.generator.FLNWorksheetGenerator
import com.alchemists.tribetalk.curriculum.models.DifficultyLevel
import com.alchemists.tribetalk.curriculum.repository.FLNCurriculumRepository
import com.alchemists.tribetalk.translation.Language
import com.alchemists.tribetalk.voice.VoiceTranslationBridge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksheetScreen(
    voiceTranslationBridge: VoiceTranslationBridge,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedLessonIndex by remember { mutableIntStateOf(0) }
    var selectedDifficulty by remember { mutableStateOf(DifficultyLevel.BEGINNER) }
    var randomSeed by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val currentLesson = FLNCurriculumRepository.lessons[selectedLessonIndex]
    val currentOutcome = currentLesson.learningOutcome

    val worksheet = remember(selectedLessonIndex, selectedDifficulty, randomSeed) {
        FLNWorksheetGenerator.generateWorksheet(
            outcome = currentOutcome,
            grade = currentLesson.grade,
            domain = currentLesson.domain,
            numQuestions = 4,
            difficulty = selectedDifficulty,
            seed = randomSeed
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Worksheet Generator",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 22.sp
                            )
                        )
                        Text(
                            "Stage 2 • Dynamic Visual Worksheets",
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
                    // "Regenerate / New Set" Button
                    IconButton(onClick = { randomSeed = System.currentTimeMillis() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate / New Set",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // NIPUN Outcome Selector & Difficulty Row
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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

                        // "Regenerate / New Set" Button
                        Button(
                            onClick = { randomSeed = System.currentTimeMillis() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "New Set",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Set", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    Text(
                        text = "Outcome: ${currentOutcome.descriptionHindi}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Difficulty Level Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DifficultyLevel.values().forEach { level ->
                            FilterChip(
                                selected = selectedDifficulty == level,
                                onClick = { selectedDifficulty = level },
                                label = { Text(level.labelHindi) }
                            )
                        }
                    }
                }
            }

            Text(
                text = "GENERATED WORKSHEET ITEMS (${worksheet.items.size})",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            // Render Generated Worksheet Questions
            worksheet.items.forEachIndexed { itemIdx, item ->
                val actItem = item.activityItem
                var selectedOptionIndex by remember(item.id, randomSeed) { mutableStateOf<Int?>(null) }
                var isSubmitted by remember(item.id, randomSeed) { mutableStateOf(false) }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Q${itemIdx + 1}. ${item.promptHindi}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            // Play Santali Audio Button
                            IconButton(
                                onClick = {
                                    voiceTranslationBridge.translateAndSpeak(
                                        recognizedText = item.promptHindi,
                                        sourceLanguage = Language.HINDI,
                                        targetLanguage = Language.SANTALI,
                                        isVoiceBridgeEnabled = true,
                                        onStateChange = { _, _ -> },
                                        onResult = {}
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Santali Audio",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Text(
                            text = "Santali: ${item.promptSantali}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Render Visual Stimulus (Emoji objects)
                        if (actItem?.stimulus != null) {
                            val stim = actItem.stimulus
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val iconList = List(stim.count) { stim.iconEmoji }
                                    Text(
                                        text = iconList.joinToString("  "),
                                        fontSize = 32.sp
                                    )
                                }
                            }
                        }

                        // Render Multiple Choice Options
                        item.optionsHindi.forEachIndexed { optIdx, optHindi ->
                            val optSantali = item.optionsSantali.getOrNull(optIdx) ?: optHindi
                            val isSelectedOpt = selectedOptionIndex == optIdx

                            Surface(
                                color = if (isSelectedOpt) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (isSelectedOpt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        selectedOptionIndex = optIdx
                                        isSubmitted = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelectedOpt,
                                        onClick = {
                                            selectedOptionIndex = optIdx
                                            isSubmitted = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$optHindi ($optSantali)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { isSubmitted = true },
                            enabled = selectedOptionIndex != null,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Check Answer")
                        }

                        if (isSubmitted && selectedOptionIndex != null && actItem != null) {
                            val isCorrect = selectedOptionIndex == actItem.correctAnswerIndex
                            Surface(
                                color = if (isCorrect) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (isCorrect) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                                        RoundedCornerShape(6.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (isCorrect) "✓ Correct Answer!" else "✗ Try Again",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isCorrect) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = actItem.explanationHindi,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
