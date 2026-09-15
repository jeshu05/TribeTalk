package org.tribetalk.fln

import org.junit.Assert.*
import org.junit.Test
import org.tribetalk.fln.model.*
import org.tribetalk.fln.repository.FlnCurriculumRepository
import org.tribetalk.fln.worksheet.WorksheetGenerator

class FlnCurriculumRepositoryTest {

    @Test
    fun testAllCardsAreValidAndWellFormed() {
        val cards = FlnCurriculumRepository.getAllCards()
        assertTrue("Curriculum should have at least 50 cards", cards.size >= 50)

        for (card in cards) {
            assertFalse("Card ID must not be blank", card.id.isBlank())
            assertFalse("Hindi text must not be blank", card.hindiText.isBlank())
            assertFalse("Santali Ol Chiki text must not be blank", card.santaliOlChiki.isBlank())
            assertFalse("Teacher phonetic guide must not be blank", card.teacherPhoneticGuide.isBlank())
            assertFalse("English gloss must not be blank", card.englishGloss.isBlank())
            assertFalse("NIPUN code must not be blank", card.nipunCode.isBlank())
        }
    }

    @Test
    fun testAll30AksharLettersExist() {
        val aksharCards = FlnCurriculumRepository.getCardsByCategory(FlnCategory.AKSHAR)
        assertEquals("There must be exactly 30 Ol Chiki letters", 30, aksharCards.size)

        val letters = aksharCards.map { it.santaliOlChiki }
        assertTrue("Must contain first letter ᱚ", letters.contains("ᱚ"))
        assertTrue("Must contain ᱛ", letters.contains("ᱛ"))
        assertTrue("Must contain ᱜ", letters.contains("ᱜ"))
        assertTrue("Must contain ᱝ", letters.contains("ᱝ"))
        assertTrue("Must contain ᱞ", letters.contains("ᱞ"))
        assertTrue("Must contain last modifier ᱷ", letters.contains("ᱷ"))

        for (card in aksharCards) {
            assertFalse("Akshar card must have fingerTracingGuide", card.fingerTracingGuide.isBlank())
            assertFalse("Akshar card must have exemplarWordSantali", card.exemplarWordSantali.isBlank())
        }
    }

    @Test
    fun testCategoryFiltering() {
        val categories = FlnCurriculumRepository.getFlashcardCategories()
        assertTrue("Categories list must include All Topics", categories.contains(FlnCategory.ALL))
        assertTrue("Categories must include Akshar", categories.contains(FlnCategory.AKSHAR))
        assertTrue("Categories must include Numbers", categories.contains(FlnCategory.NUMBERS))
        assertTrue("Categories must include Animals", categories.contains(FlnCategory.ANIMALS))
        assertTrue("Categories must include Fruits", categories.contains(FlnCategory.FRUITS))
        assertTrue("Categories must include Nature", categories.contains(FlnCategory.NATURE))

        val animalCards = FlnCurriculumRepository.getCardsByCategory(FlnCategory.ANIMALS)
        assertTrue("Animal cards must not be empty", animalCards.isNotEmpty())
        for (card in animalCards) {
            assertEquals(FlnCategory.ANIMALS, card.category)
        }

        val fruitCards = FlnCurriculumRepository.getCardsByCategory(FlnCategory.FRUITS)
        assertTrue("Fruit cards must not be empty", fruitCards.isNotEmpty())

        val natureCards = FlnCurriculumRepository.getCardsByCategory(FlnCategory.NATURE)
        assertTrue("Nature cards must not be empty", natureCards.isNotEmpty())

        val schoolCards = FlnCurriculumRepository.getCardsByCategory(FlnCategory.SCHOOL)
        assertTrue("School cards must not be empty", schoolCards.isNotEmpty())

        val spatialCards = FlnCurriculumRepository.getCardsByCategory(FlnCategory.SPATIAL)
        assertTrue("Spatial cards must not be empty", spatialCards.isNotEmpty())

        val arithmeticCards = FlnCurriculumRepository.getCardsByCategory(FlnCategory.ARITHMETIC)
        assertTrue("Arithmetic cards must not be empty", arithmeticCards.isNotEmpty())

        val moneyCards = FlnCurriculumRepository.getCardsByCategory(FlnCategory.MONEY)
        assertTrue("Money cards must not be empty", moneyCards.isNotEmpty())
    }

    @Test
    fun testAllCardsHaveValidSvgAssets() {
        val allCards = FlnCurriculumRepository.getAllCards()
        for (card in allCards) {
            assertNotNull("Card ${card.id} must have imageAssetPath", card.imageAssetPath)
            assertTrue("Card ${card.id} image must be an SVG file: ${card.imageAssetPath}", card.imageAssetPath!!.endsWith(".svg"))
            
            // Check file existence on disk
            val fileDirect = java.io.File("src/main/assets/${card.imageAssetPath}")
            val fileApp = java.io.File("app/src/main/assets/${card.imageAssetPath}")
            assertTrue("SVG file must exist for card ${card.id}: ${card.imageAssetPath}", fileDirect.exists() || fileApp.exists())
            assertFalse("Card ${card.id} vectorIconType must not be generic animal/fruit", card.vectorIconType in listOf("animal", "fruit", "nature"))
        }
    }

