package com.flymero.mifimanager.ui.signal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flymero.mifimanager.data.model.EngineeringInfo
import com.flymero.mifimanager.data.model.PdpContext
import com.flymero.mifimanager.data.model.StatusInfo
import com.flymero.mifimanager.data.model.WanPdpContextInfo
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
    val pdpContext: PdpContext? = null,
    val networkName: String = "",
    val isLoading: Boolean = true,
    val localCurrentConnSeconds: Long = 0,
    val localTotalConnSeconds: Long = 0
)

@HiltViewModel
class SignalViewModel @Inject constructor(
    private val repository: MiFiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SignalState())
    val state: StateFlow<SignalState> = _state

    private var lastApiCurrentConn: Long = 0
    private var lastApiTotalConn: Long = 0

    init {
        viewModelScope.launch {
            while (true) {
                val eng = repository.getEngineeringInfo()
                val status = repository.getStatusInfo()
                val pdp = repository.getWanPdpContextInfo()
                val newEng = eng.getOrDefault(_state.value.engineeringInfo)
                val newStatus = status.getOrDefault(_state.value.statusInfo)
                val pdpInfo = pdp.getOrDefault(WanPdpContextInfo())
                val newPdp = pdpInfo.pdpList.firstOrNull() ?: _state.value.pdpContext
                val newNetwork = pdpInfo.networkName.ifBlank { _state.value.networkName }

                val apiCurrent = newPdp?.currentConnSeconds?.toLongOrNull() ?: 0
                val apiTotal = newPdp?.totalConnSeconds?.toLongOrNull() ?: 0
                if (apiCurrent != lastApiCurrentConn || apiTotal != lastApiTotalConn) {
                    lastApiCurrentConn = apiCurrent
                    lastApiTotalConn = apiTotal
                }

                if (newEng != _state.value.engineeringInfo ||
                    newStatus != _state.value.statusInfo ||
                    newPdp != _state.value.pdpContext ||
                    newNetwork != _state.value.networkName ||
                    _state.value.isLoading
                ) {
                    _state.value = SignalState(
                        engineeringInfo = newEng,
                        statusInfo = newStatus,
                        pdpContext = newPdp,
                        networkName = newNetwork,
                        isLoading = false,
                        localCurrentConnSeconds = apiCurrent,
                        localTotalConnSeconds = apiTotal
                    )
                }
                delay(5000)
            }
        }
        startConnTimeTicker()
    }

    private fun startConnTimeTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _state.value.localCurrentConnSeconds
                val total = _state.value.localTotalConnSeconds
                if (current > 0 || total > 0) {
                    _state.value = _state.value.copy(
                        localCurrentConnSeconds = current + 1,
                        localTotalConnSeconds = total + 1
                    )
                }
            }
        }
    }
}
