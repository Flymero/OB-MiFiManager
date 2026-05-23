package com.flymero.mifimanager.ui.wifi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flymero.mifimanager.data.model.WlanInfo
import com.flymero.mifimanager.data.model.WlanSecurityInfo
import com.flymero.mifimanager.data.model.WpsInfo
import com.flymero.mifimanager.data.repository.MiFiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WifiState(
    val wlanInfo: WlanInfo = WlanInfo(),
    val securityInfo: WlanSecurityInfo = WlanSecurityInfo(),
    val wpsInfo: WpsInfo = WpsInfo(),
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
            val wps = repository.getWlanWpsInfo()
            _state.value = WifiState(
                wlanInfo = wlan.getOrDefault(WlanInfo()),
                securityInfo = security.getOrDefault(WlanSecurityInfo()),
                wpsInfo = wps.getOrDefault(WpsInfo()),
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

    fun saveWlanSettings(
        wlanEnable: Boolean,
        apIsolate: Boolean,
        bandwidthAcs: Boolean,
        channel: String,
        bandwidth: String,
        maxClients: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val result = repository.saveWifiSettings(
                currentWlanInfo = _state.value.wlanInfo,
                wlanEnable = wlanEnable,
                apIsolate = apIsolate,
                bandwidthAcs = bandwidthAcs,
                channel = channel,
                bandwidth = bandwidth,
                maxClients = maxClients
            )
            _state.value = _state.value.copy(
                isSaving = false,
                saveResult = if (result.getOrNull()?.isSuccess == true) "保存成功" else "保存失败"
            )
            refresh()
        }
    }

    fun saveWifiAutoOff(enabled: Boolean, minutes: String) {
        val sleepMinutes = if (!enabled) {
            "0"
        } else {
            val value = minutes.toIntOrNull()
            if (value == null || value !in 10..60) {
                _state.value = _state.value.copy(saveResult = "请输入 10-60 分钟")
                return
            }
            value.toString()
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val result = repository.saveWifiAutoOff(sleepMinutes)
            _state.value = _state.value.copy(
                isSaving = false,
                saveResult = if (result.getOrNull()?.isSuccess == true) "定时设置已保存" else "保存失败"
            )
            refresh()
        }
    }

    fun startWpsPushButton() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val result = repository.startWpsPushButton()
            _state.value = _state.value.copy(
                isSaving = false,
                saveResult = if (result.getOrNull()?.isSuccess == true) "WPS 配对已启动" else "WPS 启动失败"
            )
            refresh()
        }
    }

    fun startWpsPin(pin: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val result = repository.startWpsPin(pin)
            _state.value = _state.value.copy(
                isSaving = false,
                saveResult = if (result.getOrNull()?.isSuccess == true) "WPS PIN 配对已启动" else "WPS PIN 启动失败"
            )
            refresh()
        }
    }

    fun cancelWps() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val result = repository.cancelWps()
            _state.value = _state.value.copy(
                isSaving = false,
                saveResult = if (result.getOrNull()?.isSuccess == true) "已取消 WPS 配对" else "取消失败"
            )
            refresh()
        }
    }

    fun clearResult() {
        _state.value = _state.value.copy(saveResult = null)
    }
}
