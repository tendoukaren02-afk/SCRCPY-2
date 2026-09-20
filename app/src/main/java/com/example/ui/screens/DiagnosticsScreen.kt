package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ConnectionLog
import com.example.ui.components.CyberCard
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
import com.example.util.DeviceHardwareInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiagnosticsScreen(
    hardwareInfo: DeviceHardwareInfo?,
    recentLogs: List<ConnectionLog>,
    onRefresh: () -> Unit,
    onOpenDeveloperOptions: () -> Unit,
    onOpenTethering: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🛠️ DIAGNOSTIK & HARDWARE",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = CyanPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Inspeksi encoder chip, layar, USB & riwayat sesi streaming",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CyanPrimary)
                }
            }
        }

        // Hardware Specs Card
        item {
            CyberCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Spesifikasi Perangkat & Tampilan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    hardwareInfo?.let { info ->
                        DiagRow(label = "Model Perangkat", value = info.model)
                        DiagRow(label = "Versi Android", value = info.androidVersion)
                        DiagRow(label = "Resolusi Layar", value = info.displayResolution)
                        DiagRow(label = "Refresh Rate Max", value = "${info.refreshRate.toInt()} Hz (${if (info.refreshRate >= 90) "Mendukung High FPS" else "Standard"})")
                        DiagRow(
                            label = "Audio Playback Capture",
                            value = if (info.audioCaptureSupported) "Didukung (Internal Game Audio)" else "Mic Only",
                            valueColor = if (info.audioCaptureSupported) EmeraldSuccess else TextSecondary
                        )
                        DiagRow(
                            label = "Status USB Debugging",
                            value = if (info.isAdbEnabled) "Aktif" else "Nonaktif (Perlu diaktifkan)",
                            valueColor = if (info.isAdbEnabled) EmeraldSuccess else RoseError
                        )
                    }
                }
            }
        }

        // Quick Shortcut Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenDeveloperOptions,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Developer Options", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onOpenTethering,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("USB & Tethering", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Hardware Video Encoders
        item {
            CyberCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hardware Video Encoders Terdeteksi", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val encoders = hardwareInfo?.supportedVideoEncoders ?: emptyList()
                    if (encoders.isEmpty()) {
                        Text("Mendeteksi encoder...", color = TextSecondary, fontSize = 12.sp)
                    } else {
                        encoders.forEach { encoder ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
                                    .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = encoder,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = CyanPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Connection History Logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Riwayat Sesi Streaming", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                if (recentLogs.isNotEmpty()) {
                    IconButton(onClick = onClearHistory, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        if (recentLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceCard, RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada riwayat sesi streaming tersimpan.", color = TextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            items(recentLogs, key = { it.id }) { log ->
                val dateStr = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                val mbTransferred = log.totalBytesTransferred / (1024 * 1024)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(log.profileName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                            Text(
                                text = "Durasi: ${log.durationSeconds}s • Transfer: ${mbTransferred} MB",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(dateStr, fontSize = 10.sp, color = TextSecondary)
                            Text(log.connectionType, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
