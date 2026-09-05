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
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

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

    private val isAcceptingPollen = AtomicBoolean(false)
    private val isSavingScannedBill = AtomicBoolean(false)
    private var qrSuppressedUntilMs = 0L

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
                        budgetSummary = buildBudgetSummary(recalculatedImpact, monthlyBudget),
                        canSaveScannedBill = current.scannedBill.amount != null &&
                            current.scannedBill.dueDate != null &&
                            current.pollenReceived == null
                    )
                }
            }
        }
    }

    fun onScanResult(result: OcrScanResult, receiptImagePath: String? = null) {
        if (_uiState.value.pollenReceived != null) {
            return
        }

        val previousReceiptPath = _uiState.value.scannedBill.receiptImagePath
        val mergedScan = _uiState.value.scannedBill.merge(result, receiptImagePath)
        if (
            receiptImagePath != null &&
            previousReceiptPath != null &&
            receiptImagePath != previousReceiptPath
        ) {
            coroutineScope.launch(ioDispatcher) {
                billRepository.deleteOrphanReceiptImage(previousReceiptPath)
            }
        }
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
                budgetSummary = buildBudgetSummary(impact, monthlyBudget),
                canSaveScannedBill = mergedScan.amount != null &&
                    mergedScan.dueDate != null &&
                    current.pollenReceived == null
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

        if (System.currentTimeMillis() < qrSuppressedUntilMs) {
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
                detectionBannerMessage = "Cross-pollination pollen received — review before planting",
                canSaveScannedBill = false
            )
        }
    }

    fun discardPollen() {
        qrSuppressedUntilMs = System.currentTimeMillis() + QR_SUPPRESSION_MS
        _uiState.update { current ->
            val scanned = current.scannedBill
            current.copy(
                pollenReceived = null,
                scanStatusMessage = "Point camera at your bill to scan",
                detectionBannerMessage = DEFAULT_DETECTION_BANNER,
                canSaveScannedBill = scanned.amount != null && scanned.dueDate != null
            )
        }
    }

    fun acceptPollinatedBill(onAccepted: () -> Unit) {
        val pollen = _uiState.value.pollenReceived ?: return
        if (!isAcceptingPollen.compareAndSet(false, true)) {
            return
        }

        coroutineScope.launch(ioDispatcher) {
            try {
                billRepository.insertBill(
                    Bill(
                        name = pollen.name,
                        amount = pollen.amount,
                        dueDate = pollen.dueDate,
                        category = pollen.category
                    )
                )
                resetScanSession()
                withContext(Dispatchers.Main) {
                    onAccepted()
                }
            } finally {
                isAcceptingPollen.set(false)
            }
        }
    }

    fun saveScannedBill(onSaved: () -> Unit) {
        val scanned = _uiState.value.scannedBill
        val amount = scanned.amount ?: return
        val dueDate = scanned.dueDate ?: return
        val selectedCategory = _uiState.value.selectedCategory
        if (!isSavingScannedBill.compareAndSet(false, true)) {
            return
        }

        coroutineScope.launch(ioDispatcher) {
            try {
                _uiState.update { it.copy(isSavingScannedBill = true) }
                billRepository.insertScannedBill(
                    bill = Bill(
                        name = deriveBillName(
                            category = selectedCategory,
                            rawText = scanned.rawText
                        ),
                        amount = amount,
                        dueDate = dueDate,
                        category = selectedCategory,
                        receiptImagePath = scanned.receiptImagePath
                    ),
                    rawText = scanned.rawText
                )
                resetScanSession()
                withContext(Dispatchers.Main) {
                    onSaved()
                }
            } finally {
                isSavingScannedBill.set(false)
                _uiState.update { it.copy(isSavingScannedBill = false) }
            }
        }
    }

    fun resetScanSession() {
        val orphanReceiptPath = _uiState.value.scannedBill.receiptImagePath
        _uiState.update { current ->
            current.copy(
                scannedBill = ScannedBillData(),
                selectedCategory = BillCategory.UTILITIES,
                predictiveImpact = null,
                pollenReceived = null,
                scanStatusMessage = "Point camera at your bill to scan",
                detectionBannerMessage = DEFAULT_DETECTION_BANNER,
                budgetSummary = buildBudgetSummary(null, current.monthlyBudget),
                canSaveScannedBill = false,
                isSavingScannedBill = false
            )
        }
        if (orphanReceiptPath != null) {
            coroutineScope.launch(ioDispatcher) {
                billRepository.deleteOrphanReceiptImage(orphanReceiptPath)
            }
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

    private fun ScannedBillData.merge(
        result: OcrScanResult,
        receiptImagePath: String?
    ): ScannedBillData {
        return copy(
            amount = result.amount ?: amount,
            dueDate = result.dueDate ?: dueDate,
            rawText = if (result.rawText.isNotBlank()) result.rawText else rawText,
            receiptImagePath = receiptImagePath ?: this.receiptImagePath
        )
    }

    private fun deriveBillName(category: BillCategory, rawText: String): String {
        val firstLine = rawText.lineSequence()
            .map { line -> line.trim() }
            .firstOrNull { line -> line.isNotBlank() && line.length <= 60 }

        if (firstLine != null) {
            return firstLine
        }

        return when (category) {
            BillCategory.RENT -> "Scanned Rent Bill"
            BillCategory.FOOD -> "Scanned Grocery Bill"
            BillCategory.UTILITIES -> "Scanned Utility Bill"
            BillCategory.SUBSCRIPTIONS -> "Scanned Subscription Bill"
            BillCategory.TRANSPORTATION -> "Scanned Transportation Bill"
            BillCategory.HEALTHCARE -> "Scanned Healthcare Bill"
            BillCategory.ENTERTAINMENT -> "Scanned Entertainment Bill"
            BillCategory.OTHER -> "Scanned Bill"
        }
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
        private const val QR_SUPPRESSION_MS = 2_500L
    }
}
