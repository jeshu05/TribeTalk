package org.tribetalk.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.tribetalk.ui.theme.StatusListening
import org.tribetalk.ui.theme.StatusReady
import org.tribetalk.ui.theme.StatusSynthesizing
import org.tribetalk.ui.theme.StatusTranscribing
import org.tribetalk.ui.theme.StatusTranslating

enum class PipelineUiState(val label: String) {
    IDLE("Ready"),
    LISTENING("Listening"),
    TRANSCRIBING("Transcribing Speech"),
    TRANSLATING("Translating Text"),
    SYNTHESIZING("Synthesizing Audio"),
    PLAYING("Playing Audio"),
    ERROR("Error Occurred")
}

/**
 * Status indicator displaying live pipeline execution state.
 * Uses a breathing dot animation and color-coded chips without emojis.
 */
@Composable
fun StatusIndicator(
    state: PipelineUiState,
    modifier: Modifier = Modifier
) {
    val targetColor = when (state) {
        PipelineUiState.IDLE -> StatusReady
        PipelineUiState.LISTENING -> StatusListening
        PipelineUiState.TRANSCRIBING -> StatusTranscribing
        PipelineUiState.TRANSLATING -> StatusTranslating
        PipelineUiState.SYNTHESIZING -> StatusSynthesizing
        PipelineUiState.PLAYING -> StatusTranslating
        PipelineUiState.ERROR -> Color(0xFFEF4444)
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300),
        label = "status_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_scale")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = if (state == PipelineUiState.IDLE) 1.0f else 0.8f,
        targetValue = if (state == PipelineUiState.IDLE) 1.0f else 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = animatedColor.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(dotScale)
                    .background(animatedColor, shape = CircleShape)
            )
            Text(
                text = state.label,
                style = MaterialTheme.typography.labelSmall,
                color = animatedColor
            )
        }
    }
}
