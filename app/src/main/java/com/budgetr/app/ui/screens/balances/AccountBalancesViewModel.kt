package com.budgetr.app.ui.screens.balances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetr.app.data.model.AccountBalance
import com.budgetr.app.data.repository.SheetsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountBalancesUiState(
    val isRefreshing: Boolean = false,
    val balances: List<AccountBalance> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AccountBalancesViewModel @Inject constructor(
    private val repository: SheetsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountBalancesUiState())
    val uiState: StateFlow<AccountBalancesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAccountBalances().collect { balances ->
                _uiState.update { it.copy(balances = balances) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            try {
                repository.refreshAccountBalances()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
