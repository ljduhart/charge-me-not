package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.repository.NagModeScheduler
import com.artie.chargemenot.domain.repository.NotificationPermissionGateway
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userSettingsRepository: UserSettingsRepository,
    private val nagModeScheduler: NagModeScheduler,
    private val notificationPermissionGateway: NotificationPermissionGateway,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val permissionGrantedState = MutableStateFlow(
        notificationPermissionGateway.isNotificationPermissionGranted()
    )

    private val nagModePermissionBlockedState = MutableStateFlow(false)

    private val shouldRequestPermissionState = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(buildUiState(isNagModeEnabled = false))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        coroutineScope.launch(ioDispatcher) {
            combine(
                userSettingsRepository.observeNagModeEnabled(),
                permissionGrantedState,
                nagModePermissionBlockedState,
                shouldRequestPermissionState
            ) { isNagModeEnabled, permissionGranted, permissionBlocked, shouldRequestPermission ->
                buildUiState(
                    isNagModeEnabled = isNagModeEnabled,
                    permissionGranted = permissionGranted,
                    nagModePermissionBlocked = permissionBlocked,
                    shouldRequestNotificationPermission = shouldRequestPermission
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun refreshNotificationPermissionState() {
        permissionGrantedState.value =
            notificationPermissionGateway.isNotificationPermissionGranted()
    }

    fun onNagModeToggleRequested(isEnabled: Boolean) {
        if (!isEnabled) {
            nagModePermissionBlockedState.value = false
            shouldRequestPermissionState.value = false
            toggleNagMode(isEnabled = false)
            return
        }

        if (notificationPermissionGateway.requiresRuntimePermission() &&
            !notificationPermissionGateway.isNotificationPermissionGranted()
        ) {
            nagModePermissionBlockedState.value = true
            shouldRequestPermissionState.value = true
            return
        }

        nagModePermissionBlockedState.value = false
        shouldRequestPermissionState.value = false
        toggleNagMode(isEnabled = true)
    }

    fun onNotificationPermissionResult(isGranted: Boolean) {
        permissionGrantedState.value = isGranted
        shouldRequestPermissionState.value = false

        if (isGranted) {
            nagModePermissionBlockedState.value = false
            toggleNagMode(isEnabled = true)
            return
        }

        nagModePermissionBlockedState.value = true
    }

    fun onNotificationPermissionRequestHandled() {
        shouldRequestPermissionState.value = false
    }

    fun toggleNagMode(isEnabled: Boolean) {
        coroutineScope.launch(ioDispatcher) {
            userSettingsRepository.updateNagModeEnabled(isEnabled)
            if (isEnabled) {
                nagModeScheduler.enableNagMode()
            } else {
                nagModeScheduler.disableNagMode()
            }
            nagModePermissionBlockedState.value = false
        }
    }

    fun restoreNagModeWorkIfEnabled() {
        coroutineScope.launch(ioDispatcher) {
            if (userSettingsRepository.getNagModeEnabled()) {
                nagModeScheduler.enableNagMode()
            }
        }
    }

    private fun buildUiState(
        isNagModeEnabled: Boolean,
        permissionGranted: Boolean = notificationPermissionGateway.isNotificationPermissionGranted(),
        nagModePermissionBlocked: Boolean = false,
        shouldRequestNotificationPermission: Boolean = false
    ): SettingsUiState {
        val requiresPermission = notificationPermissionGateway.requiresRuntimePermission()
        val showWarning = requiresPermission && !permissionGranted &&
            (isNagModeEnabled || nagModePermissionBlocked)

        return SettingsUiState(
            isNagModeEnabled = isNagModeEnabled,
            notificationPermissionGranted = permissionGranted,
            showNotificationPermissionWarning = showWarning,
            shouldRequestNotificationPermission = shouldRequestNotificationPermission,
            nagModePermissionBlocked = nagModePermissionBlocked
        )
    }
}
