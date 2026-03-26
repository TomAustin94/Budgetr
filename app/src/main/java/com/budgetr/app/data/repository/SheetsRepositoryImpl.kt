package com.budgetr.app.data.repository

import com.budgetr.app.data.api.BatchUpdateRequest
import com.budgetr.app.data.api.DeleteDimensionRequest
import com.budgetr.app.data.api.DimensionRange
import com.budgetr.app.data.api.GoogleSheetsApi
import com.budgetr.app.data.api.Request
import com.budgetr.app.data.api.ValueRange
import com.budgetr.app.data.local.dao.AccountBalanceDao
import com.budgetr.app.data.local.dao.TransactionDao
import com.budgetr.app.data.local.entity.AccountBalanceEntity
import com.budgetr.app.data.local.entity.TransactionEntity
import com.budgetr.app.data.model.AccountBalance
import com.budgetr.app.data.model.SheetTab
import com.budgetr.app.data.model.Transaction
import com.budgetr.app.data.model.TransactionCategory
import com.budgetr.app.util.PreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SheetsRepositoryImpl @Inject constructor(
    private val api: GoogleSheetsApi,
    private val transactionDao: TransactionDao,
    private val accountBalanceDao: AccountBalanceDao,
    private val prefs: PreferencesManager
) : SheetsRepository {

    // Cache of sheet title -> numeric sheetId (gid) from the Sheets API
    private var sheetIdCache: Map<String, Int> = emptyMap()

    private suspend fun resolveSheetId(sheetTab: SheetTab): Int {
        sheetIdCache[sheetTab.sheetName]?.let { return it }
        val spreadsheetId = prefs.getSpreadsheetId() ?: return 0
        val metadata = api.getSpreadsheet(spreadsheetId)
        sheetIdCache = metadata.sheets
            ?.mapNotNull { it.properties }
            ?.associate { it.title to it.sheetId }
            ?: emptyMap()
        return sheetIdCache[sheetTab.sheetName] ?: 0
    }

    override fun getTransactions(sheetTab: SheetTab): Flow<List<Transaction>> =
        transactionDao.getTransactionsByTab(sheetTab.name).map { entities ->
            entities.map { it.toTransaction() }
        }

    override fun getAccountBalances(): Flow<List<AccountBalance>> =
        accountBalanceDao.getAll().map { entities ->
            entities.map { it.toAccountBalance() }
        }

    override suspend fun refreshTransactions(sheetTab: SheetTab) {
        val spreadsheetId = prefs.getSpreadsheetId() ?: return
        val range = "${sheetTab.sheetName}!A:E"
        val response = api.getValues(spreadsheetId, range)
        val rows = response.values ?: return

        // First row is header — skip it
        val entities = rows.drop(1).mapIndexedNotNull { index, row ->
            if (row.isEmpty()) return@mapIndexedNotNull null
            TransactionEntity(
                rowIndex = index + 2, // +2: skip header, 1-based
                date = row.getOrElse(0) { "" },
                info = row.getOrElse(1) { "" },
                amount = row.getOrElse(2) { "0" }.replace("[£,]".toRegex(), "").toDoubleOrNull() ?: 0.0,
                category = TransactionCategory.fromString(row.getOrElse(3) { "" }).name,
                sheetTab = sheetTab.name
            )
        }

        transactionDao.deleteByTab(sheetTab.name)
        transactionDao.insertAll(entities)
    }

    override suspend fun refreshAccountBalances() {
        val spreadsheetId = prefs.getSpreadsheetId() ?: return
        val range = "Cover Sheet!A:F"
        val response = api.getValues(spreadsheetId, range)
        val rows = response.values ?: return

        val entities = rows.drop(1).mapNotNull { row ->
            if (row.isEmpty() || row.getOrElse(0) { "" }.isBlank()) return@mapNotNull null
            AccountBalanceEntity(
                account = row.getOrElse(0) { "" },
                remainingBalance = row.getOrElse(1) { "0" }.parseCurrency(),
                itemCostThisMonth = row.getOrElse(2) { "" }.parseCurrencyOrNull(),
                subscriptionCost = row.getOrElse(3) { "" }.parseCurrencyOrNull(),
                variance = row.getOrElse(4) { "" }.parseCurrencyOrNull(),
                shouldBuySub = row.getOrElse(5) { "" }.ifBlank { null }
            )
        }

        accountBalanceDao.deleteAll()
        accountBalanceDao.insertAll(entities)
    }

    override suspend fun addTransaction(transaction: Transaction) {
        val spreadsheetId = prefs.getSpreadsheetId() ?: return
        val range = "${transaction.sheetTab.sheetName}!A:D"
        val row = listOf(
            listOf(
                transaction.date,
                transaction.info,
                transaction.amount.toString(),
                transaction.category.displayName
            )
        )
        api.appendValues(spreadsheetId, range, body = ValueRange(values = row))
        refreshTransactions(transaction.sheetTab)
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        val spreadsheetId = prefs.getSpreadsheetId() ?: return
        val range = "${transaction.sheetTab.sheetName}!A${transaction.rowIndex}:D${transaction.rowIndex}"
        val row = listOf(
            listOf(
                transaction.date,
                transaction.info,
                transaction.amount.toString(),
                transaction.category.displayName
            )
        )
        api.updateValues(spreadsheetId, range, body = ValueRange(values = row))
        refreshTransactions(transaction.sheetTab)
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        val spreadsheetId = prefs.getSpreadsheetId() ?: return
        val sheetId = resolveSheetId(transaction.sheetTab)
        val rowIndex = transaction.rowIndex - 1 // 0-based for API
        api.batchUpdate(
            spreadsheetId,
            BatchUpdateRequest(
                requests = listOf(
                    Request(
                        deleteDimension = DeleteDimensionRequest(
                            range = DimensionRange(
                                sheetId = sheetId,
                                startIndex = rowIndex,
                                endIndex = rowIndex + 1
                            )
                        )
                    )
                )
            )
        )
        refreshTransactions(transaction.sheetTab)
    }

    private fun TransactionEntity.toTransaction() = Transaction(
        rowIndex = rowIndex,
        date = date,
        info = info,
        amount = amount,
        category = TransactionCategory.fromString(category),
        sheetTab = SheetTab.valueOf(sheetTab)
    )

    private fun AccountBalanceEntity.toAccountBalance() = AccountBalance(
        account = account,
        remainingBalance = remainingBalance,
        itemCostThisMonth = itemCostThisMonth,
        subscriptionCost = subscriptionCost,
        variance = variance,
        shouldBuySub = shouldBuySub
    )

    private fun String.parseCurrency(): Double =
        replace("[£,\\s]".toRegex(), "").toDoubleOrNull() ?: 0.0

    private fun String.parseCurrencyOrNull(): Double? =
        ifBlank { null }?.replace("[£,\\s]".toRegex(), "")?.toDoubleOrNull()
}
