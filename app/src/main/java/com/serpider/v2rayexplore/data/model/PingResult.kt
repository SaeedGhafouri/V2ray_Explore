package com.serpider.v2rayexplore.data.model

data class PingResult(
    val isSuccess: Boolean,
    val latency: Long = 0,
    val error: String? = null
)