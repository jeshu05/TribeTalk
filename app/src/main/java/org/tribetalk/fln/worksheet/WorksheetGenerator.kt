package org.tribetalk.fln.worksheet

import org.tribetalk.curriculum.spec.ActivitySpec
import org.tribetalk.fln.model.*
import org.tribetalk.fln.pipeline.ActivityIR
import org.tribetalk.fln.pipeline.SceneComposer
import org.tribetalk.fln.pipeline.SvgCorpusRegistry
import org.tribetalk.fln.repository.FlnCurriculumRepository
import java.util.Random

/**
 * Clean, deterministic procedural engine for synthesizing NIPUN Bharat bilingual worksheets.
 * Runs 100% offline in < 10ms with zero network calls.
 */
object WorksheetGenerator {

    /**
     * Converts a universal ActivitySpec into printable WorksheetItems.
     */
    fun generateFromActivitySpec(spec: org.tribetalk.curriculum.spec.ActivitySpec, config: WorksheetConfig): List<WorksheetItem> {
        val primaryItem = spec.items.firstOrNull() ?: return generateWorksheet(config)
        val meta = SvgCorpusRegistry.get(spec.visualSpec.primaryAssetKey)
        val assetPath = meta?.relativeFilePath ?: "fln_svg_corpus/plants/mango.svg"

        if (spec.items.size >= 2) {
            return spec.items.mapIndexed { idx, item ->
                val itmMeta = SvgCorpusRegistry.get(item.visualAsset)
                val itmPath = itmMeta?.relativeFilePath ?: assetPath
                val hiName = itmMeta?.hindiName ?: spec.languageSpec.primaryWord
                val satName = itmMeta?.santaliName ?: spec.languageSpec.targetWord
                val olQty = FlnCurriculumRepository.toOlChikiDigits(item.quantity)

                WorksheetItem(
                    id = "${spec.id}_q_${idx + 1}",
                    promptHindi = spec.languageSpec.instructionHindi.ifBlank { "चित्र गिनें और सही संख्या लिखें:" },
                    promptSantali = spec.languageSpec.instructionSantali.ifBlank { "ᱪᱤᱛᱟᱹᱨ ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱥᱟᱹᱦᱤ ᱮᱞ ᱚᱞ ᱢᱮ:" },
                    iconType = item.visualAsset,
                    imageAssetPath = itmPath,
                    quantity = item.quantity,
                    secondaryQuantity = item.secondaryQuantity,
                    leftLabelHindi = item.correctAnswer,
                    rightLabelSantali = "$olQty ($satName)",
                    options = item.options,
                    correctIndex = item.options.indexOf(item.correctAnswer).coerceAtLeast(0),
                    mathAnswer = item.correctAnswer.toIntOrNull() ?: item.quantity,
                    teacherSolutionNote = "Answer: ${item.correctAnswer} ($hiName / $satName)",
                    teacherPhoneticAnswer = "$olQty ($satName)",
                    nipunCode = spec.objectiveId,
                    activityIR = ActivitySpec.toActivityIR(spec.copy(items = listOf(item)))
                )
            }
        }

        val count = config.questionCount.coerceIn(3, 8)
        val items = mutableListOf<WorksheetItem>()
        val random = Random(config.seed)

        when (config.type) {
            WorksheetType.ADDITION_WORD_PROBLEM -> {
                for (i in 0 until count) {
                    val q1 = if (i == 0) primaryItem.quantity.coerceIn(1, 5) else (1 + random.nextInt(5))
                    val q2 = (1 + random.nextInt(4))
                    val sum = q1 + q2
                    val ans = sum.toString()
                    val distractors = listOf((sum - 1).coerceAtLeast(1).toString(), (sum + 1).toString(), (sum + 2).toString(), ans).distinct().shuffled(random)
                    val olSum = FlnCurriculumRepository.toOlChikiDigits(sum)
                    val olQ1 = FlnCurriculumRepository.toOlChikiDigits(q1)
                    val olQ2 = FlnCurriculumRepository.toOlChikiDigits(q2)

                    items.add(
                        WorksheetItem(
                            id = "${spec.id}_add_${i + 1}",
                            promptHindi = "$q1 और $q2 मिलकर कितने होते हैं?",
                            promptSantali = "$olQ1 ᱟᱨ $olQ2 ᱢᱮᱥᱟ ᱠᱟᱛᱮ ᱛᱤᱱᱟᱹᱜ ᱦᱩᱭᱩᱜᱼᱟ?",
                            iconType = spec.visualSpec.primaryAssetKey,
                            imageAssetPath = assetPath,
                            quantity = q1,
                            secondaryQuantity = q2,
                            operationSign = "+",
                            leftLabelHindi = "$q1 + $q2",
                            rightLabelSantali = "$olQ1 + $olQ2 = $olSum",
                            options = distractors,
                            correctIndex = distractors.indexOf(ans).coerceAtLeast(0),
                            mathAnswer = sum,
                            teacherSolutionNote = "$q1 + $q2 = $sum [Ol Chiki: $olQ1 + $olQ2 = $olSum]",
                            teacherPhoneticAnswer = "$olSum ($sum)",
                            nipunCode = spec.objectiveId,
                            activityIR = ActivitySpec.toActivityIR(spec.copy(activityType = org.tribetalk.curriculum.spec.ActivityType.SIMPLE_ADDITION, items = listOf(primaryItem.copy(quantity = q1, secondaryQuantity = q2, correctAnswer = ans, options = distractors))))
                        )
                    )
                }
            }
            WorksheetType.SUBTRACTION_PROBLEM -> {
                for (i in 0 until count) {
                    val total = if (i == 0) maxOf(primaryItem.quantity, 3).coerceIn(3, 10) else (3 + random.nextInt(6))
                    val remove = 1 + random.nextInt(total - 1)
                    val diff = total - remove
                    val ans = diff.toString()
                    val distractors = listOf((diff - 1).coerceAtLeast(1).toString(), (diff + 1).toString(), (diff + 2).toString(), ans).distinct().shuffled(random)
                    val olTotal = FlnCurriculumRepository.toOlChikiDigits(total)
                    val olRemove = FlnCurriculumRepository.toOlChikiDigits(remove)
                    val olDiff = FlnCurriculumRepository.toOlChikiDigits(diff)

                    items.add(
                        WorksheetItem(
                            id = "${spec.id}_sub_${i + 1}",
                            promptHindi = "$total में से $remove घटाने पर कितने बचते हैं?",
                            promptSantali = "$olTotal ᱠᱷᱚᱱ $olRemove ᱵᱷᱮᱜᱟᱨ ᱞᱮᱠᱷᱟᱱ ᱛᱤᱱᱟᱹᱜ ᱥᱟᱨᱮᱡᱚᱜᱼᱟ?",
                            iconType = spec.visualSpec.primaryAssetKey,
                            imageAssetPath = assetPath,
                            quantity = total,
                            secondaryQuantity = remove,
                            operationSign = "-",
                            leftLabelHindi = "$total - $remove",
                            rightLabelSantali = "$olTotal - $olRemove = $olDiff",
                            options = distractors,
                            correctIndex = distractors.indexOf(ans).coerceAtLeast(0),
                            mathAnswer = diff,
                            teacherSolutionNote = "$total - $remove = $diff [Ol Chiki: $olTotal - $olRemove = $olDiff]",
                            teacherPhoneticAnswer = "$olDiff ($diff)",
                            nipunCode = spec.objectiveId,
                            activityIR = ActivitySpec.toActivityIR(spec.copy(items = listOf(primaryItem.copy(quantity = total, secondaryQuantity = remove, correctAnswer = ans, options = distractors))))
                        )
                    )
                }
            }
            WorksheetType.MULTIPLICATION_GROUPS -> {
                for (i in 0 until count) {
                    val groups = 2 + random.nextInt(3)
                    val perGroup = 2 + random.nextInt(3)
                    val prod = groups * perGroup
                    val ans = prod.toString()
                    val distractors = listOf((prod - perGroup).coerceAtLeast(2).toString(), (prod + groups).toString(), (prod + 2).toString(), ans).distinct().shuffled(random)
                    val olG = FlnCurriculumRepository.toOlChikiDigits(groups)
                    val olP = FlnCurriculumRepository.toOlChikiDigits(perGroup)
                    val olProd = FlnCurriculumRepository.toOlChikiDigits(prod)

                    items.add(
                        WorksheetItem(
                            id = "${spec.id}_mult_${i + 1}",
                            promptHindi = "$groups समूह हैं, प्रत्येक में $perGroup। कुल कितने हुए?",
                            promptSantali = "$olG ᱴᱤ ᱫᱚᱞ, ᱢᱤᱫ ᱢᱤᱫ ᱨᱮ $olP ᱴᱤ᱾ ᱢᱩᱴᱷ ᱛᱤᱱᱟᱹᱜ?",
                            iconType = spec.visualSpec.primaryAssetKey,
                            imageAssetPath = assetPath,
                            quantity = groups,
                            secondaryQuantity = perGroup,
                            operationSign = "×",
                            leftLabelHindi = "$groups × $perGroup",
                            rightLabelSantali = "$olG × $olP = $olProd",
                            options = distractors,
                            correctIndex = distractors.indexOf(ans).coerceAtLeast(0),
                            mathAnswer = prod,
                            teacherSolutionNote = "$groups × $perGroup = $prod [Ol Chiki: $olG × $olP = $olProd]",
                            teacherPhoneticAnswer = "$olProd ($prod)",
                            nipunCode = spec.objectiveId,
                            activityIR = ActivitySpec.toActivityIR(spec.copy(items = listOf(primaryItem.copy(quantity = groups, secondaryQuantity = perGroup, correctAnswer = ans, options = distractors))))
                        )
                    )
                }
            }
            else -> {
                for (i in 0 until count) {
                    val q = if (i == 0) primaryItem.quantity else (1 + random.nextInt(10))
                    val ans = q.toString()
                    val distractors = listOf((q - 1).coerceAtLeast(1).toString(), (q + 1).coerceAtMost(10).toString(), (q + 2).coerceAtMost(10).toString(), ans).distinct().shuffled(random)

                    items.add(
                        WorksheetItem(
                            id = "${spec.id}_q_${i + 1}",
                            promptHindi = spec.languageSpec.instructionHindi.ifBlank { "चित्र गिनें और सही संख्या लिखें:" },
                            promptSantali = spec.languageSpec.instructionSantali.ifBlank { "ᱪᱤᱛᱟᱹᱨ ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱥᱟᱹᱦᱤ ᱮᱞ ᱚᱞ ᱢᱮ:" },
                            iconType = spec.visualSpec.primaryAssetKey,
                            imageAssetPath = assetPath,
                            quantity = q,
                            secondaryQuantity = 0,
                            leftLabelHindi = ans,
                            rightLabelSantali = spec.languageSpec.targetWord,
                            options = distractors,
                            correctIndex = distractors.indexOf(ans).coerceAtLeast(0),
                            mathAnswer = q,
                            teacherSolutionNote = "Answer: $ans (${spec.languageSpec.primaryWord} / ${spec.languageSpec.targetWord})",
                            teacherPhoneticAnswer = spec.languageSpec.phoneticGuide,
                            nipunCode = spec.objectiveId,
                            activityIR = ActivitySpec.toActivityIR(spec.copy(items = listOf(primaryItem.copy(quantity = q, correctAnswer = ans, options = distractors))))
                        )
                    )
                }
            }
        }
        return items
    }

