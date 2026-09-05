package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.data.local.UserSettingsDao
import com.artie.chargemenot.data.local.UserSettingsEntity
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PruningViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

  @Test
  fun toggleBillStatus_excludesPrunedBillsFromProjectedTotals() {
    val billDao = FakeBillDao(seedBills())
    val viewModel = createViewModel(billDao)
    testScope.advanceUntilIdle()

    val spotifyId = 4L
    viewModel.toggleBillStatus(billId = spotifyId, isPruned = true)
    testScope.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.prunedBillIds.contains(spotifyId))
    assertFalse(state.projectedCategoryTotals.containsKey(BillCategory.SUBSCRIPTIONS) &&
      state.projectedCategoryTotals[BillCategory.SUBSCRIPTIONS] == state.originalCategoryTotals[BillCategory.SUBSCRIPTIONS])
    assertTrue(state.newMonthlyTotal < state.bills.sumOf { it.amount })
  }

  @Test
  fun adjustBillAmount_updatesSandboxWithoutAffectingOriginalSnapshotOnReset() {
    val billDao = FakeBillDao(seedBills())
    val viewModel = createViewModel(billDao)
    testScope.advanceUntilIdle()

    val netflixId = 5L
    viewModel.adjustBillAmount(billId = netflixId, newAmount = 5.0)
    testScope.advanceUntilIdle()

    assertEquals(5.0, viewModel.uiState.value.bills.first { it.id == netflixId }.amount, 0.001)

    viewModel.resetSandbox()
    testScope.advanceUntilIdle()

    assertEquals(15.49, viewModel.uiState.value.bills.first { it.id == netflixId }.amount, 0.001)
  }

  @Test
  fun resetSandbox_clearsPrunedStateAndReloadsRoomSnapshot() {
    val billDao = FakeBillDao(seedBills())
    val viewModel = createViewModel(billDao)
    testScope.advanceUntilIdle()

    viewModel.toggleBillStatus(billId = 4L, isPruned = true)
    testScope.advanceUntilIdle()
    assertTrue(viewModel.uiState.value.prunedBillIds.isNotEmpty())

    viewModel.resetSandbox()
    testScope.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.prunedBillIds.isEmpty())
    assertEquals(seedBills().size, viewModel.uiState.value.bills.size)
  }

  private fun createViewModel(billDao: FakeBillDao): PruningViewModel {
    return PruningViewModel(
      billDao = billDao,
      userSettingsRepository = UserSettingsRepository(FakeUserSettingsDao()),
      coroutineScope = testScope,
      ioDispatcher = testDispatcher
    )
  }

  private fun seedBills(): List<BillEntity> {
    val today = LocalDate.of(2026, 9, 5)
    return listOf(
      BillEntity(1, "Maple Street Apartment", 1_450.00, today.plusDays(3), BillCategory.RENT),
      BillEntity(2, "Whole Foods Groceries", 186.42, today.plusDays(5), BillCategory.FOOD),
      BillEntity(3, "Pacific Gas & Electric", 94.17, today.plusDays(8), BillCategory.UTILITIES),
      BillEntity(4, "Spotify Premium", 11.99, today.plusDays(12), BillCategory.SUBSCRIPTIONS),
      BillEntity(5, "Netflix", 15.49, today.plusDays(12), BillCategory.SUBSCRIPTIONS)
    )
  }

  private class FakeBillDao(
    seed: List<BillEntity>
  ) : BillDao {
    private val bills = MutableStateFlow(seed)

    override fun getAllBills(): Flow<List<BillEntity>> = bills

    override fun getUpcomingBills(today: LocalDate): Flow<List<BillEntity>> = bills

    override fun getBillById(billId: Long): Flow<BillEntity?> =
      MutableStateFlow(bills.value.firstOrNull { it.id == billId })

    override suspend fun insertBill(bill: BillEntity): Long = 1L

    override suspend fun updateBill(bill: BillEntity) = Unit

    override suspend fun deleteBill(bill: BillEntity) = Unit

    override suspend fun deleteBillById(billId: Long) = Unit

    override suspend fun getBillCount(): Int = bills.value.size

    override suspend fun getOverdueOrDueTodayUnpaidBillCount(today: LocalDate): Int = 0
  }

  private class FakeUserSettingsDao : UserSettingsDao {
    private val settings = MutableStateFlow(
      UserSettingsEntity(
        monthlyBudget = UserSettings.DEFAULT_MONTHLY_BUDGET,
        isNagModeEnabled = false
      )
    )

    override fun observeSettings(settingsId: Int): Flow<UserSettingsEntity?> = settings

    override suspend fun upsertSettings(settings: UserSettingsEntity) {
      this.settings.value = settings
    }

    override suspend fun getSettings(settingsId: Int): UserSettingsEntity? = settings.value

    override suspend fun getSettingsCount(settingsId: Int): Int = 1
  }
}
