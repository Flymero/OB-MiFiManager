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
    val isMacFilterSyncing: Boolean = false
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
            val homepage = async { repository.getHomepageInfo() }
            val wan = async { repository.getWanInfo() }
            val dhcp = async { repository.getDhcpInfo() }
            val apn = async { repository.getApnProfileInfo() }
            val sim = async { repository.getSimInfo() }
            val firmware = async { repository.getFirmwareInfo() }
            val macFilters = async { repository.getWlanMacFiltersInfo() }

            _state.value = _state.value.copy(
                homepageInfo = homepage.await().getOrDefault(HomepageInfo()),
                wanInfo = wan.await().getOrDefault(WanInfo()),
                dhcpInfo = dhcp.await().getOrDefault(DhcpInfo()),
                apnInfo = apn.await().getOrDefault(ApnProfileInfo()),
                simInfo = sim.await().getOrDefault(SimInfo()),
                firmwareInfo = firmware.await().getOrDefault(FirmwareInfo()),
                macFiltersInfo = macFilters.await().getOrDefault(_state.value.macFiltersInfo),
                isLoading = false,
                isPlanLoading = true
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
            val result = repository.setNetworkMode(_state.value.wanInfo, mode)
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull()?.isSuccess == true) "网络模式已切换" else "切换失败"
            )
            refresh()
        }
    }

    fun switchSim(simId: String) {
        viewModelScope.launch {
            val result = if (simId == "4") {
                repository.setSimConfig("0")
            } else {
                repository.setSimConfig("1", simId)
            }
            _state.value = _state.value.copy(
                actionResult = if (result.isSuccess) "SIM卡已切换" else "切换失败"
            )
            refresh()
        }
    }

    fun restartDevice() {
        viewModelScope.launch {
            val result = repository.restartDevice()
            if (result.getOrNull() == true) {
                globalMessageBus.post("设备正在重启…")
            } else {
                _state.value = _state.value.copy(actionResult = "重启失败")
            }
        }
    }

    fun restoreFactory() {
        viewModelScope.launch {
            repository.restoreFactory()
            _state.value = _state.value.copy(actionResult = "正在恢复出厂设置...")
        }
    }

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            val username = dataStore.getSavedUsername().ifBlank { "admin" }
            val result = repository.changePassword(username, newPassword)
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull()?.isSuccess == true) "密码修改成功" else "密码修改失败"
            )
        }
    }

    fun saveDns(enable: Boolean, dns1: String, dns2: String) {
        viewModelScope.launch {
            val data = linkedMapOf<String, Any>(
                "dns_enable" to if (enable) "1" else "0",
                "dns1" to dns1,
                "dns2" to dns2
            )
            val result = repository.setLan(data)
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull()?.isSuccess == true) "DNS 设置已保存" else "DNS 设置失败"
            )
            refresh()
        }
    }

    fun addStaticIp(mac: String, ip: String) {
        viewModelScope.launch {
            val data = linkedMapOf<String, Any>(
                "Fixed_IP_list" to listOf(mapOf("index" to 1, "mac" to mac, "ip" to ip))
            )
            val result = repository.setLan(data)
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull()?.isSuccess == true) "静态 IP 已添加" else "添加失败"
            )
            refresh()
        }
    }

    fun deleteStaticIp(mac: String) {
        viewModelScope.launch {
            val data = linkedMapOf<String, Any>(
                "Fixed_IP_list" to listOf(mapOf("delete" to 1, "mac" to mac))
            )
            val result = repository.setLan(data)
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull()?.isSuccess == true) "静态 IP 已删除" else "删除失败"
            )
            refresh()
        }
    }

    fun setMacBlacklistEnabled(enabled: Boolean) {
        viewModelScope.launch {
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
            _state.value = _state.value.copy(actionResult = actionResult)
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
            _state.value = _state.value.copy(actionResult = actionResult)
            refresh()
        }
    }

    fun removeMacFromBlacklist(index: Int) {
        viewModelScope.launch {
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
            _state.value = _state.value.copy(actionResult = actionResult)
            refresh()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            if (!dataStore.shouldRemember()) {
                dataStore.clearCredentials()
            }
            _state.value = _state.value.copy(isLoggedOut = true)
        }
    }

    fun clearResult() {
        _state.value = _state.value.copy(actionResult = null)
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
