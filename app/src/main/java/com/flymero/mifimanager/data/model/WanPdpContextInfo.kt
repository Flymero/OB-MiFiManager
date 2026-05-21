package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class WanPdpContextInfo(
    @SerializedName("network_name") val networkName: String = "",
    @SerializedName("pdp_context_list") val pdpList: List<PdpContext> = emptyList()
)

data class PdpContext(
    @SerializedName("ipv4") val ipv4: String = "",
    @SerializedName("v4dns1") val dns1: String = "",
    @SerializedName("v4dns2") val dns2: String = "",
    @SerializedName("v4gateway") val gateway: String = "",
    @SerializedName("v4netmask") val netmask: String = "",
    @SerializedName("curconntime") val currentConnSeconds: String = "0",
    @SerializedName("totalconntime") val totalConnSeconds: String = "0"
) {
    fun formattedTotalConn(): String = formatDuration(totalConnSeconds.toLongOrNull() ?: 0)
    fun formattedCurrentConn(): String = formatDuration(currentConnSeconds.toLongOrNull() ?: 0)

    private fun formatDuration(seconds: Long): String {
        if (seconds <= 0) return "--"
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return buildList {
            if (days > 0) add("${days}天")
            if (hours > 0 || days > 0) add("${hours}小时")
            if (minutes > 0 || hours > 0 || days > 0) add("${minutes}分钟")
            add("${secs}秒")
        }.joinToString("")
    }
}
