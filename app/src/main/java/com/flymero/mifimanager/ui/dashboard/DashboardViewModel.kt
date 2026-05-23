package com.flymero.mifimanager.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flymero.mifimanager.data.model.EngineeringInfo
import com.flymero.mifimanager.data.model.HomepageInfo
import com.flymero.mifimanager.data.model.OrderItem
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
    val engineeringInfo: EngineeringInfo = EngineeringInfo(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val localUptimeSeconds: Long = 0,
    val cellularConnecting: Boolean = false,
    val isPlanRefreshing: Boolean = false,
    val refreshMessage: String? = null,
    val bandSummary: String = "--",
    val routerReachable: Boolean = true,
    val lastReachableAtLeastOnce: Boolean = false,
    val orderList: List<OrderItem> = emptyList(),
    val isOrderLoading: Boolean = false,
    val orderError: String? = null
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
        refreshPlanOnce()
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
                refreshEngineering()
                delay(5000)
            }
        }
    }

    private suspend fun refreshStatus() {
        val statusResult = repository.getStatusInfo()
        val status = statusResult.getOrDefault(_state.value.statusInfo)

        val apiUptime = status.runSeconds.toLongOrNull() ?: 0
        if (statusResult.isSuccess && apiUptime != lastApiUptime) {
            lastApiUptime = apiUptime
            _state.value = _state.value.copy(localUptimeSeconds = apiUptime)
        }

        val current = _state.value
        val newReachable = statusResult.isSuccess
        val newError = statusResult.exceptionOrNull()?.message
        if (current.statusInfo != status || current.isLoading || current.routerReachable != newReachable || current.error != newError) {
            _state.value = current.copy(
                statusInfo = status,
                isLoading = false,
                error = newError,
                routerReachable = newReachable,
                lastReachableAtLeastOnce = current.lastReachableAtLeastOnce || newReachable
            )
        }
    }

    private suspend fun refreshHomepage() {
        val result = repository.getHomepageInfo()
        val homepage = result.getOrDefault(_state.value.homepageInfo)
        if (homepage != _state.value.homepageInfo) {
            _state.value = _state.value.copy(homepageInfo = homepage)
        }
    }

    private suspend fun refreshStatistics() {
        val result = repository.getStatisticsInfo()
        val stats = result.getOrDefault(_state.value.statisticsInfo)
        if (stats != _state.value.statisticsInfo) {
            _state.value = _state.value.copy(statisticsInfo = stats)
        }
    }

    private suspend fun refreshPlan() {
        val result = repository.getPlanInfo()
        if (result.isSuccess && result.getOrNull()?.isSuccess == true) {
            _state.value = _state.value.copy(planInfo = result.getOrNull()?.data)
        }
    }

    private fun refreshPlanOnce() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isPlanRefreshing = true)
            runCatching { refreshPlan() }
            _state.value = _state.value.copy(isPlanRefreshing = false)
        }
    }

    fun refreshPlanManually() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isPlanRefreshing = true)
            runCatching {
                refreshPlan()
            }.onSuccess {
                _state.value = _state.value.copy(
                    isPlanRefreshing = false,
                    refreshMessage = "套餐信息已刷新"
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    isPlanRefreshing = false,
                    refreshMessage = "套餐信息刷新失败"
                )
            }
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
        if (engineering != _state.value.engineeringInfo || bandSummary != _state.value.bandSummary) {
            _state.value = _state.value.copy(engineeringInfo = engineering, bandSummary = bandSummary)
        }
    }

    fun toggleCellular(connect: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(cellularConnecting = true)
            repository.toggleCellular(connect)
            delay(if (connect) 8000 else 2000)
            refreshHomepage()
            _state.value = _state.value.copy(cellularConnecting = false)
        }
    }

    fun clearRefreshMessage() {
        _state.value = _state.value.copy(refreshMessage = null)
    }

    fun fetchOrders() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isOrderLoading = true, orderError = null)
            val result = repository.getOrderList()
            if (result.isSuccess && result.getOrNull()?.isSuccess == true) {
                _state.value = _state.value.copy(
                    orderList = result.getOrNull()?.data?.list ?: emptyList(),
                    isOrderLoading = false
                )
            } else {
                _state.value = _state.value.copy(
                    isOrderLoading = false,
                    orderError = "获取订单失败"
                )
            }
        }
    }
}
