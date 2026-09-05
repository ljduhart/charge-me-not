package com.artie.chargemenot

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.artie.chargemenot.ui.navigation.AppRoutes
import com.artie.chargemenot.ui.screens.DashboardScreen
import com.artie.chargemenot.ui.screens.ScannerScreen
import com.artie.chargemenot.ui.theme.ChargeMeNotTheme
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowWhite

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ChargeMeNotApplication
        val dashboardViewModel = app.dashboardViewModel
        val scannerViewModel = app.scannerViewModel

        setContent {
            ChargeMeNotTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                val dashboardUiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                val scannerUiState by scannerViewModel.uiState.collectAsStateWithLifecycle()

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
                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.DASHBOARD,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable(AppRoutes.DASHBOARD) {
                            DashboardScreen(
                                uiState = dashboardUiState,
                                onKeepSubscription = dashboardViewModel::keepSubscription,
                                onPullSubscription = dashboardViewModel::pullSubscription,
                                onMonthlyBudgetChange = dashboardViewModel::updateMonthlyBudget
                            )
                        }

                        composable(AppRoutes.SCANNER) {
                            ScannerScreen(
                                uiState = scannerUiState,
                                onScanResult = scannerViewModel::onScanResult,
                                onCategorySelected = scannerViewModel::selectCategory,
                                onNavigateBack = {
                                    scannerViewModel.resetScanSession()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
