package com.artie.chargemenot.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.artie.chargemenot.data.local.AppDatabase
import com.artie.chargemenot.data.nagmode.WorkManagerNagModeScheduler
import com.artie.chargemenot.data.weedwhacker.WorkManagerWeedWhackerScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NagModeBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appContext = context.applicationContext
                val database = AppDatabase.getInstance(appContext)
                val settings = database.userSettingsDao().getSettings()
                if (settings?.isNagModeEnabled == true) {
                    WorkManagerNagModeScheduler(appContext).enableNagMode()
                }
                WorkManagerWeedWhackerScheduler(appContext).enablePeriodicAudits()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
