package com.flymero.mifimanager.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flymero.mifimanager.data.model.SmsAuthTerminal
import com.flymero.mifimanager.data.repository.MiFiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InternetAuthState(
    val terminals: List<SmsAuthTerminal> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val actionResult: String? = null,
    val smsSent: Boolean = false,
    val verifyingMac: String? = null,
    val verifyingPhone: String = ""
)

@HiltViewModel
class InternetAuthViewModel @Inject constructor(
    private val repository: MiFiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InternetAuthState())
    val state: StateFlow<InternetAuthState> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val result = repository.getSmsAuthTerminalList()
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    terminals = result.getOrNull()?.terminalList ?: emptyList(),
                    isLoading = false,
                    error = null
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "上网认证功能不可用"
                )
            }
        }
    }

    fun sendSms(phoneNum: String, mac: String) {
        viewModelScope.launch {
            val result = repository.sendSmsAuth(phoneNum, mac)
            if (result.isSuccess && result.getOrNull()?.isSuccess == true) {
                _state.value = _state.value.copy(
                    smsSent = true,
                    verifyingMac = mac,
                    verifyingPhone = phoneNum,
                    actionResult = "验证码已发送"
                )
            } else {
                _state.value = _state.value.copy(
                    actionResult = result.getOrNull()?.message ?: "发送失败"
                )
            }
        }
    }

    fun verifyCode(mac: String, code: String) {
        viewModelScope.launch {
            val phoneNum = _state.value.verifyingPhone
            val result = repository.verifySmsAuth(phoneNum, mac, code)
            if (result.isSuccess && result.getOrNull()?.isSuccess == true) {
                _state.value = _state.value.copy(
                    smsSent = false,
                    verifyingMac = null,
                    actionResult = "认证成功"
                )
                refresh()
            } else {
                _state.value = _state.value.copy(
                    actionResult = result.getOrNull()?.message ?: "验证失败"
                )
            }
        }
    }

    fun clearResult() {
        _state.value = _state.value.copy(actionResult = null)
    }

    fun cancelVerify() {
        _state.value = _state.value.copy(smsSent = false, verifyingMac = null)
    }
}
