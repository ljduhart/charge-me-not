package com.artie.chargemenot.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.ui.theme.MeadowBlush
import com.artie.chargemenot.ui.theme.MeadowEarth
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowGreenLight
import com.artie.chargemenot.ui.theme.MeadowLavender
import com.artie.chargemenot.ui.theme.MeadowRose
import com.artie.chargemenot.ui.theme.MeadowSage
import com.artie.chargemenot.ui.theme.MeadowSky
import com.artie.chargemenot.ui.theme.MeadowSunflower
import com.artie.chargemenot.ui.theme.MeadowWhite
import kotlin.math.min

private val meadowPetalSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

@Composable
fun FinancialBloomCanvas(
    categoryTotals: Map<BillCategory, Double>,
    modifier: Modifier = Modifier,
    highlightedCategory: BillCategory? = null,
    projectedCategoryTotals: Map<BillCategory, Double> = categoryTotals,
    monthlyBudget: Double = 2_500.0,
    sizeByMonthlyBudget: Boolean = false,
    categoryAlphas: Map<BillCategory, Float> = emptyMap()
) {
    val displayTotals = if (projectedCategoryTotals.isNotEmpty()) {
        projectedCategoryTotals
    } else {
        categoryTotals
    }

    var bloomTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        bloomTriggered = true
    }

    val bloomProgress by animateFloatAsState(
        targetValue = if (bloomTriggered) 1f else 0f,
        animationSpec = meadowPetalSpring,
        label = "financialBloomProgress"
    )

    val animatedAmounts = rememberAnimatedPetalAmounts(displayTotals)
    val animatedAlphas = rememberAnimatedCategoryAlphas(categoryAlphas)

    val entries = animatedAmounts.entries
        .filter { (_, amount) -> amount > 0f }
        .sortedBy { (category, _) -> category.ordinal }

    val totalAmount = entries.sumOf { it.value.toDouble() }.coerceAtLeast(1.0)
    val safeBudget = monthlyBudget.coerceAtLeast(1.0)

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = min(size.width, size.height) / 2f * 0.85f

        drawCircle(
            color = MeadowGreen.copy(alpha = 0.12f * bloomProgress),
            radius = maxRadius * 1.05f * bloomProgress,
            center = center
        )

        if (entries.isEmpty()) {
            drawCircle(
                color = MeadowSage.copy(alpha = 0.4f * bloomProgress),
                radius = maxRadius * 0.3f * bloomProgress,
                center = center
            )
            return@Canvas
        }

        val angleStep = 360f / entries.size

        entries.forEachIndexed { index, (category, amount) ->
            val proportion = if (sizeByMonthlyBudget) {
                (amount / safeBudget).toFloat()
            } else {
                (amount / totalAmount).toFloat()
            }.coerceIn(0.12f, 1f)
            val petalLength = maxRadius * (0.45f + proportion * 0.55f) * bloomProgress
            val petalWidth = maxRadius * 0.34f * proportion.coerceAtLeast(0.35f) * bloomProgress
            val angle = index * angleStep - 90f
            val isHighlighted = category == highlightedCategory
            val alphaScale = (animatedAlphas[category] ?: 1f) * bloomProgress

            rotate(angle, center) {
                drawPetal(
                    center = center,
                    length = petalLength,
                    width = petalWidth,
                    color = categoryColor(category),
                    highlighted = isHighlighted,
                    alphaScale = alphaScale
                )
            }
        }

        drawCircle(
            color = MeadowSunflower.copy(alpha = bloomProgress),
            radius = maxRadius * 0.14f * bloomProgress,
            center = center
        )
        drawCircle(
            color = MeadowEarth.copy(alpha = bloomProgress),
            radius = maxRadius * 0.06f * bloomProgress,
            center = center
        )
    }
}

@Composable
private fun rememberAnimatedPetalAmounts(
    targetTotals: Map<BillCategory, Double>
): Map<BillCategory, Float> {
    val amounts = remember { mutableStateMapOf<BillCategory, Float>() }
    BillCategory.entries.forEach { category ->
        key(category) {
            val target = (targetTotals[category] ?: 0.0).toFloat()
            val animated by animateFloatAsState(
                targetValue = target,
                animationSpec = meadowPetalSpring,
                label = "petalAmount_${category.name}"
            )
            amounts[category] = animated
        }
    }
    return amounts.toMap()
}

@Composable
private fun rememberAnimatedCategoryAlphas(
    targetAlphas: Map<BillCategory, Float>
): Map<BillCategory, Float> {
    val alphas = remember { mutableStateMapOf<BillCategory, Float>() }
    BillCategory.entries.forEach { category ->
        key(category) {
            val target = targetAlphas[category] ?: 1f
            val animated by animateFloatAsState(
                targetValue = target,
                animationSpec = meadowPetalSpring,
                label = "petalAlpha_${category.name}"
            )
            alphas[category] = animated
        }
    }
    return alphas.toMap()
}

private fun DrawScope.drawPetal(
    center: Offset,
    length: Float,
    width: Float,
    color: Color,
    highlighted: Boolean,
    alphaScale: Float = 1f
) {
    if (length <= 0f || width <= 0f || alphaScale <= 0f) {
        return
    }

    val path = Path().apply {
        val tipY = center.y - length
        val controlOffset = width * 0.6f

        moveTo(center.x, center.y)
        cubicTo(
            center.x - controlOffset, center.y - length * 0.4f,
            center.x - width * 0.3f, tipY + length * 0.15f,
            center.x, tipY
        )
        cubicTo(
            center.x + width * 0.3f, tipY + length * 0.15f,
            center.x + controlOffset, center.y - length * 0.4f,
            center.x, center.y
        )
        close()
    }

    val fillAlpha = if (highlighted) {
        1f
    } else {
        (0.85f * alphaScale).coerceIn(0.12f, 1f)
    }
    val strokeWidth = if (highlighted) 3f else 1.5f
    val strokeColor = if (highlighted) MeadowWhite else color.copy(alpha = 0.3f)

    drawPath(path = path, color = color.copy(alpha = fillAlpha))
    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(width = strokeWidth)
    )
}

fun categoryColor(category: BillCategory): Color = when (category) {
    BillCategory.RENT -> MeadowRose
    BillCategory.FOOD -> MeadowSunflower
    BillCategory.UTILITIES -> MeadowSky
    BillCategory.SUBSCRIPTIONS -> MeadowLavender
    BillCategory.TRANSPORTATION -> MeadowEarth
    BillCategory.HEALTHCARE -> MeadowBlush
    BillCategory.ENTERTAINMENT -> MeadowGreenLight
    BillCategory.OTHER -> MeadowSage
}

fun categoryDisplayName(category: BillCategory): String = when (category) {
    BillCategory.RENT -> "Rent"
    BillCategory.FOOD -> "Groceries"
    BillCategory.UTILITIES -> "Utilities"
    BillCategory.SUBSCRIPTIONS -> "Subscriptions"
    BillCategory.TRANSPORTATION -> "Transportation"
    BillCategory.HEALTHCARE -> "Healthcare"
    BillCategory.ENTERTAINMENT -> "Entertainment"
    BillCategory.OTHER -> "Other"
}