    @Test
    fun testNumeracyCardsHaveValues() {
        val numberCards = FlnCurriculumRepository.getCardsByCategory(FlnCategory.NUMBERS)
        assertEquals("There must be 20 number cards", 20, numberCards.size)
        for (card in numberCards) {
            assertEquals(FlnDomain.NUMERACY_COUNTING, card.domain)
            assertNotNull("Number card should have numeralValue", card.numeralValue)
            assertTrue("Numeral value must be between 1 and 20", card.numeralValue!! in 1..20)
        }
    }

    @Test
    fun testWorksheetGenerationCountAndMatch() {
        val config = WorksheetConfig(
            title = "Test Count & Match",
            type = WorksheetType.COUNT_AND_MATCH,
            grade = FlnGrade.GRADE_1,
            difficulty = WorksheetDifficulty.EASY,
            questionCount = 5
        )
        val items = WorksheetGenerator.generateWorksheet(config)
        assertEquals(5, items.size)
        for (item in items) {
            assertTrue("Quantity must be >= 1", item.quantity >= 1)
            assertFalse("Right Santali label must not be blank", item.rightLabelSantali.isBlank())
            assertFalse("Left Hindi label must not be blank", item.leftLabelHindi.isBlank())
            assertFalse("Teacher phonetic guide must not be blank", item.teacherPhoneticAnswer.isBlank())
        }
    }

    @Test
    fun testWorksheetGenerationPictureWordMatch() {
        val config = WorksheetConfig(
            title = "Test Picture Word",
            type = WorksheetType.PICTURE_WORD_MATCH,
            grade = FlnGrade.BALVATIKA,
            questionCount = 4
        )
        val items = WorksheetGenerator.generateWorksheet(config)
        assertEquals(4, items.size)
        for (item in items) {
            assertFalse("Left Hindi label must not be blank", item.leftLabelHindi.isBlank())
            assertFalse("Right Santali label must not be blank", item.rightLabelSantali.isBlank())
            assertFalse("Teacher phonetic guide must not be blank", item.teacherPhoneticAnswer.isBlank())
        }
    }

    @Test
    fun testWorksheetGenerationAdditionWordProblem() {
        val config = WorksheetConfig(
            title = "Test Addition Math",
            type = WorksheetType.ADDITION_WORD_PROBLEM,
            grade = FlnGrade.GRADE_1,
            difficulty = WorksheetDifficulty.MEDIUM,
            questionCount = 4
        )
        val items = WorksheetGenerator.generateWorksheet(config)
        assertEquals(4, items.size)
        for (item in items) {
            assertEquals("+", item.operationSign)
            assertTrue("Quantity 1 must be >= 1", item.quantity >= 1)
            assertTrue("Quantity 2 must be >= 1", item.secondaryQuantity >= 1)
            assertNotNull("Math answer must not be null", item.mathAnswer)
            assertEquals(item.quantity + item.secondaryQuantity, item.mathAnswer)
        }
    }

    @Test
    fun testWorksheetGenerationAksharTracing() {
        val config = WorksheetConfig(
            title = "Test Letter Tracing",
            type = WorksheetType.AKSHAR_TRACING,
            grade = FlnGrade.BALVATIKA,
            questionCount = 5
        )
        val items = WorksheetGenerator.generateWorksheet(config)
        assertEquals(5, items.size)
        for (item in items) {
            assertFalse("Left label must not be blank", item.leftLabelHindi.isBlank())
            assertFalse("Right label must not be blank", item.rightLabelSantali.isBlank())
            assertEquals("L-BAL.1", item.nipunCode)
        }
    }

    @Test
    fun testWorksheetGenerationTrain() {
        val config = WorksheetConfig(
            title = "Test Train Sequence",
            type = WorksheetType.NUMBER_SEQUENCE_TRAIN,
            grade = FlnGrade.GRADE_1,
            questionCount = 3
        )
        val items = WorksheetGenerator.generateWorksheet(config)
        assertEquals(3, items.size)
        for (item in items) {
            assertEquals(4, item.sequenceItems.size)
            assertTrue("Missing index must be in 0..3", item.missingSequenceIndex in 0..3)
        }
    }

    @Test
    fun testWorksheetGenerationMissingAkshar() {
        val config = WorksheetConfig(
            title = "Test Missing Akshar",
            type = WorksheetType.MISSING_AKSHAR_SPELLING,
            grade = FlnGrade.GRADE_2,
            questionCount = 4
        )
        val items = WorksheetGenerator.generateWorksheet(config)
        assertEquals(4, items.size)
        for (item in items) {
            assertNotNull("wordWithBlank must not be null", item.wordWithBlank)
            assertTrue("wordWithBlank must contain _", item.wordWithBlank!!.contains("_"))
            assertEquals(4, item.options.size)
            assertTrue("correctIndex must be in 0..3", item.correctIndex in 0..3)
        }
    }

    @Test
    fun testTopicConfigSynthesis() {
        val configMath = WorksheetGenerator.createConfigFromTopic("हाट बाज़ार में गिनती")
        assertEquals(WorksheetType.COUNT_AND_MATCH, configMath.type)

        val configAdd = WorksheetGenerator.createConfigFromTopic("संख्या जोड़")
        assertEquals(WorksheetType.ADDITION_WORD_PROBLEM, configAdd.type)

        val configTracing = WorksheetGenerator.createConfigFromTopic("अक्षर लेखन")
        assertEquals(WorksheetType.AKSHAR_TRACING, configTracing.type)

        val configVocab = WorksheetGenerator.createConfigFromTopic("जंगल के जानवर")
        assertEquals(WorksheetType.PICTURE_WORD_MATCH, configVocab.type)
    }
}
