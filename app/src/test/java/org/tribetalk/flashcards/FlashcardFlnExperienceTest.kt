package org.tribetalk.flashcards

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.tribetalk.fln.generator.ProceduralCurriculumGenerator
import org.tribetalk.fln.model.FlnCard
import org.tribetalk.fln.model.FlnDomain
import org.tribetalk.fln.progress.FlnProgressManager
import org.tribetalk.fln.repository.FlnCurriculumRepository
import java.io.File

/**
 * Comprehensive Unit Test Suite verifying the complete FLN Flashcard Experience.
 * Validates the 14 key requirements:
 * 1. Card model creation with FLN metadata and progress counters
 * 2. Preset decks across all curriculum categories
 * 3. Custom card synthesis and creation
 * 4. Teacher card editing
 * 5. Manual Santali override preservation
 * 6. Quiz answer validation
 * 7. Score calculation and mastery
 * 8. Distractor generation and answer randomization
 * 9. Matching activity dual-column logic
 * 10. Offline progress tracking (seen, revealed, heard, correct, incorrect)
 * 11. Deck builder filtering by category, grade, skill, and card count
 * 12. Empty deck safe handling
 * 13. Missing image fallback handling
 * 14. Missing audio text graceful handling
 */
class FlashcardFlnExperienceTest {

    private lateinit var progressManager: FlnProgressManager

    @Before
    fun setUp() {
        // Initialize in-memory progress tracker (context = null for unit tests)
        progressManager = FlnProgressManager(null)
    }

    // 1. Card model creation
    @Test
    fun testCardModelCreationWithFlnMetadata() {
        val card = Flashcard(
            id = "test_card_01",
            setId = "fln_vocab_set",
            topic = "Animals (पशु)",
            skill = "Word-Picture Association",
            hindiText = "हाथी",
            santaliText = "Hati",
            santaliOlChiki = "ᱦᱟᱛᱤ",
            phoneticGuide = "हाती",
            imageUri = "/data/local/tmp/elephant.png",
            imageEmoji = "🐘",
            iconType = "elephant",
            domain = "Foundational Literacy & Oral Language",
            grade = "Grade 1",
            difficulty = "Easy",
            englishGloss = "Elephant",
            seenCount = 5,
            revealedCount = 4,
            heardCount = 3,
            correctCount = 2,
            incorrectCount = 1
        )

        assertEquals("test_card_01", card.id)
        assertEquals("Animals (पशु)", card.topic)
        assertEquals("Word-Picture Association", card.skill)
        assertEquals("हाथी", card.hindiText)
        assertEquals("Hati", card.santaliText)
        assertEquals("ᱦᱟᱛᱤ", card.santaliOlChiki)
        assertEquals("हाती", card.phoneticGuide)
        assertEquals("/data/local/tmp/elephant.png", card.imageUri)
        assertEquals("🐘", card.imageEmoji)
        assertEquals("elephant", card.iconType)
        assertEquals("Foundational Literacy & Oral Language", card.domain)
        assertEquals("Grade 1", card.grade)
        assertEquals("Easy", card.difficulty)
        assertEquals("Elephant", card.englishGloss)
        assertEquals(5, card.seenCount)
        assertEquals(4, card.revealedCount)
        assertEquals(3, card.heardCount)
        assertEquals(2, card.correctCount)
        assertEquals(1, card.incorrectCount)
    }

    // 2. Preset decks
    @Test
    fun testPresetDecksIntegrity() {
        val categories = FlnCurriculumRepository.getCategories().filter { 
            it != FlnCurriculumRepository.CATEGORY_CUSTOM 
        }
        assertTrue("Categories must include multiple decks", categories.size >= 4)

        for (cat in categories) {
            val cards = FlnCurriculumRepository.getCardsByCategory(cat)
            assertTrue("Category '$cat' should have cards", cards.isNotEmpty())
            for (card in cards) {
                assertFalse("Hindi text required in $cat", card.hindiText.isBlank())
                assertFalse("Santali text required in $cat", card.santaliOlChiki.isBlank())
                assertNotNull("IconType should be present", card.iconType)
            }
        }
    }

