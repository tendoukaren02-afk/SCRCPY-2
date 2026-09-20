package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.entity.MirrorProfile
import com.example.engine.AudioCaptureEngine
import com.example.engine.VideoEncoderEngine
import com.example.server.HttpStreamServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StreamStatus(
    val isStreaming: Boolean = false,
    val profileName: String = "",
    val fps: Int = 0,
    val bitrateMbps: Int = 0,
    val audioEnabled: Boolean = true,
    val audioCodec: String = "opus",
    val clientsCount: Int = 0,
    val totalBytesTransferred: Long = 0L,
    val totalFramesSent: Long = 0L,
    val totalAudioPacketsSent: Long = 0L,
    val serverPort: Int = 8080,
    val durationSeconds: Long = 0L
)

class ScreenMirrorService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var wakeLock: PowerManager.WakeLock? = null

    private var mediaProjection: MediaProjection? = null
    private var videoEncoder: VideoEncoderEngine? = null
    private var audioCapture: AudioCaptureEngine? = null
    private var streamServer: HttpStreamServer? = null

    private var timerJob: kotlinx.coroutines.Job? = null
    private var startTimeMillis = 0L

    companion object {
        const val ACTION_START = "com.example.action.START_STREAM"
        const val ACTION_STOP = "com.example.action.STOP_STREAM"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"

        const val NOTIFICATION_CHANNEL_ID = "scrcpy_stream_channel"
        const val NOTIFICATION_ID = 1001

        private val _streamStatus = MutableStateFlow(StreamStatus())
        val streamStatus = _streamStatus.asStateFlow()

        var currentProfile: MirrorProfile? = null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_RESULT_DATA)
                }

                if (resultCode != 0 && resultData != null) {
                    startStreaming(resultCode, resultData)
                } else {
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                stopStreaming()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startStreaming(resultCode: Int, resultData: Intent) {
        val profile = currentProfile ?: MirrorProfile(
            name = "⚡ Balanced Pro (60 FPS)",
            maxFps = 60,
            videoBitrateMbps = 8
        )

        val notification = buildNotification("Mirroring Aktif: ${profile.name} (Port 8080)")
        startForeground(NOTIFICATION_ID, notification)

        // Acquire WakeLock if stayAwake enabled
        if (profile.stayAwake) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE, "ScrcpyMirror::StreamingLock")
            wakeLock?.acquire(10 * 60 * 60 * 1000L) // 10 hours max
        }

        try {
            val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)

            val port = 8080
            streamServer = HttpStreamServer(
                context = this,
                port = port,
                profile = profile,
                onClientCountChanged = { count ->
                    _streamStatus.value = _streamStatus.value.copy(clientsCount = count)
                }
            ).apply { start() }

            videoEncoder = VideoEncoderEngine(
                context = this,
                mediaProjection = mediaProjection!!,
                profile = profile,
                onVideoFrame = { data, isKeyFrame, timestampUs ->
                    streamServer?.broadcastVideoFrame(data, isKeyFrame, timestampUs)
                }
            ).apply { start() }

            if (profile.enableAudio) {
                audioCapture = AudioCaptureEngine(
                    context = this,
                    mediaProjection = mediaProjection,
                    profile = profile,
                    onAudioChunk = { data, sampleRate, channels, timestampUs ->
                        streamServer?.broadcastAudioChunk(data, sampleRate, channels, timestampUs)
                    }
                ).apply { start() }
            }

            startTimeMillis = System.currentTimeMillis()
            _streamStatus.value = StreamStatus(
                isStreaming = true,
                profileName = profile.name,
                fps = profile.maxFps,
                bitrateMbps = profile.videoBitrateMbps,
                audioEnabled = profile.enableAudio,
                audioCodec = profile.audioCodec,
                serverPort = port
            )

            startMonitoringLoop()

        } catch (e: Exception) {
            stopStreaming()
            stopSelf()
        }
    }

    private fun startMonitoringLoop() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val duration = (System.currentTimeMillis() - startTimeMillis) / 1000
                val server = streamServer ?: break
                _streamStatus.value = _streamStatus.value.copy(
                    durationSeconds = duration,
                    totalBytesTransferred = server.totalBytesSent.get(),
                    totalFramesSent = server.videoFramesSent.get(),
                    totalAudioPacketsSent = server.audioPacketsSent.get()
                )
            }
        }
    }

    private fun stopStreaming() {
        timerJob?.cancel()
        timerJob = null

        try { audioCapture?.stop() } catch (e: Exception) {}
        audioCapture = null

        try { videoEncoder?.stop() } catch (e: Exception) {}
        videoEncoder = null

        try { streamServer?.stop() } catch (e: Exception) {}
        streamServer = null

        try { mediaProjection?.stop() } catch (e: Exception) {}
        mediaProjection = null

        if (wakeLock?.isHeld == true) {
            try { wakeLock?.release() } catch (e: Exception) {}
        }
        wakeLock = null

        _streamStatus.value = StreamStatus(isStreaming = false)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Scrcpy Mirroring Stream Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Status streaming performa tinggi ke PC via USB"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, ScreenMirrorService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("⚡ Scrcpy USB Mirror Running")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Stream", stopIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopStreaming()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
