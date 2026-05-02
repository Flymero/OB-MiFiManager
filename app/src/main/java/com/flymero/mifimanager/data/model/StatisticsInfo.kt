package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class StatisticsInfo(
    @SerializedName("tx_byte") val txByte: String = "0",
    @SerializedName("rx_byte") val rxByte: String = "0",
    @SerializedName("tx_byte_all") val txByteAll: String = "0",
    @SerializedName("rx_byte_all") val rxByteAll: String = "0",
    @SerializedName("cur_login_status") val curLoginStatus: String = "0"
) {
    fun formattedCurrentTraffic(): Pair<String, String> {
        return Pair(formatBytes(rxByte), formatBytes(txByte))
    }

    fun formattedTotalTraffic(): Pair<String, String> {
        return Pair(formatBytes(rxByteAll), formatBytes(txByteAll))
    }

    private fun formatBytes(bytes: String): String {
        val b = bytes.toLongOrNull() ?: 0
        return when {
            b >= 1073741824L -> "%.2f GB".format(b / 1073741824.0)
            b >= 1048576L -> "%.2f MB".format(b / 1048576.0)
            b >= 1024L -> "%.2f KB".format(b / 1024.0)
            else -> "$b B"
        }
    }
}
