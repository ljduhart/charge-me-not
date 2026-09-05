package com.artie.chargemenot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CompostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompost(compost: CompostEntity)

    @Query("DELETE FROM compost_table WHERE billId = :billId")
    suspend fun deleteCompostForBill(billId: Long)
}
