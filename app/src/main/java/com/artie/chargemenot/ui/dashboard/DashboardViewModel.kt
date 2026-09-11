package com.artie.chargemenot.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.data.sensors.DeviceTiltSensor
import com.artie.chargemenot.data.sensors.StationaryDeviceTiltSensor
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.domain.model.UserSettings
import com.artie.chargemenot.domain.usecase.ForecastUseCase
import com.artie.chargemenot.ui.components.BloomCategoryDefinitions
import com.artie.chargemenot.ui.components.sortGardenPathBills
import com.artie.chargemenot.util.CurrencyParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val billRepository: BillRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val forecastUseCase: ForecastUseCase,
    private val deviceTiltSensor: DeviceTiltSensor = StationaryDeviceTiltSensor()
) : ViewModel() {

    private val overlayState = MutableStateFlow(DashboardOverlayState())
    private val parallaxOffsetState = MutableStateFlow(0f to 0f)
    private var parallaxSensorJob: Job? = null

    private val categoryBillsFlow = overlayState
        .map { overlays -> overlays.selectedCategoryForEdit }
        .distinctUntilChanged()
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

    val uiState: StateFlow<DashboardUiState> = combine(
        combine(
            billRepository.getUpcomingBills(),
            billRepository.getAllBills(),
            userSettingsRepository.observeUserSettings(),
            overlayState,
            categoryBillsFlow
        ) { upcoming, all, settings, overlays, categoryBills ->
            DashboardSnapshot(
                upcomingBills = upcoming,
                allBills = all,
                settings = settings,
                overlays = overlays,
                categoryBills = categoryBills
            )
        },
        parallaxOffsetState
    ) { snapshot, parallaxOffset ->
        snapshot.toUiState(parallaxOffset)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DashboardUiState()
    )

    fun keepSubscription(bill: Bill) {
        viewModelScope.launch {
            billRepository.updateBill(bill.copy(isPaid = true))
        }
    }

    fun pullSubscription(bill: Bill) {
        deleteBill(bill)
    }

    fun deleteBill(bill: Bill) {
        overlayState.update { overlays ->
            if (overlays.selectedBillForEdit?.id == bill.id) {
                overlays.copy(selectedBillForEdit = null)
            } else {
                overlays
            }
        }
        viewModelScope.launch {
            billRepository.deleteBill(bill)
        }
    }

    fun linkBillToParent(childBillId: Long, parentBillId: Long?) {
        viewModelScope.launch {
            billRepository.linkBillToParent(childBillId, parentBillId)
        }
        overlayState.update { overlays -> overlays.copy(billToLink = null) }
    }

    fun updateMonthlyBudget(rawBudgetInput: String): Boolean {
        val parsedBudget = CurrencyParser.parseStringToCents(rawBudgetInput)
        if (parsedBudget < UserSettings.MIN_MONTHLY_BUDGET) {
            return false
        }

        viewModelScope.launch {
            userSettingsRepository.updateMonthlyBudget(parsedBudget)
        }
        return true
    }

    fun updateDisplayName(displayName: String) {
        viewModelScope.launch {
            userSettingsRepository.updateDisplayName(displayName)
            overlayState.update { overlays -> overlays.copy(isProfileEditVisible = false) }
        }
    }

    fun insertManualBill(bill: Bill) {
        viewModelScope.launch {
            billRepository.insertBill(bill)
            overlayState.update { overlays ->
                overlays.copy(
                    isManualBillVisible = false,
                    manualBillPrefillDate = null,
                    selectedCalendarDate = null
                )
            }
        }
    }

    fun showProfileEdit() {
        overlayState.update { overlays ->
            overlays.clearedSelections().copy(isProfileEditVisible = true)
        }
    }

    fun dismissProfileEdit() {
        overlayState.update { overlays -> overlays.copy(isProfileEditVisible = false) }
    }

    fun showManualBillEntry(prefillDate: LocalDate? = null) {
        overlayState.update { overlays ->
            overlays.clearedSelections().copy(
                isManualBillVisible = true,
                manualBillPrefillDate = prefillDate,
                manualBillEntrySession = overlays.manualBillEntrySession + 1
            )
        }
    }

    fun dismissManualBillEntry() {
        overlayState.update { overlays ->
            overlays.copy(
                isManualBillVisible = false,
                manualBillPrefillDate = null,
                selectedCalendarDate = null
            )
        }
    }

    fun selectBillForEdit(bill: Bill) {
        overlayState.update { overlays ->
            overlays.clearedSelections().copy(selectedBillForEdit = bill)
        }
    }

    fun clearEditSelection() {
        overlayState.update { overlays -> overlays.copy(selectedBillForEdit = null) }
    }

    fun onPetalTapped(category: String) {
        val highlightParent = BloomCategoryDefinitions.fromDisplayName(category)?.parentName
        overlayState.update { overlays ->
            overlays.clearedSelections().copy(
                selectedCategoryForEdit = category,
                highlightedBloomParent = highlightParent
            )
        }
    }

    fun clearCategorySelection() {
        overlayState.update { overlays ->
            overlays.copy(
                selectedCategoryForEdit = null,
                highlightedBloomParent = null
            )
        }
    }

    fun saveBillEdits(updatedBill: Bill) {
        viewModelScope.launch {
            billRepository.updateBill(updatedBill)
            clearEditSelection()
        }
    }

    fun toggleBillCalendarExpanded() {
        overlayState.update { overlays ->
            overlays.copy(isBillCalendarExpanded = !overlays.isBillCalendarExpanded)
        }
    }

    fun showPreviousCalendarMonth() {
        overlayState.update { overlays ->
            overlays.copy(calendarVisibleMonth = overlays.calendarVisibleMonth.minusMonths(1))
        }
    }

    fun showNextCalendarMonth() {
        overlayState.update { overlays ->
            overlays.copy(calendarVisibleMonth = overlays.calendarVisibleMonth.plusMonths(1))
        }
    }

    fun onCalendarDayTapped(date: LocalDate) {
        overlayState.update { overlays ->
            overlays.copy(
                selectedCalendarDate = date,
                calendarVisibleMonth = YearMonth.from(date)
            )
        }
        showManualBillEntry(prefillDate = date)
    }

    fun onCalendarBillTapped(bill: Bill) {
        overlayState.update { overlays ->
            overlays.copy(selectedCalendarDate = bill.dueDate)
        }
        selectBillForEdit(bill)
    }

    fun toggleSearchActive() {
        overlayState.update { overlays ->
            val nextActive = !overlays.isSearchActive
            overlays.copy(
                isSearchActive = nextActive,
                searchQuery = if (nextActive) overlays.searchQuery else ""
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        overlayState.update { overlays -> overlays.copy(searchQuery = query) }
    }

    fun shareBill(bill: Bill) {
        overlayState.update { overlays -> overlays.copy(billToShare = bill) }
    }

    fun dismissShareBill() {
        overlayState.update { overlays -> overlays.copy(billToShare = null) }
    }

    fun linkBill(bill: Bill) {
        overlayState.update { overlays -> overlays.copy(billToLink = bill) }
    }

    fun dismissLinkBill() {
        overlayState.update { overlays -> overlays.copy(billToLink = null) }
    }

    fun clearDashboardTransientState() {
        overlayState.update { overlays ->
            overlays.clearedSelections().copy(
                selectedCalendarDate = null,
                highlightedBloomParent = null,
                isSearchActive = false,
                searchQuery = "",
                billToShare = null,
                billToLink = null
            )
        }
    }

    fun openBloomSettingsEdit() {
        val state = uiState.value
        val subscription = state.subscriptionBills.firstOrNull()
            ?: state.upcomingBills.firstOrNull()
        if (subscription != null) {
            selectBillForEdit(subscription)
        }
    }

    fun startParallaxSensor() {
        if (parallaxSensorJob?.isActive == true) {
            return
        }
        parallaxSensorJob = viewModelScope.launch {
            deviceTiltSensor.tiltOffsets().collect { offset ->
                parallaxOffsetState.value = offset
            }
        }
    }

    fun stopParallaxSensor() {
        parallaxSensorJob?.cancel()
        parallaxSensorJob = null
        parallaxOffsetState.value = 0f to 0f
    }

    override fun onCleared() {
        stopParallaxSensor()
        super.onCleared()
    }

    private fun DashboardSnapshot.toUiState(
        parallaxOffset: Pair<Float, Float>
    ): DashboardUiState {
        val subscriptions = allBills.filter { bill ->
            bill.parentCategory == MeadowCategories.VINES && !bill.isPaid
        }
        val parentCategoryTotals = upcomingBills
            .groupBy { bill -> bill.parentCategory }
            .mapValues { (_, bills) -> bills.sumOf { bill -> bill.amount } }
        val forecast = forecastUseCase.calculateForecastFromDomainBills(
            bills = allBills,
            today = LocalDate.now()
        )
        val billsByDueDate = allBills
            .filter { bill -> !bill.isPaid }
            .groupBy { bill -> bill.dueDate }

        return DashboardUiState(
            userDisplayName = settings.displayName,
            formattedDate = formatDisplayDate(LocalDate.now()),
            greeting = resolveGreeting(),
            totalUpcoming = upcomingBills.sumOf { bill -> bill.amount },
            upcomingBillCount = upcomingBills.size,
            monthlyBudget = settings.monthlyBudget,
            upcomingBills = upcomingBills,
            subscriptionBills = subscriptions,
            allBills = allBills,
            gardenPathBills = sortGardenPathBills(allBills),
            parentCategoryTotals = parentCategoryTotals,
            forecastResult = forecast,
            highlightedBloomParent = overlays.highlightedBloomParent,
            selectedCurrency = SupportedCurrency.fromCode(settings.selectedCurrency),
            isBillCalendarExpanded = overlays.isBillCalendarExpanded,
            calendarVisibleMonth = overlays.calendarVisibleMonth,
            billsByDueDate = billsByDueDate,
            selectedCalendarDate = overlays.selectedCalendarDate,
            isLoading = false,
            selectedBillForEdit = overlays.selectedBillForEdit,
            selectedCategoryForEdit = overlays.selectedCategoryForEdit,
            categoryBills = categoryBills,
            isProfileEditVisible = overlays.isProfileEditVisible,
            isManualBillVisible = overlays.isManualBillVisible,
            manualBillEntrySession = overlays.manualBillEntrySession,
            manualBillPrefillDate = overlays.manualBillPrefillDate,
            isSearchActive = overlays.isSearchActive,
            searchQuery = overlays.searchQuery,
            billToShare = overlays.billToShare,
            billToLink = overlays.billToLink,
            parallaxOffset = parallaxOffset
        )
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

    private data class DashboardOverlayState(
        val highlightedBloomParent: String? = null,
        val isBillCalendarExpanded: Boolean = false,
        val calendarVisibleMonth: YearMonth = YearMonth.now(),
        val selectedCalendarDate: LocalDate? = null,
        val selectedBillForEdit: Bill? = null,
        val selectedCategoryForEdit: String? = null,
        val isProfileEditVisible: Boolean = false,
        val isManualBillVisible: Boolean = false,
        val manualBillEntrySession: Int = 0,
        val manualBillPrefillDate: LocalDate? = null,
        val isSearchActive: Boolean = false,
        val searchQuery: String = "",
        val billToShare: Bill? = null,
        val billToLink: Bill? = null
    ) {
        fun clearedSelections(): DashboardOverlayState = copy(
            selectedBillForEdit = null,
            selectedCategoryForEdit = null,
            isProfileEditVisible = false,
            isManualBillVisible = false
        )
    }

    private data class DashboardSnapshot(
        val upcomingBills: List<Bill>,
        val allBills: List<Bill>,
        val settings: UserSettings,
        val overlays: DashboardOverlayState,
        val categoryBills: List<Bill>
    )
}
