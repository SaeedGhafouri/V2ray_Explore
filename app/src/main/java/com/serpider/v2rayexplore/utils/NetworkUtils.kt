package com.serpider.v2rayexplore.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

object NetworkUtils {
    suspend fun ping(host: String): Long = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val address = InetAddress.getByName(host)
            val reachable = address.isReachable(5000)
            val endTime = System.currentTimeMillis()

            if (reachable) {
                endTime - startTime
            } else {
                throw Exception("Host not reachable")
            }
        } catch (e: Exception) {
            throw Exception("Ping failed: ${e.message}")
        }
    }
}