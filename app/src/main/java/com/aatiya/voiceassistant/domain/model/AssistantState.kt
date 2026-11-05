package com.aatiya.voiceassistant.domain.model

/**
 * Represents the current operational state of the AATIYA AI voice assistant.
 * Every state directly reflects actual hardware and network state.
 */
enum class AssistantState(val label: String) {
    /** Inactive, microphone closed, audio player idle */
    IDLE("Idle"),

    /** Establishing secure WebSocket session to Gemini Live */
    CONNECTING("Connecting..."),

    /** Microphone is actively recording and streaming PCM audio */
    LISTENING("Listening..."),

    /** Audio sent, waiting for Gemini response generation */
    PROCESSING("Thinking..."),

    /** AudioTrack is actively playing incoming PCM audio from Gemini */
    SPEAKING("Speaking..."),

    /** An error occurred during connection, streaming, or playback */
    ERROR("Error")
}
