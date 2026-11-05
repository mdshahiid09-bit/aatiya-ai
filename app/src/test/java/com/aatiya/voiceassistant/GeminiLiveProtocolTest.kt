package com.aatiya.voiceassistant

import com.aatiya.voiceassistant.data.remote.GeminiLiveClient
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiLiveProtocolTest {

    @Test
    fun testSetupPayloadGeneration() {
        val modelPath = "models/gemini-2.0-flash-exp"
        val voiceName = "Aoede"
        val systemPrompt = "You are AATIYA AI for MD Shahid"

        val payload = GeminiLiveClient.ProtocolHelper.createSetupPayload(
            modelPath = modelPath,
            voiceName = voiceName,
            systemInstruction = systemPrompt
        )

        val json = JSONObject(payload)
        assertTrue(json.has("setup"))

        val setup = json.getJSONObject("setup")
        assertEquals(modelPath, setup.getString("model"))

        val genConfig = setup.getJSONObject("generationConfig")
        val modalities = genConfig.getJSONArray("responseModalities")
        assertEquals("AUDIO", modalities.getString(0))

        val speechConfig = genConfig.getJSONObject("speechConfig")
        val voiceConfig = speechConfig.getJSONObject("voiceConfig")
        val prebuilt = voiceConfig.getJSONObject("prebuiltVoiceConfig")
        assertEquals("Aoede", prebuilt.getString("voiceName"))

        val sysInst = setup.getJSONObject("systemInstruction")
        val parts = sysInst.getJSONArray("parts")
        assertEquals(systemPrompt, parts.getJSONObject(0).getString("text"))
    }

    @Test
    fun testRealtimeAudioPayloadGeneration() {
        val dummyBase64 = "UklGRiQAAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQAAAAA="
        val payload = GeminiLiveClient.ProtocolHelper.createRealtimeAudioPayload(dummyBase64)

        val json = JSONObject(payload)
        assertTrue(json.has("realtimeInput"))

        val realtimeInput = json.getJSONObject("realtimeInput")
        val chunks = realtimeInput.getJSONArray("mediaChunks")
        assertEquals(1, chunks.length())

        val firstChunk = chunks.getJSONObject(0)
        assertEquals("audio/pcm;rate=16000", firstChunk.getString("mimeType"))
        assertEquals(dummyBase64, firstChunk.getString("data"))
    }

    @Test
    fun testServerResponseParsing() {
        val serverJson = """
            {
                "serverContent": {
                    "modelTurn": {
                        "parts": [
                            {
                                "inlineData": {
                                    "mimeType": "audio/pcm;rate=24000",
                                    "data": "AAAA////"
                                }
                            },
                            {
                                "text": "Hello Shahid! I am Aatiya AI."
                            }
                        ]
                    },
                    "turnComplete": true
                }
            }
        """.trimIndent()

        val root = JSONObject(serverJson)
        val serverContent = root.getJSONObject("serverContent")
        val modelTurn = serverContent.getJSONObject("modelTurn")
        val parts = modelTurn.getJSONArray("parts")

        assertEquals(2, parts.length())
        val audioPart = parts.getJSONObject(0).getJSONObject("inlineData")
        assertEquals("audio/pcm;rate=24000", audioPart.getString("mimeType"))
        assertEquals("AAAA////", audioPart.getString("data"))

        val textPart = parts.getJSONObject(1)
        assertEquals("Hello Shahid! I am Aatiya AI.", textPart.getString("text"))
        assertTrue(serverContent.getBoolean("turnComplete"))
    }

    @Test
    fun testInterruptionPayloadParsing() {
        val interruptJson = """
            {
                "serverContent": {
                    "interrupted": true
                }
            }
        """.trimIndent()

        val root = JSONObject(interruptJson)
        val serverContent = root.getJSONObject("serverContent")
        assertTrue(serverContent.getBoolean("interrupted"))
    }
}
