package com.budgetr.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetr.app.data.model.AccountBalance
import com.budgetr.app.data.model.SheetTab
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
    val totalFixedCosts: Double = 0.0,
    val totalOneOffCosts: Double = 0.0,
    val totalAvailable: Double = 0.0,
    val error: String? = null,
    val userName: String? = null
)

private data class SummaryData(
    val balances: List<AccountBalance>,
    val totalAvailable: Double,
    val income: Double,
    val outgoings: Double,
    val fixedCosts: Double,
    val oneOffCosts: Double
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
                    .filter { it.category == TransactionCategory.INCOME || it.category == TransactionCategory.SALARY || it.category == TransactionCategory.RECURRING_INCOME }
                    .sumOf { it.amount }
                val fixedCosts = allTx
                    .filter { it.category == TransactionCategory.FIXED_COST }
                    .sumOf { kotlin.math.abs(it.amount) }
                val oneOffCosts = allTx
                    .filter { it.category == TransactionCategory.ONE_OFF_COST }
                    .sumOf { kotlin.math.abs(it.amount) }
                val outgoings = allTx
                    .filter { it.category != TransactionCategory.INCOME && it.category != TransactionCategory.SALARY && it.category != TransactionCategory.RECURRING_INCOME && it.category != TransactionCategory.TRANSFER }
                    .sumOf { kotlin.math.abs(it.amount) }
                val totalAvailable = balances.sumOf { it.remainingBalance }
                SummaryData(balances, totalAvailable, income, outgoings, fixedCosts, oneOffCosts)
            }.collect { data ->
                _uiState.update {
                    it.copy(
                        accountBalances = data.balances,
                        totalIncome = data.income,
                        totalOutgoings = data.outgoings,
                        totalFixedCosts = data.fixedCosts,
                        totalOneOffCosts = data.oneOffCosts,
                        totalAvailable = data.totalAvailable
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
