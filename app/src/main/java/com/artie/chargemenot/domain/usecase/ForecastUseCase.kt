package com.artie.chargemenot.domain.usecase

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.domain.model.ForecastResult
import com.artie.chargemenot.domain.model.ForecastTimelinePoint
import com.artie.chargemenot.domain.model.WeatherStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

class ForecastUseCase(
    private val billDao: BillDao
) {

    fun observeForecast(): Flow<ForecastResult?> {
        return billDao.getAllBills().map { bills ->
            calculateForecast(bills, LocalDate.now())
        }
    }

    fun calculateForecastFromDomainBills(
        bills: List<Bill>,
        today: LocalDate = LocalDate.now()
    ): ForecastResult? {
        return calculateForecast(
            bills = bills.map { bill -> bill.toBillEntity() },
            today = today
        )
    }

    fun calculateForecast(
        bills: List<BillEntity>,
        today: LocalDate = LocalDate.now()
    ): ForecastResult? {
        val currentMonth = YearMonth.from(today)
        val targetMonth = currentMonth.plusMonths(1)

        val monthlyTotals = bills
            .asSequence()
            .filter { bill -> bill.parentCategory in VARIABLE_PARENT_CATEGORIES }
            .groupBy { bill -> YearMonth.from(bill.dueDate) }
            .mapValues { (_, monthBills) -> monthBills.sumOf { bill -> bill.amount } }
            .filterValues { total -> total > 0.0 }

        val historicalMonths = monthlyTotals.keys
            .filter { month -> month < currentMonth }
            .sorted()

        if (historicalMonths.size < MIN_HISTORICAL_MONTHS) {
            return null
        }

        val historicalAmounts = historicalMonths.map { month -> monthlyTotals[month]!! }
        val predictedAmount = predictNextMonthLinearRegression(historicalAmounts)
            .coerceAtLeast(0.0)

        val lastMonthAmount = historicalAmounts.last()
        if (lastMonthAmount <= 0.0) {
            return null
        }

        val percentageVariance =
            ((predictedAmount - lastMonthAmount) / lastMonthAmount) * PERCENT_SCALE
        val weatherStatus = resolveWeatherStatus(percentageVariance)

        val timelineHistoricalMonths = historicalMonths.takeLast(TIMELINE_HISTORICAL_MONTHS)
        val timelinePoints = buildTimelinePoints(
            historicalMonths = timelineHistoricalMonths,
            monthlyTotals = monthlyTotals,
            predictedAmount = predictedAmount,
            predictionStatus = weatherStatus,
            targetMonth = targetMonth
        )

        return ForecastResult(
            predictedAmount = predictedAmount,
            percentageVariance = percentageVariance,
            weatherStatus = weatherStatus,
            timelinePoints = timelinePoints,
            targetMonthLabel = formatMonthLabel(targetMonth)
        )
    }

    private fun buildTimelinePoints(
        historicalMonths: List<YearMonth>,
        monthlyTotals: Map<YearMonth, Double>,
        predictedAmount: Double,
        predictionStatus: WeatherStatus,
        targetMonth: YearMonth
    ): List<ForecastTimelinePoint> {
        val points = mutableListOf<ForecastTimelinePoint>()

        historicalMonths.forEachIndexed { index, month ->
            val amount = monthlyTotals[month] ?: 0.0
            val previousMonth = historicalMonths.getOrNull(index - 1)
            val previousAmount = previousMonth?.let { prior -> monthlyTotals[prior] }
            val status = if (previousAmount != null) {
                resolveWeatherStatus(
                    ((amount - previousAmount) / previousAmount) * PERCENT_SCALE
                )
            } else {
                WeatherStatus.SUNNY
            }

            points += ForecastTimelinePoint(
                monthLabel = formatMonthLabel(month),
                amount = amount,
                isPrediction = false,
                weatherStatus = status
            )
        }

        points += ForecastTimelinePoint(
            monthLabel = formatMonthLabel(targetMonth),
            amount = predictedAmount,
            isPrediction = true,
            weatherStatus = predictionStatus
        )

        return points
    }

    internal fun predictNextMonthLinearRegression(monthlyAmounts: List<Double>): Double {
        if (monthlyAmounts.isEmpty()) {
            return 0.0
        }
        if (monthlyAmounts.size == 1) {
            return monthlyAmounts.first()
        }

        val n = monthlyAmounts.size
        var sumX = 0.0
        var sumY = 0.0
        var sumXY = 0.0
        var sumX2 = 0.0

        monthlyAmounts.forEachIndexed { index, amount ->
            val x = index.toDouble()
            sumX += x
            sumY += amount
            sumXY += x * amount
            sumX2 += x * x
        }

        val denominator = (n * sumX2) - (sumX * sumX)
        if (abs(denominator) < EPSILON) {
            return monthlyAmounts.average()
        }

        val slope = ((n * sumXY) - (sumX * sumY)) / denominator
        val intercept = (sumY - (slope * sumX)) / n
        return intercept + (slope * n)
    }

    internal fun resolveWeatherStatus(percentageVariance: Double): WeatherStatus {
        return when {
            percentageVariance > DROUGHT_THRESHOLD_PERCENT -> WeatherStatus.DROUGHT
            percentageVariance > CLOUDY_THRESHOLD_PERCENT -> WeatherStatus.CLOUDY
            else -> WeatherStatus.SUNNY
        }
    }

    private fun formatMonthLabel(month: YearMonth): String {
        return month.month.getDisplayName(TextStyle.SHORT, Locale.US)
    }

    private fun Bill.toBillEntity(): BillEntity {
        return BillEntity(
            id = id,
            name = name,
            amount = amount,
            dueDate = dueDate,
            parentCategory = parentCategory,
            subCategory = subCategory,
            isPaid = isPaid,
            usageCount = usageCount,
            auditPromptCount = auditPromptCount,
            parentBillId = parentBillId,
            receiptImagePath = receiptImagePath
        )
    }

    companion object {
        private val VARIABLE_PARENT_CATEGORIES = setOf(
            MeadowCategories.ROOT_SYSTEM,
            MeadowCategories.FERTILIZER
        )

        const val MIN_HISTORICAL_MONTHS = 2
        private const val TIMELINE_HISTORICAL_MONTHS = 3
        private const val DROUGHT_THRESHOLD_PERCENT = 15.0
        private const val CLOUDY_THRESHOLD_PERCENT = 5.0
        private const val PERCENT_SCALE = 100.0
        private const val EPSILON = 1e-6
    }
}
