package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.local.UserSettingsDao
import com.artie.chargemenot.data.local.UserSettingsEntity
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.domain.model.UserSettings
import com.artie.chargemenot.domain.repository.NagModeScheduler
import com.artie.chargemenot.domain.repository.NotificationPermissionGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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

class OnboardingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var settingsDao: TrackingSettingsDao
    private lateinit var nagModeScheduler: RecordingNagModeScheduler

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsDao = TrackingSettingsDao()
        nagModeScheduler = RecordingNagModeScheduler()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveOnboardingData_persistsPreferencesAndMarksComplete() {
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()

        viewModel.setBudgetEnabled(true)
        viewModel.setBudgetAmount(3_200f)
        viewModel.selectCurrency(SupportedCurrency.EUR)
        viewModel.onNotificationPermissionResult(isGranted = true)

        viewModel.saveOnboardingData(onComplete = {})
        testScope.advanceUntilIdle()

        val saved = settingsDao.lastSaved
        requireNotNull(saved)
        assertEquals(320_000L, saved.monthlyBudget)
        assertEquals("EUR", saved.selectedCurrency)
        assertTrue(saved.isNagModeEnabled)
        assertTrue(saved.isOnboardingComplete)
        assertTrue(nagModeScheduler.isEnabled)
        assertTrue(viewModel.uiState.value.isOnboardingComplete)
    }

    @Test
    fun saveOnboardingData_ignoresDuplicateSaveWhileInFlight() {
        val blockingDao = BlockingSettingsDao()
        val viewModel = OnboardingViewModel(
            userSettingsRepository = UserSettingsRepository(blockingDao),
            nagModeScheduler = nagModeScheduler,
            notificationPermissionGateway = FakeNotificationPermissionGateway(),
        )
        testScope.advanceUntilIdle()

        var completionCount = 0
        viewModel.saveOnboardingData(onComplete = { completionCount++ })
        viewModel.saveOnboardingData(onComplete = { completionCount++ })

        assertTrue(viewModel.uiState.value.isSaving)
        assertEquals(1, blockingDao.saveCount)

        blockingDao.releaseSave()
        testScope.advanceUntilIdle()

        assertEquals(1, completionCount)
        assertEquals(1, blockingDao.saveCount)
    }

    @Test
    fun saveOnboardingData_invokesOnCompleteCallback() {
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()

        var didComplete = false
        viewModel.saveOnboardingData(onComplete = { didComplete = true })
        testScope.advanceUntilIdle()

        assertTrue(didComplete)
    }

    @Test
    fun saveOnboardingData_withBudgetDisabled_usesDefaultBudget() {
        val viewModel = createViewModel()
        testScope.advanceUntilIdle()

        viewModel.setBudgetEnabled(false)
        viewModel.selectCurrency(SupportedCurrency.CAD)

        viewModel.saveOnboardingData(onComplete = {})
        testScope.advanceUntilIdle()

        val saved = requireNotNull(settingsDao.lastSaved)
        assertEquals(UserSettings.DEFAULT_MONTHLY_BUDGET, saved.monthlyBudget)
        assertEquals("CAD", saved.selectedCurrency)
        assertFalse(nagModeScheduler.isEnabled)
    }

    private fun createViewModel(): OnboardingViewModel {
        return OnboardingViewModel(
            userSettingsRepository = UserSettingsRepository(settingsDao),
            nagModeScheduler = nagModeScheduler,
            notificationPermissionGateway = FakeNotificationPermissionGateway(),
        )
    }

    private class TrackingSettingsDao : UserSettingsDao {
        var lastSaved: UserSettingsEntity? = null
        var saveCount = 0

        private val settings = MutableStateFlow(
            UserSettingsEntity(
                monthlyBudget = UserSettings.DEFAULT_MONTHLY_BUDGET,
                isOnboardingComplete = false
            )
        )

        override fun observeSettings(settingsId: Int): Flow<UserSettingsEntity?> = settings

        override suspend fun upsertSettings(settings: UserSettingsEntity) {
            lastSaved = settings
            saveCount++
            this.settings.value = settings
        }

        override suspend fun getSettings(settingsId: Int): UserSettingsEntity? = settings.value

        override suspend fun getSettingsCount(settingsId: Int): Int = 1
    }

    private class BlockingSettingsDao : UserSettingsDao {
        var saveCount = 0
        private val saveGate = CompletableDeferred<Unit>()
        private val settings = MutableStateFlow(
            UserSettingsEntity(
                monthlyBudget = UserSettings.DEFAULT_MONTHLY_BUDGET,
                isOnboardingComplete = false
            )
        )

        override fun observeSettings(settingsId: Int): Flow<UserSettingsEntity?> = settings

        override suspend fun upsertSettings(settings: UserSettingsEntity) {
            saveCount++
            saveGate.await()
            this.settings.value = settings
        }

        fun releaseSave() {
            saveGate.complete(Unit)
        }

        override suspend fun getSettings(settingsId: Int): UserSettingsEntity? = settings.value

        override suspend fun getSettingsCount(settingsId: Int): Int = 1
    }

    private class RecordingNagModeScheduler : NagModeScheduler {
        var isEnabled = false

        override fun enableNagMode() {
            isEnabled = true
        }

        override fun disableNagMode() {
            isEnabled = false
        }
    }

    private class FakeNotificationPermissionGateway : NotificationPermissionGateway {
        override fun isNotificationPermissionGranted(): Boolean = true

        override fun requiresRuntimePermission(): Boolean = false
    }
}
