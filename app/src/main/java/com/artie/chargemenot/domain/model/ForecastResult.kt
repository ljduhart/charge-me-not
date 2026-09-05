package com.artie.chargemenot.domain.model

data class ForecastResult(
    val predictedAmount: Double,
    val percentageVariance: Double,
    val weatherStatus: WeatherStatus,
    val timelinePoints: List<ForecastTimelinePoint>,
    val targetMonthLabel: String
)

data class ForecastTimelinePoint(
    val monthLabel: String,
    val amount: Double,
    val isPrediction: Boolean,
    val weatherStatus: WeatherStatus
)
