package com.artie.chargemenot.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowSky
import com.artie.chargemenot.ui.theme.MeadowWhite
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MeadowBillCalendarCard(
    visibleMonth: YearMonth,
    isExpanded: Boolean,
    billsByDueDate: Map<LocalDate, List<Bill>>,
    selectedDate: LocalDate?,
    onToggleExpanded: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayTapped: (LocalDate) -> Unit,
    onBillTapped: (Bill) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthFlower = MonthlyMeadowFlower.forMonth(visibleMonth.month)
    val today = LocalDate.now()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MeadowWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
        ) {
            MonthlyMeadowFlowerWatermark(
                month = visibleMonth.month,
                modifier = Modifier.matchParentSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.meadow_bill_calendar_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MeadowGreenDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(
                                R.string.meadow_bill_calendar_month_flower,
                                visibleMonth.month.getDisplayName(TextStyle.FULL, Locale.US),
                                visibleMonth.year,
                                monthFlower.displayName
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onToggleExpanded) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = stringResource(
                                if (isExpanded) {
                                    R.string.meadow_bill_calendar_collapse
                                } else {
                                    R.string.meadow_bill_calendar_expand
                                }
                            ),
                            tint = MeadowGreenDark
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onPreviousMonth) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = stringResource(R.string.meadow_bill_calendar_previous_month),
                            tint = MeadowGreenDark
                        )
                    }
                    Text(
                        text = visibleMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.US),
                        style = MaterialTheme.typography.titleSmall,
                        color = MeadowGreenDark,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = onNextMonth) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = stringResource(R.string.meadow_bill_calendar_next_month),
                            tint = MeadowGreenDark
                        )
                    }
                }

                if (!isExpanded) {
                    MeadowBillCalendarWeekStrip(
                        visibleMonth = visibleMonth,
                        today = today,
                        billsByDueDate = billsByDueDate,
                        selectedDate = selectedDate,
                        onDayTapped = onDayTapped,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                } else {
                    MeadowBillCalendarWeekdayHeader(modifier = Modifier.padding(top = 10.dp))
                    MeadowBillCalendarMonthGrid(
                        visibleMonth = visibleMonth,
                        today = today,
                        billsByDueDate = billsByDueDate,
                        selectedDate = selectedDate,
                        onDayTapped = onDayTapped,
                        onBillTapped = onBillTapped,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.meadow_bill_calendar_tap_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun MeadowBillCalendarWeekdayHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DayOfWeek.entries.forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.US),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MeadowGreenDark,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MeadowBillCalendarWeekStrip(
    visibleMonth: YearMonth,
    today: LocalDate,
    billsByDueDate: Map<LocalDate, List<Bill>>,
    selectedDate: LocalDate?,
    onDayTapped: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekDates = buildCalendarWeekDates(anchorDate = today, visibleMonth = visibleMonth)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekDates.forEach { date ->
            MeadowBillCalendarDayCell(
                date = date,
                isToday = date == today,
                isSelected = date == selectedDate,
                bills = billsByDueDate[date].orEmpty(),
                onDayTapped = onDayTapped,
                onBillTapped = {},
                showBillDots = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MeadowBillCalendarMonthGrid(
    visibleMonth: YearMonth,
    today: LocalDate,
    billsByDueDate: Map<LocalDate, List<Bill>>,
    selectedDate: LocalDate?,
    onDayTapped: (LocalDate) -> Unit,
    onBillTapped: (Bill) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthDates = buildCalendarMonthDates(visibleMonth)
    Column(modifier = modifier.fillMaxWidth()) {
        monthDates.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                week.forEach { date ->
                    if (date == null) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        MeadowBillCalendarDayCell(
                            date = date,
                            isToday = date == today,
                            isSelected = date == selectedDate,
                            bills = billsByDueDate[date].orEmpty(),
                            onDayTapped = onDayTapped,
                            onBillTapped = onBillTapped,
                            showBillDots = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MeadowBillCalendarDayCell(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    bills: List<Bill>,
    onDayTapped: (LocalDate) -> Unit,
    onBillTapped: (Bill) -> Unit,
    showBillDots: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MeadowSky.copy(alpha = 0.55f)
        isToday -> MeadowGreen.copy(alpha = 0.14f)
        else -> Color.Transparent
    }
    val borderColor = when {
        isSelected -> MeadowGreenDark
        isToday -> MeadowGreen
        else -> Color.Transparent
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected || isToday) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .then(
                if (!showBillDots) {
                    Modifier.clickable { onDayTapped(date) }
                } else {
                    Modifier
                }
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = if (isToday) MeadowGreenDark else Color(0xFF1A1A1A),
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = if (showBillDots) {
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onDayTapped(date) }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            } else {
                Modifier
            }
        )
        if (showBillDots && bills.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                bills.take(3).forEach { bill ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(MeadowGreen)
                            .clickable { onBillTapped(bill) },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MeadowGreenDark)
                        )
                    }
                }
            }
            if (bills.size > 3) {
                Text(
                    text = "+${bills.size - 3}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MeadowGreenDark,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
    }
}

internal fun buildCalendarMonthDates(visibleMonth: YearMonth): List<LocalDate?> {
    val firstDayOfMonth = visibleMonth.atDay(1)
    val leadingEmptyDays = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = visibleMonth.lengthOfMonth()
    val cells = mutableListOf<LocalDate?>()

    repeat(leadingEmptyDays) { cells.add(null) }
    repeat(daysInMonth) { offset ->
        cells.add(visibleMonth.atDay(offset + 1))
    }
    while (cells.size % 7 != 0) {
        cells.add(null)
    }
    return cells
}

internal fun buildCalendarWeekDates(anchorDate: LocalDate, visibleMonth: YearMonth): List<LocalDate> {
    val focusedDate = when {
        anchorDate.year == visibleMonth.year && anchorDate.month == visibleMonth.month -> anchorDate
        else -> visibleMonth.atDay(1)
    }
    val startOfWeek = focusedDate.minusDays(focusedDate.dayOfWeek.value.toLong() % 7)
    return (0..6).map { offset -> startOfWeek.plusDays(offset.toLong()) }
}
