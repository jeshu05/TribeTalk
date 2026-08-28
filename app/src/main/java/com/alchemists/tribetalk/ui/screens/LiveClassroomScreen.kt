package com.alchemists.tribetalk.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.core.content.ContextCompat
import com.alchemists.tribetalk.translation.Language
import com.alchemists.tribetalk.translation.TranslationEngine
import com.alchemists.tribetalk.translation.TranslationEntry
import com.alchemists.tribetalk.translation.TranslationResult
import com.alchemists.tribetalk.translation.OfflineFLNTranslationEngine
import com.alchemists.tribetalk.voice.TextToSpeechManager
import com.alchemists.tribetalk.voice.VoiceInputManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveClassroomScreen(
    translationEngine: TranslationEngine,
    voiceInputManager: VoiceInputManager,
    textToSpeechManager: TextToSpeechManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Teacher -> Student State
    var teacherInput by remember { mutableStateOf("") }
    var teacherOutput by remember { mutableStateOf<TranslationResult?>(null) }
    var teacherVoiceState by remember { mutableStateOf("Idle") }
    var teacherSpeechState by remember { mutableStateOf("Idle") }
    var teacherIsEditing by remember { mutableStateOf(false) }
    var teacherEditedText by remember { mutableStateOf("") }
    var teacherStatusMessage by remember { mutableStateOf("") }

    // Student -> Teacher State
    var studentInput by remember { mutableStateOf("") }
    var studentOutput by remember { mutableStateOf<TranslationResult?>(null) }
    var studentVoiceState by remember { mutableStateOf("Idle") }
    var studentSpeechState by remember { mutableStateOf("Idle") }
    var studentIsEditing by remember { mutableStateOf(false) }
    var studentEditedText by remember { mutableStateOf("") }
    var studentStatusMessage by remember { mutableStateOf("") }

    // Track which language side requested recording when permission is prompted
    var pendingLangRequest by remember { mutableStateOf<Language?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                pendingLangRequest?.let { lang ->
                    if (lang == Language.HINDI) {
                        startTeacherListening(
                            voiceInputManager,
                            translationEngine,
                            onResult = { text ->
                                teacherInput = text
                                teacherOutput = translationEngine.translate(text, Language.HINDI, Language.SANTALI)
                                teacherVoiceState = "Speech Recognized"
                            },
                            onError = { err -> teacherVoiceState = "Error: $err" },
                            onState = { state -> teacherVoiceState = state }
                        )
                    } else {
                        startStudentListening(
                            voiceInputManager,
                            translationEngine,
                            onResult = { text ->
                                studentInput = text
                                studentOutput = translationEngine.translate(text, Language.SANTALI, Language.HINDI)
                                studentVoiceState = "Speech Recognized"
                            },
                            onError = { err -> studentVoiceState = "Error: $err" },
                            onState = { state -> studentVoiceState = state }
                        )
                    }
                }
            } else {
                Toast.makeText(context, "Microphone permission denied", Toast.LENGTH_SHORT).show()
                if (pendingLangRequest == Language.HINDI) {
                    teacherVoiceState = "Permission Denied"
                } else {
                    studentVoiceState = "Permission Denied"
                }
            }
            pendingLangRequest = null
        }
    )

    fun handleMicClick(lang: Language) {
        val checkPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (checkPermission) {
            if (lang == Language.HINDI) {
                startTeacherListening(
                    voiceInputManager,
                    translationEngine,
                    onResult = { text ->
                        teacherInput = text
                        teacherOutput = translationEngine.translate(text, Language.HINDI, Language.SANTALI)
                        teacherVoiceState = "Speech Recognized"
                    },
                    onError = { err -> teacherVoiceState = "Error: $err" },
                    onState = { state -> teacherVoiceState = state }
                )
            } else {
                startStudentListening(
                    voiceInputManager,
                    translationEngine,
                    onResult = { text ->
                        studentInput = text
                        studentOutput = translationEngine.translate(text, Language.SANTALI, Language.HINDI)
                        studentVoiceState = "Speech Recognized"
                    },
                    onError = { err -> studentVoiceState = "Error: $err" },
                    onState = { state -> studentVoiceState = state }
                )
            }
        } else {
            pendingLangRequest = lang
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Classroom") },
                navigationIcon = {
                    IconButton(onClick = {
                        voiceInputManager.stopListening()
                        textToSpeechManager.stop()
                        onBack()
                    }) {
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
            // Info Header Card
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
                        text = "This screen displays the translation architecture flow. Use the voice cards below to simulate Hindi ↔ Santali translation in classroom mode.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Preserved Flow Diagram Title
            Text(
                text = "Two-Way Speech Translation Flow",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Flow Card 1: Teacher -> Student
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

            // Flow Card 2: Student -> Teacher
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

            // Stacked Voice Translation Cards Section
            Text(
                text = "Live Classroom Voice Translation",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // -----------------------------------------------------------------
            // TEACHER PANEL (HINDI -> SANTALI)
            // -----------------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TEACHER (Hindi)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Voice Input") }
                        )
                    }

                    // Recording State Label
                    if (teacherVoiceState != "Idle") {
                        Text(
                            text = "Status: $teacherVoiceState",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (teacherVoiceState.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Manual Text Input + Microphone Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = teacherInput,
                            onValueChange = {
                                teacherInput = it
                                if (it.isEmpty()) {
                                    teacherOutput = null
                                    teacherIsEditing = false
                                    teacherStatusMessage = ""
                                }
                            },
                            placeholder = { Text("Speak or type in Hindi...") },
                            modifier = Modifier.weight(1f),
                            maxLines = 3
                        )

                        // Mic Trigger Button
                        Button(
                            onClick = { handleMicClick(Language.HINDI) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (teacherVoiceState == "Listening..." || teacherVoiceState == "Recording...") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.size(56.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("🎤", fontSize = 20.sp)
                        }
                    }

                    // Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                if (teacherInput.isNotBlank()) {
                                    teacherVoiceState = "Translating..."
                                    teacherOutput = translationEngine.translate(
                                        teacherInput,
                                        Language.HINDI,
                                        Language.SANTALI
                                    )
                                    teacherVoiceState = "Translation Complete"
                                    teacherIsEditing = false
                                    teacherStatusMessage = ""
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = teacherInput.isNotBlank()
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Translate")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Translate")
                        }

                        OutlinedButton(
                            onClick = {
                                teacherInput = ""
                                teacherOutput = null
                                teacherVoiceState = "Idle"
                                teacherSpeechState = "Idle"
                                teacherIsEditing = false
                                teacherStatusMessage = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear")
                        }
                    }

                    // Output Display Block
                    teacherOutput?.let { result ->
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "STUDENT (Santali Translation)",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Offline match confidence: ${result.confidence}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }

                                if (result.requiresReview) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = "⚠️ Needs teacher review",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (teacherIsEditing) {
                                    OutlinedTextField(
                                        value = teacherEditedText,
                                        onValueChange = { teacherEditedText = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Corrected Translation") }
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (teacherEditedText.isNotBlank()) {
                                                    val correctedEntry = TranslationEntry(
                                                        sourceText = teacherInput,
                                                        targetText = teacherEditedText,
                                                        sourceLang = Language.HINDI,
                                                        targetLang = Language.SANTALI,
                                                        category = "Teacher Correction"
                                                    )
                                                    if (translationEngine is OfflineFLNTranslationEngine) {
                                                        translationEngine.addCorrection(correctedEntry)
                                                    }
                                                    teacherOutput = result.copy(
                                                        translatedText = teacherEditedText,
                                                        confidence = "High (Teacher Validated)",
                                                        requiresReview = false
                                                    )
                                                    teacherIsEditing = false
                                                    teacherStatusMessage = "Saved to translation memory"
                                                }
                                            }
                                        ) {
                                            Text("Save")
                                        }
                                        OutlinedButton(onClick = { teacherIsEditing = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                } else {
                                    Text(
                                        text = result.translatedText,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )

                                    if (teacherStatusMessage.isNotEmpty()) {
                                        Text(
                                            text = teacherStatusMessage,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // TTS Play status message
                                    if (teacherSpeechState != "Idle") {
                                        Text(
                                            text = "Voice: $teacherSpeechState",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (teacherSpeechState.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Play (TTS) Button
                                        Button(
                                            onClick = {
                                                val isSupported = textToSpeechManager.isLanguageSupported("sat")
                                                if (!isSupported) {
                                                    teacherSpeechState = "Error: Santali voice unavailable on this device"
                                                } else {
                                                    textToSpeechManager.speak(
                                                        result.translatedText,
                                                        "sat",
                                                        onStart = { teacherSpeechState = "Speaking..." },
                                                        onDone = { teacherSpeechState = "Idle" },
                                                        onError = { err -> teacherSpeechState = "Error: $err" }
                                                    )
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Play")
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    teacherIsEditing = true
                                                    teacherEditedText = result.translatedText
                                                }
                                            ) {
                                                Text("Edit")
                                            }
                                            TextButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(result.translatedText))
                                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Text("Copy")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // STUDENT PANEL (SANTALI -> HINDI)
            // -----------------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STUDENT (Santali)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Voice Input") }
                        )
                    }

                    // Recording State Label
                    if (studentVoiceState != "Idle") {
                        Text(
                            text = "Status: $studentVoiceState",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (studentVoiceState.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Manual Text Input + Microphone Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = studentInput,
                            onValueChange = {
                                studentInput = it
                                if (it.isEmpty()) {
                                    studentOutput = null
                                    studentIsEditing = false
                                    studentStatusMessage = ""
                                }
                            },
                            placeholder = { Text("Speak or type in Santali...") },
                            modifier = Modifier.weight(1f),
                            maxLines = 3
                        )

                        // Mic Trigger Button
                        Button(
                            onClick = { handleMicClick(Language.SANTALI) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (studentVoiceState == "Listening..." || studentVoiceState == "Recording...") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.size(56.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("🎤", fontSize = 20.sp)
                        }
                    }

                    // Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                if (studentInput.isNotBlank()) {
                                    studentVoiceState = "Translating..."
                                    studentOutput = translationEngine.translate(
                                        studentInput,
                                        Language.SANTALI,
                                        Language.HINDI
                                    )
                                    studentVoiceState = "Translation Complete"
                                    studentIsEditing = false
                                    studentStatusMessage = ""
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = studentInput.isNotBlank()
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Translate")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Translate")
                        }

                        OutlinedButton(
                            onClick = {
                                studentInput = ""
                                studentOutput = null
                                studentVoiceState = "Idle"
                                studentSpeechState = "Idle"
                                studentIsEditing = false
                                studentStatusMessage = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear")
                        }
                    }

                    // Output Display Block
                    studentOutput?.let { result ->
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TEACHER (Hindi Translation)",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Offline match confidence: ${result.confidence}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }

                                if (result.requiresReview) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = "⚠️ Needs teacher review",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (studentIsEditing) {
                                    OutlinedTextField(
                                        value = studentEditedText,
                                        onValueChange = { studentEditedText = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Corrected Translation") }
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (studentEditedText.isNotBlank()) {
                                                    val correctedEntry = TranslationEntry(
                                                        sourceText = studentInput,
                                                        targetText = studentEditedText,
                                                        sourceLang = Language.SANTALI,
                                                        targetLang = Language.HINDI,
                                                        category = "Teacher Correction"
                                                    )
                                                    if (translationEngine is OfflineFLNTranslationEngine) {
                                                        translationEngine.addCorrection(correctedEntry)
                                                    }
                                                    studentOutput = result.copy(
                                                        translatedText = studentEditedText,
                                                        confidence = "High (Teacher Validated)",
                                                        requiresReview = false
                                                    )
                                                    studentIsEditing = false
                                                    studentStatusMessage = "Saved to translation memory"
                                                }
                                            }
                                        ) {
                                            Text("Save")
                                        }
                                        OutlinedButton(onClick = { studentIsEditing = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                } else {
                                    Text(
                                        text = result.translatedText,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )

                                    if (studentStatusMessage.isNotEmpty()) {
                                        Text(
                                            text = studentStatusMessage,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // TTS Play status message
                                    if (studentSpeechState != "Idle") {
                                        Text(
                                            text = "Voice: $studentSpeechState",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (studentSpeechState.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Play (TTS) Button
                                        Button(
                                            onClick = {
                                                val isSupported = textToSpeechManager.isLanguageSupported("hi")
                                                if (!isSupported) {
                                                    studentSpeechState = "Error: Hindi voice unavailable on this device"
                                                } else {
                                                    textToSpeechManager.speak(
                                                        result.translatedText,
                                                        "hi",
                                                        onStart = { studentSpeechState = "Speaking..." },
                                                        onDone = { studentSpeechState = "Idle" },
                                                        onError = { err -> studentSpeechState = "Error: $err" }
                                                    )
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Play")
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    studentIsEditing = true
                                                    studentEditedText = result.translatedText
                                                }
                                            ) {
                                                Text("Edit")
                                            }
                                            TextButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(result.translatedText))
                                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Text("Copy")
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
    }
}

private fun startTeacherListening(
    voiceInputManager: VoiceInputManager,
    translationEngine: TranslationEngine,
    onResult: (String) -> Unit,
    onError: (String) -> Unit,
    onState: (String) -> Unit
) {
    voiceInputManager.startListening(
        languageCode = "hi-IN",
        onResult = onResult,
        onError = onError,
        onStateChange = onState
    )
}

private fun startStudentListening(
    voiceInputManager: VoiceInputManager,
    translationEngine: TranslationEngine,
    onResult: (String) -> Unit,
    onError: (String) -> Unit,
    onState: (String) -> Unit
) {
    voiceInputManager.startListening(
        languageCode = "sat-IN",
        onResult = onResult,
        onError = onError,
        onStateChange = onState
    )
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

