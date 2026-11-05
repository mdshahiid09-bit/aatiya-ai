package com.aatiya.voiceassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aatiya.voiceassistant.data.remote.GeminiRestClient
import com.aatiya.voiceassistant.domain.model.GeminiModel
import com.aatiya.voiceassistant.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SetupViewModel(
    private val settingsRepository: SettingsRepository,
    private val restClient: GeminiRestClient
) : ViewModel() {

    private val _apiKeyInput = MutableStateFlow(settingsRepository.getApiKey() ?: "")
    val apiKeyInput: StateFlow<String> = _apiKeyInput.asStateFlow()

    private val _showApiKey = MutableStateFlow(false)
    val showApiKey: StateFlow<Boolean> = _showApiKey.asStateFlow()

    private val _selectedTextModel = MutableStateFlow(settingsRepository.getTextModel())
    val selectedTextModel: StateFlow<String> = _selectedTextModel.asStateFlow()

    private val _selectedLiveModel = MutableStateFlow(settingsRepository.getLiveModel())
    val selectedLiveModel: StateFlow<String> = _selectedLiveModel.asStateFlow()

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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    val hasStoredKey: Boolean get() = settingsRepository.hasApiKey()
    val maskedStoredKey: String get() = settingsRepository.getMaskedApiKey()

    fun updateApiKey(value: String) {
        _apiKeyInput.value = value
        _errorMessage.value = null
        _saveSuccess.value = false
    }

    fun toggleShowApiKey() {
        _showApiKey.value = !_showApiKey.value
    }

    fun updateTextModel(model: String) {
        _selectedTextModel.value = model
    }

    fun updateLiveModel(model: String) {
        _selectedLiveModel.value = model
    }

    fun deleteStoredKey() {
        settingsRepository.deleteApiKey()
        _apiKeyInput.value = ""
        _textTestResult.value = null
        _liveTestResult.value = null
    }

    fun saveSettings(): Boolean {
        val key = _apiKeyInput.value.trim()
        if (key.isEmpty()) {
            _errorMessage.value = "Please enter a valid Gemini API key."
            return false
        }

        val saved = settingsRepository.saveApiKey(key)
        if (!saved) {
            _errorMessage.value = "Failed to encrypt and store API key in Android Keystore."
            return false
        }

        settingsRepository.saveTextModel(_selectedTextModel.value)
        settingsRepository.saveLiveModel(_selectedLiveModel.value)
        _saveSuccess.value = true
        _errorMessage.value = null
        return true
    }

    fun testTextConnection() {
        val key = _apiKeyInput.value.trim()
        if (key.isEmpty()) {
            _errorMessage.value = "Enter an API key first before testing."
            return
        }

        _isTestingText.value = true
        _textTestResult.value = null
        _errorMessage.value = null

        viewModelScope.launch {
            val result = restClient.testTextConnection(key, _selectedTextModel.value)
            _isTestingText.value = false
            result.onSuccess { (response, latency) ->
                _textTestResult.value = "SUCCESS (${latency}ms): \"$response\""
            }.onFailure { err ->
                _errorMessage.value = "Text Connection Test Failed: ${err.message}"
            }
        }
    }

    fun testLiveConnection() {
        val key = _apiKeyInput.value.trim()
        if (key.isEmpty()) {
            _errorMessage.value = "Enter an API key first before testing."
            return
        }

        settingsRepository.saveApiKey(key)
        settingsRepository.saveLiveModel(_selectedLiveModel.value)

        _isTestingLive.value = true
        _liveTestResult.value = null
        _errorMessage.value = null

        viewModelScope.launch {
            val result = settingsRepository.testLiveConnection()
            _isTestingLive.value = false
            result.onSuccess { latency ->
                _liveTestResult.value = "LIVE WEBSOCKET CONNECTED (${latency}ms) - Handshake & Setup Verified!"
            }.onFailure { err ->
                _errorMessage.value = "Live Voice Test Failed: ${err.message}"
            }
        }
    }

    fun refreshAvailableModels() {
        val key = _apiKeyInput.value.trim()
        if (key.isEmpty()) {
            _errorMessage.value = "Enter an API key first to fetch models."
            return
        }

        viewModelScope.launch {
            val result = restClient.fetchAvailableModels(key)
            result.onSuccess { list ->
                _availableModels.value = list
            }.onFailure { err ->
                _errorMessage.value = "Failed to fetch models: ${err.message}"
            }
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val restClient: GeminiRestClient
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SetupViewModel(settingsRepository, restClient) as T
        }
    }
}
