package com.example.util

import com.example.data.entity.MirrorProfile

object AdbCommandGenerator {

    enum class Platform(val label: String, val icon: String, val extension: String) {
        WINDOWS_CMD("Windows CMD / Batch", "🪟", "bat"),
        WINDOWS_POWERSHELL("PowerShell", "⚡", "ps1"),
        LINUX_MAC("macOS / Linux Bash", "🐧", "sh")
    }

    fun buildScrcpyCommand(profile: MirrorProfile): String {
        val args = mutableListOf<String>()
        args.add("scrcpy")

        // Video Flags
        if (profile.maxSize > 0) {
            args.add("-m ${profile.maxSize}")
        }
        if (profile.maxFps > 0) {
            args.add("--max-fps=${profile.maxFps}")
        }
        if (profile.videoBitrateMbps > 0) {
            args.add("-b ${profile.videoBitrateMbps}M")
        }
        if (profile.videoCodec.isNotBlank()) {
            args.add("--video-codec=${profile.videoCodec.lowercase()}")
        }
        if (profile.lowLatencyBuffer) {
            args.add("--video-buffer=0")
        }

        // Audio Flags (Scrcpy 2.0+)
        if (!profile.enableAudio) {
            args.add("--no-audio")
        } else {
            if (profile.audioSource == "mic") {
                args.add("--audio-source=mic")
            } else {
                args.add("--audio-source=playback")
            }
            if (profile.audioCodec.isNotBlank()) {
                args.add("--audio-codec=${profile.audioCodec.lowercase()}")
            }
            if (profile.audioBitrateKbps > 0) {
                args.add("--audio-bit-rate=${profile.audioBitrateKbps}K")
            }
            if (profile.audioBufferMs > 0) {
                args.add("--audio-buffer=${profile.audioBufferMs}")
            }
            if (profile.audioDup) {
                args.add("--audio-dup")
            }
        }

        // Device & Display Controls
        if (profile.turnScreenOff) {
            args.add("-S") // --turn-screen-off
        }
        if (profile.stayAwake) {
            args.add("-w") // --stay-awake
        }
        if (profile.showTouches) {
            args.add("-t") // --show-touches
        }
        if (profile.fullscreen) {
            args.add("-f") // --fullscreen
        }
        if (profile.alwaysOnTop) {
            args.add("--always-on-top")
        }
        if (profile.recordToFile) {
            args.add("--record=mirror_recording_%DATE%.mp4")
        }

        if (profile.customArgs.isNotBlank()) {
            args.add(profile.customArgs.trim())
        }

        return args.joinToString(" ")
    }

    fun generateFullScript(profile: MirrorProfile, platform: Platform): String {
        val baseCmd = buildScrcpyCommand(profile)
        return when (platform) {
            Platform.WINDOWS_CMD -> """
@echo off
title Scrcpy Ultra Low-Latency USB Mirror (${profile.name})
echo ========================================================
echo  [SCRCPY USB MIRROR] High Performance & Low Latency
echo  Preset: ${profile.name}
echo  Audio: ${if (profile.enableAudio) "${profile.audioCodec.uppercase()} ${profile.audioBitrateKbps}kbps (${profile.audioSource})" else "Disabled"}
echo  Video: ${if (profile.maxSize > 0) "${profile.maxSize}p" else "Native"} @ ${profile.maxFps}fps (${profile.videoBitrateMbps}Mbps ${profile.videoCodec.uppercase()})
echo ========================================================
echo.
echo [*] Checking connected ADB USB devices...
adb devices
echo.
echo [*] Launching scrcpy with optimal latency parameters...
$baseCmd
echo.
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [!] Scrcpy exited with error code %ERRORLEVEL%.
    echo [!] Make sure USB Debugging is ON and Scrcpy is installed on PC.
    pause
)
""".trimIndent()

            Platform.WINDOWS_POWERSHELL -> """
# Scrcpy Ultra Low-Latency USB Mirror (${profile.name})
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host " [SCRCPY USB MIRROR] High Performance & Low Latency" -ForegroundColor Cyan
Write-Host " Preset: ${profile.name}" -ForegroundColor Yellow
Write-Host " Audio: ${if (profile.enableAudio) "${profile.audioCodec.uppercase()} ${profile.audioBitrateKbps}kbps" else "Disabled"}" -ForegroundColor Green
Write-Host " Video: ${profile.maxFps} FPS | ${profile.videoBitrateMbps} Mbps | ${profile.videoCodec.uppercase()}" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "`n[*] Verifying ADB Connection..." -ForegroundColor Gray
adb devices
Write-Host "`n[*] Starting Scrcpy streaming session..." -ForegroundColor Yellow
$baseCmd
""".trimIndent()

            Platform.LINUX_MAC -> """
#!/usr/bin/env bash
# Scrcpy Ultra Low-Latency USB Mirror (${profile.name})
echo -e "\033[1;36m========================================================\033[0m"
echo -e "\033[1;36m [SCRCPY USB MIRROR] High Performance & Low Latency\033[0m"
echo -e "\033[1;33m Preset: ${profile.name}\033[0m"
echo -e "\033[1;32m Audio: ${if (profile.enableAudio) "${profile.audioCodec.uppercase()} ${profile.audioBitrateKbps}kbps" else "Disabled"}\033[0m"
echo -e "\033[1;36m========================================================\033[0m"

echo -e "\n\033[0;34m[*] Verifying connected USB device via ADB...\033[0m"
adb devices

echo -e "\n\033[0;32m[*] Executing scrcpy...\033[0m"
$baseCmd
""".trimIndent()
        }
    }

