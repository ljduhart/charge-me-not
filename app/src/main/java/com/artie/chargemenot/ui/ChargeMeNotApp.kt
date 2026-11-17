package com.artie.chargemenot.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.artie.chargemenot.ChargeMeNotApplication
import com.artie.chargemenot.di.AppViewModelProvider
import com.artie.chargemenot.ui.components.BloomCategoryDefinitions
import com.artie.chargemenot.ui.components.MeadowAppOverlays
import com.artie.chargemenot.ui.dashboard.DashboardViewModel
import com.artie.chargemenot.ui.motion.ScannerCircularReveal
import com.artie.chargemenot.ui.motion.ScannerFabOrigin
import com.artie.chargemenot.ui.navigation.AppRoutes
import com.artie.chargemenot.ui.navigation.ChargeMeNotNavHost
import com.artie.chargemenot.ui.navigation.MeadowBottomBar
import com.artie.chargemenot.ui.navigation.MeadowRoute
import com.artie.chargemenot.ui.navigation.SCANNER_PARENT_CATEGORY_KEY
import com.artie.chargemenot.ui.navigation.navigateMeadowRoute
import com.artie.chargemenot.ui.navigation.navigatePendingNotificationRoute
import com.artie.chargemenot.ui.screens.onboarding.OnboardingLoadingScreen
import com.artie.chargemenot.ui.viewmodels.CategoryViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChargeMeNotApp(
    application: ChargeMeNotApplication,
    pendingNavigationRoute: String?,
    onPendingNavigationConsumed: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val layoutDirection = LocalLayoutDirection.current

    var graphStartDestination by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val onboardingComplete = application.container.userSettingsRepository.getOnboardingComplete()
        graphStartDestination = if (onboardingComplete) {
            AppRoutes.MEADOW_HUB
        } else {
            AppRoutes.ONBOARDING
        }
    }

    LaunchedEffect(pendingNavigationRoute, currentRoute) {
        val route = pendingNavigationRoute ?: return@LaunchedEffect
        if (!AppRoutes.shouldConsumePendingNavigation(currentRoute)) {
            return@LaunchedEffect
        }
        if (AppRoutes.isAllowedPendingNavigationRoute(route)) {
            navController.navigatePendingNotificationRoute(route)
        }
        onPendingNavigationConsumed()
    }

    if (graphStartDestination == null) {
        OnboardingLoadingScreen(modifier = Modifier.fillMaxSize())
        return
    }

    val meadowHubEntry = remember(backStackEntry) {
        runCatching { navController.getBackStackEntry(AppRoutes.MEADOW_HUB) }.getOrNull()
    }
    val selectedBottomBarRoute = MeadowRoute.selectedBottomBarRoute(currentRoute)
    var scannerFabOrigin by remember { mutableStateOf<ScannerFabOrigin?>(null) }
    var scannerRevealVisible by remember { mutableStateOf(false) }
    var scannerRevealConsumed by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        if (currentRoute == AppRoutes.SCANNER && scannerRevealVisible) {
            scannerRevealVisible = false
        }
        if (currentRoute != AppRoutes.SCANNER) {
            scannerRevealConsumed = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0.dp),
            bottomBar = {
                if (AppRoutes.showsMeadowBottomBar(currentRoute)) {
                    MeadowBottomBar(
                        selectedRoute = selectedBottomBarRoute,
                        onNavigate = { meadowRoute ->
                            navController.navigateMeadowRoute(meadowRoute, currentRoute)
                        },
                        onScanBill = {
                            if (currentRoute != AppRoutes.SCANNER) {
                                scannerRevealConsumed = false
                                scannerRevealVisible = true
                            }
                        },
                        onScannerFabPositioned = { windowOffset, radiusPx ->
                            scannerFabOrigin = ScannerFabOrigin(
                                windowOffset = windowOffset,
                                radiusPx = radiusPx
                            )
                        }
                    )
                }
            }
        ) { innerPadding ->
            SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                ChargeMeNotNavHost(
                    navController = navController,
                    startDestination = graphStartDestination!!,
                    sharedTransitionScope = this,
                    modifier = Modifier.padding(
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection),
                        bottom = innerPadding.calculateBottomPadding()
                    )
                )
            }
        }

        ScannerCircularReveal(
            visible = scannerRevealVisible,
            origin = scannerFabOrigin,
            onExpanded = {
                if (!scannerRevealConsumed && currentRoute != AppRoutes.SCANNER) {
                    scannerRevealConsumed = true
                    navController.navigate(AppRoutes.SCANNER) {
                        launchSingleTop = true
                    }
                }
            }
        )

        if (meadowHubEntry != null) {
            MeadowHubOverlays(
                navController = navController,
                currentRoute = currentRoute,
                meadowHubEntry = meadowHubEntry
            )
        }
    }
}

@Composable
private fun MeadowHubOverlays(
    navController: NavHostController,
    currentRoute: String?,
    meadowHubEntry: NavBackStackEntry
) {
    val dashboardViewModel: DashboardViewModel = viewModel(
        viewModelStoreOwner = meadowHubEntry,
        factory = AppViewModelProvider.Factory
    )
    val categoryViewModel: CategoryViewModel = viewModel(
        viewModelStoreOwner = meadowHubEntry,
        factory = AppViewModelProvider.Factory
    )
    val dashboardUiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(currentRoute) {
        if (AppRoutes.shouldClearOverlayState(currentRoute)) {
            dashboardViewModel.clearDashboardTransientState()
        }
    }

    if (AppRoutes.showsMeadowOverlays(currentRoute)) {
        MeadowAppOverlays(
            uiState = dashboardUiState,
            categoryViewModel = categoryViewModel,
            dashboardViewModel = dashboardViewModel,
            onAddBillToCategory = { categoryName ->
                BloomCategoryDefinitions.parentNameFor(categoryName)?.let { parentName ->
                    dashboardViewModel.clearCategorySelection()
                    navController.navigate(AppRoutes.SCANNER) {
                        launchSingleTop = true
                    }
                    navController.getBackStackEntry(AppRoutes.SCANNER)
                        .savedStateHandle[SCANNER_PARENT_CATEGORY_KEY] = parentName
                }
            }
        )
    }
}
