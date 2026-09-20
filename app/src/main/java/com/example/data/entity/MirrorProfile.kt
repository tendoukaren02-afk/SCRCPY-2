package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mirror_profiles")
data class MirrorProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val maxFps: Int = 60, // 30, 60, 90, 120, 144
    val maxSize: Int = 1080, // 0 (native), 720, 1080, 1440, 2160 (4K)
    val videoBitrateMbps: Int = 8, // 2 to 50 Mbps
    val videoCodec: String = "h264", // h264, h265, av1
    val enableAudio: Boolean = true,
    val audioSource: String = "playback", // playback (Internal Game/App Audio), mic (Microphone)
    val audioCodec: String = "opus", // opus, aac, raw, flac
    val audioBitrateKbps: Int = 128, // 64, 128, 192, 256, 320
    val audioBufferMs: Int = 50, // 20ms to 200ms
    val audioDup: Boolean = false, // --audio-dup (Duplicate sound on device & PC)
    val turnScreenOff: Boolean = false, // --turn-screen-off
    val stayAwake: Boolean = true, // --stay-awake
    val showTouches: Boolean = false, // --show-touches
    val lowLatencyBuffer: Boolean = true, // --video-buffer=0
    val fullscreen: Boolean = false,
    val alwaysOnTop: Boolean = false,
    val recordToFile: Boolean = false,
    val customArgs: String = "",
    val isPreset: Boolean = false,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
