package com.alchemists.tribetalk.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alchemists.tribetalk.translation.TranslationEngine
import com.alchemists.tribetalk.worksheet.Worksheet
import com.alchemists.tribetalk.worksheet.WorksheetGenerator

/**
 * Screen 1: Worksheet Generator Input.
 *
 * Allows a teacher to enter/select a lesson topic, enter Hindi lesson sentences,
 * and trigger deterministic bilingual worksheet generation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksheetGeneratorScreen(
    translationEngine: TranslationEngine,
    onWorksheetGenerated: (Worksheet) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var topic by remember { mutableStateOf("Animals") }
    var grade by remember { mutableStateOf("Grade 3") }
    var hindiContent by remember {
        mutableStateOf("गाय एक पालतू जानवर है।\nकुत्ता एक पालतू जानवर है।\nमछली पानी में रहती है।")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val generator = remember(translationEngine) { WorksheetGenerator(translationEngine) }
    val scrollState = rememberScrollState()

    // Sample Presets for quick classroom use
    val presets = listOf(
        Triple("Animals", "Grade 3", "गाय एक पालतू जानवर है।\nकुत्ता एक पालतू जानवर है।\nमछली पानी में रहती है।"),
        Triple("Classroom", "Grade 2", "किताब खोलो।\nलिखना शुरू करो।\nब्लैकबोर्ड देखो।\nशांत रहो।"),
        Triple("Nature", "Grade 1", "सूरज पूर्व में उगता है।\nपेड़ हमें छाया देते हैं।\nपानी जीवन के लिए जरूरी है।"),
        Triple("Numbers & FLN", "Grade 2", "एक दो तीन चार।\nपांच छह सात आठ।\nसंख्या की गिनती करो।")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Create Worksheet",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            "Auto-Generated Bilingual FLN Activities",
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
                            contentDescription = "Back to Dashboard",
                            tint = MaterialTheme.colorScheme.onSurface
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
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Sample Preset Chips
            Text(
                text = "Quick Lesson Presets",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { (presetTopic, presetGrade, presetContent) ->
                    SuggestionChip(
                        onClick = {
                            topic = presetTopic
                            grade = presetGrade
                            hindiContent = presetContent
                            errorMessage = null
                        },
                        label = { Text(presetTopic, fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (topic == presetTopic) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = if (topic == presetTopic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Topic Field
                    OutlinedTextField(
                        value = topic,
                        onValueChange = {
                            topic = it
                            errorMessage = null
                        },
                        label = { Text("Topic / विषय") },
                        placeholder = { Text("e.g. Animals, Classroom, Nature") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (topic.isNotEmpty()) {
                                IconButton(onClick = { topic = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Topic")
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Class / Grade Field
                    OutlinedTextField(
                        value = grade,
                        onValueChange = { grade = it },
                        label = { Text("Class / Grade (Optional)") },
                        placeholder = { Text("e.g. Grade 1, Grade 2, Grade 3") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Hindi Lesson / Content Field
                    OutlinedTextField(
                        value = hindiContent,
                        onValueChange = {
                            hindiContent = it
                            errorMessage = null
                        },
                        label = { Text("Lesson Content in Hindi (हिन्दी पाठ)") },
                        placeholder = {
                            Text("Enter simple lesson sentences...\nगाय एक पालतू जानवर है।\nमछली पानी में रहती है।")
                        },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Error Feedback if any
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dominant Generate Worksheet Button
            Button(
                onClick = {
                    if (topic.isBlank()) {
                        errorMessage = "Please enter a lesson topic (e.g. Animals, Nature)."
                        return@Button
                    }
                    if (hindiContent.isBlank()) {
                        errorMessage = "Please enter Hindi lesson sentences to generate activities."
                        return@Button
                    }

                    isGenerating = true
                    try {
                        val worksheet = generator.generateWorksheet(
                            topic = topic.trim(),
                            hindiContent = hindiContent.trim(),
                            grade = grade.trim().ifBlank { null }
                        )
                        onWorksheetGenerated(worksheet)
                    } catch (e: Exception) {
                        errorMessage = "Failed to generate worksheet: ${e.message}"
                    } finally {
                        isGenerating = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Generating Activities...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Generate",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "GENERATE WORKSHEET",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            // Info note
            Text(
                text = "✓ Generates 5–10 FLN activities (Matching, Fill in the blanks, Multiple choice, Vocabulary, Reading) offline.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
