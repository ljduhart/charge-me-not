package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.local.UserSettingsDao
import com.artie.chargemenot.data.local.UserSettingsEntity
import com.artie.chargemenot.data.repository.UserSettingsRepository
import com.artie.chargemenot.domain.repository.NagModeScheduler
import com.artie.chargemenot.domain.repository.NotificationPermissionGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @Test
  fun onNagModeToggleRequested_enablesSchedulerWhenPermissionGranted() {
    val scheduler = FakeNagModeScheduler()
    val viewModel = createViewModel(
      scheduler = scheduler,
      permissionGateway = FakeNotificationPermissionGateway(granted = true)
    )

    viewModel.onNagModeToggleRequested(isEnabled = true)
    testScope.advanceUntilIdle()

    assertTrue(scheduler.isEnabled)
  }

  @Test
  fun onNagModeToggleRequested_requestsPermissionWhenDenied() {
    val scheduler = FakeNagModeScheduler()
    val viewModel = createViewModel(
      scheduler = scheduler,
      permissionGateway = FakeNotificationPermissionGateway(
        granted = false,
        requiresRuntime = true
      )
    )

    viewModel.onNagModeToggleRequested(isEnabled = true)

    assertTrue(viewModel.uiState.value.shouldRequestNotificationPermission)
    assertTrue(viewModel.uiState.value.showNotificationPermissionWarning)
    assertFalse(scheduler.isEnabled)
  }

  @Test
  fun onNotificationPermissionResult_enablesNagModeWhenGranted() {
    val scheduler = FakeNagModeScheduler()
    val viewModel = createViewModel(
      scheduler = scheduler,
      permissionGateway = FakeNotificationPermissionGateway(
        granted = false,
        requiresRuntime = true
      )
    )

    viewModel.onNotificationPermissionResult(isGranted = true)
    testScope.advanceUntilIdle()

    assertTrue(scheduler.isEnabled)
    assertTrue(viewModel.uiState.value.notificationPermissionGranted)
  }

  @Test
  fun onNagModeToggleRequested_preservesPermissionRequestFlagAfterCombineEmissions() {
    val scheduler = FakeNagModeScheduler()
    val viewModel = createViewModel(
      scheduler = scheduler,
      permissionGateway = FakeNotificationPermissionGateway(
        granted = false,
        requiresRuntime = true
      )
    )
    testScope.advanceUntilIdle()

    viewModel.onNagModeToggleRequested(isEnabled = true)
    testScope.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.shouldRequestNotificationPermission)
    assertTrue(viewModel.uiState.value.nagModePermissionBlocked)
    assertFalse(scheduler.isEnabled)
  }

  @Test
  fun onNotificationPermissionRequestHandled_clearsPermissionRequestFlag() {
    val viewModel = createViewModel(
      scheduler = FakeNagModeScheduler(),
      permissionGateway = FakeNotificationPermissionGateway(
        granted = false,
        requiresRuntime = true
      )
    )
    testScope.advanceUntilIdle()

    viewModel.onNagModeToggleRequested(isEnabled = true)
    testScope.advanceUntilIdle()
    viewModel.onNotificationPermissionRequestHandled()
    testScope.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.shouldRequestNotificationPermission)
  }

  @Test
  fun onNagModeToggleRequested_disablesSchedulerWhenTurnedOff() {
    val scheduler = FakeNagModeScheduler()
    val viewModel = createViewModel(
      scheduler = scheduler,
      permissionGateway = FakeNotificationPermissionGateway(granted = true)
    )

    viewModel.onNagModeToggleRequested(isEnabled = true)
    testScope.advanceUntilIdle()
    viewModel.onNagModeToggleRequested(isEnabled = false)
    testScope.advanceUntilIdle()

    assertFalse(scheduler.isEnabled)
  }

  private fun createViewModel(
    scheduler: FakeNagModeScheduler,
    permissionGateway: FakeNotificationPermissionGateway
  ): SettingsViewModel {
    return SettingsViewModel(
      userSettingsRepository = UserSettingsRepository(FakeUserSettingsDao()),
      nagModeScheduler = scheduler,
      notificationPermissionGateway = permissionGateway,
      coroutineScope = testScope,
      ioDispatcher = testDispatcher
    )
  }

  private class FakeNagModeScheduler : NagModeScheduler {
    var isEnabled: Boolean = false
      private set

    override fun enableNagMode() {
      isEnabled = true
    }

    override fun disableNagMode() {
      isEnabled = false
    }
  }

  private class FakeNotificationPermissionGateway(
    private var granted: Boolean,
    private val requiresRuntime: Boolean = false
  ) : NotificationPermissionGateway {

    override fun isNotificationPermissionGranted(): Boolean = granted

    override fun requiresRuntimePermission(): Boolean = requiresRuntime
  }

  private class FakeUserSettingsDao : UserSettingsDao {
    private val settings = MutableStateFlow(
      UserSettingsEntity(
        monthlyBudget = com.artie.chargemenot.domain.model.UserSettings.DEFAULT_MONTHLY_BUDGET,
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
