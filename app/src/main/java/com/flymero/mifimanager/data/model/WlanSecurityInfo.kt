package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class WlanSecurityInfo(
    @SerializedName("net_mode") val netMode: String = "",
    @SerializedName("ssid") val ssid: String = "",
    @SerializedName("ssid_bcast") val ssidBcast: String = "",
    @SerializedName("mode") val mode: String = "",
    @SerializedName("WPA2-PSK") val wpa2Psk: WpaConfig? = null,
    @SerializedName("WPA-PSK") val wpaPsk: WpaConfig? = null,
    @SerializedName("Mixed") val mixed: WpaConfig? = null,
    @SerializedName("WPA3-SAE") val wpa3Sae: WpaConfig? = null,
    @SerializedName("WPA2-WPA3") val wpa2Wpa3: WpaConfig? = null,
    @SerializedName("WEP") val wep: WepConfig? = null
) {
    fun decodedSsid(): String {
        if (ssid.isEmpty()) return ""
        return try {
            ssid.chunked(4).map { it.toInt(16).toChar() }.joinToString("")
        } catch (e: Exception) {
            ssid
        }
    }

    fun currentKey(): String {
        return when (mode) {
            "WPA2-PSK" -> wpa2Psk?.key ?: ""
            "WPA-PSK" -> wpaPsk?.key ?: ""
            "Mixed" -> mixed?.key ?: ""
            "WPA3-SAE" -> wpa3Sae?.key ?: ""
            "WPA2-WPA3" -> wpa2Wpa3?.key ?: ""
            else -> ""
        }
    }

    fun encodeSsid(text: String): String {
        return text.map { "%04x".format(it.code) }.joinToString("")
    }
}

data class WpaConfig(
    @SerializedName("mode") val mode: String = "",
    @SerializedName("key") val key: String = ""
)

data class WepConfig(
    @SerializedName("key1") val key1: String = "",
    @SerializedName("auth") val auth: String = "",
    @SerializedName("encrypt") val encrypt: String = ""
)
