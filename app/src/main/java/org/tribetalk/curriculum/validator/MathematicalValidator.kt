package org.tribetalk.curriculum.validator

import org.tribetalk.curriculum.spec.ActivitySpec
import org.tribetalk.curriculum.spec.ActivityType

object MathematicalValidator {

    fun validate(spec: ActivitySpec): ValidationStepResult {
        val issues = mutableListOf<String>()
        var repaired = spec

        val checkedItems = repaired.items.map { item ->
            var itm = item
            when (spec.activityType) {
                ActivityType.COUNT,
                ActivityType.COUNT_AND_MATCH,
                ActivityType.CIRCLE_CORRECT -> {
                    val expected = itm.quantity.toString()
                    if (itm.correctAnswer != expected) {
                        issues.add("Math error: Count of ${itm.quantity} did not match answer '${itm.correctAnswer}'. Repaired to '$expected'.")
                        itm = itm.copy(correctAnswer = expected)
                    }

                    // Repair options only if missing answer, has duplicates, or fewer than 3 options
                    if (needsOptionsRepair(itm.correctAnswer, itm.options)) {
                        val fixedOptions = repairOptions(itm.correctAnswer, itm.options, itm.quantity)
                        issues.add("Options repaired for uniqueness and inclusion of correct answer.")
                        itm = itm.copy(options = fixedOptions)
                    }
                }

                ActivityType.COMPARE_QUANTITIES -> {
                    val q1 = itm.quantity
                    val q2 = itm.secondaryQuantity
                    val expectedSymbol = when {
                        q1 < q2 -> "<"
                        q1 > q2 -> ">"
                        else -> "="
                    }
                    if (itm.correctAnswer != expectedSymbol) {
                        issues.add("Math error: Comparison of $q1 and $q2 expected '$expectedSymbol' but got '${itm.correctAnswer}'. Repaired.")
                        itm = itm.copy(correctAnswer = expectedSymbol, options = listOf("<", "=", ">"))
                    }
                }

                ActivityType.SIMPLE_ADDITION -> {
                    val sum = itm.quantity + itm.secondaryQuantity
                    val expectedSum = sum.toString()
                    if (itm.correctAnswer != expectedSum) {
                        issues.add("Math error: Addition of ${itm.quantity} + ${itm.secondaryQuantity} expected '$expectedSum' but got '${itm.correctAnswer}'. Repaired.")
                        itm = itm.copy(correctAnswer = expectedSum)
                    }
                    if (needsOptionsRepair(expectedSum, itm.options)) {
                        val fixedOptions = repairOptions(expectedSum, itm.options, sum)
                        issues.add("Options repaired for addition.")
                        itm = itm.copy(options = fixedOptions)
                    }
                }

                else -> {
                    if (!itm.options.contains(itm.correctAnswer)) {
                        itm = itm.copy(options = (itm.options + itm.correctAnswer).distinct())
                    }
                }
            }
            itm
        }

        // Also synchronize top-level AnswerSpec
        val primaryItem = checkedItems.firstOrNull()
        if (primaryItem != null && repaired.answerSpec.correctValue != primaryItem.correctAnswer) {
            repaired = repaired.copy(
                answerSpec = repaired.answerSpec.copy(
                    correctValue = primaryItem.correctAnswer,
                    numericValue = primaryItem.correctAnswer.toIntOrNull() ?: primaryItem.quantity,
                    distractors = primaryItem.options.filter { it != primaryItem.correctAnswer }
                )
            )
        }

        repaired = repaired.copy(items = checkedItems)

        return ValidationStepResult(
            isValid = issues.isEmpty(),
            issues = issues,
            repairedSpec = repaired
        )
    }

    private fun needsOptionsRepair(correctVal: String, current: List<String>): Boolean {
        if (current.size < 3) return true
        if (!current.contains(correctVal)) return true
        if (current.distinct().size != current.size) return true
        return false
    }

    private fun repairOptions(correctVal: String, current: List<String>, centerNum: Int): List<String> {
        val unique = LinkedHashSet<String>()
        unique.add(correctVal)

        for (opt in current) {
            if (opt.isNotBlank() && opt != correctVal) {
                unique.add(opt)
            }
            if (unique.size >= 4) break
        }

        if (unique.size < 3) {
            val candidates = listOf(
                (centerNum - 1).coerceAtLeast(1),
                centerNum + 1,
                (centerNum - 2).coerceAtLeast(1),
                centerNum + 2
            ).map { it.toString() }.filter { it != correctVal }

            for (c in candidates) {
                unique.add(c)
                if (unique.size >= 4) break
            }
        }

        return unique.toList().shuffled()
    }
}
