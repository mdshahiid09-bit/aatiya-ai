package com.aatiya.voiceassistant.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

/**
 * Manages low-latency native audio playback via Android AudioTrack.
 * Plays 16-bit little-endian PCM at 24,000 Hz Mono (Gemini Live output format).
 * Supports instant barge-in interruption.
 */
class AudioPlayer {

    companion object {
        const val SAMPLE_RATE = 24000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioTrack: AudioTrack? = null
    private val audioQueue = Channel<ByteArray>(Channel.UNLIMITED)
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    fun start() {
        if (audioTrack != null) return

        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )
        val bufferSize = maxOf(minBufferSize * 2, 8192)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AUDIO_FORMAT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(CHANNEL_CONFIG)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        track.play()
        audioTrack = track

        playbackJob = scope.launch {
            for (chunk in audioQueue) {
                if (!isActive) break
                if (chunk.isNotEmpty()) {
                    _isPlaying.value = true
                    calculateRms(chunk)
                    track.write(chunk, 0, chunk.size)
                }
                if (audioQueue.isEmpty) {
                    _isPlaying.value = false
                    _amplitude.value = 0f
                }
            }
        }
    }

    fun enqueueAudio(pcmBytes: ByteArray) {
        if (pcmBytes.isEmpty()) return
        start()
        audioQueue.trySend(pcmBytes)
    }

    fun interruptAndClear() {
        while (!audioQueue.isEmpty) {
            audioQueue.tryReceive()
        }
        try {
            audioTrack?.apply {
                pause()
                flush()
                play()
            }
        } catch (_: Exception) {
        }
        _isPlaying.value = false
        _amplitude.value = 0f
    }

    fun stop() {
        interruptAndClear()
        playbackJob?.cancel()
        playbackJob = null

        try {
            audioTrack?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {
        }
        audioTrack = null
        _isPlaying.value = false
        _amplitude.value = 0f
    }

    private fun calculateRms(buffer: ByteArray) {
        val shortBuffer = ByteBuffer.wrap(buffer)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()

        var sumSquare = 0.0
        val sampleCount = shortBuffer.remaining()
        while (shortBuffer.hasRemaining()) {
            val sample = shortBuffer.get()
            sumSquare += sample * sample
        }

        if (sampleCount > 0) {
            val rms = sqrt(sumSquare / sampleCount)
            val normalized = (rms / 8000.0).toFloat().coerceIn(0f, 1f)
            _amplitude.value = normalized
        }
    }
}