    /**
     * Converts a planned ActivityIR into a printable WorksheetItem.
     */
    fun activityIRToWorksheetItem(ir: ActivityIR): WorksheetItem {
        val meta = SvgCorpusRegistry.get(ir.primaryObjectKey)
        val assetPath = meta?.relativeFilePath ?: "fln_svg_corpus/plants/mango.svg"

        return WorksheetItem(
            id = ir.id,
            promptHindi = ir.instructionHindi.ifBlank { "चित्र देखकर सही उत्तर लिखें:" },
            promptSantali = ir.instructionSantali.ifBlank { "ᱪᱤᱛᱟᱹᱨ ᱧᱮᱞ ᱠᱟᱛᱮ ᱥᱟᱹᱦᱤ ᱛᱮᱞᱟ ᱚᱞ ᱢᱮ:" },
            iconType = ir.primaryObjectKey,
            imageAssetPath = assetPath,
            quantity = ir.quantity,
            secondaryQuantity = ir.secondaryQuantity,
            leftLabelHindi = ir.correctValue,
            rightLabelSantali = ir.santaliWord.ifBlank { ir.bilingualPhraseSantali },
            options = ir.distractorOptions,
            correctIndex = ir.distractorOptions.indexOf(ir.correctValue).coerceAtLeast(0),
            mathAnswer = ir.correctValue.toIntOrNull(),
            teacherSolutionNote = ir.teacherSolutionNote.ifBlank { "Answer: ${ir.correctValue}" },
            teacherPhoneticAnswer = ir.phonicsGuide,
            nipunCode = ir.nipunCompetencyCode,
            activityIR = ir
        )
    }

