package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.domain.model.UserSettings

data class OnboardingUiState(
    val budgetEnabled: Boolean = true,
    val budgetAmount: Float = UserSettings.DEFAULT_MONTHLY_BUDGET.toFloat(),
    val selectedCurrency: SupportedCurrency = SupportedCurrency.USD,
    val nagModeEnabled: Boolean = false,
    val isOnboardingComplete: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val shouldRequestNotificationPermission: Boolean = false,
    val notificationPermissionGranted: Boolean = false
)
