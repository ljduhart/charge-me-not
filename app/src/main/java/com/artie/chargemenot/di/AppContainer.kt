package com.artie.chargemenot.di

import android.content.Context
import com.artie.chargemenot.data.local.AppDatabase
import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.nagmode.WorkManagerNagModeScheduler
import com.artie.chargemenot.data.notification.AndroidNotificationPermissionGateway
import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.data.repository.CategoryRepository
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.data.sensors.AndroidDeviceTiltSensor
import com.artie.chargemenot.data.sensors.DeviceTiltSensor
import com.artie.chargemenot.data.weedwhacker.WorkManagerWeedWhackerScheduler
import com.artie.chargemenot.domain.repository.NagModeScheduler
import com.artie.chargemenot.domain.repository.NotificationPermissionGateway
import com.artie.chargemenot.domain.repository.WeedWhackerScheduler
import com.artie.chargemenot.domain.usecase.ForecastUseCase

/**
 * Manual composition root. Holds Room and the repositories/use cases ViewModels need.
 * ViewModels are never stored here — they are created by [AppViewModelProvider].
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AppDatabase = AppDatabase.getInstance(appContext)

    val billDao: BillDao = database.billDao()

    val billRepository: BillRepository = BillRepository(
        billDao = billDao,
        compostDao = database.compostDao()
    )

    val userSettingsRepository: UserSettingsRepository =
        UserSettingsRepository(database.userSettingsDao())

    val categoryRepository: CategoryRepository =
        CategoryRepository(database.categoryDao())

    val forecastUseCase: ForecastUseCase = ForecastUseCase(billDao = billDao)

    val nagModeScheduler: NagModeScheduler = WorkManagerNagModeScheduler(appContext)

    val weedWhackerScheduler: WeedWhackerScheduler =
        WorkManagerWeedWhackerScheduler(appContext)

    val notificationPermissionGateway: NotificationPermissionGateway =
        AndroidNotificationPermissionGateway(appContext)

    val deviceTiltSensor: DeviceTiltSensor = AndroidDeviceTiltSensor(appContext)
}
