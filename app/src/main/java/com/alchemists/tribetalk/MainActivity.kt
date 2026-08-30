package com.alchemists.tribetalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.alchemists.tribetalk.translation.HybridEdgeAITranslationEngine
import com.alchemists.tribetalk.translation.OnnxTranslationEngine
import com.alchemists.tribetalk.translation.TranslationMemory
import com.alchemists.tribetalk.ui.screens.DashboardScreen
import com.alchemists.tribetalk.ui.screens.LiveClassroomScreen
import com.alchemists.tribetalk.ui.screens.PlaceholderScreen
import com.alchemists.tribetalk.ui.screens.WorksheetGeneratorScreen
import com.alchemists.tribetalk.ui.screens.WorksheetPreviewScreen
import com.alchemists.tribetalk.worksheet.Worksheet
import com.alchemists.tribetalk.ui.theme.TribeTalkTheme
import com.alchemists.tribetalk.voice.SpeechOutputManager
import com.alchemists.tribetalk.voice.VoiceInputManager
import com.alchemists.tribetalk.voice.VoiceTranslationBridge
import java.io.File

import com.alchemists.tribetalk.voice.NeuralSpeechSynthesizer
import com.alchemists.tribetalk.voice.NeuralSpeechRecognizer

enum class Screen {
    Dashboard,
    LiveClassroom,
    Lessons,
    Worksheets,
    LearningInsights,
    Settings
}

class MainActivity : ComponentActivity() {
    private lateinit var translationEngine: HybridEdgeAITranslationEngine
    private lateinit var onnxEngine: OnnxTranslationEngine
    private lateinit var voiceInputManager: VoiceInputManager
    private lateinit var speechOutputManager: SpeechOutputManager
    private lateinit var neuralSynthesizer: NeuralSpeechSynthesizer
    private lateinit var neuralRecognizer: NeuralSpeechRecognizer
    private lateinit var voiceTranslationBridge: VoiceTranslationBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup persistent translation memory and Hybrid Edge AI translation engine
        val tmFile = File(filesDir, "translation_memory.csv")
        val translationMemory = TranslationMemory(tmFile)
        onnxEngine = OnnxTranslationEngine(this)
        translationEngine = HybridEdgeAITranslationEngine(translationMemory, onnxEngine)

        // 8 GB RAM High-Performance Neural Audio Engines
        neuralSynthesizer = NeuralSpeechSynthesizer(this)
        neuralRecognizer = NeuralSpeechRecognizer(this)

        // Initialize voice input and output services with offline neural fallback
        voiceInputManager = VoiceInputManager(this, neuralRecognizer)
        speechOutputManager = SpeechOutputManager(this) { success ->
            // Log or handle init state if needed
        }
        
        // Setup Voice translation bridge with Neural TTS fallback
        voiceTranslationBridge = VoiceTranslationBridge(translationEngine, speechOutputManager, neuralSynthesizer)

        setContent {
            TribeTalkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }

                    when (currentScreen) {
                        Screen.Dashboard -> {
                            DashboardScreen(
                                onNavigate = { screen -> currentScreen = screen }
                            )
                        }
                        Screen.LiveClassroom -> {
                            LiveClassroomScreen(
                                translationEngine = translationEngine,
                                voiceInputManager = voiceInputManager,
                                speechOutputManager = speechOutputManager,
                                voiceTranslationBridge = voiceTranslationBridge,
                                onBack = { currentScreen = Screen.Dashboard }
                            )
                        }
                        Screen.Lessons -> {
                            PlaceholderScreen(
                                title = "Lessons",
                                onBack = { currentScreen = Screen.Dashboard }
                            )
                        }
                        Screen.Worksheets -> {
                            var generatedWorksheet by remember { mutableStateOf<Worksheet?>(null) }
                            if (generatedWorksheet == null) {
                                WorksheetGeneratorScreen(
                                    translationEngine = translationEngine,
                                    onWorksheetGenerated = { worksheet ->
                                        generatedWorksheet = worksheet
                                    },
                                    onBack = { currentScreen = Screen.Dashboard }
                                )
                            } else {
                                WorksheetPreviewScreen(
                                    initialWorksheet = generatedWorksheet!!,
                                    onBack = { generatedWorksheet = null }
                                )
                            }
                        }
                        Screen.LearningInsights -> {
                            PlaceholderScreen(
                                title = "Learning Insights",
                                onBack = { currentScreen = Screen.Dashboard }
                            )
                        }
                        Screen.Settings -> {
                            PlaceholderScreen(
                                title = "Settings",
                                onBack = { currentScreen = Screen.Dashboard }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceInputManager.destroy()
        speechOutputManager.destroy()
        neuralSynthesizer.close()
        neuralRecognizer.close()
        onnxEngine.close()
    }
}
