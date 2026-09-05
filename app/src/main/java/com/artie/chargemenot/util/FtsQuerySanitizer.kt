package com.artie.chargemenot.util

object FtsQuerySanitizer {

    fun sanitize(rawQuery: String): String {
        val tokens = rawQuery
            .trim()
            .split(Regex("\\s+"))
            .filter { token -> token.isNotBlank() }

        if (tokens.isEmpty()) {
            return ""
        }

        return tokens.joinToString(separator = " ") { token ->
            "\"${token.replace("\"", "\"\"")}\""
        }
    }
}
