package com.budgetr.app.data.model

data class AccountBalance(
    val account: String,
    val remainingBalance: Double,
    val itemCostThisMonth: Double?,
    val subscriptionCost: Double?,
    val variance: Double?,
    val shouldBuySub: String?
)
