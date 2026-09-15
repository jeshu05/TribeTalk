package org.tribetalk.fln.pipeline

import org.tribetalk.fln.model.FlnGrade
import kotlin.random.Random

/**
 * Result of validating an ActivityIR.
 */
data class ValidationResult(
    val isValid: Boolean,
    val repairedIR: ActivityIR,
    val issuesFound: List<String>
)

/**
 * Deterministic Validator and Auto-Repair Engine for Activity IRs.
 * Ensures strict curriculum adherence, math soundness, and option uniqueness.
 */
object ActivityValidator {

    /**
     * Validates and auto-repairs an ActivityIR before presentation to child or worksheet engine.
     */
    fun validateAndRepair(ir: ActivityIR): ValidationResult {
        val issues = mutableListOf<String>()
        var repaired = ir

        // 1. Grade-based Quantity Bounds
        val maxAllowedQuantity = when (ir.grade) {
            FlnGrade.BALVATIKA -> 10
            FlnGrade.GRADE_1 -> 20
            FlnGrade.GRADE_2 -> 99
            FlnGrade.GRADE_3 -> 999
        }

        if (repaired.quantity < 1 || repaired.quantity > maxAllowedQuantity) {
            val clamped = repaired.quantity.coerceIn(1, maxAllowedQuantity)
            issues.add("Quantity ${repaired.quantity} exceeded limit $maxAllowedQuantity for ${ir.grade}. Clamped to $clamped.")
            repaired = repaired.copy(quantity = clamped)
        }

        // 2. Fallback Object Key
        if (repaired.primaryObjectKey.isBlank()) {
            issues.add("Primary object key was blank. Defaulted to 'mango'.")
            repaired = repaired.copy(primaryObjectKey = "mango")
        }

        // 3. Action-Type Specific Validations
        when (repaired.actionType) {
            ActivityActionType.COUNT_AND_SELECT,
            ActivityActionType.CIRCLE_THE_ANSWER -> {
                // Ensure correctValue matches quantity if numerical
                val expectedNumStr = repaired.quantity.toString()
                if (repaired.correctValue.toIntOrNull() != null && repaired.correctValue != expectedNumStr) {
                    issues.add("Correct value '${repaired.correctValue}' did not match quantity ${repaired.quantity}. Auto-repaired.")
                    repaired = repaired.copy(correctValue = expectedNumStr)
                }

                // Ensure options contain correctValue and have no duplicates
                val validatedOptions = repairDistractorOptions(
                    correctVal = repaired.correctValue,
                    currentOptions = repaired.distractorOptions,
                    isNumeric = repaired.correctValue.toIntOrNull() != null,
                    maxQuantity = maxAllowedQuantity
                )

                if (validatedOptions != repaired.distractorOptions) {
                    issues.add("Distractor options repaired for uniqueness and inclusion of correct answer.")
                    repaired = repaired.copy(distractorOptions = validatedOptions)
                }
            }

            ActivityActionType.ADDITION_CONCRETE -> {
                val sum = repaired.quantity + repaired.secondaryQuantity
                val sumStr = sum.toString()
                if (repaired.correctValue != sumStr) {
                    issues.add("Addition sum '${repaired.correctValue}' repaired to '$sumStr'.")
                    repaired = repaired.copy(correctValue = sumStr)
                }

                val validatedOptions = repairDistractorOptions(
                    correctVal = sumStr,
                    currentOptions = repaired.distractorOptions,
                    isNumeric = true,
                    maxQuantity = maxAllowedQuantity * 2
                )
                repaired = repaired.copy(distractorOptions = validatedOptions)
            }

            ActivityActionType.SUBTRACTION_CONCRETE -> {
                val diff = (repaired.quantity - repaired.secondaryQuantity).coerceAtLeast(0)
                val diffStr = diff.toString()
                if (repaired.correctValue != diffStr) {
                    issues.add("Subtraction difference '${repaired.correctValue}' repaired to '$diffStr'.")
                    repaired = repaired.copy(correctValue = diffStr)
                }

                val validatedOptions = repairDistractorOptions(
                    correctVal = diffStr,
                    currentOptions = repaired.distractorOptions,
                    isNumeric = true,
                    maxQuantity = maxAllowedQuantity
                )
                repaired = repaired.copy(distractorOptions = validatedOptions)
            }

            ActivityActionType.MULTIPLICATION_GROUPS -> {
                val groups = repaired.quantity.coerceIn(1, 10)
                val perGroup = repaired.secondaryQuantity.coerceIn(1, 10)
                val product = groups * perGroup
                val prodStr = product.toString()
                if (repaired.correctValue != prodStr) {
                    issues.add("Multiplication product '${repaired.correctValue}' repaired to '$prodStr'.")
                    repaired = repaired.copy(correctValue = prodStr)
                }

                val validatedOptions = repairDistractorOptions(
                    correctVal = prodStr,
                    currentOptions = repaired.distractorOptions,
                    isNumeric = true,
                    maxQuantity = 100
                )
                repaired = repaired.copy(distractorOptions = validatedOptions)
            }

            ActivityActionType.MONEY_CALCULATION -> {
                val validatedOptions = repairDistractorOptions(
                    correctVal = repaired.correctValue,
                    currentOptions = repaired.distractorOptions,
                    isNumeric = repaired.correctValue.toIntOrNull() != null,
                    maxQuantity = 100
                )
                repaired = repaired.copy(distractorOptions = validatedOptions)
            }

            ActivityActionType.SHAPE_RECOGNITION -> {
                if (repaired.distractorOptions.isNotEmpty() && !repaired.distractorOptions.contains(repaired.correctValue)) {
                    val fixed = (repaired.distractorOptions.take(3) + repaired.correctValue).distinct().shuffled()
                    issues.add("Correct shape '${repaired.correctValue}' inserted into options.")
                    repaired = repaired.copy(distractorOptions = fixed)
                }
            }

            else -> {
                // For linguistic tasks, ensure options contain correctValue
                if (repaired.distractorOptions.isNotEmpty() && !repaired.distractorOptions.contains(repaired.correctValue)) {
                    val fixed = (repaired.distractorOptions.take(3) + repaired.correctValue).distinct().shuffled()
                    issues.add("Correct word '${repaired.correctValue}' inserted into options.")
                    repaired = repaired.copy(distractorOptions = fixed)
                }
            }
        }

        // 4. Default instructions if missing
        if (repaired.instructionHindi.isBlank()) {
            val fallbackHindi = when (repaired.actionType) {
                ActivityActionType.COUNT_AND_SELECT -> "चित्रों को गिनें और सही संख्या चुनें।"
                ActivityActionType.COUNT_AND_MATCH -> "वस्तुओं को सही संख्या से मिलाएँ।"
                ActivityActionType.ADDITION_CONCRETE -> "दोनों समूहों को जोड़कर कुल संख्या बताएँ।"
                ActivityActionType.SUBTRACTION_CONCRETE -> "वस्तुओं को घटाकर बची संख्या बताएँ।"
                ActivityActionType.MULTIPLICATION_GROUPS -> "समान समूहों को देखकर कुल संख्या बताएँ।"
                ActivityActionType.MONEY_CALCULATION -> "सिक्कों और रुपयों को जोड़कर कुल मूल्य बताएँ।"
                ActivityActionType.SHAPE_RECOGNITION -> "सही आकार की पहचान करें।"
                ActivityActionType.AKSHAR_PHONICS_TRACE -> "अक्षर की ध्वनि सुनें और अनुरेखण करें।"
                ActivityActionType.PICTURE_WORD_MATCH -> "चित्र को उसके सही नाम से मिलाएँ।"
                else -> "गतिविधि को ध्यानपूर्वक हल करें।"
            }
            repaired = repaired.copy(instructionHindi = fallbackHindi)
        }

        if (repaired.instructionSantali.isBlank()) {
            val fallbackSantali = when (repaired.actionType) {
                ActivityActionType.COUNT_AND_SELECT -> "ᱪᱤᱛᱟᱹᱨ ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱥᱟᱹᱦᱤ ᱮᱞ ᱵᱟᱪᱷᱟᱣ ᱢᱮ᱾"
                ActivityActionType.COUNT_AND_MATCH -> "ᱡᱤᱱᱤᱥ ᱠᱚ ᱥᱟᱹᱦᱤ ᱮᱞ ᱥᱟᱶ ᱡᱚᱲᱟᱣ ᱢᱮ᱾"
                ActivityActionType.ADDITION_CONCRETE -> "ᱵᱟᱱᱟᱨ ᱫᱚᱞ ᱡᱚᱲ ᱠᱟᱛᱮ ᱢᱩᱴᱷ ᱮᱞ ᱞᱟᱹᱭ ᱢᱮ᱾"
                ActivityActionType.SUBTRACTION_CONCRETE -> "ᱡᱤᱱᱤᱥ ᱵᱷᱮᱜᱟᱨ ᱠᱟᱛᱮ ᱥᱟᱨᱮᱡ ᱮᱞ ᱞᱟᱹᱭ ᱢᱮ᱾"
                ActivityActionType.MULTIPLICATION_GROUPS -> "ᱥᱚᱢᱟᱱ ᱫᱚᱞ ᱧᱮᱞ ᱠᱟᱛᱮ ᱢᱩᱴᱷ ᱮᱞ ᱞᱟᱹᱭ ᱢᱮ᱾"
                ActivityActionType.MONEY_CALCULATION -> "ᱯᱩᱭᱥᱟᱹ ᱟᱨ ᱴᱟᱠᱟ ᱞᱮᱠᱷᱟ ᱠᱟᱛᱮ ᱞᱟᱹᱭ ᱢᱮ᱾"
                ActivityActionType.SHAPE_RECOGNITION -> "ᱥᱟᱹᱦᱤ ᱨᱩᱯ ᱪᱤᱱᱦᱟᱹᱣ ᱢᱮ᱾"
                ActivityActionType.AKSHAR_PHONICS_TRACE -> "ᱪᱤᱠᱤ ᱟᱲᱟᱝ ᱟᱸᱡᱚᱢ ᱢᱮ ᱟᱨ ᱚᱞ ᱢᱮ᱾"
                ActivityActionType.PICTURE_WORD_MATCH -> "ᱪᱤᱛᱟᱹᱨ ᱟᱡᱟᱜ ᱥᱟᱹᱦᱤ ᱧᱩᱛᱩᱢ ᱥᱟᱶ ᱡᱚᱲᱟᱣ ᱢᱮ᱾"
                else -> "ᱠᱟᱹᱢᱤ ᱫᱚ ᱱᱟᱯᱟᱭ ᱛᱮ ᱯᱩᱨᱟᱹᱣ ᱢᱮ᱾"
            }
            repaired = repaired.copy(instructionSantali = fallbackSantali)
        }

        return ValidationResult(
            isValid = issues.isEmpty(),
            repairedIR = repaired,
            issuesFound = issues
        )
    }

