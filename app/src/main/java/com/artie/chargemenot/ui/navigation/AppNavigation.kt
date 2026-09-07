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
import com.artie.chargemenot.ui.dashboard.DashboardBottomNavItem
import com.artie.chargemenot.ui.dashboard.DashboardUiState
import com.artie.chargemenot.ui.screens.CompostBinScreen
import com.artie.chargemenot.ui.screens.DashboardScreen
import com.artie.chargemenot.ui.screens.PruningSimulatorScreen
import com.artie.chargemenot.ui.screens.ScannerScreen
import com.artie.chargemenot.ui.screens.WeedWhackerScreen
import com.artie.chargemenot.ui.viewmodels.CompostBinUiState
import com.artie.chargemenot.ui.viewmodels.PruningUiState
import com.artie.chargemenot.ui.viewmodels.ScannerUiState
import com.artie.chargemenot.ui.viewmodels.SettingsUiState
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.ui.screens.onboarding.OnboardingScreen
import com.artie.chargemenot.ui.viewmodels.OnboardingUiState
import com.artie.chargemenot.ui.viewmodels.WeedWhackerUiState
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
    startDestination: String,
    onboardingUiState: OnboardingUiState,
    onBudgetEnabledChange: (Boolean) -> Unit,
    onBudgetAmountChange: (Float) -> Unit,
    onCurrencySelected: (SupportedCurrency) -> Unit,
    onRequestWateringSchedule: () -> Unit,
    onOnboardingNotificationPermissionResult: (Boolean) -> Unit,
    onOnboardingNotificationPermissionRequestHandled: () -> Unit,
    onRefreshOnboardingNotificationPermissionState: () -> Unit,
    onSaveOnboardingData: () -> Unit,
    dashboardUiState: DashboardUiState,
    selectedBillForEdit: Bill?,
    selectedCategoryForEdit: String?,
    categoryBills: List<Bill>,
    isProfileEditVisible: Boolean,
    isManualBillVisible: Boolean,
    manualBillEntrySession: Int,
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
    onToggleRootExpansion: (Long) -> Unit,
    onResetSandbox: () -> Unit,
    onScanResult: (OcrScanResult, String?) -> Unit,
    onQrPayloadDetected: (CrossPollinationPayload) -> Unit,
    onCategorySelected: (BillCategory) -> Unit,
    onAcceptPollinatedBill: () -> Unit,
    onDiscardPollen: () -> Unit,
    onScannerNavigateBack: () -> Unit,
    onPruningNavigateBack: () -> Unit,
    weedWhackerUiState: WeedWhackerUiState,
    onRecordAuditResponse: (Long, Boolean) -> Unit,
    onRestartAuditSession: () -> Unit,
    onWeedWhackerNavigateBack: () -> Unit,
    onNavigateToWeedWhacker: () -> Unit,
    onLinkBillToParent: (Long, Long?) -> Unit,
    onNavigateToCompostBin: () -> Unit,
    onSaveScannedBill: () -> Unit,
    compostBinUiState: CompostBinUiState,
    onCompostSearchQueryChanged: (String) -> Unit,
    onCompostBinNavigateBack: () -> Unit,
    onSelectBillForEdit: (Bill) -> Unit,
    onClearEditSelection: () -> Unit,
    onSaveBillEdits: (Bill) -> Unit,
    onPetalTapped: (String) -> Unit,
    onClearCategorySelection: () -> Unit,
    onAddBillToCategory: (String) -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    onSaveManualBill: (Bill) -> Unit,
    onShowProfileEdit: () -> Unit,
    onDismissProfileEdit: () -> Unit,
    onShowManualBillEntry: () -> Unit,
    onDismissManualBillEntry: () -> Unit,
    onSelectBottomNavItem: (DashboardBottomNavItem) -> Unit,
    onBloomSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize()
    ) {
        composable(
            route = AppRoutes.ONBOARDING,
            enterTransition = { meadowEnterTransition },
            exitTransition = { meadowExitTransition },
            popEnterTransition = { meadowPopEnterTransition },
            popExitTransition = { meadowPopExitTransition }
        ) {
            OnboardingScreen(
                uiState = onboardingUiState,
                onBudgetEnabledChange = onBudgetEnabledChange,
                onBudgetAmountChange = onBudgetAmountChange,
                onCurrencySelected = onCurrencySelected,
                onRequestWateringSchedule = onRequestWateringSchedule,
                onNotificationPermissionResult = onOnboardingNotificationPermissionResult,
                onNotificationPermissionRequestHandled = onOnboardingNotificationPermissionRequestHandled,
                onRefreshNotificationPermissionState = onRefreshOnboardingNotificationPermissionState,
                onSaveOnboardingData = onSaveOnboardingData
            )
        }

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
                selectedBillForEdit = selectedBillForEdit,
                selectedCategoryForEdit = selectedCategoryForEdit,
                categoryBills = categoryBills,
                isProfileEditVisible = isProfileEditVisible,
                isManualBillVisible = isManualBillVisible,
                manualBillEntrySession = manualBillEntrySession,
                onKeepSubscription = onKeepSubscription,
                onPullSubscription = onPullSubscription,
                onMonthlyBudgetChange = onMonthlyBudgetChange,
                onNagModeToggleRequested = onNagModeToggleRequested,
                onNotificationPermissionResult = onNotificationPermissionResult,
                onNotificationPermissionRequestHandled = onNotificationPermissionRequestHandled,
                onRefreshNotificationPermissionState = onRefreshNotificationPermissionState,
                onNavigateToPruningSimulator = onNavigateToPruningSimulator,
                onNavigateToWeedWhacker = onNavigateToWeedWhacker,
                onNavigateToCompostBin = onNavigateToCompostBin,
                onLinkBillToParent = onLinkBillToParent,
                onSelectBillForEdit = onSelectBillForEdit,
                onClearEditSelection = onClearEditSelection,
                onSaveBillEdits = onSaveBillEdits,
                onPetalTapped = onPetalTapped,
                onClearCategorySelection = onClearCategorySelection,
                onAddBillToCategory = onAddBillToCategory,
                onUpdateDisplayName = onUpdateDisplayName,
                onSaveManualBill = onSaveManualBill,
                onShowProfileEdit = onShowProfileEdit,
                onDismissProfileEdit = onDismissProfileEdit,
                onShowManualBillEntry = onShowManualBillEntry,
                onDismissManualBillEntry = onDismissManualBillEntry,
                onSelectBottomNavItem = onSelectBottomNavItem,
                onBloomSettingsClick = onBloomSettingsClick
            )
        }

        composable(
            route = AppRoutes.WEED_WHACKER,
            enterTransition = { meadowEnterTransition },
            exitTransition = { meadowExitTransition },
            popEnterTransition = { meadowPopEnterTransition },
            popExitTransition = { meadowPopExitTransition }
        ) {
            WeedWhackerScreen(
                uiState = weedWhackerUiState,
                onRecordAuditResponse = onRecordAuditResponse,
                onRestartAuditSession = onRestartAuditSession,
                onNavigateBack = onWeedWhackerNavigateBack
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
                onToggleRootExpansion = onToggleRootExpansion,
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
                onSaveScannedBill = onSaveScannedBill,
                onNavigateBack = onScannerNavigateBack
            )
        }

        composable(
            route = AppRoutes.COMPOST_BIN,
            enterTransition = { meadowEnterTransition },
            exitTransition = { meadowExitTransition },
            popEnterTransition = { meadowPopEnterTransition },
            popExitTransition = { meadowPopExitTransition }
        ) {
            CompostBinScreen(
                uiState = compostBinUiState,
                onSearchQueryChanged = onCompostSearchQueryChanged,
                onNavigateBack = onCompostBinNavigateBack
            )
        }
    }
}
