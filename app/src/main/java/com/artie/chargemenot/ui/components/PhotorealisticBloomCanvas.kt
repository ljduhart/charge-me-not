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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.ui.theme.MeadowEarth
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowWhite
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

private const val FOREGROUND_PARALLAX_FACTOR = 1.5f

@Composable
fun PhotorealisticBloomCanvas(
    parentCategoryTotals: Map<String, Double>,
    monthlyBudget: Double,
    highlightedParent: String?,
    onPetalTapped: (String) -> Unit,
    parallaxOffset: Pair<Float, Float> = 0f to 0f,
    modifier: Modifier = Modifier
) {
    var pressedCategory by remember { mutableStateOf<String?>(null) }
    val density = LocalDensity.current.density
    val (tiltX, tiltY) = parallaxOffset
    val foregroundParallaxPx = with(LocalDensity.current) { 12.dp.toPx() }
    val foregroundTranslationX = tiltX * FOREGROUND_PARALLAX_FACTOR * foregroundParallaxPx
    val foregroundTranslationY = tiltY * FOREGROUND_PARALLAX_FACTOR * foregroundParallaxPx

    val categoryScales = BloomCategoryDefinitions.categories.associate { definition ->
        val targetScale = if (pressedCategory == definition.displayName) 0.92f else 1f
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

    val labelPaint = remember(density) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = MeadowGreenDark.toArgb()
            textSize = 11f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isSubpixelText = true
            isLinearText = true
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(360.dp)
            .padding(horizontal = 2.dp)
            .graphicsLayer {
                translationX = foregroundTranslationX
                translationY = foregroundTranslationY
            }
            .pointerInput(density, foregroundTranslationX, foregroundTranslationY) {
                detectTapGestures { offset ->
                    val layout = BloomLayout.compute(
                        canvasWidth = size.width.toFloat(),
                        canvasHeight = size.height.toFloat(),
                        density = density
                    )

                    val sliceIndex = BloomTouchMath.resolveSliceIndex(
                        tapX = offset.x - foregroundTranslationX,
                        tapY = offset.y - foregroundTranslationY,
                        layout = layout
                    ) ?: return@detectTapGestures

                    val categoryName = BloomCategoryDefinitions.displayNameAtSliceIndex(sliceIndex)
                    pressedCategory = categoryName
                    onPetalTapped(categoryName)
                }
            }
    ) {
        val layout = BloomLayout.compute(
            canvasWidth = size.width,
            canvasHeight = size.height,
            density = density
        )
        val center = Offset(layout.centerX, layout.centerY)
        labelPaint.textSize = layout.labelTextSizePx
        val soilLayout = computeSoilMoundLayout(
            centerX = layout.centerX,
            canvasHeight = size.height,
            maxRadius = layout.maxRadius
        )

        drawSoilMound(
            soilLayout = soilLayout,
            canvasHeight = size.height
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    MeadowEarth.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = center,
                radius = layout.maxRadius * 1.35f
            ),
            radius = layout.maxRadius * 1.35f,
            center = center
        )

        drawStemAndLeaves(
            center = center,
            maxRadius = layout.maxRadius,
            stemBottomY = soilLayout.stemBottomY
        )

        BloomCategoryDefinitions.categories.forEachIndexed { index, definition ->
                val amount = parentCategoryTotals[definition.parentName] ?: 0.0
                val spendIntensity = if (amount <= 0.0 || monthlyBudget <= 0.0) {
                    0.72f
                } else {
                    (amount / monthlyBudget.coerceAtLeast(1.0)).toFloat().coerceIn(0.72f, 1f)
                }
                val sliceAngle = -90f + index * BloomCategoryDefinitions.SLICE_DEGREES
                val isHighlighted = highlightedParent == null || highlightedParent == definition.parentName
                val alphaMultiplier = (if (isHighlighted) 1f else 0.52f) * spendIntensity.coerceAtLeast(0.75f)
                val scale = categoryScales[definition.displayName]?.value ?: 1f

                rotate(sliceAngle, center) {
                    scale(scale, scale, pivot = center) {
                        drawPhotorealisticPetal(
                            center = center,
                            length = layout.petalLength,
                            width = layout.petalWidth,
                            definition = definition,
                            alphaMultiplier = alphaMultiplier
                        )
                    }
                }

                drawCategoryLabel(
                    definition = definition,
                    layout = layout,
                    sliceIndex = index,
                    sliceAngle = sliceAngle,
                    alphaMultiplier = alphaMultiplier,
                    labelPaint = labelPaint
                )
            }

        drawFlowerCenter(center = center, radius = layout.maxRadius * 0.15f)
    }
}

private data class SoilMoundLayout(
    val centerX: Float,
    val moundCenterY: Float,
    val moundRadius: Float,
    val stemBottomY: Float
)

