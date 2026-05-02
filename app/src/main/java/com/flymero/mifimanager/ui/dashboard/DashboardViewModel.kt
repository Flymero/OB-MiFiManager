package com.flymero.mifimanager.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flymero.mifimanager.data.model.HomepageInfo
import com.flymero.mifimanager.data.model.PlanInfo
import com.flymero.mifimanager.data.model.StatisticsInfo
import com.flymero.mifimanager.data.model.StatusInfo
import com.flymero.mifimanager.data.repository.MiFiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val statusInfo: StatusInfo = StatusInfo(),
    val homepageInfo: HomepageInfo = HomepageInfo(),
    val statisticsInfo: StatisticsInfo = StatisticsInfo(),
    val planInfo: PlanInfo? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val localUptimeSeconds: Long = 0,
    val cellularConnecting: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: MiFiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    private var lastApiUptime: Long = 0
    private var lastUptimeTimestamp: Long = 0

    init {
        startStatusPolling()
        startUptimeCounter()
        startSlowPolling()
    }

    private fun startStatusPolling() {
        viewModelScope.launch {
            while (true) {
                refreshStatus()
                delay(1000)
            }
        }
    }

    private fun startUptimeCounter() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _state.value.localUptimeSeconds
                if (current > 0) {
                    _state.value = _state.value.copy(localUptimeSeconds = current + 1)
                }
            }
        }
    }

    private fun startSlowPolling() {
        viewModelScope.launch {
            while (true) {
                refreshHomepage()
                refreshStatistics()
                refreshPlan()
                delay(5000)
            }
        }
    }

    private suspend fun refreshStatus() {
        val statusResult = repository.getStatusInfo()
        val status = statusResult.getOrDefault(_state.value.statusInfo)

        val apiUptime = status.runSeconds.toLongOrNull() ?: 0
        if (apiUptime != lastApiUptime) {
            lastApiUptime = apiUptime
            lastUptimeTimestamp = System.currentTimeMillis()
            _state.value = _state.value.copy(localUptimeSeconds = apiUptime)
        }

        _state.value = _state.value.copy(
            statusInfo = status,
            isLoading = false,
            error = statusResult.exceptionOrNull()?.message
        )
    }

    private suspend fun refreshHomepage() {
        val result = repository.getHomepageInfo()
        _state.value = _state.value.copy(
            homepageInfo = result.getOrDefault(_state.value.homepageInfo)
        )
    }

    private suspend fun refreshStatistics() {
        val result = repository.getStatisticsInfo()
        _state.value = _state.value.copy(
            statisticsInfo = result.getOrDefault(_state.value.statisticsInfo)
        )
    }

    private suspend fun refreshPlan() {
        val result = repository.getPlanInfo()
        if (result.isSuccess && result.getOrNull()?.isSuccess == true) {
            _state.value = _state.value.copy(planInfo = result.getOrNull()?.data)
        }
    }

    fun toggleCellular(connect: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(cellularConnecting = true)
            repository.toggleCellular(connect)
            delay(2000)
            refreshHomepage()
            _state.value = _state.value.copy(cellularConnecting = false)
        }
    }
}
