package com.artie.chargemenot.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowSky

@Composable
fun MeadowBottomBar(
    selectedRoute: MeadowRoute?,
    onNavigate: (MeadowRoute) -> Unit,
    onScanBill: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 3.dp
        ) {
            MeadowRoute.bottomBarLeadingRoutes.forEach { route ->
                MeadowBottomBarItem(
                    route = route,
                    selected = selectedRoute == route,
                    onClick = { onNavigate(route) }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            MeadowRoute.bottomBarTrailingRoutes.forEach { route ->
                MeadowBottomBarItem(
                    route = route,
                    selected = selectedRoute == route,
                    onClick = { onNavigate(route) }
                )
            }
        }
        FloatingActionButton(
            onClick = onScanBill,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
                .size(64.dp),
            shape = CircleShape,
            containerColor = MeadowSky,
            contentColor = MeadowGreenDark,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = stringResource(R.string.meadow_scan_bill),
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun MeadowBottomBarItem(
    route: MeadowRoute,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = route.icon,
                contentDescription = stringResource(route.titleRes)
            )
        },
        label = {
            Text(
                text = stringResource(route.bottomBarLabelRes),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
