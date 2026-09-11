package com.artie.chargemenot.ui.dashboard

import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.data.sensors.DeviceTiltSensor
import com.artie.chargemenot.data.sensors.StationaryDeviceTiltSensor
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.ForecastResult
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.domain.model.UserSettings
import com.artie.chargemenot.ui.components.BloomCategoryDefinitions
import com.artie.chargemenot.ui.components.sortGardenPathBills
import com.artie.chargemenot.domain.usecase.ForecastUseCase
import com.artie.chargemenot.util.CurrencyParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class DashboardViewModel(
    private val billRepository: BillRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val forecastUseCase: ForecastUseCase,
    private val deviceTiltSensor: DeviceTiltSensor = StationaryDeviceTiltSensor(),
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _parallaxOffset = MutableStateFlow(0f to 0f)
    val parallaxOffset: StateFlow<Pair<Float, Float>> = _parallaxOffset.asStateFlow()

    private var parallaxSensorJob: Job? = null

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _selectedBillForEdit = MutableStateFlow<Bill?>(null)
    val selectedBillForEdit: StateFlow<Bill?> = _selectedBillForEdit.asStateFlow()

    private val _selectedCategoryForEdit = MutableStateFlow<String?>(null)
    val selectedCategoryForEdit: StateFlow<String?> = _selectedCategoryForEdit.asStateFlow()

    private val _isProfileEditVisible = MutableStateFlow(false)
    val isProfileEditVisible: StateFlow<Boolean> = _isProfileEditVisible.asStateFlow()

    private val _isManualBillVisible = MutableStateFlow(false)
    val isManualBillVisible: StateFlow<Boolean> = _isManualBillVisible.asStateFlow()

    private val _manualBillEntrySession = MutableStateFlow(0)
    val manualBillEntrySession: StateFlow<Int> = _manualBillEntrySession.asStateFlow()

    private val _manualBillPrefillDate = MutableStateFlow<LocalDate?>(null)
    val manualBillPrefillDate: StateFlow<LocalDate?> = _manualBillPrefillDate.asStateFlow()

    val categoryBills: StateFlow<List<Bill>> = _selectedCategoryForEdit
        .flatMapLatest { categoryName ->
            if (categoryName == null) {
                flowOf(emptyList())
            } else {
                val categoryKey = BloomCategoryDefinitions.parentNameFor(categoryName)
                if (categoryKey == null) {
                    flowOf(emptyList())
                } else {
                    billRepository.getBillsByParentCategory(categoryKey)
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
                userSettingsRepository.observeUserSettings()
            ) { upcoming, all, settings ->
                val today = LocalDate.now()
                val subscriptions = all.filter {
                    it.parentCategory == MeadowCategories.VINES && !it.isPaid
                }
                val parentCategoryTotals = upcoming
                    .groupBy { it.parentCategory }
                    .mapValues { (_, bills) -> bills.sumOf { bill -> bill.amount } }
                val forecast = forecastUseCase.calculateForecastFromDomainBills(
                    bills = all,
                    today = LocalDate.now()
                )

                val billsByDueDate = all
                    .filter { bill -> !bill.isPaid }
                    .groupBy { bill -> bill.dueDate }

                DashboardUiState(
                    userDisplayName = settings.displayName,
                    formattedDate = formatDisplayDate(LocalDate.now()),
                    greeting = resolveGreeting(),
                    totalUpcoming = upcoming.sumOf { it.amount },
                    upcomingBillCount = upcoming.size,
                    monthlyBudget = settings.monthlyBudget,
                    upcomingBills = upcoming,
                    subscriptionBills = subscriptions,
                    allBills = all,
                    gardenPathBills = sortGardenPathBills(all),
                    parentCategoryTotals = parentCategoryTotals,
                    forecastResult = forecast,
                    highlightedBloomParent = _uiState.value.highlightedBloomParent,
                    selectedCurrency = SupportedCurrency.fromCode(settings.selectedCurrency),
                    isBillCalendarExpanded = _uiState.value.isBillCalendarExpanded,
                    calendarVisibleMonth = _uiState.value.calendarVisibleMonth,
                    billsByDueDate = billsByDueDate,
                    selectedCalendarDate = _uiState.value.selectedCalendarDate,
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
        deleteBill(bill)
    }

    fun deleteBill(bill: Bill) {
        if (_selectedBillForEdit.value?.id == bill.id) {
            _selectedBillForEdit.value = null
        }
        coroutineScope.launch(ioDispatcher) {
            billRepository.deleteBill(bill)
        }
    }

    fun linkBillToParent(childBillId: Long, parentBillId: Long?) {
        coroutineScope.launch(ioDispatcher) {
            billRepository.linkBillToParent(childBillId, parentBillId)
        }
    }

    fun updateMonthlyBudget(rawBudgetInput: String): Boolean {
        val parsedBudget = CurrencyParser.parseStringToCents(rawBudgetInput)
        if (parsedBudget < UserSettings.MIN_MONTHLY_BUDGET) {
            return false
        }

        coroutineScope.launch(ioDispatcher) {
            userSettingsRepository.updateMonthlyBudget(parsedBudget)
        }
        return true
    }

    fun updateDisplayName(displayName: String) {
        coroutineScope.launch(ioDispatcher) {
            userSettingsRepository.updateDisplayName(displayName)
            _isProfileEditVisible.value = false
        }
    }

    fun insertManualBill(bill: Bill) {
        coroutineScope.launch(ioDispatcher) {
            billRepository.insertBill(bill)
            _isManualBillVisible.value = false
            _manualBillPrefillDate.value = null
            _uiState.update { current -> current.copy(selectedCalendarDate = null) }
        }
    }

    fun showProfileEdit() {
        dismissDashboardOverlays()
        clearBillAndCategorySelection()
        _isProfileEditVisible.value = true
    }

    fun dismissProfileEdit() {
        _isProfileEditVisible.value = false
    }

    fun showManualBillEntry(prefillDate: LocalDate? = null) {
        dismissDashboardOverlays()
        clearBillAndCategorySelection()
        _manualBillPrefillDate.value = prefillDate
        _manualBillEntrySession.update { session -> session + 1 }
        _isManualBillVisible.value = true
    }

    fun dismissManualBillEntry() {
        _isManualBillVisible.value = false
        _manualBillPrefillDate.value = null
        _uiState.update { current -> current.copy(selectedCalendarDate = null) }
    }

    fun selectBillForEdit(bill: Bill) {
        dismissDashboardOverlays()
        clearBillAndCategorySelection()
        _selectedBillForEdit.value = bill
    }

    fun clearEditSelection() {
        _selectedBillForEdit.value = null
    }

    fun onPetalTapped(category: String) {
        dismissDashboardOverlays()
        clearBillAndCategorySelection()
        val highlightParent = BloomCategoryDefinitions.fromDisplayName(category)?.parentName
        _uiState.update { current ->
            current.copy(highlightedBloomParent = highlightParent)
        }
        _selectedCategoryForEdit.value = category
    }

    fun clearCategorySelection() {
        _selectedCategoryForEdit.value = null
        _uiState.update { current -> current.copy(highlightedBloomParent = null) }
    }

    fun saveBillEdits(updatedBill: Bill) {
        coroutineScope.launch(ioDispatcher) {
            billRepository.updateBill(updatedBill)
            clearEditSelection()
        }
    }

    fun toggleBillCalendarExpanded() {
        _uiState.update { current ->
            current.copy(isBillCalendarExpanded = !current.isBillCalendarExpanded)
        }
    }

    fun showPreviousCalendarMonth() {
        _uiState.update { current ->
            current.copy(calendarVisibleMonth = current.calendarVisibleMonth.minusMonths(1))
        }
    }

    fun showNextCalendarMonth() {
        _uiState.update { current ->
            current.copy(calendarVisibleMonth = current.calendarVisibleMonth.plusMonths(1))
        }
    }

    fun onCalendarDayTapped(date: LocalDate) {
        _uiState.update { current ->
            current.copy(
                selectedCalendarDate = date,
                calendarVisibleMonth = YearMonth.from(date)
            )
        }
        showManualBillEntry(prefillDate = date)
    }

    fun onCalendarBillTapped(bill: Bill) {
        _uiState.update { current ->
            current.copy(selectedCalendarDate = bill.dueDate)
        }
        selectBillForEdit(bill)
    }

    fun clearDashboardTransientState() {
        dismissDashboardOverlays()
        clearBillAndCategorySelection()
        _uiState.update { current ->
            current.copy(
                selectedCalendarDate = null,
                highlightedBloomParent = null
            )
        }
    }

    private fun dismissDashboardOverlays() {
        _isProfileEditVisible.value = false
        _isManualBillVisible.value = false
    }

    private fun clearBillAndCategorySelection() {
        _selectedBillForEdit.value = null
        _selectedCategoryForEdit.value = null
    }

    fun openBloomSettingsEdit() {
        val subscription = _uiState.value.subscriptionBills.firstOrNull()
            ?: _uiState.value.upcomingBills.firstOrNull()
        if (subscription != null) {
            selectBillForEdit(subscription)
        }
    }

    fun startParallaxSensor() {
        if (parallaxSensorJob?.isActive == true) {
            return
        }
        parallaxSensorJob = coroutineScope.launch(Dispatchers.Main.immediate) {
            deviceTiltSensor.tiltOffsets().collect { offset ->
                _parallaxOffset.value = offset
            }
        }
    }

    fun stopParallaxSensor() {
        parallaxSensorJob?.cancel()
        parallaxSensorJob = null
        _parallaxOffset.value = 0f to 0f
    }

    fun onCleared() {
        stopParallaxSensor()
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
