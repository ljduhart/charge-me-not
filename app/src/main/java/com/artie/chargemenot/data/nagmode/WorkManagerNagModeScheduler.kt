package com.artie.chargemenot.data.nagmode

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.artie.chargemenot.domain.repository.NagModeScheduler
import com.artie.chargemenot.workers.NagModeWorker
import java.util.concurrent.TimeUnit

class WorkManagerNagModeScheduler(
    private val context: Context
) : NagModeScheduler {

    override fun enableNagMode() {
        val workRequest = PeriodicWorkRequestBuilder<NagModeWorker>(
            repeatInterval = 12,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .addTag(NagModeWorker.WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NagModeWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    override fun disableNagMode() {
        WorkManager.getInstance(context).cancelUniqueWork(NagModeWorker.UNIQUE_WORK_NAME)
    }
}
