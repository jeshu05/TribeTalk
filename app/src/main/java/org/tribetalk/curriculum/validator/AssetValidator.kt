package org.tribetalk.curriculum.validator

import org.tribetalk.curriculum.spec.ActivitySpec
import org.tribetalk.fln.pipeline.SvgCorpusRegistry

object AssetValidator {

    private val VALID_FALLBACK_KEY = "mango"

    fun validate(spec: ActivitySpec): ValidationStepResult {
        val issues = mutableListOf<String>()
        var repaired = spec

        var primaryKey = repaired.visualSpec.primaryAssetKey.lowercase().trim()
        if (SvgCorpusRegistry.get(primaryKey) == null) {
            val matched = SvgCorpusRegistry.findMatchingKey(primaryKey) ?: VALID_FALLBACK_KEY
            issues.add("Visual asset '$primaryKey' not found in SVG corpus. Replaced with '$matched'.")
            primaryKey = matched
        }

        val checkedItems = repaired.items.map { item ->
            var itm = item
            var asset = itm.visualAsset.lowercase().trim()
            if (SvgCorpusRegistry.get(asset) == null) {
                val matched = SvgCorpusRegistry.findMatchingKey(asset) ?: primaryKey
                issues.add("Item asset '$asset' not found in SVG corpus. Replaced with '$matched'.")
                itm = itm.copy(visualAsset = matched)
            }
            itm
        }

        repaired = repaired.copy(
            items = checkedItems,
            visualSpec = repaired.visualSpec.copy(primaryAssetKey = primaryKey)
        )

        return ValidationStepResult(
            isValid = issues.isEmpty(),
            issues = issues,
            repairedSpec = repaired
        )
    }
}
