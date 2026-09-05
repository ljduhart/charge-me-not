package com.artie.chargemenot.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface BillDao {

    @Query("SELECT * FROM bills ORDER BY dueDate ASC")
    fun getAllBills(): Flow<List<BillEntity>>

    @Query(
        """
        SELECT * FROM bills
        WHERE isPaid = 0 AND dueDate >= :today
        ORDER BY dueDate ASC
        """
    )
    fun getUpcomingBills(today: LocalDate = LocalDate.now()): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE id = :billId")
    fun getBillById(billId: Long): Flow<BillEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity): Long

    @Update
    suspend fun updateBill(bill: BillEntity)

    @Delete
    suspend fun deleteBill(bill: BillEntity)

    @Query("DELETE FROM bills WHERE id = :billId")
    suspend fun deleteBillById(billId: Long)

    @Query("SELECT COUNT(*) FROM bills")
    suspend fun getBillCount(): Int

    @Query(
        """
        SELECT COUNT(*) FROM bills
        WHERE isPaid = 0 AND dueDate <= :today
        """
    )
    suspend fun getOverdueOrDueTodayUnpaidBillCount(today: LocalDate = LocalDate.now()): Int
}
