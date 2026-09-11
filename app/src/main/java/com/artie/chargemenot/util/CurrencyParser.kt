package com.artie.chargemenot.util

/**
 * Parses free-form amount text into database-safe minor units (cents).
 * Invalid or empty input returns `0L`.
 */
object CurrencyParser {

    fun parseStringToCents(input: String): Long {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return 0L
        val negative = trimmed.startsWith('-') || trimmed.startsWith('(')
        val cleaned = trimmed.filter { it.isDigit() || it == '.' }
        if (cleaned.isEmpty() || cleaned.all { it == '.' }) return 0L
        val parts = cleaned.split('.', limit = 2)
        val major = parts[0].filter { it.isDigit() }.toLongOrNull() ?: 0L
        val fractionDigits = parts.getOrNull(1)?.filter { it.isDigit() }.orEmpty()
        val minor = when {
            fractionDigits.isEmpty() -> 0L
            fractionDigits.length == 1 -> (fractionDigits.toLongOrNull() ?: 0L) * 10L
            else -> fractionDigits.take(2).toLongOrNull() ?: 0L
        }
        val cents = major * 100L + minor
        return if (negative) -cents else cents
    }
}
