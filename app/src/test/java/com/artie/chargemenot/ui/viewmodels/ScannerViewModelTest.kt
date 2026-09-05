package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.data.local.UserSettingsDao
import com.artie.chargemenot.data.local.UserSettingsEntity
import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ScannerViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @Test
  fun calculatePredictiveImpact_usesProvidedMonthlyBudget() {
    val viewModel = createViewModel()

    val impact = viewModel.calculatePredictiveImpact(
      category = BillCategory.UTILITIES,
      scannedAmount = 100.0,
      categoryTotals = mapOf(BillCategory.UTILITIES to 200.0),
      monthlyBudget = 1_000.0
    )

    assertEquals(30.0, impact.newPetalSizePercent, 0.001)
    assertEquals(100.0, impact.scannedAmount, 0.001)
    assertTrue(impact.withinBudget)
  }

  @Test
  fun calculatePredictiveImpact_marksOverBudgetWhenProjectedSpendExceedsUserBudget() {
    val viewModel = createViewModel()

    val impact = viewModel.calculatePredictiveImpact(
      category = BillCategory.RENT,
      scannedAmount = 250.0,
      categoryTotals = mapOf(
        BillCategory.RENT to 1_450.0,
        BillCategory.FOOD to 400.0
      ),
      monthlyBudget = 1_500.0
    )

    assertFalse(impact.withinBudget)
    assertEquals(2_100.0, impact.totalProjectedSpend, 0.001)
  }

  @Test
  fun calculatePredictiveImpact_guardsAgainstZeroBudget() {
    val viewModel = createViewModel()

    val impact = viewModel.calculatePredictiveImpact(
      category = BillCategory.FOOD,
      scannedAmount = 50.0,
      categoryTotals = mapOf(BillCategory.FOOD to 25.0),
      monthlyBudget = 0.0
    )

    assertEquals(7_500.0, impact.newPetalSizePercent, 0.001)
  }

  private fun createViewModel(): ScannerViewModel {
    return ScannerViewModel(
      billRepository = BillRepository(FakeBillDao()),
      userSettingsRepository = UserSettingsRepository(FakeUserSettingsDao()),
      coroutineScope = testScope,
      ioDispatcher = testDispatcher
    )
  }

  private class FakeBillDao : BillDao {
    override fun getAllBills(): Flow<List<BillEntity>> = MutableStateFlow(emptyList())

    override fun getUpcomingBills(today: LocalDate): Flow<List<BillEntity>> =
      MutableStateFlow(emptyList())

    override fun getBillById(billId: Long): Flow<BillEntity?> = MutableStateFlow(null)

    override suspend fun insertBill(bill: BillEntity): Long = 1L

    override suspend fun updateBill(bill: BillEntity) = Unit

    override suspend fun deleteBill(bill: BillEntity) = Unit

    override suspend fun deleteBillById(billId: Long) = Unit

    override suspend fun getBillCount(): Int = 0

    override suspend fun getOverdueOrDueTodayUnpaidBillCount(today: LocalDate): Int = 0
  }

  private class FakeUserSettingsDao : UserSettingsDao {
    override fun observeSettings(settingsId: Int): Flow<UserSettingsEntity?> =
      MutableStateFlow(
        UserSettingsEntity(monthlyBudget = UserSettings.DEFAULT_MONTHLY_BUDGET)
      )

    override suspend fun upsertSettings(settings: UserSettingsEntity) = Unit

    override suspend fun getSettings(settingsId: Int): UserSettingsEntity? =
      UserSettingsEntity(monthlyBudget = UserSettings.DEFAULT_MONTHLY_BUDGET)

    override suspend fun getSettingsCount(settingsId: Int): Int = 1
  }
}
