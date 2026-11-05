package com.aatiya.voiceassistant.ui.screens.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aatiya.voiceassistant.domain.model.AssistantState
import com.aatiya.voiceassistant.ui.components.DiagnosticsSheet
import com.aatiya.voiceassistant.ui.components.LiveCaptionCard
import com.aatiya.voiceassistant.ui.components.OrbVisualizer
import com.aatiya.voiceassistant.ui.components.StatusBadge
import com.aatiya.voiceassistant.ui.theme.DarkBackground
import com.aatiya.voiceassistant.ui.theme.DarkSurface
import com.aatiya.voiceassistant.ui.theme.DarkSurfaceBorder
import com.aatiya.voiceassistant.ui.theme.DarkSurfaceVariant
import com.aatiya.voiceassistant.ui.theme.PrimaryCyan
import com.aatiya.voiceassistant.ui.theme.PrimaryPurple
import com.aatiya.voiceassistant.ui.theme.StateError
import com.aatiya.voiceassistant.ui.theme.StateListening
import com.aatiya.voiceassistant.ui.theme.TextMuted
import com.aatiya.voiceassistant.ui.theme.TextPrimary
import com.aatiya.voiceassistant.viewmodel.MainViewModel

@Composable
fun MainAssistantScreen(
    viewModel: MainViewModel,
    onNavigateSettings: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val assistantState by viewModel.assistantState.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val liveTranscript by viewModel.liveTranscript.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val activeError by viewModel.activeError.collectAsState()

    var typedMessage by remember { mutableStateOf("") }
    var showDiagnostics by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceSession()
        }
    }

    fun handleVoiceButtonClick() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.toggleVoiceSession()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    if (showDiagnostics) {
        DiagnosticsSheet(
            textModel = viewModel.textModel,
            liveModel = viewModel.liveModel,
            voiceName = viewModel.voiceName,
            hasApiKey = true,
            maskedKey = viewModel.maskedKey,
            onDismiss = { showDiagnostics = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AATIYA AI",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Gemini Live Assistant",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(state = assistantState)

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showDiagnostics = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Diagnostics",
                            tint = PrimaryCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onNavigateSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = activeError != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                activeError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StateError.copy(alpha = 0.15f))
                            .border(1.dp, StateError.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = StateError,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = StateError,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.4f))

            OrbVisualizer(
                state = assistantState,
                amplitude = audioAmplitude,
                size = 230.dp
            )

            Spacer(modifier = Modifier.weight(0.4f))

            val lastUserMsg = messages.lastOrNull { it.role == com.aatiya.voiceassistant.domain.model.MessageRole.USER }?.text
            LiveCaptionCard(
                captionText = liveTranscript,
                state = assistantState,
                lastUserMessage = lastUserMsg,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            val isVoiceActive = assistantState == AssistantState.LISTENING ||
                    assistantState == AssistantState.SPEAKING ||
                    assistantState == AssistantState.CONNECTING

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = if (isVoiceActive) {
                                listOf(StateListening, PrimaryPurple)
                            } else {
                                listOf(PrimaryPurple, DarkSurfaceVariant)
                            }
                        )
                    )
                    .border(
                        width = 2.dp,
                        color = if (isVoiceActive) StateListening else DarkSurfaceBorder,
                        shape = CircleShape
                    )
                    .clickable { handleVoiceButtonClick() },
                contentAlignment = Alignment.Center
            ) {
                if (assistantState == AssistantState.CONNECTING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isVoiceActive) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Toggle Gemini Live Voice",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isVoiceActive) "Tap to End Live Voice Session" else "Tap to Start Real-Time Voice Conversation",
                style = MaterialTheme.typography.labelSmall,
                color = if (isVoiceActive) StateListening else TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(26.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = typedMessage,
                    onValueChange = { typedMessage = it },
                    placeholder = {
                        Text(
                            text = "Ask AATIYA anything in Hindi or English...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (typedMessage.isNotBlank()) {
                                viewModel.sendTextMessage(typedMessage)
                                typedMessage = ""
                                focusManager.clearFocus()
                            }
                        }
                    )
                )

                IconButton(
                    onClick = {
                        if (typedMessage.isNotBlank()) {
                            viewModel.sendTextMessage(typedMessage)
                            typedMessage = ""
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
