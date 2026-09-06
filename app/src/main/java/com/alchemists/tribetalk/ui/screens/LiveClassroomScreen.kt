package com.alchemists.tribetalk.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.alchemists.tribetalk.translation.HybridEdgeAITranslationEngine
import com.alchemists.tribetalk.translation.OlChikiTransliterator
import com.alchemists.tribetalk.ui.theme.*
import com.alchemists.tribetalk.voice.SpeechOutputManager
import com.alchemists.tribetalk.voice.VoiceInputManager
import com.alchemists.tribetalk.voice.VoiceTranslationBridge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveClassroomScreen(
    translationEngine: TranslationEngine,
    voiceInputManager: VoiceInputManager,
    speechOutputManager: SpeechOutputManager,
    voiceTranslationBridge: VoiceTranslationBridge,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Voice Bridge Auto-Play Toggle (Default: OFF)
    var isVoiceBridgeEnabled by remember { mutableStateOf(false) }

    // Teacher -> Student State
    var teacherInput by remember { mutableStateOf("") }
    var teacherOutput by remember { mutableStateOf<TranslationResult?>(null) }
    var teacherVoiceState by remember { mutableStateOf(VoiceTranslationBridge.State.Idle) }
    var teacherSpeechState by remember { mutableStateOf("") }
    var teacherIsEditing by remember { mutableStateOf(false) }
    var teacherEditedText by remember { mutableStateOf("") }
    var teacherStatusMessage by remember { mutableStateOf("") }
    var teacherKeyboardExpanded by remember { mutableStateOf(false) }

    // Student -> Teacher State
    var studentInput by remember { mutableStateOf("") }
    var studentOutput by remember { mutableStateOf<TranslationResult?>(null) }
    var studentVoiceState by remember { mutableStateOf(VoiceTranslationBridge.State.Idle) }
    var studentSpeechState by remember { mutableStateOf("") }
    var studentIsEditing by remember { mutableStateOf(false) }
    var studentEditedText by remember { mutableStateOf("") }
    var studentStatusMessage by remember { mutableStateOf("") }
    var studentKeyboardExpanded by remember { mutableStateOf(false) }

    // Phase 12: Production-Style Continuous Live Voice State
    var isLiveVoiceActive by remember { mutableStateOf(false) }
    var liveStatusLabel by remember { mutableStateOf("Idle") }
    var liveHindiUtterance by remember { mutableStateOf("") }
    var liveSantaliUtterance by remember { mutableStateOf("") }
    var liveSantaliPhonetic by remember { mutableStateOf("") }
    var isLiveVoiceRequested by remember { mutableStateOf(false) }

    fun startLiveVoiceSessionCoordinator() {
        android.util.Log.i("VOICE", "[VOICE] AUDIO_CAPTURE_INITIALIZING")
        android.util.Log.i("VoiceIntegration", "ASR_INITIALIZED")
        android.util.Log.i("LiveHindiASR", "ASR INITIALIZATION STARTED")
        isLiveVoiceActive = true
        liveStatusLabel = "LIVE LISTENING"

        voiceTranslationBridge.startLiveVoiceSession(
            onStateChange = { state, status ->
                liveStatusLabel = when (state) {
                    VoiceTranslationBridge.State.Listening -> "LIVE LISTENING"
                    VoiceTranslationBridge.State.Processing -> "Processing..."
                    VoiceTranslationBridge.State.Translating -> "Translating..."
                    VoiceTranslationBridge.State.GeneratingSantaliSpeech -> "Generating Santali voice..."
                    VoiceTranslationBridge.State.Speaking -> "Playing Santali"
                    VoiceTranslationBridge.State.Error -> status
                    else -> status
                }
            },
            onUtteranceResult = { hindi, transRes ->
                android.util.Log.i("VOICE", "[VOICE] SANTALI_TEXT = \"${transRes.translatedText}\" (${transRes.latinPhonetic})")
                liveHindiUtterance = hindi
                liveSantaliUtterance = transRes.translatedText
                liveSantaliPhonetic = transRes.latinPhonetic
                teacherInput = hindi
                teacherOutput = transRes
            },
            onError = { _ ->
                liveStatusLabel = "Error — Please try again"
            }
        )

        android.util.Log.i("LiveHindiASR", "ASR START REQUESTED")
        liveStatusLabel = "MIC STARTING"
        voiceInputManager.startContinuousListening(
            languageCode = "hi-IN",
            onPartial = { partial ->
                android.util.Log.i("VOICE", "[VOICE] HINDI_PARTIAL = $partial")
                liveStatusLabel = "ASR PARTIAL: $partial"
                liveHindiUtterance = partial
                voiceTranslationBridge.handleLivePartial(partial)
            },
            onFinal = { finalHindi ->
                android.util.Log.i("VOICE", "[VOICE] HINDI_FINAL = $finalHindi")
                liveStatusLabel = "ASR FINAL: $finalHindi"
                liveHindiUtterance = finalHindi
                teacherInput = finalHindi
                voiceTranslationBridge.enqueueLiveUtterance(finalHindi)
            },
            onError = { err ->
                android.util.Log.e("LiveHindiASR", "ASR ERROR: $err")
                liveStatusLabel = "ASR ERROR: $err"
            },
            onStateChange = { stateStr ->
                if (isLiveVoiceActive) {
                    liveStatusLabel = when {
                        stateStr.contains("Recording") || stateStr.contains("RECEIVING") -> "ASR RECEIVING AUDIO"
                        stateStr == "Listening..." || stateStr == "LIVE LISTENING" -> "MIC ACTIVE (Listening...)"
                        else -> stateStr
                    }
                }
            }
        )
    }

    // Track which language side requested recording when permission is prompted
    var pendingLangRequest by remember { mutableStateOf<Language?>(null) }
    var handleMicClick: (Language) -> Unit by remember { mutableStateOf({}) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            android.util.Log.i("VOICE", "[VOICE] RECORD_AUDIO_PERMISSION = ${if (isGranted) "GRANTED" else "DENIED"}")
            android.util.Log.i("LiveHindiASR", "MICROPHONE PERMISSION RESULT: isGranted=$isGranted")
            if (isGranted) {
                android.util.Log.i("VoiceIntegration", "MIC_PERMISSION_GRANTED")
                if (isLiveVoiceRequested) {
                    isLiveVoiceRequested = false
                    startLiveVoiceSessionCoordinator()
                } else {
                    pendingLangRequest?.let { lang ->
                        handleMicClick(lang)
                    }
                }
            } else {
                Toast.makeText(context, "Microphone permission required", Toast.LENGTH_LONG).show()
                liveStatusLabel = "Microphone permission required"
                if (pendingLangRequest == Language.HINDI) {
                    teacherVoiceState = VoiceTranslationBridge.State.Error
                    teacherSpeechState = "Permission Denied"
                } else {
                    studentVoiceState = VoiceTranslationBridge.State.Error
                    studentSpeechState = "Permission Denied"
                }
            }
            pendingLangRequest = null
            isLiveVoiceRequested = false
        }
    )

    handleMicClick = { lang ->
        val checkPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (checkPermission) {
            if (lang == Language.HINDI) {
                if (teacherVoiceState == VoiceTranslationBridge.State.Listening) {
                    voiceInputManager.stopListening()
                    teacherVoiceState = VoiceTranslationBridge.State.Idle
                    teacherSpeechState = "Idle"
                } else {
                    teacherVoiceState = VoiceTranslationBridge.State.Listening
                    teacherSpeechState = "Listening..."
                    voiceInputManager.startListening(
                        languageCode = "hi-IN",
                        onResult = { text ->
                            teacherInput = text
                            teacherVoiceState = VoiceTranslationBridge.State.Recognized
                            voiceTranslationBridge.translateAndSpeak(
                                recognizedText = text,
                                sourceLanguage = Language.HINDI,
                                targetLanguage = Language.SANTALI,
                                isVoiceBridgeEnabled = isVoiceBridgeEnabled,
                                onStateChange = { state, status ->
                                    teacherVoiceState = state
                                    teacherSpeechState = status
                                },
                                onResult = { result ->
                                    teacherOutput = result
                                }
                            )
                        },
                        onError = { err ->
                            teacherVoiceState = VoiceTranslationBridge.State.Error
                            teacherSpeechState = "Error: $err"
                        },
                        onStateChange = { state ->
                            if (state == "Listening..." || state == "Recording...") {
                                teacherVoiceState = VoiceTranslationBridge.State.Listening
                            }
                            teacherSpeechState = state
                        }
                    )
                }
            } else {
                if (studentVoiceState == VoiceTranslationBridge.State.Listening) {
                    voiceInputManager.stopListening()
                    studentVoiceState = VoiceTranslationBridge.State.Idle
                    studentSpeechState = "Idle"
                } else {
                    studentVoiceState = VoiceTranslationBridge.State.Listening
                    studentSpeechState = "Listening..."
                    voiceInputManager.startListening(
                        languageCode = "sat-IN",
                        onResult = { text ->
                            studentInput = text
                            studentVoiceState = VoiceTranslationBridge.State.Recognized
                            voiceTranslationBridge.translateAndSpeak(
                                recognizedText = text,
                                sourceLanguage = Language.SANTALI,
                                targetLanguage = Language.HINDI,
                                isVoiceBridgeEnabled = isVoiceBridgeEnabled,
                                onStateChange = { state, status ->
                                    studentVoiceState = state
                                    studentSpeechState = status
                                },
                                onResult = { result ->
                                    studentOutput = result
                                }
                            )
                        },
                        onError = { err ->
                            studentVoiceState = VoiceTranslationBridge.State.Error
                            studentSpeechState = "Error: $err"
                        },
                        onStateChange = { state ->
                            if (state == "Listening..." || state == "Recording...") {
                                studentVoiceState = VoiceTranslationBridge.State.Listening
                            }
                            studentSpeechState = state
                        }
                    )
                }
            }
        } else {
            pendingLangRequest = lang
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "TribeTalk",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 24.sp
                            )
                        )
                        Text(
                            "Offline Classroom Translation",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 12.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        voiceInputManager.stopListening()
                        speechOutputManager.stop()
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 8 GB RAM High-Performance Edge AI Diagnostic Ribbon
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Text(
                            text = "8 GB RAM PROFILE: ONNX NEURAL NMT ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Seq2Seq Autoregressive",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Phase 12: Production-Style Continuous Live Voice Bridge Hero Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isLiveVoiceActive) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) 
                    else 
                        MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    if (isLiveVoiceActive) 2.dp else 1.dp,
                    if (isLiveVoiceActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
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
                        Column {
                            Text(
                                "LIVE VOICE BRIDGE",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Hindi Teacher  ↓  Santali Student",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        // Live Indicator Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = if (isLiveVoiceActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Text(
                                text = if (isLiveVoiceActive) "● $liveStatusLabel" else "● IDLE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isLiveVoiceActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    if (isLiveVoiceActive || liveHindiUtterance.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column {
                                    Text(
                                        "Current Hindi:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = if (liveHindiUtterance.isNotEmpty()) liveHindiUtterance else "Listening for continuous speech...",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                Column {
                                    Text(
                                        "Santali (Ol Chiki):",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (liveSantaliUtterance.isNotEmpty()) liveSantaliUtterance else "...",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (liveSantaliPhonetic.isNotEmpty()) {
                                        Text(
                                            text = "Phonetic: $liveSantaliPhonetic",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                voiceTranslationBridge.lastMeasuredLatency?.let { lat ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "⚡ Latency: ${lat.totalE2eLatencyMs}ms (${if (lat.isWithinTarget) "≤3s PASS" else "Processed"})",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (lat.isWithinTarget) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                            )
                                        )
                                        Text(
                                            text = "NLP: ${lat.nlpDurationMs}ms | TTS: ${lat.ttsDurationMs}ms",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Master Action Button: START LIVE VOICE / STOP LIVE VOICE
                    Button(
                        onClick = {
                            android.util.Log.i("VOICE", "[VOICE] BUTTON_PRESSED")
                            android.util.Log.i("VoiceIntegration", "BUTTON_PRESSED")
                            android.util.Log.i("LiveHindiASR", "LIVE VOICE BUTTON PRESSED")
                            if (isLiveVoiceActive) {
                                android.util.Log.i("LiveHindiASR", "ASR STOPPED")
                                isLiveVoiceActive = false
                                liveStatusLabel = "Idle"
                                voiceTranslationBridge.stopLiveVoiceSession()
                            } else {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                android.util.Log.i("VOICE", "[VOICE] RECORD_AUDIO_PERMISSION = ${if (hasPermission) "GRANTED" else "DENIED"}")
                                android.util.Log.i("LiveHindiASR", "MICROPHONE PERMISSION CHECK: granted=$hasPermission")

                                if (hasPermission) {
                                    startLiveVoiceSessionCoordinator()
                                } else {
                                    isLiveVoiceRequested = true
                                    pendingLangRequest = Language.HINDI
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLiveVoiceActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiveVoiceActive) Icons.Default.Clear else Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLiveVoiceActive) "STOP LIVE VOICE" else "START LIVE VOICE",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Visual process pipeline indicator (restrained typography, clean Material icons)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FlowItem(Icons.Default.Mic, "Speak")
                        Text("→", color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                        FlowItem(Icons.Default.Settings, "Recognize")
                        Text("→", color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                        FlowItem(Icons.Default.Refresh, "Translate")
                        Text("→", color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                        FlowItem(Icons.Default.PlayArrow, "Play")
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Voice Bridge Auto-Play Toggle (ON / OFF)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Voice Bridge Mode",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Automatically play translated audio output",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = isVoiceBridgeEnabled,
                            onCheckedChange = { isVoiceBridgeEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.surface,
                                checkedTrackColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }
            }

            // Language direction headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE CLASSROOM",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp
                    )
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hindi",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text("↔", color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Santali",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // -----------------------------------------------------------------
            // CARD 1: TEACHER (HINDI -> SANTALI)
            // -----------------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TEACHER",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "Hindi ➜ Santali",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Large Voice Input Button (Teal for active/listening states, indigo for idle)
                    val isTeacherListening = teacherVoiceState == VoiceTranslationBridge.State.Listening
                    Button(
                        onClick = { handleMicClick(Language.HINDI) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTeacherListening) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Speak",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTeacherListening) "Stop Listening" else "Speak in Hindi",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // State color cues and prompts
                    if (teacherVoiceState != VoiceTranslationBridge.State.Idle) {
                        val stateColor = when (teacherVoiceState) {
                            VoiceTranslationBridge.State.Listening -> MaterialTheme.colorScheme.secondary
                            VoiceTranslationBridge.State.Recognized -> MaterialTheme.colorScheme.secondary
                            VoiceTranslationBridge.State.Translating -> MaterialTheme.colorScheme.primary
                            VoiceTranslationBridge.State.TranslationComplete -> MaterialTheme.colorScheme.secondary
                            VoiceTranslationBridge.State.Speaking -> MaterialTheme.colorScheme.secondary
                            VoiceTranslationBridge.State.Error -> {
                                if (teacherSpeechState.contains("unavailable")) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                            }
                            else -> MaterialTheme.colorScheme.outline
                        }
                        Text(
                            text = "Voice State: $teacherSpeechState",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = stateColor
                        )
                    }

                    // Recognized input display text box
                    if (teacherInput.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Recognized Hindi",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Text(
                                    text = teacherInput,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }

                    // Collapsible Keyboard Manual Entry
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { teacherKeyboardExpanded = !teacherKeyboardExpanded }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Keyboard Input Override",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Icon(
                                imageVector = if (teacherKeyboardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Keyboard Input",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }

                        AnimatedVisibility(visible = teacherKeyboardExpanded) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 8.dp)
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
                                    placeholder = { Text("Type Hindi message manually...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                                Button(
                                    onClick = {
                                        if (teacherInput.isNotBlank()) {
                                            teacherVoiceState = VoiceTranslationBridge.State.Recognized
                                            voiceTranslationBridge.translateAndSpeak(
                                                recognizedText = teacherInput,
                                                sourceLanguage = Language.HINDI,
                                                targetLanguage = Language.SANTALI,
                                                isVoiceBridgeEnabled = isVoiceBridgeEnabled,
                                                onStateChange = { state, status ->
                                                    teacherVoiceState = state
                                                    teacherSpeechState = status
                                                },
                                                onResult = { result ->
                                                    teacherOutput = result
                                                }
                                            )
                                            teacherIsEditing = false
                                            teacherStatusMessage = ""
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Translate")
                                }
                            }
                        }
                    }

                    // Distinct Translation Output Card (Teal successful background)
                    teacherOutput?.let { result ->
                        val isAmber = result.requiresReview
                        Surface(
                            color = if (isAmber) {
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                            } else {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (isAmber) {
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                                    } else {
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
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
                                        text = "Santali Translation",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isAmber) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                                    )
                                    // Confidence text & indicator dot
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .border(
                                                    0.dp,
                                                    Color.Transparent,
                                                    RoundedCornerShape(4.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Surface(
                                                color = if (isAmber) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.fillMaxSize(),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {}
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = result.confidence,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isAmber) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                // Authentic Ol Chiki Student View
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "STUDENT VIEW (OL CHIKI / ᱚᱞ ᱪᱤᱠᱤ)",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = result.engineTier,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (result.olChikiText.isNotBlank()) result.olChikiText else result.translatedText,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 24.sp,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Teacher Assist Heads-Up Display (HUD)
                                if (result.phoneticDevanagari.isNotBlank()) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "TEACHER ASSIST HUD (PHONETIC GUIDE)",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = result.phoneticDevanagari,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 17.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Glance & pronounce if classroom is noisy or Santali audio is muted.",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }

                                // Amber warning alerts (instead of giant red blocks)
                                if (isAmber) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.border(
                                            1.dp,
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                            RoundedCornerShape(4.dp)
                                        )
                                    ) {
                                        Text(
                                            text = "⚠️ Needs teacher review",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.tertiary
                                            ),
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
                                                    if (translationEngine is HybridEdgeAITranslationEngine) {
                                                        translationEngine.addCorrection(correctedEntry)
                                                    } else if (translationEngine is OfflineFLNTranslationEngine) {
                                                        translationEngine.addCorrection(correctedEntry)
                                                    }
                                                    teacherOutput = result.copy(
                                                        translatedText = teacherEditedText,
                                                        confidence = "High (Teacher Validated)",
                                                        requiresReview = false
                                                    )
                                                    teacherIsEditing = false
                                                    teacherStatusMessage = "✓ Saved to Translation Memory"
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            modifier = Modifier.weight(1f).height(44.dp)
                                        ) {
                                            Text("Save")
                                        }
                                        OutlinedButton(
                                            onClick = { teacherIsEditing = false },
                                            modifier = Modifier.weight(1f).height(44.dp)
                                        ) {
                                            Text("Cancel")
                                        }
                                    }
                                } else {
                                    if (teacherStatusMessage.isNotEmpty()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Saved",
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = teacherStatusMessage,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }

                                    // Action Buttons Row (Equal weights, no text clipping)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val isSupported = speechOutputManager.isLanguageAvailable("sat")
                                                if (!isSupported) {
                                                    teacherVoiceState = VoiceTranslationBridge.State.Error
                                                    teacherSpeechState = "Santali voice unavailable on this device"
                                                } else {
                                                    speechOutputManager.speak(
                                                        result.translatedText,
                                                        "sat",
                                                        onStart = {
                                                            teacherVoiceState = VoiceTranslationBridge.State.Speaking
                                                            teacherSpeechState = "Speaking..."
                                                        },
                                                        onDone = {
                                                            teacherVoiceState = VoiceTranslationBridge.State.TranslationComplete
                                                            teacherSpeechState = "Translation Complete"
                                                        },
                                                        onError = { err ->
                                                            teacherVoiceState = VoiceTranslationBridge.State.Error
                                                            teacherSpeechState = "Audio: $err"
                                                        }
                                                    )
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Play", fontSize = 13.sp)
                                        }

                                        Button(
                                            onClick = {
                                                teacherIsEditing = true
                                                teacherEditedText = result.translatedText
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Correct", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Correct", fontSize = 13.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(result.translatedText))
                                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                        ) {
                                            Text("Copy", fontSize = 13.sp)
                                        }
                                    }

                                    // Restored Clear Button Row (Outlined style, no TM deletion)
                                    OutlinedButton(
                                        onClick = {
                                            teacherInput = ""
                                            teacherOutput = null
                                            teacherVoiceState = VoiceTranslationBridge.State.Idle
                                            teacherSpeechState = ""
                                            teacherIsEditing = false
                                            teacherStatusMessage = ""
                                            speechOutputManager.stop()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                    ) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Clear Card")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // CARD 2: STUDENT (SANTALI -> HINDI)
            // -----------------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STUDENT",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "Santali ➜ Hindi",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Large Voice Input Button
                    val isStudentListening = studentVoiceState == VoiceTranslationBridge.State.Listening
                    Button(
                        onClick = { handleMicClick(Language.SANTALI) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStudentListening) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Speak",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isStudentListening) "Stop Listening" else "Speak in Santali",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // State color cues and prompts
                    if (studentVoiceState != VoiceTranslationBridge.State.Idle) {
                        val stateColor = when (studentVoiceState) {
                            VoiceTranslationBridge.State.Listening -> MaterialTheme.colorScheme.secondary
                            VoiceTranslationBridge.State.Recognized -> MaterialTheme.colorScheme.secondary
                            VoiceTranslationBridge.State.Translating -> MaterialTheme.colorScheme.primary
                            VoiceTranslationBridge.State.TranslationComplete -> MaterialTheme.colorScheme.secondary
                            VoiceTranslationBridge.State.Speaking -> MaterialTheme.colorScheme.secondary
                            VoiceTranslationBridge.State.Error -> {
                                if (studentSpeechState.contains("unavailable")) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                            }
                            else -> MaterialTheme.colorScheme.outline
                        }
                        Text(
                            text = "Voice State: $studentSpeechState",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = stateColor
                        )
                    }

                    // Recognized input display text box
                    if (studentInput.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Recognized Santali",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Text(
                                    text = studentInput,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }

                    // Collapsible Keyboard Manual Entry
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { studentKeyboardExpanded = !studentKeyboardExpanded }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Keyboard Input Override",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Icon(
                                imageVector = if (studentKeyboardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Keyboard Input",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }

                        AnimatedVisibility(visible = studentKeyboardExpanded) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 8.dp)
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
                                    placeholder = { Text("Type Santali message manually...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                                Button(
                                    onClick = {
                                        if (studentInput.isNotBlank()) {
                                            studentVoiceState = VoiceTranslationBridge.State.Recognized
                                            voiceTranslationBridge.translateAndSpeak(
                                                recognizedText = studentInput,
                                                sourceLanguage = Language.SANTALI,
                                                targetLanguage = Language.HINDI,
                                                isVoiceBridgeEnabled = isVoiceBridgeEnabled,
                                                onStateChange = { state, status ->
                                                    studentVoiceState = state
                                                    studentSpeechState = status
                                                },
                                                onResult = { result ->
                                                    studentOutput = result
                                                }
                                            )
                                            studentIsEditing = false
                                            studentStatusMessage = ""
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Translate")
                                }
                            }
                        }
                    }

                    // Distinct Translation Output Card (Teal successful background)
                    studentOutput?.let { result ->
                        val isAmber = result.requiresReview
                        Surface(
                            color = if (isAmber) {
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                            } else {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (isAmber) {
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                                    } else {
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
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
                                        text = "Hindi Translation",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isAmber) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                                    )
                                    // Confidence text & indicator dot
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .border(
                                                    0.dp,
                                                    Color.Transparent,
                                                    RoundedCornerShape(4.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Surface(
                                                color = if (isAmber) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.fillMaxSize(),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {}
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = result.confidence,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isAmber) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                // Large translated text
                                Text(
                                    text = result.translatedText,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Amber warning alerts (instead of giant red blocks)
                                if (isAmber) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.border(
                                            1.dp,
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                            RoundedCornerShape(4.dp)
                                        )
                                    ) {
                                        Text(
                                            text = "⚠️ Needs teacher review",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.tertiary
                                            ),
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
                                                    if (translationEngine is HybridEdgeAITranslationEngine) {
                                                        translationEngine.addCorrection(correctedEntry)
                                                    } else if (translationEngine is OfflineFLNTranslationEngine) {
                                                        translationEngine.addCorrection(correctedEntry)
                                                    }
                                                    studentOutput = result.copy(
                                                        translatedText = studentEditedText,
                                                        confidence = "High (Teacher Validated)",
                                                        requiresReview = false
                                                    )
                                                    studentIsEditing = false
                                                    studentStatusMessage = "✓ Saved to Translation Memory"
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            modifier = Modifier.weight(1f).height(44.dp)
                                        ) {
                                            Text("Save")
                                        }
                                        OutlinedButton(
                                            onClick = { studentIsEditing = false },
                                            modifier = Modifier.weight(1f).height(44.dp)
                                        ) {
                                            Text("Cancel")
                                        }
                                    }
                                } else {
                                    if (studentStatusMessage.isNotEmpty()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Saved",
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = studentStatusMessage,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }

                                    // Action Buttons Row (Equal weights, no text clipping)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val isSupported = speechOutputManager.isLanguageAvailable("hi")
                                                if (!isSupported) {
                                                    studentVoiceState = VoiceTranslationBridge.State.Error
                                                    studentSpeechState = "Hindi voice unavailable on this device"
                                                } else {
                                                    speechOutputManager.speak(
                                                        result.translatedText,
                                                        "hi",
                                                        onStart = {
                                                            studentVoiceState = VoiceTranslationBridge.State.Speaking
                                                            studentSpeechState = "Speaking..."
                                                        },
                                                        onDone = {
                                                            studentVoiceState = VoiceTranslationBridge.State.TranslationComplete
                                                            studentSpeechState = "Translation Complete"
                                                        },
                                                        onError = { err ->
                                                            studentVoiceState = VoiceTranslationBridge.State.Error
                                                            studentSpeechState = "Audio: $err"
                                                        }
                                                    )
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Play", fontSize = 13.sp)
                                        }

                                        Button(
                                            onClick = {
                                                studentIsEditing = true
                                                studentEditedText = result.translatedText
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Correct", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Correct", fontSize = 13.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(result.translatedText))
                                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                        ) {
                                            Text("Copy", fontSize = 13.sp)
                                        }
                                    }

                                    // Restored Clear Button Row (Outlined style, no TM deletion)
                                    OutlinedButton(
                                        onClick = {
                                            studentInput = ""
                                            studentOutput = null
                                            studentVoiceState = VoiceTranslationBridge.State.Idle
                                            studentSpeechState = ""
                                            studentIsEditing = false
                                            studentStatusMessage = ""
                                            speechOutputManager.stop()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                    ) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Clear Card")
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
private fun FlowItem(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
