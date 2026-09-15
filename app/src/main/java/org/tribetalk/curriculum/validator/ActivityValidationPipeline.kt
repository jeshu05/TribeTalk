package org.tribetalk.curriculum.validator

import org.tribetalk.curriculum.ontology.LearningObjective
import org.tribetalk.curriculum.spec.ActivitySpec

data class ValidationPipelineResult(
    val isValid: Boolean,
    val spec: ActivitySpec,
    val allIssues: List<String>,
    val wasRepaired: Boolean
)

/**
 * Multi-stage pipeline executing:
 * 1. SchemaValidator
 * 2. CurriculumValidator
 * 3. MathematicalValidator
 * 4. AssetValidator
 * 5. LanguageValidator
 */
object ActivityValidationPipeline {

    fun validate(spec: ActivitySpec, objective: LearningObjective): ValidationPipelineResult {
        val allIssues = mutableListOf<String>()

        // 1. Schema
        val schemaRes = SchemaValidator.validate(spec)
        allIssues.addAll(schemaRes.issues)

        // 2. Curriculum
        val currRes = CurriculumValidator.validate(schemaRes.repairedSpec, objective)
        allIssues.addAll(currRes.issues)

        // 3. Math
        val mathRes = MathematicalValidator.validate(currRes.repairedSpec)
        allIssues.addAll(mathRes.issues)

        // 4. Asset
        val assetRes = AssetValidator.validate(mathRes.repairedSpec)
        allIssues.addAll(assetRes.issues)

        // 5. Language
        val langRes = LanguageValidator.validate(assetRes.repairedSpec)
        allIssues.addAll(langRes.issues)

        val finalSpec = langRes.repairedSpec
        val wasRepaired = allIssues.isNotEmpty()

        return ValidationPipelineResult(
            isValid = allIssues.isEmpty(),
            spec = finalSpec,
            allIssues = allIssues,
            wasRepaired = wasRepaired
        )
    }
}
