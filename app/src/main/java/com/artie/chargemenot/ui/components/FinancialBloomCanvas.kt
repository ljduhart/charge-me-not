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
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.ui.theme.MeadowEarth
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowSage
import com.artie.chargemenot.ui.theme.MeadowSunflower
import com.artie.chargemenot.ui.theme.MeadowWhite
import com.artie.chargemenot.ui.theme.meadowParentColor
import kotlin.math.min

private val meadowPetalSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

@Composable
fun FinancialBloomCanvas(
    parentCategoryTotals: Map<String, Long>,
    modifier: Modifier = Modifier,
    highlightedParent: String? = null,
    projectedParentCategoryTotals: Map<String, Long> = parentCategoryTotals,
    monthlyBudget: Long = 250_000L,
    sizeByMonthlyBudget: Boolean = false,
    parentCategoryAlphas: Map<String, Float> = emptyMap()
) {
    val displayTotals = if (projectedParentCategoryTotals.isNotEmpty()) {
        projectedParentCategoryTotals
    } else {
        parentCategoryTotals
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
    val animatedAlphas = rememberAnimatedParentAlphas(parentCategoryAlphas)

    val entries = animatedAmounts.entries
        .filter { (_, amount) -> amount > 0f }
        .sortedBy { (parent, _) ->
            MeadowCategories.parentNames.indexOf(parent).takeIf { it >= 0 } ?: Int.MAX_VALUE
        }

    val totalAmount = entries.sumOf { it.value.toDouble() }.coerceAtLeast(1.0)
    val safeBudget = monthlyBudget.coerceAtLeast(1L).toDouble()

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

        entries.forEachIndexed { index, (parentCategory, amount) ->
            val proportion = if (sizeByMonthlyBudget) {
                (amount / safeBudget).toFloat()
            } else {
                (amount / totalAmount).toFloat()
            }.coerceIn(0.12f, 1f)
            val petalLength = maxRadius * (0.45f + proportion * 0.55f) * bloomProgress
            val petalWidth = maxRadius * 0.34f * proportion.coerceAtLeast(0.35f) * bloomProgress
            val angle = index * angleStep - 90f
            val isHighlighted = parentCategory == highlightedParent
            val alphaScale = (animatedAlphas[parentCategory] ?: 1f) * bloomProgress

            rotate(angle, center) {
                drawPetal(
                    center = center,
                    length = petalLength,
                    width = petalWidth,
                    color = meadowParentColor(parentCategory),
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
    targetTotals: Map<String, Long>
): Map<String, Float> {
    val amounts = remember { mutableStateMapOf<String, Float>() }
    MeadowCategories.parentNames.forEach { parent ->
        key(parent) {
            val target = (targetTotals[parent] ?: 0L).toFloat()
            val animated by animateFloatAsState(
                targetValue = target,
                animationSpec = meadowPetalSpring,
                label = "petalAmount_$parent"
            )
            amounts[parent] = animated
        }
    }
    targetTotals.keys.filter { it !in MeadowCategories.parentNames }.forEach { parent ->
        key(parent) {
            val target = (targetTotals[parent] ?: 0L).toFloat()
            val animated by animateFloatAsState(
                targetValue = target,
                animationSpec = meadowPetalSpring,
                label = "petalAmount_$parent"
            )
            amounts[parent] = animated
        }
    }
    return amounts.toMap()
}

@Composable
private fun rememberAnimatedParentAlphas(
    targetAlphas: Map<String, Float>
): Map<String, Float> {
    val alphas = remember { mutableStateMapOf<String, Float>() }
    MeadowCategories.parentNames.forEach { parent ->
        key(parent) {
            val target = targetAlphas[parent] ?: 1f
            val animated by animateFloatAsState(
                targetValue = target,
                animationSpec = meadowPetalSpring,
                label = "petalAlpha_$parent"
            )
            alphas[parent] = animated
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

fun categoryDisplayName(parentCategory: String, subCategory: String? = null): String {
    val parentLabel = MeadowCategories.shortDisplayName(parentCategory)
    return if (subCategory.isNullOrBlank()) {
        parentLabel
    } else {
        "$parentLabel · $subCategory"
    }
}

fun billInitial(name: String): String {
    val trimmed = name.trim()
    return if (trimmed.isEmpty()) {
        "?"
    } else {
        trimmed.first().uppercaseChar().toString()
    }
}
