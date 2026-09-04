package com.artie.chargemenot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artie.chargemenot.ui.screens.DashboardScreen
import com.artie.chargemenot.ui.theme.ChargeMeNotTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ChargeMeNotApplication
        val viewModel = app.dashboardViewModel

        setContent {
            ChargeMeNotTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DashboardScreen(
                        uiState = uiState,
                        onKeepSubscription = viewModel::keepSubscription,
                        onPullSubscription = viewModel::pullSubscription,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
