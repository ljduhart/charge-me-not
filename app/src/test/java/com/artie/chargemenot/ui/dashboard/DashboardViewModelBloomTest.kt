package com.artie.chargemenot.ui.dashboard

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.data.local.BillWithCompost
import com.artie.chargemenot.data.local.CompostDao
import com.artie.chargemenot.data.local.UserSettingsDao
import com.artie.chargemenot.data.local.UserSettingsEntity
import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.domain.usecase.ForecastUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class DashboardViewModelBloomTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var billDao: CategoryTrackingBillDao

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        billDao = CategoryTrackingBillDao()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onPetalTapped_setsSelectedCategory() {
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()

        viewModel.onPetalTapped("Utilities")

        assertEquals("Utilities", viewModel.selectedCategoryForEdit.value)
    }

    @Test
    fun clearCategorySelection_resetsSelectedCategory() {
        val viewModel = createViewModel()
        viewModel.onPetalTapped("Food")
        viewModel.clearCategorySelection()

        assertNull(viewModel.selectedCategoryForEdit.value)
    }

    @Test
    fun selectBillForEdit_clearsSelectedCategory() {
        val viewModel = createViewModel()
        viewModel.onPetalTapped("Rent")

        viewModel.selectBillForEdit(
            Bill(
                id = 1L,
                name = "Maple Street Apartment",
                amount = 1_450.0,
                dueDate = LocalDate.of(2026, 9, 12),
                category = BillCategory.RENT
            )
        )

        assertNull(viewModel.selectedCategoryForEdit.value)
    }

    @Test
    fun onPetalTapped_clearsSelectedBillForEdit() {
        val viewModel = createViewModel()
        val bill = Bill(
            id = 1L,
            name = "Maple Street Apartment",
            amount = 1_450.0,
            dueDate = LocalDate.of(2026, 9, 12),
            category = BillCategory.RENT
        )
        viewModel.selectBillForEdit(bill)
        viewModel.onPetalTapped("Utilities")

        assertNull(viewModel.selectedBillForEdit.value)
        assertEquals("Utilities", viewModel.selectedCategoryForEdit.value)
    }

    @Test
    fun selectBottomNavItem_clearsSelectedCategory() {
        val viewModel = createViewModel()
        viewModel.onPetalTapped("Food")

        viewModel.selectBottomNavItem(DashboardBottomNavItem.RENT)

        assertNull(viewModel.selectedCategoryForEdit.value)
        assertEquals(DashboardBottomNavItem.RENT, viewModel.uiState.value.selectedBottomNavItem)
    }

    @Test
    fun categoryBills_emitsBillsForSelectedCategory() {
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()

        viewModel.onPetalTapped("Rent")
        testScope.advanceUntilIdle()

        val bills = viewModel.categoryBills.value
        assertEquals(1, bills.size)
        assertEquals("Maple Street Apartment", bills.first().name)
        assertEquals(BillCategory.RENT, bills.first().category)
    }

    @Test
    fun showManualBillEntry_clearsCategorySelection() {
        val viewModel = createViewModel()
        viewModel.onPetalTapped("Food")

        viewModel.showManualBillEntry()

        assertNull(viewModel.selectedCategoryForEdit.value)
        assertTrue(viewModel.isManualBillVisible.value)
    }

    @Test
    fun showProfileEdit_clearsCategorySelection() {
        val viewModel = createViewModel()
        viewModel.onPetalTapped("Rent")

        viewModel.showProfileEdit()

        assertNull(viewModel.selectedCategoryForEdit.value)
        assertTrue(viewModel.isProfileEditVisible.value)
    }

    @Test
    fun selectBillForEdit_dismissesProfileAndManualOverlays() {
        val viewModel = createViewModel()
        viewModel.showProfileEdit()
        viewModel.showManualBillEntry()

        viewModel.selectBillForEdit(
            Bill(
                id = 1L,
                name = "Netflix",
                amount = 15.49,
                dueDate = LocalDate.of(2026, 9, 12),
                category = BillCategory.SUBSCRIPTIONS
            )
        )

        assertFalse(viewModel.isProfileEditVisible.value)
        assertFalse(viewModel.isManualBillVisible.value)
        assertEquals("Netflix", viewModel.selectedBillForEdit.value?.name)
    }

    @Test
    fun showManualBillEntry_incrementsSessionForFreshForm() {
        val viewModel = createViewModel()

        viewModel.showManualBillEntry()
        val firstSession = viewModel.manualBillEntrySession.value
        viewModel.dismissManualBillEntry()
        viewModel.showManualBillEntry()

        assertEquals(firstSession + 1, viewModel.manualBillEntrySession.value)
    }

    @Test
    fun clearDashboardTransientState_resetsAllOverlaySelections() {
        val viewModel = createViewModel()
        viewModel.showProfileEdit()
        viewModel.onPetalTapped("Rent")
        viewModel.selectBillForEdit(
            Bill(
                id = 1L,
                name = "Netflix",
                amount = 15.49,
                dueDate = LocalDate.of(2026, 9, 12),
                category = BillCategory.SUBSCRIPTIONS
            )
        )

        viewModel.clearDashboardTransientState()

        assertFalse(viewModel.isProfileEditVisible.value)
        assertFalse(viewModel.isManualBillVisible.value)
        assertNull(viewModel.selectedBillForEdit.value)
        assertNull(viewModel.selectedCategoryForEdit.value)
    }

    @Test
    fun updateDisplayName_persistsThroughRepository() {
        val settingsDao = FakeSettingsDao()
        val viewModel = DashboardViewModel(
            billRepository = BillRepository(
                billDao = CategoryTrackingBillDao(),
                compostDao = FakeCompostDao()
            ),
            userSettingsRepository = UserSettingsRepository(settingsDao),
            forecastUseCase = ForecastUseCase(CategoryTrackingBillDao()),
            coroutineScope = testScope,
            ioDispatcher = testDispatcher
        )
        testScope.advanceUntilIdle()

        viewModel.updateDisplayName("Morgan")
        testScope.advanceUntilIdle()

        assertEquals("Morgan", settingsDao.lastSaved?.displayName)
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(
            billRepository = BillRepository(
                billDao = billDao,
                compostDao = FakeCompostDao()
            ),
            userSettingsRepository = UserSettingsRepository(FakeSettingsDao()),
            forecastUseCase = ForecastUseCase(billDao),
            coroutineScope = testScope,
            ioDispatcher = testDispatcher
        )
    }

    private class CategoryTrackingBillDao : BillDao {
        private val bills = MutableStateFlow(
            listOf(
                BillEntity(
                    id = 1L,
                    name = "Maple Street Apartment",
                    amount = 1_450.0,
                    dueDate = LocalDate.of(2026, 9, 12),
                    category = BillCategory.RENT
                ),
                BillEntity(
                    id = 2L,
                    name = "Pacific Gas & Electric",
                    amount = 94.17,
                    dueDate = LocalDate.of(2026, 9, 15),
                    category = BillCategory.UTILITIES
                )
            )
        )

        override fun getAllBills(): Flow<List<BillEntity>> = bills

        override fun getUpcomingBills(today: LocalDate): Flow<List<BillEntity>> = bills

        override fun getBillById(billId: Long): Flow<BillEntity?> =
            bills.map { entities -> entities.firstOrNull { bill -> bill.id == billId } }

        override suspend fun insertBill(bill: BillEntity): Long = 1L

        override suspend fun updateBill(bill: BillEntity) = Unit

        override suspend fun deleteBill(bill: BillEntity) = Unit

        override suspend fun deleteBillById(billId: Long) = Unit

        override suspend fun getBillCount(): Int = bills.value.size

        override fun getActiveSubscriptions(): Flow<List<BillEntity>> =
            bills.map { entities ->
                entities.filter { bill -> bill.category == BillCategory.SUBSCRIPTIONS }
            }

        override fun getBillsByCategory(category: String): Flow<List<BillEntity>> =
            bills.map { entities ->
                entities.filter { bill ->
                    bill.category.name == category && !bill.isPaid
                }
            }

        override suspend fun getBillByIdOnce(billId: Long): BillEntity? =
            bills.value.firstOrNull { bill -> bill.id == billId }

        override suspend fun getOverdueOrDueTodayUnpaidBillCount(today: LocalDate): Int = 0

        override fun getChildrenForParent(parentId: Long): Flow<List<BillEntity>> =
            bills.map { entities -> entities.filter { bill -> bill.parentBillId == parentId } }

        override fun searchCompost(query: String): Flow<List<BillWithCompost>> =
            MutableStateFlow(emptyList())
    }

    private class FakeCompostDao : CompostDao {
        override suspend fun insertCompost(compost: com.artie.chargemenot.data.local.CompostEntity) = Unit
        override suspend fun deleteCompostForBill(billId: Long) = Unit
    }

    private class FakeSettingsDao : UserSettingsDao {
        var lastSaved: UserSettingsEntity? = null

        override fun observeSettings(settingsId: Int): Flow<UserSettingsEntity?> =
            MutableStateFlow(UserSettingsEntity(monthlyBudget = 2_500.0, isOnboardingComplete = true))

        override suspend fun upsertSettings(settings: UserSettingsEntity) {
            lastSaved = settings
        }

        override suspend fun getSettings(settingsId: Int): UserSettingsEntity? =
            UserSettingsEntity(monthlyBudget = 2_500.0, isOnboardingComplete = true)

        override suspend fun getSettingsCount(settingsId: Int): Int = 1
    }
}
