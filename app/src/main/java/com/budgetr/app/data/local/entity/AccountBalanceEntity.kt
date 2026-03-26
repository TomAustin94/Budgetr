package com.budgetr.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_balances")
data class AccountBalanceEntity(
    @PrimaryKey val account: String,
    val remainingBalance: Double,
    val itemCostThisMonth: Double?,
    val subscriptionCost: Double?,
    val variance: Double?,
    val shouldBuySub: String?
)
