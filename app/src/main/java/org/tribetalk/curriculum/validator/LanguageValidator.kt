package org.tribetalk.curriculum.validator

import org.tribetalk.core.TribeTalkTranslator
import org.tribetalk.curriculum.spec.ActivitySpec
import org.tribetalk.curriculum.spec.ActivityType
import org.tribetalk.fln.pipeline.SvgCorpusRegistry

object LanguageValidator {

    fun validate(spec: ActivitySpec): ValidationStepResult {
        val issues = mutableListOf<String>()
        var repaired = spec

        val primaryAsset = repaired.visualSpec.primaryAssetKey
        val meta = SvgCorpusRegistry.get(primaryAsset)

        var lang = repaired.languageSpec
        val hiWord = if (lang.primaryWord.isNotBlank()) lang.primaryWord else (meta?.hindiName ?: "वस्तु")
        val satWord = if (lang.targetWord.isNotBlank()) lang.targetWord else (meta?.santaliName ?: "ᱡᱤᱱᱤᱥ")
        val engWord = if (lang.englishWord.isNotBlank()) lang.englishWord else (meta?.englishName ?: "Object")

        var hiInstr = lang.instructionHindi
        var satInstr = lang.instructionSantali

        if (hiInstr.isBlank()) {
            issues.add("Hindi instruction was blank. Auto-synthesized.")
            hiInstr = when (repaired.activityType) {
                ActivityType.COUNT, ActivityType.CIRCLE_CORRECT -> "$hiWord गिनें और सही संख्या चुनें:"
                ActivityType.COUNT_AND_MATCH -> "$hiWord गिनें और सही संख्या से मिलाएँ:"
                ActivityType.COMPARE_QUANTITIES -> "दोनों समूहों की तुलना करें (<, =, >):"
                ActivityType.SIMPLE_ADDITION -> "$hiWord जोड़ें और कुल संख्या लिखें:"
                else -> "गतिविधि को पूरा करें:"
            }
        }

        if (satInstr.isBlank()) {
            issues.add("Santali instruction was blank. Realized via translation pipeline.")
            satInstr = when (repaired.activityType) {
                ActivityType.COUNT, ActivityType.CIRCLE_CORRECT -> "$satWord ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱥᱟᱹᱦᱤ ᱮᱞ ᱵᱟᱪᱷᱟᱣ ᱢᱮ:"
                ActivityType.COUNT_AND_MATCH -> "$satWord ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱥᱟᱹᱦᱤ ᱮᱞ ᱥᱟᱶ ᱡᱚᱲᱟᱣ ᱢᱮ:"
                ActivityType.COMPARE_QUANTITIES -> "ᱵᱟᱱᱟᱨ ᱫᱚᱞ ᱛᱩᱞᱟᱹᱭ ᱢᱮ (<, =, >):"
                ActivityType.SIMPLE_ADDITION -> "$satWord ᱡᱚᱲ ᱠᱟᱛᱮ ᱢᱩᱴᱷ ᱮᱞ ᱚᱞ ᱢᱮ:"
                else -> "ᱠᱟᱹᱢᱤ ᱯᱩᱨᱟᱹᱣ ᱢᱮ:"
            }
        }

        val phonetic = if (lang.phoneticGuide.isNotBlank()) lang.phoneticGuide else TribeTalkTranslator.olChikiToSpeechPhonetics(satWord)

        lang = lang.copy(
            primaryWord = hiWord,
            targetWord = satWord,
            englishWord = engWord,
            instructionHindi = hiInstr,
            instructionSantali = satInstr,
            phoneticGuide = phonetic,
            phraseHindi = if (lang.phraseHindi.isNotBlank()) lang.phraseHindi else "मीठा $hiWord",
            phraseSantali = if (lang.phraseSantali.isNotBlank()) lang.phraseSantali else "$satWord ᱡᱚ",
            phraseEnglish = if (lang.phraseEnglish.isNotBlank()) lang.phraseEnglish else "Sweet $engWord"
        )

        repaired = repaired.copy(languageSpec = lang)

        return ValidationStepResult(
            isValid = issues.isEmpty(),
            issues = issues,
            repairedSpec = repaired
        )
    }
}
