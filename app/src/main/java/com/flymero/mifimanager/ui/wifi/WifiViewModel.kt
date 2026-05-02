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

    fun saveSecurity(ssid: String, password: String, mode: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val encodedSsid = _state.value.securityInfo.encodeSsid(ssid)
            val data = mapOf(
                "ssid" to encodedSsid,
                "mode" to mode,
                mode to mapOf("mode" to "AES-CCMP", "key" to password)
            )
            val result = repository.setWlanSecurity(data)
            _state.value = _state.value.copy(
                isSaving = false,
                saveResult = if (result.isSuccess) "保存成功" else "保存失败"
            )
            refresh()
        }
    }

    fun setWlanSettings(channel: String, maxClients: String, apIsolate: String, wlanEnable: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val data = mapOf(
                "channel" to channel,
                "max_clients" to maxClients,
                "ap_isolate" to apIsolate,
                "wlan_enable" to wlanEnable
            )
            val result = repository.setWlan(data)
            _state.value = _state.value.copy(
                isSaving = false,
                saveResult = if (result.isSuccess) "保存成功" else "保存失败"
            )
            refresh()
        }
    }

    fun clearResult() {
        _state.value = _state.value.copy(saveResult = null)
    }
}
