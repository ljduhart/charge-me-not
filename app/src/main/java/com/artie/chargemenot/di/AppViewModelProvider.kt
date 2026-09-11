package com.artie.chargemenot.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.artie.chargemenot.ChargeMeNotApplication
import com.artie.chargemenot.ui.dashboard.DashboardViewModel
import com.artie.chargemenot.ui.viewmodels.CategoryViewModel
import com.artie.chargemenot.ui.viewmodels.CompostBinViewModel
import com.artie.chargemenot.ui.viewmodels.OnboardingViewModel
import com.artie.chargemenot.ui.viewmodels.PruningViewModel
import com.artie.chargemenot.ui.viewmodels.ScannerViewModel
import com.artie.chargemenot.ui.viewmodels.SettingsViewModel
import com.artie.chargemenot.ui.viewmodels.WeedWhackerViewModel

object AppViewModelProvider {

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val container = appContainer()
            DashboardViewModel(
                billRepository = container.billRepository,
                userSettingsRepository = container.userSettingsRepository,
                forecastUseCase = container.forecastUseCase,
                deviceTiltSensor = container.deviceTiltSensor
            )
        }
        initializer {
            val container = appContainer()
            ScannerViewModel(
                billRepository = container.billRepository,
                userSettingsRepository = container.userSettingsRepository
            )
        }
        initializer {
            val container = appContainer()
            SettingsViewModel(
                userSettingsRepository = container.userSettingsRepository,
                nagModeScheduler = container.nagModeScheduler,
                weedWhackerScheduler = container.weedWhackerScheduler,
                notificationPermissionGateway = container.notificationPermissionGateway
            )
        }
        initializer {
            val container = appContainer()
            PruningViewModel(
                billDao = container.billDao,
                userSettingsRepository = container.userSettingsRepository
            )
        }
        initializer {
            val container = appContainer()
            WeedWhackerViewModel(
                billDao = container.billDao,
                userSettingsRepository = container.userSettingsRepository
            )
        }
        initializer {
            val container = appContainer()
            CompostBinViewModel(billRepository = container.billRepository)
        }
        initializer {
            val container = appContainer()
            CategoryViewModel(categoryRepository = container.categoryRepository)
        }
        initializer {
            val container = appContainer()
            OnboardingViewModel(
                userSettingsRepository = container.userSettingsRepository,
                nagModeScheduler = container.nagModeScheduler,
                notificationPermissionGateway = container.notificationPermissionGateway
            )
        }
    }
}

private fun CreationExtras.appContainer(): AppContainer {
    val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
        as ChargeMeNotApplication
    return application.container
}
