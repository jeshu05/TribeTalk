package org.tribetalk.worksheet.curriculum

import org.tribetalk.worksheet.QuestionType
import org.tribetalk.worksheet.SantaliVerificationStatus

/**
 * 6 Official Levels of the Foundational Stage Continuum (Ages 3 to 9)
 * Grounded in NCF-FS 2022 and NIPUN Bharat Guidelines.
 */
enum class FoundationalStage(
    val stageId: String,
    val displayName: String,
    val hindiDisplayName: String,
    val ageRange: String,
    val stageCode: String
) {
    PRE_SCHOOL_1("ps1", "Pre-School 1 (Nursery)", "पूर्व-प्राथमिक १ (नर्सरी)", "Ages 3–4", "PS-1"),
    PRE_SCHOOL_2("ps2", "Pre-School 2 (LKG)", "पूर्व-प्राथमिक २ (एलकेजी)", "Ages 4–5", "PS-2"),
    BALVATIKA("ps3_balvatika", "Pre-School 3 / Balvatika (UKG)", "बालवाटिका / यूकेजी", "Ages 5–6", "BAL"),
    GRADE_1("g1", "Grade 1 (Class 1)", "कक्षा १", "Ages 6–7", "G-1"),
    GRADE_2("g2", "Grade 2 (Class 2)", "कक्षा २", "Ages 7–8", "G-2"),
    GRADE_3("g3", "Grade 3 (Class 3)", "कक्षा ३", "Ages 8–9", "G-3")
}

/**
 * 7 Curricular Domains aligned with the 5 Koshas (Pancha Kosha) and Positive Learning Habits.
 */
enum class CurriculumDomain(
    val domainId: String,
    val displayName: String,
    val hindiName: String,
    val panchaKoshaName: String,
    val code: String
) {
    LANGUAGE_LITERACY("lang", "Language & Literacy Development", "भाषा एवं साक्षरता विकास", "Bhashik Vikas", "LANG"),
    NUMERACY("num", "Foundational Numeracy & Mathematics", "प्रारंभिक संख्या ज्ञान एवं गणित", "Bauddhik / Ganitik Vikas", "NUM"),
    COGNITIVE("cog", "Cognitive & Environmental Understanding", "ज्ञानात्मक एवं परिवेशीय समझ", "Bauddhik Vikas", "COG"),
    PHYSICAL("phy", "Physical & Fine Motor Development", "शारीरिक एवं सूक्ष्म गत्यात्मक विकास", "Sharirik Vikas", "PHY"),
    SOCIO_EMOTIONAL("se", "Socio-Emotional & Ethical Development", "सामाजिक-भावनात्मक एवं नैतिक विकास", "Manasik Vikas", "SE"),
    AESTHETIC("aes", "Aesthetic & Cultural Expression", "सौंदर्यात्मक एवं कलात्मक अभिव्यक्ति", "Chaitsik Vikas", "AES"),
    LEARNING_HABITS("hab", "Positive Learning Habits", "सकारात्मक अधिगम आदतें", "Pratyahara", "HAB")
}

/**
 * Pedagogical suitability of the competency.
 * Distinguishes paper worksheets from play, activity, and observational assessments.
 */
enum class WorksheetSuitability(val displayName: String, val badgeColor: String) {
    WORKSHEET_SUITABLE("Worksheet Practice Suitable", "#059669"),
    ACTIVITY_BASED("Play / Physical Activity Based", "#D97706"),
    TEACHER_LED("Teacher-Led Guided Discussion", "#2563EB"),
    OBSERVATION_BASED("Observational Assessment", "#7C3AED")
}

/**
 * Core Curriculum Item representing an official NCF-FS / NIPUN competency and learning trajectory.
 */
data class CurriculumItem(
    val id: String,
    val stage: FoundationalStage,
    val domain: CurriculumDomain,
    val curricularGoalId: String,          // e.g. "CG-6", "CG-8", "CG-9"
    val curricularGoalTitle: String,
    val competencyId: String,              // e.g. "C-6.2", "C-8.2", "C-9.1"
    val competencyTitle: String,
    val learningOutcomeId: String,         // e.g. "LO-NUM-BAL-01"
    val learningOutcomeText: String,       // Developmental trajectory statement
    val suitability: WorksheetSuitability,
    val supportedActivityTypes: List<QuestionType> = emptyList(),
    val difficultyBand: String = "Foundational",
    // Exemplar content for bilingual worksheet synthesis
    val hindiPrompt: String = "",
    val santaliOlChiki: String = "",
    val phoneticGuide: String = "",
    val englishGloss: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val verificationStatus: SantaliVerificationStatus = SantaliVerificationStatus.VERIFIED,
    val culturalTheme: String = "General", // "Animals", "Nature", "Family", "School", "Village"
    val visualAssetRef: String? = null
)