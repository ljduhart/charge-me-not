package com.artie.chargemenot

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.artie.chargemenot.ui.components.BloomCategoryDefinitions
import com.artie.chargemenot.ui.components.MeadowAppOverlays
import com.artie.chargemenot.ui.navigation.AppDrawer
import com.artie.chargemenot.ui.navigation.AppRoutes
import com.artie.chargemenot.ui.navigation.MeadowRoute
import com.artie.chargemenot.ui.screens.onboarding.OnboardingLoadingScreen
import com.artie.chargemenot.ui.navigation.ChargeMeNotNavHost
import com.artie.chargemenot.ui.navigation.navigateBackOrGardenHub
import com.artie.chargemenot.ui.theme.ChargeMeNotTheme
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowSky
import androidx.compose.ui.res.stringResource
import com.artie.chargemenot.R
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var pendingNavigationRoute by mutableStateOf<String?>(null)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingNavigationRoute = intent.getStringExtra(EXTRA_NAVIGATION_ROUTE)

        val app = application as ChargeMeNotApplication
        val dashboardViewModel = app.dashboardViewModel
        val scannerViewModel = app.scannerViewModel
        val settingsViewModel = app.settingsViewModel
        val pruningViewModel = app.pruningViewModel
        val weedWhackerViewModel = app.weedWhackerViewModel
        val compostBinViewModel = app.compostBinViewModel
        val categoryViewModel = app.categoryViewModel
        val onboardingViewModel = app.onboardingViewModel

        setContent {
            ChargeMeNotTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                val dashboardUiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                val scannerUiState by scannerViewModel.uiState.collectAsStateWithLifecycle()
                val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
                val pruningUiState by pruningViewModel.uiState.collectAsStateWithLifecycle()
                val weedWhackerUiState by weedWhackerViewModel.uiState.collectAsStateWithLifecycle()
                val compostBinUiState by compostBinViewModel.uiState.collectAsStateWithLifecycle()
                val selectedBillForEdit by dashboardViewModel.selectedBillForEdit.collectAsStateWithLifecycle()
                val selectedCategoryForEdit by dashboardViewModel.selectedCategoryForEdit.collectAsStateWithLifecycle()
                val categoryBills by dashboardViewModel.categoryBills.collectAsStateWithLifecycle()
                val isProfileEditVisible by dashboardViewModel.isProfileEditVisible.collectAsStateWithLifecycle()
                val isManualBillVisible by dashboardViewModel.isManualBillVisible.collectAsStateWithLifecycle()
                val manualBillEntrySession by dashboardViewModel.manualBillEntrySession.collectAsStateWithLifecycle()
                val manualBillPrefillDate by dashboardViewModel.manualBillPrefillDate.collectAsStateWithLifecycle()
                val onboardingUiState by onboardingViewModel.uiState.collectAsStateWithLifecycle()
                val parallaxOffset by dashboardViewModel.parallaxOffset.collectAsStateWithLifecycle()

                var graphStartDestination by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(onboardingUiState.isLoading) {
                    if (!onboardingUiState.isLoading && graphStartDestination == null) {
                        graphStartDestination = if (onboardingUiState.isOnboardingComplete) {
                            AppRoutes.DASHBOARD
                        } else {
                            AppRoutes.ONBOARDING
                        }
                    }
                }

                LaunchedEffect(currentRoute) {
                    if (currentRoute != null && currentRoute !in AppRoutes.overlayPreservingRoutes) {
                        dashboardViewModel.clearDashboardTransientState()
                    }
                }

                LaunchedEffect(pendingNavigationRoute, onboardingUiState.isLoading, onboardingUiState.isOnboardingComplete) {
                    val route = pendingNavigationRoute
                    if (route != null &&
                        !onboardingUiState.isLoading &&
                        onboardingUiState.isOnboardingComplete
                    ) {
                        if (route == AppRoutes.WEED_WHACKER) {
                            weedWhackerViewModel.restartAuditSession()
                        }
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                        pendingNavigationRoute = null
                        intent.removeExtra(EXTRA_NAVIGATION_ROUTE)
                    }
                }

                val openDrawer: () -> Unit = {
                    coroutineScope.launch {
                        drawerState.open()
                    }
                }

                val navigateMeadowRoute: (MeadowRoute) -> Unit = { meadowRoute ->
                    coroutineScope.launch {
                        drawerState.close()
                    }
                    if (currentRoute == AppRoutes.SCANNER) {
                        scannerViewModel.resetScanSession()
                    }
                    if (meadowRoute.route != currentRoute) {
                        when (meadowRoute) {
                            MeadowRoute.PruningSkills -> pruningViewModel.resetSandbox()
                            MeadowRoute.WeedWhacker -> weedWhackerViewModel.restartAuditSession()
                            else -> Unit
                        }
                        navController.navigate(meadowRoute.route) {
                            launchSingleTop = true
                            popUpTo(AppRoutes.DASHBOARD) {
                                inclusive = meadowRoute.route == AppRoutes.DASHBOARD
                                saveState = true
                            }
                            restoreState = true
                        }
                    }
                }

                if (onboardingUiState.isLoading || graphStartDestination == null) {
                    OnboardingLoadingScreen(modifier = Modifier.fillMaxSize())
                } else {
                    AppDrawer(
                        drawerState = drawerState,
                        selectedRoute = MeadowRoute.fromNavRouteOrNull(currentRoute),
                        userDisplayName = dashboardUiState.userDisplayName,
                        drawerEnabled = currentRoute != AppRoutes.ONBOARDING,
                        onNavigate = navigateMeadowRoute
                    ) {
                        MeadowAppOverlays(
                            selectedBillForEdit = selectedBillForEdit,
                            categoryViewModel = categoryViewModel,
                            selectedCategoryForEdit = selectedCategoryForEdit,
                            categoryBills = categoryBills,
                            isProfileEditVisible = isProfileEditVisible,
                            isManualBillVisible = isManualBillVisible,
                            manualBillEntrySession = manualBillEntrySession,
                            manualBillPrefillDate = manualBillPrefillDate,
                            userDisplayName = dashboardUiState.userDisplayName,
                            currency = dashboardUiState.selectedCurrency,
                            onClearEditSelection = dashboardViewModel::clearEditSelection,
                            onSaveBillEdits = dashboardViewModel::saveBillEdits,
                            onClearCategorySelection = dashboardViewModel::clearCategorySelection,
                            onAddBillToCategory = { categoryName ->
                                BloomCategoryDefinitions.parentNameFor(categoryName)?.let { parentName ->
                                    scannerViewModel.selectParentCategory(parentName)
                                    dashboardViewModel.clearCategorySelection()
                                    navController.navigate(AppRoutes.SCANNER) {
                                        launchSingleTop = true
                                    }
                                }
                            },
                            onSelectBillForEdit = dashboardViewModel::selectBillForEdit,
                            onDismissProfileEdit = dashboardViewModel::dismissProfileEdit,
                            onUpdateDisplayName = dashboardViewModel::updateDisplayName,
                            onDismissManualBillEntry = dashboardViewModel::dismissManualBillEntry,
                            onSaveManualBill = dashboardViewModel::insertManualBill
                        )

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            floatingActionButton = {
                                if (currentRoute == AppRoutes.DASHBOARD) {
                                    ExtendedFloatingActionButton(
                                        onClick = {
                                            navController.navigate(AppRoutes.SCANNER) {
                                                launchSingleTop = true
                                            }
                                        },
                                        containerColor = MeadowSky,
                                        contentColor = MeadowGreenDark,
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = stringResource(R.string.dashboard_add_bill)
                                            )
                                        },
                                        text = {
                                            Text(stringResource(R.string.dashboard_add_bill))
                                        }
                                    )
                                }
                            }
                        ) { innerPadding ->
                            ChargeMeNotNavHost(
                                navController = navController,
                                startDestination = graphStartDestination!!,
                                onboardingUiState = onboardingUiState,
                                onBudgetEnabledChange = onboardingViewModel::setBudgetEnabled,
                                onBudgetAmountChange = onboardingViewModel::setBudgetAmount,
                                onCurrencySelected = onboardingViewModel::selectCurrency,
                                onRequestWateringSchedule = onboardingViewModel::requestWateringSchedule,
                                onOnboardingNotificationPermissionResult = onboardingViewModel::onNotificationPermissionResult,
                                onOnboardingNotificationPermissionRequestHandled = onboardingViewModel::onNotificationPermissionRequestHandled,
                                onRefreshOnboardingNotificationPermissionState = onboardingViewModel::refreshNotificationPermissionState,
                                onSaveOnboardingData = {
                                    onboardingViewModel.saveOnboardingData {
                                        navController.navigate(AppRoutes.DASHBOARD) {
                                            popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                dashboardUiState = dashboardUiState,
                                scannerUiState = scannerUiState,
                                settingsUiState = settingsUiState,
                                pruningUiState = pruningUiState,
                                onKeepSubscription = dashboardViewModel::keepSubscription,
                                onPullSubscription = dashboardViewModel::pullSubscription,
                                onMonthlyBudgetChange = dashboardViewModel::updateMonthlyBudget,
                                onNagModeToggleRequested = settingsViewModel::onNagModeToggleRequested,
                                onNotificationPermissionResult = settingsViewModel::onNotificationPermissionResult,
                                onNotificationPermissionRequestHandled = settingsViewModel::onNotificationPermissionRequestHandled,
                                onRefreshNotificationPermissionState = settingsViewModel::refreshNotificationPermissionState,
                                onToggleBillStatus = pruningViewModel::toggleBillStatus,
                                onToggleRootExpansion = pruningViewModel::toggleRootExpansion,
                                onResetSandbox = pruningViewModel::resetSandbox,
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
                                onScannerNavigateBack = {
                                    scannerViewModel.resetScanSession()
                                    navController.navigateBackOrGardenHub()
                                },
                                onEnterBillManuallyFromScanner = {
                                    scannerViewModel.resetScanSession()
                                    dashboardViewModel.showManualBillEntry()
                                    navController.navigateBackOrGardenHub()
                                },
                                onPruningNavigateBack = {
                                    pruningViewModel.clearRootExpansion()
                                    navController.navigateBackOrGardenHub()
                                },
                                weedWhackerUiState = weedWhackerUiState,
                                onRecordAuditResponse = weedWhackerViewModel::recordAuditResponse,
                                onRestartAuditSession = weedWhackerViewModel::restartAuditSession,
                                onWeedWhackerNavigateBack = {
                                    navController.navigateBackOrGardenHub()
                                },
                                onLinkBillToParent = dashboardViewModel::linkBillToParent,
                                onSaveScannedBill = {
                                    scannerViewModel.saveScannedBill {
                                        navController.navigateBackOrGardenHub()
                                    }
                                },
                                compostBinUiState = compostBinUiState,
                                onCompostSearchQueryChanged = compostBinViewModel::onSearchQueryChanged,
                                onCompostBinNavigateBack = {
                                    navController.navigateBackOrGardenHub()
                                },
                                onSelectBillForEdit = dashboardViewModel::selectBillForEdit,
                                onPetalTapped = dashboardViewModel::onPetalTapped,
                                onShowProfileEdit = dashboardViewModel::showProfileEdit,
                                onToggleBillCalendarExpanded = dashboardViewModel::toggleBillCalendarExpanded,
                                onPreviousCalendarMonth = dashboardViewModel::showPreviousCalendarMonth,
                                onNextCalendarMonth = dashboardViewModel::showNextCalendarMonth,
                                onCalendarDayTapped = dashboardViewModel::onCalendarDayTapped,
                                onCalendarBillTapped = dashboardViewModel::onCalendarBillTapped,
                                onBloomSettingsClick = dashboardViewModel::openBloomSettingsEdit,
                                onOpenDrawer = openDrawer,
                                onMeadowHubNavigateBack = {
                                    navController.navigateBackOrGardenHub()
                                },
                                onShowManualBillEntry = dashboardViewModel::showManualBillEntry,
                                onDeleteBill = dashboardViewModel::deleteBill,
                                parallaxOffset = parallaxOffset,
                                onStartParallaxSensor = dashboardViewModel::startParallaxSensor,
                                onStopParallaxSensor = dashboardViewModel::stopParallaxSensor,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNavigationRoute = intent.getStringExtra(EXTRA_NAVIGATION_ROUTE)
    }

    companion object {
        const val EXTRA_NAVIGATION_ROUTE = "extra_navigation_route"
    }
}
