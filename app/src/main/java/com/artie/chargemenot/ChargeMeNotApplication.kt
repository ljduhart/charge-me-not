package com.artie.chargemenot

import android.app.Application
import com.artie.chargemenot.data.local.AppDatabase
import com.artie.chargemenot.data.sensors.AndroidDeviceTiltSensor
import com.artie.chargemenot.data.nagmode.WorkManagerNagModeScheduler
import com.artie.chargemenot.data.notification.AndroidNotificationPermissionGateway
import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.data.repository.CategoryRepository
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.data.weedwhacker.WorkManagerWeedWhackerScheduler
import com.artie.chargemenot.ui.viewmodels.CategoryViewModel
import com.artie.chargemenot.domain.usecase.ForecastUseCase
import com.artie.chargemenot.notification.NagModeNotificationHelper
import com.artie.chargemenot.notification.WeedWhackerNotificationHelper
import com.artie.chargemenot.ui.dashboard.DashboardViewModel
import com.artie.chargemenot.ui.viewmodels.CompostBinViewModel
import com.artie.chargemenot.ui.viewmodels.PruningViewModel
import com.artie.chargemenot.ui.viewmodels.ScannerViewModel
import com.artie.chargemenot.ui.viewmodels.OnboardingViewModel
import com.artie.chargemenot.ui.viewmodels.SettingsViewModel
import com.artie.chargemenot.ui.viewmodels.WeedWhackerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ChargeMeNotApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database by lazy { AppDatabase.getInstance(this) }
    private val billRepository by lazy {
        BillRepository(
            billDao = database.billDao(),
            compostDao = database.compostDao()
        )
    }
    private val userSettingsRepository by lazy {
        UserSettingsRepository(database.userSettingsDao())
    }
    private val nagModeScheduler by lazy { WorkManagerNagModeScheduler(this) }
    private val weedWhackerScheduler by lazy { WorkManagerWeedWhackerScheduler(this) }
    private val notificationPermissionGateway by lazy {
        AndroidNotificationPermissionGateway(this)
    }

    private val forecastUseCase by lazy {
        ForecastUseCase(billDao = database.billDao())
    }

    private val categoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }

    val categoryViewModel: CategoryViewModel by lazy {
        CategoryViewModel(
            categoryRepository = categoryRepository,
            coroutineScope = applicationScope
        )
    }

    private val deviceTiltSensor by lazy {
        AndroidDeviceTiltSensor(applicationContext)
    }

    val dashboardViewModel: DashboardViewModel by lazy {
        DashboardViewModel(
            billRepository = billRepository,
            userSettingsRepository = userSettingsRepository,
            forecastUseCase = forecastUseCase,
            deviceTiltSensor = deviceTiltSensor,
            coroutineScope = applicationScope
        )
    }

    val scannerViewModel: ScannerViewModel by lazy {
        ScannerViewModel(
            billRepository = billRepository,
            userSettingsRepository = userSettingsRepository,
            coroutineScope = applicationScope
        )
    }

    val settingsViewModel: SettingsViewModel by lazy {
        SettingsViewModel(
            userSettingsRepository = userSettingsRepository,
            nagModeScheduler = nagModeScheduler,
            weedWhackerScheduler = weedWhackerScheduler,
            notificationPermissionGateway = notificationPermissionGateway,
            coroutineScope = applicationScope
        )
    }

    val pruningViewModel: PruningViewModel by lazy {
        PruningViewModel(
            billDao = database.billDao(),
            userSettingsRepository = userSettingsRepository,
            coroutineScope = applicationScope
        )
    }

    val weedWhackerViewModel: WeedWhackerViewModel by lazy {
        WeedWhackerViewModel(
            billDao = database.billDao(),
            coroutineScope = applicationScope
        )
    }

    val compostBinViewModel: CompostBinViewModel by lazy {
        CompostBinViewModel(
            billRepository = billRepository,
            coroutineScope = applicationScope
        )
    }

    val onboardingViewModel: OnboardingViewModel by lazy {
        OnboardingViewModel(
            userSettingsRepository = userSettingsRepository,
            nagModeScheduler = nagModeScheduler,
            notificationPermissionGateway = notificationPermissionGateway,
            coroutineScope = applicationScope
        )
    }

    override fun onCreate() {
        super.onCreate()
        NagModeNotificationHelper.createNotificationChannel(this)
        WeedWhackerNotificationHelper.createNotificationChannel(this)
        applicationScope.launch {
            userSettingsRepository.ensureDefaultSettingsIfNeeded()
            settingsViewModel.restoreNagModeWorkIfEnabled()
            settingsViewModel.restoreWeedWhackerWork()
        }
    }
}
