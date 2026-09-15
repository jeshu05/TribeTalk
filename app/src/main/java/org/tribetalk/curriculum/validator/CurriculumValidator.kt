package org.tribetalk.curriculum.validator

import org.tribetalk.curriculum.ontology.LearningObjective
import org.tribetalk.curriculum.spec.ActivitySpec

object CurriculumValidator {

    fun validate(spec: ActivitySpec, objective: LearningObjective): ValidationStepResult {
        val issues = mutableListOf<String>()
        var repaired = spec

        // 1. Check Objective ID
        if (repaired.objectiveId != objective.id) {
            issues.add("Objective ID '${repaired.objectiveId}' mismatched expected '${objective.id}'. Synchronized.")
            repaired = repaired.copy(objectiveId = objective.id)
        }

        // 2. Check Allowed Activity Type
        if (!objective.allowedActivityTypes.contains(repaired.activityType)) {
            val fallbackType = objective.allowedActivityTypes.firstOrNull() ?: org.tribetalk.curriculum.spec.ActivityType.COUNT
            issues.add("ActivityType '${repaired.activityType}' not allowed for ${objective.id}. Replaced with $fallbackType.")
            repaired = repaired.copy(activityType = fallbackType)
        }

        // 3. Check Number Range (e.g. 1..10)
        val minRange = objective.numberRange.first
        val maxRange = objective.numberRange.last
        val checkedItems = repaired.items.map { item ->
            var itm = item
            if (itm.quantity < minRange || itm.quantity > maxRange) {
                val clamped = itm.quantity.coerceIn(minRange, maxRange)
                issues.add("Item quantity ${itm.quantity} exceeded objective range $minRange..$maxRange. Clamped to $clamped.")
                val oldAns = itm.correctAnswer
                val newAns = if (oldAns == itm.quantity.toString()) clamped.toString() else oldAns
                itm = itm.copy(quantity = clamped, correctAnswer = newAns)
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
