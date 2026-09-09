package org.tribetalk.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import org.tribetalk.fln.model.WorksheetItem
import org.tribetalk.fln.model.WorksheetType
import org.tribetalk.ui.components.FlnVectorGraphic
import org.tribetalk.ui.theme.DarkBorder
import org.tribetalk.ui.theme.DarkBorderGreen
import org.tribetalk.ui.theme.DarkCard
import org.tribetalk.ui.theme.DarkSurface
import org.tribetalk.ui.theme.EmeraldGreen
import org.tribetalk.ui.theme.PureBlack
import org.tribetalk.ui.theme.PureWhite
import org.tribetalk.ui.theme.WhiteSecondary

/**
 * NIPUN Bharat bilingual worksheet studio.
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
    val isGeneratingPdf by flnViewModel.isGeneratingPdf.collectAsState()
    val lastPdf by flnViewModel.lastGeneratedPdf.collectAsState()

    var showTypeDropdown by remember { mutableStateOf(false) }
    var showGradeDropdown by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PureBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Worksheet Studio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite
                        )
                        Text(
                            text = "NIPUN Bharat Bilingual Generator • Offline A4",
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
                                "Generating Printable PDF...",
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
                                text = "Export & Print A4 PDF",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PureBlack
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "100% Offline • Ready to print or share via Bluetooth / WhatsApp",
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
                // Configuration Controls
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkCard
                    ),
                    border = BorderStroke(1.dp, DarkBorderGreen)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Worksheet Parameters",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Type Selector
                            Box(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = { showTypeDropdown = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkSurface,
                                        contentColor = PureWhite
                                    ),
                                    border = BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ListAlt,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (config.type) {
                                            WorksheetType.COUNT_AND_MATCH -> "Count & Match"
                                            WorksheetType.PICTURE_WORD_MATCH -> "Word & Picture"
                                            WorksheetType.AKSHAR_TRACING -> "Letter Tracing"
                                            WorksheetType.ASSESSMENT_CIRCLE -> "Multiple Choice"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PureWhite,
                                        maxLines = 1
                                    )
                                }

                                DropdownMenu(
                                    expanded = showTypeDropdown,
                                    onDismissRequest = { showTypeDropdown = false }
                                ) {
                                    WorksheetType.values().forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type.displayName) },
                                            onClick = {
                                                flnViewModel.setWorksheetType(type)
                                                showTypeDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Grade Selector
                            Box(modifier = Modifier.weight(0.9f)) {
                                Button(
                                    onClick = { showGradeDropdown = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkSurface,
                                        contentColor = PureWhite
                                    ),
                                    border = BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.School,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (config.grade) {
                                            FlnGrade.BALVATIKA -> "Balvatika"
                                            FlnGrade.GRADE_1 -> "Grade 1"
                                            FlnGrade.GRADE_2 -> "Grade 2"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PureWhite,
                                        maxLines = 1
                                    )
                                }

                                DropdownMenu(
                                    expanded = showGradeDropdown,
                                    onDismissRequest = { showGradeDropdown = false }
                                ) {
                                    FlnGrade.values().forEach { grade ->
                                        DropdownMenuItem(
                                            text = { Text(grade.displayName) },
                                            onClick = {
                                                flnViewModel.setWorksheetGrade(grade)
                                                showGradeDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = config.type.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = WhiteSecondary
                        )
                    }
                }
            }

            // Sheet Paper Preview Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sheet Preview (Print Layout)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )

                    AssistChip(
                        onClick = { flnViewModel.regenerateWorksheet() },
                        label = { Text("Shuffle Items", color = EmeraldGreen) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = DarkCard
                        ),
                        border = BorderStroke(1.dp, DarkBorderGreen)
                    )
                }
            }

            // Printable Paper Container (Crisp White Paper with Black Typography and Green Accents)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Official Worksheet Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TRIBETALK • NIPUN BHARAT FLN BILINGUAL WORKSHEET",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00E676)
                                )
                                Text(
                                    text = "${config.type.displayName}  |  ${config.grade.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Student metadata line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Name: ________________", style = MaterialTheme.typography.bodySmall, color = Color(0xFF0F172A))
                            Text("Date: ________", style = MaterialTheme.typography.bodySmall, color = Color(0xFF0F172A))
                            Text("Roll: ____", style = MaterialTheme.typography.bodySmall, color = Color(0xFF0F172A))
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFCBD5E1)
                        )

                        // Problem Items
                        items.forEachIndexed { index, item ->
                            WorksheetItemRow(
                                index = index,
                                item = item,
                                type = config.type
                            )
                            if (index < items.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = Color(0xFFF1F5F9)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFF0F172A), thickness = 1.5.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "NIPUN Bharat Target: Foundational Literacy & Numeracy • Generated 100% Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF059669),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun WorksheetItemRow(
    index: Int,
    item: WorksheetItem,
    type: WorksheetType
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Question number
        Text(
            text = "${index + 1}.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.width(28.dp)
        )

        when (type) {
            WorksheetType.COUNT_AND_MATCH -> {
                // Visual dots / shapes in green
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until item.quantity) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                                .border(1.dp, Color(0xFF059669), CircleShape)
                        )
                    }
                }

                Text(
                    text = "• - - - - •",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )

                Text(
                    text = "${item.rightLabelSantali}  [ ${item.leftLabelHindi} ]",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                )
            }

            WorksheetType.PICTURE_WORD_MATCH -> {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FlnVectorGraphic(
                        iconType = item.iconType,
                        size = 32.dp,
                        tint = Color(0xFF059669)
                    )
                    Text(
                        text = item.leftLabelHindi,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Text(
                    text = "• - - - - •",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )

                Text(
                    text = item.rightLabelSantali,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF059669),
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(start = 12.dp)
                )
            }

            WorksheetType.AKSHAR_TRACING -> {
                Text(
                    text = item.leftLabelHindi,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF059669)),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(38.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Practice: . . . . . .",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF059669),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            WorksheetType.ASSESSMENT_CIRCLE -> {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.prompt,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item.options.forEach { opt ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .border(1.5.dp, Color(0xFF059669), CircleShape)
                                )
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
