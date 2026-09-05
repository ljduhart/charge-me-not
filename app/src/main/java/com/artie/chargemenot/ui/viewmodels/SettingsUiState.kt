package com.artie.chargemenot.ui.viewmodels

data class SettingsUiState(
    val isNagModeEnabled: Boolean = false,
    val notificationPermissionGranted: Boolean = true,
    val showNotificationPermissionWarning: Boolean = false,
    val shouldRequestNotificationPermission: Boolean = false,
    val nagModePermissionBlocked: Boolean = false
)
