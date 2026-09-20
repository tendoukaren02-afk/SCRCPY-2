package com.example.data.repository

import com.example.data.dao.ConnectionLogDao
import com.example.data.dao.MirrorProfileDao
import com.example.data.entity.ConnectionLog
import com.example.data.entity.MirrorProfile
import kotlinx.coroutines.flow.Flow

class MirrorRepository(
    private val profileDao: MirrorProfileDao,
    private val logDao: ConnectionLogDao
) {
    val allProfiles: Flow<List<MirrorProfile>> = profileDao.getAllProfiles()
    val defaultProfile: Flow<MirrorProfile?> = profileDao.getDefaultProfile()
    val recentLogs: Flow<List<ConnectionLog>> = logDao.getRecentLogs()

    suspend fun initializeDefaultPresetsIfEmpty() {
        if (profileDao.getProfileCount() == 0) {
            val presets = listOf(
                MirrorProfile(
                    name = "🚀 Gaming Ultra (120 FPS)",
                    description = "Ultra-low latency for competitive mobile gaming (PUBG, MLBB, CODM). Requires high refresh rate display.",
                    maxFps = 120,
                    maxSize = 1080,
                    videoBitrateMbps = 16,
                    videoCodec = "h264",
                    enableAudio = true,
                    audioSource = "playback",
                    audioCodec = "opus",
                    audioBitrateKbps = 192,
                    audioBufferMs = 25,
                    turnScreenOff = true,
                    stayAwake = true,
                    showTouches = false,
                    lowLatencyBuffer = true,
                    isPreset = true,
                    isDefault = false
                ),
                MirrorProfile(
                    name = "⚡ Balanced Pro (60 FPS)",
                    description = "Recommended for general streaming, fast responsiveness with crisp 1080p visuals & rich Opus sound.",
                    maxFps = 60,
                    maxSize = 1080,
                    videoBitrateMbps = 8,
                    videoCodec = "h264",
                    enableAudio = true,
                    audioSource = "playback",
                    audioCodec = "opus",
                    audioBitrateKbps = 128,
                    audioBufferMs = 50,
                    turnScreenOff = false,
                    stayAwake = true,
                    showTouches = false,
                    lowLatencyBuffer = true,
                    isPreset = true,
                    isDefault = true
                ),
                MirrorProfile(
                    name = "🎮 Smooth Gaming (90 FPS)",
                    description = "Smooth 90Hz frame rate balancing bandwidth and responsiveness for high frame rate titles.",
                    maxFps = 90,
                    maxSize = 1080,
                    videoBitrateMbps = 12,
                    videoCodec = "h264",
                    enableAudio = true,
                    audioSource = "playback",
                    audioCodec = "opus",
                    audioBitrateKbps = 160,
                    audioBufferMs = 30,
                    turnScreenOff = true,
                    stayAwake = true,
                    showTouches = false,
                    lowLatencyBuffer = true,
                    isPreset = true,
                    isDefault = false
                ),
                MirrorProfile(
                    name = "🎬 Cinema & Presentation (4K / HEVC)",
                    description = "Crystal clear 4K 2160p resolution using high-efficiency H.265 / HEVC and studio-grade AAC audio.",
                    maxFps = 60,
                    maxSize = 2160,
                    videoBitrateMbps = 24,
                    videoCodec = "h265",
                    enableAudio = true,
                    audioSource = "playback",
                    audioCodec = "aac",
                    audioBitrateKbps = 256,
                    audioBufferMs = 60,
                    turnScreenOff = false,
                    stayAwake = true,
                    showTouches = true,
                    lowLatencyBuffer = false,
                    isPreset = true,
                    isDefault = false
                ),
                MirrorProfile(
                    name = "🎧 Audio Streamer (PC Soundbar)",
                    description = "Turns PC into high-quality wireless/USB speaker for your phone. Minimum video overhead.",
                    maxFps = 30,
                    maxSize = 720,
                    videoBitrateMbps = 2,
                    videoCodec = "h264",
                    enableAudio = true,
                    audioSource = "playback",
                    audioCodec = "opus",
                    audioBitrateKbps = 320,
                    audioBufferMs = 40,
                    turnScreenOff = true,
                    stayAwake = true,
                    showTouches = false,
                    lowLatencyBuffer = true,
                    isPreset = true,
                    isDefault = false
                ),
                MirrorProfile(
                    name = "🔋 Battery Saver (Low Latency)",
                    description = "Optimized 720p 30fps lightweight stream with screen turned off to keep device cool during long sessions.",
                    maxFps = 30,
                    maxSize = 720,
                    videoBitrateMbps = 4,
                    videoCodec = "h264",
                    enableAudio = true,
                    audioSource = "playback",
                    audioCodec = "opus",
                    audioBitrateKbps = 96,
                    audioBufferMs = 50,
                    turnScreenOff = true,
                    stayAwake = true,
                    showTouches = false,
                    lowLatencyBuffer = true,
                    isPreset = true,
                    isDefault = false
                )
            )
            profileDao.insertProfiles(presets)
        }
    }

    suspend fun saveProfile(profile: MirrorProfile): Long {
        return if (profile.id == 0L) {
            profileDao.insertProfile(profile)
        } else {
            profileDao.updateProfile(profile)
            profile.id
        }
    }

    suspend fun deleteProfile(profile: MirrorProfile) {
        profileDao.deleteProfile(profile)
    }

    suspend fun setDefaultProfile(id: Long) {
        profileDao.clearDefaultFlags()
        profileDao.setDefaultProfile(id)
    }

    suspend fun insertLog(log: ConnectionLog) = logDao.insertLog(log)
    suspend fun clearLogs() = logDao.clearAllLogs()
}
