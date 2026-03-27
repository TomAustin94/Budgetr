package com.budgetr.app.ui.screens.balances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetr.app.data.model.AccountBalance
import com.budgetr.app.data.model.BalanceRollover
import com.budgetr.app.data.repository.SheetsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AccountBalancesUiState(
    val isRefreshing: Boolean = false,
    val balances: List<AccountBalance> = emptyList(),
    val rollovers: List<BalanceRollover> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    // Rename dialog
    val renameAccount: AccountBalance? = null,
    val renameText: String = "",
    val isRenaming: Boolean = false,
    // Add account dialog
    val showAddAccount: Boolean = false,
    val newAccountName: String = "",
    val isAddingAccount: Boolean = false,
    // Rollover edit dialog
    val rolloverEditAccount: String? = null,
    val rolloverEditText: String = "",
    val isRecordingRollover: Boolean = false
)

@HiltViewModel
class AccountBalancesViewModel @Inject constructor(
    private val repository: SheetsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountBalancesUiState())
    val uiState: StateFlow<AccountBalancesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAccountBalances(),
                repository.getBalanceRollovers()
            ) { balances, rollovers -> Pair(balances, rollovers) }.collect { (balances, rollovers) ->
                _uiState.update { it.copy(balances = balances, rollovers = rollovers) }
            }
        }
        checkPayPeriod()
        refresh()
    }

    private fun checkPayPeriod() {
        viewModelScope.launch {
            try {
                val wasReset = repository.checkAndProcessNewPayPeriod()
                if (wasReset) {
                    _uiState.update { it.copy(successMessage = "New pay period started — one-off costs cleared") }
                }
            } catch (_: Exception) {}
        }
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

    // --- Rename ---

    fun showRenameDialog(balance: AccountBalance) {
        _uiState.update { it.copy(renameAccount = balance, renameText = balance.account) }
    }

    fun setRenameText(text: String) = _uiState.update { it.copy(renameText = text) }

    fun dismissRenameDialog() = _uiState.update { it.copy(renameAccount = null, renameText = "") }

    fun confirmRename() {
        val account = _uiState.value.renameAccount ?: return
        val newName = _uiState.value.renameText.trim()
        if (newName.isBlank() || newName == account.account) {
            dismissRenameDialog()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isRenaming = true) }
            try {
                repository.renameAccount(account.account, newName)
                _uiState.update { it.copy(renameAccount = null, renameText = "", isRenaming = false, successMessage = "Account renamed to \"$newName\"") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRenaming = false, error = e.message) }
            }
        }
    }

    // --- Add Account ---

    fun showAddAccountDialog() = _uiState.update { it.copy(showAddAccount = true, newAccountName = "") }

    fun setNewAccountName(name: String) = _uiState.update { it.copy(newAccountName = name) }

    fun dismissAddAccountDialog() = _uiState.update { it.copy(showAddAccount = false, newAccountName = "") }

    fun confirmAddAccount() {
        val name = _uiState.value.newAccountName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingAccount = true) }
            try {
                repository.addAccount(name)
                _uiState.update { it.copy(showAddAccount = false, newAccountName = "", isAddingAccount = false, successMessage = "\"$name\" account created") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAddingAccount = false, error = e.message) }
            }
        }
    }

    // --- Rollover ---

    fun showRolloverEditDialog(account: String) {
        val existing = _uiState.value.rollovers.find { it.account == account }
        _uiState.update {
            it.copy(
                rolloverEditAccount = account,
                rolloverEditText = existing?.rolloverAmount?.let { amt -> if (amt == 0.0) "" else amt.toString() } ?: ""
            )
        }
    }

    fun setRolloverEditText(text: String) = _uiState.update { it.copy(rolloverEditText = text) }

    fun dismissRolloverEditDialog() = _uiState.update { it.copy(rolloverEditAccount = null, rolloverEditText = "") }

    fun confirmRolloverEdit() {
        val account = _uiState.value.rolloverEditAccount ?: return
        val amount = _uiState.value.rolloverEditText.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            _uiState.update { it.copy(isRecordingRollover = true) }
            try {
                val today = SimpleDateFormat("dd/MM/yyyy", Locale.UK).format(Date())
                repository.recordRollover(account, amount, today)
                _uiState.update { it.copy(rolloverEditAccount = null, rolloverEditText = "", isRecordingRollover = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRecordingRollover = false, error = e.message) }
            }
        }
    }

    fun deleteRollover(account: String) {
        viewModelScope.launch {
            try {
                repository.deleteRollover(account)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    /** Records current balance of every account as rollover for the next pay period. */
    fun recordAllRollovers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRecordingRollover = true) }
            try {
                val today = SimpleDateFormat("dd/MM/yyyy", Locale.UK).format(Date())
                _uiState.value.balances.forEach { balance ->
                    repository.recordRollover(balance.account, balance.remainingBalance, today)
                }
                _uiState.update { it.copy(isRecordingRollover = false, successMessage = "Rollover recorded for ${_uiState.value.balances.size} accounts") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRecordingRollover = false, error = e.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearSuccessMessage() = _uiState.update { it.copy(successMessage = null) }
}
