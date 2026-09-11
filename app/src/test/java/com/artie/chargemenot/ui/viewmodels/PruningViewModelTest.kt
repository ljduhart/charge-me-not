package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.data.local.UserSettingsDao
import com.artie.chargemenot.data.local.UserSettingsEntity
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.domain.model.UserSettings
import com.artie.chargemenot.data.local.BillWithCompost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class PruningViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

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
    assertFalse(state.projectedParentCategoryTotals.containsKey(MeadowCategories.VINES) &&
      state.projectedParentCategoryTotals[MeadowCategories.VINES] == state.originalParentCategoryTotals[MeadowCategories.VINES])
    assertTrue(state.newMonthlyTotal < state.bills.sumOf { it.amount })
  }

  @Test
  fun adjustBillAmount_updatesSandboxWithoutAffectingOriginalSnapshotOnReset() {
    val billDao = FakeBillDao(seedBills())
    val viewModel = createViewModel(billDao)
    testScope.advanceUntilIdle()

    val netflixId = 5L
    viewModel.adjustBillAmount(billId = netflixId, newAmount = 500L)
    testScope.advanceUntilIdle()

    assertEquals(500L, viewModel.uiState.value.bills.first { it.id == netflixId }.amount)

    viewModel.resetSandbox()
    testScope.advanceUntilIdle()

    assertEquals(1_549L, viewModel.uiState.value.bills.first { it.id == netflixId }.amount)
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

  @Test
  fun toggleBillStatus_recursivelyPrunesChildBills() {
    val today = LocalDate.of(2026, 9, 5)
    val bills = listOf(
      BillEntity(1, "Car Payment", 45_000L, today.plusDays(5), MeadowCategories.ROOT_SYSTEM, "Transportation"),
      BillEntity(
        id = 2,
        name = "Car Insurance",
        amount = 12_000L,
        dueDate = today.plusDays(6),
        parentCategory = MeadowCategories.POLLINATORS,
        subCategory = "Healthcare",
        parentBillId = 1L
      ),
      BillEntity(
        id = 3,
        name = "Roadside Assistance",
        amount = 800L,
        dueDate = today.plusDays(7),
        parentCategory = MeadowCategories.VINES,
        subCategory = "Subscriptions",
        parentBillId = 2L
      )
    )
    val billDao = FakeBillDao(bills)
    val viewModel = createViewModel(billDao)
    testScope.advanceUntilIdle()

    viewModel.toggleBillStatus(billId = 1L, isPruned = true)
    testScope.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.prunedBillIds.containsAll(setOf(1L, 2L, 3L)))
    assertEquals(1, state.childRelationships[1L]?.size)
    assertEquals("Car Insurance", state.childRelationships[1L]?.first()?.name)
  }

  @Test
  fun childRelationships_mapsParentToChildren() {
    val today = LocalDate.of(2026, 9, 5)
    val bills = listOf(
      BillEntity(1, "Car Payment", 45_000L, today.plusDays(5), MeadowCategories.ROOT_SYSTEM, "Transportation"),
      BillEntity(
        id = 2,
        name = "Car Insurance",
        amount = 12_000L,
        dueDate = today.plusDays(6),
        parentCategory = MeadowCategories.POLLINATORS,
        subCategory = "Healthcare",
        parentBillId = 1L
      )
    )
    val viewModel = createViewModel(FakeBillDao(bills))
    testScope.advanceUntilIdle()

    val relationships = viewModel.uiState.value.childRelationships
    assertEquals(1, relationships[1L]?.size)
    assertEquals("Car Insurance", relationships[1L]?.first()?.name)
  }

  @Test
  fun unpruneParent_preservesManuallyPrunedChild() {
    val today = LocalDate.of(2026, 9, 5)
    val bills = listOf(
      BillEntity(1, "Car Payment", 45_000L, today.plusDays(5), MeadowCategories.ROOT_SYSTEM, "Transportation"),
      BillEntity(
        id = 2,
        name = "Car Insurance",
        amount = 12_000L,
        dueDate = today.plusDays(6),
        parentCategory = MeadowCategories.POLLINATORS,
        subCategory = "Healthcare",
        parentBillId = 1L
      )
    )
    val viewModel = createViewModel(FakeBillDao(bills))
    testScope.advanceUntilIdle()

    viewModel.toggleBillStatus(billId = 2L, isPruned = true)
    testScope.advanceUntilIdle()
    assertTrue(viewModel.uiState.value.prunedBillIds.contains(2L))

    viewModel.toggleBillStatus(billId = 1L, isPruned = true)
    testScope.advanceUntilIdle()
    assertTrue(viewModel.uiState.value.prunedBillIds.containsAll(setOf(1L, 2L)))

    viewModel.toggleBillStatus(billId = 1L, isPruned = false)
    testScope.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.prunedBillIds.contains(1L))
    assertTrue(viewModel.uiState.value.prunedBillIds.contains(2L))
  }

  private fun createViewModel(billDao: FakeBillDao): PruningViewModel {
    return PruningViewModel(
      billDao = billDao,
      userSettingsRepository = UserSettingsRepository(FakeUserSettingsDao()),
    )
  }

  private fun seedBills(): List<BillEntity> {
    val today = LocalDate.of(2026, 9, 5)
    return listOf(
      BillEntity(1, "Maple Street Apartment", 145_000L, today.plusDays(3), MeadowCategories.CANOPY, "Rent"),
      BillEntity(2, "Whole Foods Groceries", 18_642L, today.plusDays(5), MeadowCategories.FERTILIZER, "Groceries"),
      BillEntity(3, "Pacific Gas & Electric", 9_417L, today.plusDays(8), MeadowCategories.ROOT_SYSTEM, "Utilities"),
      BillEntity(4, "Spotify Premium", 1_199L, today.plusDays(12), MeadowCategories.VINES, "Subscriptions"),
      BillEntity(5, "Netflix", 1_549L, today.plusDays(12), MeadowCategories.VINES, "Subscriptions")
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

    override fun getActiveSubscriptions(): Flow<List<BillEntity>> =
      bills.map { items ->
        items.filter { bill ->
          bill.parentCategory == MeadowCategories.VINES && !bill.isPaid
        }
      }

    override fun getBillsByParentCategory(parentCategory: String): Flow<List<BillEntity>> =
      bills.map { items ->
        items.filter { bill -> bill.parentCategory == parentCategory && !bill.isPaid }
      }

    override suspend fun getBillByIdOnce(billId: Long): BillEntity? =
      bills.value.firstOrNull { it.id == billId }

    override suspend fun getOverdueOrDueTodayUnpaidBillCount(today: LocalDate): Int = 0

    override fun getChildrenForParent(parentId: Long): Flow<List<BillEntity>> =
      bills.map { items -> items.filter { bill -> bill.parentBillId == parentId } }

    override fun searchCompost(query: String): Flow<List<BillWithCompost>> =
      flowOf(emptyList())
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
