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

    fun formattedRx(): String = formatTrafficValue(rx)
    fun formattedTx(): String = formatTrafficValue(tx)
    fun formattedRxMonth(): String = formatTrafficValue(rxMonth)
    fun formattedTxMonth(): String = formatTrafficValue(txMonth)
    fun formattedRxLast3Days(): String = formatTrafficValue(rxLast3Days)
    fun formattedTxLast3Days(): String = formatTrafficValue(txLast3Days)

    fun isTrafficValueSuspicious(value: String): Boolean {
        val numeric = value.toBigIntegerOrNull() ?: return true
        return numeric > BigInteger.valueOf(Long.MAX_VALUE)
    }

    private fun formatTrafficValue(value: String): String {
        val numeric = value.toBigIntegerOrNull() ?: return value.ifBlank { "--" }
        if (numeric > BigInteger.valueOf(Long.MAX_VALUE)) return value
        val bytes = numeric.toLong()
        return when {
            bytes >= 1073741824L -> "%.2f GB".format(bytes / 1073741824.0)
            bytes >= 1048576L -> "%.2f MB".format(bytes / 1048576.0)
            bytes >= 1024L -> "%.2f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
