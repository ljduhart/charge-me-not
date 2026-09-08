package com.artie.chargemenot.ui.components

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
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.ui.theme.MeadowEarth
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowWhite
import kotlin.math.min

@Composable
fun BloomCanvas(
    parentCategoryTotals: Map<String, Double>,
    modifier: Modifier = Modifier,
    highlightedParent: String? = null
) {
    val activePetals = remember(parentCategoryTotals) {
        BloomCategoryDefinitions.categories.filter { definition ->
            (parentCategoryTotals[definition.parentName] ?: 0.0) > 0.0
        }.ifEmpty {
            BloomCategoryDefinitions.categories.take(4)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = min(size.width, size.height) / 2f * 0.85f
        val angleStep = 360f / activePetals.size

        activePetals.forEachIndexed { index, definition ->
            val isHighlighted = highlightedParent == null || highlightedParent == definition.parentName
            val alpha = if (isHighlighted) 1f else 0.5f
            val petalLength = maxRadius * 0.72f
            val petalWidth = maxRadius * 0.44f

            rotate(index * angleStep - 90f, center) {
                drawSimplePetal(
                    center = center,
                    length = petalLength,
                    width = petalWidth,
                    color = definition.midColor.copy(alpha = alpha)
                )
            }
        }

        drawCircle(color = MeadowEarth, radius = maxRadius * 0.12f, center = center)
        drawCircle(color = MeadowGreen, radius = maxRadius * 0.05f, center = center)
    }
}

private fun DrawScope.drawSimplePetal(
    center: Offset,
    length: Float,
    width: Float,
    color: Color
) {
    val path = Path().apply {
        val tipY = center.y - length
        moveTo(center.x, center.y)
        cubicTo(
            center.x - width * 0.5f, center.y - length * 0.35f,
            center.x - width * 0.25f, tipY + length * 0.1f,
            center.x, tipY
        )
        cubicTo(
            center.x + width * 0.25f, tipY + length * 0.1f,
            center.x + width * 0.5f, center.y - length * 0.35f,
            center.x, center.y
        )
        close()
    }

    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = 0.65f)),
            center = center,
            radius = length
        )
    )
    drawPath(path = path, color = MeadowWhite.copy(alpha = 0.25f))
}
