package com.artie.chargemenot.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["parentName", "subCategoryName"], unique = true)
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val parentName: String,
    val subCategoryName: String,
    val isCustom: Boolean = false
)
