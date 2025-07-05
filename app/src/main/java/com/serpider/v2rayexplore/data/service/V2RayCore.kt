package com.serpider.v2rayexplore.data.service


import com.serpider.v2rayexplore.data.model.V2RayConfig
import kotlinx.coroutines.delay

class V2RayCore {
    private var isRunning = false

    suspend fun startV2Ray(config: V2RayConfig): Boolean {
        delay(2000)
        isRunning = true
        return true
    }

    fun stopV2Ray() {
        isRunning = false
    }

    fun isConnected(): Boolean = isRunning
}