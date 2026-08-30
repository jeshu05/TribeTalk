package com.alchemists.tribetalk.curriculum

/**
 * Development-time seed/import mechanism for the NIPUN/FLN curriculum.
 *
 * IMPORTANT: No official NIPUN/FLN source files are present in this project.
 * Per project rules this seeder populates only a small [DEMO DATA] dataset for
 * testing, and every record is marked `isOfficial = false`. It must NOT be treated
 * as approved NIPUN curriculum.
 *
 * When approved JSON/CSV curriculum sources are supplied, convert them into
 * `app/src/main/assets/database/nipun.db` and bundle it in the APK
 * (Room's createFromAsset then takes precedence over this seeder automatically).
 */
class DemoDataSeeder {

    suspend fun seed(dao: NipunDao) {
        seedClassrooms(dao)
        if (dao.domainCount() > 0) return

        // ---------- Domains ----------
        val literacyId = dao.insertDomain(Domain(
            name = "Foundational Literacy", kind = "LITERACY",
            description = "Oral language, phonological awareness, reading, writing, stories, poems & rhymes.",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))
        val numeracyId = dao.insertDomain(Domain(
            name = "Foundational Numeracy", kind = "NUMERACY",
            description = "Pre-number concepts, counting, numbers, operations, patterns, shapes, measurement, data.",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 2))

        // ---------- Competencies ----------
        val vocabId = dao.insertCompetency(Competency(
            domainId = literacyId, grade = 1, name = "Vocabulary",
            description = "Build and use vocabulary in context.", isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))
        val speechId = dao.insertCompetency(Competency(
            domainId = literacyId, grade = 1, name = "Speaking & Listening",
            description = "Speak clearly and listen with understanding.", isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 2))
        val readingId = dao.insertCompetency(Competency(
            domainId = literacyId, grade = 2, name = "Reading with Understanding",
            description = "Read words, sentences and short texts fluently.", isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 3))
        val writingId = dao.insertCompetency(Competency(
            domainId = literacyId, grade = 1, name = "Writing",
            description = "Write letters, words and simple sentences legibly.", isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 4))
        val countId = dao.insertCompetency(Competency(
            domainId = numeracyId, grade = 1, name = "Counting & Numbers",
            description = "Count objects and recognise numbers.", isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))
        val opsId = dao.insertCompetency(Competency(
            domainId = numeracyId, grade = 2, name = "Addition & Subtraction",
            description = "Add and subtract within the grade range.", isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 2))
        val shapeId = dao.insertCompetency(Competency(
            domainId = numeracyId, grade = 1, name = "Shapes & Patterns",
            description = "Recognise shapes and extend simple patterns.", isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 3))

        // ---------- Learning Outcomes (DEMO, not official NIPUN codes) ----------
        val loVocab = dao.insertLearningOutcome(LearningOutcome(
            competencyId = vocabId, domainId = literacyId, grade = 1, code = "DEMO-LIT-VOC-1",
            title = "Uses common Hindi words related to family, school and nature",
            description = "Says the Hindi name for common objects (किताब, कलम, घर, सूरज, पेड़).",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))
        val loSpeech = dao.insertLearningOutcome(LearningOutcome(
            competencyId = speechId, domainId = literacyId, grade = 1, code = "DEMO-LIT-SPEAK-1",
            title = "Recites short rhymes and answers simple questions",
            description = "Recites a short rhyme and answers simple who/what questions.",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 2))
        val loWrite = dao.insertLearningOutcome(LearningOutcome(
            competencyId = writingId, domainId = literacyId, grade = 1, code = "DEMO-LIT-WRITE-1",
            title = "Writes letters and simple words",
            description = "Writes vowel letters and two-letter words legibly.",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 3))
        val loCount = dao.insertLearningOutcome(LearningOutcome(
            competencyId = countId, domainId = numeracyId, grade = 1, code = "DEMO-NUM-COUNT-1",
            title = "Counts objects up to 10 and recognises numerals 1–10",
            description = "Counts up to 10 objects and points to numerals 1 to 10.",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))
        val loShape = dao.insertLearningOutcome(LearningOutcome(
            competencyId = shapeId, domainId = numeracyId, grade = 1, code = "DEMO-NUM-SHAPE-1",
            title = "Identifies basic 2D shapes and extends patterns",
            description = "Names circle, square, triangle and continues simple patterns.",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 2))
        val loRead = dao.insertLearningOutcome(LearningOutcome(
            competencyId = readingId, domainId = literacyId, grade = 2, code = "DEMO-LIT-READ-2",
            title = "Reads simple sentences and short stories",
            description = "Reads 3–4 word sentences and narrates what was read.",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))
        val loOps = dao.insertLearningOutcome(LearningOutcome(
            competencyId = opsId, domainId = numeracyId, grade = 2, code = "DEMO-NUM-OPS-2",
            title = "Adds and subtracts within 20 using objects",
            description = "Finds totals and differences for numbers up to 20.",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))

        // ---------- Lessons + Activities ----------
        val l1 = dao.insertLesson(Lesson(
            learningOutcomeId = loVocab, domainId = literacyId, grade = 1,
            title = "मेरा घर और विद्यालय (My Home and School)",
            content = "Introduce words: घर, स्कूल, किताब, कलम, कॉपी. Show pictures and say each word clearly.",
            story = "रानी स्कूल जाती है। उसके झोले में किताब और कलम है।",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))
        dao.insertActivity(Activity(
            lessonId = l1, learningOutcomeId = loVocab, domainId = literacyId, grade = 1,
            title = "Point and Say", instructions = "Teacher points to an object; children say its Hindi name.",
            type = "ORAL", isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))
        dao.insertActivity(Activity(
            lessonId = l1, learningOutcomeId = loVocab, domainId = literacyId, grade = 1,
            title = "Pictionary", instructions = "Children match picture cards to word cards.",
            type = "PICTURE", isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 2))

        val l2 = dao.insertLesson(Lesson(
            learningOutcomeId = loSpeech, domainId = literacyId, grade = 1,
            title = "लोरी (Rhyme)", content = "Recite a short rhyme with actions and repeat.",
            story = "चाँदा मामा दूर के, खिलौने लाए भर के...",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 2))
        dao.insertActivity(Activity(
            lessonId = l2, learningOutcomeId = loSpeech, domainId = literacyId, grade = 1,
            title = "Listen and Repeat", instructions = "Children echo each line and do the action.",
            type = "LISTENING", isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))

        val l3 = dao.insertLesson(Lesson(
            learningOutcomeId = loWrite, domainId = literacyId, grade = 1,
            title = "Vowel Letter Writing", content = "Practice tracing vowels अ, आ, इ, ई with correct strokes.",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 3))
        dao.insertActivity(Activity(
            lessonId = l3, learningOutcomeId = loWrite, domainId = literacyId, grade = 1,
            title = "Trace on Sand", instructions = "Children trace letters in a tray of sand.",
            type = "WRITING", isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))

        dao.insertLesson(Lesson(
            learningOutcomeId = loRead, domainId = literacyId, grade = 2,
            title = "पढ़ना: कहानी (Reading: Story)",
            content = "Read the story aloud, then ask what/who questions.",
            story = "एक कौआ प्यासा था। उसे घड़े में थोड़ा पानी मिला। उसने कंकड़ डाले और पानी पिया।",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))

        val n1 = dao.insertLesson(Lesson(
            learningOutcomeId = loCount, domainId = numeracyId, grade = 1,
            title = "गिनती 1 से 10 (Counting 1 to 10)",
            content = "Count objects using one-to-one correspondence and recognise numerals.",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))
        dao.insertActivity(Activity(
            lessonId = n1, learningOutcomeId = loCount, domainId = numeracyId, grade = 1,
            title = "Count the Beads", instructions = "Count beads one by one and place the correct number card.",
            type = "COUNTING", isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))

        val n2 = dao.insertLesson(Lesson(
            learningOutcomeId = loOps, domainId = numeracyId, grade = 2,
            title = "जोड़ और घटाव (Addition and Subtraction)",
            content = "Use objects to add and subtract within 20.",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))
        dao.insertActivity(Activity(
            lessonId = n2, learningOutcomeId = loOps, domainId = numeracyId, grade = 2,
            title = "Join and Take Away", instructions = "Combine two groups and then remove one group.",
            type = "MANIPULATIVE", isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))

        // ---------- Flashcards ----------
        seedFlashcard(dao, "किताब", "book", "WORD", 1, literacyId, loVocab, 1)
        seedFlashcard(dao, "कलम", "pen", "WORD", 1, literacyId, loVocab, 2)
        seedFlashcard(dao, "घर", "house", "PICTURE", 1, literacyId, loVocab, 3)
        seedFlashcard(dao, "सूरज", "sun", "PICTURE", 1, literacyId, loVocab, 4)
        seedFlashcard(dao, "अ", "a", "LETTER", 1, literacyId, loWrite, 5)
        seedFlashcard(dao, "1", "one", "NUMBER", 1, numeracyId, loCount, 1)
        seedFlashcard(dao, "5", "five", "NUMBER", 1, numeracyId, loCount, 2)
        seedFlashcard(dao, "Circle", "वृत्त", "SHAPE", 1, numeracyId, loShape, 3)
        seedFlashcard(dao, "Square", "वर्ग", "SHAPE", 1, numeracyId, loShape, 4)

        // ---------- Worksheets ----------
        dao.insertWorksheet(Worksheet(
            learningOutcomeId = loCount, domainId = numeracyId, grade = 1,
            title = "Count the objects", questionType = "COUNTING",
            content = "[{\"q\":\"Count and write the number of stars\",\"a\":\"5\"},{\"q\":\"Count and write the number of circles\",\"a\":\"3\"}]",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))
        dao.insertWorksheet(Worksheet(
            learningOutcomeId = loOps, domainId = numeracyId, grade = 2,
            title = "Add and subtract within 20", questionType = "MCQ",
            content = "[{\"q\":\"2 + 3 = ?\",\"options\":[\"3\",\"5\",\"6\"],\"a\":\"5\"},{\"q\":\"8 - 3 = ?\",\"options\":[\"5\",\"6\",\"4\"],\"a\":\"5\"}]",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 2))
        dao.insertWorksheet(Worksheet(
            learningOutcomeId = loShape, domainId = numeracyId, grade = 1,
            title = "Match the shapes", questionType = "MATCHING",
            content = "[{\"q\":\"Match circle to its name\",\"a\":\"वृत्त\"},{\"q\":\"Match square to its name\",\"a\":\"वर्ग\"}]",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 3))

        // ---------- Assessments ----------
        dao.insertAssessment(Assessment(
            learningOutcomeId = loCount, domainId = numeracyId, grade = 1,
            title = "Counting Check", instruction = "Show the child 5 objects; ask them to count aloud.",
            questionType = "ORAL", prompt = "How many objects are there?", answerKey = "5",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))
        dao.insertAssessment(Assessment(
            learningOutcomeId = loOps, domainId = numeracyId, grade = 2,
            title = "Addition Quick Check", instruction = "Ask the child to solve orally with objects.",
            questionType = "ORAL", prompt = "3 + 4 = ?", answerKey = "7",
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = 1))
    }

    private suspend fun seedFlashcard(
        dao: NipunDao, front: String, back: String, type: String,
        grade: Int, domainId: Long, loId: Long, order: Int
    ) {
        dao.insertFlashcard(Flashcard(
            learningOutcomeId = loId, domainId = domainId, grade = grade,
            cardType = type, front = front, back = back,
            imagePath = null, audioPath = null,
            isOfficial = false, sourceRef = "DEMO DATA", sortOrder = order))
    }

    private suspend fun seedClassrooms(dao: NipunDao) {
        if (dao.classroomByGrade(0) != null) return
        listOf(
            0 to "Balvatika / Preparatory",
            1 to "Class 1",
            2 to "Class 2",
            3 to "Class 3"
        ).forEach { (grade, label) ->
            dao.insertClassroom(Classroom(grade = grade, label = label))
        }
    }
}
