package org.tribetalk.ui.screens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.tribetalk.audio.TribeTalkTtsManager
import org.tribetalk.core.orchestration.ResourceOrchestrator
import org.tribetalk.core.orchestration.TaskPriority
import org.tribetalk.curriculum.ai.QwenContentPlanner
import org.tribetalk.curriculum.ai.QwenLocalModel
import org.tribetalk.curriculum.cache.CurriculumCacheManager
import org.tribetalk.curriculum.cache.CurriculumPrefetcher
import org.tribetalk.curriculum.context.TeachingContext
import org.tribetalk.curriculum.ontology.CurriculumRegistry
import org.tribetalk.curriculum.ontology.LearningObjective
import org.tribetalk.curriculum.render.FlashcardSetGenerator
import org.tribetalk.curriculum.spec.ActivitySpec
import org.tribetalk.curriculum.spec.VisualTheme
import org.tribetalk.fln.model.*
import org.tribetalk.fln.pipeline.*
import org.tribetalk.fln.repository.FlnCurriculumRepository
import org.tribetalk.fln.worksheet.WorksheetGenerator
import org.tribetalk.fln.worksheet.WorksheetPdfExporter
import java.io.File

/**
 * Clean, unified ViewModel managing state for the Foundational Literacy & Numeracy (FLN) suite.
 * High-performance, reactive, and 100% offline.
 */
class FlnViewModel(application: Application) : AndroidViewModel(application) {

    private val ttsManager = TribeTalkTtsManager(application.applicationContext)
    val isSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking

    // -------------------------------------------------------------------------
    // Real Qwen 0.5B Inference & Central Teaching Context
    // -------------------------------------------------------------------------
    val localModel = QwenLocalModel(application.applicationContext)
    val contentPlanner = QwenContentPlanner(localModel)
    val prefetcher = CurriculumPrefetcher(contentPlanner)

    private val _teachingContext = MutableStateFlow(TeachingContext())
    val teachingContext: StateFlow<TeachingContext> = _teachingContext.asStateFlow()

    private val _activeObjective = MutableStateFlow(CurriculumRegistry.getDefaultObjective())
    val activeObjective: StateFlow<LearningObjective> = _activeObjective.asStateFlow()

    private val _currentActivitySpec = MutableStateFlow<ActivitySpec?>(null)
    val currentActivitySpec: StateFlow<ActivitySpec?> = _currentActivitySpec.asStateFlow()

    private val _isStudioOpen = MutableStateFlow(false)
    val isStudioOpen: StateFlow<Boolean> = _isStudioOpen.asStateFlow()

    private val _isGeneratingStudioContent = MutableStateFlow(false)
    val isGeneratingStudioContent: StateFlow<Boolean> = _isGeneratingStudioContent.asStateFlow()

    private val _studioProgressStep = MutableStateFlow(0)
    val studioProgressStep: StateFlow<Int> = _studioProgressStep.asStateFlow()

    private val _studioGeneratedSpec = MutableStateFlow<ActivitySpec?>(null)
    val studioGeneratedSpec: StateFlow<ActivitySpec?> = _studioGeneratedSpec.asStateFlow()

    // -------------------------------------------------------------------------
    // Shared Generative Pipeline & Activity IR State
    // -------------------------------------------------------------------------
    private val _currentActivityIR = MutableStateFlow<ActivityIR?>(null)
    val currentActivityIR: StateFlow<ActivityIR?> = _currentActivityIR.asStateFlow()

    private val _learnerState = MutableStateFlow(LearnerState())
    val learnerState: StateFlow<LearnerState> = _learnerState.asStateFlow()

    // -------------------------------------------------------------------------
    // Flashcard Deck State
    // -------------------------------------------------------------------------
    private val _selectedDomain = MutableStateFlow<FlnDomain?>(null)
    val selectedDomain: StateFlow<FlnDomain?> = _selectedDomain.asStateFlow()

    private val _selectedCategory = MutableStateFlow(FlnCategory.ALL)
    val selectedCategory: StateFlow<FlnCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredCards = MutableStateFlow(FlnCurriculumRepository.getAllCards())
    val filteredCards: StateFlow<List<FlnCard>> = _filteredCards.asStateFlow()

    private val _activeCardIndex = MutableStateFlow(0)
    val activeCardIndex: StateFlow<Int> = _activeCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    private val _playMode = MutableStateFlow(FlnPlayMode.EXPLORE)
    val playMode: StateFlow<FlnPlayMode> = _playMode.asStateFlow()

