package com.aatiya.voiceassistant.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aatiya.voiceassistant.domain.model.SupportedVoices
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
import com.aatiya.voiceassistant.ui.theme.TextSecondary
import com.aatiya.voiceassistant.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val apiKey by viewModel.apiKeyInput.collectAsState()
    val showApiKey by viewModel.showApiKey.collectAsState()
    val textModel by viewModel.textModel.collectAsState()
    val liveModel by viewModel.liveModel.collectAsState()
    val voiceName by viewModel.voiceName.collectAsState()
    val systemInstruction by viewModel.systemInstruction.collectAsState()
    val isTestingText by viewModel.isTestingText.collectAsState()
    val textResult by viewModel.textTestResult.collectAsState()
    val isTestingLive by viewModel.isTestingLive.collectAsState()
    val liveResult by viewModel.liveTestResult.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "AATIYA AI Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionBox(title = "API Key & Keystore Security") {
                Text(
                    text = "Stored securely in Android Keystore using AES-256-GCM hardware encryption.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { viewModel.updateApiKey(it) },
                    label = { Text("Gemini API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleShowApiKey() }) {
                            Icon(
                                imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Visibility",
                                tint = TextSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveApiKey() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save New Key", fontSize = 12.sp, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { viewModel.deleteApiKey() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StateError)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionBox(title = "Model Configuration") {
                Text(text = "Text Generation Model", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = textModel,
                    onValueChange = { viewModel.updateTextModel(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Live Voice Model (WebSocket)", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = liveModel,
                    onValueChange = { viewModel.updateLiveModel(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionBox(title = "Gemini Live Voice Preference") {
                Text(
                    text = "Initial preferred voice: Aoede (Warm, conversational)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                SupportedVoices.ALL.forEach { voice ->
                    val isSelected = voice.name.equals(voiceName, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) PrimaryPurple.copy(alpha = 0.2f) else DarkBackground)
                            .border(1.dp, if (isSelected) PrimaryPurple else DarkSurfaceBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = voice.name, style = MaterialTheme.typography.titleMedium, color = if (isSelected) PrimaryPurple else TextPrimary)
                            Text(text = voice.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }

                        Button(
                            onClick = { viewModel.updateVoice(voice.name) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) PrimaryPurple else DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isSelected) "Active" else "Select", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionBox(title = "Language & Personality Prompt") {
                Text(
                    text = "AATIYA AI understands Hindi, Hinglish, and English naturally.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = systemInstruction,
                    onValueChange = { viewModel.updateSystemInstruction(it) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionBox(title = "Connection Testing") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.testTextConnection() },
                        enabled = !isTestingText,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isTestingText) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PrimaryCyan, strokeWidth = 2.dp)
                        } else {
                            Text("Test Text", color = PrimaryCyan, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = { viewModel.testLiveConnection() },
                        enabled = !isTestingLive,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isTestingLive) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PrimaryPurple, strokeWidth = 2.dp)
                        } else {
                            Text("Test Live Voice", color = PrimaryPurple, fontSize = 13.sp)
                        }
                    }
                }

                if (!textResult.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = textResult ?: "", style = MaterialTheme.typography.bodySmall, color = StateListening)
                }

                if (!liveResult.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = liveResult ?: "", style = MaterialTheme.typography.bodySmall, color = StateListening)
                }
            }

            if (!statusMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = statusMessage ?: "", style = MaterialTheme.typography.bodySmall, color = PrimaryCyan)
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionBox(title = "About AATIYA AI") {
                Text(text = "Application: AATIYA AI v1.0.0", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Text(text = "Package: com.aatiya.voiceassistant", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(text = "Developed by MD Shahid", style = MaterialTheme.typography.bodyMedium, color = PrimaryPurple)
                Text(text = "Gemini API usage may incur charges or be subject to quotas according to Google's terms.", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SectionBox(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}
