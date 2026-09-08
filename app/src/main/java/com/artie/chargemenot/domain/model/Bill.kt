package com.artie.chargemenot.domain.model

import java.time.LocalDate

data class Bill(
    val id: Long = 0L,
    val name: String,
    val amount: Double,
    val dueDate: LocalDate,
    val parentCategory: String,
    val subCategory: String,
    val isPaid: Boolean = false,
    val usageCount: Int = 0,
    val auditPromptCount: Int = 0,
    val parentBillId: Long? = null,
    val receiptImagePath: String? = null
)
