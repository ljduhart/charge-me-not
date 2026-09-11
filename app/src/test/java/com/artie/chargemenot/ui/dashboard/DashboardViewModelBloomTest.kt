package com.artie.chargemenot.ui.dashboard

import com.artie.chargemenot.data.local.BillDao
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.data.local.BillWithCompost
import com.artie.chargemenot.data.local.CompostDao
import com.artie.chargemenot.data.local.UserSettingsDao
import com.artie.chargemenot.data.local.UserSettingsEntity
import com.artie.chargemenot.data.repository.BillRepository
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.data.sensors.DeviceTiltSensor
import com.artie.chargemenot.data.sensors.StationaryDeviceTiltSensor
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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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

        assertEquals("Roots", viewModel.uiState.value.selectedCategoryForEdit)
    }

    @Test
    fun clearCategorySelection_resetsSelectedCategory() {
        val viewModel = createViewModel()
        viewModel.onPetalTapped("Fertilizer")
        viewModel.clearCategorySelection()

        assertNull(viewModel.uiState.value.selectedCategoryForEdit)
    }

    @Test
    fun selectBillForEdit_clearsSelectedCategory() {
        val viewModel = createViewModel()
        viewModel.onPetalTapped("Canopy")

        viewModel.selectBillForEdit(
            Bill(
                id = 1L,
                name = "Maple Street Apartment",
                amount = 145_000L,
                dueDate = LocalDate.of(2026, 9, 12),
                parentCategory = MeadowCategories.CANOPY,
                subCategory = "Rent"
            )
        )

        assertNull(viewModel.uiState.value.selectedCategoryForEdit)
    }

    @Test
    fun onPetalTapped_clearsSelectedBillForEdit() {
        val viewModel = createViewModel()
        val bill = Bill(
            id = 1L,
            name = "Maple Street Apartment",
            amount = 145_000L,
            dueDate = LocalDate.of(2026, 9, 12),
            parentCategory = MeadowCategories.CANOPY,
            subCategory = "Rent"
        )
        viewModel.selectBillForEdit(bill)
        viewModel.onPetalTapped("Roots")

        assertNull(viewModel.uiState.value.selectedBillForEdit)
        assertEquals("Roots", viewModel.uiState.value.selectedCategoryForEdit)
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

        assertEquals(dueDate, viewModel.uiState.value.manualBillPrefillDate)
        assertTrue(viewModel.uiState.value.isManualBillVisible)
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

        val bills = viewModel.uiState.value.categoryBills
        assertEquals(1, bills.size)
        assertEquals("Maple Street Apartment", bills.first().name)
        assertEquals(MeadowCategories.CANOPY, bills.first().parentCategory)
    }

    @Test
    fun showManualBillEntry_clearsCategorySelection() {
        val viewModel = createViewModel()
        viewModel.onPetalTapped("Fertilizer")

        viewModel.showManualBillEntry()

        assertNull(viewModel.uiState.value.selectedCategoryForEdit)
        assertTrue(viewModel.uiState.value.isManualBillVisible)
    }

    @Test
    fun showProfileEdit_clearsCategorySelection() {
        val viewModel = createViewModel()
        viewModel.onPetalTapped("Canopy")

        viewModel.showProfileEdit()

        assertNull(viewModel.uiState.value.selectedCategoryForEdit)
        assertTrue(viewModel.uiState.value.isProfileEditVisible)
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
                amount = 1_549L,
                dueDate = LocalDate.of(2026, 9, 12),
                parentCategory = MeadowCategories.VINES,
                subCategory = "Subscriptions"
            )
        )

        assertFalse(viewModel.uiState.value.isProfileEditVisible)
        assertFalse(viewModel.uiState.value.isManualBillVisible)
        assertEquals("Netflix", viewModel.uiState.value.selectedBillForEdit?.name)
    }

    @Test
    fun showManualBillEntry_incrementsSessionForFreshForm() {
        val viewModel = createViewModel()

        viewModel.showManualBillEntry()
        val firstSession = viewModel.uiState.value.manualBillEntrySession
        viewModel.dismissManualBillEntry()
        viewModel.showManualBillEntry()

        assertEquals(firstSession + 1, viewModel.uiState.value.manualBillEntrySession)
    }

    @Test
    fun toggleSearchActive_livesInConsolidatedUiState() {
        val viewModel = createViewModel()

        viewModel.toggleSearchActive()
        viewModel.onSearchQueryChanged("net")

        assertTrue(viewModel.uiState.value.isSearchActive)
        assertEquals("net", viewModel.uiState.value.searchQuery)

        viewModel.toggleSearchActive()

        assertFalse(viewModel.uiState.value.isSearchActive)
        assertEquals("", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun dismissManualBillEntry_clearsSelectedCalendarDate() {
        val viewModel = createViewModel()
        val dueDate = LocalDate.of(2026, 9, 18)

        viewModel.onCalendarDayTapped(dueDate)
        viewModel.dismissManualBillEntry()

        assertNull(viewModel.uiState.value.selectedCalendarDate)
        assertFalse(viewModel.uiState.value.isManualBillVisible)
    }

    @Test
    fun onCalendarBillTapped_opensBillEditAndHighlightsDueDate() {
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()
        val bill = viewModel.uiState.value.allBills.first()

        viewModel.onCalendarBillTapped(bill)

        assertEquals(bill.dueDate, viewModel.uiState.value.selectedCalendarDate)
        assertEquals(bill.id, viewModel.uiState.value.selectedBillForEdit?.id)
    }

    @Test
    fun observeDashboard_populatesGardenPathBillsSortedByTimelineState() {
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()

        val gardenPathBills = viewModel.uiState.value.gardenPathBills

        assertEquals(3, gardenPathBills.size)
        assertEquals("Maple Street Apartment", gardenPathBills.first().name)
        assertTrue(
            gardenPathBills.zipWithNext().all { (earlier, later) ->
                earlier.dueDate <= later.dueDate
            }
        )
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
                amount = 1_549L,
                dueDate = LocalDate.of(2026, 9, 12),
                parentCategory = MeadowCategories.VINES,
                subCategory = "Subscriptions"
            )
        )

        viewModel.clearDashboardTransientState()

        assertFalse(viewModel.uiState.value.isProfileEditVisible)
        assertFalse(viewModel.uiState.value.isManualBillVisible)
        assertNull(viewModel.uiState.value.selectedBillForEdit)
        assertNull(viewModel.uiState.value.selectedCategoryForEdit)
        assertNull(viewModel.uiState.value.highlightedBloomParent)
    }

    @Test
    fun showManualBillEntry_reopensAfterScannerClearsTransients() {
        val viewModel = createViewModel()
        viewModel.onPetalTapped("Canopy")
        viewModel.showProfileEdit()

        viewModel.clearDashboardTransientState()
        viewModel.showManualBillEntry()

        assertTrue(viewModel.uiState.value.isManualBillVisible)
        assertNull(viewModel.uiState.value.selectedCategoryForEdit)
        assertFalse(viewModel.uiState.value.isProfileEditVisible)
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

    @Test
    fun startParallaxSensor_whenAlreadyActive_doesNotRestartCollection() = runTest(testDispatcher) {
        val tiltSensor = FakeDeviceTiltSensor()
        val viewModel = createViewModel(deviceTiltSensor = tiltSensor)
        testScope.advanceUntilIdle()

        viewModel.startParallaxSensor()
        viewModel.startParallaxSensor()
        tiltSensor.emit(0.3f, 0.1f)
        testScope.advanceUntilIdle()

        assertEquals(0.3f to 0.1f, viewModel.uiState.value.parallaxOffset)
    }

    @Test
    fun stopParallaxSensor_resetsParallaxOffset() = runTest(testDispatcher) {
        val tiltSensor = FakeDeviceTiltSensor()
        val viewModel = createViewModel(deviceTiltSensor = tiltSensor)
        testScope.advanceUntilIdle()

        viewModel.startParallaxSensor()
        tiltSensor.emit(0.4f, -0.2f)
        testScope.advanceUntilIdle()
        assertEquals(0.4f to -0.2f, viewModel.uiState.value.parallaxOffset)

        viewModel.stopParallaxSensor()
        assertEquals(0f to 0f, viewModel.uiState.value.parallaxOffset)
    }

    @Test
    fun emptyGarden_exposesZeroTotalsAndNoForecast() {
        billDao = CategoryTrackingBillDao(seedBills = emptyList())
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.gardenPathBills.isEmpty())
        assertTrue(state.upcomingBills.isEmpty())
        assertTrue(state.subscriptionBills.isEmpty())
        assertTrue(state.parentCategoryTotals.isEmpty())
        assertEquals(0L, state.totalUpcoming)
        assertEquals(0, state.upcomingBillCount)
        assertNull(state.forecastResult)
        assertFalse(state.isLoading)
    }

    @Test
    fun onCleared_stopsParallaxSensor() = runTest(testDispatcher) {
        val tiltSensor = FakeDeviceTiltSensor()
        val viewModel = createViewModel(deviceTiltSensor = tiltSensor)
        testScope.advanceUntilIdle()

        viewModel.startParallaxSensor()
        tiltSensor.emit(0.2f, 0.1f)
        testScope.advanceUntilIdle()

        viewModel.stopParallaxSensor()
        assertEquals(0f to 0f, viewModel.uiState.value.parallaxOffset)
    }

    private fun createViewModel(
        deviceTiltSensor: DeviceTiltSensor = StationaryDeviceTiltSensor()
    ): DashboardViewModel {
        return DashboardViewModel(
            billRepository = BillRepository(
                billDao = billDao,
                compostDao = FakeCompostDao()
            ),
            userSettingsRepository = UserSettingsRepository(FakeSettingsDao()),
            forecastUseCase = ForecastUseCase(billDao),
            deviceTiltSensor = deviceTiltSensor,
        )
    }

    private class CategoryTrackingBillDao(
        seedBills: List<BillEntity> = defaultSeedBills()
    ) : BillDao {
        private val bills = MutableStateFlow(seedBills)

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
            MutableStateFlow(UserSettingsEntity(monthlyBudget = 250_000L, isOnboardingComplete = true))

        override suspend fun upsertSettings(settings: UserSettingsEntity) {
            lastSaved = settings
        }

        override suspend fun getSettings(settingsId: Int): UserSettingsEntity? =
            UserSettingsEntity(monthlyBudget = 250_000L, isOnboardingComplete = true)

        override suspend fun getSettingsCount(settingsId: Int): Int = 1
    }

    private class FakeDeviceTiltSensor : DeviceTiltSensor {
        private val emissions = MutableSharedFlow<Pair<Float, Float>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        override fun tiltOffsets(): Flow<Pair<Float, Float>> = emissions

        suspend fun emit(x: Float, y: Float) {
            emissions.emit(x to y)
        }
    }

    companion object {
        private fun defaultSeedBills(): List<BillEntity> = listOf(
            BillEntity(
                id = 1L,
                name = "Maple Street Apartment",
                amount = 145_000L,
                dueDate = LocalDate.of(2026, 9, 12),
                parentCategory = MeadowCategories.CANOPY,
                subCategory = "Rent"
            ),
            BillEntity(
                id = 2L,
                name = "Pacific Gas & Electric",
                amount = 9_417L,
                dueDate = LocalDate.of(2026, 9, 15),
                parentCategory = MeadowCategories.ROOT_SYSTEM,
                subCategory = "Utilities"
            ),
            BillEntity(
                id = 3L,
                name = "Hulu",
                amount = 799L,
                dueDate = LocalDate.of(2026, 9, 20),
                parentCategory = MeadowCategories.VINES,
                subCategory = "Streaming"
            )
        )
    }
}
