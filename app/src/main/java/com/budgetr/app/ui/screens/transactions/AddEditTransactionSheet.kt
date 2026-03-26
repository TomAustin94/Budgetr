package com.budgetr.app.ui.screens.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.budgetr.app.data.model.SheetTab
import com.budgetr.app.data.model.Transaction
import com.budgetr.app.data.model.TransactionCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionSheet(
    existingTransaction: Transaction?,
    currentTab: SheetTab,
    onSave: (Transaction) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = SimpleDateFormat("dd/MM/yyyy", Locale.UK).format(Date())

    var date by remember { mutableStateOf(existingTransaction?.date ?: today) }
    var info by remember { mutableStateOf(existingTransaction?.info ?: "") }
    var amount by remember { mutableStateOf(existingTransaction?.amount?.let { if (it < 0) (-it).toString() else it.toString() } ?: "") }
    var category by remember { mutableStateOf(existingTransaction?.category ?: TransactionCategory.ONE_OFF_COST) }
    var selectedTab by remember { mutableStateOf(existingTransaction?.sheetTab ?: currentTab) }

    var showDatePicker by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var tabExpanded by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = SimpleDateFormat("dd/MM/yyyy", Locale.UK).format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (existingTransaction != null) "Edit Transaction" else "Add Transaction",
                style = MaterialTheme.typography.titleLarge
            )

            // Date field
            OutlinedTextField(
                value = date,
                onValueChange = {},
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) { Text("Pick") }
                }
            )

            // Description
            OutlinedTextField(
                value = info,
                onValueChange = { info = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (£)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = category.displayName,
                    onValueChange = {},
                    label = { Text("Category") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    TransactionCategory.entries
                        .filter { it != TransactionCategory.UNKNOWN }
                        .forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                }
            }

            // Account/sheet tab dropdown
            ExposedDropdownMenuBox(
                expanded = tabExpanded,
                onExpandedChange = { tabExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedTab.displayName,
                    onValueChange = {},
                    label = { Text("Account") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tabExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = tabExpanded,
                    onDismissRequest = { tabExpanded = false }
                ) {
                    SheetTab.entries.forEach { tab ->
                        DropdownMenuItem(
                            text = { Text(tab.displayName) },
                            onClick = {
                                selectedTab = tab
                                tabExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }

                Button(
                    onClick = {
                        val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                        val signedAmount = when (category) {
                            TransactionCategory.INCOME -> parsedAmount
                            else -> -parsedAmount
                        }
                        onSave(
                            Transaction(
                                rowIndex = existingTransaction?.rowIndex ?: 0,
                                date = date,
                                info = info,
                                amount = signedAmount,
                                category = category,
                                sheetTab = selectedTab
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = info.isNotBlank() && amount.isNotBlank()
                ) { Text("Save") }
            }
        }
    }
}
