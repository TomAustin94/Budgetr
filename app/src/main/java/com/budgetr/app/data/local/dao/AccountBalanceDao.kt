package com.budgetr.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.budgetr.app.data.local.entity.AccountBalanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountBalanceDao {
    @Query("SELECT * FROM account_balances")
    fun getAll(): Flow<List<AccountBalanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(balances: List<AccountBalanceEntity>)

    @Query("DELETE FROM account_balances")
    suspend fun deleteAll()
}
