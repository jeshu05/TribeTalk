package org.tribetalk.curriculum.render

import org.tribetalk.curriculum.context.TeachingContext
import org.tribetalk.curriculum.spec.ActivitySpec
import org.tribetalk.curriculum.spec.ActivityType
import org.tribetalk.fln.model.FlnCard
import org.tribetalk.fln.model.FlnCategory
import org.tribetalk.fln.model.FlnDomain
import org.tribetalk.fln.pipeline.SvgCorpusRegistry

/**
 * Generates a structured, lesson-aligned 6-card pedagogical progression
 * from a single ActivitySpec. Guarantees thematic and curriculum cohesion.
 */
object FlashcardSetGenerator {

    fun generateSet(spec: ActivitySpec, context: TeachingContext): List<FlnCard> {
        val primaryItem = spec.items.firstOrNull() ?: return emptyList()
        val assetKey = primaryItem.visualAsset
        val count = primaryItem.quantity
        val meta = SvgCorpusRegistry.get(assetKey)
        val assetPath = meta?.relativeFilePath ?: "fln_svg_corpus/plants/mango.svg"

        val hiWord = spec.languageSpec.primaryWord.ifBlank { meta?.hindiName ?: "वस्तु" }
        val satWord = spec.languageSpec.targetWord.ifBlank { meta?.santaliName ?: "ᱡᱤᱱᱤᱥ" }
        val engWord = spec.languageSpec.englishWord.ifBlank { meta?.englishName ?: "Object" }

        val cards = mutableListOf<FlnCard>()

        // Card 1: Introduction & Thematic Concept
        cards.add(
            FlnCard(
                id = "${spec.id}_card_1_intro",
                domain = FlnDomain.NUMERACY_COUNTING,
                category = FlnCategory.NUMBERS,
                santaliOlChiki = satWord,
                hindiText = hiWord,
                englishGloss = engWord,
                teacherPhoneticGuide = spec.languageSpec.phoneticGuide,
                santaliDevanagariPhonetic = spec.languageSpec.phoneticGuide,
                imageAssetPath = assetPath,
                vectorIconType = assetKey,
                exemplarWordSantali = spec.languageSpec.phraseSantali,
                exemplarWordHindi = spec.languageSpec.phraseHindi,
                numeralValue = count,
                countingQuantity = 1,
                countingTokenNameSantali = satWord,
                countingTokenNameHindi = hiWord,
                nipunCode = spec.objectiveId,
                grade = context.grade,
                phonicsClassification = "Step 1: Introduction"
            )
        )

        // Card 2: Concrete Object Counting (Ten-Frame/Grid)
        cards.add(
            FlnCard(
                id = "${spec.id}_card_2_count",
                domain = FlnDomain.NUMERACY_COUNTING,
                category = FlnCategory.NUMBERS,
                santaliOlChiki = "$satWord ᱞᱮᱠᱷᱟ ($count)",
                hindiText = "$hiWord की गिनती ($count)",
                englishGloss = "Count $count ${engWord}s",
                teacherPhoneticGuide = "$count $engWord",
                santaliDevanagariPhonetic = "$count $satWord",
                imageAssetPath = assetPath,
                vectorIconType = assetKey,
                exemplarWordSantali = "$count $satWord ᱢᱮᱱᱟᱜᱼᱟ",
                exemplarWordHindi = "$count $hiWord हैं",
                numeralValue = count,
                countingQuantity = count,
                countingTokenNameSantali = satWord,
                countingTokenNameHindi = hiWord,
                nipunCode = spec.objectiveId,
                grade = context.grade,
                fingerTracingGuide = "Tap and count each $engWord",
                phonicsClassification = "Step 2: Concrete Counting"
            )
        )

        // Card 3: Numeral Matching & Tactile Recognition
        cards.add(
            FlnCard(
                id = "${spec.id}_card_3_match",
                domain = FlnDomain.NUMERACY_COUNTING,
                category = FlnCategory.NUMBERS,
                santaliOlChiki = "ᱮᱞ: $count",
                hindiText = "संख्या: $count",
                englishGloss = "Number $count",
                teacherPhoneticGuide = "$count",
                santaliDevanagariPhonetic = "$count",
                imageAssetPath = assetPath,
                vectorIconType = assetKey,
                exemplarWordSantali = "$satWord - $count",
                exemplarWordHindi = "$hiWord - $count",
                numeralValue = count,
                countingQuantity = count,
                countingTokenNameSantali = satWord,
                countingTokenNameHindi = hiWord,
                nipunCode = spec.objectiveId,
                grade = context.grade,
                fingerTracingGuide = "Trace the numeral $count",
                phonicsClassification = "Step 3: Numeral Matching"
            )
        )

        // Card 4: Quantity Comparison
        val compCount = if (count > 2) count - 1 else count + 2
        cards.add(
            FlnCard(
                id = "${spec.id}_card_4_compare",
                domain = FlnDomain.NUMERACY_COUNTING,
                category = FlnCategory.NUMBERS,
                santaliOlChiki = "ᱠᱚᱢ / ᱰᱷᱮᱨ ᱛᱩᱞᱟᱹ",
                hindiText = "कम और अधिक तुलना",
                englishGloss = "Compare ($count vs $compCount)",
                teacherPhoneticGuide = if (count > compCount) "More ($count)" else "Less ($count)",
                santaliDevanagariPhonetic = if (count > compCount) "ᱰᱷᱮᱨ" else "ᱠᱚᱢ",
                imageAssetPath = assetPath,
                vectorIconType = assetKey,
                exemplarWordSantali = "$count $satWord ᱫᱚ $compCount ᱠᱷᱚᱱ ᱰᱷᱮᱨᱟ",
                exemplarWordHindi = "$count $hiWord, $compCount से अधिक हैं",
                numeralValue = count,
                countingQuantity = count,
                countingTokenNameSantali = satWord,
                countingTokenNameHindi = hiWord,
                nipunCode = spec.objectiveId,
                grade = context.grade,
                phonicsClassification = "Step 4: Comparison"
            )
        )

        // Card 5: Story-Based Contextual Counting
        cards.add(
            FlnCard(
                id = "${spec.id}_card_5_story",
                domain = FlnDomain.NUMERACY_COUNTING,
                category = FlnCategory.NUMBERS,
                santaliOlChiki = "ᱠᱟᱹᱦᱱᱤ ᱞᱮᱠᱷᱟ",
                hindiText = "कहानी में गिनती",
                englishGloss = "Story Counting",
                teacherPhoneticGuide = "$satWord kahni",
                santaliDevanagariPhonetic = "ᱜᱤᱫᱽᱨᱟᱹ ᱴᱷᱮᱱ $count $satWord ᱢᱮᱱᱟᱜᱼᱟ",
                imageAssetPath = assetPath,
                vectorIconType = assetKey,
                exemplarWordSantali = "ᱜᱤᱫᱽᱨᱟᱹ ᱴᱷᱮᱱ $count $satWord ᱢᱮᱱᱟᱜᱼᱟ᱾",
                exemplarWordHindi = "बच्चे के पास $count $hiWord हैं।",
                numeralValue = count,
                countingQuantity = count,
                countingTokenNameSantali = satWord,
                countingTokenNameHindi = hiWord,
                nipunCode = spec.objectiveId,
                grade = context.grade,
                exampleSentenceSantali = "ᱜᱤᱫᱽᱨᱟᱹ ᱴᱷᱮᱱ $count $satWord ᱢᱮᱱᱟᱜᱼᱟ᱾",
                exampleSentenceHindi = "बच्चे के पास $count $hiWord हैं।",
                phonicsClassification = "Step 5: Contextual Story"
            )
        )

        // Card 6: Recall & Quick Assessment
        cards.add(
            FlnCard(
                id = "${spec.id}_card_6_assessment",
                domain = FlnDomain.NUMERACY_COUNTING,
                category = FlnCategory.NUMBERS,
                santaliOlChiki = "ᱠᱩᱠᱞᱤ: ᱛᱤᱱᱟᱹᱜ $satWord?",
                hindiText = "प्रश्न: कितने $hiWord?",
                englishGloss = "How many ${engWord}s?",
                teacherPhoneticGuide = "tinag $satWord?",
                santaliDevanagariPhonetic = "ᱥᱟᱹᱦᱤ ᱛᱮᱞᱟ: $count",
                imageAssetPath = assetPath,
                vectorIconType = assetKey,
                exemplarWordSantali = "ᱥᱟᱹᱦᱤ ᱛᱮᱞᱟ ᱫᱚ $count ᱠᱟᱱᱟ",
                exemplarWordHindi = "सही उत्तर $count है",
                numeralValue = count,
                countingQuantity = count,
                countingTokenNameSantali = satWord,
                countingTokenNameHindi = hiWord,
                nipunCode = spec.objectiveId,
                grade = context.grade,
                phonicsClassification = "Step 6: Quick Check"
            )
        )

        return cards
    }
}
