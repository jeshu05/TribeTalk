package org.tribetalk.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.ui.theme.EduPrimary

/**
 * Direction selector pill cleanly toggling between:
 * Hindi (Devanagari) -> Santali (Ol Chiki) and Santali -> Hindi.
 * Designed with a clean, modern educational aesthetic.
 */
@Composable
fun LanguageSelectorPill(
    isHindiToSantali: Boolean,
    onSwapDirection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rotationState by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotationState,
        animationSpec = tween(durationMillis = 300),
        label = "swap_rotation"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(32.dp)
            ),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Source Language Tag
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isHindiToSantali) "हिन्दी" else "ᱥᱟᱱᱛᱟᱲᱤ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp
                )
                Text(
                    text = if (isHindiToSantali) "Hindi" else "Santali",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Interactive Swap Button
            IconButton(
                onClick = {
                    rotationState += 180f
                    onSwapDirection()
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(EduPrimary)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SwapHoriz,
                    contentDescription = "Swap translation direction",
                    tint = Color.White,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(animatedRotation)
                )
            }

            // Target Language Tag
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isHindiToSantali) "ᱥᱟᱱᱛᱟᱲᱤ" else "हिन्दी",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EduPrimary,
                    fontSize = 17.sp
                )
                Text(
                    text = if (isHindiToSantali) "Santali" else "Hindi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

