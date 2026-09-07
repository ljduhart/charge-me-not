package com.artie.chargemenot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.ui.navigation.MeadowRoute

private val MeadowDrawerCream = Color(0xFFF9F9F4)
private val MeadowForestGreen = Color(0xFF1B3B22)
private val MeadowEmerald = Color(0xFF2E7D32)
private val MeadowSelectedContainer = Color(0x334CAF50)
private val MeadowDividerBrown = Color(0xFFD7CCC8)
private val MeadowHeaderSage = Color(0xFF9CAF88)

@Composable
fun MeadowDrawerSheet(
    selectedRoute: MeadowRoute,
    userDisplayName: String,
    onNavigate: (MeadowRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = MeadowDrawerCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            MeadowDrawerHeader(
                userDisplayName = userDisplayName,
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(
                    items = MeadowRoute.coreDailyRoutes,
                    key = { route -> route.name }
                ) { route ->
                    MeadowDrawerNavItem(
                        route = route,
                        selected = selectedRoute == route,
                        onClick = { onNavigate(route) }
                    )
                }

                item(key = "meadow_drawer_divider") {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                        color = MeadowDividerBrown
                    )
                }

                items(
                    items = MeadowRoute.analyticalRoutes,
                    key = { route -> route.name }
                ) { route ->
                    MeadowDrawerNavItem(
                        route = route,
                        selected = selectedRoute == route,
                        onClick = { onNavigate(route) }
                    )
                }

                item(key = "meadow_drawer_bottom_spacer") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun MeadowDrawerHeader(
    userDisplayName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(elevation = 4.dp)
            .background(MeadowHeaderSage.copy(alpha = 0.35f))
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        MeadowFlowerLogo(petalColor = MeadowEmerald)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MeadowForestGreen,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.dashboard_welcome_back, userDisplayName),
            style = MaterialTheme.typography.bodyLarge,
            color = MeadowForestGreen,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun MeadowDrawerNavItem(
    route: MeadowRoute,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = stringResource(route.titleRes),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        selected = selected,
        onClick = onClick,
        icon = {
            androidx.compose.material3.Icon(
                imageVector = route.icon,
                contentDescription = stringResource(route.titleRes)
            )
        },
        shape = RoundedCornerShape(28.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MeadowSelectedContainer,
            unselectedContainerColor = Color.Transparent,
            selectedIconColor = MeadowEmerald,
            selectedTextColor = MeadowEmerald,
            unselectedIconColor = MeadowForestGreen,
            unselectedTextColor = MeadowForestGreen
        ),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