    val activeCard: StateFlow<FlnCard?> = combine(_filteredCards, _activeCardIndex) { cards, idx ->
        if (cards.isNotEmpty() && idx in cards.indices) cards[idx] else cards.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // -------------------------------------------------------------------------
    // Star Quiz State
    // -------------------------------------------------------------------------
    private val _quizQuestion = MutableStateFlow<QuizQuestion?>(null)
    val quizQuestion: StateFlow<QuizQuestion?> = _quizQuestion.asStateFlow()

    private val _selectedQuizOption = MutableStateFlow<Int?>(null)
    val selectedQuizOption: StateFlow<Int?> = _selectedQuizOption.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizTotal = MutableStateFlow(0)
    val quizTotal: StateFlow<Int> = _quizTotal.asStateFlow()

    // -------------------------------------------------------------------------
    // Worksheet Studio State
    // -------------------------------------------------------------------------
    private val _worksheetConfig = MutableStateFlow(WorksheetConfig())
    val worksheetConfig: StateFlow<WorksheetConfig> = _worksheetConfig.asStateFlow()

    private val _worksheetItems = MutableStateFlow<List<WorksheetItem>>(emptyList())
    val worksheetItems: StateFlow<List<WorksheetItem>> = _worksheetItems.asStateFlow()

    private val _previewTab = MutableStateFlow(WorksheetPreviewTab.STUDENT_SHEET)
    val previewTab: StateFlow<WorksheetPreviewTab> = _previewTab.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _lastGeneratedPdf = MutableStateFlow<File?>(null)
    val lastGeneratedPdf: StateFlow<File?> = _lastGeneratedPdf.asStateFlow()

    init {
        SvgCorpusRegistry.init(application.applicationContext)
        ResourceOrchestrator.registerSlm(localModel)
        updateFilteredCards()
        regenerateWorksheet()
        setupNewQuizQuestion()
        initializeDefaultActivityIR()
        prefetcher.prefetchForLesson(_teachingContext.value, _activeObjective.value)
    }

    // -------------------------------------------------------------------------
    // Content Studio & Real Qwen Generation Actions
    // -------------------------------------------------------------------------

    fun openStudio() {
        _isStudioOpen.value = true
        _studioGeneratedSpec.value = null
        _studioProgressStep.value = 0
    }

    fun closeStudio() {
        _isStudioOpen.value = false
        _isGeneratingStudioContent.value = false
    }

    fun generateStudioMaterials(
        difficulty: WorksheetDifficulty = WorksheetDifficulty.EASY,
        theme: VisualTheme = VisualTheme.GARDEN,
        genFlashcards: Boolean = true,
        genWorksheet: Boolean = true
    ) {
        viewModelScope.launch {
            _isGeneratingStudioContent.value = true
            _studioProgressStep.value = 0

            try {
                // Step 0: Understanding learning objective
                _studioProgressStep.value = 1
                delay(80)

                // Step 1: Planning activities with Qwen 0.5B
                _studioProgressStep.value = 2
                val context = _teachingContext.value.copy(difficulty = difficulty)
                val objective = _activeObjective.value
                val seed = System.currentTimeMillis()

                val planResult = contentPlanner.generateActivityPlan(context, objective, seed)

                // Step 2 & 3: Creating questions & checking math
                _studioProgressStep.value = 3
                delay(60)
                _studioProgressStep.value = 4
                delay(60)

                // Step 4: Building offline visuals
                val visualSpec = planResult.spec.visualSpec.copy(theme = theme)
                val tunedSpec = planResult.spec.copy(visualSpec = visualSpec)
                _studioProgressStep.value = 5
                delay(60)

                // Step 5 & 6: Preparing Hindi & Santhali
                _studioProgressStep.value = 6
                delay(60)
                _studioProgressStep.value = 7
                delay(60)

                _studioGeneratedSpec.value = tunedSpec
            } finally {
                _isGeneratingStudioContent.value = false
            }
        }
    }

    fun applyGeneratedSpec(spec: ActivitySpec) {
        _currentActivitySpec.value = spec
        val ir = ActivitySpec.toActivityIR(spec)
        setActivityIR(ir)

        // 1. Update Flashcards with the structured 6-card lesson set
        val cards = FlashcardSetGenerator.generateSet(spec, _teachingContext.value)
        _filteredCards.value = cards
        _activeCardIndex.value = 0

        // 2. Update Worksheets with the same items
        val wsItems = WorksheetGenerator.generateFromActivitySpec(spec, _worksheetConfig.value)
        _worksheetItems.value = wsItems

        // 3. Cache the real generation results
        val cacheKey = CurriculumCacheManager.buildCacheKey(_teachingContext.value, _activeObjective.value, spec.metadata.generationSeed)
        CurriculumCacheManager.putActivity(cacheKey, spec)
        CurriculumCacheManager.putFlashcards(cacheKey, cards)
        CurriculumCacheManager.putWorksheet(cacheKey, wsItems)
    }

    fun setActiveObjective(objective: LearningObjective) {
        _activeObjective.value = objective
        _teachingContext.value = _teachingContext.value.copy(
            objectiveId = objective.id,
            grade = objective.grade,
            domain = objective.domain
        )
        prefetcher.prefetchForLesson(_teachingContext.value, objective)
    }

    // -------------------------------------------------------------------------
    // Shared Activity IR & Progression Pipeline Actions
    // -------------------------------------------------------------------------

    fun setActivityIR(ir: ActivityIR) {
        val validated = ActivityValidator.validateAndRepair(ir)
        _currentActivityIR.value = validated.repairedIR
    }

    fun advanceToNextPhrase(objectKey: String) {
        val nextTier = when (_learnerState.value.currentTier) {
            LinguisticTier.ISOLATED_AKSHAR -> LinguisticTier.CORE_VOCABULARY
            LinguisticTier.CORE_VOCABULARY -> LinguisticTier.DESCRIPTIVE_PHRASE
            LinguisticTier.DESCRIPTIVE_PHRASE -> LinguisticTier.FLUENCY_SENTENCE
            else -> LinguisticTier.FLUENCY_SENTENCE
        }
        val nextIR = LearnerProgressionEngine.generateNextPhraseActivity(
            objectKey = objectKey,
            targetTier = nextTier,
            grade = _learnerState.value.grade
        )
        setActivityIR(nextIR)
    }

    fun submitActivityAnswer(selectedAnswer: String) {
        val current = _currentActivityIR.value ?: return
        val isSuccess = selectedAnswer == current.correctValue
        val updatedState = LearnerProgressionEngine.recordActivityCompletion(_learnerState.value, current, isSuccess)
        _learnerState.value = updatedState

        if (isSuccess) {
            ttsManager.speak("ᱟᱹᱰᱤ ᱱᱟᱯᱟᱭ! ${current.correctValue}", "sat")
        }
    }

    fun playActivitySantaliAudio() {
        val current = _currentActivityIR.value ?: return
        val text = current.bilingualPhraseSantali.ifBlank { current.santaliWord.ifBlank { current.correctValue } }
        ttsManager.speak(text, "sat")
    }

    fun playActivityHindiAudio() {
        val current = _currentActivityIR.value ?: return
        val text = current.bilingualPhraseHindi.ifBlank { current.hindiWord.ifBlank { current.instructionHindi } }
        ttsManager.speak(text, "hi")
    }

    fun playSantaliText(text: String) {
        if (text.isNotBlank()) ttsManager.speak(text, "sat")
    }

    fun playHindiText(text: String) {
        if (text.isNotBlank()) ttsManager.speak(text, "hi")
    }

    private fun initializeDefaultActivityIR() {
        val defaultIR = ActivityIR(
            id = "act_init_mango",
            nipunCompetencyCode = "N-BAL.1",
            actionType = ActivityActionType.COUNT_AND_SELECT,
            grade = FlnGrade.BALVATIKA,
            difficulty = WorksheetDifficulty.EASY,
            linguisticTier = LinguisticTier.CORE_VOCABULARY,
            primaryObjectKey = "mango",
            quantity = 5,
            correctValue = "5",
            distractorOptions = listOf("3", "5", "6"),
            santaliWord = "ᱩᱞ",
            hindiWord = "आम",
            englishWord = "Mango",
            bilingualPhraseSantali = "ᱦᱮᱲᱮᱢ ᱩᱞ",
            bilingualPhraseHindi = "मीठा आम",
            phraseEnglishGloss = "Sweet Mango",
            instructionSantali = "ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱥᱟᱹᱦᱤ ᱞᱮᱠᱷᱟ ᱵᱟᱪᱷᱟᱣ ᱢᱮ:",
            instructionHindi = "आम गिनें और सही संख्या चुनें:"
        )
        setActivityIR(defaultIR)
    }

    // -------------------------------------------------------------------------
    // Flashcard Actions
    // -------------------------------------------------------------------------

    fun setDomain(domain: FlnDomain?) {
        _selectedDomain.value = domain
        _selectedCategory.value = FlnCategory.ALL
        _activeCardIndex.value = 0
        _isCardFlipped.value = false
        updateFilteredCards()
        setupNewQuizQuestion()
    }

    fun setCategory(category: FlnCategory) {
        _selectedCategory.value = category
        _activeCardIndex.value = 0
        _isCardFlipped.value = false
        updateFilteredCards()
        setupNewQuizQuestion()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _activeCardIndex.value = 0
        updateFilteredCards()
    }

    fun setPlayMode(mode: FlnPlayMode) {
        _playMode.value = mode
        _isCardFlipped.value = false
        if (mode == FlnPlayMode.QUIZ && _quizQuestion.value == null) {
            setupNewQuizQuestion()
        }
    }

    fun toggleCardFlip() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun nextCard() {
        val total = _filteredCards.value.size
        if (total > 0) {
            _activeCardIndex.value = (_activeCardIndex.value + 1) % total
            _isCardFlipped.value = false
        }
    }

    fun previousCard() {
        val total = _filteredCards.value.size
        if (total > 0) {
            _activeCardIndex.value = if (_activeCardIndex.value - 1 < 0) total - 1 else _activeCardIndex.value - 1
            _isCardFlipped.value = false
        }
    }

    fun selectCard(card: FlnCard) {
        val index = _filteredCards.value.indexOf(card)
        if (index != -1) {
            _activeCardIndex.value = index
            _isCardFlipped.value = false
        }
    }

    fun playActiveCardAudio() {
        val card = activeCard.value ?: return
        val textToSpeak = when {
            card.category == FlnCategory.AKSHAR -> {
                card.teacherPhoneticGuide.substringBefore("(").trim().ifBlank { card.santaliOlChiki }
            }
            card.exemplarPhonetic.isNotBlank() -> card.exemplarPhonetic
            card.teacherPhoneticGuide.isNotBlank() -> card.teacherPhoneticGuide.substringBefore("(").trim()
            else -> card.santaliOlChiki
        }
        ttsManager.speakSantaliPhonetic(textToSpeak)
    }

    fun playActiveCardHindiAudio() {
        val card = activeCard.value ?: return
        val text = card.hindiText.substringBefore("(").trim()
        ttsManager.speak(text, "hi")
    }

    private fun updateFilteredCards() {
        val query = _searchQuery.value.trim()
        val domain = _selectedDomain.value
        val cat = _selectedCategory.value

        var list = when {
            query.isNotBlank() -> FlnCurriculumRepository.searchCards(query)
            cat != FlnCategory.ALL -> FlnCurriculumRepository.getCardsByCategory(cat)
            domain != null -> FlnCurriculumRepository.getCardsByDomain(domain)
            else -> FlnCurriculumRepository.getAllCards()
        }

        if (domain != null && cat == FlnCategory.ALL && query.isBlank()) {
            list = list.filter { it.domain == domain }
        }

        _filteredCards.value = list
        if (_activeCardIndex.value >= list.size) {
            _activeCardIndex.value = 0
        }
    }

    // -------------------------------------------------------------------------
    // Star Quiz Actions
    // -------------------------------------------------------------------------

    fun setupNewQuizQuestion() {
        val pool = _filteredCards.value.ifEmpty { FlnCurriculumRepository.getAllCards() }
        if (pool.isEmpty()) return

        val targetCard = pool.random()
        val isAkshar = targetCard.category == FlnCategory.AKSHAR
        val isNumber = targetCard.category == FlnCategory.NUMBERS

        val promptHindi = when {
            isAkshar -> "चित्र के पहले अक्षर / ध्वनि की पहचान करें:"
            isNumber -> "चित्र में कितनी वस्तुएं हैं? गिनकर सही संख्या चुनें:"
            else -> "चित्र में क्या दिखाया गया है? सही नाम चुनें:"
        }

        val promptSantali = when {
            isAkshar -> "ᱱᱚᱣᱟ ᱪᱤᱛᱟᱹᱨ ᱨᱮᱭᱟᱜ ᱮᱛᱚᱦᱚᱵ ᱪᱤᱠᱤ ᱪᱮᱫ ᱠᱟᱱᱟ?"
            isNumber -> "ᱱᱚᱣᱟ ᱪᱤᱛᱟᱹᱨ ᱨᱮ ᱛᱤᱱᱟᱹᱜ ᱡᱤᱱᱤᱥ ᱢᱮᱱᱟᱜᱼᱟ? ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱥᱟᱹᱦᱤ ᱮᱞ ᱵᱟᱪᱷᱟᱣ ᱢᱮ:"
            else -> "ᱱᱚᱣᱟ ᱪᱤᱛᱟᱹᱨ ᱨᱮ ᱪᱮᱫ ᱢᱮᱱᱟᱜᱼᱟ? ᱥᱟᱹᱦᱤ ᱧᱩᱛᱩᱢ ᱵᱟᱪᱷᱟᱣ ᱢᱮ:"
        }

        val correctAnswer = when {
            isNumber -> {
                val num = targetCard.numeralValue ?: 1
                "${FlnCurriculumRepository.toOlChikiDigits(num)} ($num)"
            }
            isAkshar -> targetCard.santaliOlChiki
            else -> targetCard.santaliOlChiki
        }

        // Category-matched distractors: strictly 3 distinct distractors matching the semantic type
        val distractors: List<String> = when {
            isNumber -> {
                val num = targetCard.numeralValue ?: 1
                val candidates = mutableListOf<Int>()
                for (offset in listOf(1, -1, 2, -2, 3, -3, 4, -4)) {
                    val cand = num + offset
                    if (cand in 1..20 && cand != num && !candidates.contains(cand)) {
                        candidates.add(cand)
                    }
                    if (candidates.size >= 3) break
                }
                var fallback = 1
                while (candidates.size < 3 && fallback <= 20) {
                    if (fallback != num && !candidates.contains(fallback)) {
                        candidates.add(fallback)
                    }
                    fallback++
                }
                candidates.take(3).map { "${FlnCurriculumRepository.toOlChikiDigits(it)} ($it)" }
            }
            isAkshar -> {
                val otherAkshars = FlnCurriculumRepository.getCardsByCategory(FlnCategory.AKSHAR)
                    .filter { it.id != targetCard.id && it.santaliOlChiki != correctAnswer }
                    .map { it.santaliOlChiki }
                    .distinct()
                    .shuffled()
                val list = otherAkshars.take(3).toMutableList()
                val defaultAkshars = listOf("ᱚ", "ᱛ", "ᱜ", "ᱝ", "ᱞ", "ᱟ", "ᱠ", "ᱡ", "ᱢ", "ᱣ", "ᱤ", "ᱥ")
                for (a in defaultAkshars) {
                    if (list.size >= 3) break
                    if (a != correctAnswer && !list.contains(a)) list.add(a)
                }
                list.take(3)
            }
            else -> {
                FlnCurriculumRepository.generateDistractorsForCard(targetCard, 3)
            }
        }

        // Ensure strictly 4 distinct shuffled options, always containing correctAnswer
        val allOptions = (distractors + correctAnswer).distinct().toMutableList()
        if (allOptions.size < 4 && isNumber) {
            val num = targetCard.numeralValue ?: 1
            for (fallback in 1..20) {
                val opt = "${FlnCurriculumRepository.toOlChikiDigits(fallback)} ($fallback)"
                if (!allOptions.contains(opt)) allOptions.add(opt)
                if (allOptions.size >= 4) break
            }
        }
        val finalOptions = allOptions.take(4).shuffled()
        val correctIdx = finalOptions.indexOf(correctAnswer)

        _quizQuestion.value = QuizQuestion(
            card = targetCard,
            promptHindi = promptHindi,
            promptSantali = promptSantali,
            options = finalOptions,
            correctOptionIndex = if (correctIdx != -1) correctIdx else 0,
            hintHindi = "सही उत्तर है: $correctAnswer"
        )
        _selectedQuizOption.value = null
    }

    fun submitQuizAnswer(optionIndex: Int) {
        if (_selectedQuizOption.value != null) return // Already submitted
        _selectedQuizOption.value = optionIndex
        _quizTotal.value += 1

        val current = _quizQuestion.value ?: return
        if (optionIndex == current.correctOptionIndex) {
            _quizScore.value += 1
            val phoneticText = current.card.exemplarPhonetic.ifBlank {
                current.card.teacherPhoneticGuide.substringBefore("(").trim().ifBlank { current.card.santaliOlChiki }
            }
            ttsManager.speakSantaliPhonetic(phoneticText)
        } else {
            ttsManager.speak("ᱟᱨ ᱢᱤᱫ ᱫᱷᱟᱣ ᱪᱮᱥᱴᱟᱭ ᱢᱮ", "sat")
        }
    }

    fun nextQuizQuestion() {
        setupNewQuizQuestion()
    }

    fun playQuizAudio() {
        val q = _quizQuestion.value ?: return
        val text = q.promptSantali.ifBlank { q.promptHindi }
        ttsManager.speak(text, "sat")
    }

    // -------------------------------------------------------------------------
    // Worksheet Studio Actions
    // -------------------------------------------------------------------------

    fun setWorksheetType(type: WorksheetType) {
        _worksheetConfig.value = _worksheetConfig.value.copy(type = type)
        regenerateWorksheet()
    }

    fun setWorksheetGrade(grade: FlnGrade) {
        _worksheetConfig.value = _worksheetConfig.value.copy(grade = grade)
        regenerateWorksheet()
    }

    fun setWorksheetDifficulty(diff: WorksheetDifficulty) {
        _worksheetConfig.value = _worksheetConfig.value.copy(difficulty = diff)
        regenerateWorksheet()
    }

    fun setPreviewTab(tab: WorksheetPreviewTab) {
        _previewTab.value = tab
    }

    fun regenerateWorksheet() {
        val newSeed = System.currentTimeMillis()
        val currentConfig = _worksheetConfig.value.copy(seed = newSeed)
        _worksheetConfig.value = currentConfig
        _worksheetItems.value = WorksheetGenerator.generateWorksheet(currentConfig)
    }

    fun exportAndSharePdf(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGeneratingPdf.value = true
            try {
                val pdf = WorksheetPdfExporter.generatePdf(
                    context = context,
                    config = _worksheetConfig.value,
                    items = _worksheetItems.value
                )
                _lastGeneratedPdf.value = pdf
                if (pdf != null) {
                    launch(Dispatchers.Main) {
                        WorksheetPdfExporter.sharePdf(context, pdf)
                    }
                }
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    // -------------------------------------------------------------------------
    // Instant Bridges from Home Screen Translation
    // -------------------------------------------------------------------------

    fun createCustomFlashcard(topic: String) {
        val matchingCard = FlnCurriculumRepository.searchCards(topic).firstOrNull()
        if (matchingCard != null) {
            selectCard(matchingCard)
        }
    }

    fun generateWorksheetFromTopic(topic: String) {
        viewModelScope.launch {
            _isGeneratingStudioContent.value = true
            try {
                val context = _teachingContext.value.copy(
                    difficulty = _worksheetConfig.value.difficulty,
                    grade = _worksheetConfig.value.grade
                )
                val seed = System.currentTimeMillis()

                // Generate via Qwen 0.5B on-device content planner
                val planResult = contentPlanner.generateActivityPlanFromTopic(topic, context, seed)

                val newConfig = WorksheetGenerator.createConfigFromTopic(topic, _worksheetConfig.value.grade).copy(seed = seed)
                _worksheetConfig.value = newConfig

                val wsItems = WorksheetGenerator.generateFromActivitySpec(planResult.spec, newConfig)
                _worksheetItems.value = wsItems

                val ir = ActivitySpec.toActivityIR(planResult.spec)
                setActivityIR(ir)
            } catch (e: Exception) {
                // Safe procedural fallback
                val newConfig = WorksheetGenerator.createConfigFromTopic(topic, _worksheetConfig.value.grade)
                _worksheetConfig.value = newConfig
                _worksheetItems.value = WorksheetGenerator.generateWorksheet(newConfig)
                val ir = ActivitySynthesizer.synthesizeFromTopic(topic, _worksheetConfig.value.grade)
                setActivityIR(ir)
            } finally {
                _isGeneratingStudioContent.value = false
            }
        }
    }

    fun planActivityFromPrompt(topic: String, grade: FlnGrade = _worksheetConfig.value.grade) {
        generateWorksheetFromTopic(topic)
    }
}
