package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.BillEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WeedWhackerViewModel(
    private val billDao: BillDao,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val subscriptionsState = MutableStateFlow<List<BillEntity>>(emptyList())
    private val currentAuditIndex = MutableStateFlow(0)
    private val auditSessionComplete = MutableStateFlow(false)
    private val hasLoadedSubscriptions = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(WeedWhackerUiState())
    val uiState: StateFlow<WeedWhackerUiState> = _uiState.asStateFlow()

    init {
        observeSubscriptions()
        coroutineScope.launch(ioDispatcher) {
            billDao.getActiveSubscriptions().collect { subscriptions ->
                subscriptionsState.value = subscriptions
                hasLoadedSubscriptions.value = true
            }
        }
    }

    private fun observeSubscriptions() {
        coroutineScope.launch(ioDispatcher) {
            combine(
                subscriptionsState,
                currentAuditIndex,
                auditSessionComplete,
                hasLoadedSubscriptions
            ) { subscriptions, auditIndex, sessionComplete, hasLoaded ->
                buildUiState(
                    subscriptions = subscriptions,
                    auditIndex = auditIndex,
                    sessionComplete = sessionComplete,
                    isLoading = !hasLoaded
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun recordAuditResponse(billId: Long, used: Boolean) {
        coroutineScope.launch(ioDispatcher) {
            val bill = billDao.getBillByIdOnce(billId) ?: return@launch

            val updatedBill = if (used) {
                bill.copy(
                    usageCount = bill.usageCount + 1,
                    auditPromptCount = bill.auditPromptCount + 1
                )
            } else {
                bill.copy(auditPromptCount = bill.auditPromptCount + 1)
            }

            billDao.updateBill(updatedBill)

            val subscriptions = subscriptionsState.value
            val nextIndex = currentAuditIndex.value + 1
            if (nextIndex >= subscriptions.size) {
                auditSessionComplete.value = true
            } else {
                currentAuditIndex.value = nextIndex
            }
        }
    }

    fun restartAuditSession() {
        currentAuditIndex.value = 0
        auditSessionComplete.value = false
    }

    private fun buildUiState(
        subscriptions: List<BillEntity>,
        auditIndex: Int,
        sessionComplete: Boolean,
        isLoading: Boolean
    ): WeedWhackerUiState {
        val reportRows = subscriptions.map { bill -> bill.toCostPerUseReportRow() }
        val primeWeedIds = reportRows
            .filter { row -> row.isPrimeWeed }
            .map { row -> row.billId }
            .toSet()

        val currentBill = if (!sessionComplete && auditIndex in subscriptions.indices) {
            subscriptions[auditIndex]
        } else {
            null
        }

        val currentAuditCard = currentBill?.let { bill ->
            SubscriptionAuditCard(
                billId = bill.id,
                name = bill.name,
                amount = bill.amount
            )
        }

        val pendingAuditCount = if (sessionComplete) {
            0
        } else {
            (subscriptions.size - auditIndex).coerceAtLeast(0)
        }

        return WeedWhackerUiState(
            currentAuditCard = currentAuditCard,
            pendingAuditCount = pendingAuditCount,
            costPerUseReport = reportRows,
            primeWeedBillIds = primeWeedIds,
            isLoading = isLoading,
            auditSessionComplete = sessionComplete || subscriptions.isEmpty(),
            hasSubscriptions = subscriptions.isNotEmpty()
        )
    }

    private fun BillEntity.toCostPerUseReportRow(): CostPerUseReportRow {
        val safeUsageCount = usageCount.coerceAtLeast(1)
        val costPerUse = amount / safeUsageCount
        val isPrimeWeed = auditPromptCount > PRIME_WEED_AUDIT_THRESHOLD && usageCount == 0

        return CostPerUseReportRow(
            billId = id,
            name = name,
            amount = amount,
            usageCount = usageCount,
            auditPromptCount = auditPromptCount,
            costPerUse = costPerUse,
            isPrimeWeed = isPrimeWeed
        )
    }

    companion object {
        const val PRIME_WEED_AUDIT_THRESHOLD = 2
    }
}
