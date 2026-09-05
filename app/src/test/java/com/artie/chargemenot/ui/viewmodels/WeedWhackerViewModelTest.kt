package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.domain.model.BillCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeedWhackerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun recordAuditResponse_used_incrementsUsageAndAuditCounts() {
        val billDao = FakeBillDao(
            initialBills = listOf(
                subscriptionBill(id = 1L, name = "Netflix")
            )
        )
        val viewModel = WeedWhackerViewModel(
            billDao = billDao,
            coroutineScope = testScope,
            ioDispatcher = testDispatcher
        )
        testScope.advanceUntilIdle()

        viewModel.recordAuditResponse(billId = 1L, used = true)
        testScope.advanceUntilIdle()

        val updated = billDao.getBillSnapshot(1L)
        assertEquals(1, updated?.usageCount)
        assertEquals(1, updated?.auditPromptCount)
        assertTrue(viewModel.uiState.value.auditSessionComplete)
    }

    @Test
    fun recordAuditResponse_notUsed_incrementsAuditCountOnly() {
        val billDao = FakeBillDao(
            initialBills = listOf(
                subscriptionBill(id = 1L, name = "Spotify Premium")
            )
        )
        val viewModel = WeedWhackerViewModel(
            billDao = billDao,
            coroutineScope = testScope,
            ioDispatcher = testDispatcher
        )
        testScope.advanceUntilIdle()

        viewModel.recordAuditResponse(billId = 1L, used = false)
        testScope.advanceUntilIdle()

        val updated = billDao.getBillSnapshot(1L)
        assertEquals(0, updated?.usageCount)
        assertEquals(1, updated?.auditPromptCount)
    }

    @Test
    fun uiState_flagsPrimeWeedsWhenAuditPromptsExceedThresholdWithoutUsage() {
        val billDao = FakeBillDao(
            initialBills = listOf(
                subscriptionBill(
                    id = 1L,
                    name = "Disney+",
                    usageCount = 0,
                    auditPromptCount = 3
                )
            )
        )
        val viewModel = WeedWhackerViewModel(
            billDao = billDao,
            coroutineScope = testScope,
            ioDispatcher = testDispatcher
        )
        testScope.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.primeWeedBillIds.contains(1L))
        val reportRow = state.costPerUseReport.first()
        assertTrue(reportRow.isPrimeWeed)
        assertEquals(13.99, reportRow.costPerUse, 0.001)
    }

    @Test
    fun uiState_calculatesCostPerUseWithMinimumDivisorOfOne() {
        val billDao = FakeBillDao(
            initialBills = listOf(
                subscriptionBill(
                    id = 2L,
                    name = "Adobe Creative Cloud",
                    amount = 54.99,
                    usageCount = 0,
                    auditPromptCount = 0
                )
            )
        )
        val viewModel = WeedWhackerViewModel(
            billDao = billDao,
            coroutineScope = testScope,
            ioDispatcher = testDispatcher
        )
        testScope.advanceUntilIdle()

        val reportRow = viewModel.uiState.value.costPerUseReport.first()
        assertEquals(54.99, reportRow.costPerUse, 0.001)
        assertFalse(reportRow.isPrimeWeed)
    }

    @Test
    fun recordAuditResponse_ignoresConcurrentDuplicateTaps() {
        val billDao = FakeBillDao(
            initialBills = listOf(
                subscriptionBill(id = 1L, name = "Netflix"),
                subscriptionBill(id = 2L, name = "Spotify Premium")
            ),
            updateDelayMs = 50L
        )
        val viewModel = WeedWhackerViewModel(
            billDao = billDao,
            coroutineScope = testScope,
            ioDispatcher = testDispatcher
        )
        testScope.advanceUntilIdle()

        viewModel.recordAuditResponse(billId = 1L, used = true)
        viewModel.recordAuditResponse(billId = 1L, used = true)
        testScope.advanceUntilIdle()

        val updated = billDao.getBillSnapshot(1L)
        assertEquals(1, updated?.usageCount)
        assertEquals(1, updated?.auditPromptCount)
        assertEquals("Spotify Premium", viewModel.uiState.value.currentAuditCard?.name)
    }

    @Test
    fun reconcileAuditIndex_marksSessionCompleteWhenSubscriptionsShrink() {
        val billDao = FakeBillDao(
            initialBills = listOf(
                subscriptionBill(id = 1L, name = "Netflix"),
                subscriptionBill(id = 2L, name = "Spotify Premium")
            )
        )
        val viewModel = WeedWhackerViewModel(
            billDao = billDao,
            coroutineScope = testScope,
            ioDispatcher = testDispatcher
        )
        testScope.advanceUntilIdle()

        viewModel.recordAuditResponse(billId = 1L, used = true)
        testScope.advanceUntilIdle()

        billDao.setSubscriptions(
            listOf(subscriptionBill(id = 2L, name = "Spotify Premium"))
        )
        testScope.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.auditSessionComplete)
    }

    @Test
    fun restartAuditSession_resetsCurrentAuditCard() {
        val billDao = FakeBillDao(
            initialBills = listOf(
                subscriptionBill(id = 1L, name = "Netflix"),
                subscriptionBill(id = 2L, name = "Spotify Premium")
            )
        )
        val viewModel = WeedWhackerViewModel(
            billDao = billDao,
            coroutineScope = testScope,
            ioDispatcher = testDispatcher
        )
        testScope.advanceUntilIdle()

        viewModel.recordAuditResponse(billId = 1L, used = true)
        testScope.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.currentAuditCard)

        viewModel.restartAuditSession()
        testScope.advanceUntilIdle()

        assertEquals("Netflix", viewModel.uiState.value.currentAuditCard?.name)
        assertFalse(viewModel.uiState.value.auditSessionComplete)
        assertEquals(2, viewModel.uiState.value.pendingAuditCount)
    }

    private fun subscriptionBill(
        id: Long,
        name: String,
        amount: Double = 13.99,
        usageCount: Int = 0,
        auditPromptCount: Int = 0
    ): BillEntity {
        return BillEntity(
            id = id,
            name = name,
            amount = amount,
            dueDate = LocalDate.of(2026, 9, 20),
            category = BillCategory.SUBSCRIPTIONS,
            isPaid = false,
            usageCount = usageCount,
            auditPromptCount = auditPromptCount
        )
    }

    private open class FakeBillDao(
        initialBills: List<BillEntity>,
        private val updateDelayMs: Long = 0L
    ) : BillDao {

        private val bills = MutableStateFlow(initialBills)

        fun setSubscriptions(updatedBills: List<BillEntity>) {
            bills.value = updatedBills
        }

        fun getBillSnapshot(billId: Long): BillEntity? =
            bills.value.firstOrNull { bill -> bill.id == billId }

        override fun getAllBills(): Flow<List<BillEntity>> = bills

        override fun getUpcomingBills(today: LocalDate): Flow<List<BillEntity>> = bills

        override fun getBillById(billId: Long): Flow<BillEntity?> =
            bills.map { items -> items.firstOrNull { bill -> bill.id == billId } }

        override suspend fun insertBill(bill: BillEntity): Long {
            bills.value = bills.value + bill
            return bill.id
        }

        override suspend fun updateBill(bill: BillEntity) {
            if (updateDelayMs > 0L) {
                kotlinx.coroutines.delay(updateDelayMs)
            }
            bills.value = bills.value.map { existing ->
                if (existing.id == bill.id) bill else existing
            }
        }

        override suspend fun deleteBill(bill: BillEntity) {
            bills.value = bills.value.filterNot { existing -> existing.id == bill.id }
        }

        override suspend fun deleteBillById(billId: Long) {
            bills.value = bills.value.filterNot { existing -> existing.id == billId }
        }

        override suspend fun getBillCount(): Int = bills.value.size

        override fun getActiveSubscriptions(): Flow<List<BillEntity>> =
            bills.map { items ->
                items.filter { bill ->
                    bill.category == BillCategory.SUBSCRIPTIONS && !bill.isPaid
                }
            }

        override suspend fun getBillByIdOnce(billId: Long): BillEntity? =
            bills.value.firstOrNull { bill -> bill.id == billId }

        override suspend fun getOverdueOrDueTodayUnpaidBillCount(today: LocalDate): Int = 0
    }
}
