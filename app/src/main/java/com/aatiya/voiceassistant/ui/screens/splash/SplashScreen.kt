package com.aatiya.voiceassistant.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aatiya.voiceassistant.domain.model.AssistantState
import com.aatiya.voiceassistant.ui.components.OrbVisualizer
import com.aatiya.voiceassistant.ui.theme.DarkBackground
import com.aatiya.voiceassistant.ui.theme.PrimaryCyan
import com.aatiya.voiceassistant.ui.theme.TextMuted
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    hasApiKey: Boolean,
    onNavigateNext: (destination: String) -> Unit
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        delay(600)
        if (hasApiKey) {
            onNavigateNext("main")
        } else {
            onNavigateNext("setup")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            OrbVisualizer(
                state = AssistantState.IDLE,
                amplitude = 0.2f,
                size = 180.dp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "AATIYA AI",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Next-Gen Gemini Live Voice Assistant",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryCyan
            )

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Crafted for MD Shahid",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}
