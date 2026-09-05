package com.artie.chargemenot.data.model

import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.BillCategory
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class CrossPollinationPayload(
    val name: String,
    val amount: Double,
    val dueDate: String,
    val category: String
) {

    fun toJson(): String {
        return JSONObject()
            .put(KEY_APP, APP_IDENTIFIER)
            .put(KEY_VERSION, SCHEMA_VERSION)
            .put(KEY_NAME, name)
            .put(KEY_AMOUNT, amount)
            .put(KEY_DUE_DATE, dueDate)
            .put(KEY_CATEGORY, category)
            .toString()
    }

    fun toBillEntity(): BillEntity? {
        val sanitizedName = name.trim()
        if (sanitizedName.isBlank() || sanitizedName.length > MAX_NAME_LENGTH) {
            return null
        }

        if (amount <= 0.0 || amount > MAX_AMOUNT) {
            return null
        }

        val parsedDueDate = runCatching {
            LocalDate.parse(dueDate.trim(), DATE_FORMATTER)
        }.getOrNull() ?: return null

        val parsedCategory = runCatching {
            BillCategory.valueOf(category.trim().uppercase())
        }.getOrNull() ?: return null

        return BillEntity(
            name = sanitizedName,
            amount = amount,
            dueDate = parsedDueDate,
            category = parsedCategory,
            isPaid = false
        )
    }

    companion object {
        private const val APP_IDENTIFIER = "charge-me-not"
        private const val SCHEMA_VERSION = 1
        private const val MAX_NAME_LENGTH = 120
        private const val MAX_AMOUNT = 1_000_000.0

        private const val KEY_APP = "app"
        private const val KEY_VERSION = "v"
        private const val KEY_NAME = "name"
        private const val KEY_AMOUNT = "amount"
        private const val KEY_DUE_DATE = "dueDate"
        private const val KEY_CATEGORY = "category"

        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        fun fromBillEntity(entity: BillEntity): CrossPollinationPayload {
            return CrossPollinationPayload(
                name = entity.name,
                amount = entity.amount,
                dueDate = entity.dueDate.format(DATE_FORMATTER),
                category = entity.category.name
            )
        }

        fun fromBill(bill: Bill): CrossPollinationPayload {
            return CrossPollinationPayload(
                name = bill.name,
                amount = bill.amount,
                dueDate = bill.dueDate.format(DATE_FORMATTER),
                category = bill.category.name
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
                if (version != SCHEMA_VERSION) {
                    return null
                }

                val name = json.optString(KEY_NAME, "").trim()
                if (name.isBlank() || name.length > MAX_NAME_LENGTH) {
                    return null
                }

                if (!json.has(KEY_AMOUNT) || !json.has(KEY_DUE_DATE) || !json.has(KEY_CATEGORY)) {
                    return null
                }

                val amount = json.getDouble(KEY_AMOUNT)
                if (amount <= 0.0 || amount > MAX_AMOUNT) {
                    return null
                }

                val dueDate = json.getString(KEY_DUE_DATE).trim()
                validateDueDate(dueDate)

                val category = json.getString(KEY_CATEGORY).trim()
                BillCategory.valueOf(category.uppercase())

                CrossPollinationPayload(
                    name = name,
                    amount = amount,
                    dueDate = dueDate,
                    category = category.uppercase()
                )
            }.getOrNull()
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
