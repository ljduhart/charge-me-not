package com.artie.chargemenot.domain.model

import java.util.Locale

/**
 * Markets Charge-me Not is legally and operationally restricted to.
 * Unknown persisted codes (legacy GBP/JPY/etc.) map to [USD].
 */
enum class SupportedCurrency(
    val code: String,
    val locale: Locale,
) {
    USD("USD", Locale.US),
    CAD("CAD", Locale.CANADA),
    MXN("MXN", Locale("es", "MX")),
    EUR("EUR", Locale.FRANCE),
    ;

    companion object {
        fun fromCode(code: String?): SupportedCurrency =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: USD
    }
}
