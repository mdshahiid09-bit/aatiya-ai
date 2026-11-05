package com.aatiya.voiceassistant

import com.aatiya.voiceassistant.data.local.EncryptedSettingsStorage
import com.aatiya.voiceassistant.domain.model.GeminiModel
import com.aatiya.voiceassistant.domain.model.SupportedVoices
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelConfigTest {

    @Test
    fun testDefaultModelConstants() {
        assertEquals("gemini-2.0-flash", EncryptedSettingsStorage.DEFAULT_TEXT_MODEL)
        assertEquals("gemini-2.0-flash-exp", EncryptedSettingsStorage.DEFAULT_LIVE_MODEL)
        assertEquals("Aoede", SupportedVoices.DEFAULT_VOICE)
    }

    @Test
    fun testSupportedVoicesList() {
        val voices = SupportedVoices.ALL
        assertEquals(5, voices.size)
        assertTrue(voices.any { it.name == "Aoede" })
        assertTrue(voices.any { it.name == "Charon" })
        assertTrue(voices.any { it.name == "Fenrir" })
        assertTrue(voices.any { it.name == "Kore" })
        assertTrue(voices.any { it.name == "Puck" })
    }

    @Test
    fun testGeminiModelCleanId() {
        val modelWithPrefix = GeminiModel(
            id = "models/gemini-2.0-flash",
            displayName = "Gemini 2.0 Flash",
            supportsLive = true
        )
        assertEquals("gemini-2.0-flash", modelWithPrefix.cleanId)

        val modelWithoutPrefix = GeminiModel(
            id = "gemini-1.5-flash",
            displayName = "Gemini 1.5 Flash",
            supportsLive = false
        )
        assertEquals("gemini-1.5-flash", modelWithoutPrefix.cleanId)
    }
}