private fun computeSoilMoundLayout(
    centerX: Float,
    canvasHeight: Float,
    maxRadius: Float
): SoilMoundLayout {
    val moundRadius = maxRadius * 0.64f
    val moundCenterY = canvasHeight - moundRadius * 0.38f
    val stemBottomY = moundCenterY + moundRadius * 0.18f
    return SoilMoundLayout(
        centerX = centerX,
        moundCenterY = moundCenterY,
        moundRadius = moundRadius,
        stemBottomY = stemBottomY
    )
}

private fun DrawScope.drawSoilMound(
    soilLayout: SoilMoundLayout,
    canvasHeight: Float
) {
    val centerX = soilLayout.centerX
    val halfWidth = soilLayout.moundRadius * 1.05f
    val peakY = soilLayout.moundCenterY - soilLayout.moundRadius * 0.62f
    val baseY = soilLayout.moundCenterY + soilLayout.moundRadius * 0.32f

    val basePath = Path().apply {
        moveTo(centerX - halfWidth, baseY)
        cubicTo(
            centerX - halfWidth * 0.48f,
            baseY - soilLayout.moundRadius * 0.42f,
            centerX - halfWidth * 0.18f,
            peakY + soilLayout.moundRadius * 0.08f,
            centerX,
            peakY
        )
        cubicTo(
            centerX + halfWidth * 0.18f,
            peakY + soilLayout.moundRadius * 0.08f,
            centerX + halfWidth * 0.48f,
            baseY - soilLayout.moundRadius * 0.42f,
            centerX + halfWidth,
            baseY
        )
        lineTo(centerX + halfWidth * 1.04f, canvasHeight)
        lineTo(centerX - halfWidth * 1.04f, canvasHeight)
        close()
    }

    drawPath(
        path = basePath,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF3E2723),
                Color(0xFF1B100B)
            ),
            center = Offset(centerX, peakY + soilLayout.moundRadius * 0.18f),
            radius = halfWidth * 1.15f
        )
    )

    val soilClumps = listOf(
        SoilClumpSpec(offsetXFactor = -0.30f, offsetYFactor = 0.22f, radiusFactor = 0.36f),
        SoilClumpSpec(offsetXFactor = 0.24f, offsetYFactor = 0.18f, radiusFactor = 0.32f),
        SoilClumpSpec(offsetXFactor = -0.06f, offsetYFactor = 0.30f, radiusFactor = 0.40f),
        SoilClumpSpec(offsetXFactor = 0.38f, offsetYFactor = 0.26f, radiusFactor = 0.28f),
        SoilClumpSpec(offsetXFactor = -0.44f, offsetYFactor = 0.14f, radiusFactor = 0.26f),
        SoilClumpSpec(offsetXFactor = 0.10f, offsetYFactor = 0.10f, radiusFactor = 0.24f)
    )

    soilClumps.forEach { clump ->
        val clumpCenterX = centerX + halfWidth * clump.offsetXFactor
        val clumpCenterY = baseY - soilLayout.moundRadius * clump.offsetYFactor
        val clumpRadius = soilLayout.moundRadius * clump.radiusFactor
        val clumpPath = Path().apply {
            moveTo(clumpCenterX - clumpRadius, clumpCenterY)
            cubicTo(
                clumpCenterX - clumpRadius * 0.55f,
                clumpCenterY - clumpRadius * 0.72f,
                clumpCenterX + clumpRadius * 0.45f,
                clumpCenterY - clumpRadius * 0.68f,
                clumpCenterX + clumpRadius,
                clumpCenterY
            )
            cubicTo(
                clumpCenterX + clumpRadius * 0.42f,
                clumpCenterY + clumpRadius * 0.48f,
                clumpCenterX - clumpRadius * 0.38f,
                clumpCenterY + clumpRadius * 0.44f,
                clumpCenterX - clumpRadius,
                clumpCenterY
            )
            close()
        }

        drawPath(
            path = clumpPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF4E342E),
                    Color(0xFF3E2723).copy(alpha = 0.72f),
                    Color(0xFF1B100B).copy(alpha = 0.45f)
                ),
                center = Offset(clumpCenterX, clumpCenterY - clumpRadius * 0.18f),
                radius = clumpRadius * 1.25f
            )
        )
    }
}

private data class SoilClumpSpec(
    val offsetXFactor: Float,
    val offsetYFactor: Float,
    val radiusFactor: Float
)