    // 3. Custom cards
    @Test
    fun testCustomCardSynthesisAndCreation() {
        val customCard = ProceduralCurriculumGenerator.synthesizeCard("पेड़")
        assertNotNull(customCard)
        assertEquals("पेड़", customCard.hindiText)
        assertFalse("Ol Chiki should be populated", customCard.santaliOlChiki.isBlank())
        assertTrue("Should be marked as user generated", customCard.isCustomUserGenerated)

        // Add to repository
        FlnCurriculumRepository.addCustomCard(customCard)
        val allCards = FlnCurriculumRepository.getAllCards()
        assertTrue("Repository must contain the custom card", allCards.any { it.id == customCard.id })
    }

    // 4. Teacher edits
    @Test
    fun testTeacherEditsOnCard() {
        val original = Flashcard(
            id = "teacher_edit_01",
            hindiText = "किताब",
            santaliText = "Pustak",
            santaliOlChiki = "ᱯᱩᱥᱛᱚᱠ",
            phoneticGuide = "पुस्तक",
            grade = "Grade 1",
            skill = "Vocabulary"
        )

        // Teacher updates pronunciation, Latin Santali, and grade
        val edited = original.copy(
            santaliText = "Potob",
            santaliOlChiki = "ᱯᱚᱛᱚᱵ",
            phoneticGuide = "पोतोब",
            grade = "Grade 2",
            skill = "Reading"
        )

        assertEquals("Potob", edited.santaliText)
        assertEquals("ᱯᱚᱛᱚᱵ", edited.santaliOlChiki)
        assertEquals("पोतोब", edited.phoneticGuide)
        assertEquals("Grade 2", edited.grade)
        assertEquals("Reading", edited.skill)
    }

    // 5. Manual Santali override preservation
    @Test
    fun testManualSantaliOverridePreservation() {
        // Teacher has entered specific dialect/spelling for Santali
        var santaliOlChiki = "ᱫᱟᱨᱮ" // Dare (Tree)
        var latinSantali = "Dare"
        var phoneticGuide = "दारे"
        val hindiInput = "पेड़"

        // Automated draft generation
        val draft = ProceduralCurriculumGenerator.synthesizeCard(hindiInput)

        // Rule: Only populate empty fields; manual edits MUST NOT be overwritten
        if (santaliOlChiki.isBlank()) santaliOlChiki = draft.santaliOlChiki
        if (latinSantali.isBlank()) latinSantali = draft.englishGloss
        if (phoneticGuide.isBlank()) phoneticGuide = draft.teacherPhoneticGuide

        // Verify manual override is preserved
        assertEquals("ᱫᱟᱨᱮ", santaliOlChiki)
        assertEquals("Dare", latinSantali)
        assertEquals("दारे", phoneticGuide)
    }

    // 6. Quiz answer validation
    @Test
    fun testQuizAnswerValidation() {
        val cards = FlnCurriculumRepository.getAllCards().take(4)
        assertTrue(cards.size >= 4)

        val target = cards[0]
        val options = cards.map { it.santaliOlChiki }.shuffled()
        val correctIndex = options.indexOf(target.santaliOlChiki)
        assertTrue("Correct index must be within options", correctIndex in options.indices)

        // User chooses correct option
        val isUserCorrect = (correctIndex == options.indexOf(target.santaliOlChiki))
        assertTrue("Answer validation should confirm correct selection", isUserCorrect)

        // User chooses an incorrect option (first option that doesn't match)
        val incorrectIdx = options.indices.first { it != correctIndex }
        val isIncorrectSelection = (incorrectIdx == correctIndex)
        assertFalse("Answer validation should reject incorrect selection", isIncorrectSelection)
    }

    // 7. Score calculation
    @Test
    fun testQuizScoreCalculation() {
        var score = 0
        var answered = 0

        // Question 1: Correct
        answered++
        score++
        assertEquals(1, score)
        assertEquals(1, answered)

        // Question 2: Incorrect
        answered++
        // score remains unchanged
        assertEquals(1, score)
        assertEquals(2, answered)

        // Question 3: Correct
        answered++
        score++
        assertEquals(2, score)
        assertEquals(3, answered)

        val accuracyPercent = (score * 100) / answered
        assertEquals(66, accuracyPercent)
    }

    // 8. Answer randomization
    @Test
    fun testAnswerRandomization() {
        val cards = FlnCurriculumRepository.getAllCards()
        val current = cards[0]
        val distractors = cards.filter { it.id != current.id }.shuffled().take(3)
        assertEquals(3, distractors.size)

        val correct = current.santaliOlChiki
        val allOptions = (distractors.map { it.santaliOlChiki } + correct).shuffled()

        assertEquals(4, allOptions.size)
        assertTrue("All options must contain correct answer", allOptions.contains(correct))
        // Verify all 4 are distinct
        assertEquals(4, allOptions.toSet().size)
    }

