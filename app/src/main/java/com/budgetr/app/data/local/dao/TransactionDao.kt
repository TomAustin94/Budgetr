package com.budgetr.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.budgetr.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE sheetTab = :sheetTab ORDER BY rowIndex DESC")
    fun getTransactionsByTab(sheetTab: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE sheetTab = :sheetTab ORDER BY rowIndex DESC")
    suspend fun getTransactionsByTabSync(sheetTab: String): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE sheetTab = :sheetTab")
    suspend fun deleteByTab(sheetTab: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
