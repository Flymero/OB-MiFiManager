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
        "1" -> "自动"
        "2" -> "仅 4G"
        "3" -> "4G/3G 自动"
        "4" -> "3G/2G 自动"
        "5" -> "仅 3G"
        "6" -> "仅 2G"
        "7" -> "关闭移动网络"
        "8" -> "4G/2G 自动"
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
    @SerializedName("qci") val qci: String = "0",
    @SerializedName("authtype2g3") val authType2g3: String = "",
    @SerializedName("usr2g3") val usr2g3: String = "",
    @SerializedName("paswd2g3") val paswd2g3: String = "",
    @SerializedName("authtype4g") val authType4g: String = "",
    @SerializedName("usr4g") val usr4g: String = "",
    @SerializedName("paswd4g") val paswd4g: String = ""
) {
    fun isEnabled(): Boolean = enable == "1"
    fun isDefault(): Boolean = default == "1"
    fun displayName(): String = profileName.ifBlank { apn.ifBlank { "--" } }
    fun authType(): String = authType4g.ifBlank { authType2g3.ifBlank { "NONE" } }
    fun username(): String = usr4g.ifBlank { usr2g3 }
    fun password(): String = paswd4g.ifBlank { paswd2g3 }
}

data class DhcpInfo(
    @SerializedName("status") val status: String = "",
    @SerializedName("start") val start: String = "",
    @SerializedName("end") val end: String = "",
    @SerializedName("lease_time") val leaseTime: String = "",
    @SerializedName("ip") val ip: String = "",
    @SerializedName("mask") val mask: String = "",
    @SerializedName("dhcpv6server") val dhcpv6Server: String = "0",
    @SerializedName("dns_enable") val dnsEnable: String = "0",
    @SerializedName("dns1") val dns1: String = "",
    @SerializedName("dns2") val dns2: String = "",
    @SerializedName("mac") val mac: String = "",
    @SerializedName("redirect_enable") val redirectEnable: String = "0",
    @SerializedName("redirect_url") val redirectUrl: String = "",
    @SerializedName("Fixed_IP_list") val fixedIpList: List<FixedIpEntry> = emptyList()
)

data class FixedIpEntry(
    @SerializedName("mac") val mac: String = "",
    @SerializedName("ip") val ip: String = ""
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
    @SerializedName("multi_account") val multiAccount: String = "",
    @SerializedName("router_username") val routerUsername: String = "",
    @SerializedName("router_password") val routerPassword: String = "",
    @SerializedName("router_user_list") val routerUserList: List<AccountUser> = emptyList()
)

data class AccountUser(
    @SerializedName("username") val username: String = "",
    @SerializedName("password") val password: String = "",
    @SerializedName("authority") val authority: String = ""
)
