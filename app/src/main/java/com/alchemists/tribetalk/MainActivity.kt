package com.alchemists.tribetalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.alchemists.tribetalk.translation.OfflineFLNTranslationEngine
import com.alchemists.tribetalk.translation.TranslationMemory
import com.alchemists.tribetalk.ui.screens.DashboardScreen
import com.alchemists.tribetalk.ui.screens.LiveClassroomScreen
import com.alchemists.tribetalk.ui.screens.PlaceholderScreen
import com.alchemists.tribetalk.ui.theme.TribeTalkTheme
import com.alchemists.tribetalk.voice.TextToSpeechManager
import com.alchemists.tribetalk.voice.VoiceInputManager
import java.io.File

enum class Screen {
    Dashboard,
    LiveClassroom,
    Lessons,
    Worksheets,
    LearningInsights,
    Settings
}

class MainActivity : ComponentActivity() {
    private lateinit var translationEngine: OfflineFLNTranslationEngine
    private lateinit var voiceInputManager: VoiceInputManager
    private lateinit var textToSpeechManager: TextToSpeechManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup persistent translation memory and FLN engine
        val tmFile = File(filesDir, "translation_memory.csv")
        val translationMemory = TranslationMemory(tmFile)
        translationEngine = OfflineFLNTranslationEngine(translationMemory)

        // Initialize voice input and output services
        voiceInputManager = VoiceInputManager(this)
        textToSpeechManager = TextToSpeechManager(this) { success ->
            // Log or handle init state if needed
        }

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
                                textToSpeechManager = textToSpeechManager,
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
                            PlaceholderScreen(
                                title = "Worksheets",
                                onBack = { currentScreen = Screen.Dashboard }
                            )
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
        textToSpeechManager.destroy()
    }
}
