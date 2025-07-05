package com.serpider.v2rayexplore.data.model

import com.serpider.v2rayexplore.utils.ConfigType

data class V2RayConfig(
    val configText: String,
    val configType: ConfigType,
    val serverAddress: String = "",
    val serverPort: Int = 0
)