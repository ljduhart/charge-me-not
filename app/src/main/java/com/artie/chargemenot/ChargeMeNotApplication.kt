package com.artie.chargemenot

import android.app.Application
import com.artie.chargemenot.di.AppContainer
import com.artie.chargemenot.notification.NagModeNotificationHelper
import com.artie.chargemenot.notification.WeedWhackerNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ChargeMeNotApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NagModeNotificationHelper.createNotificationChannel(this)
        WeedWhackerNotificationHelper.createNotificationChannel(this)
        applicationScope.launch {
            container.userSettingsRepository.ensureDefaultSettingsIfNeeded()
            if (container.userSettingsRepository.getNagModeEnabled()) {
                container.nagModeScheduler.enableNagMode()
            }
            container.weedWhackerScheduler.enablePeriodicAudits()
        }
    }
}
