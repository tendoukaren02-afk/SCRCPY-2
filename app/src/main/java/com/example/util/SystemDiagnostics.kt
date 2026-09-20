package com.example.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.view.Display
import java.net.Inet4Address
import java.net.NetworkInterface

data class DeviceHardwareInfo(
    val model: String,
    val manufacturer: String,
    val androidVersion: String,
    val apiLevel: Int,
    val displayResolution: String,
    val refreshRate: Float,
    val isUsbConnected: Boolean,
    val isAdbEnabled: Boolean,
    val ipAddresses: List<String>,
    val supportedVideoEncoders: List<String>,
    val audioCaptureSupported: Boolean
)

object SystemDiagnostics {

    fun getDeviceHardwareInfo(context: Context): DeviceHardwareInfo {
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val androidVersion = Build.VERSION.RELEASE
        val apiLevel = Build.VERSION.SDK_INT

        // Display info
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
        val mode = display?.mode
        val displayResolution = if (mode != null) "${mode.physicalWidth} x ${mode.physicalHeight}" else "Unknown"
        val refreshRate = display?.refreshRate ?: 60f

        // USB connection state
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val isUsbConnected = plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_AC

        // ADB Debugging state
        val isAdbEnabled = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (e: Exception) {
            false
        }

        // IP addresses
        val ipList = getDeviceIps()

        // Hardware encoders
        val encoders = getHardwareEncoders()

        // Audio capture support (API 29+ Android 10+ supports AudioPlaybackCapture)
        val audioCaptureSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        return DeviceHardwareInfo(
            model = "$manufacturer $model",
            manufacturer = manufacturer,
            androidVersion = "Android $androidVersion (API $apiLevel)",
            apiLevel = apiLevel,
            displayResolution = displayResolution,
            refreshRate = refreshRate,
            isUsbConnected = isUsbConnected,
            isAdbEnabled = isAdbEnabled,
            ipAddresses = ipList,
            supportedVideoEncoders = encoders,
            audioCaptureSupported = audioCaptureSupported
        )
    }

    private fun getDeviceIps(): List<String> {
        val ips = mutableListOf<String>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        ips.add("${iface.displayName}: ${addr.hostAddress}")
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        if (ips.isEmpty()) {
            ips.add("USB ADB Loopback: 127.0.0.1 (via adb forward)")
        }
        return ips
    }

    private fun getHardwareEncoders(): List<String> {
        val encoderNames = mutableListOf<String>()
        try {
            val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            for (info in codecList.codecInfos) {
                if (!info.isEncoder) continue
                val types = info.supportedTypes
                for (type in types) {
                    if (type.equals("video/avc", ignoreCase = true) ||
                        type.equals("video/hevc", ignoreCase = true) ||
                        type.equals("video/av01", ignoreCase = true)
                    ) {
                        val hw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info.isHardwareAccelerated) " [HW-Acc]" else ""
                        encoderNames.add("${info.name} ($type)$hw")
                    }
                }
            }
        } catch (e: Exception) {
            encoderNames.add("Default Hardware H.264 Encoder (OMX.google.h264)")
        }
        return encoderNames.distinct()
    }
}
