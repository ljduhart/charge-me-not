package com.artie.chargemenot.ui.dashboard

import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.ForecastResult
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.domain.model.UserSettings
import java.time.LocalDate
import java.time.YearMonth

data class DashboardUiState(
    val userDisplayName: String = "Sarah",
    val formattedDate: String = "",
    val greeting: String = "Good morning",
    val totalUpcoming: Long = 0L,
    val upcomingBillCount: Int = 0,
    val monthlyBudget: Long = UserSettings.DEFAULT_MONTHLY_BUDGET,
    val upcomingBills: List<Bill> = emptyList(),
    val subscriptionBills: List<Bill> = emptyList(),
    val allBills: List<Bill> = emptyList(),
    val gardenPathBills: List<Bill> = emptyList(),
    val parentCategoryTotals: Map<String, Long> = emptyMap(),
    val forecastResult: ForecastResult? = null,
    val highlightedBloomParent: String? = null,
    val selectedCurrency: SupportedCurrency = SupportedCurrency.USD,
    val isBillCalendarExpanded: Boolean = false,
    val calendarVisibleMonth: YearMonth = YearMonth.now(),
    val billsByDueDate: Map<LocalDate, List<Bill>> = emptyMap(),
    val selectedCalendarDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val selectedBillForEdit: Bill? = null,
    val selectedCategoryForEdit: String? = null,
    val categoryBills: List<Bill> = emptyList(),
    val isProfileEditVisible: Boolean = false,
    val isManualBillVisible: Boolean = false,
    val manualBillEntrySession: Int = 0,
    val manualBillPrefillDate: LocalDate? = null,
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val billToShare: Bill? = null,
    val billToLink: Bill? = null,
    val parallaxOffset: Pair<Float, Float> = 0f to 0f
)