    /**
     * Generates a problem set for the given worksheet configuration.
     */
    fun generateWorksheet(config: WorksheetConfig): List<WorksheetItem> {
        val random = Random(config.seed)
        val count = config.questionCount.coerceIn(3, 8)

        return when (config.type) {
            WorksheetType.AKSHAR_TRACING -> generateAksharTracing(count, random)
            WorksheetType.COUNT_AND_MATCH -> generateCountAndMatch(count, config, random)
            WorksheetType.PICTURE_WORD_MATCH -> generatePictureWordMatch(count, config, random)
            WorksheetType.ADDITION_WORD_PROBLEM -> generateAdditionProblems(count, config, random)
            WorksheetType.NUMBER_SEQUENCE_TRAIN -> generateSequenceTrain(count, config, random)
            WorksheetType.MISSING_AKSHAR_SPELLING -> generateMissingAkshar(count, random)
            WorksheetType.SUBTRACTION_PROBLEM -> generateSubtractionProblems(count, config, random)
            WorksheetType.MULTIPLICATION_GROUPS -> generateMultiplicationGroups(count, config, random)
            WorksheetType.MONEY_COUNTING -> generateMoneyCounting(count, config, random)
        }
    }

    /**
     * Synthesizes an optimal worksheet configuration from a spoken teacher prompt or topic.
     */
    fun createConfigFromTopic(topic: String, grade: FlnGrade = FlnGrade.GRADE_1): WorksheetConfig {
        val clean = topic.trim().lowercase()

        val type = when {
            clean.contains("गिन") || clean.contains("संख्या") || clean.contains("जोड़") || clean.contains("math") || clean.contains("नंबर") -> {
                if (clean.contains("जोड़") || clean.contains("add")) {
                    WorksheetType.ADDITION_WORD_PROBLEM
                } else if (clean.contains("रेल") || clean.contains("क्रम") || clean.contains("sequence")) {
                    WorksheetType.NUMBER_SEQUENCE_TRAIN
                } else {
                    WorksheetType.COUNT_AND_MATCH
                }
            }
            clean.contains("अक्षर") || clean.contains("वर्ण") || clean.contains("लिख") || clean.contains("trace") || clean.contains("letter") -> {
                WorksheetType.AKSHAR_TRACING
            }
            clean.contains("खाली") || clean.contains("स्पेल") || clean.contains("वर्तनी") || clean.contains("spell") -> {
                WorksheetType.MISSING_AKSHAR_SPELLING
            }
            else -> WorksheetType.PICTURE_WORD_MATCH
        }

        return WorksheetConfig(
            title = "NIPUN $topic Lesson Worksheet",
            type = type,
            grade = grade,
            topicPrompt = topic,
            seed = System.currentTimeMillis()
        )
    }

