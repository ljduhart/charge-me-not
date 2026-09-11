package com.artie.chargemenot.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.artie.chargemenot.ChargeMeNotApplication
import com.artie.chargemenot.R
import com.artie.chargemenot.di.AppViewModelProvider
import com.artie.chargemenot.domain.model.UserSettings
import com.artie.chargemenot.ui.components.BloomCategoryDefinitions
import com.artie.chargemenot.ui.components.MeadowAppOverlays
import com.artie.chargemenot.ui.dashboard.DashboardViewModel
import com.artie.chargemenot.ui.navigation.AppDrawer
import com.artie.chargemenot.ui.navigation.AppRoutes
import com.artie.chargemenot.ui.navigation.ChargeMeNotNavHost
import com.artie.chargemenot.ui.navigation.MeadowRoute
import com.artie.chargemenot.ui.navigation.SCANNER_PARENT_CATEGORY_KEY
import com.artie.chargemenot.ui.navigation.navigateMeadowRoute
import com.artie.chargemenot.ui.screens.onboarding.OnboardingLoadingScreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowSky
import com.artie.chargemenot.ui.viewmodels.CategoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargeMeNotApp(
    application: ChargeMeNotApplication,
    pendingNavigationRoute: String?,
    onPendingNavigationConsumed: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val userSettings by application.container.userSettingsRepository
        .observeUserSettings()
        .collectAsStateWithLifecycle(initialValue = UserSettings())

    var graphStartDestination by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val onboardingComplete = application.container.userSettingsRepository.getOnboardingComplete()
        graphStartDestination = if (onboardingComplete) {
            AppRoutes.MEADOW_HUB
        } else {
            AppRoutes.ONBOARDING
        }
    }

    LaunchedEffect(pendingNavigationRoute, graphStartDestination) {
        val route = pendingNavigationRoute
        if (route != null && graphStartDestination != null && graphStartDestination != AppRoutes.ONBOARDING) {
            navController.navigate(route) {
                launchSingleTop = true
            }
            onPendingNavigationConsumed()
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
        navController.navigateMeadowRoute(meadowRoute, currentRoute)
    }

    if (graphStartDestination == null) {
        OnboardingLoadingScreen(modifier = Modifier.fillMaxSize())
        return
    }

    val meadowHubEntry = remember(backStackEntry) {
        runCatching { navController.getBackStackEntry(AppRoutes.MEADOW_HUB) }.getOrNull()
    }

    AppDrawer(
        drawerState = drawerState,
        selectedRoute = MeadowRoute.fromNavRouteOrNull(currentRoute),
        userDisplayName = userSettings.displayName,
        drawerEnabled = currentRoute != AppRoutes.ONBOARDING,
        onNavigate = navigateMeadowRoute
    ) {
        if (meadowHubEntry != null) {
            MeadowHubOverlays(
                navController = navController,
                currentRoute = currentRoute
            )
        }

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
                onOpenDrawer = openDrawer,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun MeadowHubOverlays(
    navController: NavHostController,
    currentRoute: String?
) {
    val meadowHubEntry = remember(currentRoute) {
        navController.getBackStackEntry(AppRoutes.MEADOW_HUB)
    }
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
        if (currentRoute != null && currentRoute !in AppRoutes.overlayPreservingRoutes) {
            dashboardViewModel.clearDashboardTransientState()
        }
    }

    if (currentRoute in AppRoutes.overlayPreservingRoutes) {
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
