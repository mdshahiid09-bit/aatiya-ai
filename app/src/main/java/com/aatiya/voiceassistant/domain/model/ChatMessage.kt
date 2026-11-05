package com.aatiya.voiceassistant.domain.model

import java.util.UUID

enum class MessageRole {
    USER,
    MODEL
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiveVoice: Boolean = false
)
