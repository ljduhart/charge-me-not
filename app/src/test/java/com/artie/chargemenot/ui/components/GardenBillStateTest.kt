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
    fun resolveGardenBillState_marksFutureUnpaidBillsAsUpcoming() {
        val state = resolveGardenBillState(
            bill = sampleBill(isPaid = false, dueDate = today.plusDays(5)),
            today = today
        )

        assertEquals(GardenBillState.Upcoming, state)
    }

    @Test
    fun gardenBillState_timelineOrder_sortsOverdueBeforeUpcomingBeforePaid() {
        assertEquals(0, GardenBillState.Overdue.timelineOrder)
        assertEquals(1, GardenBillState.Upcoming.timelineOrder)
        assertEquals(2, GardenBillState.Paid.timelineOrder)
    }
}
