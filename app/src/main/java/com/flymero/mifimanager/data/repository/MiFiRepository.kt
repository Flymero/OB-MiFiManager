package com.flymero.mifimanager.data.repository

import com.flymero.mifimanager.data.api.DigestAuthInterceptor
import com.flymero.mifimanager.data.api.MiFiApi
import com.flymero.mifimanager.data.local.DataStoreHelper
import com.flymero.mifimanager.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MiFiRepository @Inject constructor(
    private val api: MiFiApi,
    private val digestAuthInterceptor: DigestAuthInterceptor,
    private val okHttpClient: OkHttpClient,
    private val dataStore: DataStoreHelper
) {
    private val gson = Gson()

    suspend fun login(username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val challengeRequest = Request.Builder()
                .url("http://192.168.1.1/login.cgi")
                .build()
            val challengeResponse = okHttpClient.newBuilder()
                .followRedirects(false)
                .build()
                .newCall(challengeRequest).execute()

            val authHeader = challengeResponse.header("WWW-Authenticate")
            challengeResponse.close()
            if (authHeader == null || !authHeader.startsWith("Digest")) return@withContext false

            val params = parseDigestHeader(authHeader)
            val realm = params["realm"] ?: return@withContext false
            val nonce = params["nonce"] ?: return@withContext false
            val qop = params["qop"] ?: "auth"
            val nc = "00000001"
            val cnonce = generateCnonce()

            val ha1 = md5("$username:$realm:$password")
            val ha2 = md5("GET:/cgi/protected.cgi")
            val response = md5("$ha1:$nonce:$nc:$cnonce:$qop:$ha2")

            val loginUrl = "http://192.168.1.1/login.cgi?Action=Digest" +
                "&username=$username&realm=$realm&nonce=$nonce" +
                "&response=$response&qop=$qop&cnonce=$cnonce&temp=marvell"

            val authValue = "Digest username=\"$username\", realm=\"$realm\", " +
                "nonce=\"$nonce\", uri=\"/cgi/protected.cgi\", response=\"$response\", " +
                "qop=$qop, nc=$nc, cnonce=\"$cnonce\""

            val loginRequest = Request.Builder()
                .url(loginUrl)
                .header("Authorization", authValue)
                .build()
            val loginResponse = okHttpClient.newBuilder()
                .followRedirects(false)
                .build()
                .newCall(loginRequest).execute()
            loginResponse.close()

            val success = loginResponse.code == 200
            if (success) {
                digestAuthInterceptor.updateCredentials(username, password)
                digestAuthInterceptor.updateNonce(realm, nonce, qop)
            }
            success
        } catch (e: Exception) {
            false
        }
    }

    private fun parseDigestHeader(header: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        val regex = Regex("""(\w+)="?([^",]+)"?""")
        regex.findAll(header.removePrefix("Digest").trim()).forEach {
            params[it.groupValues[1]] = it.groupValues[2]
        }
        return params
    }

    private fun generateCnonce(): String {
        val bytes = ByteArray(16)
        java.security.SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }

    private fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    suspend fun getHomepageInfo(): Result<HomepageInfo> = runCatching { api.getHomepageInfo() }
    suspend fun getStatusInfo(): Result<StatusInfo> = runCatching { api.getStatusInfo() }
    suspend fun getEngineeringInfo(): Result<EngineeringInfo> = runCatching { api.getEngineeringInfo() }
    suspend fun getWlanInfo(): Result<WlanInfo> = runCatching { api.getWlanInfo() }
    suspend fun getWlanSecurityInfo(): Result<WlanSecurityInfo> = runCatching { api.getWlanSecurityInfo() }
    suspend fun getWanInfo(): Result<WanInfo> = runCatching { api.getWanInfo() }
    suspend fun getApnProfileInfo(): Result<ApnProfileInfo> = runCatching { api.getApnProfileInfo() }
    suspend fun getDeviceManagementInfo(): Result<DeviceManagementInfo> = runCatching { api.getDeviceManagementInfo() }
    suspend fun getDhcpInfo(): Result<DhcpInfo> = runCatching { api.getDhcpInfo() }
    suspend fun getLoginInfo(): Result<LoginInfo> = runCatching { api.getLoginInfo() }
    suspend fun getFirmwareInfo(): Result<FirmwareInfo> = runCatching { api.getFirmwareInfo() }
    suspend fun getCustomFwInfo(): Result<CustomFwInfo> = runCatching { api.getCustomFwInfo() }
    suspend fun getWlanMacFiltersInfo(): Result<WlanMacFiltersInfo> = runCatching { api.getWlanMacFiltersInfo() }
    suspend fun getAccountManagementInfo(): Result<AccountManagementInfo> = runCatching { api.getAccountManagementInfo() }
    suspend fun getStatisticsInfo(): Result<StatisticsInfo> = runCatching { api.getStatisticsInfo() }
    suspend fun getSimInfo(): Result<SimInfo> = runCatching { api.getSimInfo() }
    suspend fun getSmsAuthTerminalList(): Result<SmsAuthTerminalList> = runCatching { api.getSmsAuthTerminalList() }
    suspend fun logout(): Result<Unit> = runCatching { api.logout() }

    private fun jsonBody(data: Any): okhttp3.RequestBody {
        return gson.toJson(data).toRequestBody("application/json".toMediaType())
    }

    suspend fun setWlanSecurity(data: Map<String, Any>): Result<ApiResult> =
        runCatching { api.setWlanSecurity(jsonBody(data)) }

    suspend fun setWlan(data: Map<String, Any>): Result<ApiResult> =
        runCatching { api.setWlan(jsonBody(data)) }

    suspend fun setWan(data: Map<String, Any>): Result<ApiResult> =
        runCatching { api.setWan(jsonBody(data)) }

    suspend fun setDeviceManagement(data: Map<String, Any>): Result<ApiResult> =
        runCatching { api.setDeviceManagement(jsonBody(data)) }

    suspend fun setAccountManagement(data: Map<String, Any>): Result<ApiResult> =
        runCatching { api.setAccountManagement(jsonBody(data)) }

    suspend fun setLan(data: Map<String, Any>): Result<ApiResult> =
        runCatching { api.setLan(jsonBody(data)) }

    suspend fun setWlanMacFilters(data: Map<String, Any>): Result<ApiResult> =
        runCatching { api.setWlanMacFilters(jsonBody(data)) }

    suspend fun setStatistics(data: Map<String, Any>): Result<ApiResult> =
        runCatching { api.setStatistics(jsonBody(data)) }

    suspend fun setCustomFw(data: Map<String, Any>): Result<ApiResult> =
        runCatching { api.setCustomFw(jsonBody(data)) }

    suspend fun restartDevice(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            api.restartDevice()
            Result.success(true)
        } catch (e: java.net.SocketException) {
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFactory(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            api.restoreFactory()
            Result.success(true)
        } catch (e: java.net.SocketException) {
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun blockDevice(mac: String): Result<ApiResult> =
        runCatching { api.setDeviceManagement(jsonBody(mapOf("action" to "2", "mac" to mac))) }

    suspend fun unblockDevice(mac: String): Result<ApiResult> =
        runCatching { api.setDeviceManagement(jsonBody(mapOf("action" to "3", "mac" to mac))) }

    suspend fun toggleCellular(connect: Boolean): Result<ApiResult> =
        runCatching {
            api.setWan(jsonBody(mapOf(
                "connect_disconnect" to if (connect) "cellular" else "disabled"
            )))
        }

    suspend fun sendSmsAuth(phoneNum: String, mac: String): Result<SmsAuthResult> =
        runCatching { api.smsAuthSendSms(jsonBody(mapOf("phoneNum" to phoneNum, "mac" to mac))) }

    suspend fun verifySmsAuth(phoneNum: String, mac: String, code: String): Result<SmsAuthResult> =
        runCatching {
            api.smsAuthVerify(jsonBody(mapOf(
                "phoneNum" to phoneNum, "mac" to mac, "verifyCode" to code
            )))
        }

    suspend fun setSimConfig(switchMode: String, soleSimId: String? = null): Result<ApiResult> =
        runCatching {
            val data = mutableMapOf<String, Any>("switch_mode" to switchMode)
            if (soleSimId != null) data["sole_sim_id"] = soleSimId
            api.setSimConfig(jsonBody(data))
        }

    suspend fun clearTrafficStats(): Result<ApiResult> =
        runCatching { api.setStatistics(jsonBody(mapOf("clear" to "1"))) }

    suspend fun changePassword(username: String, newPassword: String): Result<ApiResult> =
        runCatching {
            api.setAccountManagement(jsonBody(mapOf(
                "account_action" to "1",
                "router_user_list" to listOf(mapOf(
                    "username" to encodeValue(username),
                    "password" to encodeValue(newPassword)
                ))
            )))
        }

    suspend fun saveWifiSecurity(
        currentSecurityInfo: WlanSecurityInfo,
        ssid: String,
        password: String,
        mode: String,
        ssidBroadcast: Boolean
    ): Result<ApiResult> = runCatching {
        val data = linkedMapOf<String, Any>(
            "ssid" to currentSecurityInfo.encodeSsid(ssid),
            "ssid_bcast" to if (ssidBroadcast) "1" else "0",
            "mode" to mode
        )
        if (mode != "None") {
            val modeKey = if (mode == "WEP") "$mode.key1" else "$mode.key"
            data[modeKey] = password
            currentSecurityModeValue(currentSecurityInfo, mode)?.let {
                data["$mode.mode"] = it
            }
            if (mode == "WAPI-PSK") {
                currentSecurityKeyType(currentSecurityInfo)?.let {
                    data["$mode.key_type"] = it
                }
            }
        }
        api.setWlanSecurity(jsonBody(data))
    }

    suspend fun saveWifiSettings(
        currentWlanInfo: WlanInfo,
        wlanEnable: Boolean,
        apIsolate: Boolean
    ): Result<ApiResult> = runCatching {
        val data = linkedMapOf<String, Any>(
            "wlan_enable" to if (wlanEnable) "1" else "0",
            "rf_band" to currentWlanInfo.rfBand,
            "net_mode" to currentWlanInfo.netMode,
            "max_clients" to currentWlanInfo.maxClients,
            "ap_isolate" to if (apIsolate) "1" else "0",
            "bandwidth_acs" to currentWlanInfo.bandwidthAcsOrDefault()
        )
        if (currentWlanInfo.bandwidthAcsOrDefault() == "1") {
            api.setWlan(jsonBody(data))
        } else {
            data["channel"] = currentWlanInfo.channel
            data["bandwidth"] = currentWlanInfo.bandwidth
            api.setWlan(jsonBody(data))
        }
    }

    suspend fun setNetworkMode(currentWanInfo: WanInfo, mode: String): Result<ApiResult> = runCatching {
        val data = linkedMapOf<String, Any>(
            "NW_mode" to mode,
            "NW_mode_action" to "1"
        )
        when (mode) {
            "1" -> {
                data["prefer_mode"] = "1"
                data["prefer_mode_action"] = "1"
            }
            "3" -> {
                data["prefer_mode"] = "3"
                data["prefer_mode_action"] = "1"
            }
            "4" -> {
                data["prefer_mode"] = "5"
                data["prefer_mode_action"] = "1"
            }
            "8" -> {
                data["prefer_mode"] = "8"
                data["prefer_mode_action"] = "1"
            }
        }
        if (currentWanInfo.mtu.isNotBlank()) {
            data["mtu"] = currentWanInfo.mtu
            data["mtu_action"] = "1"
        }
        api.setWan(jsonBody(data))
    }

    private fun encodeValue(value: String): String = java.net.URLEncoder.encode(value, Charsets.UTF_8.name())

    private fun currentSecurityModeValue(info: WlanSecurityInfo, mode: String): String? = when (mode) {
        "WPA2-PSK" -> info.wpa2Psk?.mode
        "WPA-PSK" -> info.wpaPsk?.mode
        "Mixed" -> info.mixed?.mode
        "WPA3-SAE" -> info.wpa3Sae?.mode
        "WPA2-WPA3" -> info.wpa2Wpa3?.mode
        else -> null
    }

    private fun currentSecurityKeyType(info: WlanSecurityInfo): String? = info.wapiPsk?.keyType

    private var cachedPlanUrl: String? = null

    private fun getPlanUrl(): String {
        cachedPlanUrl?.let { return it }
        val prefs = dataStore.context.getSharedPreferences("mifi_prefs", android.content.Context.MODE_PRIVATE)
        val saved = prefs.getString("plan_api_url", null)
        if (saved != null) {
            cachedPlanUrl = saved
            return saved
        }
        return "http://bcdc.ruijiadashop.cn/api/card/loginCard"
    }

    private fun savePlanUrl(url: String) {
        cachedPlanUrl = url
        dataStore.context.getSharedPreferences("mifi_prefs", android.content.Context.MODE_PRIVATE)
            .edit().putString("plan_api_url", url).apply()
    }

    private fun makePlanRequest(url: String, body: okhttp3.RequestBody): okhttp3.Response {
        val request = Request.Builder().url(url).post(body).build()
        return okHttpClient.newBuilder()
            .followRedirects(false)
            .build()
            .newCall(request).execute()
    }

    suspend fun getPlanInfo(): Result<PlanLoginResponse> = withContext(Dispatchers.IO) {
        try {
            val rechargeNo = dataStore.getRechargeNo()
            if (rechargeNo.isEmpty()) return@withContext Result.failure(Exception("未设置充值号"))
            val body = gson.toJson(mapOf("dev_no" to rechargeNo, "type" to 2))
                .toRequestBody("application/json".toMediaType())

            var url = getPlanUrl()
            var response = makePlanRequest(url, body)

            if (response.code in 301..302) {
                val newUrl = response.header("Location")
                response.close()
                if (newUrl != null) {
                    savePlanUrl(newUrl)
                    url = newUrl
                    response = makePlanRequest(url, body)
                }
            }

            val json = response.body?.string() ?: ""
            response.close()
            val result = gson.fromJson(json, PlanLoginResponse::class.java)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
