package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.ScreenMirrorService
import com.example.ui.MainViewModel
import com.example.ui.screens.CommandGeneratorScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.EditProfileDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val startIntent = Intent(this, ScreenMirrorService::class.java).apply {
                action = ScreenMirrorService.ACTION_START
                putExtra(ScreenMirrorService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenMirrorService.EXTRA_RESULT_DATA, result.data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(startIntent)
            } else {
                startService(startIntent)
            }
            Toast.makeText(this, "⚡ Mirroring Server Aktif di Port 8080", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Izin Screen Capture ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Continue stream launch
        startMediaProjectionRequest()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen(
                    viewModel = viewModel,
                    onStartStream = { handleStartStream() },
                    onStopStream = { handleStopStream() }
                )
            }
        }
    }

    private fun handleStartStream() {
        val selected = viewModel.selectedProfile.value ?: viewModel.defaultProfile.value
        if (selected?.enableAudio == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        } else {
            startMediaProjectionRequest()
        }
    }

    private fun startMediaProjectionRequest() {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
    }

    private fun handleStopStream() {
        val status = viewModel.streamStatus.value
        if (status.durationSeconds > 0) {
            viewModel.recordSessionLog(
                status.durationSeconds,
                status.totalBytesTransferred,
                status.totalFramesSent,
                status.totalAudioPacketsSent
            )
        }
        val stopIntent = Intent(this, ScreenMirrorService::class.java).apply {
            action = ScreenMirrorService.ACTION_STOP
        }
        startService(stopIntent)
        Toast.makeText(this, "Mirroring dihentikan", Toast.LENGTH_SHORT).show()
    }
}

enum class NavigationTab(val title: String, val icon: ImageVector) {
    HOME("Stream", Icons.Default.Cast),
    PROFILES("Presets", Icons.Default.Tune),
    SCRIPTS("PC Scripts", Icons.Default.Terminal),
    HARDWARE("Diagnostik", Icons.Default.Speed)
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    onStartStream: () -> Unit,
    onStopStream: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val profiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val activeProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val defaultProfile by viewModel.defaultProfile.collectAsStateWithLifecycle()
    val hardwareInfo by viewModel.hardwareInfo.collectAsStateWithLifecycle()
    val streamStatus by viewModel.streamStatus.collectAsStateWithLifecycle()
    val selectedPlatform by viewModel.selectedPlatform.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
    val editingProfile by viewModel.editingProfile.collectAsStateWithLifecycle()

    val currentActive = activeProfile ?: defaultProfile ?: profiles.firstOrNull()

    // Edit Profile Modal
    editingProfile?.let { profile ->
        EditProfileDialog(
            profile = profile,
            onSave = { updated -> viewModel.saveProfile(updated) },
            onDismiss = { viewModel.closeEditProfile() }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(1.dp, DarkBorder.copy(alpha = 0.5f)),
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                NavigationTab.values().forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0B0F19),
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    profiles = profiles,
                    activeProfile = currentActive,
                    hardwareInfo = hardwareInfo,
                    streamStatus = streamStatus,
                    onSelectProfile = { viewModel.selectProfile(it) },
                    onStartStream = onStartStream,
                    onStopStream = onStopStream,
                    onOpenDeveloperSettings = {
                        viewModel.openDeveloperSettings(context)
                    }
                )
                1 -> ProfilesScreen(
                    profiles = profiles,
                    selectedProfile = currentActive,
                    onSelectProfile = { viewModel.selectProfile(it) },
                    onSetDefaultProfile = { viewModel.setDefaultProfile(it) },
                    onEditProfile = { viewModel.openEditProfile(it) },
                    onDeleteProfile = { viewModel.deleteProfile(it) }
                )
                2 -> CommandGeneratorScreen(
                    activeProfile = currentActive,
                    selectedPlatform = selectedPlatform,
                    onSelectPlatform = { viewModel.setPlatform(it) }
                )
                3 -> DiagnosticsScreen(
                    hardwareInfo = hardwareInfo,
                    recentLogs = recentLogs,
                    onRefresh = { viewModel.refreshDiagnostics() },
                    onOpenDeveloperOptions = {
                        viewModel.openDeveloperSettings(context)
                    },
                    onOpenTethering = {
                        viewModel.openTetheringSettings(context)
                    },
                    onClearHistory = { viewModel.clearHistory() }
                )
            }
        }
    }
}
