package com.artie.chargemenot.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4
@Entity(tableName = "compost_table")
data class CompostEntity(
    @ColumnInfo(name = "billId")
    val billId: Long,
    @ColumnInfo(name = "rawText")
    val rawText: String
)
