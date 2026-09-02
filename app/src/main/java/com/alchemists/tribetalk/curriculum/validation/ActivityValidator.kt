package com.alchemists.tribetalk.curriculum.validation

import com.alchemists.tribetalk.curriculum.models.ActivityType
import com.alchemists.tribetalk.curriculum.models.GeneratedActivityItem

object ActivityValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validate(item: GeneratedActivityItem): ValidationResult {
        // 1. Mandatory Identifiers & Text Fields
        if (item.questionId.isBlank()) return ValidationResult(false, "Question ID cannot be blank")
        if (item.learningOutcomeId.isBlank()) return ValidationResult(false, "Learning outcome ID cannot be blank")
        if (item.nipunCode.isBlank()) return ValidationResult(false, "NIPUN code cannot be blank")
        if (item.questionHindi.isBlank()) return ValidationResult(false, "Hindi question cannot be blank")
        if (item.questionSantali.isBlank()) return ValidationResult(false, "Santali question cannot be blank")

        // 2. Options and Correct Answer Index Bounds
        if (item.optionsHindi.isEmpty() || item.optionsSantali.isEmpty()) {
            return ValidationResult(false, "Options lists cannot be empty")
        }
        if (item.correctAnswerIndex !in item.optionsHindi.indices) {
            return ValidationResult(false, "Correct answer index ${item.correctAnswerIndex} out of bounds for options size ${item.optionsHindi.size}")
        }
        if (item.correctAnswerValue.isBlank()) {
            return ValidationResult(false, "Correct answer value cannot be blank")
        }

        // 3. Activity Type Specific Mathematical & Structural Integrity Checks
        when (item.activityType) {
            ActivityType.COUNT_OBJECTS -> {
                if (item.stimulus == null) return ValidationResult(false, "Count objects activity requires a visual stimulus")
                if (item.stimulus.count <= 0) return ValidationResult(false, "Visual stimulus count must be positive")
            }
            ActivityType.ADDITION -> {
                // Parse op1 + op2 = sum pattern from question if present
                val match = Regex("""(\d+)\s*\+\s*(\d+)""").find(item.questionHindi)
                if (match != null) {
                    val op1 = match.groupValues[1].toInt()
                    val op2 = match.groupValues[2].toInt()
                    val expectedSum = op1 + op2
                    val actualVal = item.correctAnswerValue.filter { it.isDigit() }.toIntOrNull()
                    if (actualVal != null && actualVal != expectedSum) {
                        return ValidationResult(false, "Addition math error: $op1 + $op2 should equal $expectedSum but got $actualVal")
                    }
                }
            }
            ActivityType.SUBTRACTION -> {
                val match = Regex("""(\d+)\s*-\s*(\d+)""").find(item.questionHindi)
                if (match != null) {
                    val op1 = match.groupValues[1].toInt()
                    val op2 = match.groupValues[2].toInt()
                    val expectedDiff = op1 - op2
                    val actualVal = item.correctAnswerValue.filter { it.isDigit() }.toIntOrNull()
                    if (actualVal != null && actualVal != expectedDiff) {
                        return ValidationResult(false, "Subtraction math error: $op1 - $op2 should equal $expectedDiff but got $actualVal")
                    }
                }
            }
            ActivityType.MATCHING -> {
                if (item.matchingPairs.isEmpty()) return ValidationResult(false, "Matching activity requires matching pairs")
            }
            else -> {}
        }

        return ValidationResult(true)
    }
}
