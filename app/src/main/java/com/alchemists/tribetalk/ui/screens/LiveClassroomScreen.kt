package com.alchemists.tribetalk.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveClassroomScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var simulatorInput by remember { mutableStateOf("") }
    var simulatorOutput by remember { mutableStateOf("") }
    var selectedLanguageIndex by remember { mutableIntStateOf(0) } // 0 = Hindi to Santali, 1 = Santali to Hindi

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
            // Header Info Card
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
                        text = "This screen displays the translation architecture flow. In Phase 2, this will operate as a real-time, voice-activated classroom interface.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Flow Diagram Title
            Text(
                text = "Two-Way Speech Translation Flow",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Flow 1: Teacher -> Student
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

            // Flow 2: Student -> Teacher
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

            // Interactive Simulator Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Translation Simulator",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Direction Selector Tab
                    TabRow(selectedTabIndex = selectedLanguageIndex) {
                        Tab(
                            selected = selectedLanguageIndex == 0,
                            onClick = {
                                selectedLanguageIndex = 0
                                simulatorInput = ""
                                simulatorOutput = ""
                            },
                            text = { Text("Hindi → Santali") }
                        )
                        Tab(
                            selected = selectedLanguageIndex == 1,
                            onClick = {
                                selectedLanguageIndex = 1
                                simulatorInput = ""
                                simulatorOutput = ""
                            },
                            text = { Text("Santali → Hindi") }
                        )
                    }

                    // Text Input
                    OutlinedTextField(
                        value = simulatorInput,
                        onValueChange = { simulatorInput = it },
                        label = { Text(if (selectedLanguageIndex == 0) "Type Hindi Speech Text" else "Type Santali Speech Text") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    // Simulator action button
                    Button(
                        onClick = {
                            if (simulatorInput.isNotBlank()) {
                                simulatorOutput = if (selectedLanguageIndex == 0) {
                                    // Mock Hindi to Santali responses
                                    when (simulatorInput.trim()) {
                                        "नमस्ते", "हैलो" -> "Johar (जोहार)"
                                        "आपका नाम क्या है?" -> "Amaḥ nutum cet’? (आमाः नुतुम चेत’?)"
                                        "आप कैसे हैं?" -> "Ceka menama? (चेका मेनामा?)"
                                        "चलो पढ़ते हैं" -> "Dela bon paṛhao-a (देला बोन पढ़ाओ-आ)"
                                        else -> "[Translated Santali output placeholder]"
                                    }
                                } else {
                                    // Mock Santali to Hindi responses
                                    when (simulatorInput.trim().lowercase()) {
                                        "johar", "जोहार" -> "नमस्ते (Namaste)"
                                        "ceka menama?", "चेका मेनामा?" -> "आप कैसे हैं? (Aap kaise hain?)"
                                        "amaḥ nutum cet’?", "आमाः नुतुम चेत’?" -> "आपका नाम क्या है? (Aapka naam kya hai?)"
                                        else -> "[Translated Hindi output placeholder]"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = simulatorInput.isNotBlank()
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Simulate")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulate Pipeline")
                    }

                    if (simulatorOutput.isNotEmpty()) {
                        // Simulated translation result container
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Simulated Pipeline Output:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = simulatorOutput,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
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
