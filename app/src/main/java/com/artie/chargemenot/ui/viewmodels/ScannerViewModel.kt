package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.model.CrossPollinationPayload
import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.domain.model.UserSettings
import com.artie.chargemenot.scanner.OcrScanResult
import com.artie.chargemenot.util.CurrencyFormatter
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
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean

class ScannerViewModel(
    private val billRepository: BillRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

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
                userSettingsRepository.observeUserSettings()
            ) { upcomingBills, settings ->
                val parentCategoryTotals = upcomingBills
                    .groupBy { bill -> bill.parentCategory }
                    .mapValues { (_, bills) -> bills.sumOf { bill -> bill.amount } }

                Triple(
                    parentCategoryTotals,
                    settings.monthlyBudget,
                    SupportedCurrency.fromCode(settings.selectedCurrency)
                )
            }.collect { (parentCategoryTotals, monthlyBudget, selectedCurrency) ->
                _uiState.update { current ->
                    val recalculatedImpact = current.scannedBill.amount?.let { amount ->
                        calculatePredictiveImpact(
                            parentCategory = current.selectedParentCategory,
                            scannedAmount = amount,
                            parentCategoryTotals = parentCategoryTotals,
                            monthlyBudget = monthlyBudget
                        )
                    }

                    current.copy(
                        parentCategoryTotals = parentCategoryTotals,
                        monthlyBudget = monthlyBudget,
                        selectedCurrency = selectedCurrency,
                        predictiveImpact = recalculatedImpact,
                        scanStatusMessage = observedScanStatusMessage(
                            current = current,
                            currency = selectedCurrency
                        ),
                        budgetSummary = buildBudgetSummary(
                            impact = recalculatedImpact,
                            monthlyBudget = monthlyBudget,
                            currency = selectedCurrency
                        ),
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
                parentCategory = _uiState.value.selectedParentCategory,
                scannedAmount = amount,
                parentCategoryTotals = _uiState.value.parentCategoryTotals,
                monthlyBudget = monthlyBudget
            )
        }

        _uiState.update { current ->
            current.copy(
                scannedBill = mergedScan,
                predictiveImpact = impact,
                scanStatusMessage = buildScanStatusMessage(mergedScan, _uiState.value.selectedCurrency),
                budgetSummary = buildBudgetSummary(impact, monthlyBudget, _uiState.value.selectedCurrency),
                canSaveScannedBill = mergedScan.amount != null &&
                    mergedScan.dueDate != null &&
                    current.pollenReceived == null
            )
        }
    }

    fun selectParentCategory(parentCategory: String) {
        val scannedAmount = _uiState.value.scannedBill.amount
        val defaultSubcategory = MeadowCategories.defaultSubcategoryByParent[parentCategory]
            ?: _uiState.value.selectedSubCategory
        val monthlyBudget = _uiState.value.monthlyBudget
        val impact = scannedAmount?.let { amount ->
            calculatePredictiveImpact(
                parentCategory = parentCategory,
                scannedAmount = amount,
                parentCategoryTotals = _uiState.value.parentCategoryTotals,
                monthlyBudget = monthlyBudget
            )
        }

        _uiState.update { current ->
            current.copy(
                selectedParentCategory = parentCategory,
                selectedSubCategory = defaultSubcategory,
                predictiveImpact = impact,
                budgetSummary = buildBudgetSummary(impact, monthlyBudget, _uiState.value.selectedCurrency)
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
                    parentCategory = billEntity.parentCategory,
                    subCategory = billEntity.subCategory
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
                        parentCategory = pollen.parentCategory,
                        subCategory = pollen.subCategory
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
        val selectedParent = _uiState.value.selectedParentCategory
        val selectedSub = _uiState.value.selectedSubCategory
        if (!isSavingScannedBill.compareAndSet(false, true)) {
            return
        }

        coroutineScope.launch(ioDispatcher) {
            try {
                _uiState.update { it.copy(isSavingScannedBill = true) }
                billRepository.insertScannedBill(
                    bill = Bill(
                        name = deriveBillName(
                            parentCategory = selectedParent,
                            rawText = scanned.rawText
                        ),
                        amount = amount,
                        dueDate = dueDate,
                        parentCategory = selectedParent,
                        subCategory = selectedSub,
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
                selectedParentCategory = MeadowCategories.ROOT_SYSTEM,
                selectedSubCategory = MeadowCategories.defaultSubcategoryByParent[MeadowCategories.ROOT_SYSTEM]!!,
                predictiveImpact = null,
                pollenReceived = null,
                scanStatusMessage = "Point camera at your bill to scan",
                detectionBannerMessage = DEFAULT_DETECTION_BANNER,
                budgetSummary = buildBudgetSummary(null, current.monthlyBudget, current.selectedCurrency),
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
        parentCategory: String,
        scannedAmount: Long,
        parentCategoryTotals: Map<String, Long>,
        monthlyBudget: Long
    ): PredictiveImpact {
        val safeBudget = monthlyBudget.coerceAtLeast(UserSettings.MIN_MONTHLY_BUDGET)
        val currentCategorySpend = parentCategoryTotals[parentCategory] ?: 0L
        val newCategorySpend = currentCategorySpend + scannedAmount
        val newPetalSizePercent = (newCategorySpend.toDouble() / safeBudget.toDouble()) * PERCENT_SCALE
        val totalProjectedSpend = parentCategoryTotals.values.sum() + scannedAmount

        return PredictiveImpact(
            parentCategory = parentCategory,
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

    private fun deriveBillName(parentCategory: String, rawText: String): String {
        val firstLine = rawText.lineSequence()
            .map { line -> line.trim() }
            .firstOrNull { line -> line.isNotBlank() && line.length <= 60 }

        if (firstLine != null) {
            return firstLine
        }

        return when (parentCategory) {
            MeadowCategories.CANOPY -> "Scanned Rent Bill"
            MeadowCategories.FERTILIZER -> "Scanned Grocery Bill"
            MeadowCategories.VINES -> "Scanned Subscription Bill"
            MeadowCategories.POLLINATORS -> "Scanned Healthcare Bill"
            MeadowCategories.WILDFLOWERS -> "Scanned Entertainment Bill"
            else -> "Scanned Utility Bill"
        }
    }

    private fun observedScanStatusMessage(
        current: ScannerUiState,
        currency: SupportedCurrency
    ): String {
        if (current.pollenReceived != null) {
            return current.scanStatusMessage
        }
        val scannedBill = current.scannedBill
        return if (scannedBill.amount != null || scannedBill.dueDate != null) {
            buildScanStatusMessage(scannedBill, currency)
        } else {
            current.scanStatusMessage
        }
    }

    private fun buildScanStatusMessage(
        scannedBill: ScannedBillData,
        currency: SupportedCurrency
    ): String {
        val amountText = scannedBill.amount?.let { amount ->
            CurrencyFormatter.format(amount, currency)
        } ?: "—"
        val dateText = scannedBill.dueDate?.format(dateFormat) ?: "—"
        return "Scanned Details Captured! Date: $dateText, Amount: $amountText"
    }

    private fun buildBudgetSummary(
        impact: PredictiveImpact?,
        monthlyBudget: Long,
        currency: SupportedCurrency
    ): String {
        if (impact == null) {
            return "Scan a bill to preview budget impact"
        }

        val formattedBudget = CurrencyFormatter.format(
            monthlyBudget.coerceAtLeast(UserSettings.MIN_MONTHLY_BUDGET),
            currency
        )
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
