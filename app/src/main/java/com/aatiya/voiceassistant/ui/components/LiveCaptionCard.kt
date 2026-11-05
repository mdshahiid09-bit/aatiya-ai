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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aatiya.voiceassistant.domain.model.AssistantState
import com.aatiya.voiceassistant.ui.theme.DarkSurface
import com.aatiya.voiceassistant.ui.theme.DarkSurfaceBorder
import com.aatiya.voiceassistant.ui.theme.DarkSurfaceVariant
import com.aatiya.voiceassistant.ui.theme.PrimaryCyan
import com.aatiya.voiceassistant.ui.theme.PrimaryPurple
import com.aatiya.voiceassistant.ui.theme.StateListening
import com.aatiya.voiceassistant.ui.theme.StateSpeaking
import com.aatiya.voiceassistant.ui.theme.TextMuted
import com.aatiya.voiceassistant.ui.theme.TextPrimary
import com.aatiya.voiceassistant.ui.theme.TextSecondary

@Composable
fun LiveCaptionCard(
    captionText: String,
    state: AssistantState,
    lastUserMessage: String? = null,
    lastModelResponse: String? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurface.copy(alpha = 0.95f))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            // User query header if present
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = lastUserMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // AI Status / Response Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val headerDotColor = when (state) {
                    AssistantState.LISTENING -> StateListening
                    AssistantState.SPEAKING -> StateSpeaking
                    AssistantState.PROCESSING, AssistantState.CONNECTING -> PrimaryCyan
                    else -> PrimaryPurple
                }

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(headerDotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AATIYA AI",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                    color = headerDotColor
                )

                Spacer(modifier = Modifier.width(8.dp))

                val stateLabel = when (state) {
                    AssistantState.CONNECTING -> "Connecting Live..."
                    AssistantState.LISTENING -> "Listening..."
                    AssistantState.PROCESSING -> "Thinking..."
                    AssistantState.SPEAKING -> "Speaking..."
                    else -> if (!lastModelResponse.isNullOrBlank() || captionText.isNotBlank()) "Response" else ""
                }

                if (stateLabel.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stateLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = headerDotColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Body content selection
            val (displayText, isPlaceholder) = when {
                captionText.isNotBlank() -> Pair(captionText, false)
                state == AssistantState.PROCESSING -> Pair("Thinking... Generating response...", true)
                state == AssistantState.SPEAKING -> Pair("Speaking... Playing Gemini Live audio", false)
                state == AssistantState.CONNECTING -> Pair("Connecting to Gemini Live WebSocket...", true)
                !lastModelResponse.isNullOrBlank() -> Pair(lastModelResponse, false)
                state == AssistantState.LISTENING -> Pair("Listening to your voice... Speak now", true)
                else -> Pair("Ask AATIYA anything in Hindi or English, or tap the mic for live voice.", true)
            }

            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isPlaceholder) TextMuted else TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}
