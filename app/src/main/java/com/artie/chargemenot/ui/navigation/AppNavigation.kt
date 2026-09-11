package com.artie.chargemenot.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.artie.chargemenot.di.destinationViewModel
import com.artie.chargemenot.di.meadowHubViewModel
import com.artie.chargemenot.ui.dashboard.DashboardViewModel
import com.artie.chargemenot.ui.screens.CompostBinScreen
import com.artie.chargemenot.ui.screens.DashboardScreen
import com.artie.chargemenot.ui.screens.GreenhouseSettingsScreen
import com.artie.chargemenot.ui.screens.HarvestReportScreen
import com.artie.chargemenot.ui.screens.PetalsAndWeedsScreen
import com.artie.chargemenot.ui.screens.PruningSimulatorScreen
import com.artie.chargemenot.ui.screens.RichSoilScreen
import com.artie.chargemenot.ui.screens.ScannerScreen
import com.artie.chargemenot.ui.screens.WeedWhackerScreen
import com.artie.chargemenot.ui.screens.onboarding.OnboardingScreen
import com.artie.chargemenot.ui.viewmodels.CompostBinViewModel
import com.artie.chargemenot.ui.viewmodels.OnboardingViewModel
import com.artie.chargemenot.ui.viewmodels.PruningViewModel
import com.artie.chargemenot.ui.viewmodels.ScannerViewModel
import com.artie.chargemenot.ui.viewmodels.SettingsViewModel
import com.artie.chargemenot.ui.viewmodels.WeedWhackerViewModel

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
    onOpenDrawer: () -> Unit,
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
            val onboardingViewModel: OnboardingViewModel = destinationViewModel()
            val onboardingUiState by onboardingViewModel.uiState.collectAsStateWithLifecycle()
            OnboardingScreen(
                uiState = onboardingUiState,
                onBudgetEnabledChange = onboardingViewModel::setBudgetEnabled,
                onBudgetAmountChange = onboardingViewModel::setBudgetAmount,
                onCurrencySelected = onboardingViewModel::selectCurrency,
                onRequestWateringSchedule = onboardingViewModel::requestWateringSchedule,
                onNotificationPermissionResult = onboardingViewModel::onNotificationPermissionResult,
                onNotificationPermissionRequestHandled = onboardingViewModel::onNotificationPermissionRequestHandled,
                onRefreshNotificationPermissionState = onboardingViewModel::refreshNotificationPermissionState,
                onSaveOnboardingData = {
                    onboardingViewModel.saveOnboardingData {
                        navController.navigate(AppRoutes.MEADOW_HUB) {
                            popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        navigation(
            route = AppRoutes.MEADOW_HUB,
            startDestination = AppRoutes.DASHBOARD
        ) {
            composable(
                route = AppRoutes.DASHBOARD,
                enterTransition = { meadowEnterTransition },
                exitTransition = { meadowExitTransition },
                popEnterTransition = { meadowPopEnterTransition },
                popExitTransition = { meadowPopExitTransition }
            ) { entry ->
                val dashboardViewModel: DashboardViewModel = entry.meadowHubViewModel(navController)
                val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                DashboardScreen(
                    uiState = uiState,
                    onStartParallaxSensor = dashboardViewModel::startParallaxSensor,
                    onStopParallaxSensor = dashboardViewModel::stopParallaxSensor,
                    onOpenDrawer = onOpenDrawer,
                    onKeepSubscription = dashboardViewModel::keepSubscription,
                    onPullSubscription = dashboardViewModel::pullSubscription,
                    onMonthlyBudgetChange = dashboardViewModel::updateMonthlyBudget,
                    onLinkBillToParent = dashboardViewModel::linkBillToParent,
                    onSelectBillForEdit = dashboardViewModel::selectBillForEdit,
                    onPetalTapped = dashboardViewModel::onPetalTapped,
                    onShowProfileEdit = dashboardViewModel::showProfileEdit,
                    onToggleBillCalendarExpanded = dashboardViewModel::toggleBillCalendarExpanded,
                    onPreviousCalendarMonth = dashboardViewModel::showPreviousCalendarMonth,
                    onNextCalendarMonth = dashboardViewModel::showNextCalendarMonth,
                    onCalendarDayTapped = dashboardViewModel::onCalendarDayTapped,
                    onCalendarBillTapped = dashboardViewModel::onCalendarBillTapped,
                    onBloomSettingsClick = dashboardViewModel::openBloomSettingsEdit,
                    onToggleSearchActive = dashboardViewModel::toggleSearchActive,
                    onSearchQueryChanged = dashboardViewModel::onSearchQueryChanged,
                    onShareBill = dashboardViewModel::shareBill,
                    onDismissShareBill = dashboardViewModel::dismissShareBill,
                    onLinkBill = dashboardViewModel::linkBill,
                    onDismissLinkBill = dashboardViewModel::dismissLinkBill
                )
            }

            composable(
                route = AppRoutes.PETALS_AND_WEEDS,
                enterTransition = { meadowEnterTransition },
                exitTransition = { meadowExitTransition },
                popEnterTransition = { meadowPopEnterTransition },
                popExitTransition = { meadowPopExitTransition }
            ) { entry ->
                val dashboardViewModel: DashboardViewModel = entry.meadowHubViewModel(navController)
                val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                PetalsAndWeedsScreen(
                    gardenBills = uiState.gardenPathBills,
                    currency = uiState.selectedCurrency,
                    parallaxOffset = uiState.parallaxOffset,
                    onStartParallaxSensor = dashboardViewModel::startParallaxSensor,
                    onStopParallaxSensor = dashboardViewModel::stopParallaxSensor,
                    onOpenDrawer = onOpenDrawer,
                    onNavigateBack = { navController.navigateBackOrGardenHub() },
                    onSelectBillForEdit = dashboardViewModel::selectBillForEdit,
                    onShowManualBillEntry = { dashboardViewModel.showManualBillEntry() },
                    onDeleteBill = dashboardViewModel::deleteBill
                )
            }

            composable(
                route = AppRoutes.RICH_SOIL,
                enterTransition = { meadowEnterTransition },
                exitTransition = { meadowExitTransition },
                popEnterTransition = { meadowPopEnterTransition },
                popExitTransition = { meadowPopExitTransition }
            ) { entry ->
                val dashboardViewModel: DashboardViewModel = entry.meadowHubViewModel(navController)
                val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                RichSoilScreen(
                    monthlyBudget = uiState.monthlyBudget,
                    totalUpcoming = uiState.totalUpcoming,
                    currency = uiState.selectedCurrency,
                    onOpenDrawer = onOpenDrawer,
                    onNavigateBack = { navController.navigateBackOrGardenHub() },
                    onMonthlyBudgetChange = dashboardViewModel::updateMonthlyBudget
                )
            }

            composable(
                route = AppRoutes.HARVEST_REPORT,
                enterTransition = { meadowEnterTransition },
                exitTransition = { meadowExitTransition },
                popEnterTransition = { meadowPopEnterTransition },
                popExitTransition = { meadowPopExitTransition }
            ) { entry ->
                val dashboardViewModel: DashboardViewModel = entry.meadowHubViewModel(navController)
                val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                HarvestReportScreen(
                    forecastResult = uiState.forecastResult,
                    currency = uiState.selectedCurrency,
                    onOpenDrawer = onOpenDrawer,
                    onNavigateBack = { navController.navigateBackOrGardenHub() }
                )
            }

            composable(
                route = AppRoutes.GREENHOUSE_SETTINGS,
                enterTransition = { meadowEnterTransition },
                exitTransition = { meadowExitTransition },
                popEnterTransition = { meadowPopEnterTransition },
                popExitTransition = { meadowPopExitTransition }
            ) { entry ->
                val dashboardViewModel: DashboardViewModel = entry.meadowHubViewModel(navController)
                val settingsViewModel: SettingsViewModel = destinationViewModel()
                val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
                GreenhouseSettingsScreen(
                    userDisplayName = uiState.userDisplayName,
                    selectedCurrencyCode = uiState.selectedCurrency.code,
                    settingsUiState = settingsUiState,
                    onOpenDrawer = onOpenDrawer,
                    onNavigateBack = { navController.navigateBackOrGardenHub() },
                    onShowProfileEdit = dashboardViewModel::showProfileEdit,
                    onNagModeToggleRequested = settingsViewModel::onNagModeToggleRequested,
                    onNotificationPermissionResult = settingsViewModel::onNotificationPermissionResult,
                    onNotificationPermissionRequestHandled = settingsViewModel::onNotificationPermissionRequestHandled,
                    onRefreshNotificationPermissionState = settingsViewModel::refreshNotificationPermissionState
                )
            }
        }

        composable(
            route = AppRoutes.WEED_WHACKER,
            enterTransition = { meadowEnterTransition },
            exitTransition = { meadowExitTransition },
            popEnterTransition = { meadowPopEnterTransition },
            popExitTransition = { meadowPopExitTransition }
        ) {
            val weedWhackerViewModel: WeedWhackerViewModel = destinationViewModel()
            val weedWhackerUiState by weedWhackerViewModel.uiState.collectAsStateWithLifecycle()
            WeedWhackerScreen(
                uiState = weedWhackerUiState,
                onRecordAuditResponse = weedWhackerViewModel::recordAuditResponse,
                onRestartAuditSession = weedWhackerViewModel::restartAuditSession,
                onNavigateBack = { navController.navigateBackOrGardenHub() },
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
            val pruningViewModel: PruningViewModel = destinationViewModel()
            val pruningUiState by pruningViewModel.uiState.collectAsStateWithLifecycle()
            PruningSimulatorScreen(
                uiState = pruningUiState,
                onToggleBillStatus = pruningViewModel::toggleBillStatus,
                onToggleRootExpansion = pruningViewModel::toggleRootExpansion,
                onResetSandbox = pruningViewModel::resetSandbox,
                onNavigateBack = {
                    pruningViewModel.clearRootExpansion()
                    navController.navigateBackOrGardenHub()
                },
                onOpenDrawer = onOpenDrawer
            )
        }

        composable(
            route = AppRoutes.SCANNER,
            enterTransition = { meadowEnterTransition },
            exitTransition = { meadowExitTransition },
            popEnterTransition = { meadowPopEnterTransition },
            popExitTransition = { meadowPopExitTransition }
        ) { entry ->
            val scannerViewModel: ScannerViewModel = destinationViewModel()
            val scannerUiState by scannerViewModel.uiState.collectAsStateWithLifecycle()
            val dashboardViewModel: DashboardViewModel = entry.meadowHubViewModel(navController)
            val preselectedParent = entry.savedStateHandle.get<String>(SCANNER_PARENT_CATEGORY_KEY)
            LaunchedEffect(preselectedParent) {
                if (preselectedParent != null) {
                    scannerViewModel.selectParentCategory(preselectedParent)
                    entry.savedStateHandle.remove<String>(SCANNER_PARENT_CATEGORY_KEY)
                }
            }
            ScannerScreen(
                uiState = scannerUiState,
                onScanResult = { result, receiptImagePath ->
                    scannerViewModel.onScanResult(result, receiptImagePath)
                },
                onQrPayloadDetected = scannerViewModel::onQrPayloadDetected,
                onCategorySelected = scannerViewModel::selectParentCategory,
                onAcceptPollinatedBill = {
                    scannerViewModel.acceptPollinatedBill {
                        navController.navigateBackOrGardenHub()
                    }
                },
                onDiscardPollen = scannerViewModel::discardPollen,
                onSaveScannedBill = {
                    scannerViewModel.saveScannedBill {
                        navController.navigateBackOrGardenHub()
                    }
                },
                onNavigateBack = {
                    scannerViewModel.resetScanSession()
                    navController.navigateBackOrGardenHub()
                },
                onEnterBillManually = {
                    scannerViewModel.resetScanSession()
                    dashboardViewModel.showManualBillEntry()
                    navController.navigateBackOrGardenHub()
                }
            )
        }

        composable(
            route = AppRoutes.COMPOST_BIN,
            enterTransition = { meadowEnterTransition },
            exitTransition = { meadowExitTransition },
            popEnterTransition = { meadowPopEnterTransition },
            popExitTransition = { meadowPopExitTransition }
        ) {
            val compostBinViewModel: CompostBinViewModel = destinationViewModel()
            val compostBinUiState by compostBinViewModel.uiState.collectAsStateWithLifecycle()
            CompostBinScreen(
                uiState = compostBinUiState,
                onSearchQueryChanged = compostBinViewModel::onSearchQueryChanged,
                onNavigateBack = { navController.navigateBackOrGardenHub() },
                onOpenDrawer = onOpenDrawer
            )
        }
    }
}

fun NavController.navigateBackOrGardenHub() {
    val didPop = popBackStack()
    if (AppRoutes.shouldReturnToGardenHub(didPop)) {
        navigate(AppRoutes.MEADOW_HUB) {
            launchSingleTop = true
        }
    }
}

fun NavController.navigateMeadowRoute(
    meadowRoute: MeadowRoute,
    currentRoute: String?
) {
    if (meadowRoute.route == currentRoute) {
        return
    }
    navigate(meadowRoute.route) {
        launchSingleTop = true
        popUpTo(AppRoutes.DASHBOARD) {
            inclusive = meadowRoute.route == AppRoutes.DASHBOARD
            saveState = currentRoute in AppRoutes.overlayPreservingRoutes
        }
        restoreState = meadowRoute.route in AppRoutes.overlayPreservingRoutes
    }
}

const val SCANNER_PARENT_CATEGORY_KEY = "scanner_parent_category"
