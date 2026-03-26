package com.budgetr.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetr.app.data.model.AccountBalance
import com.budgetr.app.data.model.SheetTab
import com.budgetr.app.data.model.Transaction
import com.budgetr.app.data.model.TransactionCategory
import com.budgetr.app.data.repository.SheetsRepository
import com.budgetr.app.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val accountBalances: List<AccountBalance> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalOutgoings: Double = 0.0,
    val totalAvailable: Double = 0.0,
    val error: String? = null,
    val userName: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SheetsRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(userName = authManager.getUserName()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
        refresh()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.getAccountBalances(),
                repository.getTransactions(SheetTab.MONZO),
                repository.getTransactions(SheetTab.HALIFAX_DEBIT),
                repository.getTransactions(SheetTab.HALIFAX_CREDIT)
            ) { balances, monzoTx, halifaxDebitTx, halifaxCreditTx ->
                val allTx = monzoTx + halifaxDebitTx + halifaxCreditTx
                val income = allTx
                    .filter { it.category == TransactionCategory.INCOME }
                    .sumOf { it.amount }
                val outgoings = allTx
                    .filter { it.category != TransactionCategory.INCOME && it.category != TransactionCategory.TRANSFER }
                    .sumOf { kotlin.math.abs(it.amount) }
                // Available money = sum of actual account balances (avoids double-counting transfers)
                val totalAvailable = balances.sumOf { it.remainingBalance }
                Triple(Pair(balances, totalAvailable), income, outgoings)
            }.collect { (balanceData, income, outgoings) ->
                val (balances, totalAvailable) = balanceData
                _uiState.update {
                    it.copy(
                        accountBalances = balances,
                        totalIncome = income,
                        totalOutgoings = outgoings,
                        totalAvailable = totalAvailable
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            try {
                repository.refreshAccountBalances()
                repository.refreshTransactions(SheetTab.MONZO)
                repository.refreshTransactions(SheetTab.HALIFAX_DEBIT)
                repository.refreshTransactions(SheetTab.HALIFAX_CREDIT)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isRefreshing = false, isLoading = false) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
