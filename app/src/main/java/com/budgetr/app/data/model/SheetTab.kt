package com.budgetr.app.data.model

enum class SheetTab(val displayName: String, val sheetName: String) {
    MONZO("Monzo", "Monzo Transactions"),
    HALIFAX("Halifax", "Halifax Transactions"),
    FOOD("Food / Spending", "Monzo Spending")
}
