package com.artie.chargemenot.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

class MeadowBillCalendarTest {

    @Test
    fun monthlyMeadowFlower_mapsAllTwelveMonths() {
        Month.entries.forEach { month ->
            val flower = MonthlyMeadowFlower.forMonth(month)
            assertEquals(month, flower.month)
        }
        assertEquals(12, MonthlyMeadowFlower.entries.size)
    }

    @Test
    fun buildCalendarMonthDates_includesLeadingAndTrailingPadding() {
        val dates = buildCalendarMonthDates(YearMonth.of(2026, 9))
        assertEquals(0, dates.size % 7)
        assertEquals(30, dates.count { it != null })
        assertEquals(LocalDate.of(2026, 9, 1), dates.first { it != null })
    }

    @Test
    fun buildCalendarWeekDates_returnsSevenDaysStartingSunday() {
        val week = buildCalendarWeekDates(
            anchorDate = LocalDate.of(2026, 9, 10),
            visibleMonth = YearMonth.of(2026, 9)
        )
        assertEquals(7, week.size)
        assertEquals(LocalDate.of(2026, 9, 6), week.first())
        assertEquals(LocalDate.of(2026, 9, 12), week.last())
    }

    @Test
    fun buildCalendarMonthDates_mayExampleHasNullLeadingCells() {
        val dates = buildCalendarMonthDates(YearMonth.of(2026, 5))
        assertNull(dates.first())
        assertEquals(LocalDate.of(2026, 5, 1), dates[5])
    }
}
