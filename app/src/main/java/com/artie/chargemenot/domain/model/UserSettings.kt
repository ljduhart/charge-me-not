package com.artie.chargemenot.domain.model

data class UserSettings(
    val monthlyBudget: Double = DEFAULT_MONTHLY_BUDGET,
    val isNagModeEnabled: Boolean = false
) {
    companion object {
        const val DEFAULT_MONTHLY_BUDGET = 2_500.0
        const val MIN_MONTHLY_BUDGET = 1.0
    }
}
