package com.artie.chargemenot.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.ui.theme.MeadowEarth
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowWhite
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class BloomPetalSpec(
    val category: BillCategory,
    val label: String,
    val baseColor: Color,
    val accentColor: Color
)

private val bloomPetalSpecs = listOf(
    BloomPetalSpec(BillCategory.RENT, "RENT/MORTGAGE", Color(0xFFE07A5F), Color(0xFFF4A261)),
    BloomPetalSpec(BillCategory.FOOD, "FOOD", Color(0xFFF2CC8F), Color(0xFFE9C46A)),
    BloomPetalSpec(BillCategory.UTILITIES, "UTILITIES", Color(0xFF81B29A), Color(0xFF3D8B7A)),
    BloomPetalSpec(BillCategory.SUBSCRIPTIONS, "SUBSCRIPTIONS", Color(0xFFB8A9C9), Color(0xFF9B8EC4)),
    BloomPetalSpec(BillCategory.TRANSPORTATION, "LOANS", Color(0xFF6D9DC5), Color(0xFF4A7FC1)),
    BloomPetalSpec(BillCategory.HEALTHCARE, "HEALTH", Color(0xFFE8B4B8), Color(0xFFD4737A)),
    BloomPetalSpec(BillCategory.ENTERTAINMENT, "FUN", Color(0xFFA8C8D8), Color(0xFF7EB6D7)),
    BloomPetalSpec(BillCategory.OTHER, "OTHERS", Color(0xFF9CAF88), Color(0xFF7CB87E))
)

@Composable
fun BloomCanvas(
    categoryTotals: Map<BillCategory, Double>,
    pendingSubscriptionCount: Int,
    modifier: Modifier = Modifier,
    monthlyBudget: Double = 2_500.0,
    highlightedCategory: BillCategory? = null
) {
    val activePetals = remember(categoryTotals) {
        bloomPetalSpecs.filter { spec ->
            (categoryTotals[spec.category] ?: 0.0) > 0.0
        }.ifEmpty {
            bloomPetalSpecs.take(4)
        }
    }

    val labelPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = MeadowGreenDark.toArgb()
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
    }

    val tooltipPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = MeadowGreenDark.toArgb()
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        val centerX = size.width / 2f
        val flowerCenterY = size.height * 0.42f
        val center = Offset(centerX, flowerCenterY)
        val maxRadius = min(size.width, size.height) * 0.34f
        val safeBudget = monthlyBudget.coerceAtLeast(1.0)
        val totalAmount = categoryTotals.values.sum().coerceAtLeast(1.0)
        val angleStep = 360f / activePetals.size

        activePetals.forEachIndexed { index, spec ->
            val amount = categoryTotals[spec.category] ?: (totalAmount / activePetals.size)
            val proportion = (amount / safeBudget).toFloat().coerceIn(0.18f, 1f)
            val petalLength = maxRadius * (0.72f + proportion * 0.28f)
            val petalWidth = maxRadius * 0.92f * proportion.coerceAtLeast(0.35f)
            val angle = index * angleStep - 90f
            val isHighlighted = highlightedCategory == null || highlightedCategory == spec.category

            rotate(angle, center) {
                drawGradientPetal(
                    center = center,
                    length = petalLength,
                    width = petalWidth,
                    baseColor = if (isHighlighted) spec.baseColor else spec.baseColor.copy(alpha = 0.45f),
                    accentColor = if (isHighlighted) spec.accentColor else spec.accentColor.copy(alpha = 0.45f)
                )
            }
        }

        drawCircle(
            color = Color(0xFFE8C547),
            radius = maxRadius * 0.12f,
            center = center
        )
        drawCircle(
            color = MeadowEarth,
            radius = maxRadius * 0.05f,
            center = center
        )

        val stemTop = Offset(centerX, flowerCenterY + maxRadius * 0.18f)
        val stemBottom = Offset(centerX, size.height * 0.88f)
        drawStemAndLeaves(stemTop = stemTop, stemBottom = stemBottom)

        activePetals.forEachIndexed { index, spec ->
            val angleRadians = Math.toRadians((index * angleStep - 90f).toDouble())
            val labelRadius = maxRadius * 1.18f
            val labelX = centerX + cos(angleRadians).toFloat() * labelRadius
            val labelY = flowerCenterY + sin(angleRadians).toFloat() * labelRadius + 10f
            drawContext.canvas.nativeCanvas.drawText(
                spec.label,
                labelX,
                labelY,
                labelPaint
            )
        }

        if (pendingSubscriptionCount > 0) {
            drawPendingCullTooltip(
                anchor = Offset(centerX + maxRadius * 0.35f, stemTop.y + 24f),
                pendingCount = pendingSubscriptionCount,
                textPaint = tooltipPaint
            )
        }
    }
}

