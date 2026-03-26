package com.budgetr.app.util

import java.text.NumberFormat
import java.util.Locale

fun Double.toCurrencyString(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.UK)
    return format.format(this)
}

fun Double.toSignedCurrencyString(): String {
    val formatted = toCurrencyString()
    return if (this >= 0) "+$formatted" else formatted
}
