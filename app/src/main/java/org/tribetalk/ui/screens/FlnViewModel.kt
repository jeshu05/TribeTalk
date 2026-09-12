package org.tribetalk.ui.screens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.tribetalk.audio.TribeTalkTtsManager
import org.tribetalk.fln.generator.ProceduralCurriculumGenerator
import org.tribetalk.fln.model.*
import org.tribetalk.fln.repository.FlnCurriculumRepository
import org.tribetalk.fln.worksheet.WorksheetPdfExporter
import java.io.File

/**
 * Interactive Classroom Modes for the Single-Tablet Rural Classroom Station.
 * Designed for 30-second rapid turn-taking when handed to random kids.
 */
enum class ClassroomPlayMode(val displayName: String, val santaliName: String, val icon: String) {
    CHORUS_REVEAL("मिलकर बोलो", "ᱢᱤᱫᱛᱮ ᱨᱚᱲ", "📢"),
    LISTEN_AND_TAP("सुनो और पहचानो", "ᱟᱸᱡᱚᱢ ᱟᱨ ᱥᱟᱵ", "🎯"),
    VOICE_ECHO("सुनो और बोलो", "ᱟᱸᱡᱚᱢ ᱟᱨ ᱨᱚᱲ", "🎤")
}

/**
 * Preview modes for the interactive worksheet studio.
 */
enum class WorksheetPreviewTab {
    STUDENT_SHEET,
    TEACHER_KEY
}

/**
 * ViewModel managing state for the NIPUN Bharat Foundational Literacy and Numeracy suite.
 * Runs completely offline and isolated from heavy neural ASR inference models (< 5 MB RAM).
 */
class FlnViewModel(application: Application) : AndroidViewModel(application) {

    private val ttsManager = TribeTalkTtsManager(application.applicationContext)
    private val speechRecognizer = org.tribetalk.audio.AndroidSpeechRecognizer(application.applicationContext)

    // -------------------------------------------------------------------------
    // Classroom Station & Turn-Taking State
    // -------------------------------------------------------------------------
    private val _classroomMode = MutableStateFlow(ClassroomPlayMode.CHORUS_REVEAL)
    val classroomMode: StateFlow<ClassroomPlayMode> = _classroomMode.asStateFlow()

    private val _kidsTurnCount = MutableStateFlow(0)
    val kidsTurnCount: StateFlow<Int> = _kidsTurnCount.asStateFlow()

    private val _listenAndTapOptions = MutableStateFlow<List<FlnCard>>(emptyList())
    val listenAndTapOptions: StateFlow<List<FlnCard>> = _listenAndTapOptions.asStateFlow()

    private val _selectedTapCard = MutableStateFlow<FlnCard?>(null)
    val selectedTapCard: StateFlow<FlnCard?> = _selectedTapCard.asStateFlow()

    private val _isTapAnswerCorrect = MutableStateFlow<Boolean?>(null)
    val isTapAnswerCorrect: StateFlow<Boolean?> = _isTapAnswerCorrect.asStateFlow()

    private val _isKidSpeaking = MutableStateFlow(false)
    val isKidSpeaking: StateFlow<Boolean> = _isKidSpeaking.asStateFlow()

    private val _kidVoiceSuccess = MutableStateFlow<Boolean?>(null)
    val kidVoiceSuccess: StateFlow<Boolean?> = _kidVoiceSuccess.asStateFlow()

    val speechAmplitude: StateFlow<Float> = speechRecognizer.amplitude

    // -------------------------------------------------------------------------
    // Flashcards State
    // -------------------------------------------------------------------------
    private val _categories = MutableStateFlow(FlnCurriculumRepository.getFlashcardCategories())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow(FlnCurriculumRepository.CATEGORY_ALL)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _cards = MutableStateFlow(FlnCurriculumRepository.getFlashcards())
    val cards: StateFlow<List<FlnCard>> = _cards.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isQuizMode = MutableStateFlow(false)
    val isQuizMode: StateFlow<Boolean> = _isQuizMode.asStateFlow()

    private val _isCardRevealed = MutableStateFlow(false)
    val isCardRevealed: StateFlow<Boolean> = _isCardRevealed.asStateFlow()

    val isPlayingAudio: StateFlow<Boolean> = ttsManager.isSpeaking

    // Interactive Quiz State
    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizAnsweredCount = MutableStateFlow(0)
    val quizAnsweredCount: StateFlow<Int> = _quizAnsweredCount.asStateFlow()

    private val _selectedQuizOption = MutableStateFlow<Int?>(null)
    val selectedQuizOption: StateFlow<Int?> = _selectedQuizOption.asStateFlow()

