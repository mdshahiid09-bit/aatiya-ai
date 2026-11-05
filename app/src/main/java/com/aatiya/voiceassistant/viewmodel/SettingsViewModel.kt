package com.aatiya.voiceassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aatiya.voiceassistant.domain.model.GeminiModel
import com.aatiya.voiceassistant.domain.model.SupportedVoices
import com.aatiya.voiceassistant.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _apiKeyInput = MutableStateFlow(settingsRepository.getApiKey() ?: "")
    val apiKeyInput: StateFlow<String> = _apiKeyInput.asStateFlow()

    private val _showApiKey = MutableStateFlow(false)
    val showApiKey: StateFlow<Boolean> = _showApiKey.asStateFlow()

    private val _textModel = MutableStateFlow(settingsRepository.getTextModel())
    val textModel: StateFlow<String> = _textModel.asStateFlow()

    private val _liveModel = MutableStateFlow(settingsRepository.getLiveModel())
    val liveModel: StateFlow<String> = _liveModel.asStateFlow()

    private val _voiceName = MutableStateFlow(settingsRepository.getVoice())
    val voiceName: StateFlow<String> = _voiceName.asStateFlow()

    private val _systemInstruction = MutableStateFlow(settingsRepository.getSystemInstruction())
    val systemInstruction: StateFlow<String> = _systemInstruction.asStateFlow()

    private val _availableModels = MutableStateFlow<List<GeminiModel>>(emptyList())
    val availableModels: StateFlow<List<GeminiModel>> = _availableModels.asStateFlow()

    private val _isTestingText = MutableStateFlow(false)
    val isTestingText: StateFlow<Boolean> = _isTestingText.asStateFlow()

    private val _textTestResult = MutableStateFlow<String?>(null)
    val textTestResult: StateFlow<String?> = _textTestResult.asStateFlow()

    private val _isTestingLive = MutableStateFlow(false)
    val isTestingLive: StateFlow<Boolean> = _isTestingLive.asStateFlow()

    private val _liveTestResult = MutableStateFlow<String?>(null)
    val liveTestResult: StateFlow<String?> = _liveTestResult.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val hasStoredKey: Boolean get() = settingsRepository.hasApiKey()
    val maskedStoredKey: String get() = settingsRepository.getMaskedApiKey()

    fun updateApiKey(key: String) {
        _apiKeyInput.value = key
    }

    fun toggleShowApiKey() {
        _showApiKey.value = !_showApiKey.value
    }

    fun updateTextModel(model: String) {
        _textModel.value = model
        settingsRepository.saveTextModel(model)
    }

    fun updateLiveModel(model: String) {
        _liveModel.value = model
        settingsRepository.saveLiveModel(model)
    }

    fun updateVoice(voice: String) {
        _voiceName.value = voice
        settingsRepository.saveVoice(voice)
    }

    fun updateSystemInstruction(instruction: String) {
        _systemInstruction.value = instruction
        settingsRepository.saveSystemInstruction(instruction)
    }

    fun saveApiKey(): Boolean {
        val key = _apiKeyInput.value.trim()
        if (key.isEmpty()) {
            _statusMessage.value = "API key cannot be empty."
            return false
        }
        val saved = settingsRepository.saveApiKey(key)
        if (saved) {
            _statusMessage.value = "API Key encrypted and saved securely."
        } else {
            _statusMessage.value = "Failed to encrypt key with Keystore."
        }
        return saved
    }

    fun deleteApiKey() {
        settingsRepository.deleteApiKey()
        _apiKeyInput.value = ""
        _statusMessage.value = "API key removed from Keystore."
    }

    fun testTextConnection() {
        _isTestingText.value = true
        _textTestResult.value = null
        _statusMessage.value = null

        viewModelScope.launch {
            val result = settingsRepository.testTextConnection()
            _isTestingText.value = false
            result.onSuccess { (text, latency) ->
                _textTestResult.value = "Text Connection OK (${latency}ms): \"$text\""
            }.onFailure { err ->
                _statusMessage.value = "Text Test Failed: ${err.message}"
            }
        }
    }

    fun testLiveConnection() {
        _isTestingLive.value = true
        _liveTestResult.value = null
        _statusMessage.value = null

        viewModelScope.launch {
            val result = settingsRepository.testLiveConnection()
            _isTestingLive.value = false
            result.onSuccess { latency ->
                _liveTestResult.value = "Gemini Live WebSocket Handshake Verified (${latency}ms)!"
            }.onFailure { err ->
                _statusMessage.value = "Live Test Failed: ${err.message}"
            }
        }
    }

    fun refreshModels() {
        viewModelScope.launch {
            val result = settingsRepository.fetchAvailableModels()
            result.onSuccess { list ->
                _availableModels.value = list
                _statusMessage.value = "Fetched ${list.size} available models."
            }.onFailure { err ->
                _statusMessage.value = "Model fetch error: ${err.message}"
            }
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    class Factory(
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepository) as T
        }
    }
}
