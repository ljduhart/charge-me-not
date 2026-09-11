package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.data.local.BillWithCompost
import com.artie.chargemenot.data.local.CompostDao
import com.artie.chargemenot.data.local.CompostEntity
import com.artie.chargemenot.data.local.UserSettingsDao
import com.artie.chargemenot.data.local.UserSettingsEntity
import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.domain.model.UserSettings
import com.artie.chargemenot.data.model.CrossPollinationPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ScannerViewModelTest {

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
  fun calculatePredictiveImpact_usesProvidedMonthlyBudget() {
    val viewModel = createViewModel()

    val impact = viewModel.calculatePredictiveImpact(
      parentCategory = MeadowCategories.ROOT_SYSTEM,
      scannedAmount = 10_000L,
      parentCategoryTotals = mapOf(MeadowCategories.ROOT_SYSTEM to 20_000L),
      monthlyBudget = 100_000L
    )

    assertEquals(30.0, impact.newPetalSizePercent, 0.001)
    assertEquals(10_000L, impact.scannedAmount)
    assertTrue(impact.withinBudget)
  }

  @Test
  fun calculatePredictiveImpact_marksOverBudgetWhenProjectedSpendExceedsUserBudget() {
    val viewModel = createViewModel()

    val impact = viewModel.calculatePredictiveImpact(
      parentCategory = MeadowCategories.CANOPY,
      scannedAmount = 25_000L,
      parentCategoryTotals = mapOf(
        MeadowCategories.CANOPY to 145_000L,
        MeadowCategories.FERTILIZER to 40_000L
      ),
      monthlyBudget = 150_000L
    )

    assertFalse(impact.withinBudget)
    assertEquals(210_000L, impact.totalProjectedSpend)
  }

  @Test
  fun calculatePredictiveImpact_guardsAgainstZeroBudget() {
    val viewModel = createViewModel()

    val impact = viewModel.calculatePredictiveImpact(
      parentCategory = MeadowCategories.FERTILIZER,
      scannedAmount = 5_000L,
      parentCategoryTotals = mapOf(MeadowCategories.FERTILIZER to 2_500L),
      monthlyBudget = 0L
    )

    assertEquals(7_500.0, impact.newPetalSizePercent, 0.001)
  }

  @Test
  fun onQrPayloadDetected_setsPollenReceivedState() {
    val viewModel = createViewModel()
    val payload = CrossPollinationPayload(
      name = "Shared Electric",
      amount = 8_450L,
      dueDate = "2026-10-01",
      parentCategory = MeadowCategories.ROOT_SYSTEM,
      subCategory = "Utilities"
    )

    viewModel.onQrPayloadDetected(payload)

    val pollen = viewModel.uiState.value.pollenReceived
    assertNotNull(pollen)
    assertEquals("Shared Electric", pollen!!.name)
    assertEquals(8_450L, pollen.amount)
    assertEquals(MeadowCategories.ROOT_SYSTEM, pollen.parentCategory)
    assertEquals("Utilities", pollen.subCategory)
  }

  @Test
  fun acceptPollinatedBill_insertsBillAndResetsSession() {
    val billDao = TrackingBillDao()
    val viewModel = ScannerViewModel(
      billRepository = BillRepository(billDao, FakeCompostDao()),
      userSettingsRepository = UserSettingsRepository(FakeUserSettingsDao()),
      coroutineScope = testScope,
      ioDispatcher = testDispatcher
    )
    testScope.advanceUntilIdle()

    viewModel.onQrPayloadDetected(
      CrossPollinationPayload(
        name = "Roommate Rent Split",
        amount = 72_500L,
        dueDate = "2026-09-15",
        parentCategory = MeadowCategories.CANOPY,
        subCategory = "Rent"
      )
    )

    var accepted = false
    viewModel.acceptPollinatedBill { accepted = true }
    testScope.advanceUntilIdle()

    assertTrue(accepted)
    assertEquals(1, billDao.insertedBills.size)
    assertEquals("Roommate Rent Split", billDao.insertedBills.first().name)
    assertNull(viewModel.uiState.value.pollenReceived)
  }

  @Test
  fun discardPollen_suppressesImmediateQrRedetection() {
    val viewModel = createViewModel()
    val payload = CrossPollinationPayload(
      name = "Spotify Premium",
      amount = 1_199L,
      dueDate = "2026-09-12",
      parentCategory = MeadowCategories.VINES,
      subCategory = "Subscriptions"
    )

    viewModel.onQrPayloadDetected(payload)
    viewModel.discardPollen()
    viewModel.onQrPayloadDetected(payload)

    assertNull(viewModel.uiState.value.pollenReceived)
  }

  @Test
  fun acceptPollinatedBill_preventsDuplicateInsertsOnDoubleTap() {
    val billDao = TrackingBillDao()
    val viewModel = ScannerViewModel(
      billRepository = BillRepository(billDao, FakeCompostDao()),
      userSettingsRepository = UserSettingsRepository(FakeUserSettingsDao()),
      coroutineScope = testScope,
      ioDispatcher = testDispatcher
    )
    testScope.advanceUntilIdle()

    viewModel.onQrPayloadDetected(
      CrossPollinationPayload(
        name = "Roommate Rent Split",
        amount = 72_500L,
        dueDate = "2026-09-15",
        parentCategory = MeadowCategories.CANOPY,
        subCategory = "Rent"
      )
    )

    var acceptCount = 0
    viewModel.acceptPollinatedBill { acceptCount++ }
    viewModel.acceptPollinatedBill { acceptCount++ }
    testScope.advanceUntilIdle()

    assertEquals(1, billDao.insertedBills.size)
    assertEquals(1, acceptCount)
  }

  @Test
  fun discardPollen_clearsPollenReceivedState() {
    val viewModel = createViewModel()
    viewModel.onQrPayloadDetected(
      CrossPollinationPayload(
        name = "Spotify Premium",
        amount = 1_199L,
        dueDate = "2026-09-12",
        parentCategory = MeadowCategories.VINES,
        subCategory = "Subscriptions"
      )
    )
    viewModel.discardPollen()

    assertNull(viewModel.uiState.value.pollenReceived)
  }

  private fun createViewModel(): ScannerViewModel {
    return ScannerViewModel(
      billRepository = BillRepository(FakeBillDao(), FakeCompostDao()),
      userSettingsRepository = UserSettingsRepository(FakeUserSettingsDao()),
      coroutineScope = testScope,
      ioDispatcher = testDispatcher
    )
  }

  private class TrackingBillDao : BillDao {
    val insertedBills = mutableListOf<BillEntity>()

    override fun getAllBills(): Flow<List<BillEntity>> = MutableStateFlow(emptyList())

    override fun getUpcomingBills(today: LocalDate): Flow<List<BillEntity>> =
      MutableStateFlow(emptyList())

    override fun getBillById(billId: Long): Flow<BillEntity?> = MutableStateFlow(null)

    override suspend fun insertBill(bill: BillEntity): Long {
      insertedBills.add(bill)
      return insertedBills.size.toLong()
    }

    override suspend fun updateBill(bill: BillEntity) = Unit

    override suspend fun deleteBill(bill: BillEntity) = Unit

    override suspend fun deleteBillById(billId: Long) = Unit

    override suspend fun getBillCount(): Int = insertedBills.size

    override fun getActiveSubscriptions(): Flow<List<BillEntity>> =
      MutableStateFlow(emptyList())

    override fun getBillsByParentCategory(parentCategory: String): Flow<List<BillEntity>> =
      MutableStateFlow(emptyList())

    override suspend fun getBillByIdOnce(billId: Long): BillEntity? = null

    override suspend fun getOverdueOrDueTodayUnpaidBillCount(today: LocalDate): Int = 0

    override fun getChildrenForParent(parentId: Long): Flow<List<BillEntity>> =
      MutableStateFlow(emptyList())

    override fun searchCompost(query: String): Flow<List<BillWithCompost>> =
      flowOf(emptyList())
  }

  private class FakeCompostDao : CompostDao {
    override suspend fun insertCompost(compost: CompostEntity) = Unit

    override suspend fun deleteCompostForBill(billId: Long) = Unit
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

    override fun getActiveSubscriptions(): Flow<List<BillEntity>> =
      MutableStateFlow(emptyList())

    override fun getBillsByParentCategory(parentCategory: String): Flow<List<BillEntity>> =
      MutableStateFlow(emptyList())

    override suspend fun getBillByIdOnce(billId: Long): BillEntity? = null

    override suspend fun getOverdueOrDueTodayUnpaidBillCount(today: LocalDate): Int = 0

    override fun getChildrenForParent(parentId: Long): Flow<List<BillEntity>> =
      MutableStateFlow(emptyList())

    override fun searchCompost(query: String): Flow<List<BillWithCompost>> =
      flowOf(emptyList())
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
