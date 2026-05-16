package com.flymero.mifimanager.ui.wifi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flymero.mifimanager.data.model.WlanInfo
import com.flymero.mifimanager.data.model.WlanSecurityInfo
import com.flymero.mifimanager.data.repository.MiFiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WifiState(
    val wlanInfo: WlanInfo = WlanInfo(),
    val securityInfo: WlanSecurityInfo = WlanSecurityInfo(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveResult: String? = null
)

@HiltViewModel
class WifiViewModel @Inject constructor(
    private val repository: MiFiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WifiState())
    val state: StateFlow<WifiState> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val wlan = repository.getWlanInfo()
            val security = repository.getWlanSecurityInfo()
            _state.value = WifiState(
                wlanInfo = wlan.getOrDefault(WlanInfo()),
                securityInfo = security.getOrDefault(WlanSecurityInfo()),
                isLoading = false
            )
        }
    }

    fun saveSecurity(ssid: String, password: String, mode: String, ssidBroadcast: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val result = repository.saveWifiSecurity(
                currentSecurityInfo = _state.value.securityInfo,
                ssid = ssid,
                password = password,
                mode = mode,
                ssidBroadcast = ssidBroadcast
            )
            _state.value = _state.value.copy(
                isSaving = false,
                saveResult = if (result.getOrNull()?.isSuccess == true) "保存成功" else "保存失败"
            )
            refresh()
        }
    }

    fun saveWlanSettings(wlanEnable: Boolean, apIsolate: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val result = repository.saveWifiSettings(
                currentWlanInfo = _state.value.wlanInfo,
                wlanEnable = wlanEnable,
                apIsolate = apIsolate
            )
            _state.value = _state.value.copy(
                isSaving = false,
                saveResult = if (result.getOrNull()?.isSuccess == true) "保存成功" else "保存失败"
            )
            refresh()
        }
    }

    fun clearResult() {
        _state.value = _state.value.copy(saveResult = null)
    }
}
