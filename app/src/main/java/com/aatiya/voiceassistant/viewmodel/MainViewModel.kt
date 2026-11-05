package com.aatiya.voiceassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aatiya.voiceassistant.domain.model.AssistantState
import com.aatiya.voiceassistant.domain.model.ChatMessage
import com.aatiya.voiceassistant.domain.repository.LiveAssistantRepository
import com.aatiya.voiceassistant.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val assistantRepository: LiveAssistantRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val assistantState: StateFlow<AssistantState> = assistantRepository.assistantState
    val audioAmplitude: StateFlow<Float> = assistantRepository.audioAmplitude
    val liveTranscript: StateFlow<String> = assistantRepository.liveTranscript
    val messages: StateFlow<List<ChatMessage>> = assistantRepository.messages
    val activeError: StateFlow<String?> = assistantRepository.activeError

    private val _isConfigured = MutableStateFlow(settingsRepository.hasApiKey())
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()

    fun refreshConfigState() {
        _isConfigured.value = settingsRepository.hasApiKey()
    }

    val textModel: String get() = settingsRepository.getTextModel()
    val liveModel: String get() = settingsRepository.getLiveModel()
    val voiceName: String get() = settingsRepository.getVoice()
    val maskedKey: String get() = settingsRepository.getMaskedApiKey()

    fun toggleVoiceSession() {
        when (assistantState.value) {
            AssistantState.IDLE, AssistantState.ERROR -> {
                assistantRepository.startLiveSession()
            }
            AssistantState.CONNECTING, AssistantState.LISTENING,
            AssistantState.PROCESSING, AssistantState.SPEAKING -> {
                assistantRepository.stopLiveSession()
            }
        }
    }

    fun stopVoiceSession() {
        assistantRepository.stopLiveSession()
    }

    fun sendTextMessage(text: String) {
        assistantRepository.sendTextMessage(text)
    }

    fun clearError() {
        assistantRepository.clearError()
    }

    class Factory(
        private val assistantRepository: LiveAssistantRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(assistantRepository, settingsRepository) as T
        }
    }
}
