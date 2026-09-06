package com.artie.chargemenot.domain.model

enum class SupportedCurrency(
    val code: String,
    val displayName: String,
    val symbol: String
) {
    USD(code = "USD", displayName = "US Dollar", symbol = "$"),
    EUR(code = "EUR", displayName = "Euro", symbol = "€"),
    GBP(code = "GBP", displayName = "British Pound", symbol = "£"),
    CAD(code = "CAD", displayName = "Canadian Dollar", symbol = "CA$"),
    AUD(code = "AUD", displayName = "Australian Dollar", symbol = "A$"),
    JPY(code = "JPY", displayName = "Japanese Yen", symbol = "¥"),
    CHF(code = "CHF", displayName = "Swiss Franc", symbol = "CHF"),
    INR(code = "INR", displayName = "Indian Rupee", symbol = "₹");

    companion object {
        fun fromCode(code: String): SupportedCurrency {
            return entries.firstOrNull { currency -> currency.code == code } ?: USD
        }
    }
}
