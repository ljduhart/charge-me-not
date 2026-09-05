package com.artie.chargemenot.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.artie.chargemenot.data.local.AppDatabase
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.notification.WeedWhackerNotificationHelper
import kotlinx.coroutines.flow.first

class WeedWhackerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(applicationContext)
        val billDao = database.billDao()

        val activeSubscriptions = billDao.getActiveSubscriptions().first()
            .filter { bill -> bill.category == BillCategory.SUBSCRIPTIONS && !bill.isPaid }

        if (activeSubscriptions.isEmpty()) {
            return Result.success()
        }

        WeedWhackerNotificationHelper.showGardenAuditNotification(applicationContext)
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "weed_whacker_periodic_work"
        const val WORK_TAG = "weed_whacker_work"
    }
}
