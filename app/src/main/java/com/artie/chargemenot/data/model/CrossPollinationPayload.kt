package com.artie.chargemenot.data.model

import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.domain.model.MeadowCategories
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.roundToLong

data class CrossPollinationPayload(
    val name: String,
    val amount: Long,
    val dueDate: String,
    val parentCategory: String,
    val subCategory: String
) {

    fun toJson(): String {
        return JSONObject()
            .put(KEY_APP, APP_IDENTIFIER)
            .put(KEY_VERSION, SCHEMA_VERSION)
            .put(KEY_NAME, name)
            .put(KEY_AMOUNT, amount)
            .put(KEY_DUE_DATE, dueDate)
            .put(KEY_PARENT_CATEGORY, parentCategory)
            .put(KEY_SUB_CATEGORY, subCategory)
            .toString()
    }

    fun toBillEntity(): BillEntity? {
        val sanitizedName = name.trim()
        if (sanitizedName.isBlank() || sanitizedName.length > MAX_NAME_LENGTH) {
            return null
        }

        if (amount <= 0L || amount > MAX_AMOUNT_CENTS) {
            return null
        }

        val parsedDueDate = runCatching {
            LocalDate.parse(dueDate.trim(), DATE_FORMATTER)
        }.getOrNull() ?: return null

        val trimmedParent = parentCategory.trim()
        val trimmedSub = subCategory.trim()
        if (trimmedParent.isBlank() || trimmedSub.isBlank()) {
            return null
        }

        return BillEntity(
            name = sanitizedName,
            amount = amount,
            dueDate = parsedDueDate,
            parentCategory = trimmedParent,
            subCategory = trimmedSub,
            isPaid = false
        )
    }

    companion object {
        private const val APP_IDENTIFIER = "charge-me-not"
        private const val SCHEMA_VERSION = 3
        private const val MAX_NAME_LENGTH = 120
        private const val MAX_AMOUNT_CENTS = 100_000_000L

        private const val KEY_APP = "app"
        private const val KEY_VERSION = "v"
        private const val KEY_NAME = "name"
        private const val KEY_AMOUNT = "amount"
        private const val KEY_DUE_DATE = "dueDate"
        private const val KEY_CATEGORY = "category"
        private const val KEY_PARENT_CATEGORY = "parentCategory"
        private const val KEY_SUB_CATEGORY = "subCategory"

        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        fun fromBillEntity(entity: BillEntity): CrossPollinationPayload {
            return CrossPollinationPayload(
                name = entity.name,
                amount = entity.amount,
                dueDate = entity.dueDate.format(DATE_FORMATTER),
                parentCategory = entity.parentCategory,
                subCategory = entity.subCategory
            )
        }

        fun fromBill(bill: Bill): CrossPollinationPayload {
            return CrossPollinationPayload(
                name = bill.name,
                amount = bill.amount,
                dueDate = bill.dueDate.format(DATE_FORMATTER),
                parentCategory = bill.parentCategory,
                subCategory = bill.subCategory
            )
        }

        fun fromJson(rawJson: String): CrossPollinationPayload? {
            return runCatching {
                val json = JSONObject(rawJson.trim())
                val app = json.optString(KEY_APP, "")
                if (app.isNotBlank() && app != APP_IDENTIFIER) {
                    return null
                }

                val version = json.optInt(KEY_VERSION, SCHEMA_VERSION)
                if (version !in 1..SCHEMA_VERSION) {
                    return null
                }

                val name = json.optString(KEY_NAME, "").trim()
                if (name.isBlank() || name.length > MAX_NAME_LENGTH) {
                    return null
                }

                if (!json.has(KEY_AMOUNT) || !json.has(KEY_DUE_DATE)) {
                    return null
                }

                val amount = parseAmountCents(json, version)
                if (amount <= 0L || amount > MAX_AMOUNT_CENTS) {
                    return null
                }

                val dueDate = json.getString(KEY_DUE_DATE).trim()
                validateDueDate(dueDate)

                if (version >= 2) {
                    if (!json.has(KEY_PARENT_CATEGORY) || !json.has(KEY_SUB_CATEGORY)) {
                        return null
                    }
                    val parentCategory = json.getString(KEY_PARENT_CATEGORY).trim()
                    val subCategory = json.getString(KEY_SUB_CATEGORY).trim()
                    if (parentCategory.isBlank() || subCategory.isBlank()) {
                        return null
                    }
                    CrossPollinationPayload(
                        name = name,
                        amount = amount,
                        dueDate = dueDate,
                        parentCategory = parentCategory,
                        subCategory = subCategory
                    )
                } else {
                    val category = json.getString(KEY_CATEGORY).trim()
                    val taxonomy = MeadowCategories.legacyBillCategoryToTaxonomy(
                        BillCategory.valueOf(category.uppercase())
                    )
                    CrossPollinationPayload(
                        name = name,
                        amount = amount,
                        dueDate = dueDate,
                        parentCategory = taxonomy.first,
                        subCategory = taxonomy.second
                    )
                }
            }.getOrNull()
        }

        private fun parseAmountCents(json: JSONObject, version: Int): Long {
            return if (version >= SCHEMA_VERSION) {
                json.getLong(KEY_AMOUNT)
            } else {
                val dollars = json.getDouble(KEY_AMOUNT)
                if (!dollars.isFinite()) {
                    0L
                } else {
                    (dollars * 100.0).roundToLong()
                }
            }
        }

        private fun validateDueDate(dueDate: String) {
            try {
                LocalDate.parse(dueDate, DATE_FORMATTER)
            } catch (_: DateTimeParseException) {
                throw IllegalArgumentException("Invalid due date")
            }
        }
    }
}
