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
import org.tribetalk.fln.model.*
import org.tribetalk.fln.repository.FlnCurriculumRepository
import org.tribetalk.fln.worksheet.WorksheetPdfExporter
import java.io.File

/**
 * ViewModel managing state for NIPUN Bharat FLN flashcard deck and worksheet studio.
 * Runs completely offline and isolated from heavy neural ASR inference models.
 */
class FlnViewModel(application: Application) : AndroidViewModel(application) {

    private val ttsManager = TribeTalkTtsManager(application.applicationContext)

    // Flashcards State
    private val _categories = MutableStateFlow(FlnCurriculumRepository.getCategories())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow(FlnCurriculumRepository.CATEGORY_ALL)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _cards = MutableStateFlow(FlnCurriculumRepository.getAllCards())
    val cards: StateFlow<List<FlnCard>> = _cards.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isQuizMode = MutableStateFlow(false)
    val isQuizMode: StateFlow<Boolean> = _isQuizMode.asStateFlow()

    private val _isCardRevealed = MutableStateFlow(false)
    val isCardRevealed: StateFlow<Boolean> = _isCardRevealed.asStateFlow()

    val isPlayingAudio: StateFlow<Boolean> = ttsManager.isSpeaking

    // Worksheets State
    private val _worksheetConfig = MutableStateFlow(
        WorksheetConfig(
            title = "NIPUN Bharat FLN Practice Sheet",
            type = WorksheetType.COUNT_AND_MATCH,
            grade = FlnGrade.GRADE_1,
            questionCount = 5
        )
    )
    val worksheetConfig: StateFlow<WorksheetConfig> = _worksheetConfig.asStateFlow()

    private val _worksheetItems = MutableStateFlow(
        FlnCurriculumRepository.generateWorksheet(_worksheetConfig.value)
    )
    val worksheetItems: StateFlow<List<WorksheetItem>> = _worksheetItems.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _lastGeneratedPdf = MutableStateFlow<File?>(null)
    val lastGeneratedPdf: StateFlow<File?> = _lastGeneratedPdf.asStateFlow()

    // -------------------------------------------------------------------------
    // Flashcard Actions
    // -------------------------------------------------------------------------

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        _cards.value = FlnCurriculumRepository.getCardsByCategory(category)
        _currentCardIndex.value = 0
        _isCardRevealed.value = false
    }

    fun nextCard() {
        val total = _cards.value.size
        if (total > 0) {
            _currentCardIndex.value = (_currentCardIndex.value + 1) % total
            _isCardRevealed.value = false
        }
    }

    fun prevCard() {
        val total = _cards.value.size
        if (total > 0) {
            _currentCardIndex.value = if (_currentCardIndex.value - 1 < 0) total - 1 else _currentCardIndex.value - 1
            _isCardRevealed.value = false
        }
    }

    fun shuffleCards() {
        _cards.value = _cards.value.shuffled()
        _currentCardIndex.value = 0
        _isCardRevealed.value = false
    }

    fun toggleQuizMode() {
        _isQuizMode.value = !_isQuizMode.value
        _isCardRevealed.value = false
    }

    fun toggleCardReveal() {
        _isCardRevealed.value = !_isCardRevealed.value
    }

    fun playSantaliAudio(card: FlnCard) {
        ttsManager.speak(card.santaliOlChiki, "sat")
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
}
