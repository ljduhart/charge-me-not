package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.domain.model.UserSettings

data class PruningUiState(
    val bills: List<BillEntity> = emptyList(),
    val prunedBillIds: Set<Long> = emptySet(),
    val originalCategoryTotals: Map<BillCategory, Double> = emptyMap(),
    val projectedCategoryTotals: Map<BillCategory, Double> = emptyMap(),
    val categoryAlphas: Map<BillCategory, Float> = emptyMap(),
    val newMonthlyTotal: Double = 0.0,
    val monthlyBudget: Double = UserSettings.DEFAULT_MONTHLY_BUDGET,
    val isLoading: Boolean = true
)
