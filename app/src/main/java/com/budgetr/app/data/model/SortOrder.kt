package com.budgetr.app.data.model

enum class SortOrder(val displayName: String) {
    DATE_DESC("Date (Newest)"),
    DATE_ASC("Date (Oldest)"),
    AMOUNT_DESC("Amount (Highest)"),
    AMOUNT_ASC("Amount (Lowest)"),
    CATEGORY_ASC("Type (A-Z)")
}
