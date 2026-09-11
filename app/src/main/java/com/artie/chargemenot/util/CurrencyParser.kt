package com.artie.chargemenot.util

/**
 * Parses free-form amount text into database-safe minor units (cents).
 * Invalid, empty, or overflowing input returns `0L`.
 *
 * Decimal detection is locale-tolerant for the four supported markets:
 * the last `.` or `,` followed by 1–2 digits is the decimal separator, so
 * `15.99`, `15,99`, `1,450.00`, and `1.450,00` all parse correctly.
 */
object CurrencyParser {

    fun parseStringToCents(input: String): Long {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return 0L
        val negative = trimmed.startsWith('-') || trimmed.startsWith('(')
        val cleaned = buildString(trimmed.length) {
            trimmed.forEach { character ->
                if (character.isDigit() || character == '.' || character == ',') {
                    append(character)
                }
            }
        }
        if (cleaned.isEmpty() || cleaned.all { it == '.' || it == ',' }) return 0L

        val decimalIndex = resolveDecimalSeparatorIndex(cleaned)
        val majorDigits: String
        val fractionDigits: String
        if (decimalIndex >= 0) {
            majorDigits = cleaned.substring(0, decimalIndex).filter { it.isDigit() }
            fractionDigits = cleaned.substring(decimalIndex + 1).filter { it.isDigit() }
        } else {
            majorDigits = cleaned.filter { it.isDigit() }
            fractionDigits = ""
        }

        val major = majorDigits.toLongOrNull() ?: 0L
        val minor = when {
            fractionDigits.isEmpty() -> 0L
            fractionDigits.length == 1 -> (fractionDigits.toLongOrNull() ?: 0L) * 10L
            else -> fractionDigits.take(2).toLongOrNull() ?: 0L
        }
        val cents = try {
            Math.addExact(Math.multiplyExact(major, 100L), minor)
        } catch (_: ArithmeticException) {
            return 0L
        }
        return if (negative) -cents else cents
    }

    private fun resolveDecimalSeparatorIndex(cleaned: String): Int {
        val lastComma = cleaned.lastIndexOf(',')
        val lastDot = cleaned.lastIndexOf('.')
        val commaIsDecimal = isDecimalSeparator(cleaned, lastComma)
        val dotIsDecimal = isDecimalSeparator(cleaned, lastDot)
        return when {
            commaIsDecimal && dotIsDecimal -> maxOf(lastComma, lastDot)
            commaIsDecimal -> lastComma
            dotIsDecimal -> lastDot
            else -> -1
        }
    }

    private fun isDecimalSeparator(cleaned: String, separatorIndex: Int): Boolean {
        if (separatorIndex < 0) return false
        val fractionDigits = cleaned.substring(separatorIndex + 1).filter { it.isDigit() }
        return fractionDigits.length in 1..2
    }
}
