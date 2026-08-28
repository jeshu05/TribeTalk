package com.alchemists.tribetalk.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alchemists.tribetalk.translation.Language
import com.alchemists.tribetalk.translation.TranslationEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveClassroomScreen(
    translationEngine: TranslationEngine,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sourceLanguage by remember { mutableStateOf(Language.HINDI) }
    var targetLanguage by remember { mutableStateOf(Language.SANTALI) }

    var sourceExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }

    var inputValue by remember { mutableStateOf("") }
    var outputValue by remember { mutableStateOf("") }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Classroom") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
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
            // Header Info Card (Phase 1, preserved)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Information",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "This screen displays the translation architecture flow. Use the workspace below to simulate text translation between Hindi and Santali.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Flow Diagram Title (Phase 1, preserved)
            Text(
                text = "Two-Way Speech Translation Flow",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Flow 1: Teacher -> Student (Phase 1, preserved)
            FlowCard(
                title = "1. Teacher → Student (Hindi to Santali)",
                steps = listOf(
                    "🎤 Teacher speaks in Hindi",
                    "⚙️ ASR processes Hindi Speech to Text",
                    "📝 Hindi Text generated",
                    "🔄 Hindi → Santali Machine Translation",
                    "🔊 Student receives Santali voice/text"
                )
            )

            // Flow 2: Student -> Teacher (Phase 1, preserved)
            FlowCard(
                title = "2. Student → Teacher (Santali to Hindi)",
                steps = listOf(
                    "🎤 Student speaks in Santali",
                    "⚙️ ASR processes Santali Speech to Text",
                    "📝 Santali Text generated",
                    "🔄 Santali → Hindi Machine Translation",
                    "🔊 Teacher receives Hindi voice/text"
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Functional Translation Workspace Section (Phase 2, updated)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Text Translation Workspace",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Language Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Source Selector
                        Box {
                            TextButton(onClick = { sourceExpanded = true }) {
                                Text(
                                    text = sourceLanguage.displayName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            DropdownMenu(
                                expanded = sourceExpanded,
                                onDismissRequest = { sourceExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Hindi") },
                                    onClick = {
                                        sourceLanguage = Language.HINDI
                                        sourceExpanded = false
                                        if (sourceLanguage == targetLanguage) {
                                            targetLanguage = Language.SANTALI
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Santali") },
                                    onClick = {
                                        sourceLanguage = Language.SANTALI
                                        sourceExpanded = false
                                        if (sourceLanguage == targetLanguage) {
                                            targetLanguage = Language.HINDI
                                        }
                                    }
                                )
                            }
                        }

                        // Swap Button
                        Button(
                            onClick = {
                                val tempLang = sourceLanguage
                                sourceLanguage = targetLanguage
                                targetLanguage = tempLang

                                val tempVal = inputValue
                                inputValue = outputValue
                                outputValue = tempVal
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text("Swap")
                        }

                        // Target Selector
                        Box {
                            TextButton(onClick = { targetExpanded = true }) {
                                Text(
                                    text = targetLanguage.displayName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            DropdownMenu(
                                expanded = targetExpanded,
                                onDismissRequest = { targetExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Hindi") },
                                    onClick = {
                                        targetLanguage = Language.HINDI
                                        targetExpanded = false
                                        if (targetLanguage == sourceLanguage) {
                                            sourceLanguage = Language.SANTALI
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Santali") },
                                    onClick = {
                                        targetLanguage = Language.SANTALI
                                        targetExpanded = false
                                        if (targetLanguage == sourceLanguage) {
                                            sourceLanguage = Language.HINDI
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Multiline Text Input
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        placeholder = { Text("Enter or type a sentence...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        maxLines = 5
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                if (inputValue.isNotBlank()) {
                                    outputValue = translationEngine.translate(
                                        inputValue,
                                        sourceLanguage,
                                        targetLanguage
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = inputValue.isNotBlank()
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Translate")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Translate")
                        }

                        OutlinedButton(
                            onClick = {
                                inputValue = ""
                                outputValue = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear")
                        }
                    }

                    if (outputValue.isNotEmpty()) {
                        // Output Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Translated Text (Demo Output)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = outputValue,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(outputValue))
                                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Copy Output")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlowCard(
    title: String,
    steps: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider()
            steps.forEachIndexed { index, step ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (index < steps.lastIndex) {
                    // Show a down arrow separator between steps
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp)
                    ) {
                        Text(
                            text = "↓",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