private fun DrawScope.drawCategoryLabel(
    definition: BloomCategoryDefinition,
    layout: BloomLayoutSpec,
    sliceIndex: Int,
    sliceAngle: Float,
    alphaMultiplier: Float,
    labelPaint: Paint
) {
    val (labelX, labelY) = BloomTouchMath.labelPositionFor(
        layout = layout,
        definition = definition,
        sliceIndex = sliceIndex
    )
    val connectorAngleRadians = Math.toRadians((sliceAngle - 90f).toDouble())
    val petalTipRadius = layout.maxRadius * 0.82f
    val petalTipX = layout.centerX + cos(connectorAngleRadians).toFloat() * petalTipRadius
    val petalTipY = layout.centerY + sin(connectorAngleRadians).toFloat() * petalTipRadius

    drawLine(
        color = MeadowGreenDark.copy(alpha = 0.28f * alphaMultiplier),
        start = Offset(petalTipX, petalTipY),
        end = Offset(labelX, labelY),
        strokeWidth = 1.25f
    )

    val horizontalMargin = layout.maxRadius * 0.1f
    labelPaint.textAlign = when (definition.displayName) {
        "Pollinators" -> Paint.Align.RIGHT
        "Wildflowers" -> Paint.Align.LEFT
        else -> when {
            labelX > layout.centerX + horizontalMargin -> Paint.Align.RIGHT
            labelX < layout.centerX - horizontalMargin -> Paint.Align.LEFT
            else -> Paint.Align.CENTER
        }
    }

    val baselineOffset = when (definition.displayName) {
        "Pollinators" -> layout.labelTextSizePx * 0.2f
        "Wildflowers" -> layout.labelTextSizePx * 0.2f
        else -> when {
            labelY < layout.centerY - layout.maxRadius * 0.2f -> layout.labelTextSizePx * 0.35f
            labelY > layout.centerY + layout.maxRadius * 0.2f -> -layout.labelTextSizePx * 0.15f
            else -> layout.labelTextSizePx * 0.35f
        }
    }

    drawContext.canvas.nativeCanvas.drawText(
        definition.bloomLabel,
        labelX,
        labelY + baselineOffset,
        labelPaint.apply {
            alpha = (255 * alphaMultiplier).toInt().coerceIn(0, 255)
        }
    )
}

private fun DrawScope.drawPhotorealisticPetal(
    center: Offset,
    length: Float,
    width: Float,
    definition: BloomCategoryDefinition,
    alphaMultiplier: Float
) {
    val petalPath = createPetalPath(center = center, length = length, width = width)

    drawPath(
        path = petalPath,
        brush = Brush.linearGradient(
            colors = listOf(
                definition.shadowColor.copy(alpha = 0.62f * alphaMultiplier),
                definition.shadowColor.copy(alpha = 0.18f * alphaMultiplier)
            ),
            start = Offset(center.x - width * 0.25f, center.y + length * 0.1f),
            end = Offset(center.x, center.y - length * 1.05f)
        )
    )

    drawPath(
        path = petalPath,
        brush = Brush.radialGradient(
            colors = listOf(
                definition.highlightColor.copy(alpha = alphaMultiplier),
                definition.midColor.copy(alpha = alphaMultiplier),
                definition.baseColor.copy(alpha = alphaMultiplier),
                definition.shadowColor.copy(alpha = 0.9f * alphaMultiplier)
            ),
            center = Offset(center.x, center.y - length * 0.7f),
            radius = length * 1.05f
        )
    )

    drawPath(
        path = petalPath,
        brush = Brush.linearGradient(
            colors = listOf(
                MeadowWhite.copy(alpha = 0.42f * alphaMultiplier),
                Color.Transparent,
                definition.shadowColor.copy(alpha = 0.22f * alphaMultiplier)
            ),
            start = Offset(center.x - width * 0.38f, center.y - length * 0.18f),
            end = Offset(center.x + width * 0.22f, center.y - length * 0.88f)
        )
    )

    drawPetalVeins(center = center, length = length, width = width, definition = definition, alphaMultiplier = alphaMultiplier)
    drawPetalEdgeHighlight(petalPath = petalPath, alphaMultiplier = alphaMultiplier)
    drawPetalMicroTexture(center = center, length = length, width = width, alphaMultiplier = alphaMultiplier)
}

private fun DrawScope.drawPetalVeins(
    center: Offset,
    length: Float,
    width: Float,
    definition: BloomCategoryDefinition,
    alphaMultiplier: Float
) {
    val primaryVein = Path().apply {
        moveTo(center.x, center.y - length * 0.06f)
        cubicTo(
            center.x - width * 0.04f,
            center.y - length * 0.42f,
            center.x + width * 0.03f,
            center.y - length * 0.7f,
            center.x,
            center.y - length * 0.96f
        )
    }
    drawPath(
        path = primaryVein,
        color = definition.shadowColor.copy(alpha = 0.34f * alphaMultiplier),
        style = Stroke(width = 2.2f)
    )

    listOf(-0.22f, 0.22f).forEach { side ->
        val sideVein = Path().apply {
            moveTo(center.x, center.y - length * 0.2f)
            cubicTo(
                center.x + width * side * 0.35f,
                center.y - length * 0.42f,
                center.x + width * side * 0.28f,
                center.y - length * 0.62f,
                center.x + width * side * 0.12f,
                center.y - length * 0.78f
            )
        }
        drawPath(
            path = sideVein,
            color = definition.shadowColor.copy(alpha = 0.2f * alphaMultiplier),
            style = Stroke(width = 1.2f)
        )
    }
}

