package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.domain.model.UserSettings

data class PruningUiState(
    val bills: List<BillEntity> = emptyList(),
    val prunedBillIds: Set<Long> = emptySet(),
    val childRelationships: Map<Long, List<BillEntity>> = emptyMap(),
    val expandedRootBillId: Long? = null,
    val originalParentCategoryTotals: Map<String, Long> = emptyMap(),
    val projectedParentCategoryTotals: Map<String, Long> = emptyMap(),
    val parentCategoryAlphas: Map<String, Float> = emptyMap(),
    val newMonthlyTotal: Long = 0L,
    val monthlyBudget: Long = UserSettings.DEFAULT_MONTHLY_BUDGET,
    val selectedCurrency: SupportedCurrency = SupportedCurrency.USD,
    val isLoading: Boolean = true
)
