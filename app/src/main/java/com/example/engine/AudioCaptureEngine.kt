package com.example.engine

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.Build
import com.example.data.entity.MirrorProfile
import java.util.concurrent.atomic.AtomicBoolean

class AudioCaptureEngine(
    private val context: Context,
    private val mediaProjection: MediaProjection?,
    private val profile: MirrorProfile,
    private val onAudioChunk: (data: ByteArray, sampleRate: Int, channels: Int, timestampUs: Long) -> Unit
) {
    private var audioRecord: AudioRecord? = null
    private val isRunning = AtomicBoolean(false)
    private var audioThread: Thread? = null

    private val sampleRate = 48000
    private val channelConfig = AudioFormat.CHANNEL_IN_STEREO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val channelCount = 2

    @SuppressLint("MissingPermission")
    fun start() {
        if (isRunning.get() || !profile.enableAudio) return
        isRunning.set(true)

        try {
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = maxOf(minBufferSize * 2, 4096)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mediaProjection != null && profile.audioSource == "playback") {
                // Internal audio capture via AudioPlaybackCapture API (Android 10+)
                val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                    .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                    .addMatchingUsage(AudioAttributes.USAGE_GAME)
                    .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                    .build()

                val audioFormatBuilder = AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build()

                audioRecord = AudioRecord.Builder()
                    .setAudioFormat(audioFormatBuilder)
                    .setBufferSizeInBytes(bufferSize)
                    .setAudioPlaybackCaptureConfig(config)
                    .build()
            } else {
                // Microphone input or pre-Android 10 fallback
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )
            }

            audioRecord?.startRecording()
            startCaptureThread(bufferSize)
        } catch (e: Exception) {
            // Audio capture might fail if permission denied or projection not allowed; continue gracefully
            isRunning.set(false)
        }
    }

    private fun startCaptureThread(bufferSize: Int) {
        audioThread = Thread {
            val buffer = ByteArray(bufferSize)
            val record = audioRecord ?: return@Thread

            while (isRunning.get()) {
                try {
                    val readBytes = record.read(buffer, 0, buffer.size)
                    if (readBytes > 0) {
                        val chunk = ByteArray(readBytes)
                        System.arraycopy(buffer, 0, chunk, 0, readBytes)
                        val timestampUs = System.nanoTime() / 1000
                        onAudioChunk(chunk, sampleRate, channelCount, timestampUs)
                    }
                } catch (e: Exception) {
                    if (!isRunning.get()) break
                }
            }
        }.apply {
            name = "ScrcpyAudioCaptureThread"
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stop() {
        if (!isRunning.getAndSet(false)) return

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) { /* ignore */ }

        try {
            audioThread?.interrupt()
            audioThread = null
        } catch (e: Exception) { /* ignore */ }
    }
}
