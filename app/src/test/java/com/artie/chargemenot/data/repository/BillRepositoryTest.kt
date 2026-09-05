package com.artie.chargemenot.data.repository

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.BillCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class BillRepositoryTest {

    @Test
    fun linkBillToParent_rejectsCycles() = runTest {
        val today = LocalDate.of(2026, 9, 5)
        val dao = InMemoryBillDao(
            listOf(
                bill(id = 1L, name = "Car Payment"),
                bill(id = 2L, name = "Car Insurance", parentBillId = 1L),
                bill(id = 3L, name = "Roadside Assistance", parentBillId = 2L)
            )
        )
        val repository = BillRepository(dao)

        repository.linkBillToParent(childBillId = 1L, parentBillId = 3L)

        assertNull(dao.snapshot().first { it.id == 1L }.parentBillId)
    }

    @Test
    fun linkBillToParent_persistsValidParent() = runTest {
        val dao = InMemoryBillDao(
            listOf(
                bill(id = 1L, name = "Car Payment"),
                bill(id = 2L, name = "Car Insurance")
            )
        )
        val repository = BillRepository(dao)

        repository.linkBillToParent(childBillId = 2L, parentBillId = 1L)

        assertEquals(1L, dao.snapshot().first { it.id == 2L }.parentBillId)
    }

    @Test
    fun deleteBill_orphansChildren() = runTest {
        val dao = InMemoryBillDao(
            listOf(
                bill(id = 1L, name = "Car Payment"),
                bill(id = 2L, name = "Car Insurance", parentBillId = 1L),
                bill(id = 3L, name = "Roadside Assistance", parentBillId = 2L)
            )
        )
        val repository = BillRepository(dao)

        repository.deleteBill(
            Bill(
                id = 1L,
                name = "Car Payment",
                amount = 450.0,
                dueDate = LocalDate.of(2026, 9, 10),
                category = BillCategory.TRANSPORTATION
            )
        )

        val remaining = dao.snapshot()
        assertEquals(2, remaining.size)
        assertNull(remaining.first { it.id == 2L }.parentBillId)
        assertEquals(2L, remaining.first { it.id == 3L }.parentBillId)
    }

    private fun bill(
        id: Long,
        name: String,
        parentBillId: Long? = null
    ): BillEntity {
        return BillEntity(
            id = id,
            name = name,
            amount = 100.0,
            dueDate = LocalDate.of(2026, 9, 10),
            category = BillCategory.SUBSCRIPTIONS,
            parentBillId = parentBillId
        )
    }

    private class InMemoryBillDao(
        seed: List<BillEntity>
    ) : BillDao {
        private val bills = MutableStateFlow(seed)

        fun snapshot(): List<BillEntity> = bills.value

        override fun getAllBills(): Flow<List<BillEntity>> = bills

        override fun getUpcomingBills(today: LocalDate): Flow<List<BillEntity>> = bills

        override fun getBillById(billId: Long): Flow<BillEntity?> =
            MutableStateFlow(bills.value.firstOrNull { it.id == billId })

        override suspend fun insertBill(bill: BillEntity): Long {
            bills.value = bills.value + bill
            return bill.id
        }

        override suspend fun updateBill(bill: BillEntity) {
            bills.value = bills.value.map { existing ->
                if (existing.id == bill.id) bill else existing
            }
        }

        override suspend fun deleteBill(bill: BillEntity) {
            bills.value = bills.value.filterNot { it.id == bill.id }
        }

        override suspend fun deleteBillById(billId: Long) {
            bills.value = bills.value.filterNot { it.id == billId }
        }

        override suspend fun getBillCount(): Int = bills.value.size

        override fun getActiveSubscriptions(): Flow<List<BillEntity>> = bills

        override suspend fun getBillByIdOnce(billId: Long): BillEntity? =
            bills.value.firstOrNull { it.id == billId }

        override suspend fun getOverdueOrDueTodayUnpaidBillCount(today: LocalDate): Int = 0

        override fun getChildrenForParent(parentId: Long): Flow<List<BillEntity>> =
            MutableStateFlow(bills.value.filter { it.parentBillId == parentId })
    }
}
