package com.artie.chargemenot.data.model

import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.domain.model.BillCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CrossPollinationPayloadTest {

    @Test
    fun toJsonAndFromJson_roundTripBillEntityFields() {
        val entity = BillEntity(
            id = 1L,
            name = "Netflix",
            amount = 15.49,
            dueDate = LocalDate.of(2026, 9, 12),
            category = BillCategory.SUBSCRIPTIONS
        )

        val json = CrossPollinationPayload.fromBillEntity(entity).toJson()
        val decoded = CrossPollinationPayload.fromJson(json)

        assertNotNull(decoded)
        val restored = decoded!!.toBillEntity()
        assertNotNull(restored)
        assertEquals(entity.name, restored!!.name)
        assertEquals(entity.amount, restored.amount, 0.001)
        assertEquals(entity.dueDate, restored.dueDate)
        assertEquals(entity.category, restored.category)
    }

    @Test
    fun fromJson_rejectsInvalidAmount() {
        val json = CrossPollinationPayload(
            name = "Test Bill",
            amount = -5.0,
            dueDate = "2026-09-12",
            category = "FOOD"
        ).toJson()

        assertNull(CrossPollinationPayload.fromJson(json))
    }

    @Test
    fun fromJson_rejectsUnknownCategory() {
        val payload = """
            {"app":"charge-me-not","v":1,"name":"Test","amount":10.0,"dueDate":"2026-09-12","category":"INVALID"}
        """.trimIndent()

        assertNull(CrossPollinationPayload.fromJson(payload))
    }

    @Test
    fun fromJson_rejectsWrongAppIdentifier() {
        val payload = """
            {"app":"other-app","v":1,"name":"Test","amount":10.0,"dueDate":"2026-09-12","category":"FOOD"}
        """.trimIndent()

        assertNull(CrossPollinationPayload.fromJson(payload))
    }

    @Test
    fun toBillEntity_rejectsBlankName() {
        val payload = CrossPollinationPayload(
            name = "   ",
            amount = 12.0,
            dueDate = "2026-09-12",
            category = "FOOD"
        )

        assertNull(payload.toBillEntity())
    }

    @Test
    fun toJson_containsSchemaMetadata() {
        val json = CrossPollinationPayload(
            name = "PG&E",
            amount = 94.17,
            dueDate = "2026-09-08",
            category = "UTILITIES"
        ).toJson()

        assertTrue(json.contains("\"app\":\"charge-me-not\""))
        assertTrue(json.contains("\"v\":1"))
    }
}
