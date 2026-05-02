package com.flymero.mifimanager.ui.signal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flymero.mifimanager.data.model.EngineeringInfo
import com.flymero.mifimanager.data.model.StatusInfo
import com.flymero.mifimanager.data.repository.MiFiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignalState(
    val engineeringInfo: EngineeringInfo = EngineeringInfo(),
    val statusInfo: StatusInfo = StatusInfo(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SignalViewModel @Inject constructor(
    private val repository: MiFiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SignalState())
    val state: StateFlow<SignalState> = _state

    init {
        viewModelScope.launch {
            while (true) {
                val eng = repository.getEngineeringInfo()
                val status = repository.getStatusInfo()
                _state.value = SignalState(
                    engineeringInfo = eng.getOrDefault(_state.value.engineeringInfo),
                    statusInfo = status.getOrDefault(_state.value.statusInfo),
                    isLoading = false
                )
                delay(5000)
            }
        }
    }
}
