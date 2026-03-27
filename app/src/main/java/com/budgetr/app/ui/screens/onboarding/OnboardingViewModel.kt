package com.budgetr.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetr.app.data.api.DriveFile
import com.budgetr.app.data.repository.SheetsRepository
import com.budgetr.app.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingStep { WELCOME, CHOOSE, LOADING, DONE }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val isLoading: Boolean = false,
    val availableSheets: List<DriveFile> = emptyList(),
    val showSheetPicker: Boolean = false,
    val isLoadingSheets: Boolean = false,
    val templateName: String = "My Budgetr",
    val error: String? = null,
    val createdSheetName: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: SheetsRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun goToChoose() = _uiState.update { it.copy(step = OnboardingStep.CHOOSE) }

    fun setTemplateName(name: String) = _uiState.update { it.copy(templateName = name) }

    fun createTemplate(onComplete: () -> Unit) {
        val name = _uiState.value.templateName.ifBlank { "My Budgetr" }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, step = OnboardingStep.LOADING) }
            try {
                val spreadsheetId = repository.createSpreadsheetTemplate(name)
                prefs.setSpreadsheetId(spreadsheetId)
                prefs.setSpreadsheetName(name)
                _uiState.update { it.copy(isLoading = false, step = OnboardingStep.DONE, createdSheetName = name) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, step = OnboardingStep.CHOOSE, error = "Failed to create sheet: ${e.message}")
                }
            }
        }
    }

    fun loadExistingSheets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSheets = true, showSheetPicker = true, error = null) }
            try {
                val sheets = repository.listSpreadsheets()
                _uiState.update { it.copy(availableSheets = sheets, isLoadingSheets = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingSheets = false,
                        showSheetPicker = false,
                        error = "Could not load sheets. Sign out and back in if this is your first time."
                    )
                }
            }
        }
    }

    fun selectExistingSheet(id: String, name: String, onComplete: () -> Unit) {
        prefs.setSpreadsheetId(id)
        prefs.setSpreadsheetName(name)
        _uiState.update { it.copy(showSheetPicker = false, step = OnboardingStep.DONE, createdSheetName = name) }
    }

    fun dismissSheetPicker() = _uiState.update { it.copy(showSheetPicker = false) }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
