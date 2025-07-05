package com.serpider.v2rayexplore.data.repository


import com.serpider.v2rayexplore.data.model.*
import com.serpider.v2rayexplore.data.service.V2RayCore
import com.serpider.v2rayexplore.utils.NetworkUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class V2RayRepository {
    private val v2rayCore = V2RayCore()
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: Flow<ConnectionState> = _connectionState.asStateFlow()

    suspend fun connect(config: V2RayConfig): Result<Unit> {
        return try {
            _connectionState.value = ConnectionState.Connecting

            // شبیه‌سازی اتصال (جایگزین با V2Ray Core واقعی)
            val success = v2rayCore.startV2Ray(config)

            if (success) {
                _connectionState.value = ConnectionState.Connected
                Result.success(Unit)
            } else {
                _connectionState.value = ConnectionState.Error("Failed to connect")
                Result.failure(Exception("Connection failed"))
            }
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    suspend fun disconnect(): Result<Unit> {
        return try {
            v2rayCore.stopV2Ray()
            _connectionState.value = ConnectionState.Disconnected
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ping(): PingResult {
        return try {
            val latency = NetworkUtils.ping("8.8.8.8")
            PingResult(isSuccess = true, latency = latency)
        } catch (e: Exception) {
            PingResult(isSuccess = false, error = e.message)
        }
    }
}