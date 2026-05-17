package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class CustomFwInfo(
    @SerializedName("custom_rules_list") val customRulesList: List<FirewallRule> = emptyList()
)

data class FirewallRule(
    @SerializedName("index") val index: String = "",
    @SerializedName("rule_name") val ruleName: String = "",
    @SerializedName("enabled") val enabled: String = "",
    @SerializedName("src_ip") val srcIp: String = "",
    @SerializedName("src_port") val srcPort: String = "",
    @SerializedName("dst_ip") val dstIp: String = "",
    @SerializedName("dst_port") val dstPort: String = "",
    @SerializedName("proto") val proto: String = ""
)

data class WlanMacFiltersInfo(
    @SerializedName("enable") val enable: String = "",
    @SerializedName("mode") val mode: String = "",
    @SerializedName("allow_list") val allowList: List<MacFilterEntry> = emptyList(),
    @SerializedName("deny_list") val denyList: List<MacFilterEntry> = emptyList(),
    @SerializedName("currnet_device_mac") val currentDeviceMac: String = ""
) {
    fun isEnabled(): Boolean = enable == "1"
    fun isBlacklistMode(): Boolean = mode == "2"
    fun blacklistEntries(): List<MacFilterEntry> = denyList
    fun normalizedCurrentDeviceMac(): String = currentDeviceMac.trim().replace('-', ':').uppercase()
}

data class MacFilterEntry(
    @SerializedName("mac") val mac: String = ""
)

data class WpsInfo(
    @SerializedName("wps_status") val wpsStatus: String = ""
) {
    fun statusText(): String = when (wpsStatus) {
        "0" -> "WiFi 关闭或不可用"
        "1" -> "配对中"
        "2" -> "配对成功"
        "3" -> "配对失败"
        "4" -> "已中断"
        "5" -> "PIN 校验失败"
        else -> "未知状态"
    }

    fun isMatching(): Boolean = wpsStatus == "1"
}

