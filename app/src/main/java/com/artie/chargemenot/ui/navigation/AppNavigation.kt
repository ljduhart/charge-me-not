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
import com.artie.chargemenot.ui.screens.CompostBinScreen
import com.artie.chargemenot.ui.screens.DashboardScreen
import com.artie.chargemenot.ui.screens.GreenhouseSettingsScreen
import com.artie.chargemenot.ui.screens.HarvestReportScreen
import com.artie.chargemenot.ui.screens.PetalsAndWeedsScreen
import com.artie.chargemenot.ui.screens.PruningSimulatorScreen
import com.artie.chargemenot.ui.screens.RichSoilScreen
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
    scannerUiState: ScannerUiState,
    settingsUiState: SettingsUiState,
    pruningUiState: PruningUiState,
    onKeepSubscription: (Bill) -> Unit,
    onPullSubscription: (Bill) -> Unit,
    onMonthlyBudgetChange: (String) -> Boolean,
    onNagModeToggleRequested: (Boolean) -> Unit,
    onNotificationPermissionResult: (Boolean) -> Unit,
    onNotificationPermissionRequestHandled: () -> Unit,
    onRefreshNotificationPermissionState: () -> Unit,
    onToggleBillStatus: (Long, Boolean) -> Unit,
    onToggleRootExpansion: (Long) -> Unit,
    onResetSandbox: () -> Unit,
    onScanResult: (OcrScanResult, String?) -> Unit,
    onQrPayloadDetected: (CrossPollinationPayload) -> Unit,
    onCategorySelected: (String) -> Unit,
    onAcceptPollinatedBill: () -> Unit,
    onDiscardPollen: () -> Unit,
    onScannerNavigateBack: () -> Unit,
    onPruningNavigateBack: () -> Unit,
    weedWhackerUiState: WeedWhackerUiState,
    onRecordAuditResponse: (Long, Boolean) -> Unit,
    onRestartAuditSession: () -> Unit,
    onWeedWhackerNavigateBack: () -> Unit,
    onLinkBillToParent: (Long, Long?) -> Unit,
    onSaveScannedBill: () -> Unit,
    compostBinUiState: CompostBinUiState,
    onCompostSearchQueryChanged: (String) -> Unit,
    onCompostBinNavigateBack: () -> Unit,
    onSelectBillForEdit: (Bill) -> Unit,
    onPetalTapped: (String) -> Unit,
    onShowProfileEdit: () -> Unit,
    onToggleBillCalendarExpanded: () -> Unit,
    onPreviousCalendarMonth: () -> Unit,
    onNextCalendarMonth: () -> Unit,
    onCalendarDayTapped: (java.time.LocalDate) -> Unit,
    onCalendarBillTapped: (Bill) -> Unit,
    onBloomSettingsClick: () -> Unit,
    onOpenDrawer: () -> Unit,
    onMeadowHubNavigateBack: () -> Unit,
    onShowManualBillEntry: () -> Unit,
    onDeleteBill: (Bill) -> Unit,
    parallaxOffset: Pair<Float, Float>,
    onStartParallaxSensor: () -> Unit,
    onStopParallaxSensor: () -> Unit,
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
                parallaxOffset = parallaxOffset,
                onStartParallaxSensor = onStartParallaxSensor,
                onStopParallaxSensor = onStopParallaxSensor,
                onOpenDrawer = onOpenDrawer,
                onKeepSubscription = onKeepSubscription,
                onPullSubscription = onPullSubscription,
                onMonthlyBudgetChange = onMonthlyBudgetChange,
                onLinkBillToParent = onLinkBillToParent,
                onSelectBillForEdit = onSelectBillForEdit,
                onPetalTapped = onPetalTapped,
                onShowProfileEdit = onShowProfileEdit,
                onToggleBillCalendarExpanded = onToggleBillCalendarExpanded,
                onPreviousCalendarMonth = onPreviousCalendarMonth,
                onNextCalendarMonth = onNextCalendarMonth,
                onCalendarDayTapped = onCalendarDayTapped,
                onCalendarBillTapped = onCalendarBillTapped,
                onBloomSettingsClick = onBloomSettingsClick
            )
        }

        composable(
            route = AppRoutes.PETALS_AND_WEEDS,
            enterTransition = { meadowEnterTransition },
            exitTransition = { meadowExitTransition },
            popEnterTransition = { meadowPopEnterTransition },
            popExitTransition = { meadowPopExitTransition }
        ) {
            PetalsAndWeedsScreen(
                gardenBills = dashboardUiState.gardenPathBills,
                parallaxOffset = parallaxOffset,
                onStartParallaxSensor = onStartParallaxSensor,
                onStopParallaxSensor = onStopParallaxSensor,
                onOpenDrawer = onOpenDrawer,
                onNavigateBack = onMeadowHubNavigateBack,
                onSelectBillForEdit = onSelectBillForEdit,
                onShowManualBillEntry = onShowManualBillEntry,
                onDeleteBill = onDeleteBill
            )
        }

        composable(
            route = AppRoutes.RICH_SOIL,
            enterTransition = { meadowEnterTransition },
            exitTransition = { meadowExitTransition },
            popEnterTransition = { meadowPopEnterTransition },
            popExitTransition = { meadowPopExitTransition }
        ) {
            RichSoilScreen(
                monthlyBudget = dashboardUiState.monthlyBudget,
                totalUpcoming = dashboardUiState.totalUpcoming,
                onOpenDrawer = onOpenDrawer,
                onNavigateBack = onMeadowHubNavigateBack,
                onMonthlyBudgetChange = onMonthlyBudgetChange
            )
        }

        composable(
            route = AppRoutes.HARVEST_REPORT,
            enterTransition = { meadowEnterTransition },
            exitTransition = { meadowExitTransition },
            popEnterTransition = { meadowPopEnterTransition },
            popExitTransition = { meadowPopExitTransition }
        ) {
            HarvestReportScreen(
                forecastResult = dashboardUiState.forecastResult,
                onOpenDrawer = onOpenDrawer,
                onNavigateBack = onMeadowHubNavigateBack
            )
        }

        composable(
            route = AppRoutes.GREENHOUSE_SETTINGS,
            enterTransition = { meadowEnterTransition },
            exitTransition = { meadowExitTransition },
            popEnterTransition = { meadowPopEnterTransition },
            popExitTransition = { meadowPopExitTransition }
        ) {
            GreenhouseSettingsScreen(
                userDisplayName = dashboardUiState.userDisplayName,
                selectedCurrencyCode = dashboardUiState.selectedCurrency,
                settingsUiState = settingsUiState,
                onOpenDrawer = onOpenDrawer,
                onNavigateBack = onMeadowHubNavigateBack,
                onShowProfileEdit = onShowProfileEdit,
                onNagModeToggleRequested = onNagModeToggleRequested,
                onNotificationPermissionResult = onNotificationPermissionResult,
                onNotificationPermissionRequestHandled = onNotificationPermissionRequestHandled,
                onRefreshNotificationPermissionState = onRefreshNotificationPermissionState
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
                onNavigateBack = onWeedWhackerNavigateBack,
                onOpenDrawer = onOpenDrawer
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
                onNavigateBack = onPruningNavigateBack,
                onOpenDrawer = onOpenDrawer
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
                onNavigateBack = onCompostBinNavigateBack,
                onOpenDrawer = onOpenDrawer
            )
        }
    }
}