    private val _isQuizAnswerChecked = MutableStateFlow(false)
    val isQuizAnswerChecked: StateFlow<Boolean> = _isQuizAnswerChecked.asStateFlow()

    private val _quizOptions = MutableStateFlow<List<String>>(emptyList())
    val quizOptions: StateFlow<List<String>> = _quizOptions.asStateFlow()

    private val _quizCorrectIndex = MutableStateFlow(0)
    val quizCorrectIndex: StateFlow<Int> = _quizCorrectIndex.asStateFlow()

    private val _customCardSuccessMessage = MutableStateFlow<String?>(null)
    val customCardSuccessMessage: StateFlow<String?> = _customCardSuccessMessage.asStateFlow()

    // -------------------------------------------------------------------------
    // Worksheets State
    // -------------------------------------------------------------------------
    private val _worksheetConfig = MutableStateFlow(
        WorksheetConfig(
            title = "NIPUN Bharat FLN Practice Sheet",
            type = WorksheetType.COUNT_AND_MATCH,
            grade = FlnGrade.GRADE_1,
            difficulty = WorksheetDifficulty.MEDIUM,
            questionCount = 5,
            schoolName = "Prathmik Vidyalaya (प्राथमिक विद्यालय)"
        )
    )
    val worksheetConfig: StateFlow<WorksheetConfig> = _worksheetConfig.asStateFlow()

    private val _worksheetItems = MutableStateFlow(
        FlnCurriculumRepository.generateWorksheet(_worksheetConfig.value)
    )
    val worksheetItems: StateFlow<List<WorksheetItem>> = _worksheetItems.asStateFlow()

    private val _previewTab = MutableStateFlow(WorksheetPreviewTab.STUDENT_SHEET)
    val previewTab: StateFlow<WorksheetPreviewTab> = _previewTab.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _lastGeneratedPdf = MutableStateFlow<File?>(null)
    val lastGeneratedPdf: StateFlow<File?> = _lastGeneratedPdf.asStateFlow()

    // -------------------------------------------------------------------------
    // On-Device SLM State
    // -------------------------------------------------------------------------
    private val _isSlmGenerating = MutableStateFlow(false)
    val isSlmGenerating: StateFlow<Boolean> = _isSlmGenerating.asStateFlow()

    private val _slmStatusMessage = MutableStateFlow<String?>(null)
    val slmStatusMessage: StateFlow<String?> = _slmStatusMessage.asStateFlow()

    private val _activeSlmPlan = MutableStateFlow<SlmCurriculumPlan?>(null)
    val activeSlmPlan: StateFlow<SlmCurriculumPlan?> = _activeSlmPlan.asStateFlow()

    init {
        org.tribetalk.core.TribeTalkTranslator.initialize(application.applicationContext)
        org.tribetalk.fln.slm.SlmCurriculumEngine.initialize(application.applicationContext)
        setupQuizQuestion()
    }

