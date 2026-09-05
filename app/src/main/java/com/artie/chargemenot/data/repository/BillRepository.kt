package com.artie.chargemenot.data.repository

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.toDomain
import com.artie.chargemenot.data.local.toEntity
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.data.local.BillEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class BillRepository(private val billDao: BillDao) {

    fun getAllBills(): Flow<List<Bill>> =
        billDao.getAllBills().map { entities -> entities.map { it.toDomain() } }

    fun getUpcomingBills(today: LocalDate = LocalDate.now()): Flow<List<Bill>> =
        billDao.getUpcomingBills(today).map { entities -> entities.map { it.toDomain() } }

    suspend fun insertBill(bill: Bill): Long =
        billDao.insertBill(bill.toEntity())

    suspend fun updateBill(bill: Bill) {
        billDao.updateBill(bill.toEntity())
    }

    suspend fun deleteBill(bill: Bill) {
        billDao.deleteBill(bill.toEntity())
    }

    suspend fun deleteBillById(billId: Long) {
        billDao.deleteBillById(billId)
    }

    fun getChildrenForParent(parentId: Long): Flow<List<Bill>> =
        billDao.getChildrenForParent(parentId).map { entities -> entities.map { it.toDomain() } }

    suspend fun linkBillToParent(childBillId: Long, parentBillId: Long?) {
        val allBills = billDao.getAllBills().first()
        val childBill = allBills.firstOrNull { bill -> bill.id == childBillId } ?: return

        if (parentBillId != null) {
            if (parentBillId == childBillId) return
            if (allBills.none { bill -> bill.id == parentBillId }) return
            if (wouldCreateCycle(childBillId, parentBillId, allBills)) return
        }

        billDao.updateBill(childBill.copy(parentBillId = parentBillId))
    }

    private fun wouldCreateCycle(
        childBillId: Long,
        proposedParentId: Long,
        bills: List<BillEntity>
    ): Boolean {
        val billById = bills.associateBy { bill -> bill.id }
        var currentParentId: Long? = proposedParentId
        while (currentParentId != null) {
            if (currentParentId == childBillId) {
                return true
            }
            currentParentId = billById[currentParentId]?.parentBillId
        }
        return false
    }
}
