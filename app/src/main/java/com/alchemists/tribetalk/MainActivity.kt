package com.alchemists.tribetalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.alchemists.tribetalk.auth.AuthManager
import com.alchemists.tribetalk.auth.User
import com.alchemists.tribetalk.auth.UserRole
import com.alchemists.tribetalk.curriculum.NipunRepository
import com.alchemists.tribetalk.curriculum.Teacher
import com.alchemists.tribetalk.ui.screens.NipunViewModel
import com.alchemists.tribetalk.translation.HybridEdgeAITranslationEngine
import com.alchemists.tribetalk.translation.OnnxTranslationEngine
import com.alchemists.tribetalk.translation.TranslationMemory
import com.alchemists.tribetalk.ui.screens.AdminDashboardScreen
import com.alchemists.tribetalk.ui.screens.LiveClassroomScreen
import com.alchemists.tribetalk.ui.screens.LoginScreen
import com.alchemists.tribetalk.ui.screens.PlaceholderScreen
import com.alchemists.tribetalk.ui.screens.RegisterScreen
import com.alchemists.tribetalk.ui.screens.StudentDashboardScreen
import com.alchemists.tribetalk.ui.screens.TeacherDashboardScreen
import com.alchemists.tribetalk.ui.screens.UserManagementScreen
import com.alchemists.tribetalk.ui.theme.TribeTalkTheme
import com.alchemists.tribetalk.voice.SpeechOutputManager
import com.alchemists.tribetalk.voice.VoiceInputManager
import com.alchemists.tribetalk.voice.VoiceTranslationBridge
import java.io.File

import com.alchemists.tribetalk.voice.NeuralSpeechSynthesizer
import com.alchemists.tribetalk.voice.NeuralSpeechRecognizer

enum class Screen {
    Login,
    Register,
    AdminDashboard,
    TeacherDashboard,
    StudentDashboard,
    LiveClassroom,
    Lessons,
    Worksheets,
    LearningInsights,
    Settings,
    Users,
    Reports,
    NipunTeacherLogin,
    NipunClassroomSetup,
    NipunHome,
    NipunClassContent,
    NipunOutcomes,
    NipunOutcomeDetail,
    NipunFlashcards,
    NipunWorksheets,
    NipunResources,
    NipunStudents
}

class MainActivity : ComponentActivity() {
    private lateinit var translationEngine: HybridEdgeAITranslationEngine
    private lateinit var onnxEngine: OnnxTranslationEngine
    private lateinit var voiceInputManager: VoiceInputManager
    private lateinit var speechOutputManager: SpeechOutputManager
    private lateinit var neuralSynthesizer: NeuralSpeechSynthesizer
    private lateinit var neuralRecognizer: NeuralSpeechRecognizer
    private lateinit var voiceTranslationBridge: VoiceTranslationBridge
    private lateinit var authManager: AuthManager

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

        authManager = AuthManager(this)

        val nipunRepo = NipunRepository.getInstance(this)

