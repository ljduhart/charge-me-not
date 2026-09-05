package com.artie.chargemenot

import android.app.Application
import com.artie.chargemenot.data.local.AppDatabase
import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.ui.dashboard.DashboardViewModel
import com.artie.chargemenot.ui.viewmodels.ScannerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

class ChargeMeNotApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database by lazy { AppDatabase.getInstance(this) }
    private val billRepository by lazy { BillRepository(database.billDao()) }

    val dashboardViewModel: DashboardViewModel by lazy {
        DashboardViewModel(
            billRepository = billRepository,
            coroutineScope = applicationScope
        )
    }

    val scannerViewModel: ScannerViewModel by lazy {
        ScannerViewModel(
            billRepository = billRepository,
            coroutineScope = applicationScope
        )
    }

    override fun onCreate() {
        super.onCreate()
        seedInitialDataIfNeeded()
    }

    private fun seedInitialDataIfNeeded() {
        applicationScope.launch {
            if (database.billDao().getBillCount() > 0) return@launch

            val today = LocalDate.now()
            val seedBills = listOf(
                Bill(name = "Maple Street Apartment", amount = 1_450.00, dueDate = today.plusDays(3), category = BillCategory.RENT),
                Bill(name = "Whole Foods Groceries", amount = 186.42, dueDate = today.plusDays(5), category = BillCategory.FOOD),
                Bill(name = "Pacific Gas & Electric", amount = 94.17, dueDate = today.plusDays(8), category = BillCategory.UTILITIES),
                Bill(name = "Spotify Premium", amount = 11.99, dueDate = today.plusDays(12), category = BillCategory.SUBSCRIPTIONS),
                Bill(name = "Netflix", amount = 15.49, dueDate = today.plusDays(12), category = BillCategory.SUBSCRIPTIONS),
                Bill(name = "Adobe Creative Cloud", amount = 54.99, dueDate = today.plusDays(15), category = BillCategory.SUBSCRIPTIONS),
                Bill(name = "LA Metro Pass", amount = 100.00, dueDate = today.plusDays(18), category = BillCategory.TRANSPORTATION),
                Bill(name = "Kaiser Health", amount = 325.00, dueDate = today.plusDays(22), category = BillCategory.HEALTHCARE),
                Bill(name = "Trader Joe's", amount = 72.30, dueDate = today.plusDays(6), category = BillCategory.FOOD),
                Bill(name = "Disney+", amount = 13.99, dueDate = today.plusDays(20), category = BillCategory.SUBSCRIPTIONS)
            )
            seedBills.forEach { billRepository.insertBill(it) }
        }
    }
}
