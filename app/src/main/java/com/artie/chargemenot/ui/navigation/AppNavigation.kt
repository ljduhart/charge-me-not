package com.artie.chargemenot.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.artie.chargemenot.ui.dashboard.DashboardUiState
import com.artie.chargemenot.ui.screens.DashboardScreen
import com.artie.chargemenot.ui.screens.PruningSimulatorScreen
import com.artie.chargemenot.ui.screens.ScannerScreen
import com.artie.chargemenot.ui.viewmodels.PruningUiState
import com.artie.chargemenot.ui.viewmodels.ScannerUiState
import com.artie.chargemenot.ui.viewmodels.SettingsUiState
import com.artie.chargemenot.data.model.CrossPollinationPayload
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.scanner.OcrScanResult
import com.artie.chargemenot.domain.model.BillCategory

private const val MEADOW_TRANSITION_DURATION_MS = 320
private const val MEADOW_SLIDE_FRACTION = 12

private val meadowEnterTransition = fadeIn(
    animationSpec = tween(MEADOW_TRANSITION_DURATION_MS)
) + slideInVertically(
    animationSpec = tween(MEADOW_TRANSITION_DURATION_MS),
    initialOffsetY = { fullHeight -> fullHeight / MEADOW_SLIDE_FRACTION }
)

private val meadowExitTransition = fadeOut(
    animationSpec = tween(MEADOW_TRANSITION_DURATION_MS)
) + slideOutVertically(
    animationSpec = tween(MEADOW_TRANSITION_DURATION_MS),
    targetOffsetY = { fullHeight -> -fullHeight / MEADOW_SLIDE_FRACTION }
)

private val meadowPopEnterTransition = fadeIn(
    animationSpec = tween(MEADOW_TRANSITION_DURATION_MS)
) + slideInVertically(
    animationSpec = tween(MEADOW_TRANSITION_DURATION_MS),
    initialOffsetY = { fullHeight -> -fullHeight / MEADOW_SLIDE_FRACTION }
)

private val meadowPopExitTransition = fadeOut(
    animationSpec = tween(MEADOW_TRANSITION_DURATION_MS)
) + slideOutVertically(
    animationSpec = tween(MEADOW_TRANSITION_DURATION_MS),
    targetOffsetY = { fullHeight -> fullHeight / MEADOW_SLIDE_FRACTION }
)

@Composable
fun ChargeMeNotNavHost(
    navController: NavHostController,
    dashboardUiState: DashboardUiState,
    scannerUiState: ScannerUiState,
    settingsUiState: SettingsUiState,
    pruningUiState: PruningUiState,
    onKeepSubscription: (Bill) -> Unit,
    onPullSubscription: (Bill) -> Unit,
    onMonthlyBudgetChange: (String) -> Unit,
    onNagModeToggleRequested: (Boolean) -> Unit,
    onNotificationPermissionResult: (Boolean) -> Unit,
    onNotificationPermissionRequestHandled: () -> Unit,
    onRefreshNotificationPermissionState: () -> Unit,
    onNavigateToPruningSimulator: () -> Unit,
    onToggleBillStatus: (Long, Boolean) -> Unit,
    onResetSandbox: () -> Unit,
    onScanResult: (OcrScanResult) -> Unit,
    onQrPayloadDetected: (CrossPollinationPayload) -> Unit,
    onCategorySelected: (BillCategory) -> Unit,
    onAcceptPollinatedBill: () -> Unit,
    onDiscardPollen: () -> Unit,
    onScannerNavigateBack: () -> Unit,
    onPruningNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.DASHBOARD,
        modifier = modifier.fillMaxSize()
    ) {
        composable(
            route = AppRoutes.DASHBOARD,
            enterTransition = { meadowEnterTransition },
            exitTransition = { meadowExitTransition },
            popEnterTransition = { meadowPopEnterTransition },
            popExitTransition = { meadowPopExitTransition }
        ) {
            DashboardScreen(
                uiState = dashboardUiState,
                settingsUiState = settingsUiState,
                onKeepSubscription = onKeepSubscription,
                onPullSubscription = onPullSubscription,
                onMonthlyBudgetChange = onMonthlyBudgetChange,
                onNagModeToggleRequested = onNagModeToggleRequested,
                onNotificationPermissionResult = onNotificationPermissionResult,
                onNotificationPermissionRequestHandled = onNotificationPermissionRequestHandled,
                onRefreshNotificationPermissionState = onRefreshNotificationPermissionState,
                onNavigateToPruningSimulator = onNavigateToPruningSimulator
            )
        }

        composable(
            route = AppRoutes.PRUNING_SIMULATOR,
            enterTransition = { meadowEnterTransition },
            exitTransition = { meadowExitTransition },
            popEnterTransition = { meadowPopEnterTransition },
            popExitTransition = { meadowPopExitTransition }
        ) {
            PruningSimulatorScreen(
                uiState = pruningUiState,
                onToggleBillStatus = onToggleBillStatus,
                onResetSandbox = onResetSandbox,
                onNavigateBack = onPruningNavigateBack
            )
        }

        composable(
            route = AppRoutes.SCANNER,
            enterTransition = { meadowEnterTransition },
            exitTransition = { meadowExitTransition },
            popEnterTransition = { meadowPopEnterTransition },
            popExitTransition = { meadowPopExitTransition }
        ) {
            ScannerScreen(
                uiState = scannerUiState,
                onScanResult = onScanResult,
                onQrPayloadDetected = onQrPayloadDetected,
                onCategorySelected = onCategorySelected,
                onAcceptPollinatedBill = onAcceptPollinatedBill,
                onDiscardPollen = onDiscardPollen,
                onNavigateBack = onScannerNavigateBack
            )
        }
    }
}