    // -------------------------------------------------------------------------
    // Private Generators
    // -------------------------------------------------------------------------

    private fun generateAksharTracing(count: Int, random: Random): List<WorksheetItem> {
        val aksharCards = FlnCurriculumRepository.getCardsByCategory(FlnCategory.AKSHAR).shuffled(random)
        val selected = aksharCards.take(count)

        return selected.mapIndexed { index, card ->
            WorksheetItem(
                id = "trace_${card.id}_$index",
                promptHindi = "चित्र देखकर अक्षर लिखें और बोलें: ${card.exemplarWordHindi}",
                promptSantali = "ᱪᱤᱛᱟᱹᱨ ᱧᱮᱞ ᱠᱟᱛᱮ ᱪᱤᱠᱤ ᱚᱞ ᱢᱮ: ${card.exemplarWordSantali}",
                iconType = card.vectorIconType,
                imageAssetPath = card.imageAssetPath,
                leftLabelHindi = card.santaliOlChiki,
                rightLabelSantali = "${card.exemplarWordSantali} [ ${card.exemplarWordHindi} ]",
                teacherSolutionNote = "Letter: ${card.santaliOlChiki} (${card.hindiText}) • Exemplar: ${card.exemplarWordSantali}",
                teacherPhoneticAnswer = card.teacherPhoneticGuide,
                nipunCode = "L-BAL.1"
            )
        }
    }

    private fun generateCountAndMatch(count: Int, config: WorksheetConfig, random: Random): List<WorksheetItem> {
        val maxQty = when (config.grade) {
            FlnGrade.BALVATIKA -> 5
            FlnGrade.GRADE_1 -> 8
            else -> 12
        }

        val usedQuantities = mutableSetOf<Int>()
        val icons = listOf("apple", "fish", "star", "bird", "flower", "tree", "mango", "duck")

        return (1..count).map { idx ->
            var qty = random.nextInt(maxQty) + 1
            while (usedQuantities.contains(qty) && usedQuantities.size < maxQty) {
                qty = random.nextInt(maxQty) + 1
            }
            usedQuantities.add(qty)

            val olDigits = FlnCurriculumRepository.toOlChikiDigits(qty)
            val icon = icons[idx % icons.size]

            val opts = listOf(qty.toString(), (qty + 1).toString(), (qty - 1).coerceAtLeast(1).toString(), (qty + 2).toString()).distinct().shuffled()
            val correctIdx = opts.indexOf(qty.toString()).coerceAtLeast(0)

            val ir = ActivityIR(
                id = "count_ir_${idx}_$qty",
                nipunCompetencyCode = "N-G1.1",
                actionType = org.tribetalk.fln.pipeline.ActivityActionType.COUNT_AND_MATCH,
                grade = config.grade,
                primaryObjectKey = icon,
                quantity = qty,
                correctValue = qty.toString(),
                distractorOptions = opts,
                instructionHindi = "वस्तुओं को गिनें और सही संख्या से मिलाएँ:",
                instructionSantali = "ᱡᱤᱱᱤᱥ ᱠᱚ ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱴᱷᱤᱠ ᱮᱞ ᱥᱟᱶ ᱡᱚᱲᱟᱣ ᱢᱮ:"
            )

            WorksheetItem(
                id = "count_${idx}_$qty",
                promptHindi = "वस्तुओं को गिनें और सही संख्या से मिलाएँ:",
                promptSantali = "ᱡᱤᱱᱤᱥ ᱠᱚ ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱴᱷᱤᱠ ᱮᱞ ᱥᱟᱶ ᱡᱚᱲᱟᱣ ᱢᱮ:",
                quantity = qty,
                iconType = icon,
                imageAssetPath = SceneComposer.resolveAssetPath(icon),
                leftLabelHindi = "$qty",
                rightLabelSantali = "$olDigits",
                options = opts,
                correctIndex = correctIdx,
                mathAnswer = qty,
                teacherSolutionNote = "Quantity: $qty [Ol Chiki: $olDigits]",
                teacherPhoneticAnswer = "$olDigits ($qty)",
                nipunCode = "N-G1.1",
                activityIR = ir
            )
        }
    }