private fun DrawScope.drawGradientPetal(
    center: Offset,
    length: Float,
    width: Float,
    baseColor: Color,
    accentColor: Color
) {
    val path = Path().apply {
        val tipY = center.y - length
        val controlOffset = width * 0.55f
        moveTo(center.x, center.y)
        cubicTo(
            center.x - controlOffset,
            center.y - length * 0.35f,
            center.x - width * 0.42f,
            tipY + length * 0.2f,
            center.x,
            tipY
        )
        cubicTo(
            center.x + width * 0.42f,
            tipY + length * 0.2f,
            center.x + controlOffset,
            center.y - length * 0.35f,
            center.x,
            center.y
        )
        close()
    }

    val gradient = Brush.linearGradient(
        colors = listOf(accentColor.copy(alpha = 0.95f), baseColor.copy(alpha = 0.88f)),
        start = Offset(center.x - width * 0.3f, center.y - length),
        end = Offset(center.x + width * 0.3f, center.y)
    )
    drawPath(path = path, brush = gradient)
    drawPath(
        path = path,
        color = MeadowWhite.copy(alpha = 0.15f)
    )
}

private fun DrawScope.drawStemAndLeaves(
    stemTop: Offset,
    stemBottom: Offset
) {
    val stemPath = Path().apply {
        moveTo(stemTop.x, stemTop.y)
        cubicTo(
            stemTop.x - 6f,
            stemTop.y + (stemBottom.y - stemTop.y) * 0.35f,
            stemTop.x + 8f,
            stemTop.y + (stemBottom.y - stemTop.y) * 0.7f,
            stemBottom.x,
            stemBottom.y
        )
    }
    drawPath(
        path = stemPath,
        color = MeadowGreen,
        alpha = 0.9f
    )

    val leafPathLeft = Path().apply {
        moveTo(stemTop.x, stemTop.y + 36f)
        cubicTo(
            stemTop.x - 42f,
            stemTop.y + 18f,
            stemTop.x - 48f,
            stemTop.y + 58f,
            stemTop.x - 8f,
            stemTop.y + 62f
        )
        cubicTo(
            stemTop.x - 2f,
            stemTop.y + 48f,
            stemTop.x + 4f,
            stemTop.y + 40f,
            stemTop.x,
            stemTop.y + 36f
        )
        close()
    }
    drawPath(path = leafPathLeft, color = Color(0xFF5A9E4B))

    val leafPathRight = Path().apply {
        moveTo(stemTop.x, stemTop.y + 52f)
        cubicTo(
            stemTop.x + 44f,
            stemTop.y + 34f,
            stemTop.x + 50f,
            stemTop.y + 74f,
            stemTop.x + 10f,
            stemTop.y + 78f
        )
        cubicTo(
            stemTop.x + 4f,
            stemTop.y + 64f,
            stemTop.x - 2f,
            stemTop.y + 56f,
            stemTop.x,
            stemTop.y + 52f
        )
        close()
    }
    drawPath(path = leafPathRight, color = Color(0xFF4A7C59))
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
