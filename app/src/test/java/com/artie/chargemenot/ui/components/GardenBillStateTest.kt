package com.artie.chargemenot.ui.components

import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun sortGardenPathBills_usesBillIdAsAStableTieBreaker() {
        val laterId = sampleBill(dueDate = today).copy(id = 20L, name = "Later Id")
        val earlierId = sampleBill(dueDate = today).copy(id = 8L, name = "Earlier Id")

        val sorted = sortGardenPathBills(listOf(laterId, earlierId))

        assertEquals(listOf("Earlier Id", "Later Id"), sorted.map { bill -> bill.name })
    }

    @Test
    fun gardenPath_projectsOnlyCallerProvidedBillsAndDates() {
        val userBills = listOf(
            Bill(
                id = 41L,
                name = "River Loft Rent",
                amount = 210_050L,
                dueDate = LocalDate.of(2026, 3, 3),
                parentCategory = "Utilities",
                subCategory = "Rent",
                isPaid = false
            ),
            Bill(
                id = 42L,
                name = "Harbor Electric",
                amount = 8_888L,
                dueDate = LocalDate.of(2026, 4, 21),
                parentCategory = "Utilities",
                subCategory = "Power",
                isPaid = true
            )
        )
        val marketingNames = setOf(
            "Maple Street Apartment",
            "Whole Foods Groceries",
            "Trader Joe's",
            "City Water & Sewer",
            "Pacific Gas & Electric",
            "Spotify Premium",
            "Netflix"
        )

        val path = sortGardenPathBills(userBills.shuffled())

        assertEquals(listOf("River Loft Rent", "Harbor Electric"), path.map { bill -> bill.name })
        assertEquals(listOf(210_050L, 8_888L), path.map { bill -> bill.amount })
        assertEquals(
            listOf(LocalDate.of(2026, 3, 3), LocalDate.of(2026, 4, 21)),
            path.map { bill -> bill.dueDate }
        )
        assertEquals(listOf("Mar 3", "Apr 21"), path.map { bill -> gardenBillDetailDate(bill.dueDate) })
        assertEquals(42L, featuredPaidBillIdFrom(userBills.shuffled(), today = LocalDate.of(2026, 3, 10)))
        assertTrue(path.none { bill -> bill.name in marketingNames })
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
        val amplitude = 80f
        val segmentHeight = 160f
        val peakOffset = stemXAt(
            centerX = centerX,
            y = segmentHeight / 2f,
            amplitude = amplitude,
            segmentHeight = segmentHeight
        )
        val crossing = stemXAt(
            centerX = centerX,
            y = segmentHeight,
            amplitude = amplitude,
            segmentHeight = segmentHeight
        )
        val oppositePeak = stemXAt(
            centerX = centerX,
            y = segmentHeight * 1.5f,
            amplitude = amplitude,
            segmentHeight = segmentHeight
        )

        assertEquals(centerX + amplitude, peakOffset, 0.01f)
        assertEquals(centerX, crossing, 0.01f)
        assertEquals(centerX - amplitude, oppositePeak, 0.01f)

        val leftLeafAttach = stemXAt(
            centerX = centerX,
            y = segmentHeight * 1.55f,
            amplitude = amplitude,
            segmentHeight = segmentHeight
        )
        val rightLeafAttach = stemXAt(
            centerX = centerX,
            y = segmentHeight * 2.55f,
            amplitude = amplitude,
            segmentHeight = segmentHeight
        )
        assertTrue(leftLeafAttach < centerX - amplitude * 0.9f)
        assertTrue(rightLeafAttach > centerX + amplitude * 0.9f)
        assertEquals(0f, stemDxDy(y = segmentHeight / 2f, amplitude = amplitude, segmentHeight = segmentHeight), 0.01f)
    }

    @Test
    fun leafCornerPercentsForIndex_usesAsymmetricLeftAndRightLeaves() {
        val leftLeaf = leafCornerPercentsForIndex(0)
        val rightLeaf = leafCornerPercentsForIndex(1)

        assertEquals(0, leftLeaf.topStartPercent)
        assertEquals(100, leftLeaf.topEndPercent)
        assertEquals(0, leftLeaf.bottomEndPercent)
        assertEquals(100, leftLeaf.bottomStartPercent)

        assertEquals(100, rightLeaf.topStartPercent)
        assertEquals(0, rightLeaf.topEndPercent)
        assertEquals(100, rightLeaf.bottomEndPercent)
        assertEquals(0, rightLeaf.bottomStartPercent)
    }

    @Test
    fun leafTipsLeftForIndex_pointsAwayFromTheVine() {
        assertTrue(leafTipsLeftForIndex(0))
        assertFalse(leafTipsLeftForIndex(1))
        assertTrue(leafTipsLeftForIndex(2))
    }

    @Test
    fun organicLeafAnchors_placeTheStemTowardTheVine() {
        val leftLeaf = organicLeafAnchors(tipOnLeft = true)
        val rightLeaf = organicLeafAnchors(tipOnLeft = false)

        assertTrue(leftLeaf.tipXFraction < leftLeaf.stemXFraction)
        assertTrue(rightLeaf.tipXFraction > rightLeaf.stemXFraction)
        assertEquals(0.04f, leftLeaf.tipXFraction, 0.001f)
        assertEquals(0.96f, leftLeaf.stemXFraction, 0.001f)
        assertEquals(
            organicLeafShapeForIndex(0).tipOnLeft,
            true
        )
        assertEquals(
            organicLeafShapeForIndex(1).tipOnLeft,
            false
        )
    }

    @Test
    fun organicLeafChipAlignment_sitsOnTheStemSide() {
        assertEquals(androidx.compose.ui.Alignment.BottomEnd, organicLeafChipAlignment(tipOnLeft = true))
        assertEquals(androidx.compose.ui.Alignment.BottomStart, organicLeafChipAlignment(tipOnLeft = false))
    }

    @Test
    fun gardenLeafVisualFor_usesRoseOnlyForTheFeaturedPaidBloom() {
        assertEquals(
            GardenLeafVisual.FeaturedRose,
            gardenLeafVisualFor(GardenBillState.Paid, isFeaturedPaidRose = true)
        )
        assertEquals(
            GardenLeafVisual.GlassLeaf,
            gardenLeafVisualFor(GardenBillState.Paid, isFeaturedPaidRose = false)
        )
        assertEquals(
            GardenLeafVisual.DriedOverdueLeaf,
            gardenLeafVisualFor(GardenBillState.Overdue, isFeaturedPaidRose = false)
        )
        assertEquals(
            GardenLeafVisual.DriedOverdueLeaf,
            gardenLeafVisualFor(GardenBillState.Overdue, isFeaturedPaidRose = true)
        )
        assertEquals(
            GardenLeafVisual.GlassLeaf,
            gardenLeafVisualFor(GardenBillState.Upcoming, isFeaturedPaidRose = false)
        )
        assertEquals(
            GardenLeafVisual.GlassLeaf,
            gardenLeafVisualFor(GardenBillState.FarOff, isFeaturedPaidRose = true)
        )
    }

    @Test
    fun featuredPaidBillIdFrom_selectsTheChronologicallyFirstPaidBill() {
        val paidLate = sampleBill(isPaid = true, dueDate = today).copy(id = 2L, name = "Later Paid")
        val paidEarly = sampleBill(isPaid = true, dueDate = today.minusDays(5)).copy(id = 1L, name = "First Paid")
        val unpaid = sampleBill(isPaid = false, dueDate = today.plusDays(2)).copy(id = 3L, name = "Upcoming")

        assertEquals(1L, featuredPaidBillIdFrom(listOf(paidLate, unpaid, paidEarly), today))
        assertEquals(
            null,
            featuredPaidBillIdFrom(listOf(unpaid), today)
        )
    }

    @Test
    fun organicLeafPath_skipsNonPositiveOrNonFiniteSizes() {
        assertTrue(organicLeafPath(androidx.compose.ui.geometry.Size(0f, 120f), tipOnLeft = true).isEmpty)
        assertTrue(organicLeafPath(androidx.compose.ui.geometry.Size(120f, -8f), tipOnLeft = false).isEmpty)
        assertTrue(
            organicLeafPath(
                androidx.compose.ui.geometry.Size(Float.NaN, 120f),
                tipOnLeft = true
            ).isEmpty
        )
        assertTrue(
            organicLeafPath(
                androidx.compose.ui.geometry.Size(120f, Float.POSITIVE_INFINITY),
                tipOnLeft = false
            ).isEmpty
        )
        assertFalse(
            organicLeafPath(
                androidx.compose.ui.geometry.Size(160f, 96f),
                tipOnLeft = true
            ).isEmpty
        )
    }

    @Test
    fun gardenBillDetailDate_formatsMonthAndDayForEveryPetal() {
        assertEquals("Sep 9", gardenBillDetailDate(LocalDate.of(2026, 9, 9)))
        assertEquals("Sep 5", gardenBillDetailDate(LocalDate.of(2026, 9, 5)))
        assertEquals("Sep 18", gardenBillDetailDate(LocalDate.of(2026, 9, 18)))
    }

    @Test
    fun dismissShapeForGardenState_usesOrganicLeavesExceptTheFeaturedRose() {
        val featuredRose = dismissShapeForGardenState(
            gardenState = GardenBillState.Paid,
            index = 0,
            isFeaturedPaidRose = true
        )
        val paidGlass = dismissShapeForGardenState(
            gardenState = GardenBillState.Paid,
            index = 0,
            isFeaturedPaidRose = false
        )
        val overdue = dismissShapeForGardenState(
            gardenState = GardenBillState.Overdue,
            index = 1,
            isFeaturedPaidRose = false
        )

        assertTrue(featuredRose is androidx.compose.foundation.shape.RoundedCornerShape)
        assertTrue(paidGlass is OrganicLeafShape)
        assertTrue((paidGlass as OrganicLeafShape).tipOnLeft)
        assertTrue(overdue is OrganicLeafShape)
        assertFalse((overdue as OrganicLeafShape).tipOnLeft)
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
