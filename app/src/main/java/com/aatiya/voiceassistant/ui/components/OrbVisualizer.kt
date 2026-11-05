package com.aatiya.voiceassistant.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aatiya.voiceassistant.domain.model.AssistantState
import com.aatiya.voiceassistant.ui.theme.AccentRose
import com.aatiya.voiceassistant.ui.theme.PrimaryCyan
import com.aatiya.voiceassistant.ui.theme.PrimaryPurple
import com.aatiya.voiceassistant.ui.theme.StateConnecting
import com.aatiya.voiceassistant.ui.theme.StateError
import com.aatiya.voiceassistant.ui.theme.StateListening
import com.aatiya.voiceassistant.ui.theme.StateProcessing
import com.aatiya.voiceassistant.ui.theme.StateSpeaking
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OrbVisualizer(
    state: AssistantState,
    amplitude: Float,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbMotion")

    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple"
    )

    val targetPrimaryColor = when (state) {
        AssistantState.IDLE -> PrimaryPurple
        AssistantState.CONNECTING -> StateConnecting
        AssistantState.LISTENING -> StateListening
        AssistantState.PROCESSING -> StateProcessing
        AssistantState.SPEAKING -> StateSpeaking
        AssistantState.ERROR -> StateError
    }

    val targetSecondaryColor = when (state) {
        AssistantState.IDLE -> PrimaryCyan
        AssistantState.CONNECTING -> Color(0xFFFBBF24)
        AssistantState.LISTENING -> Color(0xFF34D399)
        AssistantState.PROCESSING -> PrimaryPurple
        AssistantState.SPEAKING -> AccentRose
        AssistantState.ERROR -> Color(0xFFB91C1C)
    }

    val primaryColor by animateColorAsState(
        targetValue = targetPrimaryColor,
        animationSpec = tween(durationMillis = 400),
        label = "primaryColor"
    )

    val secondaryColor by animateColorAsState(
        targetValue = targetSecondaryColor,
        animationSpec = tween(durationMillis = 400),
        label = "secondaryColor"
    )

    val smoothAmplitude by animateFloatAsState(
        targetValue = amplitude.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 120),
        label = "amplitude"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 3.4f

            val currentScale = when (state) {
                AssistantState.IDLE -> breathScale
                AssistantState.CONNECTING -> breathScale * 1.02f
                AssistantState.LISTENING -> 1f + (smoothAmplitude * 0.45f)
                AssistantState.PROCESSING -> 1f + (breathScale - 1f) * 0.5f
                AssistantState.SPEAKING -> 1f + (smoothAmplitude * 0.65f)
                AssistantState.ERROR -> 0.98f
            }

            val radius = baseRadius * currentScale

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.35f + (smoothAmplitude * 0.3f)),
                        secondaryColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.8f
                ),
                radius = radius * 1.8f,
                center = center
            )

            if ((state == AssistantState.SPEAKING || state == AssistantState.LISTENING) && smoothAmplitude > 0.05f) {
                val waveRadius = radius * rippleScale
                val waveAlpha = (1f - ((waveRadius - radius) / (radius * 0.4f))).coerceIn(0f, 0.6f)
                drawCircle(
                    color = primaryColor.copy(alpha = waveAlpha),
                    radius = waveRadius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            if (state == AssistantState.PROCESSING || state == AssistantState.CONNECTING) {
                val orbitRadius = radius * 1.25f
                val dotCount = 6
                for (i in 0 until dotCount) {
                    val angleDeg = rotationAngle + (i * (360f / dotCount))
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val dotX = center.x + (orbitRadius * cos(angleRad)).toFloat()
                    val dotY = center.y + (orbitRadius * sin(angleRad)).toFloat()
                    val dotAlpha = (i + 1) / dotCount.toFloat()
                    drawCircle(
                        color = secondaryColor.copy(alpha = dotAlpha * 0.8f),
                        radius = 4.dp.toPx(),
                        center = Offset(dotX, dotY)
                    )
                }
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondaryColor.copy(alpha = 0.95f),
                        primaryColor.copy(alpha = 0.85f),
                        Color(0xFF0F172A)
                    ),
                    center = Offset(center.x - (radius * 0.25f), center.y - (radius * 0.25f)),
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(center.x - (radius * 0.35f), center.y - (radius * 0.35f)),
                    radius = radius * 0.65f
                ),
                radius = radius * 0.65f,
                center = Offset(center.x - (radius * 0.35f), center.y - (radius * 0.35f))
            )
        }
    }
}
