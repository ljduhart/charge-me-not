package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.domain.model.UserSettings
import com.artie.chargemenot.domain.repository.NagModeScheduler
import com.artie.chargemenot.domain.repository.NotificationPermissionGateway
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import kotlin.math.roundToLong

class OnboardingViewModel(
    private val userSettingsRepository: UserSettingsRepository,
    private val nagModeScheduler: NagModeScheduler,
    private val notificationPermissionGateway: NotificationPermissionGateway,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        observeOnboardingStatus()
    }

    private fun observeOnboardingStatus() {
        coroutineScope.launch(ioDispatcher) {
            userSettingsRepository.observeOnboardingComplete().collect { isComplete ->
                _uiState.update { current ->
                    current.copy(
                        isOnboardingComplete = isComplete,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setBudgetEnabled(enabled: Boolean) {
        _uiState.update { current -> current.copy(budgetEnabled = enabled) }
    }

    fun setBudgetAmount(amount: Float) {
        _uiState.update { current -> current.copy(budgetAmount = amount) }
    }

    fun selectCurrency(currency: SupportedCurrency) {
        _uiState.update { current -> current.copy(selectedCurrency = currency) }
    }

    fun requestWateringSchedule() {
        if (notificationPermissionGateway.requiresRuntimePermission() &&
            !notificationPermissionGateway.isNotificationPermissionGranted()
        ) {
            _uiState.update { current ->
                current.copy(shouldRequestNotificationPermission = true)
            }
            return
        }

        enableWateringSchedule(isGranted = true)
    }

    fun onNotificationPermissionResult(isGranted: Boolean) {
        _uiState.update { current ->
            current.copy(
                notificationPermissionGranted = isGranted,
                shouldRequestNotificationPermission = false
            )
        }
        enableWateringSchedule(isGranted = isGranted)
    }

    fun onNotificationPermissionRequestHandled() {
        _uiState.update { current ->
            current.copy(shouldRequestNotificationPermission = false)
        }
    }

    fun refreshNotificationPermissionState() {
        val isGranted = notificationPermissionGateway.isNotificationPermissionGranted()
        _uiState.update { current ->
            current.copy(notificationPermissionGranted = isGranted)
        }
    }

    fun saveOnboardingData(onComplete: () -> Unit) {
        if (_uiState.value.isSaving) {
            return
        }

        val state = _uiState.value
        val monthlyBudget = if (state.budgetEnabled) {
            (state.budgetAmount.toDouble() * 100.0).roundToLong()
                .coerceAtLeast(UserSettings.MIN_MONTHLY_BUDGET)
        } else {
            UserSettings.DEFAULT_MONTHLY_BUDGET
        }

        _uiState.update { current -> current.copy(isSaving = true) }

        coroutineScope.launch(ioDispatcher) {
            try {
                userSettingsRepository.saveOnboardingPreferences(
                    monthlyBudget = monthlyBudget,
                    selectedCurrency = state.selectedCurrency.code,
                    isNagModeEnabled = state.nagModeEnabled
                )
                if (state.nagModeEnabled) {
                    nagModeScheduler.enableNagMode()
                } else {
                    nagModeScheduler.disableNagMode()
                }
                _uiState.update { current ->
                    current.copy(isOnboardingComplete = true)
                }
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } finally {
                _uiState.update { current -> current.copy(isSaving = false) }
            }
        }
    }

    private fun enableWateringSchedule(isGranted: Boolean) {
        _uiState.update { current ->
            current.copy(nagModeEnabled = isGranted)
        }
    }
}
