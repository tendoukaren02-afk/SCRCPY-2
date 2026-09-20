package com.example.engine

import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.os.Build
import android.util.DisplayMetrics
import android.view.Surface
import android.view.WindowManager
import com.example.data.entity.MirrorProfile
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class VideoEncoderEngine(
    private val context: Context,
    private val mediaProjection: MediaProjection,
    private val profile: MirrorProfile,
    private val onVideoFrame: (data: ByteArray, isKeyFrame: Boolean, timestampUs: Long) -> Unit
) {
    private var mediaCodec: MediaCodec? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var inputSurface: Surface? = null
    private val isRunning = AtomicBoolean(false)
    private var encoderThread: Thread? = null

    private var videoWidth = 1080
    private var videoHeight = 1920
    private var screenDensity = 320

    fun start() {
        if (isRunning.get()) return
        isRunning.set(true)

        calculateDimensions()
        setupEncoder()
        startVirtualDisplay()
        startDrainThread()
    }

    private fun calculateDimensions() {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)

        screenDensity = metrics.densityDpi
        val rawWidth = metrics.widthPixels
        val rawHeight = metrics.heightPixels

        val maxDimension = if (profile.maxSize > 0) profile.maxSize else maxOf(rawWidth, rawHeight)
        val isLandscape = rawWidth > rawHeight

        if (isLandscape) {
            val ratio = rawHeight.toDouble() / rawWidth.toDouble()
            videoWidth = (maxDimension / 16) * 16
            videoHeight = ((maxDimension * ratio).toInt() / 16) * 16
        } else {
            val ratio = rawWidth.toDouble() / rawHeight.toDouble()
            videoHeight = (maxDimension / 16) * 16
            videoWidth = ((maxDimension * ratio).toInt() / 16) * 16
        }

        // Guarantee even dimensions for H.264
        if (videoWidth % 2 != 0) videoWidth -= 1
        if (videoHeight % 2 != 0) videoHeight -= 1
        if (videoWidth <= 0) videoWidth = 720
        if (videoHeight <= 0) videoHeight = 1280
    }

    private fun setupEncoder() {
        val mimeType = if (profile.videoCodec.equals("h265", ignoreCase = true)) {
            MediaFormat.MIMETYPE_VIDEO_HEVC
        } else {
            MediaFormat.MIMETYPE_VIDEO_AVC
        }

        val format = MediaFormat.createVideoFormat(mimeType, videoWidth, videoHeight).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, profile.videoBitrateMbps * 1_000_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, profile.maxFps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1) // 1 second keyframe for fast recovery
            setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setInteger(MediaFormat.KEY_LATENCY, 0) // Realtime low latency hint
                setInteger(MediaFormat.KEY_PRIORITY, 0) // Highest priority
            }

            if (mimeType == MediaFormat.MIMETYPE_VIDEO_AVC) {
                setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline)
                setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel41)
            }
        }

        val codec = MediaCodec.createEncoderByType(mimeType)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = codec.createInputSurface()
        codec.start()
        mediaCodec = codec
    }

    private fun startVirtualDisplay() {
        val surface = inputSurface ?: return
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScrcpyMirrorVirtualDisplay",
            videoWidth,
            videoHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface,
            null,
            null
        )
    }

    private fun startDrainThread() {
        encoderThread = Thread {
            val codec = mediaCodec ?: return@Thread
            val bufferInfo = MediaCodec.BufferInfo()

            while (isRunning.get()) {
                try {
                    val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                    if (outputIndex >= 0) {
                        val outputBuffer = codec.getOutputBuffer(outputIndex)
                        if (outputBuffer != null && bufferInfo.size > 0) {
                            outputBuffer.position(bufferInfo.offset)
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size)

                            val outBytes = ByteArray(bufferInfo.size)
                            outputBuffer.get(outBytes)

                            val isKeyFrame = (bufferInfo.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0 ||
                                    (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0

                            onVideoFrame(outBytes, isKeyFrame, bufferInfo.presentationTimeUs)
                        }
                        codec.releaseOutputBuffer(outputIndex, false)
                    }
                } catch (e: Exception) {
                    if (!isRunning.get()) break
                }
            }
        }.apply {
            name = "ScrcpyVideoDrainThread"
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stop() {
        if (!isRunning.getAndSet(false)) return

        try {
            virtualDisplay?.release()
            virtualDisplay = null
        } catch (e: Exception) { /* ignore */ }

        try {
            inputSurface?.release()
            inputSurface = null
        } catch (e: Exception) { /* ignore */ }

        try {
            mediaCodec?.stop()
            mediaCodec?.release()
            mediaCodec = null
        } catch (e: Exception) { /* ignore */ }

        try {
            encoderThread?.interrupt()
            encoderThread = null
        } catch (e: Exception) { /* ignore */ }
    }
}
