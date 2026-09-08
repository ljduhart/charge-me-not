package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.domain.model.UserSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PruningViewModel(
    private val billDao: BillDao,
    private val userSettingsRepository: UserSettingsRepository,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val sandboxState = MutableStateFlow<List<BillEntity>>(emptyList())
    private val pruneStates = MutableStateFlow<Map<Long, SandboxPruneState>>(emptyMap())
    private val expandedRootBillId = MutableStateFlow<Long?>(null)
    private val monthlyBudgetState = MutableStateFlow(UserSettings.DEFAULT_MONTHLY_BUDGET)
    private val hasLoadedSnapshot = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(PruningUiState())
    val uiState: StateFlow<PruningUiState> = _uiState.asStateFlow()

    init {
        observeSandboxState()
        coroutineScope.launch(ioDispatcher) {
            loadSnapshotFromRoom()
            userSettingsRepository.observeMonthlyBudget().collect { budget ->
                monthlyBudgetState.value = budget
            }
        }
    }

    private fun observeSandboxState() {
        coroutineScope.launch(ioDispatcher) {
            combine(
                sandboxState,
                pruneStates,
                expandedRootBillId,
                monthlyBudgetState,
                hasLoadedSnapshot
            ) { bills, pruneStateMap, expandedRootId, monthlyBudget, hasLoaded ->
                val prunedIds = pruneStateMap
                    .filter { (_, state) -> state.isPruned }
                    .keys

                buildUiState(
                    bills = bills,
                    prunedIds = prunedIds,
                    expandedRootBillId = expandedRootId,
                    monthlyBudget = monthlyBudget,
                    isLoading = !hasLoaded
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleBillStatus(billId: Long, isPruned: Boolean) {
        val bills = sandboxState.value
        val descendantIds = collectDescendantIds(billId, bills)

        pruneStates.update { current ->
            val updated = current.toMutableMap()
            if (isPruned) {
                val toggledState = updated[billId] ?: SandboxPruneState()
                updated[billId] = toggledState.copy(manualPrune = true)
                descendantIds.forEach { descendantId ->
                    val descendantState = updated[descendantId] ?: SandboxPruneState()
                    updated[descendantId] = descendantState.copy(cascadePrune = true)
                }
            } else {
                val toggledState = updated[billId] ?: SandboxPruneState()
                updated[billId] = toggledState.copy(manualPrune = false, cascadePrune = false)
                descendantIds.forEach { descendantId ->
                    val descendantState = updated[descendantId] ?: SandboxPruneState()
                    if (!descendantState.manualPrune) {
                        updated[descendantId] = descendantState.copy(cascadePrune = false)
                    }
                }
            }
            updated
        }
    }

    fun toggleRootExpansion(billId: Long) {
        expandedRootBillId.update { current ->
            if (current == billId) null else billId
        }
    }

    fun clearRootExpansion() {
        expandedRootBillId.value = null
    }

    fun adjustBillAmount(billId: Long, newAmount: Double) {
        val sanitizedAmount = newAmount.coerceAtLeast(0.0)
        sandboxState.update { bills ->
            bills.map { bill ->
                if (bill.id == billId) {
                    bill.copy(amount = sanitizedAmount)
                } else {
                    bill
                }
            }
        }
    }

    fun resetSandbox() {
        coroutineScope.launch(ioDispatcher) {
            hasLoadedSnapshot.value = false
            expandedRootBillId.value = null
            loadSnapshotFromRoom()
        }
    }

    private suspend fun loadSnapshotFromRoom() {
        val snapshot = billDao.getAllBills()
            .first()
            .filter { bill -> !bill.isPaid }

        sandboxState.value = snapshot
        pruneStates.value = emptyMap()
        hasLoadedSnapshot.value = true
    }

    private fun collectDescendantIds(parentId: Long, bills: List<BillEntity>): Set<Long> {
        return bills
            .filter { bill -> bill.parentBillId == parentId }
            .flatMap { child ->
                setOf(child.id) + collectDescendantIds(child.id, bills)
            }
            .toSet()
    }

    private fun buildUiState(
        bills: List<BillEntity>,
        prunedIds: Set<Long>,
        expandedRootBillId: Long?,
        monthlyBudget: Double,
        isLoading: Boolean
    ): PruningUiState {
        val originalParentCategoryTotals = bills
            .groupBy { bill -> bill.parentCategory }
            .mapValues { (_, categoryBills) -> categoryBills.sumOf { bill -> bill.amount } }

        val activeBills = bills.filter { bill -> bill.id !in prunedIds }
        val projectedParentCategoryTotals = activeBills
            .groupBy { bill -> bill.parentCategory }
            .mapValues { (_, categoryBills) -> categoryBills.sumOf { bill -> bill.amount } }

        val parentCategoryAlphas = MeadowCategories.parentNames.associateWith { parent ->
            val originalAmount = originalParentCategoryTotals[parent] ?: 0.0
            val projectedAmount = projectedParentCategoryTotals[parent] ?: 0.0
            if (originalAmount <= 0.0) {
                1f
            } else {
                (projectedAmount / originalAmount).toFloat().coerceIn(0.12f, 1f)
            }
        }

        val childRelationships = bills
            .filter { bill -> bill.parentBillId != null }
            .groupBy { bill -> bill.parentBillId!! }

        return PruningUiState(
            bills = bills,
            prunedBillIds = prunedIds,
            childRelationships = childRelationships,
            expandedRootBillId = expandedRootBillId,
            originalParentCategoryTotals = originalParentCategoryTotals,
            projectedParentCategoryTotals = projectedParentCategoryTotals,
            parentCategoryAlphas = parentCategoryAlphas,
            newMonthlyTotal = activeBills.sumOf { bill -> bill.amount },
            monthlyBudget = monthlyBudget,
            isLoading = isLoading
        )
    }
}
