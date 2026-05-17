package com.flymero.mifimanager.ui.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flymero.mifimanager.data.local.DataStoreHelper
import com.flymero.mifimanager.data.model.*
import com.flymero.mifimanager.data.repository.MiFiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val repository: MiFiRepository,
    private val dataStore: DataStoreHelper
) : ViewModel() {

    private val _state = MutableStateFlow(DeviceState())
    val state: StateFlow<DeviceState> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val homepage = repository.getHomepageInfo()
            val wan = repository.getWanInfo()
            val dhcp = repository.getDhcpInfo()
            val apn = repository.getApnProfileInfo()
            val sim = repository.getSimInfo()
            val firmware = repository.getFirmwareInfo()
            val macFilters = repository.getWlanMacFiltersInfo()

            _state.value = _state.value.copy(
                homepageInfo = homepage.getOrDefault(HomepageInfo()),
                wanInfo = wan.getOrDefault(WanInfo()),
                dhcpInfo = dhcp.getOrDefault(DhcpInfo()),
                apnInfo = apn.getOrDefault(ApnProfileInfo()),
                simInfo = sim.getOrDefault(SimInfo()),
                firmwareInfo = firmware.getOrDefault(FirmwareInfo()),
                macFiltersInfo = macFilters.getOrDefault(_state.value.macFiltersInfo),
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
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull() == true) "设备正在重启..." else "重启失败"
            )
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
                exception.isDisconnectDuringApply() -> if (enabled) "MAC 黑名单已开启，请重新连接 Wi‑Fi 后确认" else "MAC 黑名单已关闭，请重新连接 Wi‑Fi 后确认"
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
            val result = repository.addMacToBlacklist(_state.value.macFiltersInfo, mac)
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull()?.isSuccess == true) "MAC 已加入黑名单" else "添加失败"
            )
            refresh()
        }
    }

    fun removeMacFromBlacklist(index: Int) {
        viewModelScope.launch {
            val result = repository.removeMacFromBlacklist(_state.value.macFiltersInfo, index)
            _state.value = _state.value.copy(
                actionResult = if (result.getOrNull()?.isSuccess == true) "MAC 已移除" else "删除失败"
            )
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

    private fun Throwable?.isDisconnectDuringApply(): Boolean = when (this) {
        is SocketException, is ConnectException, is UnknownHostException -> true
        is IOException -> true
        else -> false
    }
}
