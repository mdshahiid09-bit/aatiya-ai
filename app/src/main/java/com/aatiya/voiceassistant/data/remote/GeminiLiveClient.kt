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
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
) {

    companion object {
        const val LIVE_WS_BASE_V1BETA =
            "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"
        const val LIVE_WS_BASE_V1ALPHA =
            "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent"

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

        val cleanKey = apiKey.trim()
        val cleanModel = modelName.trim().removePrefix("models/")
        val fullModelPath = "models/$cleanModel"

        connectInternal(
            baseUrl = LIVE_WS_BASE_V1BETA,
            apiKey = cleanKey,
            fullModelPath = fullModelPath,
            cleanModel = cleanModel,
            voiceName = voiceName,
            systemInstruction = systemInstruction,
            allowAlphaFallback = true
        )
    }

    private fun connectInternal(
        baseUrl: String,
        apiKey: String,
        fullModelPath: String,
        cleanModel: String,
        voiceName: String,
        systemInstruction: String?,
        allowAlphaFallback: Boolean
    ) {
        val url = "$baseUrl?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", apiKey)
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
                if (code != 1000) {
                    listener?.onError("Gemini Live server closed session (Code $code): ${reason.ifEmpty { "Server rejected stream" }}")
                } else {
                    listener?.onDisconnected("Session closing (code $code)")
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected.set(false)
                isSetupComplete.set(false)
                if (code != 1000) {
                    listener?.onError("Gemini Live session closed (Code $code): ${reason.ifEmpty { "Unexpected server disconnect" }}")
                } else {
                    listener?.onDisconnected("Session ended normally")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected.set(false)
                isSetupComplete.set(false)

                // If v1beta returned 404, fallback automatically to v1alpha
                if (allowAlphaFallback && response?.code == 404 && baseUrl == LIVE_WS_BASE_V1BETA) {
                    connectInternal(
                        baseUrl = LIVE_WS_BASE_V1ALPHA,
                        apiKey = apiKey,
                        fullModelPath = fullModelPath,
                        cleanModel = cleanModel,
                        voiceName = voiceName,
                        systemInstruction = systemInstruction,
                        allowAlphaFallback = false
                    )
                    return
                }

                val errorBody = try { response?.body?.string() } catch (_: Exception) { null }
                val parsed = parseErrorJson(errorBody)

                val errorMsg = when {
                    !parsed.isNullOrBlank() -> parsed
                    response != null -> "Gemini Live rejected connection (HTTP ${response.code}): ${response.message.ifEmpty { "Check API key and live model access." }}"
                    t.message != null -> "Live connection error: ${t.message}"
                    else -> "Connection failed. Please verify API key and network."
                }
                listener?.onError(errorMsg)
            }
        })
    }

    private fun parseErrorJson(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return try {
            val root = JSONObject(body)
            val error = root.optJSONObject("error")
            val message = error?.optString("message")
            val status = error?.optString("status")
            if (!message.isNullOrBlank()) {
                "Gemini Live: $message ($status)"
            } else {
                null
            }
        } catch (_: Exception) {
            body
        }
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

            if (root.has("setupComplete") || root.has("setup_complete")) {
                isSetupComplete.set(true)
                listener?.onSetupCompleted()
                return
            }

            val serverContent = root.optJSONObject("serverContent")
                ?: root.optJSONObject("server_content")
                ?: return

            if (serverContent.optBoolean("interrupted", false)) {
                listener?.onInterrupted()
            }

            val modelTurn = serverContent.optJSONObject("modelTurn")
                ?: serverContent.optJSONObject("model_turn")
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
                            ?: part.optJSONObject("inline_data")
                        if (inlineData != null) {
                            val mimeType = inlineData.optString("mimeType", "")
                                .ifEmpty { inlineData.optString("mime_type", "") }
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

            if (serverContent.optBoolean("turnComplete", false) ||
                serverContent.optBoolean("turn_complete", false)) {
                listener?.onTurnComplete()
            }
        } catch (e: Exception) {
            listener?.onError("Failed to parse Gemini Live packet: ${e.message}")
        }
    }
}
