package com.artie.chargemenot.data.local

import androidx.room.Embedded

data class BillWithCompost(
    @Embedded
    val bill: BillEntity,
    val matchedText: String
)
