package com.artie.chargemenot.data.repository

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.toDomain
import com.artie.chargemenot.data.local.toEntity
import com.artie.chargemenot.domain.model.Bill
import kotlinx.coroutines.flow.Flow
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
}
