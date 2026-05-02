package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

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
    @SerializedName("rx") val rx: String = "0",
    @SerializedName("tx") val tx: String = "0"
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
}
