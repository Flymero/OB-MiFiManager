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
    suspend fun getWanPdpContextInfo(): Result<WanPdpContextInfo> = runCatching { api.getWanPdpContextInfo() }
    suspend fun getApnProfileInfo(): Result<ApnProfileInfo> = runCatching { api.getApnProfileInfo() }
    suspend fun getDeviceManagementInfo(): Result<DeviceManagementInfo> = runCatching { api.getDeviceManagementInfo() }
    suspend fun getDhcpInfo(): Result<DhcpInfo> = runCatching { api.getDhcpInfo() }
    suspend fun getLoginInfo(): Result<LoginInfo> = runCatching { api.getLoginInfo() }
    suspend fun getFirmwareInfo(): Result<FirmwareInfo> = runCatching { api.getFirmwareInfo() }
    suspend fun getCustomFwInfo(): Result<CustomFwInfo> = runCatching { api.getCustomFwInfo() }
    suspend fun getWlanMacFiltersInfo(): Result<WlanMacFiltersInfo> = runCatching { api.getWlanMacFiltersInfo() }
    suspend fun getWlanWpsInfo(): Result<WpsInfo> = runCatching { api.getWlanWpsInfo() }
    suspend fun getAccountManagementInfo(): Result<AccountManagementInfo> = runCatching { api.getAccountManagementInfo() }
    suspend fun getStatisticsInfo(): Result<StatisticsInfo> = runCatching { api.getStatisticsInfo() }
    suspend fun getSimInfo(): Result<SimInfo> = runCatching { api.getSimInfo() }
    suspend fun getSmsAuthTerminalList(): Result<SmsAuthTerminalList> = runCatching { api.getSmsAuthTerminalList() }
    suspend fun logout(): Result<Unit> = runCatching {
        try {
            api.logout()
        } finally {
            digestAuthInterceptor.clearCredentials()
        }
    }

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
            if (connect) {
                api.setWan(jsonBody(mapOf(
                    "connect_disconnect" to "cellular",
                    "connect_mode" to "0"
                )))
            } else {
                api.setWan(jsonBody(mapOf(
                    "connect_disconnect" to "disabled"
                )))
            }
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

    suspend fun setMacBlacklistEnabled(currentInfo: WlanMacFiltersInfo, enabled: Boolean): Result<ApiResult> =
        setWlanMacFilters(
            linkedMapOf(
                "enable" to if (enabled) "1" else "0",
                "mode" to "2",
                "deny_list" to currentInfo.blacklistEntries().map { mapOf("mac" to it.mac) }
            )
        )

    suspend fun addMacToBlacklist(currentInfo: WlanMacFiltersInfo, mac: String): Result<ApiResult> {
        val normalizedMac = normalizeMacAddress(mac)
        val entries = currentInfo.blacklistEntries().map { it.mac.uppercase() }.toMutableList()
        if (entries.any { it.equals(normalizedMac, ignoreCase = true) }) {
            return Result.success(ApiResult(result = "success"))
        }
        entries += normalizedMac
        return setWlanMacFilters(
            linkedMapOf(
                "enable" to if (currentInfo.isEnabled()) "1" else "0",
                "mode" to "2",
                "deny_list" to entries.map { mapOf("mac" to it) }
            )
        )
    }

    suspend fun removeMacFromBlacklist(currentInfo: WlanMacFiltersInfo, index: Int): Result<ApiResult> {
        val entry = currentInfo.blacklistEntries().getOrNull(index)
            ?: return Result.failure(IllegalArgumentException("Invalid blacklist index"))
        val deleteIndex = entry.index.ifBlank { index.toString() }
        return setWlanMacFilters(
            linkedMapOf(
                "enable" to if (currentInfo.isEnabled()) "1" else "0",
                "mode" to "2",
                "deny_delete_index" to "$deleteIndex,"
            )
        )
    }

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

    suspend fun startWpsPushButton(): Result<ApiResult> =
        setWlanSecurity(linkedMapOf("connect_method" to "1"))

    suspend fun startWpsPin(pin: String): Result<ApiResult> =
        setWlanSecurity(
            linkedMapOf(
                "connect_method" to "2",
                "wps_pin" to normalizeWpsPin(pin)
            )
        )

    suspend fun cancelWps(): Result<ApiResult> =
        setWlanSecurity(linkedMapOf("connect_method" to "3"))

    suspend fun saveWifiSettings(
        currentWlanInfo: WlanInfo,
        wlanEnable: Boolean,
        apIsolate: Boolean,
        bandwidthAcs: Boolean = true,
        channel: String = currentWlanInfo.channel,
        bandwidth: String = currentWlanInfo.bandwidth,
        maxClients: String = currentWlanInfo.maxClients
    ): Result<ApiResult> = runCatching {
        val data = linkedMapOf<String, Any>(
            "wlan_enable" to if (wlanEnable) "1" else "0",
            "rf_band" to currentWlanInfo.rfBand,
            "net_mode" to currentWlanInfo.netMode,
            "max_clients" to maxClients,
            "ap_isolate" to if (apIsolate) "1" else "0",
            "bandwidth_acs" to if (bandwidthAcs) "1" else "0"
        )
        if (!bandwidthAcs) {
            data["channel"] = channel
            data["bandwidth"] = bandwidth
        }
        api.setWlan(jsonBody(data))
    }

    suspend fun saveWifiAutoOff(sleepMinutes: String): Result<ApiResult> =
        setWlan(
            linkedMapOf(
                "wifi_sleep_time" to sleepMinutes,
                "wifi_sleep_action" to "1"
            )
        )

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

    suspend fun saveApnProfile(
        currentInfo: ApnProfileInfo,
        profileName: String,
        apn: String,
        ipType: String,
        authType: String,
        username: String,
        password: String
    ): Result<ApiResult> = runCatching {
        val existing = currentInfo.profileList.firstOrNull { it.profileName == profileName }
        val profile = apnProfilePayload(
            profileName = profileName,
            apn = apn,
            ipType = ipType,
            authType = authType,
            username = username,
            password = password,
            enabled = existing?.isEnabled() == true,
            isDefault = existing?.isDefault() == true
        )
        api.setWan(jsonBody(linkedMapOf("profile_list" to listOf(profile))))
    }

    suspend fun setDefaultApn(currentInfo: ApnProfileInfo, profileName: String): Result<ApiResult> = runCatching {
        val selected = currentInfo.profileList.firstOrNull { it.profileName == profileName }
            ?: return@runCatching ApiResult()
        val current = currentInfo.profileList.firstOrNull { it.isEnabled() }
        val profiles = mutableListOf<Map<String, Any>>()
        profiles += apnProfilePayload(
            profileName = selected.profileName,
            apn = selected.apn,
            ipType = selected.ipType,
            authType = selected.authType(),
            username = selected.username(),
            password = selected.password(),
            enabled = true,
            isDefault = selected.isDefault()
        )
        if (current != null && current.profileName != selected.profileName) {
            profiles += apnProfilePayload(
                profileName = current.profileName,
                apn = current.apn,
                ipType = current.ipType,
                authType = current.authType(),
                username = current.username(),
                password = current.password(),
                enabled = false,
                isDefault = current.isDefault()
            )
        }
        api.setWan(jsonBody(linkedMapOf("profile_list" to profiles)))
    }

    suspend fun deleteApnProfile(profileName: String): Result<ApiResult> =
        setWan(linkedMapOf("profile_list" to listOf(linkedMapOf("profile_name" to profileName, "delete" to "1"))))

    private fun apnProfilePayload(
        profileName: String,
        apn: String,
        ipType: String,
        authType: String,
        username: String,
        password: String,
        enabled: Boolean,
        isDefault: Boolean
    ): Map<String, Any> = linkedMapOf(
        "profile_name" to profileName,
        "enable" to if (enabled) "1" else "0",
        "default" to if (isDefault) "1" else "0",
        "apn" to apn,
        "lte_apn" to apn,
        "iptype" to ipType,
        "qci" to "0",
        "authtype2g3" to authType,
        "usr2g3" to username,
        "paswd2g3" to password,
        "authtype4g" to authType,
        "usr4g" to username,
        "paswd4g" to password
    )

    private fun encodeValue(value: String): String = java.net.URLEncoder.encode(value, Charsets.UTF_8.name())

    private fun normalizeMacAddress(value: String): String =
        value.trim().replace('-', ':').uppercase()

    private fun normalizeWpsPin(value: String): String =
        value.trim().replace("-", "")

    private fun currentSecurityModeValue(info: WlanSecurityInfo, mode: String): String? = when (mode) {
        "WPA2-PSK" -> info.wpa2Psk?.mode
        "WPA-PSK" -> info.wpaPsk?.mode
        "Mixed" -> info.mixed?.mode
        "WPA3-SAE" -> info.wpa3Sae?.mode
        "WPA2-WPA3" -> info.wpa2Wpa3?.mode
        else -> null
    }

    private fun currentSecurityKeyType(info: WlanSecurityInfo): String? = info.wapiPsk?.keyType

    private data class PlanProvider(
        val key: String,
        val name: String,
        val baseUrl: String
    ) {
        val loginCardUrl: String get() = "$baseUrl/api/card/loginCard"
        val orderListUrl: String get() = "$baseUrl/api/order/orderList"
    }

    private data class PlanProviderDiscovery(
        val provider: PlanProvider? = null,
        val response: PlanLoginResponse
    )

    private val planProviders = listOf(
        PlanProvider("xuanxiao", "喧嚣", "http://gdey.ruijiadashop.cn"),
        PlanProvider("shengtonglong", "盛通龙", "http://pddwifi.gzkpiot.com"),
        PlanProvider("jingle", "晶乐", "http://cxhy.hengheiot.com")
    )

    private var cachedPlanProviderRechargeNo: String? = null
    private var cachedPlanProviderKey: String? = null

    private fun getCachedPlanProvider(rechargeNo: String): PlanProvider? {
        if (cachedPlanProviderRechargeNo == rechargeNo) {
            cachedPlanProviderKey?.let { key -> return planProviders.firstOrNull { it.key == key } }
        }

        val prefs = dataStore.context.getSharedPreferences("mifi_prefs", android.content.Context.MODE_PRIVATE)
        val savedRechargeNo = prefs.getString("plan_provider_recharge_no", null)
        val savedKey = prefs.getString("plan_provider_key", null)
        if (savedRechargeNo == rechargeNo && savedKey != null) {
            cachedPlanProviderRechargeNo = savedRechargeNo
            cachedPlanProviderKey = savedKey
            return planProviders.firstOrNull { it.key == savedKey }
        }

        val legacyUrl = prefs.getString("plan_api_url", null)
        return legacyUrl?.let { url ->
            planProviders.firstOrNull { provider ->
                url.contains(provider.baseUrl.removePrefix("http://").removePrefix("https://")) ||
                    (provider.key == "xuanxiao" && url.contains("bcdc.ruijiadashop.cn"))
            }
        }
    }

    private fun savePlanProvider(rechargeNo: String, provider: PlanProvider) {
        cachedPlanProviderRechargeNo = rechargeNo
        cachedPlanProviderKey = provider.key
        dataStore.context.getSharedPreferences("mifi_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("plan_provider_recharge_no", rechargeNo)
            .putString("plan_provider_key", provider.key)
            .putString("plan_api_url", provider.loginCardUrl)
            .apply()
    }

    private fun candidatePlanProviders(rechargeNo: String): List<PlanProvider> {
        val cachedProvider = getCachedPlanProvider(rechargeNo)
        return if (cachedProvider == null) {
            planProviders
        } else {
            listOf(cachedProvider) + planProviders.filterNot { it.key == cachedProvider.key }
        }
    }

    private fun planLoginBody(rechargeNo: String): okhttp3.RequestBody =
        gson.toJson(mapOf("dev_no" to rechargeNo, "type" to 2))
            .toRequestBody("application/json".toMediaType())

    private fun orderListBody(rechargeNo: String): okhttp3.RequestBody =
        gson.toJson(mapOf("dev_no" to rechargeNo))
            .toRequestBody("application/json".toMediaType())

    private fun makePlanRequest(url: String, body: okhttp3.RequestBody): String {
        var requestUrl = url
        var redirectCount = 0

        while (true) {
            val request = Request.Builder()
                .url(requestUrl)
                .header("Accept", "application/json")
                .post(body)
                .build()
            val response = okHttpClient.newBuilder()
                .followRedirects(false)
                .build()
                .newCall(request)
                .execute()

            response.use {
                if (it.code in 301..308) {
                    val location = it.header("Location")
                        ?: throw java.io.IOException("Redirect without Location")
                    if (redirectCount++ >= 3) throw java.io.IOException("Too many redirects")
                    requestUrl = resolveRedirectUrl(requestUrl, location)
                    return@use
                }
                if (!it.isSuccessful) throw java.io.IOException("HTTP ${it.code}")
                return it.body?.string() ?: ""
            }
        }
    }

    private fun resolveRedirectUrl(baseUrl: String, location: String): String =
        runCatching { java.net.URI(baseUrl).resolve(location).toString() }
            .getOrElse { location }

    private fun requestPlanInfo(provider: PlanProvider, rechargeNo: String): PlanLoginResponse {
        val json = makePlanRequest(provider.loginCardUrl, planLoginBody(rechargeNo))
        return gson.fromJson(json, PlanLoginResponse::class.java)
            ?: PlanLoginResponse(code = 0, msg = "套餐服务返回为空")
    }

    private fun requestOrderList(provider: PlanProvider, rechargeNo: String): OrderListResponse {
        val json = makePlanRequest(provider.orderListUrl, orderListBody(rechargeNo))
        return gson.fromJson(json, OrderListResponse::class.java)
            ?: OrderListResponse(code = 0, msg = "订单服务返回为空")
    }

    private fun discoverPlanProvider(rechargeNo: String): PlanProviderDiscovery {
        var hasServiceResponse = false

        candidatePlanProviders(rechargeNo).forEach { provider ->
            val response = runCatching { requestPlanInfo(provider, rechargeNo) }.getOrNull()
                ?: return@forEach
            hasServiceResponse = true
            if (response.isSuccess) {
                savePlanProvider(rechargeNo, provider)
                return PlanProviderDiscovery(provider, response)
            }
        }

        val message = if (hasServiceResponse) {
            "未在${planProviders.joinToString("、") { it.name }}查询到该充值号"
        } else {
            "套餐服务暂不可用"
        }
        return PlanProviderDiscovery(response = PlanLoginResponse(code = 0, msg = message))
    }

    suspend fun getPlanInfo(): Result<PlanLoginResponse> = withContext(Dispatchers.IO) {
        try {
            val rechargeNo = dataStore.getRechargeNo()
            if (rechargeNo.isEmpty()) return@withContext Result.failure(Exception("未设置充值号"))
            Result.success(discoverPlanProvider(rechargeNo).response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderList(): Result<OrderListResponse> = withContext(Dispatchers.IO) {
        try {
            val rechargeNo = dataStore.getRechargeNo()
            if (rechargeNo.isEmpty()) return@withContext Result.failure(Exception("未设置充值号"))
            val discovery = discoverPlanProvider(rechargeNo)
            val provider = discovery.provider
            if (provider == null) {
                return@withContext Result.success(
                    OrderListResponse(code = 0, msg = discovery.response.msg)
                )
            }
            Result.success(requestOrderList(provider, rechargeNo))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
