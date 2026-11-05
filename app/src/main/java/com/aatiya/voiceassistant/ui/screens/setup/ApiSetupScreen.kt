package com.aatiya.voiceassistant.ui.screens.setup

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aatiya.voiceassistant.ui.components.DiagnosticsSheet
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
import com.aatiya.voiceassistant.viewmodel.SetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiSetupScreen(
    viewModel: SetupViewModel,
    onSetupComplete: () -> Unit
) {
    val apiKey by viewModel.apiKeyInput.collectAsState()
    val showApiKey by viewModel.showApiKey.collectAsState()
    val selectedTextModel by viewModel.selectedTextModel.collectAsState()
    val selectedLiveModel by viewModel.selectedLiveModel.collectAsState()
    val isTestingText by viewModel.isTestingText.collectAsState()
    val textResult by viewModel.textTestResult.collectAsState()
    val isTestingLive by viewModel.isTestingLive.collectAsState()
    val liveResult by viewModel.liveTestResult.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()

    var showDiagnostics by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    if (showDiagnostics) {
        DiagnosticsSheet(
            textModel = selectedTextModel,
            liveModel = selectedLiveModel,
            voiceName = "Aoede",
            hasApiKey = viewModel.hasStoredKey,
            maskedKey = viewModel.maskedStoredKey,
            onDismiss = { showDiagnostics = false }
        )
    }

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
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AATIYA AI Setup",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Configure your Google Gemini credentials",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = { showDiagnostics = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Diagnostics",
                        tint = PrimaryCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionCard(title = "1. Gemini API Key") {
                Text(
                    text = "Encrypted locally using Android Keystore AES-256-GCM. Never sent to third parties.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { viewModel.updateApiKey(it) },
                    label = { Text("Gemini API Key (AIzaSy...)") },
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (viewModel.hasStoredKey) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Stored: ${viewModel.maskedStoredKey}",
                            style = MaterialTheme.typography.labelSmall,
                            color = StateListening
                        )
                        IconButton(onClick = { viewModel.deleteStoredKey() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Key",
                                tint = StateError
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(title = "2. Model Selection") {
                Text(
                    text = "Text Generation Model",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = selectedTextModel,
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
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("gemini-2.0-flash", "gemini-1.5-flash", "gemini-2.5-flash").forEach { model ->
                        OutlinedButton(
                            onClick = { viewModel.updateTextModel(model) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (selectedTextModel == model) PrimaryCyan else TextSecondary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (selectedTextModel == model) PrimaryCyan else DarkSurfaceBorder
                                )
                            )
                        ) {
                            Text(model.removePrefix("gemini-"), fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Live Voice Model (Multimodal WebSocket)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = selectedLiveModel,
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
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("gemini-2.0-flash-exp", "gemini-2.0-flash").forEach { model ->
                        OutlinedButton(
                            onClick = { viewModel.updateLiveModel(model) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (selectedLiveModel == model) PrimaryPurple else TextSecondary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (selectedLiveModel == model) PrimaryPurple else DarkSurfaceBorder
                                )
                            )
                        ) {
                            Text(model.removePrefix("gemini-"), fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { viewModel.refreshAvailableModels() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryCyan)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Refresh Verified Models from API", fontSize = 12.sp)
                }

                if (availableModels.isNotEmpty()) {
                    Text(
                        text = "Found ${availableModels.size} verified models on Google servers",
                        style = MaterialTheme.typography.labelSmall,
                        color = StateListening,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(title = "3. Real Connection Verification") {
                Text(
                    text = "Verify real HTTP REST and WebSocket handshakes before continuing.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.testTextConnection() },
                        enabled = !isTestingText && apiKey.isNotBlank(),
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
                        enabled = !isTestingLive && apiKey.isNotBlank(),
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = textResult ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = StateListening
                    )
                }

                if (!liveResult.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = liveResult ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = StateListening
                    )
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StateError.copy(alpha = 0.15f))
                        .border(1.dp, StateError, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StateError
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val saved = viewModel.saveSettings()
                    if (saved) {
                        onSetupComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Settings & Launch Assistant",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SectionCard(
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
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}
