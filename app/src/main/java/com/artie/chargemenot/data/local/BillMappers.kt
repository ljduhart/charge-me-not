package com.artie.chargemenot.data.local

import com.artie.chargemenot.domain.model.Bill

fun BillEntity.toDomain(): Bill = Bill(
    id = id,
    name = name,
    amount = amount,
    dueDate = dueDate,
    parentCategory = parentCategory,
    subCategory = subCategory,
    isPaid = isPaid,
    usageCount = usageCount,
    auditPromptCount = auditPromptCount,
    parentBillId = parentBillId,
    receiptImagePath = receiptImagePath
)

fun Bill.toEntity(): BillEntity = BillEntity(
    id = id,
    name = name,
    amount = amount,
    dueDate = dueDate,
    parentCategory = parentCategory,
    subCategory = subCategory,
    isPaid = isPaid,
    usageCount = usageCount,
    auditPromptCount = auditPromptCount,
    parentBillId = parentBillId,
    receiptImagePath = receiptImagePath
)
