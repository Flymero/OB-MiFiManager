package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class HomepageInfo(
    @SerializedName("wlan_enable") val wlanEnable: String = "",
    @SerializedName("network_name") val networkName: String = "",
    @SerializedName("connect_disconnect") val connectDisconnect: String = "",
    @SerializedName("sim_status") val simStatus: String = "",
    @SerializedName("pin_status") val pinStatus: String = "",
    @SerializedName("mac") val mac: String = "",
    @SerializedName("imei") val imei: String = "",
    @SerializedName("imsi") val imsi: String = "",
    @SerializedName("iccid") val iccid: String = "",
    @SerializedName("sw_version") val swVersion: String = "",
    @SerializedName("hw_version") val hwVersion: String = "",
    @SerializedName("device_name") val deviceName: String = "",
    @SerializedName("lan_ip") val lanIp: String = "",
    @SerializedName("wan_ip") val wanIp: String = "",
    @SerializedName("ssid") val ssid: String = "",
    @SerializedName("max_access_number") val maxAccessNumber: String = "",
    @SerializedName("wifi_coverage") val wifiCoverage: String = "",
    @SerializedName("lan_domain") val lanDomain: String = "",
    @SerializedName("serial_number") val serialNumber: String = "",
    @SerializedName("auto_apn") val autoApn: String = "",
    @SerializedName("device_model") val deviceModel: String = "",
    @SerializedName("mainSIM") val mainSim: String = ""
) {
    fun decodedSsid(): String {
        if (ssid.isEmpty()) return ""
        return try {
            ssid.chunked(4).map { it.toInt(16).toChar() }.joinToString("")
        } catch (e: Exception) {
            ssid
        }
    }
}
