package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.domain.model.BillCategory
import java.time.LocalDate

data class ScannedBillData(
    val amount: Double? = null,
    val dueDate: LocalDate? = null
)

data class PredictiveImpact(
    val category: BillCategory,
    val newPetalSizePercent: Double,
    val scannedAmount: Double,
    val withinBudget: Boolean,
    val totalProjectedSpend: Double
)

data class ScannerUiState(
    val scannedBill: ScannedBillData = ScannedBillData(),
    val selectedCategory: BillCategory = BillCategory.UTILITIES,
    val categoryTotals: Map<BillCategory, Double> = emptyMap(),
    val monthlyBudget: Double = com.artie.chargemenot.domain.model.UserSettings.DEFAULT_MONTHLY_BUDGET,
    val predictiveImpact: PredictiveImpact? = null,
    val scanStatusMessage: String = "Point camera at your bill to scan",
    val budgetSummary: String = "Scan a bill to preview budget impact"
)
