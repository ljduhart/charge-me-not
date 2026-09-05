package com.artie.chargemenot.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.artie.chargemenot.data.local.AppDatabase
import com.artie.chargemenot.notification.NagModeNotificationHelper
import java.time.LocalDate

class NagModeWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(applicationContext)
        val userSettingsDao = database.userSettingsDao()
        val billDao = database.billDao()

        val settings = userSettingsDao.getSettings()
        if (settings?.isNagModeEnabled != true) {
            return Result.success()
        }

        val unpaidCount = billDao.getOverdueOrDueTodayUnpaidBillCount(LocalDate.now())
        if (unpaidCount <= 0) {
            return Result.success()
        }

        NagModeNotificationHelper.showOverdueBillsNotification(
            context = applicationContext,
            unpaidCount = unpaidCount
        )

        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "nag_mode_periodic_work"
        const val WORK_TAG = "nag_mode_work"
    }
}
