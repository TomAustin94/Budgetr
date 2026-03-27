package com.budgetr.app.data.model

data class BalanceRollover(
    val account: String,
    val rolloverAmount: Double,
    val recordedDate: String
)
