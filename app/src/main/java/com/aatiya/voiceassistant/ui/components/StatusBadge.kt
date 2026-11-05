package com.aatiya.voiceassistant.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aatiya.voiceassistant.domain.model.AssistantState
import com.aatiya.voiceassistant.ui.theme.DarkSurface
import com.aatiya.voiceassistant.ui.theme.DarkSurfaceBorder
import com.aatiya.voiceassistant.ui.theme.PrimaryCyan
import com.aatiya.voiceassistant.ui.theme.PrimaryPurple
import com.aatiya.voiceassistant.ui.theme.StateConnecting
import com.aatiya.voiceassistant.ui.theme.StateError
import com.aatiya.voiceassistant.ui.theme.StateListening
import com.aatiya.voiceassistant.ui.theme.StateProcessing
import com.aatiya.voiceassistant.ui.theme.StateSpeaking
import com.aatiya.voiceassistant.ui.theme.TextPrimary

@Composable
fun StatusBadge(
    state: AssistantState,
    modifier: Modifier = Modifier
) {
    val indicatorColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.IDLE -> PrimaryPurple
            AssistantState.CONNECTING -> StateConnecting
            AssistantState.LISTENING -> StateListening
            AssistantState.PROCESSING -> StateProcessing
            AssistantState.SPEAKING -> StateSpeaking
            AssistantState.ERROR -> StateError
        },
        label = "indicatorColor"
    )

    val labelText = when (state) {
        AssistantState.IDLE -> "Ready"
        AssistantState.CONNECTING -> "Connecting..."
        AssistantState.LISTENING -> "Listening"
        AssistantState.PROCESSING -> "Thinking..."
        AssistantState.SPEAKING -> "Gemini Speaking"
        AssistantState.ERROR -> "Connection Error"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface.copy(alpha = 0.85f))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = labelText,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary
        )
    }
}
