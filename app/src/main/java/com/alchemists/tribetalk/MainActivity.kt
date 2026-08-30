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
import com.alchemists.tribetalk.flashcards.FlashcardSet
import com.alchemists.tribetalk.lessons.Lesson
import com.alchemists.tribetalk.ui.screens.DashboardScreen
import com.alchemists.tribetalk.ui.screens.FlashcardGeneratorScreen
import com.alchemists.tribetalk.ui.screens.FlashcardPreviewScreen
import com.alchemists.tribetalk.ui.screens.LearningInsightsScreen
import com.alchemists.tribetalk.ui.screens.LessonDetailScreen
import com.alchemists.tribetalk.ui.screens.LessonLibraryScreen
import com.alchemists.tribetalk.ui.screens.LiveClassroomScreen
import com.alchemists.tribetalk.ui.screens.PlaceholderScreen
import com.alchemists.tribetalk.ui.screens.SettingsScreen
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
    Flashcards,
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
                            var selectedLesson by remember { mutableStateOf<Lesson?>(null) }
                            if (selectedLesson == null) {
                                LessonLibraryScreen(
                                    onSelectLesson = { lesson -> selectedLesson = lesson },
                                    onBack = { currentScreen = Screen.Dashboard }
                                )
                            } else {
                                LessonDetailScreen(
                                    lesson = selectedLesson!!,
                                    translationEngine = translationEngine,
                                    neuralSynthesizer = neuralSynthesizer,
                                    onCreateFlashcards = { _ ->
                                        currentScreen = Screen.Flashcards
                                    },
                                    onCreateWorksheet = { _ ->
                                        currentScreen = Screen.Worksheets
                                    },
                                    onBack = { selectedLesson = null }
                                )
                            }
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
                                    translationEngine = translationEngine,
                                    onBack = { generatedWorksheet = null }
                                )
                            }
                        }
                        Screen.Flashcards -> {
                            var generatedFlashcardSet by remember { mutableStateOf<FlashcardSet?>(null) }
                            if (generatedFlashcardSet == null) {
                                FlashcardGeneratorScreen(
                                    translationEngine = translationEngine,
                                    onFlashcardSetGenerated = { set ->
                                        generatedFlashcardSet = set
                                    },
                                    onBack = { currentScreen = Screen.Dashboard }
                                )
                            } else {
                                FlashcardPreviewScreen(
                                    initialSet = generatedFlashcardSet!!,
                                    translationEngine = translationEngine,
                                    onBack = { generatedFlashcardSet = null }
                                )
                            }
                        }
                        Screen.LearningInsights -> {
                            LearningInsightsScreen(
                                onBack = { currentScreen = Screen.Dashboard }
                            )
                        }
                        Screen.Settings -> {
                            SettingsScreen(
                                neuralSynthesizer = neuralSynthesizer,
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
