package org.tribetalk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.worksheet.QuestionType
import org.tribetalk.worksheet.Worksheet
import org.tribetalk.worksheet.WorksheetGenerator
import org.tribetalk.worksheet.curriculum.CurriculumDomain
import org.tribetalk.worksheet.curriculum.CurriculumItem
import org.tribetalk.worksheet.curriculum.FoundationalStage
import org.tribetalk.worksheet.curriculum.NipunCurriculumRegistry
import org.tribetalk.worksheet.curriculum.WorksheetSuitability

/**
 * Screen 1: Official NIPUN Bharat / NCF-FS Curriculum-Aligned Worksheet Generator.
 *
 * Implements progressive curriculum filtering:
 * Stage (PS1 to G3) -> Domain -> Curricular Goal / Competency -> Learning Outcome -> Activity Type.
 * Also preserves Custom Topic input with bilingual generation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksheetGeneratorScreen(
    onWorksheetGenerated: (Worksheet) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Curriculum-Driven, 1: Custom Lesson

    // Curriculum Selection State
    var selectedStage by remember { mutableStateOf(FoundationalStage.GRADE_2) }
    var selectedDomain by remember { mutableStateOf(CurriculumDomain.LANGUAGE_LITERACY) }
    var selectedItem by remember {
        mutableStateOf(
            NipunCurriculumRegistry.getItems(FoundationalStage.GRADE_2, CurriculumDomain.LANGUAGE_LITERACY).firstOrNull()
                ?: NipunCurriculumRegistry.getAllItems().first()
        )
    }
    var selectedActivityType by remember { mutableStateOf<QuestionType?>(null) }
    var questionCount by remember { mutableStateOf(5) }
    var showAlignmentDetails by remember { mutableStateOf(false) }

    // Custom Lesson Topic State
    var customTopic by remember { mutableStateOf("Animals") }
    var customGrade by remember { mutableStateOf("Grade 3") }
    var customHindiContent by remember {
        mutableStateOf("गाय एक पालतू जानवर है।\nकुत्ता एक पालतू जानवर है।\nमछली पानी में रहती है।")
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val generator = remember { WorksheetGenerator() }
    val scrollState = rememberScrollState()

    // Available items for the current stage and domain
    val availableItems = remember(selectedStage, selectedDomain) {
        val list = NipunCurriculumRegistry.getItems(selectedStage, selectedDomain)
        if (list.isNotEmpty() && !list.contains(selectedItem)) {
            selectedItem = list.first()
            selectedActivityType = list.first().supportedActivityTypes.firstOrNull()
        }
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Worksheet Studio",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            "NCF Foundational Stage 2022 • NIPUN Bharat Guidelines",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.shadow(1.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab Selector: Curriculum vs Custom
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Official Curriculum", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Custom Lesson", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // =========================================================================
                // CURRICULUM DRIVEN SELECTION (Phase 8 Progressive Filtering)
                // =========================================================================

                // Step 1: Stage Selection
                Text(
                    text = "1. Select Stage / Grade Level",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val stages = FoundationalStage.values().toList()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        stages.take(3).forEach { stage ->
                            StageFilterChip(
                                stage = stage,
                                isSelected = selectedStage == stage,
                                onSelect = { selectedStage = stage },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        stages.drop(3).forEach { stage ->
                            StageFilterChip(
                                stage = stage,
                                isSelected = selectedStage == stage,
                                onSelect = { selectedStage = stage },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Step 2: Domain Selection
                Text(
                    text = "2. Curricular Domain (Pancha Kosha / NCF-FS)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CurriculumDomain.values().forEach { domain ->
                        val isSelected = selectedDomain == domain
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDomain = domain }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = domain.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${domain.hindiName} • ${domain.panchaKoshaName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Step 3: Competency & Outcome Selection
                Text(
                    text = "3. Target Competency & Learning Trajectory",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                if (availableItems.isEmpty()) {
                    Text(
                        "इस स्तर और क्षेत्र के लिए कोई योग्यता प्रविष्टि नहीं है।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableItems.forEach { item ->
                            val isSelected = selectedItem.id == item.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedItem = item
                                        selectedActivityType = item.supportedActivityTypes.firstOrNull()
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${item.curricularGoalId} • ${item.competencyId}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        SuitabilityBadge(item.suitability)
                                    }
                                    Text(
                                        text = item.competencyTitle,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "अधिगम फल: ${item.learningOutcomeText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Step 4: Activity Type & Question Count
                if (selectedItem.suitability == WorksheetSuitability.WORKSHEET_SUITABLE) {
                    Text(
                        text = "4. Activity Type & Count",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Activity Format:", style = MaterialTheme.typography.bodySmall)
                        val types = selectedItem.supportedActivityTypes
                        types.forEach { type ->
                            FilterChip(
                                selected = selectedActivityType == type,
                                onClick = { selectedActivityType = type },
                                label = { Text(type.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Question Count:", style = MaterialTheme.typography.bodySmall)
                        listOf(3, 5, 8).forEach { count ->
                            FilterChip(
                                selected = questionCount == count,
                                onClick = { questionCount = count },
                                label = { Text("$count Qs", fontSize = 11.sp) }
                            )
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚠️ Non-Worksheet Competency (${selectedItem.suitability.displayName})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "NCF-FS specifies this competency is best developed through teacher-led dialogue, play, or observational assessment rather than paper worksheets.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Collapsible Curriculum Alignment Details Drawer (Phase 8 & 9)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAlignmentDetails = !showAlignmentDetails }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text(
                                    "Curriculum Alignment Details",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(
                                imageVector = if (showAlignmentDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }

                        AnimatedVisibility(visible = showAlignmentDetails) {
                            Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("• Framework: NIPUN Bharat + NCF for Foundational Stage (NCF-FS 2022)", fontSize = 11.sp)
                                Text("• Stage: ${selectedStage.displayName} (${selectedStage.ageRange})", fontSize = 11.sp)
                                Text("• Curricular Goal: ${selectedItem.curricularGoalId} - ${selectedItem.curricularGoalTitle}", fontSize = 11.sp)
                                Text("• Competency: ${selectedItem.competencyId} - ${selectedItem.competencyTitle}", fontSize = 11.sp)
                                Text("• Learning Outcome: ${selectedItem.learningOutcomeId} - ${selectedItem.learningOutcomeText}", fontSize = 11.sp)
                                Text("• Note: LOs are developmental trajectories, not rigid single-grade barriers.", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }

                // Generate Button for Curriculum
                Button(
                    onClick = {
                        isGenerating = true
                        try {
                            val ws = generator.generateFromCurriculum(
                                item = selectedItem,
                                targetQuestionCount = questionCount,
                                preferredType = selectedActivityType
                            )
                            onWorksheetGenerated(ws)
                        } catch (e: Exception) {
                            errorMessage = "Generation error: ${e.message}"
                        } finally {
                            isGenerating = false
                        }
                    },
                    enabled = selectedItem.suitability == WorksheetSuitability.WORKSHEET_SUITABLE && !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Curriculum Worksheet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

            } else {
                // =========================================================================
                // CUSTOM LESSON TOPIC (Preserving Existing Dynamic Input)
                // =========================================================================
                Text(
                    text = "Quick Lesson Presets",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        Triple("Animals", "Grade 3", "गाय एक पालतू जानवर है।\nकुत्ता एक पालतू जानवर है।\nमछली पानी में रहती है।"),
                        Triple("Classroom", "Grade 2", "किताब खोलो।\nलिखना शुरू करो।\nब्लैकबोर्ड देखो।\nशांत रहो।"),
                        Triple("Nature", "Grade 1", "सूरज पूर्व में उगता है।\nपेड़ हमें छाया देते हैं।\nपानी जीवन के लिए जरूरी है।")
                    )
                    presets.forEach { (presetTopic, presetGrade, presetContent) ->
                        SuggestionChip(
                            onClick = {
                                customTopic = presetTopic
                                customGrade = presetGrade
                                customHindiContent = presetContent
                                errorMessage = null
                            },
                            label = { Text(presetTopic, fontSize = 12.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = customTopic,
                    onValueChange = { customTopic = it },
                    label = { Text("Lesson Topic (पाठ का नाम)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = customGrade,
                    onValueChange = { customGrade = it },
                    label = { Text("Class / Grade (कक्षा)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = customHindiContent,
                    onValueChange = { customHindiContent = it },
                    label = { Text("Lesson Content in Hindi (पाठ की पंक्तियाँ)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        isGenerating = true
                        try {
                            val ws = generator.generateWorksheet(
                                topic = customTopic,
                                hindiContent = customHindiContent,
                                grade = customGrade
                            )
                            onWorksheetGenerated(ws)
                        } catch (e: Exception) {
                            errorMessage = "Generation error: ${e.message}"
                        } finally {
                            isGenerating = false
                        }
                    },
                    enabled = customTopic.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Bilingual Worksheet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun StageFilterChip(
    stage: FoundationalStage,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stage.stageCode,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stage.ageRange,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun SuitabilityBadge(suitability: WorksheetSuitability) {
    val (color, text) = when (suitability) {
        WorksheetSuitability.WORKSHEET_SUITABLE -> Color(0xFF059669) to "Worksheet"
        WorksheetSuitability.ACTIVITY_BASED -> Color(0xFFD97706) to "Play / Activity"
        WorksheetSuitability.TEACHER_LED -> Color(0xFF2563EB) to "Teacher-Led"
        WorksheetSuitability.OBSERVATION_BASED -> Color(0xFF7C3AED) to "Observation"
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
