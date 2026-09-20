package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.entity.MirrorProfile
import com.example.util.AdbCommandGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Scrcpy Mirror", appName)
  }

  @Test
  fun `test scrcpy command generator includes performance and audio flags`() {
    val profile = MirrorProfile(
      name = "Gaming Ultra",
      maxFps = 120,
      maxSize = 1080,
      videoBitrateMbps = 16,
      videoCodec = "h264",
      enableAudio = true,
      audioCodec = "opus",
      audioBitrateKbps = 192,
      audioBufferMs = 25,
      turnScreenOff = true,
      stayAwake = true,
      lowLatencyBuffer = true
    )

    val cmd = AdbCommandGenerator.buildScrcpyCommand(profile)
    assertTrue(cmd.contains("--max-fps=120"))
    assertTrue(cmd.contains("-b 16M"))
    assertTrue(cmd.contains("--audio-codec=opus"))
    assertTrue(cmd.contains("--audio-bit-rate=192K"))
    assertTrue(cmd.contains("--video-buffer=0"))
    assertTrue(cmd.contains("-S"))
    assertTrue(cmd.contains("-w"))
  }
}