    private fun generatePictureWordMatch(count: Int, config: WorksheetConfig, random: Random): List<WorksheetItem> {
        val vocabCards = FlnCurriculumRepository.getCardsByDomain(FlnDomain.LITERACY_VOCABULARY).shuffled(random)
        val selected = vocabCards.take(count)

        return selected.mapIndexed { index, card ->
            val objKey = SvgCorpusRegistry.findMatchingKey(card.englishGloss)
                ?: SvgCorpusRegistry.findMatchingKey(card.santaliOlChiki)
                ?: "mango"

            val ir = ActivityIR(
                id = "match_ir_${card.id}_$index",
                nipunCompetencyCode = "L-G1.1",
                actionType = org.tribetalk.fln.pipeline.ActivityActionType.PICTURE_WORD_MATCH,
                grade = config.grade,
                primaryObjectKey = objKey,
                quantity = 1,
                correctValue = card.santaliOlChiki,
                santaliWord = card.santaliOlChiki,
                hindiWord = card.hindiText,
                englishWord = card.englishGloss,
                instructionHindi = "चित्र पहचानकर सही संताली (ओल चिकी) शब्द मिलाएँ:",
                instructionSantali = "ᱪᱤᱛᱟᱹᱨ ᱧᱮᱞ ᱠᱟᱛᱮ ᱴᱷᱤᱠ ᱟᱹᱲᱟᱹ ᱡᱚᱲᱟᱣ ᱢᱮ:"
            )

            WorksheetItem(
                id = "match_${card.id}_$index",
                promptHindi = "चित्र पहचानकर सही संताली (ओल चिकी) शब्द मिलाएँ:",
                promptSantali = "ᱪᱤᱛᱟᱹᱨ ᱧᱮᱞ ᱠᱟᱛᱮ ᱴᱷᱤᱠ ᱟᱹᱲᱟᱹ ᱡᱚᱲᱟᱣ ᱢᱮ:",
                imageAssetPath = SceneComposer.resolveAssetPath(objKey),
                iconType = objKey,
                leftLabelHindi = card.hindiText,
                rightLabelSantali = card.santaliOlChiki,
                teacherSolutionNote = "${card.hindiText} = ${card.santaliOlChiki} (${card.englishGloss})",
                teacherPhoneticAnswer = card.teacherPhoneticGuide,
                nipunCode = "L-G1.1",
                activityIR = ir
            )
        }
    }

    private fun generateAdditionProblems(count: Int, config: WorksheetConfig, random: Random): List<WorksheetItem> {
        val maxA = if (config.grade == FlnGrade.BALVATIKA) 4 else 6
        val maxB = if (config.grade == FlnGrade.BALVATIKA) 3 else 5
        val icons = listOf("apple", "fish", "star", "bird", "flower", "mango")

        return (1..count).map { idx ->
            val q1 = random.nextInt(maxA) + 1
            val q2 = random.nextInt(maxB) + 1
            val sum = q1 + q2
            val icon = icons[idx % icons.size]

            val olSum = FlnCurriculumRepository.toOlChikiDigits(sum)
            val olQ1 = FlnCurriculumRepository.toOlChikiDigits(q1)
            val olQ2 = FlnCurriculumRepository.toOlChikiDigits(q2)

            val ir = ActivityIR(
                id = "add_ir_${idx}_$sum",
                nipunCompetencyCode = "N-G1.2",
                actionType = org.tribetalk.fln.pipeline.ActivityActionType.ADDITION_CONCRETE,
                grade = config.grade,
                primaryObjectKey = icon,
                quantity = q1,
                secondaryQuantity = q2,
                correctValue = sum.toString(),
                distractorOptions = listOf(sum.toString(), (sum + 1).toString(), (sum - 1).coerceAtLeast(1).toString(), (sum + 2).toString()).distinct().shuffled(),
                instructionHindi = "हाट बाज़ार में $q1 वस्तुएं थीं, $q2 और मिलीं। कुल कितनी हुईं?",
                instructionSantali = "ᱦᱟᱴ ᱨᱮ $olQ1 ᱴᱤ ᱛᱟᱦᱮᱸ ᱠᱟᱱᱟ, $olQ2 ᱴᱤ ᱟᱨᱦᱚᱸ ᱧᱟᱢ ᱮᱱᱟ᱾ ᱡᱚᱛᱚ ᱛᱮ ᱛᱤᱱᱟᱹᱜ?"
            )

            WorksheetItem(
                id = "add_${idx}_$sum",
                promptHindi = "हाट बाज़ार में $q1 वस्तुएं थीं, $q2 और मिलीं। कुल कितनी हुईं?",
                promptSantali = "ᱦᱟᱴ ᱨᱮ $olQ1 ᱴᱤ ᱛᱟᱦᱮᱸ ᱠᱟᱱᱟ, $olQ2 ᱴᱤ ᱟᱨᱦᱚᱸ ᱧᱟᱢ ᱮᱱᱟ᱾ ᱡᱚᱛᱚ ᱛᱮ ᱛᱤᱱᱟᱹᱜ?",
                quantity = q1,
                secondaryQuantity = q2,
                operationSign = "+",
                mathAnswer = sum,
                iconType = icon,
                imageAssetPath = SceneComposer.resolveAssetPath(icon),
                leftLabelHindi = "$q1 + $q2",
                rightLabelSantali = "$olQ1 + $olQ2 = $olSum",
                teacherSolutionNote = "$q1 + $q2 = $sum [Ol Chiki: $olQ1 + $olQ2 = $olSum]",
                teacherPhoneticAnswer = "$olSum ($sum)",
                nipunCode = "N-G1.2",
                activityIR = ir
            )
        }
    }

