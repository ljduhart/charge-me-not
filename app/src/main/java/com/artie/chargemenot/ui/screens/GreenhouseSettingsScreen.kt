package com.artie.chargemenot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.ui.components.MeadowHubScaffold
import com.artie.chargemenot.ui.components.NagModeCard
import com.artie.chargemenot.ui.theme.MeadowCream
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowWhite
import com.artie.chargemenot.ui.viewmodels.SettingsUiState

@Composable
fun GreenhouseSettingsScreen(
    userDisplayName: String,
    selectedCurrencyCode: String,
    settingsUiState: SettingsUiState,
    onOpenDrawer: () -> Unit,
    onShowProfileEdit: () -> Unit,
    onNagModeToggleRequested: (Boolean) -> Unit,
    onNotificationPermissionResult: (Boolean) -> Unit,
    onNotificationPermissionRequestHandled: () -> Unit,
    onRefreshNotificationPermissionState: () -> Unit,
    modifier: Modifier = Modifier
) {
    MeadowHubScaffold(
        title = stringResource(R.string.meadow_route_greenhouse_settings),
        onOpenDrawer = onOpenDrawer,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MeadowCream),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "greenhouse_profile_card") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onShowProfileEdit),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MeadowWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.greenhouse_profile_label),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = userDisplayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MeadowGreenDark,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        IconButton(onClick = onShowProfileEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.profile_edit_tap_hint)
                            )
                        }
                    }
                }
            }

            item(key = "greenhouse_currency_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MeadowWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.greenhouse_currency_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = selectedCurrencyCode,
                            style = MaterialTheme.typography.titleMedium,
                            color = MeadowGreenDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            item(key = "greenhouse_nag_mode") {
                NagModeCard(
                    uiState = settingsUiState,
                    onNagModeToggleRequested = onNagModeToggleRequested,
                    onNotificationPermissionResult = onNotificationPermissionResult,
                    onNotificationPermissionRequestHandled = onNotificationPermissionRequestHandled,
                    onRefreshPermissionState = onRefreshNotificationPermissionState
                )
            }
        }
    }
}
