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
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.fln.model.FlnGrade
import org.tribetalk.fln.model.WorksheetDifficulty
import org.tribetalk.fln.model.WorksheetItem
import org.tribetalk.fln.model.WorksheetType
import org.tribetalk.ui.components.FlnVectorGraphic
import org.tribetalk.ui.theme.DarkBorder
import org.tribetalk.ui.theme.DarkBorderGreen
import org.tribetalk.ui.theme.DarkCard
import org.tribetalk.ui.theme.DarkSurface
import org.tribetalk.ui.theme.EmeraldGreen
import org.tribetalk.ui.theme.EmeraldMint
import org.tribetalk.ui.theme.PureBlack
import org.tribetalk.ui.theme.PureWhite
import org.tribetalk.ui.theme.WhiteSecondary

/**
 * NIPUN Bharat bilingual worksheet studio.
 * 8 High-Impact Worksheet Types, Dual-Sheet Vector PDF Exporter, and Teacher Phonetics Key.
 * High-contrast Green, White, and Black styling.
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PureBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NIPUN Worksheet Studio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite
                        )
                        Text(
                            text = "Foundational Literacy & Numeracy • 2-Page Offline PDF",
                            style = MaterialTheme.typography.bodySmall,
                            color = WhiteSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { flnViewModel.regenerateWorksheet() }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "New Problems",
                            tint = EmeraldGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureBlack
                )
            )
        },
        bottomBar = {
            Surface(
                color = PureBlack,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = DarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { flnViewModel.exportAndSharePdf(context) },
                        enabled = !isGeneratingPdf,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = PureBlack
                        )
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = PureBlack,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Generating Dual-Sheet PDF...",
                                color = PureBlack,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Print,
                                contentDescription = "Print PDF",
                                tint = PureBlack,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Export & Print 2-Page A4 PDF",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PureBlack
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Page 1: Student Sheet  •  Page 2: Teacher Answer Key & Pronunciation",
                        style = MaterialTheme.typography.labelSmall,
                        color = WhiteSecondary
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PureBlack)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // 8 Worksheet Types Horizontal Carousel
                Text(
                    text = "SELECT WORKSHEET FORMAT (${WorksheetType.entries.size} NIPUN Types)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldMint,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(WorksheetType.entries) { type ->
                        val isSelected = config.type == type
                        Card(
                            modifier = Modifier
                                .width(200.dp)
                                .clickable { flnViewModel.setWorksheetType(type) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) EmeraldGreen.copy(alpha = 0.15f) else DarkCard
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) EmeraldGreen else DarkBorder
                            )
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
                                        color = if (isSelected) EmeraldGreen else WhiteSecondary
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "Active",
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                                Text(
                                    text = type.santaliName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldMint
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Configuration Bar: Grade & Difficulty Chips
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Grade Level
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Grade Level:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = WhiteSecondary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FlnGrade.entries.forEach { grade ->
                                    val isSel = config.grade == grade
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { flnViewModel.setWorksheetGrade(grade) },
                                        label = {
                                            Text(
                                                text = grade.displayName.substringBefore(" "),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = EmeraldGreen,
                                            selectedLabelColor = PureBlack,
                                            containerColor = PureBlack,
                                            labelColor = PureWhite
                                        )
                                    )
                                }
                            }
                        }

                        // Difficulty Level
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Difficulty:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = WhiteSecondary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                WorksheetDifficulty.entries.forEach { diff ->
                                    val isSel = config.difficulty == diff
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { flnViewModel.setWorksheetDifficulty(diff) },
                                        label = {
                                            Text(
                                                text = diff.displayName.substringBefore(" "),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = EmeraldGreen,
                                            selectedLabelColor = PureBlack,
                                            containerColor = PureBlack,
                                            labelColor = PureWhite
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dual-Tab Switcher: Student Sheet vs Teacher Key
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (previewTab == WorksheetPreviewTab.STUDENT_SHEET) EmeraldGreen else Color.Transparent)
                            .clickable { flnViewModel.setPreviewTab(WorksheetPreviewTab.STUDENT_SHEET) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Student Sheet (Page 1)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (previewTab == WorksheetPreviewTab.STUDENT_SHEET) PureBlack else PureWhite
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (previewTab == WorksheetPreviewTab.TEACHER_KEY) EmeraldGreen else Color.Transparent)
                            .clickable { flnViewModel.setPreviewTab(WorksheetPreviewTab.TEACHER_KEY) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Teacher Key & Phonics (Page 2)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (previewTab == WorksheetPreviewTab.TEACHER_KEY) PureBlack else PureWhite
                        )
                    }
                }
            }

            // Render Items based on selected Tab
            itemsIndexed(items) { index, item ->
                if (previewTab == WorksheetPreviewTab.STUDENT_SHEET) {
                    StudentProblemCard(index = index + 1, item = item, config = config)
                } else {
                    TeacherKeyCard(index = index + 1, item = item)
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun StudentProblemCard(
    index: Int,
    item: WorksheetItem,
    config: org.tribetalk.fln.model.WorksheetConfig
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row with Problem # and NIPUN Target
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = PureBlack
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item.nipunCode,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldMint
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prompts
            Text(
                text = item.promptHindi,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PureWhite
            )
            if (item.promptSantali.isNotEmpty()) {
                Text(
                    text = item.promptSantali,
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldMint
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Problem-specific visual layout
            when (config.type) {
                WorksheetType.COUNT_AND_MATCH -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            for (i in 0 until item.quantity.coerceAtMost(8)) {
                                FlnVectorGraphic(
                                    iconType = item.iconType,
                                    size = 28.dp,
                                    tint = EmeraldGreen
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .border(1.dp, DarkBorderGreen, RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${item.rightLabelSantali} [ ${item.leftLabelHindi} ]",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                    }
                }

                WorksheetType.PICTURE_WORD_MATCH -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FlnVectorGraphic(iconType = item.iconType, size = 42.dp, tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item.leftLabelHindi,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                        Box(
                            modifier = Modifier
                                .border(1.dp, DarkBorderGreen, RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = item.rightLabelSantali,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldMint
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
                                FlnVectorGraphic(iconType = item.iconType, size = 26.dp, tint = EmeraldGreen)
                            }
                        }
                        Text("  +  ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PureWhite)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 0 until item.secondaryQuantity) {
                                FlnVectorGraphic(iconType = item.iconType, size = 26.dp, tint = Color(0xFF60A5FA))
                            }
                        }
                        Text("  =  ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PureWhite)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.5.dp, DarkBorderGreen, RoundedCornerShape(8.dp))
                                .background(DarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("?", color = WhiteSecondary, fontWeight = FontWeight.Bold)
                        }
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
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isMissing) EmeraldGreen.copy(alpha = 0.1f) else DarkSurface)
                                    .border(
                                        width = if (isMissing) 1.5.dp else 1.dp,
                                        color = if (isMissing) EmeraldGreen else DarkBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isMissing) "[ ? ]" else seqVal,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMissing) EmeraldMint else PureWhite
                                )
                            }
                        }
                    }
                }

                WorksheetType.GREATER_LESSER_COMPARE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Count
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 0 until item.quantity) {
                                FlnVectorGraphic(iconType = item.iconType, size = 26.dp, tint = EmeraldGreen)
                            }
                        }

                        // Middle Circle
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(DarkSurface)
                                .border(1.5.dp, EmeraldGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(">", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldMint)
                        }

                        // Right Count
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 0 until item.secondaryQuantity) {
                                FlnVectorGraphic(iconType = item.iconType, size = 26.dp, tint = Color(0xFF60A5FA))
                            }
                        }
                    }
                }

                WorksheetType.MISSING_AKSHAR_SPELLING -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Word: ${item.wordWithBlank ?: ""}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item.options.forEach { optChar ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurface)
                                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = optChar,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                }
                            }
                        }
                    }
                }

                WorksheetType.AKSHAR_TRACING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.leftLabelHindi,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Box(
                            modifier = Modifier
                                .border(1.dp, DarkBorderGreen, RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Practice: . . .   . . .   . . .",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WhiteSecondary
                            )
                        }
                    }
                }

                WorksheetType.ASSESSMENT_CIRCLE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item.options.forEachIndexed { optIdx, optText ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, EmeraldGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = optText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PureWhite
                                )
                            }
                        }
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Problem $index Solution Key",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldMint
                )
                Text(
                    text = item.nipunCode,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )
            }

            // Solution Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface)
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Verified Answer:",
                        style = MaterialTheme.typography.labelSmall,
                        color = WhiteSecondary
                    )
                    Text(
                        text = item.teacherSolutionNote.ifEmpty { item.rightLabelSantali },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }

            // Phonetics pronunciation guide
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmeraldGreen.copy(alpha = 0.1f))
                    .border(1.dp, EmeraldGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.RecordVoiceOver,
                        contentDescription = "Pronounce",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Teacher Pronunciation (कक्षा में ऐसे बोलें):",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldMint
                        )
                        Text(
                            text = item.teacherPhoneticAnswer.ifEmpty { item.rightLabelSantali },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }
                }
            }
        }
    }
}
