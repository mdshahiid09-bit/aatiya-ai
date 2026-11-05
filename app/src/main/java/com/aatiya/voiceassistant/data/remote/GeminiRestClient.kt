package com.aatiya.voiceassistant.data.remote

import com.aatiya.voiceassistant.domain.model.GeminiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiRestClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()
) {

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    /**
     * Tests the Text connection using the specified API key and model.
     * Returns Result<Pair<ResponseText, LatencyMs>>
     */
    suspend fun testTextConnection(
        apiKey: String,
        model: String
    ): Result<Pair<String, Long>> = withContext(Dispatchers.IO) {
        val cleanModel = model.trim().removePrefix("models/")
        val startTime = System.currentTimeMillis()
        try {
            val prompt = "Hello! Please reply in one short sentence confirming you are online and working."
            val responseText = generateContentInternal(apiKey, cleanModel, prompt, null)
            val latency = System.currentTimeMillis() - startTime
            Result.success(Pair(responseText, latency))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates a text response from Gemini given a prompt and optional system instruction.
     */
    suspend fun generateContent(
        apiKey: String,
        model: String,
        prompt: String,
        systemInstruction: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanModel = model.trim().removePrefix("models/")
        try {
            val responseText = generateContentInternal(apiKey, cleanModel, prompt, systemInstruction)
            Result.success(responseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateContentInternal(
        apiKey: String,
        model: String,
        prompt: String,
        systemInstruction: String?
    ): String {
        val url = "$BASE_URL/models/$model:generateContent?key=${apiKey.trim()}"

        val rootJson = JSONObject()

        // System instruction if provided
        if (!systemInstruction.isNullOrBlank()) {
            val sysInstructionObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", systemInstruction))
            sysInstructionObj.put("parts", sysParts)
            rootJson.put("systemInstruction", sysInstructionObj)
        }

        // Contents
        val contentsArray = JSONArray()
        val userContent = JSONObject()
        userContent.put("role", "user")
        val partsArray = JSONArray()
        partsArray.put(JSONObject().put("text", prompt))
        userContent.put("parts", partsArray)
        contentsArray.put(userContent)
        rootJson.put("contents", contentsArray)

        val requestBody = rootJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", apiKey.trim())
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        var response = client.newCall(request).execute()
        var bodyString = response.body?.string() ?: ""

        // If 400 Bad Request and systemInstruction was provided, retry with instruction merged into prompt
        if (!response.isSuccessful && response.code == 400 && !systemInstruction.isNullOrBlank()) {
            val retryJson = JSONObject()
            val retryContents = JSONArray()
            val retryUser = JSONObject().put("role", "user")
            val combinedPrompt = "$systemInstruction\n\n$prompt"
            retryUser.put("parts", JSONArray().put(JSONObject().put("text", combinedPrompt)))
            retryContents.put(retryUser)
            retryJson.put("contents", retryContents)

            val retryRequest = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey.trim())
                .addHeader("Content-Type", "application/json")
                .post(retryJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val retryResponse = client.newCall(retryRequest).execute()
            if (retryResponse.isSuccessful) {
                response = retryResponse
                bodyString = retryResponse.body?.string() ?: ""
            }
        }

        if (!response.isSuccessful) {
            val errorMessage = parseErrorMessage(bodyString, response.code)
            throw IOException(errorMessage)
        }

        val json = JSONObject(bodyString)
        val candidates = json.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            val promptFeedback = json.optJSONObject("promptFeedback")
            val blockReason = promptFeedback?.optString("blockReason")
            if (!blockReason.isNullOrBlank()) {
                throw IOException("Response blocked by safety filters: $blockReason")
            }
            throw IOException("No candidate response generated by Gemini.")
        }

        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        if (parts == null || parts.length() == 0) {
            throw IOException("Empty response content from Gemini.")
        }

        val sb = StringBuilder()
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            sb.append(part.optString("text"))
        }
        return sb.toString().trim()
    }

    suspend fun fetchAvailableModels(apiKey: String): Result<List<GeminiModel>> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL/models?key=${apiKey.trim()}&pageSize=100"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("x-goog-api-key", apiKey.trim())
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val bodyString = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val err = parseErrorMessage(bodyString, response.code)
                    return@withContext Result.failure(IOException(err))
                }

                val json = JSONObject(bodyString)
                val modelsArray = json.optJSONArray("models") ?: JSONArray()
                val list = mutableListOf<GeminiModel>()

                for (i in 0 until modelsArray.length()) {
                    val m = modelsArray.getJSONObject(i)
                    val id = m.optString("name")
                    val displayName = m.optString("displayName", id)
                    val description = m.optString("description", "")
                    val methodsArray = m.optJSONArray("supportedGenerationMethods")
                    val methods = mutableListOf<String>()
                    if (methodsArray != null) {
                        for (j in 0 until methodsArray.length()) {
                            methods.add(methodsArray.getString(j))
                        }
                    }
                    val isLiveCandidate = id.contains("2.0") || id.contains("live") || id.contains("realtime")
                    list.add(
                        GeminiModel(
                            id = id,
                            displayName = displayName,
                            description = description,
                            supportedGenerationMethods = methods,
                            supportsLive = isLiveCandidate
                        )
                    )
                }

                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun requestEphemeralToken(apiKey: String): Result<String?> =
        withContext(Dispatchers.IO) {
            Result.success(null)
        }

    private fun parseErrorMessage(responseBody: String, statusCode: Int): String {
        return try {
            val json = JSONObject(responseBody)
            val error = json.optJSONObject("error")
            val message = error?.optString("message")
            val status = error?.optString("status")
            when {
                statusCode == 400 && message?.contains("API_KEY_INVALID", ignoreCase = true) == true ->
                    "Invalid API key. Please check your Gemini API key in settings."
                statusCode == 403 ->
                    "Access Forbidden (403): Quota exceeded or permission denied. (${message ?: "Check Google AI Studio project status"})"
                statusCode == 404 ->
                    "Model not found (404): ${message ?: "The selected model ID is not available for this key."}"
                statusCode == 429 ->
                    "Rate limit exceeded (429): Quota limit reached. Please wait a moment."
                !message.isNullOrBlank() -> "Gemini API Error ($statusCode): $message"
                else -> "HTTP $statusCode error from Gemini API"
            }
        } catch (_: Exception) {
            "HTTP $statusCode error: $responseBody"
        }
    }
}
