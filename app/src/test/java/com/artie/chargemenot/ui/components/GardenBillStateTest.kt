package com.artie.chargemenot.ui.components

import com.artie.chargemenot.domain.model.Bill
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GardenBillStateTest {

    private val today = LocalDate.of(2026, 9, 10)

    private fun sampleBill(
        isPaid: Boolean = false,
        dueDate: LocalDate = today
    ): Bill {
        return Bill(
            id = 1L,
            name = "Electric",
            amount = 85.0,
            dueDate = dueDate,
            parentCategory = "Utilities",
            subCategory = "Power",
            isPaid = isPaid
        )
    }

    @Test
    fun resolveGardenBillState_marksPaidBillsAsPaid() {
        val state = resolveGardenBillState(
            bill = sampleBill(isPaid = true, dueDate = today.minusDays(3)),
            today = today
        )

        assertEquals(GardenBillState.Paid, state)
    }

    @Test
    fun resolveGardenBillState_marksPastDueUnpaidBillsAsOverdue() {
        val state = resolveGardenBillState(
            bill = sampleBill(isPaid = false, dueDate = today.minusDays(1)),
            today = today
        )

        assertEquals(GardenBillState.Overdue, state)
    }

    @Test
    fun resolveGardenBillState_marksDueTodayUnpaidBillsAsUpcoming() {
        val state = resolveGardenBillState(
            bill = sampleBill(isPaid = false, dueDate = today),
            today = today
        )

        assertEquals(GardenBillState.Upcoming, state)
    }

    @Test
    fun resolveGardenBillState_marksNearFutureUnpaidBillsAsUpcoming() {
        val state = resolveGardenBillState(
            bill = sampleBill(isPaid = false, dueDate = today.plusDays(5)),
            today = today
        )

        assertEquals(GardenBillState.Upcoming, state)
    }

    @Test
    fun resolveGardenBillState_marksDistantFutureUnpaidBillsAsFarOff() {
        val state = resolveGardenBillState(
            bill = sampleBill(isPaid = false, dueDate = today.plusDays(8)),
            today = today
        )

        assertEquals(GardenBillState.FarOff, state)
    }

    @Test
    fun sortGardenPathBills_ordersChronologicallyByDueDate() {
        val paid = sampleBill(isPaid = true, dueDate = today.minusDays(9)).copy(id = 1L, name = "Paid")
        val overdue = sampleBill(isPaid = false, dueDate = today.minusDays(2)).copy(id = 2L, name = "Overdue")
        val upcomingSoon = sampleBill(isPaid = false, dueDate = today.plusDays(1)).copy(id = 3L, name = "Soon")
        val farOff = sampleBill(isPaid = false, dueDate = today.plusDays(10)).copy(id = 4L, name = "FarOff")

        val sorted = sortGardenPathBills(
            bills = listOf(farOff, upcomingSoon, paid, overdue)
        )

        assertEquals(listOf("Paid", "Overdue", "Soon", "FarOff"), sorted.map { bill -> bill.name })
    }
}
