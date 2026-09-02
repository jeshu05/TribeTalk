package com.alchemists.tribetalk.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.alchemists.tribetalk.curriculum.models.*
import com.alchemists.tribetalk.curriculum.repository.ContentPackRepository
import com.alchemists.tribetalk.curriculum.repository.FLNCurriculumRepository
import com.alchemists.tribetalk.translation.Language
import com.alchemists.tribetalk.voice.VoiceTranslationBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LessonSelectionUiState(
    val domainFilter: FLNDomain? = null,
    val lessons: List<Lesson> = FLNCurriculumRepository.lessons
)

data class LessonDetailUiState(
    val lesson: Lesson? = null,
    val isAudioPlaying: Boolean = false,
    val currentPlayingPrompt: String = "",
    val selectedQuizOptionIndex: Int? = null,
    val isQuizSubmitted: Boolean = false
)

class FLNViewModel : ViewModel() {

    private val _selectionState = MutableStateFlow(LessonSelectionUiState())
    val selectionState: StateFlow<LessonSelectionUiState> = _selectionState.asStateFlow()

    private val _detailState = MutableStateFlow(LessonDetailUiState())
    val detailState: StateFlow<LessonDetailUiState> = _detailState.asStateFlow()

    private var allLessons: List<Lesson> = FLNCurriculumRepository.lessons

    fun initializeContentPack(context: Context) {
        val packLessons = ContentPackRepository.loadContentPackLessons(context)
        if (packLessons.isNotEmpty()) {
            allLessons = packLessons
            val filtered = if (_selectionState.value.domainFilter == null) {
                allLessons
            } else {
                allLessons.filter { it.domain == _selectionState.value.domainFilter }
            }
            _selectionState.value = _selectionState.value.copy(lessons = filtered)
        }
    }

    fun selectDomainFilter(domain: FLNDomain?) {
        val filtered = if (domain == null) {
            allLessons
        } else {
            allLessons.filter { it.domain == domain }
        }
        _selectionState.value = _selectionState.value.copy(
            domainFilter = domain,
            lessons = filtered
        )
    }

    fun loadLesson(lessonId: String) {
        val lesson = allLessons.find { it.id == lessonId } ?: allLessons.first()
        _detailState.value = LessonDetailUiState(lesson = lesson)
    }

    fun selectQuizOption(index: Int) {
        _detailState.value = _detailState.value.copy(
            selectedQuizOptionIndex = index,
            isQuizSubmitted = false
        )
    }

    fun submitQuizAnswer() {
        _detailState.value = _detailState.value.copy(isQuizSubmitted = true)
    }

    fun playSantaliAudio(
        context: Context,
        teacherPromptHindi: String,
        santaliTranslation: String,
        audioAssetPath: String?,
        voiceTranslationBridge: VoiceTranslationBridge
    ) {
        _detailState.value = _detailState.value.copy(
            isAudioPlaying = true,
            currentPlayingPrompt = santaliTranslation
        )

        // 1. Try playing pre-rendered 24kHz WAV audio from content pack if available
        if (!audioAssetPath.isNullOrBlank()) {
            ContentPackRepository.playPreRenderedAudio(context, audioAssetPath) {
                _detailState.value = _detailState.value.copy(isAudioPlaying = false)
            }
            return
        }

        // 2. Fallback to offline local speech synthesizer
        voiceTranslationBridge.translateAndSpeak(
            recognizedText = teacherPromptHindi,
            sourceLanguage = Language.HINDI,
            targetLanguage = Language.SANTALI,
            isVoiceBridgeEnabled = true,
            onStateChange = { _, status ->
                if (status.contains("Complete") || status.contains("HUD") || status.contains("Error")) {
                    _detailState.value = _detailState.value.copy(isAudioPlaying = false)
                }
            },
            onResult = {}
        )
    }
}