private fun DrawScope.drawPetalEdgeHighlight(
    petalPath: Path,
    alphaMultiplier: Float
) {
    drawPath(
        path = petalPath,
        color = MeadowWhite.copy(alpha = 0.18f * alphaMultiplier),
        style = Stroke(width = 1.4f)
    )
}

private fun DrawScope.drawPetalMicroTexture(
    center: Offset,
    length: Float,
    width: Float,
    alphaMultiplier: Float
) {
    val speckOffsets = listOf(
        Offset(-0.18f, -0.35f),
        Offset(0.12f, -0.48f),
        Offset(-0.08f, -0.62f),
        Offset(0.2f, -0.7f),
        Offset(-0.14f, -0.8f)
    )
    speckOffsets.forEach { offset ->
        drawCircle(
            color = MeadowWhite.copy(alpha = 0.12f * alphaMultiplier),
            radius = 1.6f,
            center = Offset(
                center.x + width * offset.x,
                center.y - length * offset.y
            )
        )
    }
}

private fun createPetalPath(
    center: Offset,
    length: Float,
    width: Float
): Path {
    return Path().apply {
        moveTo(center.x, center.y)
        cubicTo(
            center.x - width * 0.58f,
            center.y - length * 0.2f,
            center.x - width * 0.74f,
            center.y - length * 0.64f,
            center.x,
            center.y - length
        )
        cubicTo(
            center.x + width * 0.74f,
            center.y - length * 0.64f,
            center.x + width * 0.58f,
            center.y - length * 0.2f,
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
                Color(0xFFFFF8DC),
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

    val pollenSpecks = listOf(
        Offset(-0.35f, -0.2f),
        Offset(0.28f, -0.15f),
        Offset(-0.1f, 0.25f),
        Offset(0.32f, 0.18f),
        Offset(0f, -0.32f),
        Offset(-0.25f, 0.12f)
    )
    pollenSpecks.forEach { offset ->
        drawCircle(
            color = Color(0xFF8B6914).copy(alpha = 0.55f),
            radius = radius * 0.09f,
            center = Offset(center.x + radius * offset.x, center.y + radius * offset.y)
        )
    }

    drawCircle(
        color = MeadowEarth.copy(alpha = 0.28f),
        radius = radius * 0.52f,
        center = center
    )
}

private fun DrawScope.drawStemAndLeaves(
    center: Offset,
    maxRadius: Float,
    stemBottomY: Float
) {
    val stemTop = Offset(center.x, center.y + maxRadius * 0.10f)
    val stemBottom = Offset(center.x, stemBottomY.coerceAtLeast(stemTop.y + 4f))
    val stemLength = (stemBottom.y - stemTop.y).coerceAtLeast(1f)

    drawLine(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF6FAF72),
                Color(0xFF4F8F57),
                Color(0xFF2F5A36),
                Color(0xFF234A2A)
            ),
            startY = stemTop.y,
            endY = stemBottom.y
        ),
        start = stemTop,
        end = stemBottom,
        strokeWidth = 10f
    )

    val leftLeaf = Path().apply {
        moveTo(stemTop.x, stemTop.y + stemLength * 0.22f)
        cubicTo(
            stemTop.x - maxRadius * 0.30f,
            stemTop.y + stemLength * 0.28f,
            stemTop.x - maxRadius * 0.24f,
            stemTop.y + stemLength * 0.48f,
            stemTop.x - maxRadius * 0.04f,
            stemTop.y + stemLength * 0.44f
        )
        close()
    }
    drawPath(
        path = leftLeaf,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF9BCF9D), Color(0xFF4A7C59)),
            start = leftLeaf.getBounds().topLeft,
            end = leftLeaf.getBounds().bottomRight
        )
    )

    val rightLeaf = Path().apply {
        moveTo(stemTop.x, stemTop.y + stemLength * 0.36f)
        cubicTo(
            stemTop.x + maxRadius * 0.28f,
            stemTop.y + stemLength * 0.40f,
            stemTop.x + maxRadius * 0.22f,
            stemTop.y + stemLength * 0.58f,
            stemTop.x + maxRadius * 0.03f,
            stemTop.y + stemLength * 0.54f
        )
        close()
    }
    drawPath(
        path = rightLeaf,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFB5D4A8), Color(0xFF5A9E4B)),
            start = rightLeaf.getBounds().topLeft,
            end = rightLeaf.getBounds().bottomRight
        )
    )
}
