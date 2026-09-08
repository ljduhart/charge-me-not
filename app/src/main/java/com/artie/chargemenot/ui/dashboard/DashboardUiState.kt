package com.artie.chargemenot.ui.dashboard

import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.ForecastResult
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.domain.model.UserSettings
import java.time.LocalDate
import java.time.YearMonth

data class DashboardUiState(
    val userDisplayName: String = "Sarah",
    val formattedDate: String = "",
    val greeting: String = "Good morning",
    val totalUpcoming: Double = 0.0,
    val upcomingBillCount: Int = 0,
    val monthlyBudget: Double = UserSettings.DEFAULT_MONTHLY_BUDGET,
    val upcomingBills: List<Bill> = emptyList(),
    val subscriptionBills: List<Bill> = emptyList(),
    val allBills: List<Bill> = emptyList(),
    val parentCategoryTotals: Map<String, Double> = emptyMap(),
    val forecastResult: ForecastResult? = null,
    val highlightedBloomParent: String? = null,
    val selectedCurrency: String = UserSettings.DEFAULT_CURRENCY,
    val isBillCalendarExpanded: Boolean = false,
    val calendarVisibleMonth: YearMonth = YearMonth.now(),
    val billsByDueDate: Map<LocalDate, List<Bill>> = emptyMap(),
    val selectedCalendarDate: LocalDate? = null,
    val isLoading: Boolean = true
)