    private fun generateSequenceTrain(count: Int, config: WorksheetConfig, random: Random): List<WorksheetItem> {
        return (1..count).map { idx ->
            val start = random.nextInt(12) + 1
            val length = 4
            val missingOffset = random.nextInt(length)

            val items = (0 until length).map { offset ->
                val num = start + offset
                FlnCurriculumRepository.toOlChikiDigits(num)
            }

            val missingVal = start + missingOffset
            val olMissing = FlnCurriculumRepository.toOlChikiDigits(missingVal)

            WorksheetItem(
                id = "seq_${idx}_$missingVal",
                promptHindi = "रेलगाड़ी के डिब्बों में छूटी हुई संख्या लिखें:",
                promptSantali = "ᱨᱮᱞᱜᱟᱹᱰᱤ ᱨᱮ ᱟᱫ ᱟᱠᱟᱱ ᱮᱞ ᱚᱞ ᱢᱮ:",
                sequenceItems = items,
                missingSequenceIndex = missingOffset,
                teacherSolutionNote = "Missing box index $missingOffset: $missingVal (Ol Chiki: $olMissing)",
                teacherPhoneticAnswer = "$olMissing ($missingVal)",
                nipunCode = "N-G1.1"
            )
        }
    }

    private fun generateMissingAkshar(count: Int, random: Random): List<WorksheetItem> {
        val candidates = listOf(
            Triple("ᱚᱲᱟᱜ", 0, "ᱚ"),   // House: _ᱲᱟᱜ
            Triple("ᱥᱮᱛᱟ", 2, "ᱛ"),   // Dog: ᱥᱮ_ᱟ
            Triple("ᱠᱟᱭᱨᱟ", 0, "ᱠ"),  // Banana: _ᱟᱭᱨᱟ
            Triple("ᱦᱟᱹᱛᱤ", 0, "ᱦ"),   // Elephant: _ᱟᱹᱛᱤ
            Triple("ᱫᱟᱨᱮ", 0, "ᱫ"),   // Tree: _ᱟᱨᱮ
            Triple("ᱵᱟᱦᱟ", 2, "ᱦ"),   // Flower: ᱵᱟ_ᱟ
            Triple("ᱯᱚᱛᱚᱵ", 0, "ᱯ"),  // Book: _ᱚᱛᱚᱵ
            Triple("ᱢᱟᱨᱟᱜ", 0, "ᱢ")   // Peacock: _ᱟᱨᱟᱜ
        ).shuffled(random)

        val aksharLetters = listOf("ᱚ", "ᱛ", "ᱜ", "ᱞ", "ᱟ", "ᱠ", "ᱡ", "ᱢ", "ᱥ", "ᱦ", "ᱩ", "ᱫ", "ᱯ", "ᱵ")

        return candidates.take(count).mapIndexed { idx, (word, blankPos, correctChar) ->
            val sb = StringBuilder()
            word.forEachIndexed { i, ch ->
                if (i == blankPos) sb.append("_") else sb.append(ch)
            }

            val distractors = aksharLetters.filter { it != correctChar }.shuffled(random).take(3)
            val options = (distractors + correctChar).shuffled(random)
            val correctIdx = options.indexOf(correctChar)

            WorksheetItem(
                id = "missing_${idx}_$correctChar",
                promptHindi = "सही अक्षर चुनकर शब्द पूरा करें: ${sb.toString()}",
                promptSantali = "ᱴᱷᱤᱠ ᱪᱤᱠᱤ ᱵᱟᱪᱷᱟᱣ ᱠᱟᱛᱮ ᱟᱹᱲᱟᱹ ᱯᱮᱨᱮᱡ ᱢᱮ: ${sb.toString()}",
                wordWithBlank = sb.toString(),
                options = options,
                correctIndex = correctIdx,
                teacherSolutionNote = "Missing letter is '$correctChar'. Completed word: $word",
                teacherPhoneticAnswer = correctChar,
                nipunCode = "L-G2.1"
            )
        }
    }

