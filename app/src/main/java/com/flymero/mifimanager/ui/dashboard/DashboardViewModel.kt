package com.flymero.mifimanager.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flymero.mifimanager.data.model.EngineeringInfo
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DashboardState(
    val statusInfo: StatusInfo = StatusInfo(),
    val homepageInfo: HomepageInfo = HomepageInfo(),
    val statisticsInfo: StatisticsInfo = StatisticsInfo(),
    val planInfo: PlanInfo? = null,
    val engineeringInfo: EngineeringInfo = EngineeringInfo(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val localUptimeSeconds: Long = 0,
    val cellularConnecting: Boolean = false,
    val isRefreshing: Boolean = false,
    val refreshMessage: String? = null,
    val lastUpdatedLabel: String? = null,
    val bandSummary: String = "--"
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: MiFiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    private var lastApiUptime: Long = 0

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
                refreshEngineering()
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
        _state.value = _state.value.copy(homepageInfo = result.getOrDefault(_state.value.homepageInfo))
    }

    private suspend fun refreshStatistics() {
        val result = repository.getStatisticsInfo()
        _state.value = _state.value.copy(statisticsInfo = result.getOrDefault(_state.value.statisticsInfo))
    }

    private suspend fun refreshPlan() {
        val result = repository.getPlanInfo()
        if (result.isSuccess && result.getOrNull()?.isSuccess == true) {
            _state.value = _state.value.copy(planInfo = result.getOrNull()?.data)
        }
    }

    private suspend fun refreshEngineering() {
        val result = repository.getEngineeringInfo()
        val engineering = result.getOrDefault(_state.value.engineeringInfo)
        val lte = engineering.lte
        val bandSummary = if (lte != null) {
            "Band ${lte.band} · ${lte.bandwidthMhz()}"
        } else {
            "--"
        }
        _state.value = _state.value.copy(engineeringInfo = engineering, bandSummary = bandSummary)
    }

    fun refreshNow() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            runCatching {
                refreshStatus()
                refreshHomepage()
                refreshStatistics()
                refreshPlan()
                refreshEngineering()
            }.onSuccess {
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    refreshMessage = "已刷新",
                    lastUpdatedLabel = "更新于 ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}"
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    refreshMessage = "更新失败，点按重试"
                )
            }
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

    fun clearRefreshMessage() {
        _state.value = _state.value.copy(refreshMessage = null)
    }
}
