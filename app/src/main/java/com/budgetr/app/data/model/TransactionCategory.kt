package com.budgetr.app.data.model

enum class TransactionCategory(val displayName: String) {
    INCOME("Income"),
    SALARY("Salary"),
    FIXED_COST("Fixed Cost"),
    ONE_OFF_COST("One Off Cost"),
    TRANSFER("Transfer"),
    UNKNOWN("Unknown");

    companion object {
        fun fromString(value: String?): TransactionCategory =
            entries.firstOrNull { it.displayName.equals(value, ignoreCase = true) }
                ?: entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: UNKNOWN
    }
}
