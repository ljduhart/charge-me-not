package com.artie.chargemenot

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.artie.chargemenot.ui.ChargeMeNotApp
import com.artie.chargemenot.ui.theme.ChargeMeNotTheme

class MainActivity : ComponentActivity() {

    private var pendingNavigationRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingNavigationRoute = intent.getStringExtra(EXTRA_NAVIGATION_ROUTE)

        val app = application as ChargeMeNotApplication

        setContent {
            ChargeMeNotTheme {
                ChargeMeNotApp(
                    application = app,
                    pendingNavigationRoute = pendingNavigationRoute,
                    onPendingNavigationConsumed = {
                        pendingNavigationRoute = null
                        intent.removeExtra(EXTRA_NAVIGATION_ROUTE)
                    }
                )
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
