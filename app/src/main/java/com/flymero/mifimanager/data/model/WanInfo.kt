package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class WanInfo(
    @SerializedName("proto") val proto: String = "",
    @SerializedName("NW_mode_default") val nwModeDefault: String = "",
    @SerializedName("NW_mode") val nwMode: String = "",
    @SerializedName("prefer_mode") val preferMode: String = "",
    @SerializedName("prefer_lte_type") val preferLteType: String = "",
    @SerializedName("connect_mode") val connectMode: String = "",
    @SerializedName("Roaming_disable_auto_dial") val roamingDisableAutoDial: String = "",
    @SerializedName("mtu") val mtu: String = ""
) {
    fun networkModeName(): String = when (nwMode) {
        "0" -> "仅 2G"
        "1" -> "仅 3G"
        "2" -> "仅 4G"
        "3" -> "自动"
        else -> "未知"
    }
}

data class ApnProfileInfo(
    @SerializedName("auto_apn") val autoApn: String = "",
    @SerializedName("apn") val apn: String = "",
    @SerializedName("profile_list") val profileList: List<ApnProfile> = emptyList()
)

data class ApnProfile(
    @SerializedName("profile_name") val profileName: String = "",
    @SerializedName("enable") val enable: String = "",
    @SerializedName("default") val default: String = "",
    @SerializedName("apn") val apn: String = "",
    @SerializedName("lte_apn") val lteApn: String = "",
    @SerializedName("iptype") val ipType: String = "",
    @SerializedName("authtype2g3") val authType2g3: String = "",
    @SerializedName("authtype4g") val authType4g: String = ""
)

data class DhcpInfo(
    @SerializedName("status") val status: String = "",
    @SerializedName("start") val start: String = "",
    @SerializedName("end") val end: String = "",
    @SerializedName("lease_time") val leaseTime: String = "",
    @SerializedName("ip") val ip: String = "",
    @SerializedName("mask") val mask: String = "",
    @SerializedName("dns_enable") val dnsEnable: String = "",
    @SerializedName("dns1") val dns1: String = "",
    @SerializedName("dns2") val dns2: String = "",
    @SerializedName("mac") val mac: String = ""
)

data class LoginInfo(
    @SerializedName("login_status") val loginStatus: String = "",
    @SerializedName("username") val username: String = ""
)

data class FirmwareInfo(
    @SerializedName("version_num") val versionNum: String = "",
    @SerializedName("version_date") val versionDate: String = "",
    @SerializedName("hardware_version") val hwVersion: String = "",
    @SerializedName("two_partition") val twoPartition: String = "0"
)

data class ApiResult(
    @SerializedName("result") val result: String = ""
) {
    val isSuccess: Boolean get() = result == "success"
}

data class AccountManagementInfo(
    @SerializedName("username") val username: String = "",
    @SerializedName("password") val password: String = ""
)
