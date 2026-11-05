package com.aatiya.voiceassistant.di

import android.content.Context
import com.aatiya.voiceassistant.audio.AudioPlayer
import com.aatiya.voiceassistant.audio.AudioRecorder
import com.aatiya.voiceassistant.data.local.EncryptedSettingsStorage
import com.aatiya.voiceassistant.data.remote.GeminiLiveClient
import com.aatiya.voiceassistant.data.remote.GeminiRestClient
import com.aatiya.voiceassistant.domain.repository.LiveAssistantRepository
import com.aatiya.voiceassistant.domain.repository.SettingsRepository
import com.aatiya.voiceassistant.security.KeystoreManager

class AppContainer(val context: Context) {

    val keystoreManager by lazy {
        KeystoreManager()
    }

    val encryptedSettingsStorage by lazy {
        EncryptedSettingsStorage(context, keystoreManager)
    }

    val geminiRestClient by lazy {
        GeminiRestClient()
    }

    val geminiLiveClient by lazy {
        GeminiLiveClient()
    }

    val audioRecorder by lazy {
        AudioRecorder()
    }

    val audioPlayer by lazy {
        AudioPlayer()
    }

    val settingsRepository by lazy {
        SettingsRepository(
            storage = encryptedSettingsStorage,
            restClient = geminiRestClient,
            liveClientFactory = { GeminiLiveClient() }
        )
    }

    val liveAssistantRepository by lazy {
        LiveAssistantRepository(
            settingsRepository = settingsRepository,
            restClient = geminiRestClient,
            liveClient = geminiLiveClient,
            audioRecorder = audioRecorder,
            audioPlayer = audioPlayer
        )
    }
}
