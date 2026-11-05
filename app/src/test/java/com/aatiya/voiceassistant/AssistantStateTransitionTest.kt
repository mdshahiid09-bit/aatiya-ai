package com.aatiya.voiceassistant

import com.aatiya.voiceassistant.domain.model.AssistantState
import com.aatiya.voiceassistant.domain.model.ConnectionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantStateTransitionTest {

    @Test
    fun testAssistantStateOrder() {
        val states = listOf(
            AssistantState.IDLE,
            AssistantState.CONNECTING,
            AssistantState.LISTENING,
            AssistantState.PROCESSING,
            AssistantState.SPEAKING,
            AssistantState.ERROR
        )

        assertEquals("Idle", states[0].label)
        assertEquals("Connecting...", states[1].label)
        assertEquals("Listening...", states[2].label)
        assertEquals("Thinking...", states[3].label)
        assertEquals("Speaking...", states[4].label)
        assertEquals("Error", states[5].label)
    }

    @Test
    fun testConnectionStates() {
        val disconnected = ConnectionState.Disconnected
        val connecting = ConnectionState.Connecting
        val connected = ConnectionState.Connected(latencyMs = 120L)
        val error = ConnectionState.Error("Quota exceeded", 429)

        assertTrue(disconnected is ConnectionState.Disconnected)
        assertTrue(connecting is ConnectionState.Connecting)
        assertTrue(connected is ConnectionState.Connected)
        assertEquals(120L, (connected as ConnectionState.Connected).latencyMs)
        assertTrue(error is ConnectionState.Error)
        assertEquals("Quota exceeded", (error as ConnectionState.Error).message)
        assertEquals(429, error.code)
    }
}