    private fun generateSubtractionProblems(count: Int, config: WorksheetConfig, random: Random): List<WorksheetItem> {
        val maxTotal = if (config.grade == FlnGrade.BALVATIKA) 6 else if (config.grade == FlnGrade.GRADE_1) 12 else 20
        val icons = listOf("mango", "apple", "fish", "bird", "flower", "leaf")

        return (1..count).map { idx ->
            val total = random.nextInt(maxTotal - 2) + 3 // at least 3
            val remove = random.nextInt(total - 1) + 1   // at least 1, less than total
            val diff = total - remove
            val icon = icons[idx % icons.size]

            val olTotal = FlnCurriculumRepository.toOlChikiDigits(total)
            val olRemove = FlnCurriculumRepository.toOlChikiDigits(remove)
            val olDiff = FlnCurriculumRepository.toOlChikiDigits(diff)

            val ir = ActivityIR(
                id = "sub_ir_${idx}_$diff",
                nipunCompetencyCode = "N-G1.2",
                actionType = org.tribetalk.fln.pipeline.ActivityActionType.SUBTRACTION_CONCRETE,
                grade = config.grade,
                primaryObjectKey = icon,
                quantity = total,
                secondaryQuantity = remove,
                correctValue = diff.toString(),
                distractorOptions = listOf(diff.toString(), (diff + 1).toString(), (diff - 1).coerceAtLeast(1).toString(), (diff + 2).toString()).distinct().shuffled(),
                instructionHindi = "पेड़ पर $total फल थे, $remove गिर गए। बचे हुए गिनें और लिखें:",
                instructionSantali = "ᱫᱟᱨᱮ ᱨᱮ $olTotal ᱴᱤ ᱡᱚ ᱛᱟᱦᱮᱸ ᱠᱟᱱᱟ, $olRemove ᱴᱤ ᱧᱩᱨ ᱮᱱᱟ᱾ ᱥᱟᱨᱮᱡ ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱚᱞ ᱢᱮ:"
            )

            WorksheetItem(
                id = "sub_${idx}_$diff",
                promptHindi = "पेड़ पर $total फल थे, $remove गिर गए। कितने बचे?",
                promptSantali = "ᱫᱟᱨᱮ ᱨᱮ $olTotal ᱴᱤ ᱡᱚ ᱛᱟᱦᱮᱸ ᱠᱟᱱᱟ, $olRemove ᱴᱤ ᱧᱩᱨ ᱮᱱᱟ᱾ ᱛᱤᱱᱟᱹᱜ ᱥᱟᱨᱮᱡ ᱮᱱᱟ?",
                quantity = total,
                secondaryQuantity = remove,
                operationSign = "-",
                mathAnswer = diff,
                iconType = icon,
                leftLabelHindi = "$total - $remove",
                rightLabelSantali = "$olTotal - $olRemove = $olDiff",
                teacherSolutionNote = "$total - $remove = $diff [Ol Chiki: $olTotal - $olRemove = $olDiff]",
                teacherPhoneticAnswer = "$olDiff ($diff)",
                nipunCode = "N-G1.2",
                activityIR = ir
            )
        }
    }

