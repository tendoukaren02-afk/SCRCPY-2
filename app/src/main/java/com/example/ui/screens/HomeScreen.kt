package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MirrorProfile
import com.example.service.StreamStatus
import com.example.ui.components.CyberCard
import com.example.ui.components.MetricStatBox
import com.example.ui.components.PulseStatusBadge
import com.example.ui.components.copyToClipboard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AdbCommandGenerator
import com.example.util.DeviceHardwareInfo

@Composable
fun HomeScreen(
    profiles: List<MirrorProfile>,
    activeProfile: MirrorProfile?,
    hardwareInfo: DeviceHardwareInfo?,
    streamStatus: StreamStatus,
    onSelectProfile: (MirrorProfile) -> Unit,
    onStartStream: () -> Unit,
    onStopStream: () -> Unit,
    onOpenDeveloperSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAudioGuide by remember { mutableStateOf(false) }

    if (showAudioGuide) {
        AudioGuideDialog(onDismiss = { showAudioGuide = false })
    }

    val currentProfile = activeProfile ?: profiles.firstOrNull { it.isDefault } ?: profiles.firstOrNull()
    val scrcpyCmd = currentProfile?.let { AdbCommandGenerator.buildScrcpyCommand(it) } ?: "scrcpy"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Header Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "⚡ SCRCPY PRO",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = CyanPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Ultra Low-Latency USB Mirror & Audio",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = { showAudioGuide = true },
                modifier = Modifier
                    .background(DarkSurfaceElevated, CircleShape)
                    .border(1.dp, DarkBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Audio Guide",
                    tint = CyanPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // USB & ADB Connection Status Card
        CyberCard(
            borderColor = if (hardwareInfo?.isUsbConnected == true) EmeraldSuccess.copy(alpha = 0.5f) else DarkBorder
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Usb,
                            contentDescription = null,
                            tint = if (hardwareInfo?.isUsbConnected == true) EmeraldSuccess else AmberWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Status Koneksi USB & ADB",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    PulseStatusBadge(
                        isActive = streamStatus.isStreaming,
                        activeText = "STREAM LIVE",
                        inactiveText = "STANDBY"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // USB Status chip
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .padding(vertical = 8.dp, horizontal = 10.dp)
                    ) {
                        Column {
                            Text("USB Hardware", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = if (hardwareInfo?.isUsbConnected == true) "Terhubung (Ready)" else "Tancapkan Kabel",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hardwareInfo?.isUsbConnected == true) EmeraldSuccess else AmberWarning
                            )
                        }
                    }

                    // ADB Debugging chip
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .clickable { onOpenDeveloperSettings() }
                            .padding(vertical = 8.dp, horizontal = 10.dp)
                    ) {
                        Column {
                            Text("USB Debugging", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = if (hardwareInfo?.isAdbEnabled == true) "ADB Aktif" else "Buka Developer Mode",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hardwareInfo?.isAdbEnabled == true) EmeraldSuccess else CyanPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // START / STOP STREAM ENGINE BUTTON
        Button(
            onClick = {
                if (streamStatus.isStreaming) {
                    onStopStream()
                } else {
                    onStartStream()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (streamStatus.isStreaming) RoseError else CyanPrimary
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = if (streamStatus.isStreaming) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (streamStatus.isStreaming) Color.White else Color(0xFF0B0F19),
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (streamStatus.isStreaming) "STOP ACTIVE MIRRORING" else "START HIGH-PERFORMANCE MIRROR",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = if (streamStatus.isStreaming) Color.White else Color(0xFF0B0F19),
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // REALTIME TELEMETRY / STATS (When streaming)
        AnimatedVisibility(visible = streamStatus.isStreaming) {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "📊 LIVE TELEMETRY & HARDWARE STATS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricStatBox(
                        label = "Frame Rate",
                        value = "${streamStatus.fps} FPS",
                        accentColor = CyanPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatBox(
                        label = "Bitrate",
                        value = "${streamStatus.bitrateMbps} Mbps",
                        accentColor = EmeraldSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatBox(
                        label = "Data Terkirim",
                        value = "${(streamStatus.totalBytesTransferred / (1024 * 1024))} MB",
                        accentColor = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricStatBox(
                        label = "Latency",
                        value = "~6-12 ms",
                        accentColor = CyanPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatBox(
                        label = "Audio Feed",
                        value = if (streamStatus.audioEnabled) "Opus 48kHz" else "Disabled",
                        accentColor = if (streamStatus.audioEnabled) EmeraldSuccess else TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatBox(
                        label = "Klien PC",
                        value = "${streamStatus.clientsCount} Connected",
                        accentColor = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // PRESET SELECTOR CHIPS
        Text(
            text = "PILIH PRESET PERFORMA",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            profiles.forEach { profile ->
                val isSelected = currentProfile?.id == profile.id
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) CyanPrimary.copy(alpha = 0.2f) else DarkSurfaceElevated,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) CyanPrimary else DarkBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectProfile(profile) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = profile.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) CyanPrimary else TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${profile.maxFps}fps • ${if (profile.maxSize == 0) "Native" else "${profile.maxSize}p"} • ${if (profile.enableAudio) (if (profile.audioDup) "🔊 Dual Audio" else "Audio (${profile.audioCodec.uppercase()})") else "No Audio"}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ONE-CLICK SCRCPY COMMAND CARD
        CyberCard(glowEffect = true) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Perintah Scrcpy 1-Klik",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = { copyToClipboard(context, scrcpyCmd, "Perintah Scrcpy disalin!") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy command",
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF070A12), RoundedCornerShape(8.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                        .clickable { copyToClipboard(context, scrcpyCmd, "Perintah Scrcpy disalin!") }
                        .padding(10.dp)
                ) {
                    Text(
                        text = scrcpyCmd,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = CyanPrimary,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Jalankan perintah di atas pada Terminal / CMD PC Anda setelah kabel USB terhubung.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DIRECT PC WEB BROWSER STREAMER CARD
        CyberCard {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Laptop,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Direct PC Web Browser Mirror",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Buka layar dan audio langsung di Browser PC (Chrome / Edge / Firefox) tanpa install software:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF070A12), RoundedCornerShape(8.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            copyToClipboard(context, "adb forward tcp:8080 tcp:8080", "ADB Forward disalin!")
                        }
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "1. adb forward tcp:8080 tcp:8080",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = ElectricBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2. Buka di PC: http://localhost:8080",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = EmeraldSuccess
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
