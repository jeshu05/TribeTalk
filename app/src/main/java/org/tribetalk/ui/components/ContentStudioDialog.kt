package org.tribetalk.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.tribetalk.curriculum.context.TeachingContext
import org.tribetalk.curriculum.ontology.LearningObjective
import org.tribetalk.curriculum.spec.ActivitySpec
import org.tribetalk.curriculum.spec.VisualTheme
import org.tribetalk.fln.model.WorksheetDifficulty
import org.tribetalk.ui.theme.*

enum class StudioState {
    CONFIGURING,
    GENERATING_PROGRESS,
    TEACHER_REVIEW
}

data class GenerationProgressState(
    val stepIndex: Int = 0,
    val steps: List<String> = listOf(
        "Understanding learning objective",
        "Planning activities with Qwen 0.5B",
        "Creating questions & items",
        "Checking mathematical answers",
        "Building offline SVG visuals",
        "Preparing Hindi content",
        "Preparing Santhali Ol Chiki",
        "Ready for classroom"
    )
)

/**
 * Production-grade Teacher Content Studio Modal.
 * Clean, educational, zero AI prompt exposure.
 */
@Composable
fun ContentStudioDialog(
    context: TeachingContext,
    objective: LearningObjective,
    onDismiss: () -> Unit,
    onGenerate: (WorksheetDifficulty, VisualTheme, Boolean, Boolean) -> Unit,
    onUseContent: (ActivitySpec) -> Unit,
    generatedSpec: ActivitySpec? = null,
    isGenerating: Boolean = false,
    progressStep: Int = 0
) {
    var state by remember { mutableStateOf(StudioState.CONFIGURING) }
    var selectedDifficulty by remember { mutableStateOf(context.difficulty) }
    var selectedTheme by remember { mutableStateOf(VisualTheme.GARDEN) }
    var genFlashcards by remember { mutableStateOf(true) }
    var genWorksheet by remember { mutableStateOf(true) }

    LaunchedEffect(isGenerating, generatedSpec) {
        if (isGenerating) {
            state = StudioState.GENERATING_PROGRESS
        } else if (generatedSpec != null && state == StudioState.GENERATING_PROGRESS) {
            state = StudioState.TEACHER_REVIEW
        }
    }

    Dialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (state) {
                                StudioState.CONFIGURING -> "Create Lesson Materials"
                                StudioState.GENERATING_PROGRESS -> "Generating Material"
                                StudioState.TEACHER_REVIEW -> "Teacher Review"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${objective.titleEnglish} • ${objective.titleHindi}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { if (!isGenerating) onDismiss() },
                        enabled = !isGenerating
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Body content based on state
                Box(modifier = Modifier.weight(1f)) {
                    when (state) {
                        StudioState.CONFIGURING -> {
                            ConfiguringView(
                                objective = objective,
                                difficulty = selectedDifficulty,
                                onDifficultyChange = { selectedDifficulty = it },
                                theme = selectedTheme,
                                onThemeChange = { selectedTheme = it },
                                genFlashcards = genFlashcards,
                                onGenFlashcardsChange = { genFlashcards = it },
                                genWorksheet = genWorksheet,
                                onGenWorksheetChange = { genWorksheet = it }
                            )
                        }

                        StudioState.GENERATING_PROGRESS -> {
                            GeneratingProgressView(progressStep = progressStep)
                        }

                        StudioState.TEACHER_REVIEW -> {
                            if (generatedSpec != null) {
                                TeacherReviewView(spec = generatedSpec, objective = objective)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (state) {
                        StudioState.CONFIGURING -> {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    state = StudioState.GENERATING_PROGRESS
                                    onGenerate(selectedDifficulty, selectedTheme, genFlashcards, genWorksheet)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EduPrimary)
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Materials", fontWeight = FontWeight.Bold)
                            }
                        }

                        StudioState.GENERATING_PROGRESS -> {
                            Text(
                                text = "Preparing 100% offline curriculum materials...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        StudioState.TEACHER_REVIEW -> {
                            OutlinedButton(
                                onClick = {
                                    state = StudioState.CONFIGURING
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Edit Options")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    generatedSpec?.let { onUseContent(it) }
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EduPrimaryDark)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Use in Classroom", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfiguringView(
    objective: LearningObjective,
    difficulty: WorksheetDifficulty,
    onDifficultyChange: (WorksheetDifficulty) -> Unit,
    theme: VisualTheme,
    onThemeChange: (VisualTheme) -> Unit,
    genFlashcards: Boolean,
    onGenFlashcardsChange: (Boolean) -> Unit,
    genWorksheet: Boolean,
    onGenWorksheetChange: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current Lesson Context Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "CURRENT INSTRUCTIONAL FOCUS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EduPrimaryDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = objective.titleEnglish, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Number range: ${objective.numberRange.first} to ${objective.numberRange.last} • Allowed activities: Count, Match, Compare", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Materials Selection
        Text(text = "MATERIALS TO CREATE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(
                selected = genFlashcards,
                onClick = { onGenFlashcardsChange(!genFlashcards) },
                label = { Text("🎴 Flashcard Set (6 cards)") }
            )
            FilterChip(
                selected = genWorksheet,
                onClick = { onGenWorksheetChange(!genWorksheet) },
                label = { Text("📝 Practice Worksheet") }
            )
        }

        // Difficulty Selector
        Text(text = "DIFFICULTY LEVEL", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WorksheetDifficulty.values().forEach { diff ->
                FilterChip(
                    selected = difficulty == diff,
                    onClick = { onDifficultyChange(diff) },
                    label = { Text(diff.displayName) }
                )
            }
        }

        // Visual Theme
        Text(text = "CHILD-FRIENDLY VISUAL THEME", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(VisualTheme.GARDEN, VisualTheme.VILLAGE, VisualTheme.CLASSROOM, VisualTheme.NATURE).forEach { thm ->
                FilterChip(
                    selected = theme == thm,
                    onClick = { onThemeChange(thm) },
                    label = { Text(thm.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        // Language Indicator
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = EduPrimaryLight.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Translate, contentDescription = null, tint = EduPrimaryDark)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = "Bilingual Target: Hindi + Santhali (Ol Chiki)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EduPrimaryDark)
                    Text(text = "Speech audio realized automatically through offline TTS.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun GeneratingProgressView(progressStep: Int) {
    val state = remember { GenerationProgressState() }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = EduPrimary,
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                state.steps.forEachIndexed { index, stepName ->
                    val isDone = index < progressStep
                    val isCurrent = index == progressStep

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDone) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = EduGreen, modifier = Modifier.size(18.dp))
                        } else if (isCurrent) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = EduPrimary)
                        } else {
                            Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stepName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDone || isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeacherReviewView(spec: ActivitySpec, objective: LearningObjective) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Verification Badges Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "VERIFIED BY CURRICULUM PIPELINE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                ReviewBadge("Objective Matched: ${objective.id} (${objective.titleEnglish})")
                ReviewBadge("Answers Verified: Target = ${spec.answerSpec.correctValue} (Arithmetically Sound)")
                ReviewBadge("Visual Assets Verified: 100% Offline SVG (${spec.visualSpec.primaryAssetKey})")
                ReviewBadge("Hindi Realized: ${spec.languageSpec.primaryWord}")
                ReviewBadge("Santhali Realized: ${spec.languageSpec.targetWord} (Ol Chiki)")
            }
        }

        // Preview Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "ACTIVITY PREVIEW", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EduPrimaryDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Activity Type: ${spec.activityType.displayName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Hindi Instruction: ${spec.languageSpec.instructionHindi}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Santhali Instruction: ${spec.languageSpec.instructionSantali}", style = MaterialTheme.typography.bodyMedium, color = EduIndigo)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Count Items: ${spec.items.firstOrNull()?.quantity} ${spec.visualSpec.primaryAssetKey}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text(text = "Answer Options: ${spec.items.firstOrNull()?.options?.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ReviewBadge(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF166534))
    }
}
