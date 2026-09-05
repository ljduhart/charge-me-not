package com.artie.chargemenot.ui.dashboard

import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.domain.model.UserSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class DashboardViewModel(
    private val billRepository: BillRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeBills()
    }

    private fun observeBills() {
        coroutineScope.launch(ioDispatcher) {
            combine(
                billRepository.getUpcomingBills(),
                billRepository.getAllBills(),
                userSettingsRepository.observeMonthlyBudget()
            ) { upcoming, all, monthlyBudget ->
                val subscriptions = all.filter { it.category == BillCategory.SUBSCRIPTIONS && !it.isPaid }
                val categoryTotals = upcoming
                    .groupBy { it.category }
                    .mapValues { (_, bills) -> bills.sumOf { bill -> bill.amount } }

                DashboardUiState(
                    greeting = resolveGreeting(),
                    totalUpcoming = upcoming.sumOf { it.amount },
                    monthlyBudget = monthlyBudget,
                    upcomingBills = upcoming,
                    subscriptionBills = subscriptions,
                    categoryTotals = categoryTotals,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun keepSubscription(bill: Bill) {
        coroutineScope.launch(ioDispatcher) {
            billRepository.updateBill(bill.copy(isPaid = true))
        }
    }

    fun pullSubscription(bill: Bill) {
        coroutineScope.launch(ioDispatcher) {
            billRepository.deleteBill(bill)
        }
    }

    fun updateMonthlyBudget(rawBudgetInput: String) {
        val parsedBudget = rawBudgetInput
            .replace(",", "")
            .replace("$", "")
            .trim()
            .toDoubleOrNull() ?: return

        coroutineScope.launch(ioDispatcher) {
            userSettingsRepository.updateMonthlyBudget(parsedBudget)
        }
    }

    private fun resolveGreeting(): String {
        val hour = LocalTime.now().hour
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}
