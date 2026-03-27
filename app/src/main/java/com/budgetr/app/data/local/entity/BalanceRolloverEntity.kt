package com.budgetr.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "balance_rollovers")
data class BalanceRolloverEntity(
    @PrimaryKey val account: String,
    val rolloverAmount: Double,
    val recordedDate: String
)
