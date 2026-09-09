package org.tribetalk.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Beautiful, hardware-accelerated audio waveform visualizer.
 * Animates responsive equalizer bars based on live audio amplitude and state.
 */
@Composable
fun WaveformVisualizer(
    amplitude: Float,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 28
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_phase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = width / (barCount * 1.6f)
        val spacing = (width - barWidth * barCount) / (barCount - 1)
        val cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)

        for (i in 0 until barCount) {
            val normalizedIdx = i.toFloat() / barCount
            val sineFactor = (sin(phase + normalizedIdx * 4.0).toFloat() + 1f) / 2f

            val baseHeight = if (isActive) {
                val dynamicAmp = amplitude.coerceIn(0.12f, 1.0f)
                val barAmp = (0.25f + 0.75f * sineFactor) * dynamicAmp
                (height * barAmp).coerceIn(6.dp.toPx(), height)
            } else {
                4.dp.toPx()
            }

            val xOffset = i * (barWidth + spacing)
            val yOffset = (height - baseHeight) / 2f

            drawRoundRect(
                color = if (isActive) barColor else barColor.copy(alpha = 0.25f),
                topLeft = Offset(xOffset, yOffset),
                size = Size(barWidth, baseHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}