    fun getCommandExplanation(profile: MirrorProfile): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        list.add("scrcpy" to "Perintah utama menjalankan scrcpy client di PC")
        if (profile.maxSize > 0) {
            list.add("-m ${profile.maxSize}" to "Membatasi resolusi sisi terpanjang ke ${profile.maxSize}px untuk mengurangi beban render & menekan latency")
        }
        if (profile.maxFps > 0) {
            list.add("--max-fps=${profile.maxFps}" to "Mengunci frame rate ke ${profile.maxFps} FPS untuk pergerakan sangat mulus")
        }
        if (profile.videoBitrateMbps > 0) {
            list.add("-b ${profile.videoBitrateMbps}M" to "Bitrate video ${profile.videoBitrateMbps} Mbps untuk gambar jernih tanpa delay")
        }
        list.add("--video-codec=${profile.videoCodec.lowercase()}" to "Codec ${profile.videoCodec.uppercase()}: akselerasi hardware langsung dari chip GPU/VPU")
        if (profile.lowLatencyBuffer) {
            list.add("--video-buffer=0" to "Zero buffering untuk latency terendah (instant frame display)")
        }
        if (profile.enableAudio) {
            list.add("--audio-source=${profile.audioSource}" to if (profile.audioSource == "playback") "Mirroring audio internal game & media langsung via AudioPlaybackCapture" else "Audio mic input")
            list.add("--audio-codec=${profile.audioCodec.lowercase()}" to "Codec audio ${profile.audioCodec.uppercase()} dengan bitrate ${profile.audioBitrateKbps}kbps")
            list.add("--audio-buffer=${profile.audioBufferMs}" to "Buffer audio kecil (${profile.audioBufferMs}ms) agar suara sinkron sempurna dengan gambar")
            if (profile.audioDup) {
                list.add("--audio-dup" to "Double Audio: Suara tetap berbunyi di speaker HP sekaligus keluar di speaker PC secara bersamaan")
            }
        } else {
            list.add("--no-audio" to "Mematikan streaming audio jika hanya butuh mirroring layar")
        }
        if (profile.turnScreenOff) {
            list.add("-S (--turn-screen-off)" to "Mematikan layar HP saat mirroring aktif untuk hemat baterai dan menjaga HP tetap dingin")
        }
        if (profile.stayAwake) {
            list.add("-w (--stay-awake)" to "Mencegah layar HP terkunci / tidur saat terhubung USB")
        }
        if (profile.showTouches) {
            list.add("-t (--show-touches)" to "Menampilkan visual titik sentuhan jari di layar PC")
        }
        return list
    }
}