    // -------------------------------------------------------------------------
    // Flashcard Actions
    // -------------------------------------------------------------------------

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        _cards.value = FlnCurriculumRepository.getFlashcardsByCategory(category)
        _currentCardIndex.value = 0
        _isCardRevealed.value = false
        setupQuizQuestion()
    }

    fun nextCard() {
        val total = _cards.value.size
        if (total > 0) {
            _currentCardIndex.value = (_currentCardIndex.value + 1) % total
            _isCardRevealed.value = false
            setupQuizQuestion()
        }
    }

    fun prevCard() {
        val total = _cards.value.size
        if (total > 0) {
            _currentCardIndex.value = if (_currentCardIndex.value - 1 < 0) total - 1 else _currentCardIndex.value - 1
            _isCardRevealed.value = false
            setupQuizQuestion()
        }
    }

    fun shuffleCards() {
        _cards.value = _cards.value.shuffled()
        _currentCardIndex.value = 0
        _isCardRevealed.value = false
        setupQuizQuestion()
    }

    fun toggleQuizMode() {
        _isQuizMode.value = !_isQuizMode.value
        _isCardRevealed.value = false
        setupQuizQuestion()
    }

    fun toggleCardReveal() {
        _isCardRevealed.value = !_isCardRevealed.value
    }

    fun playSantaliAudio(card: FlnCard) {
        ttsManager.speak(card.santaliOlChiki, "sat")
    }

    // -------------------------------------------------------------------------
    // Classroom Turn-Taking Actions
    // -------------------------------------------------------------------------

    fun setClassroomMode(mode: ClassroomPlayMode) {
        _classroomMode.value = mode
        _isCardRevealed.value = false
        _selectedTapCard.value = null
        _isTapAnswerCorrect.value = null
        _kidVoiceSuccess.value = null
        if (mode == ClassroomPlayMode.LISTEN_AND_TAP) {
            setupListenAndTap()
        } else if (mode == ClassroomPlayMode.VOICE_ECHO) {
            playCurrentCardSantali()
        }
    }

    fun nextChildTurn() {
        _kidsTurnCount.value++
        nextCard()
        _selectedTapCard.value = null
        _isTapAnswerCorrect.value = null
        _kidVoiceSuccess.value = null
        if (_classroomMode.value == ClassroomPlayMode.LISTEN_AND_TAP) {
            setupListenAndTap()
        } else if (_classroomMode.value == ClassroomPlayMode.VOICE_ECHO) {
            playCurrentCardSantali()
        }
    }

    fun setupListenAndTap() {
        val current = _cards.value.getOrNull(_currentCardIndex.value) ?: return
        val otherCards = _cards.value.filter { it.id != current.id }.shuffled().take(2)
        val all = (otherCards + current).shuffled()
        _listenAndTapOptions.value = all
        _selectedTapCard.value = null
        _isTapAnswerCorrect.value = null
        // Auto-play the target sound
        ttsManager.speak(current.santaliOlChiki, "sat")
    }

    fun selectTapCard(card: FlnCard) {
        val current = _cards.value.getOrNull(_currentCardIndex.value) ?: return
        if (_isTapAnswerCorrect.value == true) return
        _selectedTapCard.value = card
        val correct = (card.id == current.id)
        _isTapAnswerCorrect.value = correct
        if (correct) {
            ttsManager.speak(card.santaliOlChiki, "sat")
        }
    }

    fun playCurrentCardSantali() {
        val current = _cards.value.getOrNull(_currentCardIndex.value) ?: return
        ttsManager.speak(current.santaliOlChiki, "sat")
    }

    fun startKidVoicePractice() {
        val current = _cards.value.getOrNull(_currentCardIndex.value) ?: return
        _isKidSpeaking.value = true
        _kidVoiceSuccess.value = null

        speechRecognizer.startListening(
            isHindi = false,
            onResult = {
                _isKidSpeaking.value = false
                _kidVoiceSuccess.value = true
                ttsManager.speak(current.santaliOlChiki, "sat")
            },
            onError = {
                // Offline fallback: treat verbal attempt as praised learning effort
                _isKidSpeaking.value = false
                _kidVoiceSuccess.value = true
            }
        )
    }

    fun stopKidVoicePractice() {
        speechRecognizer.stopListening()
        _isKidSpeaking.value = false
    }

    // -------------------------------------------------------------------------
    // Quiz Mechanics
    // -------------------------------------------------------------------------

    private fun setupQuizQuestion() {
        val current = _cards.value.getOrNull(_currentCardIndex.value) ?: return
        val otherCards = _cards.value.filter { it.id != current.id }.shuffled().take(3)
        val distractors = otherCards.map { "${it.santaliOlChiki}  (${it.hindiText.substringBefore(" ")})" }
        val correct = "${current.santaliOlChiki}  (${current.hindiText.substringBefore(" ")})"
        val all = (distractors + correct).shuffled()
        _quizOptions.value = all
        _quizCorrectIndex.value = all.indexOf(correct)
        _selectedQuizOption.value = null
        _isQuizAnswerChecked.value = false
    }

    fun selectQuizOption(index: Int) {
        if (_isQuizAnswerChecked.value) return
        _selectedQuizOption.value = index
        _isQuizAnswerChecked.value = true
        _quizAnsweredCount.value++
        if (index == _quizCorrectIndex.value) {
            _quizScore.value++
        }
    }

    fun resetQuiz() {
        _quizScore.value = 0
        _quizAnsweredCount.value = 0
        setupQuizQuestion()
    }

    /**
     * Synthesizes and adds a custom bilingual flashcard from teacher prompt.
     */
    fun createCustomFlashcard(hindiPrompt: String) {
        if (hindiPrompt.isBlank()) return
        val card = ProceduralCurriculumGenerator.synthesizeCard(hindiPrompt)
        FlnCurriculumRepository.addCustomCard(card)
        _categories.value = FlnCurriculumRepository.getFlashcardCategories()
        _selectedCategory.value = FlnCurriculumRepository.CATEGORY_CUSTOM
        _cards.value = FlnCurriculumRepository.getFlashcardsByCategory(FlnCurriculumRepository.CATEGORY_CUSTOM)
        _currentCardIndex.value = 0
        _customCardSuccessMessage.value = "Created: ${card.santaliOlChiki} [${card.teacherPhoneticGuide}]"
        setupQuizQuestion()
    }

    fun clearCustomCardMessage() {
        _customCardSuccessMessage.value = null
    }

    // -------------------------------------------------------------------------
    // Worksheet Studio Actions
    // -------------------------------------------------------------------------

    fun setWorksheetType(type: WorksheetType) {
        val newConfig = _worksheetConfig.value.copy(type = type, seed = System.currentTimeMillis())
        _worksheetConfig.value = newConfig
        _worksheetItems.value = FlnCurriculumRepository.generateWorksheet(newConfig)
    }

    fun setWorksheetGrade(grade: FlnGrade) {
        val newConfig = _worksheetConfig.value.copy(grade = grade, seed = System.currentTimeMillis())
        _worksheetConfig.value = newConfig
        _worksheetItems.value = FlnCurriculumRepository.generateWorksheet(newConfig)
    }

    fun setWorksheetDifficulty(difficulty: WorksheetDifficulty) {
        val newConfig = _worksheetConfig.value.copy(difficulty = difficulty, seed = System.currentTimeMillis())
        _worksheetConfig.value = newConfig
        _worksheetItems.value = FlnCurriculumRepository.generateWorksheet(newConfig)
    }

    fun setWorksheetSchoolName(name: String) {
        _worksheetConfig.value = _worksheetConfig.value.copy(schoolName = name)
    }

    fun setPreviewTab(tab: WorksheetPreviewTab) {
        _previewTab.value = tab
    }

    fun regenerateWorksheet() {
        val newConfig = _worksheetConfig.value.copy(seed = System.currentTimeMillis())
        _worksheetConfig.value = newConfig
        _worksheetItems.value = FlnCurriculumRepository.generateWorksheet(newConfig)
    }

    fun exportAndSharePdf(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGeneratingPdf.value = true
            try {
                val pdfFile = WorksheetPdfExporter.generatePdf(
                    context = context,
                    config = _worksheetConfig.value,
                    items = _worksheetItems.value
                )
                _lastGeneratedPdf.value = pdfFile
                if (pdfFile != null) {
                    launch(Dispatchers.Main) {
                        WorksheetPdfExporter.sharePdf(context, pdfFile)
                    }
                }
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    // -------------------------------------------------------------------------
    // On-Device SLM Generation Actions
    // -------------------------------------------------------------------------

    /**
     * Synthesizes an entire NIPUN curriculum worksheet using the SLM pipeline.
     */
    fun generateCurriculumWithSlm(
        prompt: String,
        grade: FlnGrade? = null,
        type: WorksheetType? = null
    ) {
        val targetGrade = grade ?: _worksheetConfig.value.grade
        val targetType = type ?: _worksheetConfig.value.type

        viewModelScope.launch(Dispatchers.Default) {
            _isSlmGenerating.value = true
            try {
                val request = SlmCurriculumRequest(
                    topicPrompt = prompt,
                    grade = targetGrade,
                    worksheetType = targetType,
                    questionCount = 5
                )

                val (plan, items) = org.tribetalk.fln.slm.SlmCurriculumEngine.generateCurriculumPlan(request)

                _activeSlmPlan.value = plan
                _worksheetConfig.value = _worksheetConfig.value.copy(
                    title = "NIPUN: ${plan.theme}",
                    grade = targetGrade,
                    type = targetType
                )
                _worksheetItems.value = items
                _slmStatusMessage.value = "Generated '${plan.theme}' (${plan.nipunCode})"
            } catch (e: Exception) {
                _slmStatusMessage.value = "SLM note: using standard verified curriculum"
            } finally {
                _isSlmGenerating.value = false
            }
        }
    }

    /**
     * Synthesizes a themed 5-card bilingual flashcard deck using the SLM pipeline.
     */
    fun generateFlashcardsWithSlm(topicPrompt: String) {
        if (topicPrompt.isBlank()) return

        viewModelScope.launch(Dispatchers.Default) {
            _isSlmGenerating.value = true
            try {
                val newCards = org.tribetalk.fln.slm.SlmCurriculumEngine.generateFlashcardDeck(topicPrompt)
                for (card in newCards) {
                    FlnCurriculumRepository.addCustomCard(card)
                }

                _categories.value = FlnCurriculumRepository.getCategories()
                _selectedCategory.value = FlnCurriculumRepository.CATEGORY_CUSTOM
                _cards.value = FlnCurriculumRepository.getCardsByCategory(FlnCurriculumRepository.CATEGORY_CUSTOM)
                _currentCardIndex.value = 0
                _slmStatusMessage.value = "Synthesized ${newCards.size} cards for '$topicPrompt'"
                setupQuizQuestion()
            } catch (e: Exception) {
                _slmStatusMessage.value = "Flashcard synthesis note: ${e.message}"
            } finally {
                _isSlmGenerating.value = false
            }
        }
    }

    fun clearSlmStatusMessage() {
        _slmStatusMessage.value = null
    }
}