    private fun generateMultiplicationGroups(count: Int, config: WorksheetConfig, random: Random): List<WorksheetItem> {
        val maxGroups = if (config.grade == FlnGrade.GRADE_2) 4 else 5
        val maxPerGroup = if (config.grade == FlnGrade.GRADE_2) 4 else 5
        val icons = listOf("mango", "banana", "apple", "fish", "egg", "flower")

        return (1..count).map { idx ->
            val groups = random.nextInt(maxGroups - 1) + 2    // 2..5
            val perGroup = random.nextInt(maxPerGroup - 1) + 2  // 2..5
            val product = groups * perGroup
            val icon = icons[idx % icons.size]

            val olGroups = FlnCurriculumRepository.toOlChikiDigits(groups)
            val olPerGroup = FlnCurriculumRepository.toOlChikiDigits(perGroup)
            val olProduct = FlnCurriculumRepository.toOlChikiDigits(product)

            val ir = ActivityIR(
                id = "mult_ir_${idx}_$product",
                nipunCompetencyCode = "N-G2.2",
                actionType = org.tribetalk.fln.pipeline.ActivityActionType.MULTIPLICATION_GROUPS,
                grade = config.grade,
                primaryObjectKey = icon,
                quantity = groups,
                secondaryQuantity = perGroup,
                correctValue = product.toString(),
                distractorOptions = listOf(product.toString(), (product + groups).toString(), (product - perGroup).coerceAtLeast(2).toString(), (product + 2).toString()).distinct().shuffled(),
                instructionHindi = "$groups समूह हैं, प्रत्येक में $perGroup वस्तुएं हैं। गुणा करके कुल संख्या लिखें:",
                instructionSantali = "$olGroups ᱴᱤ ᱫᱚᱞ ᱢᱮᱱᱟᱜᱼᱟ, ᱢᱤᱫ ᱢᱤᱫ ᱨᱮ $olPerGroup ᱴᱤ ᱢᱮᱱᱟᱜᱼᱟ᱾ ᱜᱩᱬᱟᱹ ᱠᱟᱛᱮ ᱢᱩᱴᱷ ᱮᱞ ᱚᱞ ᱢᱮ:"
            )

            WorksheetItem(
                id = "mult_${idx}_$product",
                promptHindi = "$groups समूह हैं, प्रत्येक में $perGroup वस्तुएं हैं। कुल कितनी वस्तुएं हुईं?",
                promptSantali = "$olGroups ᱴᱤ ᱫᱚᱞ ᱢᱮᱱᱟᱜᱼᱟ, ᱢᱤᱫ ᱢᱤᱫ ᱨᱮ $olPerGroup ᱴᱤ ᱢᱮᱱᱟᱜᱼᱟ᱾ ᱢᱩᱴᱷ ᱛᱤᱱᱟᱹᱜ ᱦᱩᱭ ᱮᱱᱟ?",
                quantity = groups,
                secondaryQuantity = perGroup,
                operationSign = "×",
                mathAnswer = product,
                iconType = icon,
                leftLabelHindi = "$groups × $perGroup",
                rightLabelSantali = "$olGroups × $olPerGroup = $olProduct",
                teacherSolutionNote = "$groups × $perGroup = $product [Ol Chiki: $olGroups × $olPerGroup = $olProduct]",
                teacherPhoneticAnswer = "$olProduct ($product)",
                nipunCode = "N-G2.2",
                activityIR = ir
            )
        }
    }

    private fun generateMoneyCounting(count: Int, config: WorksheetConfig, random: Random): List<WorksheetItem> {
        val coinDenoms = listOf(1, 2, 5, 10)

        return (1..count).map { idx ->
            val c1 = coinDenoms[random.nextInt(coinDenoms.size)]
            val c2 = coinDenoms[random.nextInt(coinDenoms.size)]
            val total = c1 + c2

            val olC1 = FlnCurriculumRepository.toOlChikiDigits(c1)
            val olC2 = FlnCurriculumRepository.toOlChikiDigits(c2)
            val olTotal = FlnCurriculumRepository.toOlChikiDigits(total)

            val ir = ActivityIR(
                id = "money_ir_${idx}_$total",
                nipunCompetencyCode = "N-G2.3",
                actionType = org.tribetalk.fln.pipeline.ActivityActionType.MONEY_CALCULATION,
                grade = config.grade,
                primaryObjectKey = "coin_$c1",
                secondaryObjectKey = "coin_$c2",
                quantity = c1,
                secondaryQuantity = c2,
                correctValue = total.toString(),
                distractorOptions = listOf(total.toString(), (total + 1).toString(), (total - 1).coerceAtLeast(1).toString(), (total + 2).toString()).distinct().shuffled(),
                instructionHindi = "₹$c1 और ₹$c2 के सिक्कों को जोड़कर कुल मूल्य लिखें:",
                instructionSantali = "₹$olC1 ᱟᱨ ₹$olC2 ᱨᱮᱱᱟᱜ ᱥᱤᱠᱠᱟ ᱡᱚᱲ ᱠᱟᱛᱮ ᱢᱩᱴᱷ ᱴᱟᱠᱟ ᱚᱞ ᱢᱮ:"
            )

            WorksheetItem(
                id = "money_${idx}_$total",
                promptHindi = "आपके पास ₹$c1 और ₹$c2 के सिक्के हैं। कुल कितने रुपये हुए?",
                promptSantali = "ᱟᱢ ᱴᱷᱮᱱ ₹$olC1 ᱟᱨ ₹$olC2 ᱨᱮᱱᱟᱜ ᱥᱤᱠᱠᱟ ᱢᱮᱱᱟᱜᱼᱟ᱾ ᱡᱚᱛᱚ ᱛᱮ ᱛᱤᱱᱟᱹᱜ ᱴᱟᱠᱟ?",
                quantity = c1,
                secondaryQuantity = c2,
                operationSign = "+",
                mathAnswer = total,
                iconType = "coin_5",
                leftLabelHindi = "₹$c1 + ₹$c2",
                rightLabelSantali = "₹$olC1 + ₹$olC2 = ₹$olTotal",
                teacherSolutionNote = "₹$c1 + ₹$c2 = ₹$total [Ol Chiki: ₹$olC1 + ₹$olC2 = ₹$olTotal]",
                teacherPhoneticAnswer = "₹$olTotal (₹$total)",
                nipunCode = "N-G2.3",
                activityIR = ir
            )
        }
    }
}
