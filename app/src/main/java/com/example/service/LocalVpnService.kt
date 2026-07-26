package com.example.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class LocalVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var serviceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val ACTION_START = "com.example.service.START"
        const val ACTION_STOP = "com.example.service.STOP"
        const val EXTRA_HOLD_MS = "extra_hold_ms"
        const val EXTRA_HOLD_BYTES = "extra_hold_bytes"

        @Volatile
        var isRunning = false
            private set

        @Volatile
        var isDataHoldActive = false
            private set

        @Volatile
        var activeHoldDurationMs: Long = 800L
            private set

        @Volatile
        var remainingTimerSeconds: Int = 0
            private set

        fun setHoldState(active: Boolean, durationMs: Long) {
            isDataHoldActive = active
            activeHoldDurationMs = durationMs
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val holdMs = intent.getLongExtra(EXTRA_HOLD_MS, 800L)
                activeHoldDurationMs = holdMs
                startVpn()
            }
            ACTION_STOP -> {
                stopVpn()
            }
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (isRunning) return
        try {
            val builder = Builder()
                .addAddress("10.0.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .setSession("XRANS FL Local VPN")

            vpnInterface = builder.establish()
            isRunning = true

            serviceJob = scope.launch {
                runVpnLoop()
            }
            Log.d("LocalVpnService", "VPN Started successfully")
        } catch (e: Exception) {
            Log.e("LocalVpnService", "Error starting VPN", e)
            stopVpn()
        }
    }

    private suspend fun runVpnLoop() {
        val pfd = vpnInterface ?: return
        val inputStream = FileInputStream(pfd.fileDescriptor)
        val outputStream = FileOutputStream(pfd.fileDescriptor)
        val buffer = ByteBuffer.allocate(32768)

        while (isRunning && scope.isActive) {
            try {
                if (isDataHoldActive) {
                    // Simulating bandwidth throttling / subtle data hold (80-100 bytes throttle window)
                    val delayTime = activeHoldDurationMs.coerceIn(50L, 2000L)
                    remainingTimerSeconds = ((delayTime) / 1000L).toInt().coerceAtLeast(1)
                    
                    delay(delayTime)
                    
                    // Controlled byte drain
                    val readBytes = inputStream.read(buffer.array())
                    if (readBytes > 0) {
                        // Forward limited size to keep connection alive without dropping completely
                        val clampedBytes = readBytes.coerceAtMost(100)
                        outputStream.write(buffer.array(), 0, clampedBytes)
                    }
                } else {
                    remainingTimerSeconds = 0
                    // Normal network relay loop
                    val readBytes = inputStream.read(buffer.array())
                    if (readBytes > 0) {
                        outputStream.write(buffer.array(), 0, readBytes)
                    } else {
                        delay(10)
                    }
                }
            } catch (e: Exception) {
                if (!isRunning) break
                delay(100)
            }
        }
    }

    private fun stopVpn() {
        isRunning = false
        isDataHoldActive = false
        remainingTimerSeconds = 0
        serviceJob?.cancel()
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e("LocalVpnService", "Error closing interface", e)
        }
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        scope.cancel()
        super.onDestroy()
    }
}
