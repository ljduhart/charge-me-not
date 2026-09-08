package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.domain.model.UserSettings

data class PruningUiState(
    val bills: List<BillEntity> = emptyList(),
    val prunedBillIds: Set<Long> = emptySet(),
    val childRelationships: Map<Long, List<BillEntity>> = emptyMap(),
    val expandedRootBillId: Long? = null,
    val originalParentCategoryTotals: Map<String, Double> = emptyMap(),
    val projectedParentCategoryTotals: Map<String, Double> = emptyMap(),
    val parentCategoryAlphas: Map<String, Float> = emptyMap(),
    val newMonthlyTotal: Double = 0.0,
    val monthlyBudget: Double = UserSettings.DEFAULT_MONTHLY_BUDGET,
    val isLoading: Boolean = true
)
