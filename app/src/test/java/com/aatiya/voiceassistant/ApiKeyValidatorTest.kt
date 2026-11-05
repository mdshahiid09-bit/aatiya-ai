package com.aatiya.voiceassistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiKeyValidatorTest {

    private fun isValidGeminiKey(key: String?): Boolean {
        if (key.isNullOrBlank()) return false
        val trimmed = key.trim()
        return trimmed.startsWith("AIzaSy") && trimmed.length >= 25
    }

    private fun maskApiKey(key: String?): String {
        if (key.isNullOrBlank()) return "Not configured"
        val trimmed = key.trim()
        return if (trimmed.length > 8) {
            "${trimmed.take(4)}••••••••${trimmed.takeLast(4)}"
        } else {
            "••••••••"
        }
    }

    @Test
    fun testValidGeminiApiKeyFormat() {
        val sampleKey = "AIzaSyD-1234567890abcdefghijklmnopqrstuv"
        assertTrue(isValidGeminiKey(sampleKey))
    }

    @Test
    fun testInvalidOrBlankGeminiApiKey() {
        assertFalse(isValidGeminiKey(""))
        assertFalse(isValidGeminiKey("   "))
        assertFalse(isValidGeminiKey(null))
        assertFalse(isValidGeminiKey("sk-someOpenAIKey1234567890"))
        assertFalse(isValidGeminiKey("AIzaSyTooShort"))
    }

    @Test
    fun testMaskApiKey() {
        val sampleKey = "AIzaSyD-1234567890abcdefghijklmnopqrstuv"
        val masked = maskApiKey(sampleKey)
        assertTrue(masked.startsWith("AIza"))
        assertTrue(masked.endsWith("stuv"))
        assertTrue(masked.contains("••••••••"))
        assertEquals("Not configured", maskApiKey(null))
        assertEquals("Not configured", maskApiKey(""))
    }
}
