package com.budgetr.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.budgetr.app.data.local.dao.AccountBalanceDao
import com.budgetr.app.data.local.dao.TransactionDao
import com.budgetr.app.data.local.entity.AccountBalanceEntity
import com.budgetr.app.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, AccountBalanceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BudgetrDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountBalanceDao(): AccountBalanceDao
}
