package com.artie.chargemenot.ui.dashboard

import com.artie.chargemenot.domain.model.Bill

data class DashboardUiState(
    val greeting: String = "Good morning",
    val totalUpcoming: Double = 0.0,
    val upcomingBills: List<Bill> = emptyList(),
    val subscriptionBills: List<Bill> = emptyList(),
    val categoryTotals: Map<com.artie.chargemenot.domain.model.BillCategory, Double> = emptyMap(),
    val isLoading: Boolean = true
)
