package com.artie.chargemenot

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.artie.chargemenot.ui.navigation.AppRoutes
import com.artie.chargemenot.ui.navigation.ChargeMeNotNavHost
import com.artie.chargemenot.ui.theme.ChargeMeNotTheme
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowWhite

class MainActivity : ComponentActivity() {

    private var pendingNavigationRoute by mutableStateOf<String?>(null)

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

        setContent {
            ChargeMeNotTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                val dashboardUiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                val scannerUiState by scannerViewModel.uiState.collectAsStateWithLifecycle()
                val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
                val pruningUiState by pruningViewModel.uiState.collectAsStateWithLifecycle()
                val weedWhackerUiState by weedWhackerViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(pendingNavigationRoute) {
                    val route = pendingNavigationRoute
                    if (route != null) {
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                        pendingNavigationRoute = null
                        intent.removeExtra(EXTRA_NAVIGATION_ROUTE)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        if (currentRoute == AppRoutes.DASHBOARD) {
                            FloatingActionButton(
                                onClick = {
                                    navController.navigate(AppRoutes.SCANNER) {
                                        launchSingleTop = true
                                    }
                                },
                                containerColor = MeadowGreen,
                                contentColor = MeadowWhite
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = "Scan bill"
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    ChargeMeNotNavHost(
                        navController = navController,
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
                        onNavigateToPruningSimulator = {
                            pruningViewModel.resetSandbox()
                            navController.navigate(AppRoutes.PRUNING_SIMULATOR) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToWeedWhacker = {
                            weedWhackerViewModel.restartAuditSession()
                            navController.navigate(AppRoutes.WEED_WHACKER) {
                                launchSingleTop = true
                            }
                        },
                        onToggleBillStatus = pruningViewModel::toggleBillStatus,
                        onResetSandbox = pruningViewModel::resetSandbox,
                        onScanResult = scannerViewModel::onScanResult,
                        onQrPayloadDetected = scannerViewModel::onQrPayloadDetected,
                        onCategorySelected = scannerViewModel::selectCategory,
                        onAcceptPollinatedBill = {
                            scannerViewModel.acceptPollinatedBill {
                                navController.popBackStack()
                            }
                        },
                        onDiscardPollen = scannerViewModel::discardPollen,
                        onScannerNavigateBack = {
                            scannerViewModel.resetScanSession()
                            navController.popBackStack()
                        },
                        onPruningNavigateBack = {
                            navController.popBackStack()
                        },
                        weedWhackerUiState = weedWhackerUiState,
                        onRecordAuditResponse = weedWhackerViewModel::recordAuditResponse,
                        onRestartAuditSession = weedWhackerViewModel::restartAuditSession,
                        onWeedWhackerNavigateBack = {
                            navController.popBackStack()
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
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
