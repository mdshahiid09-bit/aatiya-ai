package com.aatiya.voiceassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aatiya.voiceassistant.domain.model.AssistantState
import com.aatiya.voiceassistant.ui.theme.DarkSurface
import com.aatiya.voiceassistant.ui.theme.DarkSurfaceBorder
import com.aatiya.voiceassistant.ui.theme.PrimaryCyan
import com.aatiya.voiceassistant.ui.theme.PrimaryPurple
import com.aatiya.voiceassistant.ui.theme.TextMuted
import com.aatiya.voiceassistant.ui.theme.TextPrimary
import com.aatiya.voiceassistant.ui.theme.TextSecondary

@Composable
fun LiveCaptionCard(
    captionText: String,
    state: AssistantState,
    lastUserMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurface.copy(alpha = 0.9f))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            if (!lastUserMessage.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PrimaryCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "You",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryCyan
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastUserMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AATIYA AI",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryPurple
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            val displayCaption = when {
                captionText.isNotEmpty() -> captionText
                state == AssistantState.LISTENING -> "Listening to your voice..."
                state == AssistantState.PROCESSING -> "Processing input..."
                state == AssistantState.SPEAKING -> "Responding..."
                state == AssistantState.CONNECTING -> "Initiating Gemini Live WebSocket session..."
                else -> "Tap the microphone to start real-time conversation or type a message below."
            }

            Text(
                text = displayCaption,
                style = MaterialTheme.typography.bodyLarge,
                color = if (captionText.isNotEmpty()) TextPrimary else TextMuted
            )
        }
    }
}
