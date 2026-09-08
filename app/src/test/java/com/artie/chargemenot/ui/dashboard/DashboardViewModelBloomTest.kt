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
import com.artie.chargemenot.domain.model.MeadowCategories
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

        viewModel.onPetalTapped("Roots")

        assertEquals("Roots", viewModel.selectedCategoryForEdit.value)
    }

    @Test
    fun clearCategorySelection_resetsSelectedCategory() {
        val viewModel = createViewModel()
        viewModel.onPetalTapped("Fertilizer")
        viewModel.clearCategorySelection()

        assertNull(viewModel.selectedCategoryForEdit.value)
    }

    @Test
    fun selectBillForEdit_clearsSelectedCategory() {
        val viewModel = createViewModel()
        viewModel.onPetalTapped("Canopy")

        viewModel.selectBillForEdit(
            Bill(
                id = 1L,
                name = "Maple Street Apartment",
                amount = 1_450.0,
                dueDate = LocalDate.of(2026, 9, 12),
                parentCategory = MeadowCategories.CANOPY,
                subCategory = "Rent"
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
            parentCategory = MeadowCategories.CANOPY,
            subCategory = "Rent"
        )
        viewModel.selectBillForEdit(bill)
        viewModel.onPetalTapped("Roots")

        assertNull(viewModel.selectedBillForEdit.value)
        assertEquals("Roots", viewModel.selectedCategoryForEdit.value)
    }

    @Test
    fun onPetalTapped_highlightsBloomCategory() {
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()

        viewModel.onPetalTapped("Roots")

        assertEquals(MeadowCategories.ROOT_SYSTEM, viewModel.uiState.value.highlightedBloomParent)
    }

    @Test
    fun onCalendarDayTapped_opensManualBillWithPrefillDate() {
        val viewModel = createViewModel()
        val dueDate = LocalDate.of(2026, 9, 18)

        viewModel.onCalendarDayTapped(dueDate)

        assertEquals(dueDate, viewModel.manualBillPrefillDate.value)
        assertTrue(viewModel.isManualBillVisible.value)
        assertEquals(dueDate, viewModel.uiState.value.selectedCalendarDate)
    }

    @Test
    fun toggleBillCalendarExpanded_flipsExpandedState() {
        val viewModel = createViewModel()

        viewModel.toggleBillCalendarExpanded()
        assertTrue(viewModel.uiState.value.isBillCalendarExpanded)

        viewModel.toggleBillCalendarExpanded()
        assertFalse(viewModel.uiState.value.isBillCalendarExpanded)
    }

    @Test
    fun categoryBills_emitsBillsForSelectedCategory() {
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()

        viewModel.onPetalTapped("Canopy")
        testScope.advanceUntilIdle()

        val bills = viewModel.categoryBills.value
        assertEquals(1, bills.size)
        assertEquals("Maple Street Apartment", bills.first().name)
        assertEquals(MeadowCategories.CANOPY, bills.first().parentCategory)
    }

    @Test
    fun showManualBillEntry_clearsCategorySelection() {
        val viewModel = createViewModel()
        viewModel.onPetalTapped("Fertilizer")

        viewModel.showManualBillEntry()

        assertNull(viewModel.selectedCategoryForEdit.value)
        assertTrue(viewModel.isManualBillVisible.value)
    }

    @Test
    fun showProfileEdit_clearsCategorySelection() {
        val viewModel = createViewModel()
        viewModel.onPetalTapped("Canopy")

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
                parentCategory = MeadowCategories.VINES,
                subCategory = "Subscriptions"
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
    fun dismissManualBillEntry_clearsSelectedCalendarDate() {
        val viewModel = createViewModel()
        val dueDate = LocalDate.of(2026, 9, 18)

        viewModel.onCalendarDayTapped(dueDate)
        viewModel.dismissManualBillEntry()

        assertNull(viewModel.uiState.value.selectedCalendarDate)
        assertFalse(viewModel.isManualBillVisible.value)
    }

    @Test
    fun onCalendarBillTapped_opensBillEditAndHighlightsDueDate() {
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()
        val bill = viewModel.uiState.value.allBills.first()

        viewModel.onCalendarBillTapped(bill)

        assertEquals(bill.dueDate, viewModel.uiState.value.selectedCalendarDate)
        assertEquals(bill.id, viewModel.selectedBillForEdit.value?.id)
    }

    @Test
    fun clearDashboardTransientState_resetsAllOverlaySelections() {
        val viewModel = createViewModel()
        viewModel.showProfileEdit()
        viewModel.onPetalTapped("Canopy")
        viewModel.selectBillForEdit(
            Bill(
                id = 1L,
                name = "Netflix",
                amount = 15.49,
                dueDate = LocalDate.of(2026, 9, 12),
                parentCategory = MeadowCategories.VINES,
                subCategory = "Subscriptions"
            )
        )

        viewModel.clearDashboardTransientState()

        assertFalse(viewModel.isProfileEditVisible.value)
        assertFalse(viewModel.isManualBillVisible.value)
        assertNull(viewModel.selectedBillForEdit.value)
        assertNull(viewModel.selectedCategoryForEdit.value)
        assertNull(viewModel.uiState.value.highlightedBloomParent)
    }

    @Test
    fun clearDashboardTransientState_clearsBloomHighlight() {
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()

        viewModel.onPetalTapped("Roots")
        assertEquals(MeadowCategories.ROOT_SYSTEM, viewModel.uiState.value.highlightedBloomParent)

        viewModel.clearDashboardTransientState()

        assertNull(viewModel.uiState.value.highlightedBloomParent)
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

    @Test
    fun subscriptionBills_includesAllUnpaidVinesParentBills() {
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()

        val subscriptions = viewModel.uiState.value.subscriptionBills
        assertTrue(
            subscriptions.any { bill ->
                bill.parentCategory == MeadowCategories.VINES && bill.subCategory == "Streaming"
            }
        )
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
                    parentCategory = MeadowCategories.CANOPY,
                    subCategory = "Rent"
                ),
                BillEntity(
                    id = 2L,
                    name = "Pacific Gas & Electric",
                    amount = 94.17,
                    dueDate = LocalDate.of(2026, 9, 15),
                    parentCategory = MeadowCategories.ROOT_SYSTEM,
                    subCategory = "Utilities"
                ),
                BillEntity(
                    id = 3L,
                    name = "Hulu",
                    amount = 7.99,
                    dueDate = LocalDate.of(2026, 9, 20),
                    parentCategory = MeadowCategories.VINES,
                    subCategory = "Streaming"
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
                entities.filter { bill -> bill.parentCategory == MeadowCategories.VINES && !bill.isPaid }
            }

        override fun getBillsByParentCategory(parentCategory: String): Flow<List<BillEntity>> =
            bills.map { entities ->
                entities.filter { bill ->
                    bill.parentCategory == parentCategory && !bill.isPaid
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
