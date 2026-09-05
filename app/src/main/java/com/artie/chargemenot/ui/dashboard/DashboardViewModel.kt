package com.artie.chargemenot.ui.dashboard

import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.domain.model.ForecastResult
import com.artie.chargemenot.domain.model.UserSettings
import com.artie.chargemenot.domain.usecase.ForecastUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class DashboardViewModel(
    private val billRepository: BillRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val forecastUseCase: ForecastUseCase,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val forecastResult: StateFlow<ForecastResult?> = uiState
        .map { state -> state.forecastResult }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    init {
        observeDashboard()
    }

    private fun observeDashboard() {
        coroutineScope.launch(ioDispatcher) {
            combine(
                billRepository.getUpcomingBills(),
                billRepository.getAllBills(),
                userSettingsRepository.observeMonthlyBudget(),
                forecastUseCase.observeForecast()
            ) { upcoming, all, monthlyBudget, forecast ->
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
                    allBills = all,
                    categoryTotals = categoryTotals,
                    forecastResult = forecast,
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

    fun linkBillToParent(childBillId: Long, parentBillId: Long?) {
        coroutineScope.launch(ioDispatcher) {
            billRepository.linkBillToParent(childBillId, parentBillId)
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
