package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.ConnectionLog
import com.example.data.entity.MirrorProfile
import com.example.data.repository.MirrorRepository
import com.example.service.ScreenMirrorService
import com.example.service.StreamStatus
import com.example.util.AdbCommandGenerator
import com.example.util.DeviceHardwareInfo
import com.example.util.SystemDiagnostics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MirrorRepository
    val allProfiles: StateFlow<List<MirrorProfile>>
    val defaultProfile: StateFlow<MirrorProfile?>
    val recentLogs: StateFlow<List<ConnectionLog>>

    private val _selectedProfile = MutableStateFlow<MirrorProfile?>(null)
    val selectedProfile = _selectedProfile.asStateFlow()

    private val _hardwareInfo = MutableStateFlow<DeviceHardwareInfo?>(null)
    val hardwareInfo = _hardwareInfo.asStateFlow()

    private val _selectedPlatform = MutableStateFlow(AdbCommandGenerator.Platform.WINDOWS_CMD)
    val selectedPlatform = _selectedPlatform.asStateFlow()

    private val _editingProfile = MutableStateFlow<MirrorProfile?>(null)
    val editingProfile = _editingProfile.asStateFlow()

    val streamStatus: StateFlow<StreamStatus> = ScreenMirrorService.streamStatus

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MirrorRepository(db.mirrorProfileDao(), db.connectionLogDao())

        allProfiles = repository.allProfiles.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        defaultProfile = repository.defaultProfile.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        recentLogs = repository.recentLogs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.initializeDefaultPresetsIfEmpty()
            refreshDiagnostics()
        }
    }

    fun selectProfile(profile: MirrorProfile) {
        _selectedProfile.value = profile
        ScreenMirrorService.currentProfile = profile
    }

    fun setPlatform(platform: AdbCommandGenerator.Platform) {
        _selectedPlatform.value = platform
    }

    fun openEditProfile(profile: MirrorProfile?) {
        _editingProfile.value = profile ?: MirrorProfile(
            name = "Custom Ultra Profile",
            maxFps = 60,
            maxSize = 1080,
            videoBitrateMbps = 10,
            enableAudio = true,
            audioCodec = "opus",
            audioBitrateKbps = 128
        )
    }

    fun closeEditProfile() {
        _editingProfile.value = null
    }

    fun saveProfile(profile: MirrorProfile) {
        viewModelScope.launch {
            val id = repository.saveProfile(profile)
            if (profile.isDefault) {
                repository.setDefaultProfile(id)
            }
            _selectedProfile.value = profile.copy(id = id)
            ScreenMirrorService.currentProfile = _selectedProfile.value
            closeEditProfile()
        }
    }

    fun deleteProfile(profile: MirrorProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            if (_selectedProfile.value?.id == profile.id) {
                _selectedProfile.value = allProfiles.value.firstOrNull { it.id != profile.id }
            }
        }
    }

    fun setDefaultProfile(profile: MirrorProfile) {
        viewModelScope.launch {
            repository.setDefaultProfile(profile.id)
            _selectedProfile.value = profile.copy(isDefault = true)
        }
    }

    fun refreshDiagnostics() {
        _hardwareInfo.value = SystemDiagnostics.getDeviceHardwareInfo(getApplication())
    }

    fun openDeveloperSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun openTetheringSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun recordSessionLog(durationSec: Long, bytes: Long, frames: Long, audioPackets: Long) {
        if (durationSec <= 0) return
        val current = _selectedProfile.value ?: defaultProfile.value
        val avgFps = if (durationSec > 0) (frames / durationSec).toInt() else 0
        viewModelScope.launch {
            repository.insertLog(
                ConnectionLog(
                    profileName = current?.name ?: "Default Stream",
                    durationSeconds = durationSec,
                    totalBytesTransferred = bytes,
                    averageFps = avgFps,
                    audioPacketsSent = audioPackets
                )
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }
}