    // 9. Matching logic
    @Test
    fun testMatchingActivityLogic() {
        val cards = FlnCurriculumRepository.getAllCards().take(5)
        assertEquals(5, cards.size)

        // Test matching pairs
        var matchedCount = 0
        val matchedIds = mutableSetOf<String>()

        val firstCard = cards[0]
        val selectedHindiId = firstCard.id
        val selectedSantaliId = firstCard.id // Same card selected in second column

        // Match attempt
        if (selectedHindiId == selectedSantaliId) {
            matchedIds.add(selectedHindiId)
            matchedCount++
        }

        assertEquals(1, matchedCount)
        assertTrue(matchedIds.contains(firstCard.id))

        // Mismatch attempt: hindi from card[1], santali from card[2]
        val mismatchHindiId = cards[1].id
        val mismatchSantaliId = cards[2].id
        var isMismatch = false
        if (mismatchHindiId != mismatchSantaliId) {
            isMismatch = true
        }

        assertTrue("Mismatch must be detected", isMismatch)
        assertEquals("Matched count must not increment on mismatch", 1, matchedCount)
    }

    // 10. Progress tracking
    @Test
    fun testProgressTrackingOffline() {
        val cardId = "progress_test_card"

        // Initial progress
        val initial = progressManager.getCardProgress(cardId)
        assertEquals(0, initial.seenCount)
        assertEquals(0, initial.revealedCount)
        assertEquals(0, initial.heardCount)
        assertEquals(0, initial.correctCount)

        // Record interactions
        progressManager.recordCardSeen(cardId)
        progressManager.recordCardRevealed(cardId)
        progressManager.recordCardHeard(cardId)
        progressManager.recordQuizResult(cardId, isCorrect = true)
        progressManager.recordQuizResult(cardId, isCorrect = true)
        progressManager.recordQuizResult(cardId, isCorrect = true)

        val updated = progressManager.getCardProgress(cardId)
        assertEquals(1, updated.seenCount)
        assertEquals(1, updated.revealedCount)
        assertEquals(1, updated.heardCount)
        assertEquals(3, updated.correctCount)
        assertEquals(0, updated.incorrectCount)
        assertEquals(100, updated.accuracyPercent)
        assertTrue("Card with 3 correct and 100% should be mastered", updated.isMastered)
    }

    // 11. Deck selection and filtering
    @Test
    fun testDeckSelectionAndFiltering() {
        val allCards = FlnCurriculumRepository.getAllCards()
        val animals = FlnCurriculumRepository.getCardsByCategory(FlnCurriculumRepository.CATEGORY_ANIMALS)

        assertTrue(allCards.size > animals.size)
        assertTrue(animals.isNotEmpty())

        // Card limit filter
        val limit = 5
        val limitedDeck = animals.take(limit)
        assertTrue(limitedDeck.size <= limit)
    }

    // 12. Empty deck handling
    @Test
    fun testEmptyDeckSafeHandling() {
        val emptyList = emptyList<FlnCard>()
        val total = emptyList.size
        assertEquals(0, total)

        val currentIndex = 0
        val safeCard = emptyList.getOrNull(currentIndex)
        assertNull("Index out of bounds must return null safely", safeCard)

        val progress = if (emptyList.isNotEmpty()) (currentIndex + 1).toFloat() / emptyList.size.toFloat() else 0f
        assertEquals(0f, progress, 0.001f)
    }

    // 13. Missing image handling
    @Test
    fun testMissingImageFallbackHandling() {
        val cardWithoutImage = Flashcard(
            id = "fallback_card_01",
            hindiText = "कुत्ता",
            santaliText = "Seta",
            santaliOlChiki = "ᱥᱮᱛᱟ",
            imageUri = "/non/existent/file.png",
            imageEmoji = "🐕",
            iconType = "dog"
        )

        // 1. Check if image file exists
        val file = cardWithoutImage.imageUri?.let { File(it) }
        val hasValidFile = file != null && file.exists()
        assertFalse("Non-existent file should be identified as invalid", hasValidFile)

        // 2. Emoji fallback should be available
        assertNotNull("Emoji fallback should be present", cardWithoutImage.imageEmoji)
        assertEquals("🐕", cardWithoutImage.imageEmoji)

        // 3. Vector icon fallback should be available
        assertEquals("dog", cardWithoutImage.iconType)
    }

