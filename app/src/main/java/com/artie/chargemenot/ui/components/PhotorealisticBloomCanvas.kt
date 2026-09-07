package com.artie.chargemenot.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.ui.theme.MeadowEarth
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowWhite
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun PhotorealisticBloomCanvas(
    categoryTotals: Map<BillCategory, Double>,
    monthlyBudget: Double,
    highlightedCategory: BillCategory?,
    pendingSubscriptionCount: Int,
    onPetalTapped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pressedCategory by remember { mutableStateOf<String?>(null) }

    val categoryScales = BloomCategoryDefinitions.categories.associate { definition ->
        val targetScale = if (pressedCategory == definition.displayName) 0.9f else 1f
        definition.displayName to animateFloatAsState(
            targetValue = targetScale,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "petal_scale_${definition.displayName}"
        )
    }

    LaunchedEffect(pressedCategory) {
        val activeCategory = pressedCategory
        if (activeCategory != null) {
            delay(180)
            pressedCategory = null
        }
    }

    val labelPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color(0xFF1A1A1A).toArgb()
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
    }

    val tooltipPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color(0xFF1A1A1A).toArgb()
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val centerX = size.width / 2f
                    val centerY = size.height * 0.44f
                    val maxRadius = min(size.width, size.height) * 0.36f
                    val innerRadius = maxRadius * 0.18f
                    val outerRadius = maxRadius * 1.05f

                    val sliceIndex = BloomTouchMath.sliceIndexAtPoint(
                        tapX = offset.x,
                        tapY = offset.y,
                        centerX = centerX,
                        centerY = centerY,
                        innerRadius = innerRadius,
                        outerRadius = outerRadius
                    ) ?: return@detectTapGestures

                    val categoryName = BloomCategoryDefinitions.displayNameAtSliceIndex(sliceIndex)
                    pressedCategory = categoryName
                    onPetalTapped(categoryName)
                }
            }
    ) {
        val centerX = size.width / 2f
        val centerY = size.height * 0.44f
        val center = Offset(centerX, centerY)
        val maxRadius = min(size.width, size.height) * 0.36f
        val safeBudget = monthlyBudget.coerceAtLeast(1.0)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    MeadowEarth.copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = center,
                radius = maxRadius * 1.2f
            ),
            radius = maxRadius * 1.2f,
            center = center
        )

        BloomCategoryDefinitions.categories.forEachIndexed { index, definition ->
            val amount = categoryTotals[definition.billCategory] ?: 0.0
            val proportion = if (amount <= 0.0) {
                0.35f
            } else {
                (amount / safeBudget).toFloat().coerceIn(0.35f, 1f)
            }
            val petalLength = maxRadius * (0.68f + proportion * 0.32f)
            val petalWidth = maxRadius * 0.82f * proportion.coerceAtLeast(0.42f)
            val sliceAngle = -90f + index * BloomCategoryDefinitions.SLICE_DEGREES
            val isHighlighted = highlightedCategory == null || highlightedCategory == definition.billCategory
            val alphaMultiplier = if (isHighlighted) 1f else 0.5f
            val scale = categoryScales[definition.displayName]?.value ?: 1f

            rotate(sliceAngle, center) {
                scale(scale, scale, pivot = center) {
                    drawPhotorealisticPetal(
                        center = center,
                        length = petalLength,
                        width = petalWidth,
                        definition = definition,
                        alphaMultiplier = alphaMultiplier
                    )
                }
            }

            val labelAngleRadians = Math.toRadians((sliceAngle - 90f).toDouble())
            val labelRadius = maxRadius * 1.28f
            val labelX = centerX + cos(labelAngleRadians).toFloat() * labelRadius
            val labelY = centerY + sin(labelAngleRadians).toFloat() * labelRadius
            val petalTipRadius = maxRadius * 0.92f
            val petalTipX = centerX + cos(labelAngleRadians).toFloat() * petalTipRadius
            val petalTipY = centerY + sin(labelAngleRadians).toFloat() * petalTipRadius

            drawLine(
                color = Color(0xFF1A1A1A).copy(alpha = 0.35f * alphaMultiplier),
                start = Offset(petalTipX, petalTipY),
                end = Offset(labelX, labelY),
                strokeWidth = 1.5f
            )

            labelPaint.textAlign = when {
                labelX < centerX - maxRadius * 0.08f -> Paint.Align.RIGHT
                labelX > centerX + maxRadius * 0.08f -> Paint.Align.LEFT
                else -> Paint.Align.CENTER
            }

            drawContext.canvas.nativeCanvas.drawText(
                definition.bloomLabel,
                labelX,
                labelY,
                labelPaint.apply {
                    alpha = (255 * alphaMultiplier).toInt().coerceIn(0, 255)
                }
            )
        }

        drawFlowerCenter(center = center, radius = maxRadius * 0.14f)
        drawStemAndLeaves(center = center, maxRadius = maxRadius)

        if (pendingSubscriptionCount > 0) {
            val stemTop = Offset(centerX, centerY + maxRadius * 0.12f)
            drawPendingCullTooltip(
                anchor = Offset(centerX + maxRadius * 0.35f, stemTop.y + 24f),
                pendingCount = pendingSubscriptionCount,
                textPaint = tooltipPaint
            )
        }
    }
}

