package com.artie.chargemenot.ui.dashboard

import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.domain.model.ForecastResult
import com.artie.chargemenot.domain.model.UserSettings

enum class DashboardBottomNavItem {
    RENT,
    LOANS,
    UTILITIES,
    OTHERS
}

fun DashboardBottomNavItem.toHighlightCategory(): BillCategory? = when (this) {
    DashboardBottomNavItem.RENT -> BillCategory.RENT
    DashboardBottomNavItem.LOANS -> BillCategory.TRANSPORTATION
    DashboardBottomNavItem.UTILITIES -> BillCategory.UTILITIES
    DashboardBottomNavItem.OTHERS -> BillCategory.OTHER
}

fun BillCategory.matchesDashboardNav(item: DashboardBottomNavItem): Boolean {
    return when (item) {
        DashboardBottomNavItem.RENT -> this == BillCategory.RENT
        DashboardBottomNavItem.LOANS -> this == BillCategory.TRANSPORTATION
        DashboardBottomNavItem.UTILITIES -> this == BillCategory.UTILITIES
        DashboardBottomNavItem.OTHERS -> this !in setOf(
            BillCategory.RENT,
            BillCategory.TRANSPORTATION,
            BillCategory.UTILITIES
        )
    }
}

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
    val categoryTotals: Map<BillCategory, Double> = emptyMap(),
    val forecastResult: ForecastResult? = null,
    val selectedBottomNavItem: DashboardBottomNavItem = DashboardBottomNavItem.RENT,
    val isLoading: Boolean = true
)
