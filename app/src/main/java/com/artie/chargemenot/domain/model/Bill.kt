package com.artie.chargemenot.domain.model

import java.time.LocalDate

data class Bill(
    val id: Long = 0L,
    val name: String,
    val amount: Double,
    val dueDate: LocalDate,
    val category: BillCategory,
    val isPaid: Boolean = false
)
