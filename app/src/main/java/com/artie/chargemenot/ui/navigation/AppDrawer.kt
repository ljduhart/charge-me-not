package com.artie.chargemenot.ui.navigation

import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artie.chargemenot.ui.components.MeadowDrawerSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    drawerState: DrawerState,
    selectedRoute: MeadowRoute,
    userDisplayName: String,
    drawerEnabled: Boolean,
    onNavigate: (MeadowRoute) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!drawerEnabled) {
        content()
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        modifier = modifier,
        drawerContent = {
            MeadowDrawerSheet(
                selectedRoute = selectedRoute,
                userDisplayName = userDisplayName,
                onNavigate = onNavigate
            )
        },
        content = content
    )
}
