package org.tribetalk.curriculum.validator

import org.tribetalk.curriculum.spec.ActivitySpec

data class ValidationStepResult(
    val isValid: Boolean,
    val issues: List<String> = emptyList(),
    val repairedSpec: ActivitySpec
)

object SchemaValidator {

    fun validate(spec: ActivitySpec): ValidationStepResult {
        val issues = mutableListOf<String>()
        var repaired = spec

        if (repaired.id.isBlank()) {
            issues.add("Activity ID was blank. Generated new ID.")
            repaired = repaired.copy(id = "act_${System.currentTimeMillis()}")
        }

        if (repaired.items.isEmpty()) {
            issues.add("ActivitySpec contained 0 items. Injected default counting item.")
            repaired = repaired.copy(items = listOf(org.tribetalk.curriculum.spec.ActivityItemSpec("item_1", "mango", quantity = 3, correctAnswer = "3", options = listOf("2", "3", "4"))))
        }

        val checkedItems = repaired.items.map { item ->
            var itm = item
            if (itm.quantity < 1) {
                issues.add("Item ${itm.id} quantity was < 1 (${itm.quantity}). Clamped to 1.")
                itm = itm.copy(quantity = 1)
            }
            if (itm.correctAnswer.isBlank()) {
                issues.add("Item ${itm.id} correct answer was blank. Set to ${itm.quantity}.")
                itm = itm.copy(correctAnswer = itm.quantity.toString())
            }
            if (itm.options.isEmpty() || !itm.options.contains(itm.correctAnswer)) {
                issues.add("Item ${itm.id} options did not contain correct answer. Fixed.")
                val fixedOpts = (itm.options + itm.correctAnswer).distinct()
                itm = itm.copy(options = fixedOpts)
            }
            itm
        }

        repaired = repaired.copy(items = checkedItems)

        return ValidationStepResult(
            isValid = issues.isEmpty(),
            issues = issues,
            repairedSpec = repaired
        )
    }
}
