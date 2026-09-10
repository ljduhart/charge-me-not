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
import androidx.compose.ui.unit.dp
import kotlin.math.sin

private val VineBaseGlow = Color(0x664CAF50)
private val VineCoreGlow = Color(0xAA81C784)
private val VineTendril = Color(0xAA66BB6A)
private val VineHighlight = Color(0xE6B9F6CA)

/**
 * Draws a curvy, organic glowing vine stem behind the garden-path timeline.
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
    val baseStrokeWidth = 6.dp.toPx()
    val coreStrokeWidth = 2.dp.toPx()

    val path = buildOrganicVinePath(
        centerX = centerX,
        startY = 0f,
        totalHeight = vineLength,
        amplitude = size.width * 0.08f,
        segmentHeight = estimatedItemHeightPx * 0.9f
    )

    drawContext.canvas.save()
    drawContext.canvas.translate(0f, -scrollOffset)

    drawPath(
        path = path,
        color = VineBaseGlow,
        style = Stroke(width = baseStrokeWidth, cap = StrokeCap.Round)
    )
    drawPath(
        path = path,
        color = VineCoreGlow,
        style = Stroke(width = coreStrokeWidth, cap = StrokeCap.Round)
    )

    var tendrilY = estimatedItemHeightPx * 0.72f
    var tendrilIndex = 0
    while (tendrilY < vineLength) {
        val wave = sin(tendrilIndex * 0.9f) * size.width * 0.06f
        val stemPoint = Offset(centerX + wave, tendrilY)
        drawBezierOffshoot(
            stemPoint = stemPoint,
            branchLeft = tendrilIndex % 2 == 0,
            branchLength = size.width * 0.22f
        )
        tendrilY += estimatedItemHeightPx
        tendrilIndex++
    }

    drawContext.canvas.restore()
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBezierOffshoot(
    stemPoint: Offset,
    branchLeft: Boolean,
    branchLength: Float
) {
    val direction = if (branchLeft) -1f else 1f
    val tipX = stemPoint.x + direction * branchLength
    val tipY = stemPoint.y

    val offshootPath = Path().apply {
        moveTo(stemPoint.x, stemPoint.y)
        cubicTo(
            x1 = stemPoint.x + direction * branchLength * 0.25f,
            y1 = stemPoint.y - 10f,
            x2 = stemPoint.x + direction * branchLength * 0.72f,
            y2 = stemPoint.y + 8f,
            x3 = tipX,
            y3 = tipY
        )
    }

    drawPath(
        path = offshootPath,
        color = VineTendril,
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )
    drawPath(
        path = offshootPath,
        color = VineCoreGlow.copy(alpha = 0.55f),
        style = Stroke(width = 1.2f, cap = StrokeCap.Round)
    )
    drawCircle(
        color = VineHighlight,
        radius = 3.5f,
        center = Offset(tipX, tipY)
    )
}

private fun buildOrganicVinePath(
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
        val waveA = sin(segmentIndex * 0.9f) * amplitude
        val waveB = sin((segmentIndex + 1) * 0.9f) * amplitude
        path.cubicTo(
            x1 = centerX + waveA,
            y1 = currentY + segmentHeight * 0.28f,
            x2 = centerX - waveB,
            y2 = currentY + segmentHeight * 0.72f,
            x3 = centerX + waveB * 0.45f,
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
