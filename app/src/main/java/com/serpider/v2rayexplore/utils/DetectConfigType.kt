package com.serpider.v2rayexplore.utils

import org.json.JSONException
import org.json.JSONObject

object DetectConfigType {

     fun checkType(config: String): ConfigType {
        if (isValidJson(config)) {
            return ConfigType.JSON
        }
        if (config.startsWith("vmess://")) {
            return ConfigType.VMESS
        }
        if (config.startsWith("vless://")) {
            return ConfigType.VLESS
        }
        if (config.startsWith("trojan://")) {
            return ConfigType.TROJAN
        }
        if (config.startsWith("ss://")) {
            return ConfigType.SHADOWSOCKS
        }
        return ConfigType.UNKNOWN
    }

     fun isValidJson(config: String): Boolean {
        return try {
            JSONObject(config)
            true
        } catch (e: JSONException) {
            false
        }
    }
}

enum class ConfigType {
    JSON,
    VMESS,
    VLESS,
    TROJAN,
    SHADOWSOCKS,
    UNKNOWN
}