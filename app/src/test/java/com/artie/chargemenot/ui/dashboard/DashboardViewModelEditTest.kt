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
import org.junit.Assert.assertNull
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

        assertEquals(bill, viewModel.selectedBillForEdit.value)
    }

    @Test
    fun clearEditSelection_clearsSelectedBill() {
        val viewModel = createViewModel()
        viewModel.selectBillForEdit(sampleBill(id = 4L, name = "Netflix"))
        viewModel.clearEditSelection()

        assertNull(viewModel.selectedBillForEdit.value)
    }

    @Test
    fun saveBillEdits_persistsThroughRepositoryAndClearsSelection() {
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()

        val original = sampleBill(id = 4L, name = "Netflix", amount = 15.49)
        val updated = original.copy(name = "Netflix Premium", amount = 17.99)

        viewModel.selectBillForEdit(original)
        viewModel.saveBillEdits(updated)
        testScope.advanceUntilIdle()

        assertEquals(1, trackingBillDao.updatedBills.size)
        assertEquals("Netflix Premium", trackingBillDao.updatedBills.first().name)
        assertEquals(17.99, trackingBillDao.updatedBills.first().amount, 0.001)
        assertNull(viewModel.selectedBillForEdit.value)
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(
            billRepository = BillRepository(
                billDao = trackingBillDao,
                compostDao = FakeCompostDao()
            ),
            userSettingsRepository = UserSettingsRepository(FakeSettingsDao()),
            forecastUseCase = ForecastUseCase(trackingBillDao),
            coroutineScope = testScope,
            ioDispatcher = testDispatcher
        )
    }

    private fun sampleBill(
        id: Long,
        name: String,
        amount: Double = 10.0
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

        override fun getAllBills(): Flow<List<BillEntity>> = flowOf(emptyList())
        override fun getUpcomingBills(today: LocalDate): Flow<List<BillEntity>> = flowOf(emptyList())
        override fun getBillById(billId: Long): Flow<BillEntity?> = flowOf(null)
        override suspend fun insertBill(bill: BillEntity): Long = 1L
        override suspend fun updateBill(bill: BillEntity) {
            updatedBills.add(bill)
        }
        override suspend fun deleteBill(bill: BillEntity) = Unit
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
        override fun observeSettings(settingsId: Int): Flow<UserSettingsEntity?> =
            flowOf(UserSettingsEntity(monthlyBudget = 2_500.0))
        override suspend fun upsertSettings(settings: UserSettingsEntity) = Unit
        override suspend fun getSettings(settingsId: Int): UserSettingsEntity? =
            UserSettingsEntity(monthlyBudget = 2_500.0)
        override suspend fun getSettingsCount(settingsId: Int): Int = 1
    }
}
