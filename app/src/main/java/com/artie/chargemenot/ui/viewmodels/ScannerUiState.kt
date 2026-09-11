package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.domain.model.UserSettings
import java.time.LocalDate

data class ScannedBillData(
    val amount: Long? = null,
    val dueDate: LocalDate? = null,
    val rawText: String = "",
    val receiptImagePath: String? = null
)

data class PredictiveImpact(
    val parentCategory: String,
    val newPetalSizePercent: Double,
    val scannedAmount: Long,
    val withinBudget: Boolean,
    val totalProjectedSpend: Long
)

data class PollenReceivedState(
    val name: String,
    val amount: Long,
    val dueDate: LocalDate,
    val parentCategory: String,
    val subCategory: String
)

data class ScannerUiState(
    val scannedBill: ScannedBillData = ScannedBillData(),
    val selectedParentCategory: String = MeadowCategories.ROOT_SYSTEM,
    val selectedSubCategory: String = MeadowCategories.defaultSubcategoryByParent[MeadowCategories.ROOT_SYSTEM]!!,
    val parentCategoryTotals: Map<String, Long> = emptyMap(),
    val monthlyBudget: Long = UserSettings.DEFAULT_MONTHLY_BUDGET,
    val selectedCurrency: SupportedCurrency = SupportedCurrency.USD,
    val predictiveImpact: PredictiveImpact? = null,
    val scanStatusMessage: String = "Point camera at your bill to scan",
    val budgetSummary: String = "Scan a bill to preview budget impact",
    val pollenReceived: PollenReceivedState? = null,
    val detectionBannerMessage: String = "Ready to scan paper bills or partner QR codes",
    val canSaveScannedBill: Boolean = false,
    val isSavingScannedBill: Boolean = false
)
