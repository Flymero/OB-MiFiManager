package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigInteger

data class DeviceManagementInfo(
    @SerializedName("known_devices_list") val knownDevicesList: List<ConnectedDevice> = emptyList(),
    @SerializedName("client_list") val clientList: List<ClientDevice> = emptyList()
)

data class ConnectedDevice(
    @SerializedName("index") val index: String = "",
    @SerializedName("mac") val mac: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("conn_type") val connType: String = "",
    @SerializedName("ip_address") val ipAddress: String = "",
    @SerializedName("conn_time") val connTime: String = ""
) {
    fun decodedName(): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return mac
        return try {
            trimmed.split("  ").filter { it.isNotEmpty() }
                .map { it.trim().toInt(16).toChar() }.joinToString("")
        } catch (e: Exception) {
            trimmed
        }
    }
}

data class ClientDevice(
    @SerializedName("i") val index: String = "",
    @SerializedName("m") val mac: String = "",
    @SerializedName("n") val name: String = "",
    @SerializedName("s") val status: String = "",
    @SerializedName("ct") val connType: String = "",
    @SerializedName("ip") val ip: String = "",
    @SerializedName("nt") val networkType: String = "",
    @SerializedName("rssi") val rssi: String = "0",
    @SerializedName("ta") val timeAdded: String = "",
    @SerializedName("tf") val totalConnectionSeconds: String = "0",
    @SerializedName("rx") val rx: String = "0",
    @SerializedName("tx") val tx: String = "0",
    @SerializedName("rxm") val rxMonth: String = "0",
    @SerializedName("txm") val txMonth: String = "0",
    @SerializedName("rx3") val rxLast3Days: String = "0",
    @SerializedName("tx3") val txLast3Days: String = "0"
) {
    fun isOnline(): Boolean = status == "1"

    fun decodedName(): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return mac
        return try {
            trimmed.split("  ").filter { it.isNotEmpty() }
                .map { it.trim().toInt(16).toChar() }.joinToString("")
        } catch (e: Exception) {
            trimmed
        }
    }

    fun statusText(): String = when (status) {
        "1" -> "在线"
        "2" -> "已屏蔽"
        else -> "离线"
    }

    fun formattedConnectionDuration(): String {
        val seconds = totalConnectionSeconds.toLongOrNull() ?: return totalConnectionSeconds.ifBlank { "--" }
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val remainSeconds = seconds % 60
        return buildList {
            if (days > 0) add("${days}天")
            if (hours > 0 || days > 0) add("${hours}小时")
            if (minutes > 0 || hours > 0 || days > 0) add("${minutes}分钟")
            add("${remainSeconds}秒")
        }.joinToString("")
    }

    fun hasTrafficActivity(): Boolean = trafficFields().any { value ->
        value.toBigIntegerOrNull()?.let { it > BigInteger.ZERO } == true
    }

    fun trafficActivityText(): String = if (hasTrafficActivity()) "有记录" else "暂无记录"

    fun formattedTimeAdded(): String {
        val raw = timeAdded.trim()
        if (raw.isBlank()) return "--"
        return try {
            val (datePart, timePart) = raw.split(" ", limit = 2)
            val (m, d, y) = datePart.split("/").map { it.toInt() }
            val (h, min) = timePart.split(":").take(2).map { it.toInt() }
            "%04d-%02d-%02d %02d:%02d".format(y, m, d, h, min)
        } catch (e: Exception) {
            raw
        }
    }

    fun shortConnectionDuration(): String {
        val seconds = totalConnectionSeconds.toLongOrNull() ?: return "--"
        if (seconds <= 0) return "--"
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        return when {
            days > 0 -> "${days}天${hours}小时"
            hours > 0 -> "${hours}小时${minutes}分"
            else -> "${minutes}分"
        }
    }

    private fun trafficFields(): List<String> = listOf(rx, tx, rxMonth, txMonth, rxLast3Days, txLast3Days)
}
