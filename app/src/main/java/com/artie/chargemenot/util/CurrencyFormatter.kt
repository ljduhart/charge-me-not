package com.artie.chargemenot.util

import com.artie.chargemenot.domain.model.SupportedCurrency
import java.text.NumberFormat
import java.util.Currency

/**
 * Single source of truth for displaying minor-unit (cent) amounts.
 * Presentation only — never used for arithmetic.
 */
object CurrencyFormatter {

    fun format(amountInCents: Long, currency: SupportedCurrency): String {
        val formatter = NumberFormat.getCurrencyInstance(currency.locale)
        formatter.currency = Currency.getInstance(currency.code)
        return formatter.format(amountInCents / 100.0)
    }
}
