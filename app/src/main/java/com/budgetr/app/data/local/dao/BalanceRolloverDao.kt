package com.budgetr.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.budgetr.app.data.local.entity.BalanceRolloverEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BalanceRolloverDao {

    @Query("SELECT * FROM balance_rollovers")
    fun getAll(): Flow<List<BalanceRolloverEntity>>

    @Query("SELECT * FROM balance_rollovers")
    suspend fun getAllSync(): List<BalanceRolloverEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(rollover: BalanceRolloverEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rollovers: List<BalanceRolloverEntity>)

    @Query("DELETE FROM balance_rollovers WHERE account = :account")
    suspend fun delete(account: String)

    @Query("DELETE FROM balance_rollovers")
    suspend fun deleteAll()
}
