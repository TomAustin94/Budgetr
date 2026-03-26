package com.budgetr.app.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetr.app.data.model.SheetTab
import com.budgetr.app.data.model.Transaction
import com.budgetr.app.data.model.TransactionCategory
import com.budgetr.app.data.repository.SheetsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionsUiState(
    val isRefreshing: Boolean = false,
    val selectedTab: SheetTab = SheetTab.MONZO,
    val transactions: List<Transaction> = emptyList(),
    val categoryFilter: TransactionCategory? = null,
    val error: String? = null,
    val transactionToDelete: Transaction? = null,
    val transactionToEdit: Transaction? = null,
    val showAddSheet: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: SheetsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private val selectedTabFlow = MutableStateFlow(SheetTab.MONZO)
    private val categoryFilterFlow = MutableStateFlow<TransactionCategory?>(null)

    init {
        viewModelScope.launch {
            selectedTabFlow.flatMapLatest { tab ->
                combine(
                    repository.getTransactions(tab),
                    categoryFilterFlow
                ) { transactions, filter ->
                    if (filter == null) transactions
                    else transactions.filter { it.category == filter }
                }
            }.collect { filtered ->
                _uiState.update { it.copy(transactions = filtered) }
            }
        }
        refresh()
    }

    fun selectTab(tab: SheetTab) {
        selectedTabFlow.value = tab
        _uiState.update { it.copy(selectedTab = tab) }
        refresh(tab)
    }

    fun setCategoryFilter(category: TransactionCategory?) {
        categoryFilterFlow.value = category
        _uiState.update { it.copy(categoryFilter = category) }
    }

    fun refresh(tab: SheetTab = _uiState.value.selectedTab) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            try {
                repository.refreshTransactions(tab)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun showAddSheet() = _uiState.update { it.copy(showAddSheet = true, transactionToEdit = null) }
    fun showEditSheet(transaction: Transaction) = _uiState.update { it.copy(transactionToEdit = transaction, showAddSheet = true) }
    fun dismissSheet() = _uiState.update { it.copy(showAddSheet = false, transactionToEdit = null) }

    fun confirmDelete(transaction: Transaction) = _uiState.update { it.copy(transactionToDelete = transaction) }
    fun dismissDelete() = _uiState.update { it.copy(transactionToDelete = null) }

    fun saveTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                if (transaction.rowIndex > 0) {
                    repository.updateTransaction(transaction)
                } else {
                    repository.addTransaction(transaction)
                }
                _uiState.update { it.copy(showAddSheet = false, transactionToEdit = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteTransaction() {
        val transaction = _uiState.value.transactionToDelete ?: return
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
                _uiState.update { it.copy(transactionToDelete = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(transactionToDelete = null, error = e.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
