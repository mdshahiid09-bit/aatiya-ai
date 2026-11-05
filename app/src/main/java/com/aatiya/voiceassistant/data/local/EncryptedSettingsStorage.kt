package com.aatiya.voiceassistant.data.local

import android.content.Context
import android.content.SharedPreferences
import com.aatiya.voiceassistant.domain.model.SupportedVoices
import com.aatiya.voiceassistant.security.KeystoreManager

class EncryptedSettingsStorage(
    context: Context,
    private val keystoreManager: KeystoreManager
) {

    companion object {
        private const val PREFS_NAME = "aatiya_secure_settings"
        private const val KEY_API_KEY_IV = "enc_api_key_iv"
        private const val KEY_API_KEY_CIPHER = "enc_api_key_cipher"
        private const val KEY_TEXT_MODEL = "text_model"
        private const val KEY_LIVE_MODEL = "live_model"
        private const val KEY_VOICE_NAME = "voice_name"
        private const val KEY_SYSTEM_INSTRUCTION = "system_instruction"

        const val DEFAULT_TEXT_MODEL = "gemini-2.0-flash"
        const val DEFAULT_LIVE_MODEL = "gemini-2.0-flash-exp"
        const val DEFAULT_SYSTEM_INSTRUCTION =
            "You are AATIYA AI, an intelligent, helpful, and natural conversational voice assistant created for MD Shahid. " +
            "You speak and understand Hindi, Hinglish, and English naturally and fluently. " +
            "Keep voice responses concise, conversational, friendly, and natural without reading out robotic code blocks or formatting symbols."
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun saveApiKey(apiKey: String): Boolean {
        val trimmed = apiKey.trim()
        if (trimmed.isEmpty()) return false
        return try {
            val (iv, cipher) = keystoreManager.encrypt(trimmed)
            prefs.edit()
                .putString(KEY_API_KEY_IV, iv)
                .putString(KEY_API_KEY_CIPHER, cipher)
                .commit()
        } catch (e: Exception) {
            false
        }
    }

    @Synchronized
    fun getApiKey(): String? {
        val iv = prefs.getString(KEY_API_KEY_IV, null) ?: return null
        val cipher = prefs.getString(KEY_API_KEY_CIPHER, null) ?: return null
        return keystoreManager.decrypt(iv, cipher)
    }

    @Synchronized
    fun hasApiKey(): Boolean {
        val key = getApiKey()
        return !key.isNullOrBlank()
    }

    @Synchronized
    fun deleteApiKey() {
        prefs.edit()
            .remove(KEY_API_KEY_IV)
            .remove(KEY_API_KEY_CIPHER)
            .apply()
    }

    fun getMaskedApiKey(): String {
        val key = getApiKey() ?: return "Not configured"
        return if (key.length > 8) {
            "${key.take(4)}••••••••${key.takeLast(4)}"
        } else {
            "••••••••"
        }
    }

    fun saveTextModel(model: String) {
        prefs.edit().putString(KEY_TEXT_MODEL, model.trim()).apply()
    }

    fun getTextModel(): String {
        return prefs.getString(KEY_TEXT_MODEL, DEFAULT_TEXT_MODEL) ?: DEFAULT_TEXT_MODEL
    }

    fun saveLiveModel(model: String) {
        prefs.edit().putString(KEY_LIVE_MODEL, model.trim()).apply()
    }

    fun getLiveModel(): String {
        return prefs.getString(KEY_LIVE_MODEL, DEFAULT_LIVE_MODEL) ?: DEFAULT_LIVE_MODEL
    }

    fun saveVoice(voice: String) {
        prefs.edit().putString(KEY_VOICE_NAME, voice.trim()).apply()
    }

    fun getVoice(): String {
        return prefs.getString(KEY_VOICE_NAME, SupportedVoices.DEFAULT_VOICE)
            ?: SupportedVoices.DEFAULT_VOICE
    }

    fun saveSystemInstruction(instruction: String) {
        prefs.edit().putString(KEY_SYSTEM_INSTRUCTION, instruction.trim()).apply()
    }

    fun getSystemInstruction(): String {
        return prefs.getString(KEY_SYSTEM_INSTRUCTION, DEFAULT_SYSTEM_INSTRUCTION)
            ?: DEFAULT_SYSTEM_INSTRUCTION
    }
}
