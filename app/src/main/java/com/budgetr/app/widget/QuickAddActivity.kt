package com.budgetr.app.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetr.app.data.model.SheetTab
import com.budgetr.app.data.model.TransactionCategory
import com.budgetr.app.ui.screens.transactions.AddEditTransactionSheet
import com.budgetr.app.ui.screens.transactions.TransactionsViewModel
import com.budgetr.app.ui.theme.BudgetrTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Lightweight transparent activity launched from the home screen widget.
 * Displays the add transaction bottom sheet as an overlay over the launcher.
 */
@AndroidEntryPoint
class QuickAddActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CATEGORY = "quick_add_category"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val presetCategory = intent.getStringExtra(EXTRA_CATEGORY)
            ?.let { runCatching { TransactionCategory.valueOf(it) }.getOrNull() }
            ?: TransactionCategory.ONE_OFF_COST

        setContent {
            BudgetrTheme {
                val viewModel: TransactionsViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Pre-open the add sheet with the preset category on first composition
                LaunchedEffect(Unit) {
                    viewModel.showAddSheet()
                }

                // Close activity once a transaction has been saved
                LaunchedEffect(uiState.addSaveCount) {
                    if (uiState.addSaveCount > 0) finish()
                }

                if (uiState.showAddSheet) {
                    AddEditTransactionSheet(
                        existingTransaction = null,
                        currentTab = SheetTab.MONZO,
                        addSaveCount = uiState.addSaveCount,
                        onSave = viewModel::saveTransaction,
                        onSaveTransfer = viewModel::saveTransfer,
                        onDismiss = { finish() },
                        presetCategory = presetCategory
                    )
                }
            }
        }
    }
}
