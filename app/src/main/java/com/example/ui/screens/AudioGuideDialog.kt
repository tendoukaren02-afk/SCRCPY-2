package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AudioGuideDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Panduan Mirroring Audio",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AudioFeatureCard(
                    title = "1. Scrcpy 2.0+ Native Audio",
                    description = "Mulai Scrcpy v2.0 ke atas, mirroring audio diteruskan secara native melalui protokol ADB tunnel tanpa perlu kabel aux/bluetooth tambahan.",
                    icon = Icons.Default.Usb,
                    accentColor = CyanPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                AudioFeatureCard(
                    title = "2. Sumber Audio (Playback vs Mic)",
                    description = "Playback (Internal Audio): Mengambil suara game, media, dan sistem langsung via Android AudioPlaybackCapture API (Android 10+).\nMic: Merekam via input mikrofon ponsel.",
                    icon = Icons.Default.CheckCircle,
                    accentColor = EmeraldSuccess
                )

                Spacer(modifier = Modifier.height(12.dp))

                AudioFeatureCard(
                    title = "3. Double Audio / Suara HP & PC (--audio-dup)",
                    description = "Secara default saat scrcpy aktif, suara di speaker HP dimatikan dan dipindah ke PC. Tambahkan flag --audio-dup agar suara tetap keluar di speaker HP sekaligus berbunyi di PC bersamaan!",
                    icon = Icons.Default.Audiotrack,
                    accentColor = EmeraldSuccess
                )

                Spacer(modifier = Modifier.height(12.dp))

                AudioFeatureCard(
                    title = "4. Codec & Low Latency Audio",
                    description = "• OPUS (Rekomendasi): Sangat responsif dengan delay <20ms, kualitas tinggi.\n• AAC: Kompatibilitas universal.\n• RAW: PCM tanpa kompresi dengan latency nol mutlak.",
                    icon = Icons.Default.Speed,
                    accentColor = CyanPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceCard, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "💡 Tips Suara Tanpa Delay:",
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Gunakan kabel USB 3.0 / port USB belakang PC.\n2. Pasang flag --audio-buffer=25 untuk buffer super kecil.\n3. Matikan layar HP (-S) agar prosesor fokus pada rendering audio/video.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Mengerti", color = Color(0xFF0B0F19), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AudioFeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceCard, RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )
        }
    }
}
