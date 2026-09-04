package com.artie.chargemenot.data.local

import androidx.room.TypeConverter
import com.artie.chargemenot.domain.model.BillCategory
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromBillCategory(category: BillCategory?): String? = category?.name

    @TypeConverter
    fun toBillCategory(value: String?): BillCategory? =
        value?.let { enumValueOf<BillCategory>(it) }
}
