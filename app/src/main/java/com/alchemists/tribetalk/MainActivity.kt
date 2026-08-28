package com.alchemists.tribetalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.alchemists.tribetalk.translation.MockTranslationEngine
import com.alchemists.tribetalk.ui.screens.DashboardScreen
import com.alchemists.tribetalk.ui.screens.LiveClassroomScreen
import com.alchemists.tribetalk.ui.screens.PlaceholderScreen
import com.alchemists.tribetalk.ui.theme.TribeTalkTheme

enum class Screen {
    Dashboard,
    LiveClassroom,
    Lessons,
    Worksheets,
    LearningInsights,
    Settings
}

class MainActivity : ComponentActivity() {
    private val translationEngine = MockTranslationEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}
