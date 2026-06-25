package com.flymero.mifimanager.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flymero.mifimanager.data.model.PlanInfo
import com.flymero.mifimanager.data.repository.MiFiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlanState(
    val planInfo: PlanInfo? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val repository: MiFiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlanState())
    val state: StateFlow<PlanState> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val result = repository.getPlanInfo()
            if (result.isSuccess && result.getOrNull()?.isSuccess == true) {
                _state.value = PlanState(
                    planInfo = result.getOrNull()?.data,
                    isLoading = false
                )
            } else {
                _state.value = PlanState(
                    isLoading = false,
                    error = result.getOrNull()?.msg?.ifBlank { null }
                        ?: result.exceptionOrNull()?.message
                        ?: "查询失败"
                )
            }
        }
    }
}
