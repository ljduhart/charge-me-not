package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.BillCategory
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
    private val prunedBillIds = MutableStateFlow<Set<Long>>(emptySet())
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
                prunedBillIds,
                monthlyBudgetState,
                hasLoadedSnapshot
            ) { bills, prunedIds, monthlyBudget, hasLoaded ->
                buildUiState(
                    bills = bills,
                    prunedIds = prunedIds,
                    monthlyBudget = monthlyBudget,
                    isLoading = !hasLoaded
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleBillStatus(billId: Long, isPruned: Boolean) {
        prunedBillIds.update { current ->
            if (isPruned) {
                current + billId
            } else {
                current - billId
            }
        }
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
            loadSnapshotFromRoom()
        }
    }

    private suspend fun loadSnapshotFromRoom() {
        val snapshot = billDao.getAllBills()
            .first()
            .filter { bill -> !bill.isPaid }

        sandboxState.value = snapshot
        prunedBillIds.value = emptySet()
        hasLoadedSnapshot.value = true
    }

    private fun buildUiState(
        bills: List<BillEntity>,
        prunedIds: Set<Long>,
        monthlyBudget: Double,
        isLoading: Boolean
    ): PruningUiState {
        val originalCategoryTotals = bills
            .groupBy { bill -> bill.category }
            .mapValues { (_, categoryBills) -> categoryBills.sumOf { bill -> bill.amount } }

        val activeBills = bills.filter { bill -> bill.id !in prunedIds }
        val projectedCategoryTotals = activeBills
            .groupBy { bill -> bill.category }
            .mapValues { (_, categoryBills) -> categoryBills.sumOf { bill -> bill.amount } }

        val categoryAlphas = BillCategory.entries.associateWith { category ->
            val originalAmount = originalCategoryTotals[category] ?: 0.0
            val projectedAmount = projectedCategoryTotals[category] ?: 0.0
            if (originalAmount <= 0.0) {
                1f
            } else {
                (projectedAmount / originalAmount).toFloat().coerceIn(0.12f, 1f)
            }
        }

        return PruningUiState(
            bills = bills,
            prunedBillIds = prunedIds,
            originalCategoryTotals = originalCategoryTotals,
            projectedCategoryTotals = projectedCategoryTotals,
            categoryAlphas = categoryAlphas,
            newMonthlyTotal = activeBills.sumOf { bill -> bill.amount },
            monthlyBudget = monthlyBudget,
            isLoading = isLoading
        )
    }
}
