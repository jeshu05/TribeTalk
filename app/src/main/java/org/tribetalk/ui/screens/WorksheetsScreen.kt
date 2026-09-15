package org.tribetalk.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import org.tribetalk.fln.image.rememberFlnImage
import org.tribetalk.fln.model.*
import org.tribetalk.fln.pipeline.SvgCorpusRegistry
import org.tribetalk.ui.theme.*

/**
 * World-Class NIPUN Bharat Worksheet Studio.
 * Seamlessly adapts between single-column phones and two-pane tablet workstations.
 * Generates printable 2-page A4 vector PDFs on the spot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksheetsScreen(
    flnViewModel: FlnViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config by flnViewModel.worksheetConfig.collectAsState()
    val items by flnViewModel.worksheetItems.collectAsState()
    val previewTab by flnViewModel.previewTab.collectAsState()
    val isGeneratingPdf by flnViewModel.isGeneratingPdf.collectAsState()

    var topicInputText by remember { mutableStateOf("") }
    val windowSizeInfo = rememberWindowSizeInfo()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NIPUN Worksheet Studio",
                            style = if (windowSizeInfo.isCompactHeight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!windowSizeInfo.isCompactHeight) {
                            Text(
                                text = "Foundational Literacy & Numeracy • 2-Page Printable A4 PDF",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { flnViewModel.openStudio() }) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "AI Content Studio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { flnViewModel.regenerateWorksheet() }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "New Problems",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline)
            )
        },
        bottomBar = {
            // Only show full-width bottom bar on phones; tablets host the export button inside the left studio pane
            if (!windowSizeInfo.isTabletOrExpanded) {
                WorksheetBottomExportBar(
                    isGeneratingPdf = isGeneratingPdf,
                    isCompact = windowSizeInfo.isCompactHeight,
                    onExportPdf = { flnViewModel.exportAndSharePdf(context) }
                )
            }
        }
    ) { paddingValues ->
        if (windowSizeInfo.isTabletOrExpanded) {
            // =========================================================================
            // TABLET / EXPANDED: Two-Pane Educational Studio Layout
            // Left Pane: Studio Controls & Scaffolding + Sticky PDF Export
            // Right Pane: Live Printable A4 Sheet Preview & Teacher Key
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Left Studio Console
                Column(
                    modifier = Modifier
                        .widthIn(min = 380.dp, max = 430.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            LessonTopicDesignerCard(
                                topicText = topicInputText,
                                onTopicChange = { topicInputText = it },
                                onSynthesize = { topic ->
                                    flnViewModel.generateWorksheetFromTopic(topic)
                                }
                            )
                        }

                        item {
                            WorksheetTypeSelector(
                                selectedType = config.type,
                                onTypeSelected = { flnViewModel.setWorksheetType(it) }
                            )
                        }

                        item {
                            GradeAndDifficultySelectors(
                                selectedGrade = config.grade,
                                selectedDifficulty = config.difficulty,
                                onGradeSelected = { flnViewModel.setWorksheetGrade(it) },
                                onDifficultySelected = { flnViewModel.setWorksheetDifficulty(it) }
                            )
                        }
                    }

                    // Sticky Bottom PDF Export in Left Console
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline))
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WorksheetExportButton(
                                isGeneratingPdf = isGeneratingPdf,
                                onExportPdf = { flnViewModel.exportAndSharePdf(context) }
                            )
                            Text(
                                text = "Page 1: Student Sheet  •  Page 2: Teacher Answer Key",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Right Pane: Live Printable A4 Sheet Preview
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Studio Tab Switcher
                    PreviewTabSwitcher(
                        previewTab = previewTab,
                        onTabSelected = { flnViewModel.setPreviewTab(it) },
                        modifier = Modifier.widthIn(max = 760.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated A4 Printable Sheet Container
                    Card(
                        modifier = Modifier
                            .widthIn(max = 760.dp)
                            .fillMaxSize(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Sheet Title Header & NCERT CNCL Directives
                            if (previewTab == WorksheetPreviewTab.STUDENT_SHEET) {
                                item {
                                    SimulatedA4Header(config = config)
                                }
                                item {
                                    NcertInstructionBanner(type = config.type)
                                }
                                if (config.type == WorksheetType.COUNT_AND_MATCH || config.type == WorksheetType.PICTURE_WORD_MATCH) {
                                    item {
                                        NcertMatchingSection(items = items, config = config)
                                    }
                                }
                            }

                            if (config.type != WorksheetType.COUNT_AND_MATCH && config.type != WorksheetType.PICTURE_WORD_MATCH || previewTab == WorksheetPreviewTab.TEACHER_KEY) {
                                itemsIndexed(items) { index, item ->
                                    if (previewTab == WorksheetPreviewTab.STUDENT_SHEET) {
                                        StudentProblemCard(index = index + 1, item = item, type = config.type)
                                    } else {
                                        TeacherKeyCard(index = index + 1, item = item)
                                    }
                                }
                            }

                            if (previewTab == WorksheetPreviewTab.STUDENT_SHEET) {
                                item {
                                    NcertLearningOutcomeCard(type = config.type, grade = config.grade)
                                }
                                item {
                                    TeacherRubricCard()
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        } else {
            // =========================================================================
            // PHONE / COMPACT: Single Responsive Flow
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 680.dp)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(2.dp))
                        LessonTopicDesignerCard(
                            topicText = topicInputText,
                            onTopicChange = { topicInputText = it },
                            onSynthesize = { topic ->
                                flnViewModel.generateWorksheetFromTopic(topic)
                            }
                        )
                    }

                    item {
                        WorksheetTypeSelector(
                            selectedType = config.type,
                            onTypeSelected = { flnViewModel.setWorksheetType(it) }
                        )
                    }

                    item {
                        GradeAndDifficultySelectors(
                            selectedGrade = config.grade,
                            selectedDifficulty = config.difficulty,
                            onGradeSelected = { flnViewModel.setWorksheetGrade(it) },
                            onDifficultySelected = { flnViewModel.setWorksheetDifficulty(it) }
                        )
                    }

                    item {
                        PreviewTabSwitcher(
                            previewTab = previewTab,
                            onTabSelected = { flnViewModel.setPreviewTab(it) }
                        )
                    }

                    if (previewTab == WorksheetPreviewTab.STUDENT_SHEET) {
                        item {
                            SimulatedA4Header(config = config)
                        }
                        item {
                            NcertInstructionBanner(type = config.type)
                        }
                        if (config.type == WorksheetType.COUNT_AND_MATCH || config.type == WorksheetType.PICTURE_WORD_MATCH) {
                            item {
                                NcertMatchingSection(items = items, config = config)
                            }
                        }
                    }

                    if (config.type != WorksheetType.COUNT_AND_MATCH && config.type != WorksheetType.PICTURE_WORD_MATCH || previewTab == WorksheetPreviewTab.TEACHER_KEY) {
                        itemsIndexed(items) { index, item ->
                            if (previewTab == WorksheetPreviewTab.STUDENT_SHEET) {
                                StudentProblemCard(index = index + 1, item = item, type = config.type)
                            } else {
                                TeacherKeyCard(index = index + 1, item = item)
                            }
                        }
                    }

                    if (previewTab == WorksheetPreviewTab.STUDENT_SHEET) {
                        item {
                            NcertLearningOutcomeCard(type = config.type, grade = config.grade)
                        }
                        item {
                            TeacherRubricCard()
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// =============================================================================
// Helper Components
// =============================================================================

@Composable
private fun LessonTopicDesignerCard(
    topicText: String,
    onTopicChange: (String) -> Unit,
    onSynthesize: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = EduPrimaryContainer),
        border = BorderStroke(1.dp, EduPrimary.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(EduPrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "AI",
                            tint = EduPrimaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ON-THE-SPOT LESSON SYNTHESIZER",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EduPrimaryDark,
                        letterSpacing = 0.5.sp
                    )
                }
                Surface(
                    color = EduPrimaryLight,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "100% OFFLINE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EduPrimaryDark,
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = "Generate bilingual practice problems aligned to NIPUN Bharat foundational outcomes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Quick Rural Theme Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val suggestions = listOf(
                    "हाट बाज़ार में फल" to "Market & Fruits",
                    "नदी और मछलियाँ" to "River & Fish",
                    "जंगल और पक्षी" to "Forest & Birds",
                    "गिनती 1 से 10" to "Counting 1 to 10",
                    "अक्षर लेखन (Ol Chiki)" to "Ol Chiki Letters"
                )
                items(suggestions) { (hindiTopic, subLabel) ->
                    Surface(
                        onClick = {
                            onTopicChange(hindiTopic)
                            onSynthesize(hindiTopic)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = hindiTopic,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = subLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = topicText,
                    onValueChange = onTopicChange,
                    placeholder = { Text("कक्षा का विषय लिखें (उदा. हाट बाज़ार)...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Button(
                    onClick = {
                        if (topicText.isNotBlank()) {
                            onSynthesize(topicText)
                        }
                    },
                    enabled = topicText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Generate", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun WorksheetTypeSelector(
    selectedType: WorksheetType,
    onTypeSelected: (WorksheetType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "WORKSHEET FORMAT (${WorksheetType.entries.size} NIPUN Formats)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(WorksheetType.entries) { type ->
                val isSelected = selectedType == type
                Card(
                    modifier = Modifier
                        .width(190.dp)
                        .clickable { onTypeSelected(type) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) EduPrimaryLight else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = type.nipunTargetCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) EduPrimaryDark else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Active",
                                    tint = EduPrimaryDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) EduPrimaryDark else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = type.santaliName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) EduPrimaryDark.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GradeAndDifficultySelectors(
    selectedGrade: FlnGrade,
    selectedDifficulty: WorksheetDifficulty,
    onGradeSelected: (FlnGrade) -> Unit,
    onDifficultySelected: (WorksheetDifficulty) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Grade Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "GRADE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FlnGrade.entries.forEach { grade ->
                    val isSel = selectedGrade == grade
                    FilterChip(
                        selected = isSel,
                        onClick = { onGradeSelected(grade) },
                        shape = RoundedCornerShape(20.dp),
                        label = {
                            Text(
                                text = grade.displayName.substringBefore(" "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSel,
                            borderColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }

        // Difficulty Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "LEVEL",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WorksheetDifficulty.entries.forEach { diff ->
                    val isSel = selectedDifficulty == diff
                    FilterChip(
                        selected = isSel,
                        onClick = { onDifficultySelected(diff) },
                        shape = RoundedCornerShape(20.dp),
                        label = {
                            Text(
                                text = diff.displayName.substringBefore(" "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EduIndigo,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSel,
                            borderColor = if (isSel) EduIndigo else MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewTabSwitcher(
    previewTab: WorksheetPreviewTab,
    onTabSelected: (WorksheetPreviewTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (previewTab == WorksheetPreviewTab.STUDENT_SHEET) MaterialTheme.colorScheme.surface else Color.Transparent)
                .clickable { onTabSelected(WorksheetPreviewTab.STUDENT_SHEET) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Student Sheet (Page 1)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (previewTab == WorksheetPreviewTab.STUDENT_SHEET) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (previewTab == WorksheetPreviewTab.TEACHER_KEY) MaterialTheme.colorScheme.surface else Color.Transparent)
                .clickable { onTabSelected(WorksheetPreviewTab.TEACHER_KEY) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Teacher Key & Phonics (Page 2)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (previewTab == WorksheetPreviewTab.TEACHER_KEY) EduAmber else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private object NcertUiPalette {
    val NavyPrimary = Color(0xFF123466)
    val NavyLight = Color(0xFFEEF4FF)
    val InstructionBg = Color(0xFFF6F9FF)
    val InstructionBorder = Color(0xFFB4CDF0)
    val OutcomeBg = Color(0xFFF0FDF4)
    val OutcomeBorder = Color(0xFFA7F3D0)
    val OutcomeText = Color(0xFF166534)
    val RubricBg = Color(0xFFFFFBEB)
    val RubricBorder = Color(0xFFFDE68A)
    val RubricText = Color(0xFF92400E)
    val MatchingDot = Color(0xFF123466)
}

@Composable
private fun SimulatedA4Header(config: WorksheetConfig) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = NcertUiPalette.NavyLight,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, NcertUiPalette.NavyPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NCERT • राष्ट्रीय साक्षरता केंद्र (CNCL) • ᱡᱟᱹᱛᱤᱭᱟᱹᱨᱤ ᱥᱟᱠᱷᱚᱨᱛᱟ ᱛᱟᱞᱢᱟ",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = NcertUiPalette.NavyPrimary
                    )
                    Text(
                        text = "बुनियादी साक्षरता एवं संख्या-ज्ञान (FLN) • ᱮᱛᱚᱦᱚᱵ ᱥᱟᱠᱷᱚᱨᱛᱟ ᱟᱨ ᱮᱞᱠᱷᱟ-ᱜᱮᱭᱟᱱ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = NcertUiPalette.NavyPrimary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "कार्य-पत्रक 1 • ᱠᱟᱹᱢᱤ-ᱥᱟᱠᱟᱢ ᱑",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "कक्षा: ${config.grade.hindiName}",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // 4-field student details box in 2x2 grid
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "शिक्षार्थी का नाम / ᱧᱩᱛᱩᱢ: ________________________",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "दिनांक / ᱢᱟᱹᱦᱤᱛ: _________",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "कक्षा / ᱪᱟᱱᱟᱪ: _______________________________",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "क्रमांक / ᱮᱞ: _________",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NcertInstructionBanner(type: WorksheetType) {
    val (hiInstr, satInstr) = org.tribetalk.fln.worksheet.WorksheetPdfExporter.getBilingualInstruction(type)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = NcertUiPalette.InstructionBg,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, NcertUiPalette.InstructionBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = hiInstr,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NcertUiPalette.NavyPrimary
            )
            Text(
                text = satInstr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NcertLearningOutcomeCard(type: WorksheetType, grade: FlnGrade) {
    val (hiOutcome, satOutcome) = org.tribetalk.fln.worksheet.WorksheetPdfExporter.getBilingualLearningOutcome(type, grade)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = NcertUiPalette.OutcomeBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NcertUiPalette.OutcomeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "★ हमने सीखा (Learning Outcome) • ᱟᱵᱚ ᱵᱚ ᱪᱮᱫ ᱠᱮᱫᱟ:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NcertUiPalette.OutcomeText
            )
            Text(
                text = hiOutcome,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = satOutcome,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NcertMatchingSection(items: List<WorksheetItem>, config: WorksheetConfig) {
    val count = items.size.coerceIn(3, 6)
    val random = remember(config.seed) { java.util.Random(config.seed + 99) }
    val shuffledIndices = remember(config.seed, count) { (0 until count).shuffled(random) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Two Column Headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "स्तंभ 'क' (Column A) • ᱠᱷᱟᱸᱫᱷᱟ 'A'",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NcertUiPalette.NavyPrimary,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "स्तंभ 'ख' (Column B) • ᱠᱷᱟᱸᱫᱷᱟ 'B'",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NcertUiPalette.NavyPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        for (i in 0 until count) {
            val itemA = items[i]
            val targetIdx = shuffledIndices.getOrElse(i) { i }
            val itemB = items[targetIdx]

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Column A Item Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(NcertUiPalette.NavyPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${i + 1}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            val qty = itemA.quantity.coerceIn(1, 8)
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                for (q in 0 until qty) {
                                    WorksheetSvgToken(
                                        iconType = itemA.iconType,
                                        imageAssetPath = itemA.imageAssetPath,
                                        size = 20.dp
                                    )
                                }
                            }
                        }
                        // Connection Dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(NcertUiPalette.MatchingDot)
                        )
                    }
                }

                // Connecting space
                Text(
                    text = " . . ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                // Column B Item Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Connection Dot
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(NcertUiPalette.MatchingDot)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val label = if (config.type == WorksheetType.PICTURE_WORD_MATCH) {
                                "${itemB.rightLabelSantali} • ${itemB.leftLabelHindi}"
                            } else {
                                "${itemB.quantity} (${org.tribetalk.fln.repository.FlnCurriculumRepository.toOlChikiDigits(itemB.quantity)})"
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Answer Box [  ]
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = " [   ] ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeacherRubricCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = NcertUiPalette.RubricBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NcertUiPalette.RubricBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = NcertUiPalette.RubricText,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "शिक्षक मूल्यांकन (Teacher Evaluation Rubric)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NcertUiPalette.RubricText
                    )
                }
                Text(
                    text = "★ ★ ★ ★ ★",
                    color = NcertUiPalette.RubricText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            Text(
                text = "[  ] आरम्भिक (Emerging)    [  ] प्रगतिशील (Developing)    [  ] दक्ष (Proficient)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "हस्ताक्षर (Teacher Signature): _______________________",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun WorksheetSvgToken(
    iconType: String,
    imageAssetPath: String? = null,
    size: androidx.compose.ui.unit.Dp = 26.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val svgPath = remember(iconType, imageAssetPath) {
        if (imageAssetPath != null && imageAssetPath.endsWith(".svg")) {
            imageAssetPath
        } else {
            val key = SvgCorpusRegistry.findMatchingKey(iconType) ?: iconType
            SvgCorpusRegistry.get(key)?.relativeFilePath
        }
    }

    if (svgPath != null) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/$svgPath")
                .decoderFactory(SvgDecoder.Factory())
                .build(),
            contentDescription = iconType,
            modifier = modifier.size(size)
        )
    } else {
        val bitmap = rememberFlnImage(imageAssetPath)
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = iconType,
                modifier = modifier.size(size)
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Circle,
                contentDescription = iconType,
                tint = MaterialTheme.colorScheme.primary,
                modifier = modifier.size(size * 0.75f)
            )
        }
    }
}

@Composable
private fun StudentProblemCard(index: Int, item: WorksheetItem, type: WorksheetType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(EduPrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EduPrimaryDark
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.nipunCode,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EduIndigo
                    )
                }
            }

            // Dual Prompt: Santali Ol Chiki + Devanagari Hindi
            if (item.promptSantali.isNotBlank()) {
                Text(
                    text = item.promptSantali,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EduPrimaryDark
                )
            }
            Text(
                text = item.promptHindi,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Problem Visuals
            when (type) {
                WorksheetType.AKSHAR_TRACING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.leftLabelHindi,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = "Practice: . . .   . . .   . . .",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                WorksheetType.COUNT_AND_MATCH -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val count = item.quantity.coerceIn(1, 12)
                        val itemsPerRow = if (count > 6) 5 else count
                        val rows = (0 until count).chunked(itemsPerRow)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            rows.forEach { rowIndices ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    rowIndices.forEach { _ ->
                                        WorksheetSvgToken(
                                            iconType = item.iconType,
                                            imageAssetPath = item.imageAssetPath,
                                            size = 26.dp
                                        )
                                    }
                                }
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = "Numeral: [ ? ]",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                WorksheetType.ADDITION_WORD_PROBLEM -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 0 until item.quantity) {
                                WorksheetSvgToken(
                                    iconType = item.iconType,
                                    imageAssetPath = item.imageAssetPath,
                                    size = 24.dp
                                )
                            }
                        }
                        Text("  +  ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EduPrimaryDark)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 0 until item.secondaryQuantity) {
                                WorksheetSvgToken(
                                    iconType = item.iconType,
                                    imageAssetPath = item.imageAssetPath,
                                    size = 24.dp
                                )
                            }
                        }
                        Text("  =  [ ? ]", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                WorksheetType.SUBTRACTION_PROBLEM -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val total = item.quantity.coerceIn(1, 10)
                        val sub = item.secondaryQuantity.coerceIn(1, total)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 0 until total) {
                                val isSubtracted = i >= total - sub
                                Box(contentAlignment = Alignment.Center) {
                                    WorksheetSvgToken(
                                        iconType = item.iconType,
                                        imageAssetPath = item.imageAssetPath,
                                        size = 26.dp,
                                        modifier = if (isSubtracted) Modifier.clip(CircleShape) else Modifier
                                    )
                                    if (isSubtracted) {
                                        Text("✕", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                        Text("  -  $sub  =  [ ? ]", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                WorksheetType.MULTIPLICATION_GROUPS -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val groups = item.quantity.coerceIn(2, 5)
                        val perGroup = item.secondaryQuantity.coerceIn(1, 5)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(groups) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EduPrimaryLight.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, EduPrimary.copy(alpha = 0.4f)),
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        repeat(perGroup) {
                                            WorksheetSvgToken(
                                                iconType = item.iconType,
                                                imageAssetPath = item.imageAssetPath,
                                                size = 20.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Text("  =  [ ? ]", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                WorksheetType.MONEY_COUNTING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WorksheetSvgToken(iconType = "coin_${item.quantity}", size = 36.dp)
                        Text("  +  ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EduPrimaryDark)
                        WorksheetSvgToken(iconType = "coin_${item.secondaryQuantity}", size = 36.dp)
                        Text("  =  ₹ [ ? ]", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                WorksheetType.NUMBER_SEQUENCE_TRAIN -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item.sequenceItems.forEachIndexed { seqIdx, seqVal ->
                            val isMissing = seqIdx == item.missingSequenceIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isMissing) EduPrimaryLight else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        width = if (isMissing) 1.5.dp else 1.dp,
                                        color = if (isMissing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isMissing) "[ ? ]" else seqVal,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMissing) EduPrimaryDark else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                WorksheetType.MISSING_AKSHAR_SPELLING -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Word: ${item.wordWithBlank ?: ""}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item.options.forEach { optChar ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = optChar,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                WorksheetType.PICTURE_WORD_MATCH -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WorksheetSvgToken(iconType = item.iconType, imageAssetPath = item.imageAssetPath, size = 32.dp)
                            Text(
                                text = item.leftLabelHindi,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = item.rightLabelSantali,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeacherKeyCard(index: Int, item: WorksheetItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, EduAmber.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Problem $index Solution",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = EduAmber
                )
                Text(
                    text = item.nipunCode,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Verified Answer: ${item.teacherSolutionNote}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = EduAmberLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Teacher Pronunciation: ${item.teacherPhoneticAnswer}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = EduAmber,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun WorksheetExportButton(
    isGeneratingPdf: Boolean,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 48.dp
) {
    Button(
        onClick = onExportPdf,
        enabled = !isGeneratingPdf,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        )
    ) {
        if (isGeneratingPdf) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Generating Dual-Sheet PDF...", color = Color.White, fontWeight = FontWeight.Bold)
        } else {
            Icon(
                imageVector = Icons.Rounded.Print,
                contentDescription = "Print PDF",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Export & Print 2-Page A4 PDF",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun WorksheetBottomExportBar(
    isGeneratingPdf: Boolean,
    isCompact: Boolean,
    onExportPdf: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = if (isCompact) 8.dp else 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WorksheetExportButton(
                isGeneratingPdf = isGeneratingPdf,
                onExportPdf = onExportPdf,
                height = if (isCompact) 42.dp else 48.dp
            )
            if (!isCompact) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Page 1: Student Sheet  •  Page 2: Teacher Answer Key & Phonics",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
