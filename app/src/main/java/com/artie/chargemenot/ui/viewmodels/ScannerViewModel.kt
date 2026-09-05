package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.scanner.OcrScanResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class ScannerViewModel(
    private val billRepository: BillRepository,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val monthlyBudget: Double = MONTHLY_BUDGET
) {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val dateFormat = DateTimeFormatter.ofPattern("MMM d, yyyy")

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    init {
        observeCategoryTotals()
    }

    private fun observeCategoryTotals() {
        coroutineScope.launch(ioDispatcher) {
            billRepository.getUpcomingBills().collect { upcomingBills ->
                val categoryTotals = upcomingBills
                    .groupBy { bill -> bill.category }
                    .mapValues { (_, bills) -> bills.sumOf { bill -> bill.amount } }

                _uiState.update { current ->
                    val recalculatedImpact = current.scannedBill.amount?.let { amount ->
                        calculatePredictiveImpact(
                            category = current.selectedCategory,
                            scannedAmount = amount,
                            categoryTotals = categoryTotals
                        )
                    }

                    current.copy(
                        categoryTotals = categoryTotals,
                        predictiveImpact = recalculatedImpact,
                        budgetSummary = buildBudgetSummary(recalculatedImpact)
                    )
                }
            }
        }
    }

    fun onScanResult(result: OcrScanResult) {
        val mergedScan = _uiState.value.scannedBill.merge(result)
        val impact = mergedScan.amount?.let { amount ->
            calculatePredictiveImpact(
                category = _uiState.value.selectedCategory,
                scannedAmount = amount,
                categoryTotals = _uiState.value.categoryTotals
            )
        }

        _uiState.update { current ->
            current.copy(
                scannedBill = mergedScan,
                predictiveImpact = impact,
                scanStatusMessage = buildScanStatusMessage(mergedScan),
                budgetSummary = buildBudgetSummary(impact)
            )
        }
    }

    fun selectCategory(category: BillCategory) {
        val scannedAmount = _uiState.value.scannedBill.amount ?: return
        val impact = calculatePredictiveImpact(
            category = category,
            scannedAmount = scannedAmount,
            categoryTotals = _uiState.value.categoryTotals
        )

        _uiState.update { current ->
            current.copy(
                selectedCategory = category,
                predictiveImpact = impact,
                budgetSummary = buildBudgetSummary(impact)
            )
        }
    }

    fun calculatePredictiveImpact(
        category: BillCategory,
        scannedAmount: Double,
        categoryTotals: Map<BillCategory, Double>
    ): PredictiveImpact {
        val currentCategorySpend = categoryTotals[category] ?: 0.0
        val newCategorySpend = currentCategorySpend + scannedAmount
        val newPetalSizePercent = (newCategorySpend / monthlyBudget) * PERCENT_SCALE
        val totalProjectedSpend = categoryTotals.values.sum() + scannedAmount

        return PredictiveImpact(
            category = category,
            newPetalSizePercent = newPetalSizePercent,
            scannedAmount = scannedAmount,
            withinBudget = totalProjectedSpend <= monthlyBudget,
            totalProjectedSpend = totalProjectedSpend
        )
    }

    private fun ScannedBillData.merge(result: OcrScanResult): ScannedBillData {
        return copy(
            amount = result.amount ?: amount,
            dueDate = result.dueDate ?: dueDate
        )
    }

    private fun buildScanStatusMessage(scannedBill: ScannedBillData): String {
        val amountText = scannedBill.amount?.let(currencyFormat::format) ?: "—"
        val dateText = scannedBill.dueDate?.format(dateFormat) ?: "—"
        return "Scanned Details Captured! Date: $dateText, Amount: $amountText"
    }

    private fun buildBudgetSummary(impact: PredictiveImpact?): String {
        if (impact == null) {
            return "Scan a bill to preview budget impact"
        }

        return if (impact.withinBudget) {
            "Adding this bill keeps you within your budget."
        } else {
            "Adding this bill exceeds your $${monthlyBudget.toInt()} monthly budget."
        }
    }

    companion object {
        const val MONTHLY_BUDGET = 2_500.0
        private const val PERCENT_SCALE = 100.0
    }
}
