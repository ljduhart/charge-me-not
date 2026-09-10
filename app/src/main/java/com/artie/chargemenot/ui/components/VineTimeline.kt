package com.artie.chargemenot.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin

private val VineStemColor = Color(0xAA81C784)
private val VineGlowHalo = Color(0x4481C784)
private val VineHighlight = Color(0xCCB9F6CA)

/**
 * Draws a continuous, meandering central vine behind the garden-path timeline list.
 */
fun Modifier.gardenPathVineBackground(
    listState: LazyListState,
    itemCount: Int,
    estimatedItemHeightPx: Float
): Modifier = drawBehind {
    if (itemCount == 0) return@drawBehind

    val scrollOffset =
        listState.firstVisibleItemIndex * estimatedItemHeightPx +
            listState.firstVisibleItemScrollOffset.toFloat()
    val vineLength = itemCount * estimatedItemHeightPx + size.height
    val centerX = size.width / 2f

    val path = buildMeanderingVinePath(
        centerX = centerX,
        startY = 0f,
        totalHeight = vineLength,
        amplitude = size.width * 0.07f,
        segmentHeight = estimatedItemHeightPx * 0.82f
    )

    drawContext.canvas.save()
    drawContext.canvas.translate(0f, -scrollOffset)

    drawPath(
        path = path,
        color = VineGlowHalo,
        style = Stroke(width = 14f, cap = StrokeCap.Round)
    )
    drawPath(
        path = path,
        color = VineStemColor,
        style = Stroke(width = 6f, cap = StrokeCap.Round)
    )
    drawPath(
        path = path,
        color = VineHighlight.copy(alpha = 0.55f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )

    drawContext.canvas.restore()
}

private fun buildMeanderingVinePath(
    centerX: Float,
    startY: Float,
    totalHeight: Float,
    amplitude: Float,
    segmentHeight: Float
): Path {
    val path = Path()
    path.moveTo(centerX, startY)

    var currentY = startY
    var segmentIndex = 0
    while (currentY < totalHeight) {
        val nextY = (currentY + segmentHeight).coerceAtMost(totalHeight)
        val wave = sin(segmentIndex * 0.85f) * amplitude
        val nextWave = sin((segmentIndex + 1) * 0.85f) * amplitude * 0.55f
        path.cubicTo(
            x1 = centerX + wave,
            y1 = currentY + segmentHeight * 0.33f,
            x2 = centerX - wave * 0.7f,
            y2 = currentY + segmentHeight * 0.67f,
            x3 = centerX + nextWave,
            y3 = nextY
        )
        currentY = nextY
        segmentIndex++
    }
    return path
}

/**
 * Decorative vine strokes wrapping frosted-glass pill buttons.
 */
fun Modifier.decorativeVineBorder(): Modifier = drawBehind {
    val inset = 6f
    val left = inset
    val top = inset
    val right = size.width - inset
    val bottom = size.height - inset

    val vineColor = Color(0xAA81C784)
    val accentColor = Color(0x66B9F6CA)

    rotate(degrees = -4f, pivot = Offset(left, top)) {
        drawLine(
            color = vineColor,
            start = Offset(left, bottom * 0.55f),
            end = Offset(left + size.width * 0.18f, top),
            strokeWidth = 1.6f
        )
        drawLine(
            color = accentColor,
            start = Offset(left + 8f, bottom * 0.42f),
            end = Offset(left + size.width * 0.12f, top + 10f),
            strokeWidth = 1f
        )
    }

    rotate(degrees = 4f, pivot = Offset(right, top)) {
        drawLine(
            color = vineColor,
            start = Offset(right, bottom * 0.55f),
            end = Offset(right - size.width * 0.18f, top),
            strokeWidth = 1.6f
        )
        drawLine(
            color = accentColor,
            start = Offset(right - 8f, bottom * 0.42f),
            end = Offset(right - size.width * 0.12f, top + 10f),
            strokeWidth = 1f
        )
    }

    val budRadius = 3f
    drawCircle(
        color = VineHighlight,
        radius = budRadius,
        center = Offset(left + size.width * 0.14f, top + 8f)
    )
    drawCircle(
        color = VineHighlight,
        radius = budRadius,
        center = Offset(right - size.width * 0.14f, top + 8f)
    )
}