    /**
     * Ensures distractor options contain the correct value, have 3 or 4 unique items, and no duplicates.
     */
    private fun repairDistractorOptions(
        correctVal: String,
        currentOptions: List<String>,
        isNumeric: Boolean,
        maxQuantity: Int
    ): List<String> {
        // If already valid (contains correctVal, 3-4 distinct non-blank items), keep intact
        if (currentOptions.contains(correctVal) &&
            currentOptions.size in 3..4 &&
            currentOptions.distinct().size == currentOptions.size &&
            currentOptions.all { it.isNotBlank() }
        ) {
            return currentOptions
        }

        val uniqueSet = LinkedHashSet<String>()
        uniqueSet.add(correctVal)

        // Add valid non-duplicate existing options
        for (opt in currentOptions) {
            if (opt.isNotBlank() && opt != correctVal) {
                uniqueSet.add(opt)
            }
            if (uniqueSet.size >= 4) break
        }

        // If numeric and we need more options, synthesize nearby plausible numbers
        if (isNumeric && uniqueSet.size < 3) {
            val num = correctVal.toIntOrNull() ?: 5
            val candidates = listOf(
                (num - 1).coerceAtLeast(1),
                (num + 1).coerceAtMost(maxQuantity),
                (num - 2).coerceAtLeast(1),
                (num + 2).coerceAtMost(maxQuantity),
                (num + 3).coerceAtMost(maxQuantity)
            ).map { it.toString() }.filter { it != correctVal }

            for (c in candidates) {
                uniqueSet.add(c)
                if (uniqueSet.size >= 3) break
            }
        }

        return uniqueSet.toList().shuffled()
    }
}
