package com.artie.chargemenot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.domain.model.ForecastTimelinePoint
import com.artie.chargemenot.domain.model.WeatherStatus
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowSky
import com.artie.chargemenot.ui.theme.MeadowSunflower
import com.artie.chargemenot.ui.theme.MeadowWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Composable
fun ForecastTimelineCanvas(
    timelinePoints: List<ForecastTimelinePoint>,
    modifier: Modifier = Modifier
) {
    if (timelinePoints.isEmpty()) {
        return
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val horizontalPadding = 28.dp.toPx()
        val topPadding = 24.dp.toPx()
        val bottomPadding = 36.dp.toPx()
        val chartWidth = size.width - (horizontalPadding * 2f)
        val chartHeight = size.height - topPadding - bottomPadding
        val chartBottom = size.height - bottomPadding

        val maxAmount = timelinePoints.maxOf { point -> point.amount }.coerceAtLeast(1.0)
        val minAmount = timelinePoints.minOf { point -> point.amount }
        val amountRange = max(maxAmount - minAmount, 1.0)

        val pointCount = timelinePoints.size
        val stepX = if (pointCount <= 1) 0f else chartWidth / (pointCount - 1).toFloat()

        val coordinates = timelinePoints.mapIndexed { index, point ->
            val x = horizontalPadding + (stepX * index)
            val normalized = ((point.amount - minAmount) / amountRange).toFloat()
            val y = chartBottom - (normalized * chartHeight)
            Offset(x, y)
        }

        drawTimelineBaseline(
            startX = horizontalPadding,
            endX = size.width - horizontalPadding,
            y = chartBottom
        )

        if (coordinates.size >= 2) {
            val curvePath = Path().apply {
                moveTo(coordinates.first().x, coordinates.first().y)
                for (index in 1 until coordinates.size) {
                    val previous = coordinates[index - 1]
                    val current = coordinates[index]
                    val controlX = (previous.x + current.x) / 2f
                    cubicTo(
                        controlX,
                        previous.y,
                        controlX,
                        current.y,
                        current.x,
                        current.y
                    )
                }
            }

            drawPath(
                path = curvePath,
                color = MeadowGreen.copy(alpha = 0.85f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        timelinePoints.forEachIndexed { index, point ->
            val center = coordinates[index]
            drawWeatherIcon(
                center = center,
                status = point.weatherStatus,
                isPrediction = point.isPrediction
            )
        }
    }
}

private fun DrawScope.drawTimelineBaseline(
    startX: Float,
    endX: Float,
    y: Float
) {
    drawLine(
        color = MeadowGreenDark.copy(alpha = 0.25f),
        start = Offset(startX, y),
        end = Offset(endX, y),
        strokeWidth = 1.5.dp.toPx()
    )
}

private fun DrawScope.drawWeatherIcon(
    center: Offset,
    status: WeatherStatus,
    isPrediction: Boolean
) {
    when (status) {
        WeatherStatus.SUNNY -> drawSunnyIcon(center, isPrediction)
        WeatherStatus.CLOUDY -> drawCloudyIcon(center, isPrediction)
        WeatherStatus.DROUGHT -> drawDroughtIcon(center, isPrediction)
    }
}

private fun DrawScope.drawSunnyIcon(center: Offset, isPrediction: Boolean) {
    val radius = if (isPrediction) 11.dp.toPx() else 9.dp.toPx()
    drawCircle(
        color = MeadowSunflower,
        radius = radius,
        center = center
    )

    val rayLength = radius + 5.dp.toPx()
    val rayCount = 8
    repeat(rayCount) { index ->
        val angle = (index.toDouble() / rayCount.toDouble()) * (PI * 2.0)
        val start = Offset(
            x = center.x + (cos(angle).toFloat() * (radius + 2.dp.toPx())),
            y = center.y + (sin(angle).toFloat() * (radius + 2.dp.toPx()))
        )
        val end = Offset(
            x = center.x + (cos(angle).toFloat() * rayLength),
            y = center.y + (sin(angle).toFloat() * rayLength)
        )
        drawLine(
            color = MeadowSunflower,
            start = start,
            end = end,
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawCloudyIcon(center: Offset, isPrediction: Boolean) {
    val scale = if (isPrediction) 1.15f else 1f
    val cloudColor = Color(0xFF9AA3AD)

    drawCircle(
        color = cloudColor,
        radius = 10.dp.toPx() * scale,
        center = Offset(center.x - 8.dp.toPx() * scale, center.y)
    )
    drawCircle(
        color = cloudColor,
        radius = 12.dp.toPx() * scale,
        center = Offset(center.x + 2.dp.toPx() * scale, center.y - 2.dp.toPx())
    )
    drawCircle(
        color = cloudColor,
        radius = 9.dp.toPx() * scale,
        center = Offset(center.x + 10.dp.toPx() * scale, center.y + 1.dp.toPx())
    )
}

private fun DrawScope.drawDroughtIcon(center: Offset, isPrediction: Boolean) {
    val dropColor = MeadowSky
    val dropCount = if (isPrediction) 4 else 3
    val spacing = 8.dp.toPx()

    repeat(dropCount) { index ->
        val offsetX = center.x + ((index - (dropCount - 1) / 2f) * spacing)
        val offsetY = center.y + ((index % 2) * 4.dp.toPx())
        drawRainDrop(
            top = Offset(offsetX, offsetY - 8.dp.toPx()),
            bottom = Offset(offsetX, offsetY + 8.dp.toPx()),
            color = dropColor
        )
    }
}

private fun DrawScope.drawRainDrop(
    top: Offset,
    bottom: Offset,
    color: Color
) {
    val path = Path().apply {
        moveTo(top.x, top.y)
        quadraticTo(
            top.x + 5.dp.toPx(),
            (top.y + bottom.y) / 2f,
            bottom.x,
            bottom.y
        )
        quadraticTo(
            top.x - 5.dp.toPx(),
            (top.y + bottom.y) / 2f,
            top.x,
            top.y
        )
        close()
    }

    drawPath(path = path, color = color)
    drawCircle(
        color = MeadowWhite.copy(alpha = 0.45f),
        radius = 2.dp.toPx(),
        center = Offset(top.x - 1.dp.toPx(), top.y + 3.dp.toPx())
    )
}
