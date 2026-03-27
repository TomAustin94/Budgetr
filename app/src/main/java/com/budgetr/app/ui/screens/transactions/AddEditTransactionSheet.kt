package com.budgetr.app.ui.screens.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.budgetr.app.data.model.SheetTab
import com.budgetr.app.data.model.Transaction
import com.budgetr.app.data.model.TransactionCategory
import com.budgetr.app.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Returns the pay date for the given [payDay] day-of-month.
 * - If today >= payDay: use payDay of this month.
 * - If today < payDay: use payDay of last month.
 * - If the resolved date falls on Saturday: move back 1 day to Friday.
 * - If it falls on Sunday: move back 2 days to Friday.
 */
internal fun getPayDate(payDay: Int = 26): String {
    val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.UK)
    val cal = Calendar.getInstance()
    val today = cal.get(Calendar.DAY_OF_MONTH)

    if (today < payDay) {
        cal.add(Calendar.MONTH, -1)
    }
    cal.set(Calendar.DAY_OF_MONTH, payDay)

    // Adjust if pay day lands on a weekend → move to preceding Friday
    when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SATURDAY -> cal.add(Calendar.DAY_OF_MONTH, -1)
        Calendar.SUNDAY -> cal.add(Calendar.DAY_OF_MONTH, -2)
    }

    return fmt.format(cal.time)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionSheet(
    existingTransaction: Transaction?,
    currentTab: SheetTab,
    addSaveCount: Int,
    payDay: Int = 26,
    onSave: (Transaction) -> Unit,
    onSaveTransfer: (source: Transaction, destination: Transaction) -> Unit,
    onDismiss: () -> Unit,
    presetCategory: TransactionCategory? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = SimpleDateFormat("dd/MM/yyyy", Locale.UK).format(Date())
    val isEdit = existingTransaction != null

    val initialCategory = existingTransaction?.category ?: presetCategory ?: TransactionCategory.ONE_OFF_COST
    var date by remember {
        mutableStateOf(
            existingTransaction?.date ?: if (initialCategory == TransactionCategory.FIXED_COST || initialCategory == TransactionCategory.SALARY) getPayDate(payDay) else today
        )
    }
    var info by remember { mutableStateOf(existingTransaction?.info ?: "") }
    var amount by remember {
        mutableStateOf(existingTransaction?.amount?.let { if (it < 0) (-it).toString() else it.toString() } ?: "")
    }
    var category by remember { mutableStateOf(initialCategory) }
    var selectedTab by remember { mutableStateOf(existingTransaction?.sheetTab ?: currentTab) }
    var transferToTab by remember { mutableStateOf<SheetTab?>(null) }
    var applyPayDate by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var tabExpanded by remember { mutableStateOf(false) }
    var transferToExpanded by remember { mutableStateOf(false) }
    var savedBanner by remember { mutableStateOf(false) }

    // Auto-set date based on category and applyPayDate toggle
    LaunchedEffect(category) {
        if (!isEdit) {
            when (category) {
                TransactionCategory.FIXED_COST,
                TransactionCategory.SALARY -> date = getPayDate(payDay)
                TransactionCategory.TRANSFER -> date = if (applyPayDate) getPayDate(payDay) else today
                else -> {
                    date = today
                    applyPayDate = false
                }
            }
        }
    }

    LaunchedEffect(applyPayDate) {
        if (!isEdit && category == TransactionCategory.TRANSFER) {
            date = if (applyPayDate) getPayDate(payDay) else today
        }
    }

    // Reset form after a successful add (addSaveCount increments each time)
    LaunchedEffect(addSaveCount) {
        if (addSaveCount > 0) {
            info = ""
            amount = ""
            transferToTab = null
            applyPayDate = false
            savedBanner = true
            date = if (category == TransactionCategory.FIXED_COST || category == TransactionCategory.SALARY) getPayDate(payDay) else today
        }
    }

    // Clear the "Saved!" banner after a short moment
    LaunchedEffect(savedBanner) {
        if (savedBanner) {
            kotlinx.coroutines.delay(2000)
            savedBanner = false
        }
    }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEdit) "Edit Transaction" else "Add Transaction",
                    style = MaterialTheme.typography.titleLarge
                )
                AnimatedVisibility(visible = savedBanner) {
                    Surface(
                        color = IncomeGreen.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Saved!",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = IncomeGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

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
                                    if (cat != TransactionCategory.TRANSFER) {
                                        transferToTab = null
                                        applyPayDate = false
                                    }
                                }
                            )
                        }
                }
            }

            // Account (source) dropdown
            ExposedDropdownMenuBox(
                expanded = tabExpanded,
                onExpandedChange = { tabExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedTab.displayName,
                    onValueChange = {},
                    label = { Text(if (category == TransactionCategory.TRANSFER) "Transfer From" else "Account") },
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
                                if (transferToTab == tab) transferToTab = null
                            }
                        )
                    }
                }
            }

            // Transfer destination — only shown for Transfer category
            AnimatedVisibility(visible = category == TransactionCategory.TRANSFER) {
                ExposedDropdownMenuBox(
                    expanded = transferToExpanded,
                    onExpandedChange = { transferToExpanded = it }
                ) {
                    OutlinedTextField(
                        value = transferToTab?.displayName ?: "Select destination account",
                        onValueChange = {},
                        label = { Text("Transfer To") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = transferToExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = transferToExpanded,
                        onDismissRequest = { transferToExpanded = false }
                    ) {
                        SheetTab.entries.filter { it != selectedTab }.forEach { tab ->
                            DropdownMenuItem(
                                text = { Text(tab.displayName) },
                                onClick = {
                                    transferToTab = tab
                                    transferToExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Optional pay-date toggle for transfers
            AnimatedVisibility(visible = category == TransactionCategory.TRANSFER) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Apply pay date",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Sets date to ${getPayDate(payDay)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = applyPayDate,
                        onCheckedChange = { applyPayDate = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val parsedAmount = amount.toDoubleOrNull() ?: 0.0
            val isTransfer = category == TransactionCategory.TRANSFER
            val saveEnabled = info.isNotBlank() && amount.isNotBlank() &&
                    (!isTransfer || isEdit || transferToTab != null)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) { Text(if (isEdit) "Cancel" else "Done") }

                Button(
                    onClick = {
                        val signedAmount = if (category == TransactionCategory.INCOME || category == TransactionCategory.SALARY) parsedAmount else -parsedAmount

                        if (isTransfer && !isEdit && transferToTab != null) {
                            val source = Transaction(
                                rowIndex = 0,
                                date = date,
                                info = info,
                                amount = -parsedAmount,
                                category = TransactionCategory.TRANSFER,
                                sheetTab = selectedTab
                            )
                            val destination = Transaction(
                                rowIndex = 0,
                                date = date,
                                info = info,
                                amount = parsedAmount,
                                category = TransactionCategory.TRANSFER,
                                sheetTab = transferToTab!!
                            )
                            onSaveTransfer(source, destination)
                        } else {
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
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = saveEnabled
                ) { Text("Save") }
            }
        }
    }
}
