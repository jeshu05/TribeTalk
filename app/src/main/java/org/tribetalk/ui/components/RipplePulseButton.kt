package org.tribetalk.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.tribetalk.ui.theme.StatusListening

/**
 * Concentric ripple pulse recording button.
 * Expands animated ripple waves during recording with smooth haptic-ready states.
 * Strictly uses Material vector icons without any unicode emojis.
 */
@Composable
fun RipplePulseButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple_waves")

    val ripple1Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_1"
    )

    val ripple1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha_1"
    )

    val ripple2Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_2"
    )

    val ripple2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha_2"
    )

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isRecording) {
            // Outermost Ripple Ring
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(ripple2Scale)
                    .background(
                        StatusListening.copy(alpha = ripple2Alpha),
                        shape = CircleShape
                    )
            )

            // Inner Ripple Ring
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(ripple1Scale)
                    .background(
                        StatusListening.copy(alpha = ripple1Alpha),
                        shape = CircleShape
                    )
            )
        }

        // Main Action Button
        val buttonColor = if (isRecording) StatusListening else MaterialTheme.colorScheme.primary
        val interactionSource = remember { MutableInteractionSource() }

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(buttonColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
                contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
