package com.artie.chargemenot.data.repository

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.data.local.BillWithCompost
import com.artie.chargemenot.data.local.CompostDao
import com.artie.chargemenot.data.local.CompostEntity
import com.artie.chargemenot.data.local.toDomain
import com.artie.chargemenot.data.local.toEntity
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.util.FtsQuerySanitizer
import com.artie.chargemenot.util.ImageStorageUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class BillRepository(
    private val billDao: BillDao,
    private val compostDao: CompostDao
) {

    fun getAllBills(): Flow<List<Bill>> =
        billDao.getAllBills().map { entities -> entities.map { it.toDomain() } }

    fun getUpcomingBills(today: LocalDate = LocalDate.now()): Flow<List<Bill>> =
        billDao.getUpcomingBills(today).map { entities -> entities.map { it.toDomain() } }

    fun getBillsByCategory(category: String): Flow<List<Bill>> =
        billDao.getBillsByCategory(category).map { entities -> entities.map { it.toDomain() } }

    suspend fun insertBill(bill: Bill): Long =
        billDao.insertBill(bill.toEntity())

    suspend fun insertScannedBill(bill: Bill, rawText: String): Long {
        val billId = billDao.insertBill(bill.toEntity())
        val trimmedText = rawText.trim()
        if (trimmedText.isNotEmpty()) {
            compostDao.insertCompost(
                CompostEntity(
                    billId = billId,
                    rawText = trimmedText
                )
            )
        }
        return billId
    }

    suspend fun updateBill(bill: Bill) {
        billDao.updateBill(bill.toEntity())
    }

    suspend fun deleteBill(bill: Bill) {
        val entity = bill.toEntity()
        orphanChildrenOf(entity.id)
        compostDao.deleteCompostForBill(entity.id)
        ImageStorageUtil.deleteReceiptImage(entity.receiptImagePath)
        billDao.deleteBill(entity)
    }

    suspend fun deleteBillById(billId: Long) {
        val existing = billDao.getBillByIdOnce(billId)
        orphanChildrenOf(billId)
        compostDao.deleteCompostForBill(billId)
        ImageStorageUtil.deleteReceiptImage(existing?.receiptImagePath)
        billDao.deleteBillById(billId)
    }

    private suspend fun orphanChildrenOf(deletedParentId: Long) {
        val children = billDao.getAllBills()
            .first()
            .filter { bill -> bill.parentBillId == deletedParentId }

        children.forEach { child ->
            billDao.updateBill(child.copy(parentBillId = null))
        }
    }

    fun getChildrenForParent(parentId: Long): Flow<List<Bill>> =
        billDao.getChildrenForParent(parentId).map { entities -> entities.map { it.toDomain() } }

    fun deleteOrphanReceiptImage(receiptImagePath: String?) {
        ImageStorageUtil.deleteReceiptImage(receiptImagePath)
    }

    fun searchCompost(rawQuery: String): Flow<List<BillWithCompost>> {
        val sanitizedQuery = FtsQuerySanitizer.sanitize(rawQuery)
        if (sanitizedQuery.isBlank()) {
            return flowOf(emptyList())
        }
        return billDao.searchCompost(sanitizedQuery)
    }

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
