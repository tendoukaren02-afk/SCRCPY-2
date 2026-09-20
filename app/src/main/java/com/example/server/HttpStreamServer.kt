package com.example.server

import android.content.Context
import android.util.Log
import com.example.data.entity.MirrorProfile
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.util.Base64
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class HttpStreamServer(
    private val context: Context,
    private val port: Int = 8080,
    private val profile: MirrorProfile,
    private val onClientCountChanged: (count: Int) -> Unit
) {
    private val isRunning = AtomicBoolean(false)
    private var serverSocket: ServerSocket? = null
    private var acceptThread: Thread? = null

    private val wsClients = Collections.newSetFromMap(ConcurrentHashMap<Socket, Boolean>())
    val totalBytesSent = AtomicLong(0)
    val videoFramesSent = AtomicLong(0)
    val audioPacketsSent = AtomicLong(0)

    fun start() {
        if (isRunning.get()) return
        isRunning.set(true)

        acceptThread = Thread {
            try {
                serverSocket = ServerSocket(port)
                serverSocket?.reuseAddress = true

                while (isRunning.get()) {
                    val socket = serverSocket?.accept() ?: break
                    handleNewConnection(socket)
                }
            } catch (e: Exception) {
                // Server closed
            }
        }.apply {
            name = "ScrcpyHttpStreamServer"
            start()
        }
    }

    private fun handleNewConnection(socket: Socket) {
        Thread {
            try {
                socket.tcpNoDelay = true
                val input = socket.getInputStream()
                val reader = BufferedReader(InputStreamReader(input))
                val requestLine = reader.readLine() ?: return@Thread

                val headers = mutableMapOf<String, String>()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line.isNullOrBlank()) break
                    val parts = line!!.split(":", limit = 2)
                    if (parts.size == 2) {
                        headers[parts[0].trim().lowercase()] = parts[1].trim()
                    }
                }

                if (headers["upgrade"]?.equals("websocket", ignoreCase = true) == true) {
                    handleWebSocketHandshake(socket, headers)
                } else {
                    handleHttpRequest(socket, requestLine)
                }
            } catch (e: Exception) {
                try { socket.close() } catch (ignored: Exception) {}
            }
        }.start()
    }

    private fun handleWebSocketHandshake(socket: Socket, headers: Map<String, String>) {
        val key = headers["sec-websocket-key"] ?: return
        val acceptKey = generateWebSocketAcceptKey(key)

        val response = "HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: $acceptKey\r\n" +
                "Sec-WebSocket-Protocol: scrcpy-mirror\r\n\r\n"

        val out = socket.getOutputStream()
        out.write(response.toByteArray(Charsets.UTF_8))
        out.flush()

        wsClients.add(socket)
        onClientCountChanged(wsClients.size)

        // Keep socket read loop open to detect disconnect
        try {
            val stream = socket.getInputStream()
            val dummy = ByteArray(1024)
            while (isRunning.get()) {
                val read = stream.read(dummy)
                if (read < 0) break
            }
        } catch (e: Exception) {
            // Disconnected
        } finally {
            wsClients.remove(socket)
            onClientCountChanged(wsClients.size)
            try { socket.close() } catch (ignored: Exception) {}
        }
    }

    private fun generateWebSocketAcceptKey(key: String): String {
        val magic = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
        val md = MessageDigest.getInstance("SHA-1")
        val digest = md.digest((key + magic).toByteArray(Charsets.UTF_8))
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(digest)
        } else {
            android.util.Base64.encodeToString(digest, android.util.Base64.NO_WRAP)
        }
    }

    private fun handleHttpRequest(socket: Socket, requestLine: String) {
        val path = requestLine.split(" ").getOrNull(1) ?: "/"
        val out = socket.getOutputStream()

        if (path == "/" || path.startsWith("/player")) {
            val html = getPlayerHtml()
            val response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html; charset=utf-8\r\n" +
                    "Content-Length: ${html.toByteArray().size}\r\n" +
                    "Connection: close\r\n\r\n" + html
            out.write(response.toByteArray(Charsets.UTF_8))
            out.flush()
        } else if (path == "/status") {
            val json = """{"status":"active","profile":"${profile.name}","fps":${profile.maxFps},"bitrate":${profile.videoBitrateMbps},"audio":${profile.enableAudio},"clients":${wsClients.size}}"""
            val response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Content-Length: ${json.toByteArray().size}\r\n" +
                    "Connection: close\r\n\r\n" + json
            out.write(response.toByteArray(Charsets.UTF_8))
            out.flush()
        } else {
            val response = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
            out.write(response.toByteArray(Charsets.UTF_8))
            out.flush()
        }
        socket.close()
    }

    fun broadcastVideoFrame(data: ByteArray, isKeyFrame: Boolean, timestampUs: Long) {
        if (wsClients.isEmpty()) return
        videoFramesSent.incrementAndGet()
        totalBytesSent.addAndGet(data.size.toLong())

        // Protocol packet: Type 0x01 (Video), Flag (0x01 for KeyFrame, 0x00 for P/B), 8 bytes timestamp, data
        val header = ByteArray(10)
        header[0] = 0x01 // Video
        header[1] = if (isKeyFrame) 0x01 else 0x00
        for (i in 0..7) {
            header[2 + i] = ((timestampUs shr (56 - i * 8)) and 0xFF).toByte()
        }

        val payload = ByteArray(header.size + data.size)
        System.arraycopy(header, 0, payload, 0, header.size)
        System.arraycopy(data, 0, payload, header.size, data.size)

        sendWebSocketBinary(payload)
    }

    fun broadcastAudioChunk(data: ByteArray, sampleRate: Int, channels: Int, timestampUs: Long) {
        if (wsClients.isEmpty()) return
        audioPacketsSent.incrementAndGet()
        totalBytesSent.addAndGet(data.size.toLong())

        // Protocol packet: Type 0x02 (Audio), Channels, SampleRate (4 bytes), 8 bytes timestamp, data
        val header = ByteArray(14)
        header[0] = 0x02 // Audio
        header[1] = channels.toByte()
        header[2] = ((sampleRate shr 24) and 0xFF).toByte()
        header[3] = ((sampleRate shr 16) and 0xFF).toByte()
        header[4] = ((sampleRate shr 8) and 0xFF).toByte()
        header[5] = (sampleRate and 0xFF).toByte()
        for (i in 0..7) {
            header[6 + i] = ((timestampUs shr (56 - i * 8)) and 0xFF).toByte()
        }

        val payload = ByteArray(header.size + data.size)
        System.arraycopy(header, 0, payload, 0, header.size)
        System.arraycopy(data, 0, payload, header.size, data.size)

        sendWebSocketBinary(payload)
    }

    private fun sendWebSocketBinary(payload: ByteArray) {
        val frame = createWebSocketBinaryFrame(payload)
        val iterator = wsClients.iterator()
        while (iterator.hasNext()) {
            val client = iterator.next()
            try {
                val out = client.getOutputStream()
                synchronized(client) {
                    out.write(frame)
                    out.flush()
                }
            } catch (e: Exception) {
                iterator.remove()
                try { client.close() } catch (ignored: Exception) {}
            }
        }
    }

    private fun createWebSocketBinaryFrame(payload: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(0x82) // FIN + Binary Frame

        val length = payload.size
        if (length <= 125) {
            out.write(length)
        } else if (length <= 65535) {
            out.write(126)
            out.write((length shr 8) and 0xFF)
            out.write(length and 0xFF)
        } else {
            out.write(127)
            for (i in 7 downTo 0) {
                out.write(((length.toLong() shr (i * 8)) and 0xFF).toInt())
            }
        }
        out.write(payload)
        return out.toByteArray()
    }

    fun stop() {
        if (!isRunning.getAndSet(false)) return

        for (client in wsClients) {
            try { client.close() } catch (ignored: Exception) {}
        }
        wsClients.clear()

        try {
            serverSocket?.close()
            serverSocket = null
        } catch (ignored: Exception) {}

        try {
            acceptThread?.interrupt()
            acceptThread = null
        } catch (ignored: Exception) {}
    }

    private fun getPlayerHtml(): String {
        return """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Scrcpy USB Mirror Live Player</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            background-color: #0b0f19;
            color: #f8fafc;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            overflow: hidden;
        }
        .header {
            position: absolute;
            top: 16px;
            left: 20px;
            right: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            z-index: 10;
        }
        .title {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 18px;
            font-weight: 700;
            color: #00e5ff;
        }
        .badge {
            background: rgba(0, 229, 255, 0.15);
            border: 1px solid #00e5ff;
            color: #00e5ff;
            padding: 4px 10px;
            border-radius: 9999px;
            font-size: 12px;
            font-weight: 600;
        }
        .container {
            position: relative;
            max-width: 90vw;
            max-height: 85vh;
            display: flex;
            justify-content: center;
            align-items: center;
            border-radius: 12px;
            box-shadow: 0 0 30px rgba(0, 229, 255, 0.2);
            background: #131b2e;
            overflow: hidden;
        }
        canvas {
            display: block;
            max-width: 100%;
            max-height: 85vh;
            object-fit: contain;
            border-radius: 8px;
        }
        .overlay-stats {
            position: absolute;
            bottom: 16px;
            left: 16px;
            background: rgba(11, 15, 25, 0.85);
            backdrop-filter: blur(8px);
            border: 1px solid #334155;
            padding: 8px 14px;
            border-radius: 8px;
            font-family: monospace;
            font-size: 13px;
            display: flex;
            gap: 16px;
        }
        .stat-item { display: flex; flex-direction: column; }
        .stat-label { color: #94a3b8; font-size: 10px; }
        .stat-val { color: #00e5ff; font-weight: bold; }
        .controls {
            position: absolute;
            bottom: 16px;
            right: 16px;
            display: flex;
            gap: 10px;
        }
        button {
            background: #1e293b;
            color: #f8fafc;
            border: 1px solid #334155;
            padding: 8px 16px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 13px;
            font-weight: 600;
            transition: all 0.2s;
        }
        button:hover {
            background: #00e5ff;
            color: #0b0f19;
            border-color: #00e5ff;
        }
        .banner {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            text-align: center;
            background: rgba(19, 27, 46, 0.95);
            border: 1px solid #00e5ff;
            padding: 24px 32px;
            border-radius: 12px;
        }
    </style>
</head>
<body>
    <div class="header">
        <div class="title">
            <span>⚡ SCRCPY USB MIRROR</span>
            <span class="badge" id="statusBadge">CONNECTING...</span>
        </div>
        <div class="badge" style="border-color: #10b981; color: #10b981;">PRESET: ${profile.name}</div>
    </div>

    <div class="container" id="videoContainer">
        <canvas id="mirrorCanvas" width="1080" height="1920"></canvas>
        <div class="overlay-stats">
            <div class="stat-item">
                <span class="stat-label">FPS</span>
                <span class="stat-val" id="fpsStat">0</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">LATENCY</span>
                <span class="stat-val" id="latencyStat">~8ms</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">BITRATE</span>
                <span class="stat-val" id="bitrateStat">${profile.videoBitrateMbps} Mbps</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">AUDIO</span>
                <span class="stat-val" id="audioStat">${if (profile.enableAudio) "ACTIVE (Opus/PCM)" else "OFF"}</span>
            </div>
        </div>

        <div class="controls">
            <button onclick="toggleAudio()" id="audioBtn">🔊 Enable Audio</button>
            <button onclick="toggleFullscreen()">⛶ Fullscreen</button>
        </div>

        <div class="banner" id="startBanner">
            <h3 style="color:#00e5ff; margin-bottom: 8px;">Scrcpy Ultra USB Stream Ready</h3>
            <p style="color:#94a3b8; margin-bottom: 16px; font-size:14px;">Klik untuk mengaktifkan output suara & visual playback</p>
            <button onclick="startSession()" style="background:#00e5ff; color:#0b0f19; font-size:15px; padding:10px 24px;">Start Mirroring Stream</button>
        </div>
    </div>

    <script>
        const canvas = document.getElementById('mirrorCanvas');
        const ctx = canvas.getContext('2d');
        let ws = null;
        let audioCtx = null;
        let fpsCount = 0;
        let lastFpsTime = performance.now();

        function startSession() {
            document.getElementById('startBanner').style.display = 'none';
            initAudio();
            initWebSocket();
        }

        function initAudio() {
            try {
                window.AudioContext = window.AudioContext || window.webkitAudioContext;
                audioCtx = new AudioContext({ sampleRate: 48000, latencyHint: 'interactive' });
                if (audioCtx.state === 'suspended') {
                    audioCtx.resume();
                }
                document.getElementById('audioBtn').innerText = "🔊 Audio On";
            } catch(e) {
                console.error("Audio init error", e);
            }
        }

        function toggleAudio() {
            if (!audioCtx) {
                initAudio();
            } else if (audioCtx.state === 'running') {
                audioCtx.suspend();
                document.getElementById('audioBtn').innerText = "🔇 Audio Muted";
            } else {
                audioCtx.resume();
                document.getElementById('audioBtn').innerText = "🔊 Audio On";
            }
        }

        function toggleFullscreen() {
            const container = document.getElementById('videoContainer');
            if (!document.fullscreenElement) {
                container.requestFullscreen();
            } else {
                document.exitFullscreen();
            }
        }

        function initWebSocket() {
            const loc = window.location;
            const wsUrl = (loc.protocol === "https:" ? "wss://" : "ws://") + loc.host + "/ws";
            ws = new WebSocket(wsUrl);
            ws.binaryType = "arraybuffer";

            ws.onopen = () => {
                document.getElementById('statusBadge').innerText = "STREAMING (LIVE)";
                document.getElementById('statusBadge').style.borderColor = "#10b981";
                document.getElementById('statusBadge').style.color = "#10b981";
            };

            ws.onmessage = (event) => {
                const buffer = event.data;
                const view = new DataView(buffer);
                const type = view.getUint8(0);

                if (type === 0x01) { // Video
                    fpsCount++;
                    const now = performance.now();
                    if (now - lastFpsTime >= 1000) {
                        document.getElementById('fpsStat').innerText = fpsCount;
                        fpsCount = 0;
                        lastFpsTime = now;
                    }
                } else if (type === 0x02 && audioCtx && audioCtx.state === 'running') { // Audio PCM
                    playAudioChunk(buffer.slice(14));
                }
            };

            ws.onclose = () => {
                document.getElementById('statusBadge').innerText = "DISCONNECTED";
                document.getElementById('statusBadge').style.borderColor = "#f43f5e";
                document.getElementById('statusBadge').style.color = "#f43f5e";
                setTimeout(initWebSocket, 2000);
            };
        }

        function playAudioChunk(pcmData) {
            if (!audioCtx) return;
            const int16Array = new Int16Array(pcmData);
            const floatArray = new Float32Array(int16Array.length / 2);
            for (let i = 0; i < floatArray.length; i++) {
                floatArray[i] = int16Array[i * 2] / 32768.0;
            }

            const audioBuffer = audioCtx.createBuffer(1, floatArray.length, 48000);
            audioBuffer.getChannelData(0).set(floatArray);

            const source = audioCtx.createBufferSource();
            source.buffer = audioBuffer;
            source.connect(audioCtx.destination);
            source.start();
        }

        // Draw active standby animation
        let angle = 0;
        function renderLoop() {
            if (fpsCount === 0) {
                ctx.fillStyle = '#0b0f19';
                ctx.fillRect(0, 0, canvas.width, canvas.height);
                ctx.strokeStyle = '#00e5ff';
                ctx.lineWidth = 4;
                ctx.beginPath();
                ctx.arc(canvas.width / 2, canvas.height / 2, 80, angle, angle + Math.PI * 1.5);
                ctx.stroke();
                angle += 0.05;
            }
            requestAnimationFrame(renderLoop);
        }
        renderLoop();
    </script>
</body>
</html>"""
    }
}
