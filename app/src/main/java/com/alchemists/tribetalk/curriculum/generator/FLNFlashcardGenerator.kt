package com.alchemists.tribetalk.curriculum.generator

import com.alchemists.tribetalk.curriculum.assets.LocalVisualAssetLibrary
import com.alchemists.tribetalk.curriculum.models.Flashcard
import com.alchemists.tribetalk.curriculum.models.LearningOutcome
import com.alchemists.tribetalk.translation.OlChikiTransliterator

object FLNFlashcardGenerator {

    fun generateFlashcards(
        outcome: LearningOutcome,
        count: Int = 6,
        seed: Long? = null
    ): List<Flashcard> {
        val effectiveSeed = seed ?: System.currentTimeMillis()
        val cards = mutableListOf<Flashcard>()
        val allAssets = LocalVisualAssetLibrary.fruits + LocalVisualAssetLibrary.animals + LocalVisualAssetLibrary.shapes + LocalVisualAssetLibrary.colours

        for (i in 0 until count.coerceAtMost(allAssets.size)) {
            val asset = allAssets[(i + (effectiveSeed % allAssets.size).toInt()) % allAssets.size]
            val phonetic = OlChikiTransliterator.toTeacherPhoneticHUD(asset.objectNameSantali)

            cards.add(
                Flashcard(
                    id = "fc_${outcome.id}_${i}_$effectiveSeed",
                    learningOutcomeId = outcome.id,
                    symbolOrWord = asset.iconEmoji,
                    textHindi = asset.objectNameHindi,
                    textSantali = asset.objectNameSantali,
                    phoneticDevanagari = phonetic,
                    stimulus = asset
                )
            )
        }
        return cards
    }
}
