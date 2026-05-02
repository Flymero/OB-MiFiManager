package com.flymero.mifimanager.ui.traffic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flymero.mifimanager.data.model.StatisticsInfo
import com.flymero.mifimanager.data.repository.MiFiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrafficState(
    val stats: StatisticsInfo = StatisticsInfo(),
    val isLoading: Boolean = true,
    val actionResult: String? = null
)

@HiltViewModel
class TrafficViewModel @Inject constructor(
    private val repository: MiFiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TrafficState())
    val state: StateFlow<TrafficState> = _state

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                refresh()
                delay(3000)
            }
        }
    }

    private suspend fun refresh() {
        val result = repository.getStatisticsInfo()
        _state.value = _state.value.copy(
            stats = result.getOrDefault(_state.value.stats),
            isLoading = false
        )
    }

    fun clearStats() {
        viewModelScope.launch {
            val result = repository.clearTrafficStats()
            _state.value = _state.value.copy(
                actionResult = if (result.isSuccess) "流量统计已清零" else "操作失败"
            )
            refresh()
        }
    }

    fun clearResult() {
        _state.value = _state.value.copy(actionResult = null)
    }
}
