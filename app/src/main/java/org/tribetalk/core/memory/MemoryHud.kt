package org.tribetalk.core.memory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Adaptive Real-Time Memory Consumption Monitor.
 * Automatically adapts budget and headroom metrics to the host device's physical hardware RAM
 * (e.g. 2 GB budget for 8 GB devices, 520 MB budget for 2 GB tablets).
 * Features a visual progress meter and collapsible HUD.
 */
@Composable
fun MemoryHudOverlay(governor: MemoryGovernor) {
    var snapshot by remember { mutableStateOf<MemorySnapshot?>(null) }
    var resident by remember { mutableStateOf(governor.residentStages) }
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(governor) {
        while (true) {
            governor.refresh()
            snapshot = governor.lastSnapshot
            resident = governor.residentStages
            delay(1_000L)
        }
    }

    val snap = snapshot ?: return
    val totalRamMb = snap.systemTotalMb
    val totalRamGbStr = String.format(Locale.US, "%.1f GB", totalRamMb / 1024f)
    val budgetMb = governor.currentAppBudgetMb
    val headroomThreshold = governor.currentSafetyHeadroomMb

    val appRatio = (snap.appFootprintMb / budgetMb.toFloat()).coerceIn(0f, 1f)
    val appPercent = (appRatio * 100).toInt()

    val meterColor = when {
        snap.systemHeadroomMb < headroomThreshold -> Color(0xFFFF5252) // Critical LMK zone
        appRatio > 0.85f -> Color(0xFFFFB74D) // Approaching budget
        else -> Color(0xFF69F0AE) // Healthy
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xE60D1B2A))
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .animateContentSize()
    ) {
        // Summary Bar (Always Visible)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Memory,
                    contentDescription = "Memory Meter",
                    tint = meterColor,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "SYS $totalRamGbStr RAM",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• App ${snap.appFootprintMb}/$budgetMb MB ($appPercent%)",
                    color = meterColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (resident.isEmpty()) "IDLE" else resident.joinToString { it.name },
                    color = if (resident.isEmpty()) Color(0xFF90CAF9) else Color(0xFFFFD54F),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Toggle",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Visual consumption meter bar
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { appRatio },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = meterColor,
            trackColor = Color(0x33FFFFFF)
        )

        // Detailed Hardware Diagnostics (Expandable on tap)
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Breakdown: Native ${snap.nativePssMb} MB | Java ${snap.javaHeapUsedMb}/${snap.javaHeapMaxMb} MB | Adaptive Budget: $budgetMb MB",
                    color = Color(0xFFB0BEC5),
                    fontSize = 9.5.sp,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "System RAM: Used ${snap.systemUsedMb}/$totalRamMb MB (${snap.systemUsedPercent}%) | Avail ${snap.systemAvailMb} MB | LMK Headroom ${snap.systemHeadroomMb} MB (thr ${snap.systemThresholdMb})",
                    color = if (snap.systemHeadroomMb < headroomThreshold) Color(0xFFFF5252) else Color(0xFF81D4FA),
                    fontSize = 9.5.sp,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
