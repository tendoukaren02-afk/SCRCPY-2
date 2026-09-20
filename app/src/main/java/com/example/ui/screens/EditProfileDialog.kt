package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.MirrorProfile
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileDialog(
    profile: MirrorProfile,
    onSave: (MirrorProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var description by remember { mutableStateOf(profile.description) }
    var maxFps by remember { mutableIntStateOf(profile.maxFps) }
    var maxSize by remember { mutableIntStateOf(profile.maxSize) }
    var bitrateMbps by remember { mutableFloatStateOf(profile.videoBitrateMbps.toFloat()) }
    var videoCodec by remember { mutableStateOf(profile.videoCodec) }

    var enableAudio by remember { mutableStateOf(profile.enableAudio) }
    var audioSource by remember { mutableStateOf(profile.audioSource) }
    var audioCodec by remember { mutableStateOf(profile.audioCodec) }
    var audioBitrateKbps by remember { mutableIntStateOf(profile.audioBitrateKbps) }
    var audioBufferMs by remember { mutableFloatStateOf(profile.audioBufferMs.toFloat()) }
    var audioDup by remember { mutableStateOf(profile.audioDup) }

    var turnScreenOff by remember { mutableStateOf(profile.turnScreenOff) }
    var stayAwake by remember { mutableStateOf(profile.stayAwake) }
    var showTouches by remember { mutableStateOf(profile.showTouches) }
    var lowLatencyBuffer by remember { mutableStateOf(profile.lowLatencyBuffer) }
    var fullscreen by remember { mutableStateOf(profile.fullscreen) }
    var alwaysOnTop by remember { mutableStateOf(profile.alwaysOnTop) }
    var customArgs by remember { mutableStateOf(profile.customArgs) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .border(1.dp, CyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkBackground),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (profile.id == 0L) "Buat Preset Baru" else "Edit Preset",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Preset", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Frame Rate Selector
                Text("Frame Rate (FPS): $maxFps FPS", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60, 90, 120, 144).forEach { fps ->
                        val selected = maxFps == fps
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selected) CyanPrimary else DarkSurfaceCard,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, if (selected) CyanPrimary else DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { maxFps = fps }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$fps",
                                color = if (selected) Color(0xFF0B0F19) else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resolution Selector
                Text("Resolusi Max: ${if (maxSize == 0) "Native" else "${maxSize}p"}", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(720 to "720p", 1080 to "1080p", 1440 to "2K", 2160 to "4K", 0 to "Native").forEach { (size, label) ->
                        val selected = maxSize == size
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selected) CyanPrimary else DarkSurfaceCard,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, if (selected) CyanPrimary else DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { maxSize = size }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Color(0xFF0B0F19) else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Video Bitrate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bitrate Video: ${bitrateMbps.toInt()} Mbps", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (bitrateMbps >= 30f) {
                        Text("✨ Ultra Sharp", color = EmeraldSuccess, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
                Slider(
                    value = bitrateMbps,
                    onValueChange = { bitrateMbps = it },
                    valueRange = 2f..80f,
                    steps = 38,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanPrimary,
                        activeTrackColor = CyanPrimary,
                        inactiveTrackColor = DarkBorder
                    )
                )
                Text(
                    text = "💡 Tips Resolusi Tajam: Untuk Native/2K/4K tanpa pecah/buram, gunakan Bitrate 30-50 Mbps dan Codec H.265.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Video Codec
                Text("Video Codec Hardware", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("h264" to "H.264 (Cepat)", "h265" to "H.265 (Jernih)", "av1" to "AV1").forEach { (codec, label) ->
                        val selected = videoCodec.equals(codec, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selected) CyanPrimary else DarkSurfaceCard, RoundedCornerShape(8.dp))
                                .border(1.dp, if (selected) CyanPrimary else DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { videoCodec = codec }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Color(0xFF0B0F19) else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // AUDIO SECTION
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceCard, RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Mirroring Audio (Scrcpy 2.0+)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text("Meneruskan suara internal game/media ke PC", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = enableAudio,
                            onCheckedChange = { enableAudio = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0B0F19),
                                checkedTrackColor = CyanPrimary
                            )
                        )
                    }

                    if (enableAudio) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Audio Codec", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("opus" to "OPUS (Low Delay)", "aac" to "AAC", "raw" to "RAW PCM").forEach { (ac, label) ->
                                val selected = audioCodec.equals(ac, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (selected) CyanPrimary else DarkBackground, RoundedCornerShape(6.dp))
                                        .border(1.dp, if (selected) CyanPrimary else DarkBorder, RoundedCornerShape(6.dp))
                                        .clickable { audioCodec = ac }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) Color(0xFF0B0F19) else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Audio Buffer: ${audioBufferMs.toInt()} ms (Kecil = Latency Rendah)", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Slider(
                            value = audioBufferMs,
                            onValueChange = { audioBufferMs = it },
                            valueRange = 10f..100f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = CyanPrimary,
                                activeTrackColor = CyanPrimary,
                                inactiveTrackColor = DarkBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, if (audioDup) CyanPrimary.copy(alpha = 0.6f) else DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { audioDup = !audioDup }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🔊 Double Sound / Suara Dobel (--audio-dup)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                                Text("Suara berbunyi di speaker HP & PC sekaligus", color = if (audioDup) EmeraldSuccess else TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = audioDup,
                                onCheckedChange = { audioDup = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF0B0F19),
                                    checkedTrackColor = CyanPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PERFORMANCE FLAGS
                Text("Opsi Performa & Penghematan:", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { turnScreenOff = !turnScreenOff },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = turnScreenOff,
                        onCheckedChange = { turnScreenOff = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                    )
                    Text("Matikan Layar Ponsel (-S / --turn-screen-off)", color = TextPrimary, fontSize = 13.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { lowLatencyBuffer = !lowLatencyBuffer },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = lowLatencyBuffer,
                        onCheckedChange = { lowLatencyBuffer = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                    )
                    Text("Zero Latency Buffer (--video-buffer=0)", color = TextPrimary, fontSize = 13.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { stayAwake = !stayAwake },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = stayAwake,
                        onCheckedChange = { stayAwake = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                    )
                    Text("Tetap Menyala saat Terhubung (-w / --stay-awake)", color = TextPrimary, fontSize = 13.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTouches = !showTouches },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showTouches,
                        onCheckedChange = { showTouches = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                    )
                    Text("Tampilkan Titik Sentuhan (-t / --show-touches)", color = TextPrimary, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save button
                Button(
                    onClick = {
                        onSave(
                            profile.copy(
                                name = name.ifBlank { "Custom Profile" },
                                description = description,
                                maxFps = maxFps,
                                maxSize = maxSize,
                                videoBitrateMbps = bitrateMbps.toInt(),
                                videoCodec = videoCodec,
                                enableAudio = enableAudio,
                                audioSource = audioSource,
                                audioCodec = audioCodec,
                                audioBitrateKbps = audioBitrateKbps,
                                audioBufferMs = audioBufferMs.toInt(),
                                audioDup = audioDup,
                                turnScreenOff = turnScreenOff,
                                stayAwake = stayAwake,
                                showTouches = showTouches,
                                lowLatencyBuffer = lowLatencyBuffer,
                                fullscreen = fullscreen,
                                alwaysOnTop = alwaysOnTop,
                                customArgs = customArgs
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFF0B0F19))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Konfigurasi Preset", color = Color(0xFF0B0F19), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
