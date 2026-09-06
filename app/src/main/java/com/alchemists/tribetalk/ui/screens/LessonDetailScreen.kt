package com.alchemists.tribetalk.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alchemists.tribetalk.insights.ClassroomAnalyticsManager
import com.alchemists.tribetalk.lessons.Lesson
import com.alchemists.tribetalk.lessons.LessonRepository
import com.alchemists.tribetalk.translation.TranslationEngine
import com.alchemists.tribetalk.voice.NeuralSpeechSynthesizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Screen: Lesson Detail & Pedagogical Actions.
 *
 * Displays full bilingual Hindi + Santali lesson script, activity instructions, and assessment prompts.
 * Provides audio playback via real/neural Santali TTS, in-place teacher editing,
 * and 1-tap bridges to Create Flashcards and Create Worksheets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    lesson: Lesson,
    translationEngine: TranslationEngine,
    neuralSynthesizer: NeuralSpeechSynthesizer?,
    onCreateFlashcards: (Lesson) -> Unit,
    onCreateWorksheet: (Lesson) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentLesson by remember { mutableStateOf(lesson) }
    var isEditingLesson by remember { mutableStateOf(false) }
    var isPlayingAudio by remember { mutableStateOf(false) }

    // Log classroom action in analytics manager
    LaunchedEffect(lesson.id) {
        ClassroomAnalyticsManager.logLessonOpened(lesson.title, lesson.learningDomain)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            currentLesson.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            ),
                            maxLines = 1
                        )
                        Text(
                            "${currentLesson.learningDomain} • ${currentLesson.grade}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isEditingLesson = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Lesson",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.shadow(1.dp)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Create Flashcards Bridge
                    OutlinedButton(
                        onClick = { onCreateFlashcards(currentLesson) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Style, contentDescription = "Flashcards", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("FLASHCARDS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Create Worksheet Bridge
                    Button(
                        onClick = { onCreateWorksheet(currentLesson) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1.2f).height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Create, contentDescription = "Worksheet", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WORKSHEET", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // NIPUN Alignment Metadata Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Learning Skill / Competency:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.outline
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = currentLesson.grade,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = currentLesson.learningOutcome,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = currentLesson.hindiIntroduction,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Audio Playback Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2F8F83).copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Santali Classroom Audio",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E6B61)
                            )
                            Text(
                                "Real Santali TTS (Indic Parler / VITS Engine)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Button(
                            onClick = {
                                if (isPlayingAudio) return@Button
                                isPlayingAudio = true
                                neuralSynthesizer?.speak(
                                    text = currentLesson.lessonScriptSantali,
                                    languageCode = "sat",
                                    onStart = { isPlayingAudio = true },
                                    onDone = { isPlayingAudio = false },
                                    onError = { 
                                        isPlayingAudio = false
                                        Toast.makeText(context, "TTS Notice: $it", Toast.LENGTH_SHORT).show()
                                    }
                                ) ?: run {
                                    Toast.makeText(context, "Playing Santali Audio...", Toast.LENGTH_SHORT).show()
                                    isPlayingAudio = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F8F83)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            if (isPlayingAudio) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Playing...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Play", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PLAY SANTALI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 1: Lesson Script (पाठ संवाद / ᱯᱟᱴᱷ)
            item {
                BilingualLessonSectionCard(
                    title = "1. Lesson Script (पाठ संवाद)",
                    hindiContent = currentLesson.lessonScriptHindi,
                    santaliContent = currentLesson.lessonScriptSantali
                )
            }

            // Section 2: Activity Instructions (कक्षा गतिविधि निर्देश)
            item {
                BilingualLessonSectionCard(
                    title = "2. Activity Instructions (कक्षा गतिविधि निर्देश)",
                    hindiContent = currentLesson.activityInstructionsHindi,
                    santaliContent = currentLesson.activityInstructionsSantali
                )
            }

            // Section 3: Assessment Prompts (आकलन एवं प्रश्न)
            item {
                BilingualLessonSectionCard(
                    title = "3. Assessment Prompts (आकलन प्रश्न)",
                    hindiContent = currentLesson.assessmentPromptsHindi,
                    santaliContent = currentLesson.assessmentPromptsSantali
                )
            }
        }
    }

    // Teacher Editing Dialog
    if (isEditingLesson) {
        TeacherEditLessonDialog(
            lesson = currentLesson,
            translationEngine = translationEngine,
            onDismiss = { isEditingLesson = false },
            onSave = { updatedLesson ->
                currentLesson = updatedLesson
                LessonRepository.updateLesson(updatedLesson)
                isEditingLesson = false
                Toast.makeText(context, "Lesson updated successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * Reusable card displaying paired Hindi and Santali text blocks.
 */
@Composable
fun BilingualLessonSectionCard(
    title: String,
    hindiContent: String,
    santaliContent: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Paired Blocks
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Hindi Section
                Column {
                    Text(
                        text = "Hindi (हिन्दी):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = hindiContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Santali Section
                Column {
                    Text(
                        text = "Santali (ᱥᱟᱱᱛᱟᱲᱤ):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2F8F83)
                    )
                    Text(
                        text = santaliContent,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF1E6B61)
                    )
                }
            }
        }
    }
}

/**
 * Dialog for teacher editing of lesson script, activities, and assessment prompts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherEditLessonDialog(
    lesson: Lesson,
    translationEngine: TranslationEngine,
    onDismiss: () -> Unit,
    onSave: (Lesson) -> Unit
) {
    var title by remember { mutableStateOf(lesson.title) }
    var scriptHindi by remember { mutableStateOf(lesson.lessonScriptHindi) }
    var scriptSantali by remember { mutableStateOf(lesson.lessonScriptSantali) }
    var activityHindi by remember { mutableStateOf(lesson.activityInstructionsHindi) }
    var activitySantali by remember { mutableStateOf(lesson.activityInstructionsSantali) }
    var assessmentHindi by remember { mutableStateOf(lesson.assessmentPromptsHindi) }
    var assessmentSantali by remember { mutableStateOf(lesson.assessmentPromptsSantali) }

    var isTranslating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Lesson", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Lesson Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 1. Script Section
                item {
                    OutlinedTextField(
                        value = scriptHindi,
                        onValueChange = { scriptHindi = it },
                        label = { Text("Lesson Script (Hindi)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedButton(
                        onClick = {
                            if (scriptHindi.isBlank()) return@OutlinedButton
                            isTranslating = true
                            coroutineScope.launch {
                                val res = withContext(Dispatchers.Default) {
                                    LessonRepository.translateSection(scriptHindi, translationEngine)
                                }
                                scriptSantali = res
                                isTranslating = false
                            }
                        },
                        enabled = !isTranslating && scriptHindi.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Translate, contentDescription = "Translate", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TRANSLATE SCRIPT TO SANTALI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    OutlinedTextField(
                        value = scriptSantali,
                        onValueChange = { scriptSantali = it },
                        label = { Text("Lesson Script (Santali)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 2. Activity Instructions
                item {
                    OutlinedTextField(
                        value = activityHindi,
                        onValueChange = { activityHindi = it },
                        label = { Text("Activity Instructions (Hindi)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedButton(
                        onClick = {
                            if (activityHindi.isBlank()) return@OutlinedButton
                            isTranslating = true
                            coroutineScope.launch {
                                val res = withContext(Dispatchers.Default) {
                                    LessonRepository.translateSection(activityHindi, translationEngine)
                                }
                                activitySantali = res
                                isTranslating = false
                            }
                        },
                        enabled = !isTranslating && activityHindi.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Translate, contentDescription = "Translate", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TRANSLATE ACTIVITIES TO SANTALI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    OutlinedTextField(
                        value = activitySantali,
                        onValueChange = { activitySantali = it },
                        label = { Text("Activity Instructions (Santali)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 3. Assessment Prompts
                item {
                    OutlinedTextField(
                        value = assessmentHindi,
                        onValueChange = { assessmentHindi = it },
                        label = { Text("Assessment Prompts (Hindi)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedButton(
                        onClick = {
                            if (assessmentHindi.isBlank()) return@OutlinedButton
                            isTranslating = true
                            coroutineScope.launch {
                                val res = withContext(Dispatchers.Default) {
                                    LessonRepository.translateSection(assessmentHindi, translationEngine)
                                }
                                assessmentSantali = res
                                isTranslating = false
                            }
                        },
                        enabled = !isTranslating && assessmentHindi.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Translate, contentDescription = "Translate", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TRANSLATE ASSESSMENT TO SANTALI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    OutlinedTextField(
                        value = assessmentSantali,
                        onValueChange = { assessmentSantali = it },
                        label = { Text("Assessment Prompts (Santali)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = lesson.copy(
                        title = title.trim(),
                        lessonScriptHindi = scriptHindi.trim(),
                        lessonScriptSantali = scriptSantali.trim(),
                        activityInstructionsHindi = activityHindi.trim(),
                        activityInstructionsSantali = activitySantali.trim(),
                        assessmentPromptsHindi = assessmentHindi.trim(),
                        assessmentPromptsSantali = assessmentSantali.trim()
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("SAVE CHANGES", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