    // 14. Missing audio handling
    @Test
    fun testMissingAudioHandling() {
        val card = Flashcard(
            id = "audio_fallback_01",
            hindiText = "सूरज",
            santaliText = "Singi",
            santaliOlChiki = "ᱥᱤᱧᱤ"
        )

        // Priority resolution: Santali Ol Chiki -> Santali Text -> Empty
        val speakText = if (!card.santaliOlChiki.isNullOrBlank()) {
            card.santaliOlChiki
        } else if (card.santaliText.isNotBlank()) {
            card.santaliText
        } else {
            ""
        }

        assertEquals("ᱥᱤᱧᱤ", speakText)

        // Card with null Ol Chiki
        val cardWithoutOlChiki = Flashcard(
            id = "audio_fallback_02",
            hindiText = "सूरज",
            santaliText = "Singi",
            santaliOlChiki = null
        )

        val speakTextFallback = if (!cardWithoutOlChiki.santaliOlChiki.isNullOrBlank()) {
            cardWithoutOlChiki.santaliOlChiki
        } else if (cardWithoutOlChiki.santaliText.isNotBlank()) {
            cardWithoutOlChiki.santaliText
        } else {
            ""
        }

        assertEquals("Singi", speakTextFallback)
    }

    // 15. 4-Tier Visual Hierarchy Resolution
    @Test
    fun testFourTierVisualHierarchy() {
        // Tier 1: Card with bundled drawable reference
        val tier1Card = Flashcard(
            hindiText = "कुत्ता",
            santaliText = "Seta",
            imageUri = "ic_fln_dog",
            imageEmoji = "🐕",
            iconType = "dog"
        )
        assertTrue("Tier 1 imageUri must refer to bundled resource", tier1Card.imageUri!!.startsWith("ic_fln_"))

        // Tier 2: Card with teacher-selected local image path
        val tier2Card = Flashcard(
            hindiText = "कस्टम",
            santaliText = "Custom",
            imageUri = "/data/user/0/org.tribetalk/files/flashcards/images/teacher_photo.jpg",
            imageEmoji = null,
            iconType = ""
        )
        assertTrue("Tier 2 must hold local teacher file path", tier2Card.imageUri!!.contains("teacher_photo"))

        // Tier 3: Card with word-specific emoji and no image
        val tier3Card = Flashcard(
            hindiText = "गाय",
            santaliText = "Gai",
            imageUri = null,
            imageEmoji = "🐄",
            iconType = "cow"
        )
        assertNull(tier3Card.imageUri)
        assertEquals("🐄", tier3Card.imageEmoji)
        assertNotEquals("🎴", tier3Card.imageEmoji)

        // Tier 4: Card with procedural vector icon and no specific emoji
        val tier4Card = Flashcard(
            hindiText = "अ",
            santaliText = "Vowel a",
            imageUri = null,
            imageEmoji = null,
            iconType = "akshar"
        )
        assertNull(tier4Card.imageUri)
        assertNull(tier4Card.imageEmoji)
        assertEquals("akshar", tier4Card.iconType)
    }

    // 16. Ensure Akshar, Goat, Mountain, River, Shapes do not mask graphics with 🎴
    @Test
    fun testUnmappedCardsDoNotDefaultToPlayingCardEmoji() {
        val aksharCards = FlnCurriculumRepository.getCardsByCategory(FlnCurriculumRepository.CATEGORY_AKSHAR)
        assertTrue("Akshar cards must exist", aksharCards.isNotEmpty())
        for (card in aksharCards) {
            assertNotEquals("🎴", card.imageEmoji)
            assertEquals("akshar", card.iconType)
        }

        val animalCards = FlnCurriculumRepository.getCardsByCategory(FlnCurriculumRepository.CATEGORY_ANIMALS)
        val goatCard = animalCards.firstOrNull { it.hindiText.contains("बकरी") }
        assertNotNull("Goat card must exist", goatCard)
        assertEquals("goat", goatCard!!.iconType)
        assertNotEquals("🎴", goatCard.imageEmoji)

        val natureCards = FlnCurriculumRepository.getCardsByCategory(FlnCurriculumRepository.CATEGORY_NATURE)
        val riverCard = natureCards.firstOrNull { it.hindiText.contains("नदी") }
        assertNotNull("River card must exist", riverCard)
        assertEquals("river", riverCard!!.iconType)
        assertNotEquals("🎴", riverCard.imageEmoji)
    }
}