private fun DrawScope.drawPhotorealisticPetal(
    center: Offset,
    length: Float,
    width: Float,
    definition: BloomCategoryDefinition,
    alphaMultiplier: Float
) {
    val shadowPath = createPetalPath(center = center, length = length, width = width)
    drawPath(
        path = shadowPath,
        brush = Brush.linearGradient(
            colors = listOf(
                definition.shadowColor.copy(alpha = 0.55f * alphaMultiplier),
                definition.shadowColor.copy(alpha = 0.2f * alphaMultiplier)
            ),
            start = Offset(center.x - width * 0.2f, center.y + length * 0.15f),
            end = Offset(center.x, center.y - length)
        )
    )

    val petalPath = createPetalPath(center = center, length = length, width = width)
    drawPath(
        path = petalPath,
        brush = Brush.radialGradient(
            colors = listOf(
                definition.highlightColor.copy(alpha = alphaMultiplier),
                definition.midColor.copy(alpha = alphaMultiplier),
                definition.baseColor.copy(alpha = alphaMultiplier),
                definition.shadowColor.copy(alpha = 0.85f * alphaMultiplier)
            ),
            center = Offset(center.x, center.y - length * 0.72f),
            radius = length * 0.95f
        )
    )

    drawPath(
        path = petalPath,
        brush = Brush.linearGradient(
            colors = listOf(
                MeadowWhite.copy(alpha = 0.35f * alphaMultiplier),
                Color.Transparent,
                definition.shadowColor.copy(alpha = 0.25f * alphaMultiplier)
            ),
            start = Offset(center.x - width * 0.35f, center.y - length * 0.2f),
            end = Offset(center.x + width * 0.25f, center.y - length * 0.85f)
        )
    )

    val veinPath = Path().apply {
        moveTo(center.x, center.y - length * 0.08f)
        cubicTo(
            center.x - width * 0.05f,
            center.y - length * 0.45f,
            center.x + width * 0.04f,
            center.y - length * 0.72f,
            center.x,
            center.y - length * 0.95f
        )
    }
    drawPath(
        path = veinPath,
        color = definition.shadowColor.copy(alpha = 0.28f * alphaMultiplier),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
    )
}

