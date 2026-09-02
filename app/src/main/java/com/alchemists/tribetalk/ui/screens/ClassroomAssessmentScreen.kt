package com.alchemists.tribetalk.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.alchemists.tribetalk.analytics.models.ActivityType
import com.alchemists.tribetalk.analytics.models.AssessmentAttempt
import com.alchemists.tribetalk.analytics.repository.ClassroomRosterRepository
import com.alchemists.tribetalk.ui.viewmodel.AssessmentViewModel
import com.alchemists.tribetalk.voice.VoiceTranslationBridge
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomAssessmentScreen(
    viewModel: AssessmentViewModel,
    voiceTranslationBridge: VoiceTranslationBridge,
    onFinishedAssessment: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val assessment = uiState.assessment

    val roster = remember { ClassroomRosterRepository.getRoster(context) }
    var selectedStudentId by remember { mutableStateOf(roster.students.first()) }

    if (assessment == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentQ = assessment.questions.getOrNull(uiState.currentQuestionIndex)
    if (currentQ == null) {
        onFinishedAssessment()
        return
    }

    val activityType = when (currentQ.type) {
        com.alchemists.tribetalk.assessment.models.QuestionType.VISUAL_MULTIPLE_CHOICE -> ActivityType.VISUAL_COUNTING
        com.alchemists.tribetalk.assessment.models.QuestionType.IMAGE_MATCHING -> ActivityType.NUMBER_RECOGNITION
        com.alchemists.tribetalk.assessment.models.QuestionType.IDENTIFY_SELECT -> ActivityType.PICTURE_IDENTIFICATION
        com.alchemists.tribetalk.assessment.models.QuestionType.ORDERING -> ActivityType.MATCHING
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Teacher Assessment Mode • Class 2A",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            "Assessing: $selectedStudentId • Q ${uiState.currentQuestionIndex + 1}/${assessment.questions.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Student Selection Chip Bar
            Text(
                text = "SELECT STUDENT BEING ASSESSED",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.align(Alignment.Start)
            )

            ScrollableTabRow(
                selectedTabIndex = roster.students.indexOf(selectedStudentId).coerceAtLeast(0),
                edgePadding = 0.dp
            ) {
                roster.students.forEach { sId ->
                    Tab(
                        selected = sId == selectedStudentId,
                        onClick = { selectedStudentId = sId }
                    ) {
                        Text(sId, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Question Card Stimulus
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = activityType.displayName,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Santali Audio Trigger
                        IconButton(
                            onClick = {
                                voiceTranslationBridge.translateAndSpeak(
                                    recognizedText = currentQ.promptHindi,
                                    sourceLanguage = com.alchemists.tribetalk.translation.Language.HINDI,
                                    targetLanguage = com.alchemists.tribetalk.translation.Language.SANTALI,
                                    isVoiceBridgeEnabled = true,
                                    onStateChange = { _, _ -> },
                                    onResult = {}
                                )
                            }
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Audio", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    Text(
                        text = currentQ.stimulus?.iconEmoji ?: "🔢",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = currentQ.promptSantali,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = currentQ.promptHindi,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Options Selection Grid
            currentQ.options.forEachIndexed { index, option ->
                val isSelected = uiState.selectedOptionIndex == index
                OutlinedButton(
                    onClick = { viewModel.selectOption(index) },
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = option.textSantali.ifBlank { option.textHindi },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Rapid Teacher Controls (Mark Correct / Mark Incorrect / Auto Next Student)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val isCorr = true
                        recordAndAdvance(context, viewModel, selectedStudentId, currentQ, assessment.lessonId, activityType, "Correct", isCorr)
                        selectedStudentId = ClassroomRosterRepository.getNextStudent(selectedStudentId, roster)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Correct")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("✓ Correct", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = {
                        val isCorr = false
                        recordAndAdvance(context, viewModel, selectedStudentId, currentQ, assessment.lessonId, activityType, "Incorrect", isCorr)
                        selectedStudentId = ClassroomRosterRepository.getNextStudent(selectedStudentId, roster)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Incorrect")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("✗ Incorrect", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            OutlinedButton(
                onClick = {
                    viewModel.submitAnswer(context)
                    if (uiState.currentQuestionIndex >= assessment.questions.size - 1) {
                        onFinishedAssessment()
                    } else {
                        viewModel.nextQuestion()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("Submit & Next Question")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

private fun recordAndAdvance(
    context: android.content.Context,
    viewModel: AssessmentViewModel,
    studentId: String,
    currentQ: com.alchemists.tribetalk.assessment.models.AssessmentQuestionItem,
    lessonId: String,
    activityType: ActivityType,
    answerText: String,
    isCorrect: Boolean
) {
    val attempt = AssessmentAttempt(
        id = "att_${UUID.randomUUID()}",
        classId = "class_2a",
        studentId = studentId,
        lessonId = lessonId,
        activityId = "act_$lessonId",
        learningOutcomeId = currentQ.learningOutcomeId,
        questionId = currentQ.id,
        activityType = activityType,
        selectedAnswer = answerText,
        expectedAnswer = "${currentQ.correctAnswerIndex}",
        isCorrect = isCorrect
    )
    com.alchemists.tribetalk.analytics.repository.ResponsePersistenceRepository.recordAttempt(context, attempt)
    viewModel.nextQuestion()
}
