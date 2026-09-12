package com.artie.chargemenot.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

internal val OrganicVineBase = Color(0x664CAF50)
internal val OrganicVineCore = Color(0xAA81C784)
private val VineHighlight = Color(0xE6B9F6CA)

/**
 * Draws a curvy organic stem down the garden-path center using cubicTo,
 * then small bezier offshoots that reach from the stem toward each leaf card.
 *
 * [scrollOffsetPx] must be read during composition so scrolling invalidates draw.
 */
fun Modifier.gardenPathVineBackground(
    scrollOffsetPx: Float,
    itemCount: Int,
    estimatedItemHeightPx: Float
): Modifier = drawBehind {
    if (itemCount <= 0 || estimatedItemHeightPx <= 0f || !estimatedItemHeightPx.isFinite()) {
        return@drawBehind
    }
    if (!scrollOffsetPx.isFinite()) {
        return@drawBehind
    }

    val vineLength = itemCount * estimatedItemHeightPx + size.height
    val centerX = size.width / 2f
    val amplitude = size.width * 0.16f
    val segmentHeight = estimatedItemHeightPx * 0.95f
    val baseStrokeWidth = 6.dp.toPx()
    val coreStrokeWidth = 2.dp.toPx()

    val path = buildOrganicVinePath(
        centerX = centerX,
        startY = 0f,
        totalHeight = vineLength,
        amplitude = amplitude,
        segmentHeight = segmentHeight
    )

    drawContext.canvas.save()
    try {
        drawContext.canvas.translate(0f, -scrollOffsetPx)

        drawPath(
            path = path,
            color = OrganicVineBase,
            style = Stroke(width = baseStrokeWidth, cap = StrokeCap.Round)
        )
        drawPath(
            path = path,
            color = OrganicVineCore,
            style = Stroke(width = coreStrokeWidth, cap = StrokeCap.Round)
        )

        val billCount = (itemCount - 1).coerceAtLeast(0)
        val visibleTop = scrollOffsetPx - estimatedItemHeightPx
        val visibleBottom = scrollOffsetPx + size.height + estimatedItemHeightPx
        var tendrilIndex = 0
        while (tendrilIndex < billCount) {
            val tendrilY = estimatedItemHeightPx * (tendrilIndex + 1.55f)
            if (tendrilY > visibleBottom) {
                break
            }
            if (tendrilY >= visibleTop) {
                val stemPoint = Offset(
                    x = stemXAt(
                        centerX = centerX,
                        y = tendrilY,
                        amplitude = amplitude,
                        segmentHeight = segmentHeight
                    ),
                    y = tendrilY
                )
                drawBezierOffshoot(
                    stemPoint = stemPoint,
                    branchLeft = tendrilIndex % 2 == 0,
                    branchLength = size.width * 0.20f
                )
            }
            tendrilIndex++
        }
    } finally {
        drawContext.canvas.restore()
    }
}

private fun DrawScope.drawBezierOffshoot(
    stemPoint: Offset,
    branchLeft: Boolean,
    branchLength: Float
) {
    val direction = if (branchLeft) -1f else 1f
    val tipX = offshootTipX(
        stemX = stemPoint.x,
        branchLeft = branchLeft,
        branchLength = branchLength
    )
    val tipY = stemPoint.y - 2f

    val offshootPath = Path().apply {
        moveTo(stemPoint.x, stemPoint.y)
        cubicTo(
            x1 = stemPoint.x + direction * branchLength * 0.28f,
            y1 = stemPoint.y - 22f,
            x2 = stemPoint.x + direction * branchLength * 0.72f,
            y2 = stemPoint.y + 14f,
            x3 = tipX,
            y3 = tipY
        )
    }

    drawPath(
        path = offshootPath,
        color = OrganicVineBase,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
    drawPath(
        path = offshootPath,
        color = OrganicVineCore,
        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
    )
    drawCircle(
        color = VineHighlight,
        radius = 3.6f,
        center = Offset(tipX, tipY)
    )
}

/**
 * Smooth S-curve built from cubicTo segments whose endpoints lie on [stemXAt],
 * so offshoots attach to the same center path that is drawn.
 */
internal fun buildOrganicVinePath(
    centerX: Float,
    startY: Float,
    totalHeight: Float,
    amplitude: Float,
    segmentHeight: Float
): Path {
    val path = Path()
    path.moveTo(
        stemXAt(centerX, startY, amplitude, segmentHeight),
        startY
    )

    var currentY = startY
    if (segmentHeight <= 0f || !segmentHeight.isFinite() || !totalHeight.isFinite()) {
        return path
    }
    var segmentsDrawn = 0
    while (currentY < totalHeight && segmentsDrawn < 10_000) {
        val nextY = (currentY + segmentHeight).coerceAtMost(totalHeight)
        if (nextY <= currentY) {
            break
        }
        val span = nextY - currentY
        val control1Y = currentY + span * 0.33f
        val control2Y = currentY + span * 0.67f
        path.cubicTo(
            x1 = stemXAt(centerX, control1Y, amplitude, segmentHeight),
            y1 = control1Y,
            x2 = stemXAt(centerX, control2Y, amplitude, segmentHeight),
            y2 = control2Y,
            x3 = stemXAt(centerX, nextY, amplitude, segmentHeight),
            y3 = nextY
        )
        currentY = nextY
        segmentsDrawn++
    }
    return path
}

internal fun stemXAt(
    centerX: Float,
    y: Float,
    amplitude: Float,
    segmentHeight: Float
): Float {
    if (segmentHeight <= 0f || !segmentHeight.isFinite() || !y.isFinite() || !amplitude.isFinite()) {
        return centerX
    }
    val phase = (y / segmentHeight) * (PI.toFloat() / 2f)
    val offsetX = sin(phase) * amplitude * 0.55f
    return if (offsetX.isFinite()) centerX + offsetX else centerX
}

internal fun offshootTipX(
    stemX: Float,
    branchLeft: Boolean,
    branchLength: Float
): Float {
    return if (branchLeft) stemX - branchLength else stemX + branchLength
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

    rotate(degrees = -4f, pivot = Offset(left, top)) {
        drawLine(
            color = OrganicVineBase,
            start = Offset(left, bottom * 0.55f),
            end = Offset(left + size.width * 0.18f, top),
            strokeWidth = 3.2f
        )
        drawLine(
            color = OrganicVineCore,
            start = Offset(left, bottom * 0.55f),
            end = Offset(left + size.width * 0.18f, top),
            strokeWidth = 1.6f
        )
    }

    rotate(degrees = 4f, pivot = Offset(right, top)) {
        drawLine(
            color = OrganicVineBase,
            start = Offset(right, bottom * 0.55f),
            end = Offset(right - size.width * 0.18f, top),
            strokeWidth = 3.2f
        )
        drawLine(
            color = OrganicVineCore,
            start = Offset(right, bottom * 0.55f),
            end = Offset(right - size.width * 0.18f, top),
            strokeWidth = 1.6f
        )
    }

    val budRadius = 3.4f
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

fun Modifier.gardenLeafCyanGlow(): Modifier = drawBehind {
    drawCircle(
        color = Color(0x334CAF50),
        radius = size.maxDimension * 0.42f,
        center = Offset(size.width / 2f, size.height / 2f)
    )
}
