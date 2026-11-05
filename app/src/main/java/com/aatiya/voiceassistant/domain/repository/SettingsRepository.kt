package com.aatiya.voiceassistant.domain.repository

import com.aatiya.voiceassistant.data.local.EncryptedSettingsStorage
import com.aatiya.voiceassistant.data.remote.GeminiLiveClient
import com.aatiya.voiceassistant.data.remote.GeminiRestClient
import com.aatiya.voiceassistant.domain.model.GeminiModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class SettingsRepository(
    private val storage: EncryptedSettingsStorage,
    private val restClient: GeminiRestClient,
    private val liveClientFactory: () -> GeminiLiveClient = { GeminiLiveClient() }
) {

    fun hasApiKey(): Boolean = storage.hasApiKey()

    fun getApiKey(): String? = storage.getApiKey()

    fun getMaskedApiKey(): String = storage.getMaskedApiKey()

    fun saveApiKey(apiKey: String): Boolean = storage.saveApiKey(apiKey)

    fun deleteApiKey() = storage.deleteApiKey()

    fun getTextModel(): String = storage.getTextModel()

    fun saveTextModel(model: String) = storage.saveTextModel(model)

    fun getLiveModel(): String = storage.getLiveModel()

    fun saveLiveModel(model: String) = storage.saveLiveModel(model)

    fun getVoice(): String = storage.getVoice()

    fun saveVoice(voice: String) = storage.saveVoice(voice)

    fun getSystemInstruction(): String = storage.getSystemInstruction()

    fun saveSystemInstruction(instruction: String) = storage.saveSystemInstruction(instruction)

    suspend fun testTextConnection(): Result<Pair<String, Long>> {
        val apiKey = getApiKey() ?: return Result.failure(IllegalStateException("No API key configured"))
        val model = getTextModel()
        return restClient.testTextConnection(apiKey, model)
    }

    suspend fun testLiveConnection(): Result<Long> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey() ?: return@withContext Result.failure(IllegalStateException("No API key configured"))
        val model = getLiveModel()
        val voice = getVoice()

        val client = liveClientFactory()
        val deferred = CompletableDeferred<Result<Long>>()
        val startTime = System.currentTimeMillis()

        client.setListener(object : GeminiLiveClient.Listener {
            override fun onConnected() {}

            override fun onSetupCompleted() {
                val latency = System.currentTimeMillis() - startTime
                deferred.complete(Result.success(latency))
                client.disconnect()
            }

            override fun onAudioReceived(pcmBytes: ByteArray) {}
            override fun onTranscriptReceived(text: String) {}
            override fun onInterrupted() {}
            override fun onTurnComplete() {}

            override fun onError(message: String) {
                deferred.complete(Result.failure(Exception(message)))
                client.disconnect()
            }

            override fun onDisconnected(reason: String) {
                if (!deferred.isCompleted) {
                    deferred.complete(Result.failure(Exception("Disconnected: $reason")))
                }
            }
        })

        client.connect(apiKey, model, voice)

        val result = withTimeoutOrNull(15000L) {
            deferred.await()
        } ?: run {
            client.disconnect()
            Result.failure(Exception("Live connection test timed out after 15 seconds."))
        }

        result
    }

    suspend fun fetchAvailableModels(): Result<List<GeminiModel>> {
        val apiKey = getApiKey() ?: return Result.failure(IllegalStateException("No API key configured"))
        return restClient.fetchAvailableModels(apiKey)
    }
}
