package com.aatiya.voiceassistant.data.remote

import android.util.Base64
import com.aatiya.voiceassistant.domain.model.SupportedVoices
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class GeminiLiveClient(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // Keep alive for WebSocket
        .build()
) {

    companion object {
        const val LIVE_WS_BASE =
            "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"

        fun createSetupPayload(
            modelPath: String,
            voiceName: String,
            systemInstruction: String?
        ): String {
            val root = JSONObject()
            val setup = JSONObject()
            setup.put("model", modelPath)

            val generationConfig = JSONObject()
            val modalities = JSONArray().put("AUDIO")
            generationConfig.put("responseModalities", modalities)

            val speechConfig = JSONObject()
            val voiceConfig = JSONObject()
            val prebuiltVoiceConfig = JSONObject().put("voiceName", voiceName)
            voiceConfig.put("prebuiltVoiceConfig", prebuiltVoiceConfig)
            speechConfig.put("voiceConfig", voiceConfig)
            generationConfig.put("speechConfig", speechConfig)

            setup.put("generationConfig", generationConfig)

            if (!systemInstruction.isNullOrBlank()) {
                val sysInst = JSONObject()
                val parts = JSONArray().put(JSONObject().put("text", systemInstruction))
                sysInst.put("parts", parts)
                setup.put("systemInstruction", sysInst)
            }

            root.put("setup", setup)
            return root.toString()
        }

        fun createRealtimeAudioPayload(base64Data: String): String {
            val root = JSONObject()
            val realtimeInput = JSONObject()
            val mediaChunks = JSONArray()

            val chunk = JSONObject()
            chunk.put("mimeType", "audio/pcm;rate=16000")
            chunk.put("data", base64Data)

            mediaChunks.put(chunk)
            realtimeInput.put("mediaChunks", mediaChunks)
            root.put("realtimeInput", realtimeInput)
            return root.toString()
        }
    }

    object ProtocolHelper {
        fun createSetupPayload(
            modelPath: String,
            voiceName: String,
            systemInstruction: String?
        ): String = Companion.createSetupPayload(modelPath, voiceName, systemInstruction)

        fun createRealtimeAudioPayload(base64Data: String): String =
            Companion.createRealtimeAudioPayload(base64Data)
    }

    interface Listener {
        fun onConnected()
        fun onSetupCompleted()
        fun onAudioReceived(pcmBytes: ByteArray)
        fun onTranscriptReceived(text: String)
        fun onInterrupted()
        fun onTurnComplete()
        fun onError(message: String)
        fun onDisconnected(reason: String)
    }

    private var activeWebSocket: WebSocket? = null
    private var listener: Listener? = null
    private val isConnected = AtomicBoolean(false)
    private val isSetupComplete = AtomicBoolean(false)

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun isSessionActive(): Boolean = isConnected.get() && isSetupComplete.get()

    fun connect(
        apiKey: String,
        modelName: String,
        voiceName: String = SupportedVoices.DEFAULT_VOICE,
        systemInstruction: String? = null,
        ephemeralToken: String? = null
    ) {
        disconnect()

        val cleanModel = modelName.trim().removePrefix("models/")
        val fullModelPath = "models/$cleanModel"

        val url = if (!ephemeralToken.isNullOrBlank()) {
            "$LIVE_WS_BASE?access_token=$ephemeralToken"
        } else {
            "$LIVE_WS_BASE?key=${apiKey.trim()}"
        }

        val request = Request.Builder()
            .url(url)
            .build()

        activeWebSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected.set(true)
                listener?.onConnected()

                val setupPayload = createSetupPayload(fullModelPath, voiceName, systemInstruction)
                webSocket.send(setupPayload)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleIncomingMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected.set(false)
                isSetupComplete.set(false)
                listener?.onDisconnected("Closing: $reason (code $code)")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected.set(false)
                isSetupComplete.set(false)
                listener?.onDisconnected("Closed: $reason (code $code)")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected.set(false)
                isSetupComplete.set(false)
                val statusMessage = response?.let { "HTTP ${it.code}: ${it.message}" }
                val errorMsg = when {
                    statusMessage != null -> "Gemini Live WebSocket failure: $statusMessage"
                    t.message != null -> "Gemini Live error: ${t.message}"
                    else -> "Connection failed. Please verify API key and network."
                }
                listener?.onError(errorMsg)
            }
        })
    }

    fun sendAudioChunk(pcmBytes: ByteArray, offset: Int, length: Int) {
        val ws = activeWebSocket ?: return
        if (!isConnected.get()) return

        val base64Data = Base64.encodeToString(pcmBytes, offset, length, Base64.NO_WRAP)
        val payload = createRealtimeAudioPayload(base64Data)
        ws.send(payload)
    }

    fun disconnect() {
        isConnected.set(false)
        isSetupComplete.set(false)
        try {
            activeWebSocket?.close(1000, "User terminated session")
        } catch (_: Exception) {
        }
        activeWebSocket = null
    }

    private fun handleIncomingMessage(jsonText: String) {
        try {
            val root = JSONObject(jsonText)

            if (root.has("error")) {
                val errObj = root.optJSONObject("error")
                val message = errObj?.optString("message") ?: "Unknown Live API error"
                listener?.onError("Live API Error: $message")
                return
            }

            if (root.has("setupComplete")) {
                isSetupComplete.set(true)
                listener?.onSetupCompleted()
                return
            }

            val serverContent = root.optJSONObject("serverContent") ?: return

            if (serverContent.optBoolean("interrupted", false)) {
                listener?.onInterrupted()
            }

            val modelTurn = serverContent.optJSONObject("modelTurn")
            if (modelTurn != null) {
                val parts = modelTurn.optJSONArray("parts")
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)

                        val text = part.optString("text", "")
                        if (text.isNotEmpty()) {
                            listener?.onTranscriptReceived(text)
                        }

                        val inlineData = part.optJSONObject("inlineData")
                        if (inlineData != null) {
                            val mimeType = inlineData.optString("mimeType", "")
                            if (mimeType.startsWith("audio/pcm")) {
                                val base64Audio = inlineData.optString("data", "")
                                if (base64Audio.isNotEmpty()) {
                                    val audioBytes = Base64.decode(base64Audio, Base64.NO_WRAP)
                                    listener?.onAudioReceived(audioBytes)
                                }
                            }
                        }
                    }
                }
            }

            if (serverContent.optBoolean("turnComplete", false)) {
                listener?.onTurnComplete()
            }
        } catch (e: Exception) {
            listener?.onError("Failed to parse Gemini Live packet: ${e.message}")
        }
    }
}
