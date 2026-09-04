package com.artie.chargemenot.data.local

import com.artie.chargemenot.domain.model.Bill

fun BillEntity.toDomain(): Bill = Bill(
    id = id,
    name = name,
    amount = amount,
    dueDate = dueDate,
    category = category,
    isPaid = isPaid
)

fun Bill.toEntity(): BillEntity = BillEntity(
    id = id,
    name = name,
    amount = amount,
    dueDate = dueDate,
    category = category,
    isPaid = isPaid
)