private fun createPetalPath(
    center: Offset,
    length: Float,
    width: Float
): Path {
    return Path().apply {
        moveTo(center.x, center.y)
        cubicTo(
            center.x - width * 0.55f,
            center.y - length * 0.18f,
            center.x - width * 0.72f,
            center.y - length * 0.62f,
            center.x,
            center.y - length
        )
        cubicTo(
            center.x + width * 0.72f,
            center.y - length * 0.62f,
            center.x + width * 0.55f,
            center.y - length * 0.18f,
            center.x,
            center.y
        )
        close()
    }
}

private fun DrawScope.drawFlowerCenter(center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFF4C2),
                Color(0xFFE8C547),
                Color(0xFFC9971A)
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
    drawCircle(
        color = MeadowEarth.copy(alpha = 0.35f),
        radius = radius * 0.55f,
        center = center
    )
}

private fun DrawScope.drawStemAndLeaves(center: Offset, maxRadius: Float) {
    val stemTop = Offset(center.x, center.y + maxRadius * 0.12f)
    val stemBottom = Offset(center.x, center.y + maxRadius * 0.95f)

    drawLine(
        color = Color(0xFF3E6B45),
        start = stemTop,
        end = stemBottom,
        strokeWidth = 10f
    )

    val leftLeaf = Path().apply {
        moveTo(stemTop.x, stemTop.y + maxRadius * 0.2f)
        cubicTo(
            stemTop.x - maxRadius * 0.35f,
            stemTop.y + maxRadius * 0.28f,
            stemTop.x - maxRadius * 0.28f,
            stemTop.y + maxRadius * 0.48f,
            stemTop.x - maxRadius * 0.05f,
            stemTop.y + maxRadius * 0.42f
        )
        close()
    }
    drawPath(
        path = leftLeaf,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF7CB87E), Color(0xFF4A7C59)),
            start = leftLeaf.getBounds().topLeft,
            end = leftLeaf.getBounds().bottomRight
        )
    )

    val rightLeaf = Path().apply {
        moveTo(stemTop.x, stemTop.y + maxRadius * 0.32f)
        cubicTo(
            stemTop.x + maxRadius * 0.32f,
            stemTop.y + maxRadius * 0.36f,
            stemTop.x + maxRadius * 0.26f,
            stemTop.y + maxRadius * 0.56f,
            stemTop.x + maxRadius * 0.04f,
            stemTop.y + maxRadius * 0.5f
        )
        close()
    }
    drawPath(
        path = rightLeaf,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF9CAF88), Color(0xFF5A9E4B)),
            start = rightLeaf.getBounds().topLeft,
            end = rightLeaf.getBounds().bottomRight
        )
    )
}

private fun DrawScope.drawPendingCullTooltip(
    anchor: Offset,
    pendingCount: Int,
    textPaint: Paint
) {
    val message = "$pendingCount SUBSCRIPTIONS PENDING CULL!"
    val bubbleWidth = 320f
    val bubbleHeight = 56f
    val bubbleTopLeft = Offset(anchor.x - 12f, anchor.y)

    drawRoundRect(
        color = MeadowWhite,
        topLeft = bubbleTopLeft,
        size = androidx.compose.ui.geometry.Size(bubbleWidth, bubbleHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
    )
    drawRoundRect(
        color = MeadowGreenDark.copy(alpha = 0.2f),
        topLeft = bubbleTopLeft,
        size = androidx.compose.ui.geometry.Size(bubbleWidth, bubbleHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
    )

    drawContext.canvas.nativeCanvas.drawText(
        message,
        bubbleTopLeft.x + 16f,
        bubbleTopLeft.y + 36f,
        textPaint
    )

    val badgeCenter = Offset(bubbleTopLeft.x + bubbleWidth - 22f, bubbleTopLeft.y + bubbleHeight / 2f)
    drawCircle(color = Color(0xFF4A7FC1), radius = 16f, center = badgeCenter)
    drawContext.canvas.nativeCanvas.drawText(
        pendingCount.toString(),
        badgeCenter.x,
        badgeCenter.y + 8f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = MeadowWhite.toArgb()
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
    )
}
