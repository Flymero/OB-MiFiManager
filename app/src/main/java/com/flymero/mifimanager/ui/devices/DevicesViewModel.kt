package com.flymero.mifimanager.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flymero.mifimanager.data.model.DeviceManagementInfo
import com.flymero.mifimanager.data.repository.MiFiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DevicesState(
    val deviceInfo: DeviceManagementInfo = DeviceManagementInfo(),
    val isLoading: Boolean = true,
    val actionResult: String? = null
)

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val repository: MiFiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DevicesState())
    val state: StateFlow<DevicesState> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val result = repository.getDeviceManagementInfo()
            _state.value = _state.value.copy(
                deviceInfo = result.getOrDefault(DeviceManagementInfo()),
                isLoading = false
            )
        }
    }

    fun blockDevice(mac: String) {
        viewModelScope.launch {
            val result = repository.blockDevice(mac)
            _state.value = _state.value.copy(
                actionResult = if (result.isSuccess) "设备已屏蔽" else "屏蔽失败"
            )
            refresh()
        }
    }

    fun unblockDevice(mac: String) {
        viewModelScope.launch {
            val result = repository.unblockDevice(mac)
            _state.value = _state.value.copy(
                actionResult = if (result.isSuccess) "已解除屏蔽" else "解除失败"
            )
            refresh()
        }
    }

    fun showMessage(message: String) {
        _state.value = _state.value.copy(actionResult = message)
    }

    fun clearResult() {
        _state.value = _state.value.copy(actionResult = null)
    }
}
