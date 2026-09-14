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

import org.tribetalk.flashcards.Flashcard
import org.tribetalk.flashcards.FlashcardSet
import org.tribetalk.fln.progress.CardProgress
import org.tribetalk.fln.progress.FlnProgressManager

/**
 * Operating modes for the FLN Flashcard Experience.
 */
enum class FlashcardMode {
    STUDY,
    QUIZ,
    MATCH
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
    val progressManager: FlnProgressManager = FlnProgressManager.getInstance(application.applicationContext)

    // -------------------------------------------------------------------------
    // Flashcards State
    // -------------------------------------------------------------------------
    private val _categories = MutableStateFlow(FlnCurriculumRepository.getCategories())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow(FlnCurriculumRepository.CATEGORY_ALL)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _cards = MutableStateFlow(FlnCurriculumRepository.getAllCards())
    val cards: StateFlow<List<FlnCard>> = _cards.asStateFlow()

    private val _flashcards = MutableStateFlow(_cards.value.map { it.toFlashcard() })
    val flashcards: StateFlow<List<Flashcard>> = _flashcards.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _activeMode = MutableStateFlow(FlashcardMode.STUDY)
    val activeMode: StateFlow<FlashcardMode> = _activeMode.asStateFlow()

    private val _isQuizMode = MutableStateFlow(false)
    val isQuizMode: StateFlow<Boolean> = _isQuizMode.asStateFlow()

    private val _isCardRevealed = MutableStateFlow(false)
    val isCardRevealed: StateFlow<Boolean> = _isCardRevealed.asStateFlow()

    private val _activeDeckTitle = MutableStateFlow("All Topics")
    val activeDeckTitle: StateFlow<String> = _activeDeckTitle.asStateFlow()

    val isPlayingAudio: StateFlow<Boolean> = ttsManager.isSpeaking

    // Interactive Quiz State
    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizAnsweredCount = MutableStateFlow(0)
    val quizAnsweredCount: StateFlow<Int> = _quizAnsweredCount.asStateFlow()

