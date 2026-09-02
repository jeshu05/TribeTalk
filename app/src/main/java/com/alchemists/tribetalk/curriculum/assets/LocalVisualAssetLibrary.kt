package com.alchemists.tribetalk.curriculum.assets

import com.alchemists.tribetalk.curriculum.models.VisualStimulus

object LocalVisualAssetLibrary {

    val fruits: List<VisualStimulus> = listOf(
        VisualStimulus(objectNameHindi = "सेब", objectNameSantali = "ᱥᱮᱣ", iconEmoji = "🍎"),
        VisualStimulus(objectNameHindi = "आम", objectNameSantali = "ᱩᱞ", iconEmoji = "🥭"),
        VisualStimulus(objectNameHindi = "केला", objectNameSantali = "ᱠᱟᱭᱨᱟ", iconEmoji = "🍌"),
        VisualStimulus(objectNameHindi = "संतरा", objectNameSantali = "ᱥᱚᱸᱛᱚᱨᱟ", iconEmoji = "🍊")
    )

    val vehicles: List<VisualStimulus> = listOf(
        VisualStimulus(objectNameHindi = "गाड़ी", objectNameSantali = "ᱜᱟᱹᱰᱤ", iconEmoji = "🚗"),
        VisualStimulus(objectNameHindi = "बस", objectNameSantali = "ᱵᱟᱥ", iconEmoji = "🚌"),
        VisualStimulus(objectNameHindi = "साइकिल", objectNameSantali = "ᱥᱟᱭᱠᱮᱞ", iconEmoji = "🚲")
    )

    val animals: List<VisualStimulus> = listOf(
        VisualStimulus(objectNameHindi = "चिड़िया", objectNameSantali = "ᱪᱮᱬᱮ", iconEmoji = "🐦"),
        VisualStimulus(objectNameHindi = "कुत्ता", objectNameSantali = "ᱥᱮᱛᱟ", iconEmoji = "🐶"),
        VisualStimulus(objectNameHindi = "बिल्ली", objectNameSantali = "ᱯᱩᱥᱤ", iconEmoji = "🐱"),
        VisualStimulus(objectNameHindi = "हाथी", objectNameSantali = "ᱦᱟᱹᱛᱤ", iconEmoji = "🐘")
    )

    val schoolObjects: List<VisualStimulus> = listOf(
        VisualStimulus(objectNameHindi = "किताब", objectNameSantali = "ᱯᱩᱛᱷᱤ", iconEmoji = "📖"),
        VisualStimulus(objectNameHindi = "पेंसिल", objectNameSantali = "ᱯᱮᱱᱥᱤᱞ", iconEmoji = "✏️"),
        VisualStimulus(objectNameHindi = "पानी", objectNameSantali = "ᱫᱟᱜ", iconEmoji = "💧"),
        VisualStimulus(objectNameHindi = "गेंद", objectNameSantali = "ᱜᱮᱸᱫᱽ", iconEmoji = "⚽")
    )

    val shapes: List<VisualStimulus> = listOf(
        VisualStimulus(objectNameHindi = "वृत्त (गोल)", objectNameSantali = "ᱜᱳᱞ", iconEmoji = "⭕"),
        VisualStimulus(objectNameHindi = "वर्ग (चौकोर)", objectNameSantali = "ᱪᱚᱠᱟ", iconEmoji = "⏹️"),
        VisualStimulus(objectNameHindi = "त्रिकोण", objectNameSantali = "ᱛᱮᱠᱳᱬ", iconEmoji = "🔺"),
        VisualStimulus(objectNameHindi = "तारा", objectNameSantali = "ᱤᱯᱤᱞ", iconEmoji = "⭐️")
    )

    val colours: List<VisualStimulus> = listOf(
        VisualStimulus(objectNameHindi = "लाल", objectNameSantali = "ᱟᱨᱟ", iconEmoji = "🔴"),
        VisualStimulus(objectNameHindi = "हरा", objectNameSantali = "ᱦᱟᱹᱨᱤYᱟᱹᱲ", iconEmoji = "🟢"),
        VisualStimulus(objectNameHindi = "नीला", objectNameSantali = "ᱞᱤᱞ", iconEmoji = "🔵"),
        VisualStimulus(objectNameHindi = "पीला", objectNameSantali = "ᱥᱟᱥᱟᱝ", iconEmoji = "🟡")
    )

    fun getRandomObject(seed: Long? = null): VisualStimulus {
        val allObjects = fruits + vehicles + animals + schoolObjects
        val index = if (seed != null) (seed % allObjects.size).toInt().let { if (it < 0) it + allObjects.size else it } else (allObjects.indices).random()
        return allObjects[index]
    }

    fun getRandomShape(seed: Long? = null): VisualStimulus {
        val index = if (seed != null) (seed % shapes.size).toInt().let { if (it < 0) it + shapes.size else it } else (shapes.indices).random()
        return shapes[index]
    }

    fun getRandomColour(seed: Long? = null): VisualStimulus {
        val index = if (seed != null) (seed % colours.size).toInt().let { if (it < 0) it + colours.size else it } else (colours.indices).random()
        return colours[index]
    }
}
