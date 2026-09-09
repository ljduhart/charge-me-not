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
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.MeadowCategories
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
import java.time.LocalDate

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
        seedInitialDataIfNeeded()
        settingsViewModel.restoreNagModeWorkIfEnabled()
        settingsViewModel.restoreWeedWhackerWork()
    }

    private fun seedInitialDataIfNeeded() {
        applicationScope.launch {
            userSettingsRepository.ensureDefaultSettingsIfNeeded()

            if (database.billDao().getBillCount() > 0) return@launch

            val today = LocalDate.now()
            val seedBills = listOf(
                Bill(name = "Pacific Gas & Electric", amount = 78.40, dueDate = today.minusMonths(2).withDayOfMonth(12), parentCategory = MeadowCategories.ROOT_SYSTEM, subCategory = "Utilities"),
                Bill(name = "Trader Joe's", amount = 142.18, dueDate = today.minusMonths(2).withDayOfMonth(18), parentCategory = MeadowCategories.FERTILIZER, subCategory = "Groceries"),
                Bill(name = "Pacific Gas & Electric", amount = 86.25, dueDate = today.minusMonths(1).withDayOfMonth(10), parentCategory = MeadowCategories.ROOT_SYSTEM, subCategory = "Utilities"),
                Bill(name = "Whole Foods Groceries", amount = 168.90, dueDate = today.minusMonths(1).withDayOfMonth(20), parentCategory = MeadowCategories.FERTILIZER, subCategory = "Groceries"),
                Bill(name = "Maple Street Apartment", amount = 1_450.00, dueDate = today.plusDays(3), parentCategory = MeadowCategories.CANOPY, subCategory = "Rent"),
                Bill(name = "Whole Foods Groceries", amount = 186.42, dueDate = today.plusDays(5), parentCategory = MeadowCategories.FERTILIZER, subCategory = "Groceries"),
                Bill(name = "Pacific Gas & Electric", amount = 94.17, dueDate = today.plusDays(8), parentCategory = MeadowCategories.ROOT_SYSTEM, subCategory = "Utilities"),
                Bill(name = "Spotify Premium", amount = 11.99, dueDate = today.plusDays(12), parentCategory = MeadowCategories.VINES, subCategory = "Subscriptions"),
                Bill(name = "Netflix", amount = 15.49, dueDate = today.plusDays(12), parentCategory = MeadowCategories.VINES, subCategory = "Subscriptions"),
                Bill(name = "Adobe Creative Cloud", amount = 54.99, dueDate = today.plusDays(15), parentCategory = MeadowCategories.VINES, subCategory = "Subscriptions"),
                Bill(name = "LA Metro Pass", amount = 100.00, dueDate = today.plusDays(18), parentCategory = MeadowCategories.ROOT_SYSTEM, subCategory = "Transportation"),
                Bill(name = "Kaiser Health", amount = 325.00, dueDate = today.plusDays(22), parentCategory = MeadowCategories.POLLINATORS, subCategory = "Healthcare"),
                Bill(name = "Trader Joe's", amount = 72.30, dueDate = today.plusDays(6), parentCategory = MeadowCategories.FERTILIZER, subCategory = "Groceries"),
                Bill(name = "Disney+", amount = 13.99, dueDate = today.plusDays(20), parentCategory = MeadowCategories.VINES, subCategory = "Subscriptions")
            )
            seedBills.forEach { billRepository.insertBill(it) }
        }
    }
}