        setContent {
            TribeTalkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentUser by remember { mutableStateOf<User?>(null) }
                    var currentScreen by remember { mutableStateOf(Screen.Login) }

                    // NIPUN/FLN curriculum navigation state
                    val nipunVm: NipunViewModel = remember(nipunRepo) { NipunViewModel(nipunRepo) }
                    var nipunGrade by remember { mutableStateOf(1) }
                    var nipunClassroomId by remember { mutableStateOf(0L) }
                    var nipunClassLabel by remember { mutableStateOf("Class 1") }
                    var nipunOutcome by remember { mutableStateOf<com.alchemists.tribetalk.curriculum.LearningOutcome?>(null) }
                    val contextForResources = LocalContext.current

                    fun homeScreen(): Screen = when (currentUser?.role) {
                        UserRole.ADMIN -> Screen.AdminDashboard
                        UserRole.TEACHER -> Screen.TeacherDashboard
                        UserRole.STUDENT -> Screen.StudentDashboard
                        null -> Screen.Login
                    }

                    fun handleLogin(user: User) {
                        currentUser = user
                        currentScreen = when (user.role) {
                            UserRole.ADMIN -> Screen.AdminDashboard
                            UserRole.TEACHER -> Screen.TeacherDashboard
                            UserRole.STUDENT -> Screen.StudentDashboard
                        }
                    }

                    fun handleLogout() {
                        currentUser = null
                        currentScreen = Screen.Login
                    }

                    when (currentScreen) {
                        Screen.Login -> {
                            LoginScreen(
                                authManager = authManager,
                                onLoginSuccess = { user -> handleLogin(user) },
                                onNavigateToRegister = { currentScreen = Screen.Register }
                            )
                        }
                        Screen.Register -> {
                            RegisterScreen(
                                authManager = authManager,
                                onRegistered = { user -> handleLogin(user) },
                                onBackToLogin = { currentScreen = Screen.Login }
                            )
                        }
                        Screen.AdminDashboard -> {
                            AdminDashboardScreen(
                                userName = currentUser?.displayName ?: "Admin",
                                authManager = authManager,
                                onLogout = { handleLogout() },
                                onNavigate = { screen -> currentScreen = screen }
                            )
                        }
                        Screen.TeacherDashboard -> {
                            TeacherDashboardScreen(
                                userName = currentUser?.displayName ?: "Teacher",
                                onLogout = { handleLogout() },
                                onNavigate = { screen -> currentScreen = screen }
                            )
                        }
                        Screen.StudentDashboard -> {
                            StudentDashboardScreen(
                                userName = currentUser?.displayName ?: "Student",
                                onLogout = { handleLogout() },
                                onNavigate = { screen -> currentScreen = screen }
                            )
                        }
                        Screen.Users -> {
                            UserManagementScreen(
                                authManager = authManager,
                                onBack = { currentScreen = homeScreen() }
                            )
                        }
                        Screen.LiveClassroom -> {
                            LiveClassroomScreen(
                                translationEngine = translationEngine,
                                voiceInputManager = voiceInputManager,
                                speechOutputManager = speechOutputManager,
                                voiceTranslationBridge = voiceTranslationBridge,
                                onBack = { currentScreen = homeScreen() }
                            )
                        }
                        Screen.Lessons -> {
                            PlaceholderScreen(
                                title = "Lessons",
                                onBack = { currentScreen = homeScreen() }
                            )
                        }
                        Screen.Worksheets -> {
                            PlaceholderScreen(
                                title = "Worksheets",
                                onBack = { currentScreen = homeScreen() }
                            )
                        }
                        Screen.LearningInsights -> {
                            PlaceholderScreen(
                                title = "Learning Insights",
                                onBack = { currentScreen = homeScreen() }
                            )
                        }
                        Screen.Settings -> {
                            PlaceholderScreen(
                                title = "Settings",
                                onBack = { currentScreen = homeScreen() }
                            )
                        }
                        Screen.Reports -> {
                            PlaceholderScreen(
                                title = "Reports",
                                onBack = { currentScreen = homeScreen() }
                            )
                        }
                        Screen.NipunTeacherLogin -> {
                            NipunTeacherLoginScreen(
                                vm = nipunVm,
                                onLoginSuccess = {
                                    // After login, go to classroom setup/home based on existing setup
                                    currentScreen = Screen.NipunHome
                                },
                                onBack = { currentScreen = homeScreen() }
                            )
                        }
                        Screen.NipunClassroomSetup -> {
                            NipunClassroomSetupScreen(
                                vm = nipunVm,
                                onDone = { currentScreen = Screen.NipunHome },
                                onBack = {
                                    currentScreen = if (currentUser?.role == UserRole.TEACHER) Screen.TeacherDashboard else Screen.NipunHome
                                }
                            )
                        }
                        Screen.NipunHome -> {
                            NipunHomeScreen(
                                vm = nipunVm,
                                onPickClass = { grade, cl ->
                                    nipunGrade = grade
                                    nipunClassroomId = cl.id
                                    nipunClassLabel = cl.label
                                    currentScreen = Screen.NipunClassContent
                                },
                                onManageClasses = { currentScreen = Screen.NipunClassroomSetup },
                                onBack = { currentScreen = homeScreen() }
                            )
                        }
                        Screen.NipunClassContent -> {
                            NipunClassContentScreen(
                                vm = nipunVm,
                                grade = nipunGrade,
                                classLabel = nipunClassLabel,
                                onOpenOutcomes = {
                                    nipunOutcome = null
                                    currentScreen = Screen.NipunOutcomes
                                },
                                onOpenFlashcards = {
                                    nipunOutcome = null
                                    currentScreen = Screen.NipunFlashcards
                                },
                                onOpenWorksheets = {
                                    nipunOutcome = null
                                    currentScreen = Screen.NipunWorksheets
                                },
                                onOpenResources = { currentScreen = Screen.NipunResources },
                                onOpenStudents = { currentScreen = Screen.NipunStudents },
                                onBack = { currentScreen = Screen.NipunHome }
                            )
                        }
                        Screen.NipunOutcomes -> {
                            NipunOutcomesScreen(
                                vm = nipunVm,
                                grade = nipunGrade,
                                classLabel = nipunClassLabel,
                                onOpenOutcome = { lo ->
                                    nipunOutcome = lo
                                    currentScreen = Screen.NipunOutcomeDetail
                                },
                                onBack = { currentScreen = Screen.NipunClassContent }
                            )
                        }
                        Screen.NipunOutcomeDetail -> {
                            val lo = nipunOutcome
                            if (lo != null) {
                                NipunOutcomeDetailScreen(
                                    vm = nipunVm,
                                    grade = nipunGrade,
                                    lo = lo,
                                    onOpenFlashcards = { selected ->
                                        nipunOutcome = selected
                                        currentScreen = Screen.NipunFlashcards
                                    },
                                    onOpenWorksheets = { selected ->
                                        nipunOutcome = selected
                                        currentScreen = Screen.NipunWorksheets
                                    },
                                    onBack = { currentScreen = Screen.NipunOutcomes }
                                )
                            } else {
                                LaunchedEffect(Unit) { currentScreen = Screen.NipunOutcomes }
                            }
                        }
                        Screen.NipunFlashcards -> {
                            NipunFlashcardsScreen(
                                vm = nipunVm,
                                grade = nipunGrade,
                                lo = nipunOutcome,
                                classLabel = nipunClassLabel,
                                onBack = {
                                    currentScreen = if (nipunOutcome != null) Screen.NipunOutcomeDetail else Screen.NipunClassContent
                                }
                            )
                        }
                        Screen.NipunWorksheets -> {
                            NipunWorksheetsScreen(
                                vm = nipunVm,
                                grade = nipunGrade,
                                lo = nipunOutcome,
                                classLabel = nipunClassLabel,
                                onBack = {
                                    currentScreen = if (nipunOutcome != null) Screen.NipunOutcomeDetail else Screen.NipunClassContent
                                }
                            )
                        }
                        Screen.NipunResources -> {
                            NipunResourcesScreen(
                                vm = nipunVm,
                                grade = nipunGrade,
                                classLabel = nipunClassLabel,
                                context = contextForResources,
                                onBack = { currentScreen = Screen.NipunClassContent }
                            )
                        }
                        Screen.NipunStudents -> {
                            NipunStudentsScreen(
                                vm = nipunVm,
                                grade = nipunGrade,
                                classroomId = nipunClassroomId,
                                classLabel = nipunClassLabel,
                                onBack = { currentScreen = Screen.NipunClassContent }
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