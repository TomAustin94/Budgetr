package com.budgetr.app.data.repository

import com.budgetr.app.data.model.AccountBalance
import com.budgetr.app.data.model.SheetTab
import com.budgetr.app.data.model.Transaction
import kotlinx.coroutines.flow.Flow

interface SheetsRepository {
    fun getTransactions(sheetTab: SheetTab): Flow<List<Transaction>>
    fun getAccountBalances(): Flow<List<AccountBalance>>
    suspend fun refreshTransactions(sheetTab: SheetTab)
    suspend fun refreshAccountBalances()
    suspend fun addTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
}
