package com.artie.chargemenot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.domain.model.ForecastResult
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.ui.components.MeadowHubScaffold
import com.artie.chargemenot.ui.components.WeatherForecastCard
import com.artie.chargemenot.ui.theme.MeadowCream

@Composable
fun HarvestReportScreen(
    forecastResult: ForecastResult?,
    currency: SupportedCurrency,
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    MeadowHubScaffold(
        title = stringResource(R.string.meadow_route_harvest_report),
        onOpenDrawer = onOpenDrawer,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    ) { innerPadding ->
        if (forecastResult == null) {
            Text(
                text = stringResource(R.string.harvest_report_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MeadowCream),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
            ) {
                item(key = "harvest_forecast_card") {
                    WeatherForecastCard(
                        forecastResult = forecastResult,
                        currency = currency
                    )
                }
            }
        }
    }
}
