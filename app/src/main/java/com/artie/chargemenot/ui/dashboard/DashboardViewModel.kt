package com.artie.chargemenot.ui.dashboard

import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.domain.model.ForecastResult
import com.artie.chargemenot.domain.model.UserSettings
import com.artie.chargemenot.ui.components.BloomCategoryDefinitions
import com.artie.chargemenot.domain.usecase.ForecastUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class DashboardViewModel(
    private val billRepository: BillRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val forecastUseCase: ForecastUseCase,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _selectedBillForEdit = MutableStateFlow<Bill?>(null)
    val selectedBillForEdit: StateFlow<Bill?> = _selectedBillForEdit.asStateFlow()

    private val _selectedCategoryForEdit = MutableStateFlow<String?>(null)
    val selectedCategoryForEdit: StateFlow<String?> = _selectedCategoryForEdit.asStateFlow()

    val categoryBills: StateFlow<List<Bill>> = _selectedCategoryForEdit
        .flatMapLatest { categoryName ->
            if (categoryName == null) {
                flowOf(emptyList())
            } else {
                val categoryKey = BloomCategoryDefinitions.billCategoryNameFor(categoryName)
                if (categoryKey == null) {
                    flowOf(emptyList())
                } else {
                    billRepository.getBillsByCategory(categoryKey)
                }
            }
        }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

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
                userSettingsRepository.observeMonthlyBudget()
            ) { upcoming, all, monthlyBudget ->
                val subscriptions = all.filter { it.category == BillCategory.SUBSCRIPTIONS && !it.isPaid }
                val categoryTotals = upcoming
                    .groupBy { it.category }
                    .mapValues { (_, bills) -> bills.sumOf { bill -> bill.amount } }
                val forecast = forecastUseCase.calculateForecastFromDomainBills(
                    bills = all,
                    today = LocalDate.now()
                )

                DashboardUiState(
                    userDisplayName = _uiState.value.userDisplayName,
                    formattedDate = formatDisplayDate(LocalDate.now()),
                    greeting = resolveGreeting(),
                    totalUpcoming = upcoming.sumOf { it.amount },
                    upcomingBillCount = upcoming.size,
                    monthlyBudget = monthlyBudget,
                    upcomingBills = upcoming,
                    subscriptionBills = subscriptions,
                    allBills = all,
                    categoryTotals = categoryTotals,
                    forecastResult = forecast,
                    selectedBottomNavItem = _uiState.value.selectedBottomNavItem,
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

    fun selectBillForEdit(bill: Bill) {
        _selectedBillForEdit.value = bill
    }

    fun clearEditSelection() {
        _selectedBillForEdit.value = null
    }

    fun onPetalTapped(category: String) {
        _selectedCategoryForEdit.value = category
    }

    fun clearCategorySelection() {
        _selectedCategoryForEdit.value = null
    }

    fun saveBillEdits(updatedBill: Bill) {
        coroutineScope.launch(ioDispatcher) {
            billRepository.updateBill(updatedBill)
            clearEditSelection()
        }
    }

    fun selectBottomNavItem(item: DashboardBottomNavItem) {
        _uiState.update { current -> current.copy(selectedBottomNavItem = item) }
    }

    fun openBloomSettingsEdit() {
        val subscription = _uiState.value.subscriptionBills.firstOrNull()
            ?: _uiState.value.upcomingBills.firstOrNull()
        if (subscription != null) {
            selectBillForEdit(subscription)
        }
    }

    private fun formatDisplayDate(date: LocalDate): String {
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)
        val month = date.month.getDisplayName(TextStyle.FULL, Locale.US)
        return "($dayOfWeek, $month ${date.dayOfMonth}, ${date.year})"
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
