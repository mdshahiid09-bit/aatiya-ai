package com.aatiya.voiceassistant.domain.model

sealed interface ConnectionState {
    object Disconnected : ConnectionState
    object Connecting : ConnectionState
    data class Connected(val latencyMs: Long? = null) : ConnectionState
    data class Error(val message: String, val code: Int? = null) : ConnectionState
}
