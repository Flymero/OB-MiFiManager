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
import javax.inject.Inject

data class DeviceState(
    val homepageInfo: HomepageInfo = HomepageInfo(),
    val wanInfo: WanInfo = WanInfo(),
    val dhcpInfo: DhcpInfo = DhcpInfo(),
    val apnInfo: ApnProfileInfo = ApnProfileInfo(),
    val simInfo: SimInfo = SimInfo(),
    val firmwareInfo: FirmwareInfo = FirmwareInfo(),
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

            _state.value = _state.value.copy(
                homepageInfo = homepage.getOrDefault(HomepageInfo()),
                wanInfo = wan.getOrDefault(WanInfo()),
                dhcpInfo = dhcp.getOrDefault(DhcpInfo()),
                apnInfo = apn.getOrDefault(ApnProfileInfo()),
                simInfo = sim.getOrDefault(SimInfo()),
                firmwareInfo = firmware.getOrDefault(FirmwareInfo()),
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
}
