package com.aatiya.voiceassistant.domain.model

data class GeminiModel(
    val id: String,
    val displayName: String,
    val description: String = "",
    val supportedGenerationMethods: List<String> = emptyList(),
    val supportsLive: Boolean = false
) {
    val cleanId: String get() = id.removePrefix("models/")
}

data class VoiceOption(
    val name: String,
    val description: String,
    val genderHint: String
)

object SupportedVoices {
    val ALL = listOf(
        VoiceOption("Aoede", "Warm, natural & conversational (Default)", "Neutral/Warm"),
        VoiceOption("Charon", "Deeper tone, steady and clear", "Deeper"),
        VoiceOption("Fenrir", "Strong, crisp articulation", "Crisp"),
        VoiceOption("Kore", "Gentle, balanced and calm", "Calm"),
        VoiceOption("Puck", "Energetic, dynamic and engaging", "Dynamic")
    )

    const val DEFAULT_VOICE = "Aoede"
}
