package com.budgetr.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rowIndex: Int,
    val date: String,
    val info: String,
    val amount: Double,
    val category: String,
    val sheetTab: String
)
