package com.flymero.mifimanager.ui.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flymero.mifimanager.data.local.DataStoreHelper
import com.flymero.mifimanager.data.model.*
import com.flymero.mifimanager.data.repository.MiFiRepository
import com.flymero.mifimanager.ui.GlobalMessageBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import javax.inject.Inject

data class DeviceState(
    val homepageInfo: HomepageInfo = HomepageInfo(),
    val wanInfo: WanInfo = WanInfo(),
    val dhcpInfo: DhcpInfo = DhcpInfo(),
    val apnInfo: ApnProfileInfo = ApnProfileInfo(),
    val simInfo: SimInfo = SimInfo(),
    val firmwareInfo: FirmwareInfo = FirmwareInfo(),
    val macFiltersInfo: WlanMacFiltersInfo = WlanMacFiltersInfo(),
    val planEquipment: PlanEquipment? = null,
    val isLoading: Boolean = true,
    val isPlanLoading: Boolean = false,
    val actionResult: String? = null,
    val isLoggedOut: Boolean = false,
    val isMacFilterSyncing: Boolean = false,
    val isRefreshing: Boolean = false,
    val routerReachable: Boolean = true,
    val hasLoadedRouter: Boolean = false,
    val operationInProgress: Boolean = false,
    val operationMessage: String? = null,
    val operationAffectsConnection: Boolean = false
)

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val repository: MiFiRepository,
    private val dataStore: DataStoreHelper,
    private val globalMessageBus: GlobalMessageBus
) : ViewModel() {

    private val _state = MutableStateFlow(DeviceState())
    val state: StateFlow<DeviceState> = _state

    private var macFilterReconnectRefreshJob: Job? = null

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            val homepage = async { repository.getHomepageInfo() }
            val wan = async { repository.getWanInfo() }
            val dhcp = async { repository.getDhcpInfo() }
            val apn = async { repository.getApnProfileInfo() }
            val sim = async { repository.getSimInfo() }
            val firmware = async { repository.getFirmwareInfo() }
            val macFilters = async { repository.getWlanMacFiltersInfo() }

            val homepageResult = homepage.await()
            val wanResult = wan.await()
            val dhcpResult = dhcp.await()
            val apnResult = apn.await()
            val simResult = sim.await()
            val firmwareResult = firmware.await()
            val macFiltersResult = macFilters.await()
            val routerReachable = listOf<Result<*>>(
                homepageResult,
                wanResult,
                dhcpResult,
                apnResult,
                simResult,
                firmwareResult,
                macFiltersResult
            ).any { it.isSuccess }

            _state.value = _state.value.copy(
                homepageInfo = homepageResult.getOrDefault(_state.value.homepageInfo),
                wanInfo = wanResult.getOrDefault(_state.value.wanInfo),
                dhcpInfo = dhcpResult.getOrDefault(_state.value.dhcpInfo),
                apnInfo = apnResult.getOrDefault(_state.value.apnInfo),
                simInfo = simResult.getOrDefault(_state.value.simInfo),
                firmwareInfo = firmwareResult.getOrDefault(_state.value.firmwareInfo),
                macFiltersInfo = macFiltersResult.getOrDefault(_state.value.macFiltersInfo),
                isLoading = false,
                isPlanLoading = true,
                isRefreshing = false,
                routerReachable = routerReachable,
                hasLoadedRouter = _state.value.hasLoadedRouter || routerReachable
            )

            val plan = repository.getPlanInfo()
            _state.value = _state.value.copy(
                planEquipment = if (plan.isSuccess && plan.getOrNull()?.isSuccess == true)
                    plan.getOrNull()?.data?.equipment else null,
                isPlanLoading = false
            )
        }
    }

    fun onResume() {
        refresh()
    }

    fun setNetworkMode(mode: String) {
        viewModelScope.launch {
            beginOperation("正在切换网络模式…", affectsConnection = true)
            val result = repository.setNetworkMode(_state.value.wanInfo, mode)
            val success = result.getOrNull()?.isSuccess == true
            _state.value = _state.value.copy(
                actionResult = if (success) "网络模式已切换，正在刷新状态" else "切换失败",
                operationInProgress = false,
                operationMessage = null,
                operationAffectsConnection = false
            )
            if (success) delay(3000)
            refresh()
        }
    }

    fun switchSim(simId: String) {
        viewModelScope.launch {
            beginOperation("正在切换 SIM…", affectsConnection = true)
            val result = if (simId == "4") {
                repository.setSimConfig("0")
            } else {
                repository.setSimConfig("1", simId)
            }
            val success = result.getOrNull()?.isSuccess == true
            val message = if (success) "SIM卡已切换，正在刷新状态" else "SIM卡切换失败"
            _state.value = _state.value.copy(
                actionResult = null,
                operationInProgress = false,
                operationMessage = null,
                operationAffectsConnection = false
            )
            globalMessageBus.post(message)
            if (success) delay(5000)
            refresh()
        }
    }

    fun restartDevice() {
        viewModelScope.launch {
            beginOperation("设备正在重启…", affectsConnection = true)
            val result = repository.restartDevice()
            if (result.getOrNull() == true) {
                globalMessageBus.post("设备正在重启…")
                _state.value = _state.value.copy(
                    operationInProgress = false,
                    operationMessage = "设备正在重启，网络会短暂断开。",
                    operationAffectsConnection = true
                )
            } else {
                _state.value = _state.value.copy(
                    actionResult = "重启失败",
                    operationInProgress = false,
                    operationMessage = null,
                    operationAffectsConnection = false
                )
            }
        }
    }

    fun restoreFactory() {
        viewModelScope.launch {
            beginOperation("正在恢复出厂设置…", affectsConnection = true)
            repository.restoreFactory()
            _state.value = _state.value.copy(
                actionResult = "正在恢复出厂设置...",
                operationInProgress = false,
                operationMessage = "恢复出厂设置后需要重新连接设备。",
                operationAffectsConnection = true
            )
        }
    }

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            beginOperation("正在修改管理密码…")
            val username = dataStore.getSavedUsername().ifBlank { "admin" }
            val result = repository.changePassword(username, newPassword)
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull()?.isSuccess == true) "密码修改成功" else "密码修改失败",
                operationInProgress = false,
                operationMessage = null,
                operationAffectsConnection = false
            )
        }
    }

    fun saveDns(enable: Boolean, dns1: String, dns2: String) {
        viewModelScope.launch {
            beginOperation("正在保存 DNS 设置…")
            val data = linkedMapOf<String, Any>(
                "dns_enable" to if (enable) "1" else "0",
                "dns1" to dns1,
                "dns2" to dns2
            )
            val result = repository.setLan(data)
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull()?.isSuccess == true) "DNS 设置已保存" else "DNS 设置失败",
                operationInProgress = false,
                operationMessage = null,
                operationAffectsConnection = false
            )
            refresh()
        }
    }

    fun addStaticIp(mac: String, ip: String) {
        viewModelScope.launch {
            beginOperation("正在添加静态 IP…")
            val data = linkedMapOf<String, Any>(
                "Fixed_IP_list" to listOf(mapOf("index" to 1, "mac" to mac, "ip" to ip))
            )
            val result = repository.setLan(data)
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull()?.isSuccess == true) "静态 IP 已添加" else "添加失败",
                operationInProgress = false,
                operationMessage = null,
                operationAffectsConnection = false
            )
            refresh()
        }
    }

    fun deleteStaticIp(mac: String) {
        viewModelScope.launch {
            beginOperation("正在删除静态 IP…")
            val data = linkedMapOf<String, Any>(
                "Fixed_IP_list" to listOf(mapOf("delete" to 1, "mac" to mac))
            )
            val result = repository.setLan(data)
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull()?.isSuccess == true) "静态 IP 已删除" else "删除失败",
                operationInProgress = false,
                operationMessage = null,
                operationAffectsConnection = false
            )
            refresh()
        }
    }

    fun setMacBlacklistEnabled(enabled: Boolean) {
        viewModelScope.launch {
            beginOperation(if (enabled) "正在开启 MAC 黑名单…" else "正在关闭 MAC 黑名单…", affectsConnection = true)
            val previous = _state.value.macFiltersInfo
            _state.value = _state.value.copy(
                macFiltersInfo = previous.copy(enable = if (enabled) "1" else "0")
            )

            val result = repository.setMacBlacklistEnabled(previous, enabled)
            val exception = result.exceptionOrNull()
            val actionResult = when {
                result.getOrNull()?.isSuccess == true -> if (enabled) "MAC 黑名单已开启" else "MAC 黑名单已关闭"
                exception.isDisconnectDuringApply() -> {
                    scheduleMacFilterReconnectRefresh()
                    if (enabled) "MAC 黑名单已开启，请重新连接 Wi‑Fi 后确认" else "MAC 黑名单已关闭，请重新连接 Wi‑Fi 后确认"
                }
                else -> {
                    _state.value = _state.value.copy(macFiltersInfo = previous)
                    "MAC 黑名单设置失败"
                }
            }
            _state.value = _state.value.copy(
                actionResult = actionResult,
                operationInProgress = false,
                operationMessage = null,
                operationAffectsConnection = false
            )
            refresh()
        }
    }

    fun addMacToBlacklist(mac: String) {
        val normalizedMac = mac.trim().replace('-', ':').uppercase()
        if (normalizedMac == _state.value.macFiltersInfo.normalizedCurrentDeviceMac()) {
            _state.value = _state.value.copy(actionResult = "不能将当前设备加入黑名单")
            return
        }
        viewModelScope.launch {
            beginOperation("正在添加 MAC 黑名单…", affectsConnection = true)
            val previous = _state.value.macFiltersInfo
            if (previous.blacklistEntries().none { it.mac.equals(normalizedMac, ignoreCase = true) }) {
                _state.value = _state.value.copy(
                    macFiltersInfo = previous.copy(
                        denyList = previous.blacklistEntries() + MacFilterEntry(mac = normalizedMac)
                    )
                )
            }

            val result = repository.addMacToBlacklist(previous, mac)
            val exception = result.exceptionOrNull()
            val actionResult = when {
                result.getOrNull()?.isSuccess == true -> "MAC 已加入黑名单"
                exception.isDisconnectDuringApply() -> {
                    scheduleMacFilterReconnectRefresh()
                    "MAC 已加入黑名单，请重新连接 Wi‑Fi 后确认"
                }
                else -> {
                    _state.value = _state.value.copy(macFiltersInfo = previous)
                    "添加失败"
                }
            }
            _state.value = _state.value.copy(
                actionResult = actionResult,
                operationInProgress = false,
                operationMessage = null,
                operationAffectsConnection = false
            )
            refresh()
        }
    }

    fun removeMacFromBlacklist(index: Int) {
        viewModelScope.launch {
            beginOperation("正在移除 MAC 黑名单…", affectsConnection = true)
            val previous = _state.value.macFiltersInfo
            val updatedEntries = previous.blacklistEntries().filterIndexed { entryIndex, _ -> entryIndex != index }
            _state.value = _state.value.copy(
                macFiltersInfo = previous.copy(denyList = updatedEntries)
            )

            val result = repository.removeMacFromBlacklist(previous, index)
            val exception = result.exceptionOrNull()
            val actionResult = when {
                result.getOrNull()?.isSuccess == true -> "MAC 已移除"
                exception.isDisconnectDuringApply() -> {
                    scheduleMacFilterReconnectRefresh()
                    "MAC 已移除，请重新连接 Wi‑Fi 后确认"
                }
                else -> {
                    _state.value = _state.value.copy(macFiltersInfo = previous)
                    "删除失败"
                }
            }
            _state.value = _state.value.copy(
                actionResult = actionResult,
                operationInProgress = false,
                operationMessage = null,
                operationAffectsConnection = false
            )
            refresh()
        }
    }

    fun saveApnProfile(
        profileName: String,
        apn: String,
        ipType: String,
        authType: String,
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            beginOperation("正在保存 APN…", affectsConnection = true)
            val result = repository.saveApnProfile(
                currentInfo = _state.value.apnInfo,
                profileName = profileName,
                apn = apn,
                ipType = ipType,
                authType = authType,
                username = username,
                password = password
            )
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull()?.isSuccess == true) "APN 已保存" else "APN 保存失败",
                operationInProgress = false,
                operationMessage = null,
                operationAffectsConnection = false
            )
            refresh()
        }
    }

    fun setDefaultApn(profileName: String) {
        viewModelScope.launch {
            beginOperation("正在切换默认 APN…", affectsConnection = true)
            val result = repository.setDefaultApn(_state.value.apnInfo, profileName)
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull()?.isSuccess == true) "默认 APN 已切换" else "切换失败",
                operationInProgress = false,
                operationMessage = null,
                operationAffectsConnection = false
            )
            if (result.getOrNull()?.isSuccess == true) delay(2500)
            refresh()
        }
    }

    fun deleteApnProfile(profileName: String) {
        viewModelScope.launch {
            beginOperation("正在删除 APN…")
            val result = repository.deleteApnProfile(profileName)
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull()?.isSuccess == true) "APN 已删除" else "删除失败",
                operationInProgress = false,
                operationMessage = null,
                operationAffectsConnection = false
            )
            refresh()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            if (!dataStore.shouldRemember()) {
                dataStore.clearLoginCredentials()
            }
            _state.value = _state.value.copy(isLoggedOut = true)
        }
    }

    fun clearResult() {
        _state.value = _state.value.copy(actionResult = null)
    }

    private fun beginOperation(message: String, affectsConnection: Boolean = false) {
        _state.value = _state.value.copy(
            operationInProgress = true,
            operationMessage = message,
            operationAffectsConnection = affectsConnection
        )
    }

    private fun scheduleMacFilterReconnectRefresh() {
        macFilterReconnectRefreshJob?.cancel()
        macFilterReconnectRefreshJob = viewModelScope.launch {
            _state.value = _state.value.copy(isMacFilterSyncing = true)
            repeat(12) {
                delay(3000)
                val macFilters = repository.getWlanMacFiltersInfo()
                if (macFilters.isSuccess) {
                    _state.value = _state.value.copy(
                        macFiltersInfo = macFilters.getOrDefault(_state.value.macFiltersInfo),
                        isMacFilterSyncing = false
                    )
                    return@launch
                }
            }
            _state.value = _state.value.copy(isMacFilterSyncing = false)
        }
    }

    private fun Throwable?.isDisconnectDuringApply(): Boolean = when (this) {
        is SocketException, is ConnectException, is UnknownHostException -> true
        is IOException -> true
        else -> false
    }
}
