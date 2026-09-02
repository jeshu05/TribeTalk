package com.alchemists.tribetalk.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alchemists.tribetalk.ui.viewmodel.TeacherAnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealtimeTeacherDashboardScreen(
    viewModel: TeacherAnalyticsViewModel,
    onStartRecommendedActivity: (String) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val summary by viewModel.summaryState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAnalytics(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Teacher Analytics Dashboard",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            "Class 2A • Dynamic Performance & Learning Outcomes",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.clearAllData(context) }) {
                        Text("Reset Data", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Check for Zero / Empty State
            if (summary.totalQuestionsAttempted == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No assessment data recorded yet.",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Select a lesson and start a classroom assessment activity to record student responses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                return@Scaffold
            }

            // 1. CLASS OVERVIEW METRICS
            Text(
                text = "CLASS OVERVIEW (CLASS 2A)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard("Learners", "${summary.totalStudentsAssessed}", Modifier.weight(1f))
                MetricCard("Attempted", "${summary.totalQuestionsAttempted}", Modifier.weight(1f))
                MetricCard("Correct", "${summary.totalCorrect}", Modifier.weight(1f))
                MetricCard("Accuracy", "${summary.classAccuracyPercentage.toInt()}%", Modifier.weight(1f))
            }

            // 2. AREAS NEEDING ATTENTION & FOLLOW-UP RECOMMENDATIONS
            if (summary.recommendations.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Attention", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AREAS NEEDING ATTENTION", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                        }

                        summary.recommendations.forEach { rec ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("• ${rec.weakSkill} (${rec.accuracyPercentage.toInt()}% Accuracy)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Recommended Follow-up: ${rec.recommendedActivityTitle}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)

                                Button(
                                    onClick = { onStartRecommendedActivity(rec.recommendedActivityId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Start Follow-up Activity", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Start")
                                }
                            }
                        }
                    }
                }
            }

            // 3. LEARNING OUTCOME PERFORMANCE & PROGRESS
            Text(
                text = "NIPUN BHARAT LEARNING OUTCOMES",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )

            summary.outcomeProgressList.forEach { lo ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lo.topic, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("${lo.masteryThreshold.badgeEmoji} ${lo.masteryThreshold.label}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Accuracy: ${lo.accuracyPercentage.toInt()}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            if (lo.previousAccuracy != null) {
                                Text(
                                    "Progress: ${lo.previousAccuracy.toInt()}% ➜ ${lo.accuracyPercentage.toInt()}% (${if (lo.improvementPoints >= 0) "+" else ""}${lo.improvementPoints.toInt()} pts)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { lo.accuracyPercentage / 100.0f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    }
                }
            }

            // 4. INDIVIDUAL STUDENT ROSTER BREAKDOWN TABLE
            Text(
                text = "STUDENT PERFORMANCE BREAKDOWN",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )

            summary.studentSummaries.forEach { st ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(st.studentId, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("${st.correctCount} / ${st.totalAttempted} Correct", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                        Text(
                            text = "${st.accuracyPercentage.toInt()}% ${st.masteryThreshold.badgeEmoji}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        }
    }
}
