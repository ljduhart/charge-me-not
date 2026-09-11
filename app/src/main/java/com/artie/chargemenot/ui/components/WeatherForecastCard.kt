package com.artie.chargemenot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.domain.model.ForecastResult
import com.artie.chargemenot.domain.model.ForecastTimelinePoint
import com.artie.chargemenot.domain.model.WeatherStatus
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.ui.theme.ChargeMeNotTheme
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowSage
import com.artie.chargemenot.ui.theme.MeadowSky
import com.artie.chargemenot.ui.theme.MeadowSunflower
import com.artie.chargemenot.ui.theme.MeadowWhite
import com.artie.chargemenot.util.CurrencyFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
fun WeatherForecastCard(
    forecastResult: ForecastResult,
    currency: SupportedCurrency,
    modifier: Modifier = Modifier
) {
    val predictedTotal = CurrencyFormatter.format(forecastResult.predictedAmount, currency)
    val varianceMagnitude = abs(forecastResult.percentageVariance).let { value ->
        String.format(Locale.US, "%.1f", value)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MeadowWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            forecastAccentColor(forecastResult.weatherStatus).copy(alpha = 0.18f),
                            MeadowSage.copy(alpha = 0.10f),
                            MeadowWhite
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = stringResource(R.string.weather_forecast_title),
                style = MaterialTheme.typography.titleMedium,
                color = MeadowGreenDark,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = buildForecastMessage(
                    forecastResult = forecastResult,
                    varianceMagnitude = varianceMagnitude,
                    targetMonthLabel = forecastResult.targetMonthLabel
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(
                    R.string.weather_forecast_predicted_total,
                    forecastResult.targetMonthLabel,
                    predictedTotal
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MeadowGreenDark,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            ForecastTimelineCanvas(
                timelinePoints = forecastResult.timelinePoints,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun buildForecastMessage(
    forecastResult: ForecastResult,
    varianceMagnitude: String,
    targetMonthLabel: String
): String {
    return when (forecastResult.weatherStatus) {
        WeatherStatus.DROUGHT -> stringResource(
            R.string.weather_forecast_drought,
            varianceMagnitude,
            targetMonthLabel
        )
        WeatherStatus.CLOUDY -> stringResource(
            R.string.weather_forecast_cloudy,
            varianceMagnitude,
            targetMonthLabel
        )
        WeatherStatus.SUNNY -> {
            if (forecastResult.percentageVariance <= 0.0) {
                stringResource(R.string.weather_forecast_sunny, targetMonthLabel)
            } else {
                stringResource(
                    R.string.weather_forecast_sunny_trend,
                    targetMonthLabel,
                    varianceMagnitude
                )
            }
        }
    }
}

private fun forecastAccentColor(status: WeatherStatus) = when (status) {
    WeatherStatus.SUNNY -> MeadowSunflower
    WeatherStatus.CLOUDY -> MeadowSage
    WeatherStatus.DROUGHT -> MeadowSky
}

@Preview(showBackground = true)
@Composable
private fun WeatherForecastCardPreview() {
    ChargeMeNotTheme {
        WeatherForecastCard(
            forecastResult = ForecastResult(
                predictedAmount = 31_240L,
                percentageVariance = 18.6,
                weatherStatus = WeatherStatus.DROUGHT,
                targetMonthLabel = "Oct",
                timelinePoints = listOf(
                    ForecastTimelinePoint("Jul", 21_000L, false, WeatherStatus.SUNNY),
                    ForecastTimelinePoint("Aug", 24_500L, false, WeatherStatus.CLOUDY),
                    ForecastTimelinePoint("Sep", 26_350L, false, WeatherStatus.CLOUDY),
                    ForecastTimelinePoint("Oct", 31_240L, true, WeatherStatus.DROUGHT)
                )
            ),
            currency = SupportedCurrency.USD
        )
    }
}
