package com.artie.chargemenot.domain.model

data class UserSettings(
    val monthlyBudget: Long = DEFAULT_MONTHLY_BUDGET,
    val isNagModeEnabled: Boolean = false,
    val selectedCurrency: String = DEFAULT_CURRENCY,
    val isOnboardingComplete: Boolean = false,
    val displayName: String = DEFAULT_DISPLAY_NAME
) {
    companion object {
        const val DEFAULT_MONTHLY_BUDGET = 250_000L
        const val MIN_MONTHLY_BUDGET = 100L
        const val DEFAULT_CURRENCY = "USD"
        const val DEFAULT_DISPLAY_NAME = "Sarah"
    }
}
