package org.tribetalk.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import org.tribetalk.worksheet.QuestionType
import org.tribetalk.worksheet.Worksheet
import org.tribetalk.worksheet.WorksheetGenerator
import org.tribetalk.worksheet.WorksheetPdfExporter
import org.tribetalk.worksheet.WorksheetQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Screen 2: Worksheet Preview & Teacher Editing.
 *
 * Displays the generated bilingual activities in stacked Hindi + Santali blocks.
 * Enables in-place teacher corrections, dynamic Hindi -> Santali re-translation,
 * and native offline A4 PDF export with Open, Share, and Print actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksheetPreviewScreen(
    initialWorksheet: Worksheet,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentWorksheet by remember { mutableStateOf(initialWorksheet) }
    var editingQuestionIndex by remember { mutableStateOf<Int?>(null) }
    var exportedPdfFile by remember { mutableStateOf<File?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }

    val pdfExporter = remember { WorksheetPdfExporter(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Worksheet Preview",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            "${currentWorksheet.topic} • ${currentWorksheet.questions.size} Activities",
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
                    IconButton(
                        onClick = {
                            try {
                                val file = pdfExporter.exportPdf(currentWorksheet)
                                exportedPdfFile = file
                                showExportDialog = true
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export PDF",
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val file = pdfExporter.exportPdf(currentWorksheet)
                                pdfExporter.sharePdf(file)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SHARE")
                    }

                    Button(
                        onClick = {
                            try {
                                val file = pdfExporter.exportPdf(currentWorksheet)
                                exportedPdfFile = file
                                showExportDialog = true
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1.5f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EXPORT PDF", fontWeight = FontWeight.Bold)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Overview Card
            item {
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentWorksheet.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = currentWorksheet.grade ?: "FLN Grade 1-3",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = currentWorksheet.instructions,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Question List
            itemsIndexed(currentWorksheet.questions) { index, question ->
                QuestionPreviewCard(
                    index = index + 1,
                    question = question,
                    onEditClick = { editingQuestionIndex = index }
                )
            }
        }
    }

    // Teacher Editing Dialog
    editingQuestionIndex?.let { index ->
        val questionToEdit = currentWorksheet.questions[index]
        TeacherEditQuestionDialog(
            question = questionToEdit,
            questionNumber = index + 1,
            onDismiss = { editingQuestionIndex = null },
            onSave = { updatedQuestion ->
                val newQuestions = currentWorksheet.questions.toMutableList().apply {
                    set(index, updatedQuestion)
                }
                currentWorksheet = currentWorksheet.copy(questions = newQuestions)
                editingQuestionIndex = null
                Toast.makeText(context, "Saved changes to Question #${index + 1}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Export Success Dialog
    if (showExportDialog && exportedPdfFile != null) {
        val file = exportedPdfFile!!
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Worksheet PDF Exported", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("File created successfully on local device:", style = MaterialTheme.typography.bodyMedium)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = file.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pdfExporter.openPdf(file)
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = "Open", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("OPEN / VIEW")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { pdfExporter.sharePdf(file) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SHARE")
                    }
                    OutlinedButton(onClick = { pdfExporter.printPdf(file) }) {
                        Icon(Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PRINT")
                    }
                }
            }
        )
    }
}

/**
 * Clean classroom question preview card with stacked Hindi + Santali bilingual blocks.
 */
@Composable
fun QuestionPreviewCard(
    index: Int,
    question: WorksheetQuestion,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Type Badge + Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "$index. ${question.type.displayName}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (question.editable) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Question",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Stacked Bilingual Blocks (Hindi + Santali)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(8.dp))
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
                        text = question.hindiText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Santali Section
                Column {
                    Text(
                        text = "Santali (ᱥᱟᱱᱛᱟᱲᱤ):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2F8F83) // Success Teal
                    )
                    Text(
                        text = question.santaliText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Options (for Multiple Choice)
            if (question.type == QuestionType.MULTIPLE_CHOICE && question.options.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    question.options.forEach { opt ->
                        Text(
                            text = "  • $opt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Answer Space / Answer Line
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Answer Space:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (question.answer.isNotBlank()) {
                        Text(
                            text = question.answer,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog for teacher editing of question text, Santali translation, options, answer, and type.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherEditQuestionDialog(
    question: WorksheetQuestion,
    questionNumber: Int,
    onDismiss: () -> Unit,
    onSave: (WorksheetQuestion) -> Unit
) {
    var hindiText by remember { mutableStateOf(question.hindiText) }
    var santaliText by remember { mutableStateOf(question.santaliText) }
    var selectedType by remember { mutableStateOf(question.type) }
    var optionsText by remember { mutableStateOf(question.options.joinToString("\n")) }
    var answerText by remember { mutableStateOf(question.answer) }
    var isTranslating by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val worksheetGenerator = remember { WorksheetGenerator() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Activity #$questionNumber", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Hindi Field
                OutlinedTextField(
                    value = hindiText,
                    onValueChange = {
                        hindiText = it
                        statusMessage = null
                    },
                    label = { Text("Hindi Text (हिन्दी)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                // Secondary Action: Translate to Santali
                OutlinedButton(
                    onClick = {
                        if (hindiText.isBlank()) {
                            statusMessage = "Please enter Hindi text to translate."
                            return@OutlinedButton
                        }
                        isTranslating = true
                        statusMessage = null
                        coroutineScope.launch {
                            try {
                                val result = withContext(Dispatchers.Default) {
                                    worksheetGenerator.retranslateQuestionText(hindiText, selectedType)
                                }
                                santaliText = result
                                if (result == "Translation unavailable for this text") {
                                    statusMessage = "Translation unavailable for this text."
                                } else {
                                    statusMessage = "✓ Translated from edited Hindi"
                                }
                            } catch (e: Exception) {
                                statusMessage = "Unable to translate. Please try again."
                            } finally {
                                isTranslating = false
                            }
                        }
                    },
                    enabled = !isTranslating && hindiText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isTranslating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Translating...", style = MaterialTheme.typography.labelMedium)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Translate to Santali",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "TRANSLATE TO SANTALI",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                if (statusMessage != null) {
                    Text(
                        text = statusMessage ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (statusMessage?.startsWith("✓") == true) Color(0xFF2F8F83) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Santali Field (Editable manually, teacher changes are preserved)
                OutlinedTextField(
                    value = santaliText,
                    onValueChange = {
                        santaliText = it
                        statusMessage = null
                    },
                    label = { Text("Santali Text (ᱥᱟᱱᱛᱟᱲᱤ)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                // Options (for MCQ)
                if (selectedType == QuestionType.MULTIPLE_CHOICE) {
                    OutlinedTextField(
                        value = optionsText,
                        onValueChange = { optionsText = it },
                        label = { Text("Options (one per line)") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Answer Field
                OutlinedTextField(
                    value = answerText,
                    onValueChange = { answerText = it },
                    label = { Text("Answer / उत्तर") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedOptions = if (selectedType == QuestionType.MULTIPLE_CHOICE) {
                        optionsText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                    } else {
                        question.options
                    }

                    val updated = question.copy(
                        type = selectedType,
                        hindiText = hindiText.trim(),
                        santaliText = santaliText.trim(),
                        options = parsedOptions,
                        answer = answerText.trim()
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
