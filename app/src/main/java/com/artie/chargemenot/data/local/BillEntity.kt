package com.artie.chargemenot.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.artie.chargemenot.domain.model.BillCategory
import java.time.LocalDate

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val amount: Double,
    val dueDate: LocalDate,
    val category: BillCategory,
    val isPaid: Boolean = false
)
