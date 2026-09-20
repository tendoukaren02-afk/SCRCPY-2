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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MirrorProfile
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

@Composable
fun ProfilesScreen(
    profiles: List<MirrorProfile>,
    selectedProfile: MirrorProfile?,
    onSelectProfile: (MirrorProfile) -> Unit,
    onSetDefaultProfile: (MirrorProfile) -> Unit,
    onEditProfile: (MirrorProfile?) -> Unit,
    onDeleteProfile: (MirrorProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEditProfile(null) },
                containerColor = CyanPrimary,
                contentColor = Color(0xFF0B0F19),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Preset")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚙️ PRESET & PROFIL SCRCPY",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = CyanPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Pilih atau sesuaikan konfigurasi performa, video encoder & audio",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(profiles, key = { it.id }) { profile ->
                val isSelected = selectedProfile?.id == profile.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) CyanPrimary else DarkBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectProfile(profile) },
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CyanPrimary else TextPrimary
                                    )
                                    if (profile.isDefault) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(EmeraldSuccess.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .border(1.dp, EmeraldSuccess.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("DEFAULT", fontSize = 9.sp, fontWeight = FontWeight.Black, color = EmeraldSuccess)
                                        }
                                    }
                                }

                                if (profile.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = profile.description,
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onSetDefaultProfile(profile) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (profile.isDefault) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Set Default",
                                        tint = if (profile.isDefault) EmeraldSuccess else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onEditProfile(profile) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (!profile.isPreset) {
                                    IconButton(
                                        onClick = { onDeleteProfile(profile) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Profile",
                                            tint = RoseError,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Specs Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SpecBadge(label = "${profile.maxFps} FPS", color = CyanPrimary)
                            SpecBadge(label = if (profile.maxSize == 0) "Native" else "${profile.maxSize}p", color = ElectricBlue)
                            SpecBadge(label = "${profile.videoBitrateMbps}M ${profile.videoCodec.uppercase()}", color = TextPrimary)
                            SpecBadge(
                                label = if (profile.enableAudio) "Audio ${profile.audioCodec.uppercase()}" else "No Audio",
                                color = if (profile.enableAudio) EmeraldSuccess else TextSecondary
                            )
                        }

                        if (profile.turnScreenOff || profile.lowLatencyBuffer || profile.stayAwake || profile.audioDup) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (profile.audioDup) SpecBadge(label = "Dual Audio (HP+PC)", color = EmeraldSuccess)
                                if (profile.turnScreenOff) SpecBadge(label = "Screen Off (-S)", color = TextSecondary)
                                if (profile.lowLatencyBuffer) SpecBadge(label = "Zero-Buffer", color = CyanPrimary)
                                if (profile.stayAwake) SpecBadge(label = "Stay-Awake", color = TextSecondary)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun SpecBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
