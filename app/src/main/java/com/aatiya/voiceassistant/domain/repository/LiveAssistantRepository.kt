package com.aatiya.voiceassistant.domain.repository

import com.aatiya.voiceassistant.audio.AudioPlayer
import com.aatiya.voiceassistant.audio.AudioRecorder
import com.aatiya.voiceassistant.data.remote.GeminiLiveClient
import com.aatiya.voiceassistant.data.remote.GeminiRestClient
import com.aatiya.voiceassistant.domain.model.AssistantState
import com.aatiya.voiceassistant.domain.model.ChatMessage
import com.aatiya.voiceassistant.domain.model.ConnectionState
import com.aatiya.voiceassistant.domain.model.MessageRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LiveAssistantRepository(
    private val settingsRepository: SettingsRepository,
    private val restClient: GeminiRestClient,
    private val liveClient: GeminiLiveClient,
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer
) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _activeError = MutableStateFlow<String?>(null)
    val activeError: StateFlow<String?> = _activeError.asStateFlow()

    private var activeTurnTextBuilder = StringBuilder()
    private var hasReceivedAudioInTurn = false
    private var amplitudeJob: Job? = null

    init {
        setupLiveClientListener()
        startAmplitudeCollector()
    }

    private fun setupLiveClientListener() {
        liveClient.setListener(object : GeminiLiveClient.Listener {
            override fun onConnected() {
                _connectionState.value = ConnectionState.Connecting
                _assistantState.value = AssistantState.CONNECTING
            }

            override fun onSetupCompleted() {
                _connectionState.value = ConnectionState.Connected()
                _assistantState.value = AssistantState.LISTENING

                // Start microphone capture
                val started = audioRecorder.startRecording { buffer, offset, length ->
                    liveClient.sendAudioChunk(buffer, offset, length)
                }
                if (!started) {
                    _activeError.value = "Failed to start microphone AudioRecord."
                    _assistantState.value = AssistantState.ERROR
                }
            }

            override fun onAudioReceived(pcmBytes: ByteArray) {
                hasReceivedAudioInTurn = true
                _assistantState.value = AssistantState.SPEAKING
                if (_liveTranscript.value.isEmpty() && activeTurnTextBuilder.isEmpty()) {
                    _liveTranscript.value = "🔊 Speaking..."
                }
                audioPlayer.enqueueAudio(pcmBytes)
            }

            override fun onTranscriptReceived(text: String) {
                activeTurnTextBuilder.append(text)
                _liveTranscript.value = activeTurnTextBuilder.toString()
            }

            override fun onInterrupted() {
                // Barge-in occurred! User interrupted Gemini
                audioPlayer.interruptAndClear()
                _assistantState.value = AssistantState.LISTENING
                if (activeTurnTextBuilder.isNotEmpty()) {
                    commitActiveTurn(isInterrupted = true)
                } else if (hasReceivedAudioInTurn) {
                    val chatMsg = ChatMessage(
                        role = MessageRole.MODEL,
                        text = "🔊 [Interrupted]",
                        isLiveVoice = true
                    )
                    _messages.value = _messages.value + chatMsg
                    _liveTranscript.value = ""
                }
                hasReceivedAudioInTurn = false
            }

            override fun onTurnComplete() {
                if (activeTurnTextBuilder.isNotEmpty()) {
                    commitActiveTurn(isInterrupted = false)
                } else if (hasReceivedAudioInTurn) {
                    val chatMsg = ChatMessage(
                        role = MessageRole.MODEL,
                        text = "🔊 [Gemini Voice Response]",
                        isLiveVoice = true
                    )
                    _messages.value = _messages.value + chatMsg
                    _liveTranscript.value = ""
                }
                hasReceivedAudioInTurn = false

                // Transition state back to listening if session is still alive
                if (liveClient.isSessionActive()) {
                    _assistantState.value = AssistantState.LISTENING
                }
            }

            override fun onError(message: String) {
                _activeError.value = message
                _connectionState.value = ConnectionState.Error(message)
                _assistantState.value = AssistantState.ERROR
                hasReceivedAudioInTurn = false
                cleanupSession()
            }

            override fun onDisconnected(reason: String) {
                _connectionState.value = ConnectionState.Disconnected
                if (_assistantState.value != AssistantState.ERROR) {
                    _assistantState.value = AssistantState.IDLE
                }
                hasReceivedAudioInTurn = false
                cleanupSession()
            }
        })
    }

    private fun startAmplitudeCollector() {
        amplitudeJob = scope.launch {
            while (isActive) {
                val amp = when (_assistantState.value) {
                    AssistantState.SPEAKING -> audioPlayer.amplitude.value
                    AssistantState.LISTENING -> audioRecorder.amplitude.value
                    else -> 0f
                }
                _audioAmplitude.value = amp
                kotlinx.coroutines.delay(30)
            }
        }
    }

    private fun commitActiveTurn(isInterrupted: Boolean) {
        val text = activeTurnTextBuilder.toString().trim()
        if (text.isNotEmpty()) {
            val suffix = if (isInterrupted) " [Interrupted]" else ""
            val chatMsg = ChatMessage(
                role = MessageRole.MODEL,
                text = text + suffix,
                isLiveVoice = true
            )
            _messages.value = _messages.value + chatMsg
        }
        activeTurnTextBuilder.clear()
        _liveTranscript.value = ""
    }

    /**
     * Starts continuous real-time Gemini Live voice conversation directly over WebSocket.
     */
    fun startLiveSession() {
        val apiKey = settingsRepository.getApiKey()
        if (apiKey.isNullOrBlank()) {
            _activeError.value = "Gemini API key is not configured. Please enter your key in settings."
            _assistantState.value = AssistantState.ERROR
            return
        }

        _activeError.value = null
        _assistantState.value = AssistantState.CONNECTING
        _connectionState.value = ConnectionState.Connecting
        hasReceivedAudioInTurn = false

        val model = settingsRepository.getLiveModel()
        val voice = settingsRepository.getVoice()
        val systemInstruction = settingsRepository.getSystemInstruction()

        liveClient.connect(
            apiKey = apiKey,
            modelName = model,
            voiceName = voice,
            systemInstruction = systemInstruction,
            ephemeralToken = null
        )
    }

    /**
     * Gracefully stops the active Live voice session.
     */
    fun stopLiveSession() {
        cleanupSession()
        _assistantState.value = AssistantState.IDLE
        _connectionState.value = ConnectionState.Disconnected
    }

    private fun cleanupSession() {
        audioRecorder.stopRecording()
        audioPlayer.stop()
        liveClient.disconnect()
        if (activeTurnTextBuilder.isNotEmpty()) {
            commitActiveTurn(false)
        }
        hasReceivedAudioInTurn = false
    }

    /**
     * Sends a text message to Gemini Text model and updates live response immediately.
     */
    fun sendTextMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val apiKey = settingsRepository.getApiKey()
        if (apiKey.isNullOrBlank()) {
            _activeError.value = "Gemini API key is not configured. Please enter your key in settings."
            return
        }

        _activeError.value = null
        val userMsg = ChatMessage(
            role = MessageRole.USER,
            text = trimmed,
            isLiveVoice = false
        )
        _messages.value = _messages.value + userMsg

        _assistantState.value = AssistantState.PROCESSING
        _liveTranscript.value = ""

        scope.launch {
            val model = settingsRepository.getTextModel()
            val systemInstruction = settingsRepository.getSystemInstruction()
            val result = restClient.generateContent(
                apiKey = apiKey,
                model = model,
                prompt = trimmed,
                systemInstruction = systemInstruction
            )

            result.onSuccess { reply ->
                val modelMsg = ChatMessage(
                    role = MessageRole.MODEL,
                    text = reply,
                    isLiveVoice = false
                )
                _messages.value = _messages.value + modelMsg
                _liveTranscript.value = reply
                _assistantState.value = if (liveClient.isSessionActive()) AssistantState.LISTENING else AssistantState.IDLE
            }.onFailure { err ->
                _activeError.value = err.message ?: "Failed to generate text response."
                _assistantState.value = if (liveClient.isSessionActive()) AssistantState.LISTENING else AssistantState.IDLE
            }
        }
    }

    fun clearError() {
        _activeError.value = null
        if (_assistantState.value == AssistantState.ERROR) {
            _assistantState.value = AssistantState.IDLE
        }
    }
}
