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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
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
import com.example.data.entity.MirrorProfile
import com.example.ui.components.CyberCard
import com.example.ui.components.copyToClipboard
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AdbCommandGenerator

@Composable
fun CommandGeneratorScreen(
    activeProfile: MirrorProfile?,
    selectedPlatform: AdbCommandGenerator.Platform,
    onSelectPlatform: (AdbCommandGenerator.Platform) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile = activeProfile ?: MirrorProfile(name = "Default Ultra 60fps", maxFps = 60)
    val singleCmd = AdbCommandGenerator.buildScrcpyCommand(profile)
    val fullScript = AdbCommandGenerator.generateFullScript(profile, selectedPlatform)
    val explanations = AdbCommandGenerator.getCommandExplanation(profile)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "💻 SCRIPT & PERINTAH SCRCPY",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = CyanPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Perintah PC siap pakai dengan optimalisasi latensi & audio",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Platform Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdbCommandGenerator.Platform.values().forEach { platform ->
                val isSelected = selectedPlatform == platform
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) CyanPrimary else DarkSurfaceElevated,
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) CyanPrimary else DarkBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectPlatform(platform) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${platform.icon} ${platform.label.split(" ")[0]}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isSelected) Color(0xFF0B0F19) else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Single-line command card
        CyberCard(glowEffect = true) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Perintah Single-Line (${profile.name})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = { copyToClipboard(context, singleCmd, "Perintah Scrcpy disalin!") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
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
                        .padding(10.dp)
                ) {
                    Text(
                        text = singleCmd,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = CyanPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Full Script Card
        CyberCard {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Script Otomatis (${selectedPlatform.label})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Button(
                        onClick = { copyToClipboard(context, fullScript, "Script lengkap disalin!") },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF0B0F19), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin Script", fontSize = 11.sp, color = Color(0xFF0B0F19), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF070A12), RoundedCornerShape(8.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = fullScript,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = EmeraldSuccess,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Flag Breakdown & Explanations
        Text(
            text = "PENJELASAN PARAMETER & LATENSI",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        explanations.forEach { (flag, explanation) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = flag,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = explanation,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