    private val _isQuizCompleted = MutableStateFlow(false)
    val isQuizCompleted: StateFlow<Boolean> = _isQuizCompleted.asStateFlow()

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
        _flashcards.value = _cards.value.map { it.toFlashcard() }
        setupQuizQuestion()
        org.tribetalk.fln.slm.SlmCurriculumEngine.initialize(application.applicationContext)
    }

    private fun FlnCard.toFlashcard(): Flashcard = Flashcard(
        id = this.id,
        topic = this.category,
        skill = this.domain.displayName,
        hindiText = this.hindiText,
        santaliText = this.englishGloss,
        santaliOlChiki = this.santaliOlChiki,
        phoneticGuide = this.teacherPhoneticGuide,
        imageUri = this.imageUri ?: if (this.iconType.isNotBlank()) "ic_fln_${this.iconType}" else null,
        imageEmoji = this.imageEmoji ?: when (this.iconType) {
            "dog" -> "🐕"
            "cow" -> "🐄"
            "cat" -> "🐈"
            "elephant" -> "🐘"
            "bird" -> "🐦"
            "fish" -> "🐟"
            "goat" -> "🐐"
            "tree" -> "🌳"
            "sun" -> "☀️"
            "moon" -> "🌙"
            "water" -> "💧"
            "river" -> "🌊"
            "mountain" -> "⛰️"
            "flower" -> "🌸"
            "forest" -> "🌲"
            "house" -> "🏠"
            "book" -> "📖"
            "pen", "pencil" -> "✏️"
            "school" -> "🏫"
            "mother", "father", "friend" -> "🧑"
            else -> if (this.numeralValue != null) "${this.numeralValue}️⃣" else null
        },
        iconType = this.iconType,
        domain = this.domain.displayName,
        grade = "Grade 1-3",
        englishGloss = this.englishGloss,
        exampleSentenceHindi = this.exampleSentenceHindi,
        exampleSentenceSantali = this.exampleSentenceSantali
    )

    // -------------------------------------------------------------------------
    // Flashcard Actions & Mode Management
    // -------------------------------------------------------------------------

    fun setMode(mode: FlashcardMode) {
        _activeMode.value = mode
        _isQuizMode.value = (mode == FlashcardMode.QUIZ)
        _isCardRevealed.value = false
        when (mode) {
            FlashcardMode.QUIZ -> resetQuiz()
            FlashcardMode.STUDY -> {
                _cards.value.getOrNull(_currentCardIndex.value)?.let {
                    progressManager.recordCardSeen(it.id)
                }
            }
            FlashcardMode.MATCH -> {}
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        _activeDeckTitle.value = category
        val newCards = FlnCurriculumRepository.getCardsByCategory(category)
        _cards.value = newCards
        _flashcards.value = newCards.map { it.toFlashcard() }
        _currentCardIndex.value = 0
        _isCardRevealed.value = false
        setupQuizQuestion()
        newCards.firstOrNull()?.let { progressManager.recordCardSeen(it.id) }
    }

    fun nextCard() {
        val total = _cards.value.size
        if (total > 0) {
            _currentCardIndex.value = (_currentCardIndex.value + 1) % total
            _isCardRevealed.value = false
            _cards.value.getOrNull(_currentCardIndex.value)?.let {
                progressManager.recordCardSeen(it.id)
            }
            setupQuizQuestion()
        }
    }

    fun prevCard() {
        val total = _cards.value.size
        if (total > 0) {
            _currentCardIndex.value = if (_currentCardIndex.value - 1 < 0) total - 1 else _currentCardIndex.value - 1
            _isCardRevealed.value = false
            _cards.value.getOrNull(_currentCardIndex.value)?.let {
                progressManager.recordCardSeen(it.id)
            }
            setupQuizQuestion()
        }
    }

    fun shuffleCards() {
        _cards.value = _cards.value.shuffled()
        _flashcards.value = _cards.value.map { it.toFlashcard() }
        _currentCardIndex.value = 0
        _isCardRevealed.value = false
        setupQuizQuestion()
    }

    fun toggleQuizMode() {
        val newMode = if (_activeMode.value == FlashcardMode.QUIZ) FlashcardMode.STUDY else FlashcardMode.QUIZ
        setMode(newMode)
    }

    fun toggleCardReveal() {
        val newState = !_isCardRevealed.value
        _isCardRevealed.value = newState
        if (newState) {
            _cards.value.getOrNull(_currentCardIndex.value)?.let {
                progressManager.recordCardRevealed(it.id)
            }
        }
    }

    fun playSantaliAudio(card: Flashcard) {
        val text = if (!card.santaliOlChiki.isNullOrBlank()) card.santaliOlChiki else card.santaliText
        ttsManager.speak(text, "sat")
        progressManager.recordCardHeard(card.id)
    }

    fun playSantaliAudio(card: FlnCard) {
        ttsManager.speak(card.santaliOlChiki, "sat")
        progressManager.recordCardHeard(card.id)
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
        val isCorrect = (index == _quizCorrectIndex.value)
        if (isCorrect) {
            _quizScore.value++
        }
        _cards.value.getOrNull(_currentCardIndex.value)?.let {
            progressManager.recordQuizResult(it.id, isCorrect)
        }
    }

    fun onQuizNextQuestion() {
        val total = _cards.value.size
        if (_quizAnsweredCount.value >= total) {
            _isQuizCompleted.value = true
        } else {
            nextCard()
        }
    }

    fun resetQuiz() {
        _quizScore.value = 0
        _quizAnsweredCount.value = 0
        _isQuizCompleted.value = false
        _currentCardIndex.value = 0
        setupQuizQuestion()
    }

    fun recordMatchSuccess(cardId: String) {
        progressManager.recordMatchingResult(cardId, true)
    }

    fun saveTeacherCard(card: Flashcard) {
        val flnCard = FlnCard(
            id = card.id,
            domain = FlnDomain.LITERACY_VOCAB,
            category = if (card.topic.isNotBlank()) card.topic else FlnCurriculumRepository.CATEGORY_CUSTOM,
            nipunCode = "T-CUSTOM",
            hindiText = card.hindiText,
            santaliOlChiki = if (!card.santaliOlChiki.isNullOrBlank()) card.santaliOlChiki else card.santaliText,
            teacherPhoneticGuide = card.phoneticGuide ?: "",
            englishGloss = card.santaliText,
            iconType = if (card.iconType.isNotBlank()) card.iconType else "akshar",
            exampleSentenceHindi = card.exampleSentenceHindi,
            exampleSentenceSantali = card.exampleSentenceSantali,
            isCustomUserGenerated = true,
            imageUri = card.imageUri,
            imageEmoji = card.imageEmoji
        )
        FlnCurriculumRepository.addCustomCard(flnCard)
        _categories.value = FlnCurriculumRepository.getCategories()
        selectCategory(flnCard.category)
        _customCardSuccessMessage.value = "Saved Card: ${card.hindiText} ➔ ${flnCard.santaliOlChiki}"
    }

    fun applyCustomDeck(
        category: String,
        grade: String,
        skill: String,
        cardLimit: Int,
        mode: FlashcardMode = FlashcardMode.STUDY
    ) {
        val pool = if (category == FlnCurriculumRepository.CATEGORY_ALL) {
            FlnCurriculumRepository.getAllCards()
        } else {
            FlnCurriculumRepository.getCardsByCategory(category)
        }
        val limit = if (cardLimit > 0) cardLimit else 5
        val filtered = pool.take(limit)
        _cards.value = filtered
        _flashcards.value = filtered.map { it.toFlashcard() }
        _activeDeckTitle.value = "$category ($grade • $skill)"
        _currentCardIndex.value = 0
        _isCardRevealed.value = false
        setMode(mode)
    }

    /**
     * Synthesizes and adds a custom bilingual flashcard from teacher prompt.
     */
    fun createCustomFlashcard(hindiPrompt: String) {
        if (hindiPrompt.isBlank()) return
        val card = ProceduralCurriculumGenerator.synthesizeCard(hindiPrompt)
        FlnCurriculumRepository.addCustomCard(card)
        _categories.value = FlnCurriculumRepository.getCategories()
        _selectedCategory.value = FlnCurriculumRepository.CATEGORY_CUSTOM
        _cards.value = FlnCurriculumRepository.getCardsByCategory(FlnCurriculumRepository.CATEGORY_CUSTOM)
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
