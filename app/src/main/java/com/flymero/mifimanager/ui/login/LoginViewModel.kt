package com.flymero.mifimanager.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flymero.mifimanager.data.local.DataStoreHelper
import com.flymero.mifimanager.data.repository.MiFiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val savedUsername: String = "",
    val savedPassword: String = "",
    val savedRechargeNo: String = "",
    val shouldRemember: Boolean = false,
    val autoLoginAttempted: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: MiFiRepository,
    private val dataStore: DataStoreHelper
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    init {
        loadSavedCredentials()
    }

    private fun loadSavedCredentials() {
        val savedRechargeNo = dataStore.getSavedRechargeNo()
        if (dataStore.hasSavedCredentials()) {
            _state.value = _state.value.copy(
                savedUsername = dataStore.getSavedUsername(),
                savedPassword = dataStore.getSavedPassword(),
                savedRechargeNo = savedRechargeNo,
                shouldRemember = true
            )
        } else if (savedRechargeNo.isNotEmpty()) {
            _state.value = _state.value.copy(savedRechargeNo = savedRechargeNo)
        }
    }

    fun login(username: String, password: String, rechargeNo: String = "", remember: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val success = repository.login(username, password)
            if (success) {
                dataStore.saveRechargeNo(rechargeNo)
                if (remember) {
                    dataStore.saveCredentials(username, password, rechargeNo)
                } else {
                    dataStore.clearLoginCredentials()
                }
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "登录失败，请检查账号密码"
                )
            }
        }
    }

    fun autoLogin() {
        if (_state.value.autoLoginAttempted) return
        _state.value = _state.value.copy(autoLoginAttempted = true)
        if (dataStore.hasSavedCredentials()) {
            login(
                dataStore.getSavedUsername(),
                dataStore.getSavedPassword(),
                dataStore.getSavedRechargeNo(),
                remember = true
            )
        }
    }

    fun clearSavedCredentials() {
        dataStore.clearCredentials()
        _state.value = _state.value.copy(
            savedUsername = "",
            savedPassword = "",
            savedRechargeNo = "",
            shouldRemember = false
        )
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
