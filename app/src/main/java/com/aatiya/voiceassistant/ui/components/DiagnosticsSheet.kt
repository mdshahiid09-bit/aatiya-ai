package com.aatiya.voiceassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aatiya.voiceassistant.audio.AudioPlayer
import com.aatiya.voiceassistant.audio.AudioRecorder
import com.aatiya.voiceassistant.ui.theme.DarkBackground
import com.aatiya.voiceassistant.ui.theme.DarkSurface
import com.aatiya.voiceassistant.ui.theme.DarkSurfaceBorder
import com.aatiya.voiceassistant.ui.theme.PrimaryCyan
import com.aatiya.voiceassistant.ui.theme.PrimaryPurple
import com.aatiya.voiceassistant.ui.theme.TextMuted
import com.aatiya.voiceassistant.ui.theme.TextPrimary
import com.aatiya.voiceassistant.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsSheet(
    textModel: String,
    liveModel: String,
    voiceName: String,
    hasApiKey: Boolean,
    maskedKey: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "System Diagnostics",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Text(
                text = "Real-time telemetry and hardware state",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            DiagnosticCard(title = "Security & Keystore") {
                DiagnosticRow("Storage Engine", "Android Keystore (AES-256-GCM)")
                DiagnosticRow("Key Present", if (hasApiKey) "Yes (Hardware Backed)" else "Missing")
                DiagnosticRow("Masked Key", maskedKey)
                DiagnosticRow("Ephemeral Provisioning", "Active / Fallback Ready")
            }

            Spacer(modifier = Modifier.height(12.dp))

            DiagnosticCard(title = "Gemini AI Engine") {
                DiagnosticRow("Text Model", textModel)
                DiagnosticRow("Live Voice Model", liveModel)
                DiagnosticRow("Voice Persona", voiceName)
                DiagnosticRow("Protocol", "BidiGenerateContent (WebSocket)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            DiagnosticCard(title = "Native Audio Pipeline") {
                DiagnosticRow("Microphone Input", "${AudioRecorder.SAMPLE_RATE} Hz 16-bit PCM Mono")
                DiagnosticRow("Audio Playback", "${AudioPlayer.SAMPLE_RATE} Hz 16-bit PCM Mono")
                DiagnosticRow("Barge-In / Interruption", "Enabled (VAD + Instant Flush)")
                DiagnosticRow("Synthesizer", "Gemini Native Multimodal Voice")
            }

            Spacer(modifier = Modifier.height(12.dp))

            DiagnosticCard(title = "Build & Ownership") {
                DiagnosticRow("Application", "AATIYA AI v1.0.0")
                DiagnosticRow("Package", "com.aatiya.voiceassistant")
                DiagnosticRow("Owner / Creator", "MD Shahid")
                DiagnosticRow("Target SDK", "35 (Android 15)")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close Diagnostics", color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DiagnosticCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = PrimaryCyan
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}
