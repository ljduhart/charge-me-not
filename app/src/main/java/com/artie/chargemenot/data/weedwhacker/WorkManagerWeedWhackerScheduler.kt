package com.artie.chargemenot.data.weedwhacker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.artie.chargemenot.domain.repository.WeedWhackerScheduler
import com.artie.chargemenot.workers.WeedWhackerWorker
import java.util.concurrent.TimeUnit

class WorkManagerWeedWhackerScheduler(
    private val context: Context
) : WeedWhackerScheduler {

    override fun enablePeriodicAudits() {
        val workRequest = PeriodicWorkRequestBuilder<WeedWhackerWorker>(
            repeatInterval = 7,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .addTag(WeedWhackerWorker.WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WeedWhackerWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
