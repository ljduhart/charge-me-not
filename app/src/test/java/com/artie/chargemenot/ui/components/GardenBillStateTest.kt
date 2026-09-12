package com.artie.chargemenot.ui.components

import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
            amount = 8_500L,
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
    fun resolveGardenBillState_marksExactlySevenDaysOutAsUpcoming() {
        val state = resolveGardenBillState(
            bill = sampleBill(isPaid = false, dueDate = today.plusDays(7)),
            today = today
        )

        assertEquals(GardenBillState.Upcoming, state)
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

    @Test
    fun stemXAt_returnsCenterWhenSegmentHeightIsNotPositive() {
        assertEquals(
            200f,
            stemXAt(centerX = 200f, y = 50f, amplitude = 80f, segmentHeight = 0f)
        )
        assertEquals(
            200f,
            stemXAt(centerX = 200f, y = 50f, amplitude = 80f, segmentHeight = -16f)
        )
        assertEquals(
            200f,
            stemXAt(centerX = 200f, y = 50f, amplitude = 80f, segmentHeight = Float.NaN)
        )
        assertEquals(
            200f,
            stemXAt(
                centerX = 200f,
                y = 50f,
                amplitude = 80f,
                segmentHeight = Float.POSITIVE_INFINITY
            )
        )
    }

    @Test
    fun stemXAt_weavesAwayFromTheCenterLine() {
        val centerX = 200f
        val offsetAtFirstBend = stemXAt(
            centerX = centerX,
            y = 160f,
            amplitude = 80f,
            segmentHeight = 160f
        )

        assertTrue(kotlin.math.abs(offsetAtFirstBend - centerX) > 1f)
    }

    @Test
    fun leafCornerRadiiForIndex_usesAsymmetricLeftAndRightLeaves() {
        val leftLeaf = leafCornerRadiiForIndex(0)
        val rightLeaf = leafCornerRadiiForIndex(1)

        assertEquals(0f, leftLeaf.topStartDp)
        assertEquals(48f, leftLeaf.topEndDp)
        assertEquals(0f, leftLeaf.bottomEndDp)
        assertEquals(48f, leftLeaf.bottomStartDp)

        assertEquals(48f, rightLeaf.topStartDp)
        assertEquals(0f, rightLeaf.topEndDp)
        assertEquals(48f, rightLeaf.bottomEndDp)
        assertEquals(0f, rightLeaf.bottomStartDp)
    }

    @Test
    fun offshootTipX_reachesLeftAndRightCardsFromTheStem() {
        assertEquals(80f, offshootTipX(stemX = 200f, branchLeft = true, branchLength = 120f))
        assertEquals(320f, offshootTipX(stemX = 200f, branchLeft = false, branchLength = 120f))
    }

    @Test
    fun gardenBillAmounts_areFormattedFromLongCents() {
        val formatted = CurrencyFormatter.format(8_500L, SupportedCurrency.USD)

        assertTrue(formatted.contains("85"))
        assertEquals(
            formatted,
            CurrencyFormatter.format(sampleBill().amount, SupportedCurrency.USD)
        )
    }
}
