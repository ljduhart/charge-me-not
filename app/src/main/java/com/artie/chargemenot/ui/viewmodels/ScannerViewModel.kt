package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.model.CrossPollinationPayload
import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.domain.model.UserSettings
import com.artie.chargemenot.scanner.OcrScanResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class ScannerViewModel(
    private val billRepository: BillRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val dateFormat = DateTimeFormatter.ofPattern("MMM d, yyyy")

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    init {
        observeScannerData()
    }

    private fun observeScannerData() {
        coroutineScope.launch(ioDispatcher) {
            combine(
                billRepository.getUpcomingBills(),
                userSettingsRepository.observeMonthlyBudget()
            ) { upcomingBills, monthlyBudget ->
                val categoryTotals = upcomingBills
                    .groupBy { bill -> bill.category }
                    .mapValues { (_, bills) -> bills.sumOf { bill -> bill.amount } }

                categoryTotals to monthlyBudget
            }.collect { (categoryTotals, monthlyBudget) ->
                _uiState.update { current ->
                    val recalculatedImpact = current.scannedBill.amount?.let { amount ->
                        calculatePredictiveImpact(
                            category = current.selectedCategory,
                            scannedAmount = amount,
                            categoryTotals = categoryTotals,
                            monthlyBudget = monthlyBudget
                        )
                    }

                    current.copy(
                        categoryTotals = categoryTotals,
                        monthlyBudget = monthlyBudget,
                        predictiveImpact = recalculatedImpact,
                        budgetSummary = buildBudgetSummary(recalculatedImpact, monthlyBudget)
                    )
                }
            }
        }
    }

    fun onScanResult(result: OcrScanResult) {
        if (_uiState.value.pollenReceived != null) {
            return
        }

        val mergedScan = _uiState.value.scannedBill.merge(result)
        val monthlyBudget = _uiState.value.monthlyBudget
        val impact = mergedScan.amount?.let { amount ->
            calculatePredictiveImpact(
                category = _uiState.value.selectedCategory,
                scannedAmount = amount,
                categoryTotals = _uiState.value.categoryTotals,
                monthlyBudget = monthlyBudget
            )
        }

        _uiState.update { current ->
            current.copy(
                scannedBill = mergedScan,
                predictiveImpact = impact,
                scanStatusMessage = buildScanStatusMessage(mergedScan),
                budgetSummary = buildBudgetSummary(impact, monthlyBudget)
            )
        }
    }

    fun selectCategory(category: BillCategory) {
        val scannedAmount = _uiState.value.scannedBill.amount ?: return
        val monthlyBudget = _uiState.value.monthlyBudget
        val impact = calculatePredictiveImpact(
            category = category,
            scannedAmount = scannedAmount,
            categoryTotals = _uiState.value.categoryTotals,
            monthlyBudget = monthlyBudget
        )

        _uiState.update { current ->
            current.copy(
                selectedCategory = category,
                predictiveImpact = impact,
                budgetSummary = buildBudgetSummary(impact, monthlyBudget)
            )
        }
    }

    fun onQrPayloadDetected(payload: CrossPollinationPayload) {
        if (_uiState.value.pollenReceived != null) {
            return
        }

        val billEntity = payload.toBillEntity() ?: return

        _uiState.update { current ->
            current.copy(
                pollenReceived = PollenReceivedState(
                    name = billEntity.name,
                    amount = billEntity.amount,
                    dueDate = billEntity.dueDate,
                    category = billEntity.category
                ),
                scanStatusMessage = "Partner QR detected: ${billEntity.name}",
                detectionBannerMessage = "Cross-pollination pollen received — review before planting"
            )
        }
    }

    fun discardPollen() {
        _uiState.update { current ->
            current.copy(
                pollenReceived = null,
                scanStatusMessage = "Point camera at your bill to scan",
                detectionBannerMessage = DEFAULT_DETECTION_BANNER
            )
        }
    }

    fun acceptPollinatedBill(onAccepted: () -> Unit) {
        val pollen = _uiState.value.pollenReceived ?: return

        coroutineScope.launch(ioDispatcher) {
            billRepository.insertBill(
                Bill(
                    name = pollen.name,
                    amount = pollen.amount,
                    dueDate = pollen.dueDate,
                    category = pollen.category
                )
            )
            resetScanSession()
            onAccepted()
        }
    }

    fun resetScanSession() {
        _uiState.update { current ->
            current.copy(
                scannedBill = ScannedBillData(),
                selectedCategory = BillCategory.UTILITIES,
                predictiveImpact = null,
                pollenReceived = null,
                scanStatusMessage = "Point camera at your bill to scan",
                detectionBannerMessage = DEFAULT_DETECTION_BANNER,
                budgetSummary = buildBudgetSummary(null, current.monthlyBudget)
            )
        }
    }

    fun calculatePredictiveImpact(
        category: BillCategory,
        scannedAmount: Double,
        categoryTotals: Map<BillCategory, Double>,
        monthlyBudget: Double
    ): PredictiveImpact {
        val safeBudget = monthlyBudget.coerceAtLeast(UserSettings.MIN_MONTHLY_BUDGET)
        val currentCategorySpend = categoryTotals[category] ?: 0.0
        val newCategorySpend = currentCategorySpend + scannedAmount
        val newPetalSizePercent = (newCategorySpend / safeBudget) * PERCENT_SCALE
        val totalProjectedSpend = categoryTotals.values.sum() + scannedAmount

        return PredictiveImpact(
            category = category,
            newPetalSizePercent = newPetalSizePercent,
            scannedAmount = scannedAmount,
            withinBudget = totalProjectedSpend <= safeBudget,
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

    private fun buildBudgetSummary(impact: PredictiveImpact?, monthlyBudget: Double): String {
        if (impact == null) {
            return "Scan a bill to preview budget impact"
        }

        val formattedBudget = currencyFormat.format(monthlyBudget.coerceAtLeast(UserSettings.MIN_MONTHLY_BUDGET))
        return if (impact.withinBudget) {
            "Adding this bill keeps you within your $formattedBudget monthly budget."
        } else {
            "Adding this bill exceeds your $formattedBudget monthly budget."
        }
    }

    companion object {
        private const val PERCENT_SCALE = 100.0
        private const val DEFAULT_DETECTION_BANNER = "Ready to scan paper bills or partner QR codes"
    }
}
