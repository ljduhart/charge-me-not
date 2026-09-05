package com.artie.chargemenot.domain.usecase

import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.domain.model.WeatherStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ForecastUseCaseTest {

    private val useCase = ForecastUseCase(billDao = FakeForecastBillDao())

    @Test
    fun calculateForecast_returnsNullWhenFewerThanTwoHistoricalMonths() {
        val today = LocalDate.of(2026, 9, 5)
        val bills = listOf(
            bill(
                amount = 90.0,
                dueDate = today.minusMonths(1).withDayOfMonth(10),
                category = BillCategory.UTILITIES
            )
        )

        val result = useCase.calculateForecast(bills, today)

        assertNull(result)
    }

    @Test
    fun calculateForecast_predictsNextMonthWithLinearRegression() {
        val today = LocalDate.of(2026, 9, 5)
        val bills = listOf(
            bill(
                amount = 230.0,
                dueDate = today.minusMonths(2).withDayOfMonth(10),
                category = BillCategory.UTILITIES
            ),
            bill(
                amount = 250.0,
                dueDate = today.minusMonths(1).withDayOfMonth(10),
                category = BillCategory.UTILITIES
            )
        )

        val result = useCase.calculateForecast(bills, today)

        assertNotNull(result)
        assertEquals(270.0, result!!.predictedAmount, 0.001)
        assertEquals(8.0, result.percentageVariance, 0.001)
        assertEquals(WeatherStatus.CLOUDY, result.weatherStatus)
        assertEquals(3, result.timelinePoints.size)
        assertEquals(true, result.timelinePoints.last().isPrediction)
    }

    @Test
    fun calculateForecast_marksDroughtWhenVarianceExceedsFifteenPercent() {
        val today = LocalDate.of(2026, 9, 5)
        val bills = listOf(
            bill(
                amount = 100.0,
                dueDate = today.minusMonths(2).withDayOfMonth(8),
                category = BillCategory.FOOD
            ),
            bill(
                amount = 120.0,
                dueDate = today.minusMonths(1).withDayOfMonth(8),
                category = BillCategory.FOOD
            )
        )

        val result = useCase.calculateForecast(bills, today)

        assertNotNull(result)
        assertEquals(WeatherStatus.DROUGHT, result!!.weatherStatus)
    }

    @Test
    fun calculateForecast_ignoresNonVariableCategories() {
        val today = LocalDate.of(2026, 9, 5)
        val bills = listOf(
            bill(
                amount = 1_450.0,
                dueDate = today.minusMonths(2).withDayOfMonth(1),
                category = BillCategory.RENT
            ),
            bill(
                amount = 1_450.0,
                dueDate = today.minusMonths(1).withDayOfMonth(1),
                category = BillCategory.RENT
            )
        )

        val result = useCase.calculateForecast(bills, today)

        assertNull(result)
    }

    @Test
    fun predictNextMonthLinearRegression_returnsAverageWhenDenominatorIsZero() {
        val predicted = useCase.predictNextMonthLinearRegression(listOf(100.0, 100.0, 100.0))
        assertEquals(100.0, predicted, 0.001)
    }

    @Test
    fun resolveWeatherStatus_mapsThresholdsCorrectly() {
        assertEquals(WeatherStatus.SUNNY, useCase.resolveWeatherStatus(3.0))
        assertEquals(WeatherStatus.CLOUDY, useCase.resolveWeatherStatus(8.0))
        assertEquals(WeatherStatus.DROUGHT, useCase.resolveWeatherStatus(18.0))
    }

    private fun bill(
        amount: Double,
        dueDate: LocalDate,
        category: BillCategory
    ): BillEntity {
        return BillEntity(
            id = dueDate.toEpochDay(),
            name = "Test Bill",
            amount = amount,
            dueDate = dueDate,
            category = category
        )
    }

    private class FakeForecastBillDao : com.artie.chargemenot.data.local.BillDao {
        override fun getAllBills() = kotlinx.coroutines.flow.flowOf(emptyList<BillEntity>())
        override fun getUpcomingBills(today: LocalDate) =
            kotlinx.coroutines.flow.flowOf(emptyList<BillEntity>())
        override fun getBillById(billId: Long) = kotlinx.coroutines.flow.flowOf<BillEntity?>(null)
        override suspend fun insertBill(bill: BillEntity): Long = 1L
        override suspend fun updateBill(bill: BillEntity) = Unit
        override suspend fun deleteBill(bill: BillEntity) = Unit
        override suspend fun deleteBillById(billId: Long) = Unit
        override suspend fun getBillCount(): Int = 0
        override fun getActiveSubscriptions() = kotlinx.coroutines.flow.flowOf(emptyList<BillEntity>())
        override suspend fun getBillByIdOnce(billId: Long): BillEntity? = null
        override suspend fun getOverdueOrDueTodayUnpaidBillCount(today: LocalDate): Int = 0
        override fun getChildrenForParent(parentId: Long) =
            kotlinx.coroutines.flow.flowOf(emptyList<BillEntity>())
        override fun searchCompost(query: String) =
            kotlinx.coroutines.flow.flowOf(emptyList<com.artie.chargemenot.data.local.BillWithCompost>())
    }
}
