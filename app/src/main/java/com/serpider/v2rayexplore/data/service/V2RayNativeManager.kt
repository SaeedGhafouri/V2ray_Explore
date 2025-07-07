package com.serpider.v2rayexplore.data.service

import android.net.VpnService
import android.util.Log
import java.io.File

/**
 * Alternative V2Ray Core Manager using native binary or JNI
 */
object V2RayNativeManager {
    private const val TAG = "V2RayNativeManager"

    @Volatile
    private var isRunning = false
    private var v2rayProcess: Process? = null

    fun startCore(context: VpnService, configJson: String, fd: Int): Boolean {
        return try {
            // Method 1: Using V2Ray native binary
            startWithNativeBinary(context, configJson, fd)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start V2Ray with native binary: ${e.message}", e)

            try {
                // Method 2: Using JNI (if available)
                startWithJNI(context, configJson, fd)
            } catch (jniException: Exception) {
                Log.e(TAG, "Failed to start V2Ray with JNI: ${jniException.message}", jniException)
                false
            }
        }
    }

    private fun startWithNativeBinary(context: VpnService, configJson: String, fd: Int): Boolean {
        val configFile = File(context.filesDir, "config.json")
        configFile.writeText(configJson)

        val v2rayBinary = File(context.filesDir, "v2ray")
        if (!v2rayBinary.exists()) {
            // Copy V2Ray binary from assets
            copyV2RayBinary(context, v2rayBinary)
        }

        // Make binary executable
        v2rayBinary.setExecutable(true)

        // Start V2Ray process
        val processBuilder = ProcessBuilder(
            v2rayBinary.absolutePath,
            "-config", configFile.absolutePath
        )

        // Set environment variables
        processBuilder.environment().apply {
            put("V2RAY_LOCATION_ASSET", context.filesDir.absolutePath)
            put("V2RAY_LOCATION_CONFIG", configFile.absolutePath)
        }

        // Redirect logs
        processBuilder.redirectErrorStream(true)

        v2rayProcess = processBuilder.start()

        // Monitor process
        Thread {
            try {
                val exitCode = v2rayProcess?.waitFor() ?: -1
                Log.d(TAG, "V2Ray process exited with code: $exitCode")
                isRunning = false
            } catch (e: InterruptedException) {
                Log.d(TAG, "V2Ray process monitoring interrupted")
            }
        }.start()

        // Protect socket
        protectSocket(context, fd)

        isRunning = true
        Log.d(TAG, "V2Ray started with native binary")
        return true
    }

    private fun startWithJNI(context: VpnService, configJson: String, fd: Int): Boolean {
        // Load native library
        System.loadLibrary("v2ray")

        // Call native method
        val result = nativeStartV2Ray(configJson, fd)

        if (result == 0) {
            protectSocket(context, fd)
            isRunning = true
            Log.d(TAG, "V2Ray started with JNI")
            return true
        } else {
            Log.e(TAG, "Failed to start V2Ray via JNI, result: $result")
            return false
        }
    }

    fun stopCore() {
        try {
            if (v2rayProcess != null) {
                v2rayProcess?.destroy()
                v2rayProcess = null
                Log.d(TAG, "V2Ray process stopped")
            } else {
                // Stop via JNI
                nativeStopV2Ray()
                Log.d(TAG, "V2Ray stopped via JNI")
            }

            isRunning = false

        } catch (e: Exception) {
            Log.e(TAG, "Error stopping V2Ray: ${e.message}", e)
        }
    }

    fun isCoreRunning(): Boolean {
        return isRunning && (v2rayProcess?.isAlive == true || nativeIsV2RayRunning())
    }

    private fun copyV2RayBinary(context: VpnService, target: File) {
        try {
            val inputStream = context.assets.open("v2ray")
            val outputStream = target.outputStream()

            inputStream.copyTo(outputStream)

            inputStream.close()
            outputStream.close()

            Log.d(TAG, "V2Ray binary copied to ${target.absolutePath}")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy V2Ray binary: ${e.message}", e)
            throw e
        }
    }

    private fun protectSocket(context: VpnService, fd: Int) {
        val success = context.protect(fd)
        if (!success) {
            throw RuntimeException("Failed to protect socket")
        }
        Log.d(TAG, "Socket protected successfully")
    }

    // JNI native methods (implement these in C/C++)
    private external fun nativeStartV2Ray(config: String, fd: Int): Int
    private external fun nativeStopV2Ray(): Int
    private external fun nativeIsV2RayRunning(): Boolean
}