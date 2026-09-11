package com.artie.chargemenot.ui.dashboard

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.data.local.BillWithCompost
import com.artie.chargemenot.data.local.CompostDao
import com.artie.chargemenot.data.local.CompostEntity
import com.artie.chargemenot.data.local.UserSettingsDao
import com.artie.chargemenot.data.local.UserSettingsEntity
import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.domain.usecase.ForecastUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

class DashboardViewModelEditTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var trackingBillDao: TrackingBillDao

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        trackingBillDao = TrackingBillDao()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun selectBillForEdit_exposesSelectedBill() {
        val viewModel = createViewModel()
        val bill = sampleBill(id = 4L, name = "Netflix")

        viewModel.selectBillForEdit(bill)

        assertEquals(bill, viewModel.uiState.value.selectedBillForEdit)
    }

    @Test
    fun clearEditSelection_clearsSelectedBill() {
        val viewModel = createViewModel()
        viewModel.selectBillForEdit(sampleBill(id = 4L, name = "Netflix"))
        viewModel.clearEditSelection()

        assertNull(viewModel.uiState.value.selectedBillForEdit)
    }

    @Test
    fun saveBillEdits_persistsThroughRepositoryAndClearsSelection() {
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()

        val original = sampleBill(id = 4L, name = "Netflix", amount = 1_549L)
        val updated = original.copy(name = "Netflix Premium", amount = 1_799L)

        viewModel.selectBillForEdit(original)
        viewModel.saveBillEdits(updated)
        testScope.advanceUntilIdle()

        assertEquals(1, trackingBillDao.updatedBills.size)
        assertEquals("Netflix Premium", trackingBillDao.updatedBills.first().name)
        assertEquals(1_799L, trackingBillDao.updatedBills.first().amount)
        assertNull(viewModel.uiState.value.selectedBillForEdit)
    }

    @Test
    fun deleteBill_persistsThroughRepository() {
        val viewModel = createViewModel()
        val bill = sampleBill(id = 4L, name = "Netflix")

        viewModel.deleteBill(bill)
        testScope.advanceUntilIdle()

        assertEquals(1, trackingBillDao.deletedBills.size)
        assertEquals("Netflix", trackingBillDao.deletedBills.first().name)
    }

    @Test
    fun pullSubscription_delegatesToDeleteBill() {
        val viewModel = createViewModel()
        val bill = sampleBill(id = 5L, name = "Spotify Premium")

        viewModel.pullSubscription(bill)
        testScope.advanceUntilIdle()

        assertEquals(1, trackingBillDao.deletedBills.size)
        assertEquals(5L, trackingBillDao.deletedBills.first().id)
    }

    @Test
    fun deleteBill_clearsSelectedBillForEditWhenDeletingSameBill() {
        val viewModel = createViewModel()
        val bill = sampleBill(id = 4L, name = "Netflix")

        viewModel.selectBillForEdit(bill)
        viewModel.deleteBill(bill)
        testScope.advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedBillForEdit)
        assertEquals(1, trackingBillDao.deletedBills.size)
    }

    @Test
    fun updateMonthlyBudget_returnsFalseForInvalidInput() {
        val settingsDao = FakeSettingsDao()
        val viewModel = DashboardViewModel(
            billRepository = BillRepository(
                billDao = trackingBillDao,
                compostDao = FakeCompostDao()
            ),
            userSettingsRepository = UserSettingsRepository(settingsDao),
            forecastUseCase = ForecastUseCase(trackingBillDao),
        )

        assertFalse(viewModel.updateMonthlyBudget(""))
        assertFalse(viewModel.updateMonthlyBudget("not-a-number"))
        assertNull(settingsDao.lastSaved)
    }

    @Test
    fun updateMonthlyBudget_persistsThroughRepository() {
        val settingsDao = FakeSettingsDao()
        val viewModel = DashboardViewModel(
            billRepository = BillRepository(
                billDao = trackingBillDao,
                compostDao = FakeCompostDao()
            ),
            userSettingsRepository = UserSettingsRepository(settingsDao),
            forecastUseCase = ForecastUseCase(trackingBillDao),
        )
        testScope.advanceUntilIdle()

        assertTrue(viewModel.updateMonthlyBudget("2052"))
        testScope.advanceUntilIdle()

        assertNotNull(settingsDao.lastSaved)
        assertEquals(205_200L, settingsDao.lastSaved!!.monthlyBudget)
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(
            billRepository = BillRepository(
                billDao = trackingBillDao,
                compostDao = FakeCompostDao()
            ),
            userSettingsRepository = UserSettingsRepository(FakeSettingsDao()),
            forecastUseCase = ForecastUseCase(trackingBillDao),
        )
    }

    private fun sampleBill(
        id: Long,
        name: String,
        amount: Long = 1_000L
    ): Bill {
        return Bill(
            id = id,
            name = name,
            amount = amount,
            dueDate = LocalDate.of(2026, 9, 12),
            parentCategory = MeadowCategories.VINES,
            subCategory = "Subscriptions"
        )
    }

    private class TrackingBillDao : BillDao {
        val updatedBills = mutableListOf<BillEntity>()
        val deletedBills = mutableListOf<BillEntity>()

        override fun getAllBills(): Flow<List<BillEntity>> = flowOf(emptyList())
        override fun getUpcomingBills(today: LocalDate): Flow<List<BillEntity>> = flowOf(emptyList())
        override fun getBillById(billId: Long): Flow<BillEntity?> = flowOf(null)
        override suspend fun insertBill(bill: BillEntity): Long = 1L
        override suspend fun updateBill(bill: BillEntity) {
            updatedBills.add(bill)
        }
        override suspend fun deleteBill(bill: BillEntity) {
            deletedBills.add(bill)
        }
        override suspend fun deleteBillById(billId: Long) = Unit
        override suspend fun getBillCount(): Int = 0
        override fun getActiveSubscriptions(): Flow<List<BillEntity>> = flowOf(emptyList())
        override fun getBillsByParentCategory(parentCategory: String): Flow<List<BillEntity>> = flowOf(emptyList())
        override suspend fun getBillByIdOnce(billId: Long): BillEntity? = null
        override suspend fun getOverdueOrDueTodayUnpaidBillCount(today: LocalDate): Int = 0
        override fun getChildrenForParent(parentId: Long): Flow<List<BillEntity>> = flowOf(emptyList())
        override fun searchCompost(query: String): Flow<List<BillWithCompost>> = flowOf(emptyList())
    }

    private class FakeCompostDao : CompostDao {
        override suspend fun insertCompost(compost: CompostEntity) = Unit
        override suspend fun deleteCompostForBill(billId: Long) = Unit
    }

    private class FakeSettingsDao : UserSettingsDao {
        var lastSaved: UserSettingsEntity? = null

        override fun observeSettings(settingsId: Int): Flow<UserSettingsEntity?> =
            flowOf(UserSettingsEntity(monthlyBudget = 250_000L))
        override suspend fun upsertSettings(settings: UserSettingsEntity) {
            lastSaved = settings
        }
        override suspend fun getSettings(settingsId: Int): UserSettingsEntity? =
            UserSettingsEntity(monthlyBudget = 250_000L)
        override suspend fun getSettingsCount(settingsId: Int): Int = 1
    }
}
