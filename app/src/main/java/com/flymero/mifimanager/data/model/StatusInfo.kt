package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class StatusInfo(
    @SerializedName("wifi_clients_num") val wifiClientsNum: String = "0",
    @SerializedName("run_seconds") val runSeconds: String = "0",
    @SerializedName("battery_connect") val batteryConnect: String = "0",
    @SerializedName("battery_charging") val batteryCharging: String = "0",
    @SerializedName("battery_percent") val batteryPercent: String = "0",
    @SerializedName("tx_byte_all") val txByteAll: String = "0",
    @SerializedName("rx_byte_all") val rxByteAll: String = "0",
    @SerializedName("signal_quality") val signalQuality: String = "0",
    @SerializedName("rssi") val rssi: String = "0",
    @SerializedName("sys_mode") val sysMode: String = "0",
    @SerializedName("roaming") val roaming: String = "0",
    @SerializedName("new_sms_num") val newSmsNum: String = "0",
    @SerializedName("tx_speed") val txSpeed: String = "0",
    @SerializedName("rx_speed") val rxSpeed: String = "0",
    @SerializedName("tx_max_speed") val txMaxSpeed: String = "0",
    @SerializedName("rx_max_speed") val rxMaxSpeed: String = "0",
    @SerializedName("login_status") val loginStatus: String = "0"
) {
    fun networkType(): String = when (sysMode) {
        "1" -> "GSM"
        "3" -> "GPRS"
        "5" -> "EDGE"
        "7" -> "WCDMA"
        "9" -> "HSDPA"
        "11" -> "HSUPA"
        "13" -> "HSPA"
        "15" -> "HSPA+"
        "17" -> "LTE"
        "19" -> "LTE+"
        "21" -> "NR5G"
        else -> "Unknown"
    }

    fun formattedUptime(): String {
        val totalSeconds = runSeconds.toLongOrNull() ?: 0
        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return buildString {
            if (days > 0) append("${days}天 ")
            if (hours > 0) append("${hours}时 ")
            append("${minutes}分 ${secs}秒")
        }
    }

    fun formattedTraffic(bytes: String): String {
        val b = bytes.toLongOrNull() ?: 0
        return when {
            b >= 1073741824L -> "%.2f GB".format(b / 1073741824.0)
            b >= 1048576L -> "%.2f MB".format(b / 1048576.0)
            b >= 1024L -> "%.2f KB".format(b / 1024.0)
            else -> "$b B"
        }
    }

    fun formattedSpeed(bytesPerSec: String): String {
        val b = bytesPerSec.toLongOrNull() ?: 0
        return when {
            b >= 1048576L -> "%.1f MB/s".format(b / 1048576.0)
            b >= 1024L -> "%.1f KB/s".format(b / 1024.0)
            else -> "$b B/s"
        }
    }
}
