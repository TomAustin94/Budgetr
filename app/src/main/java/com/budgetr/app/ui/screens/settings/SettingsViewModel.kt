package com.budgetr.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetr.app.util.AuthManager
import com.budgetr.app.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val spreadsheetId: String = "",
    val userEmail: String? = null,
    val userName: String? = null,
    val savedMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            spreadsheetId = prefs.getSpreadsheetId() ?: "",
            userEmail = authManager.getUserEmail(),
            userName = authManager.getUserName()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateSpreadsheetId(id: String) {
        _uiState.update { it.copy(spreadsheetId = id) }
    }

    fun saveSpreadsheetId() {
        prefs.setSpreadsheetId(_uiState.value.spreadsheetId.trim())
        _uiState.update { it.copy(savedMessage = "Spreadsheet ID saved!") }
    }

    fun clearSavedMessage() = _uiState.update { it.copy(savedMessage = null) }

    fun signOut(onComplete: () -> Unit) {
        authManager.signOut(onComplete)
    }
}
