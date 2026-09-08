package com.artie.chargemenot.ui.components

import androidx.compose.ui.graphics.Color
import com.artie.chargemenot.domain.model.MeadowCategories
import kotlin.math.atan2
import kotlin.math.sqrt

data class BloomCategoryDefinition(
    val displayName: String,
    val bloomLabel: String,
    val parentName: String,
    val baseColor: Color,
    val midColor: Color,
    val highlightColor: Color,
    val shadowColor: Color,
    val labelAngleOffsetDegrees: Float = 0f
)

object BloomCategoryDefinitions {
    const val SLICE_COUNT = 6
    const val SLICE_DEGREES = 360f / SLICE_COUNT

    val categories: List<BloomCategoryDefinition> = listOf(
        BloomCategoryDefinition(
            displayName = "Canopy",
            bloomLabel = "CANOPY",
            parentName = MeadowCategories.CANOPY,
            baseColor = Color(0xFF8B3A2E),
            midColor = Color(0xFFE07A5F),
            highlightColor = Color(0xFFF4A261),
            shadowColor = Color(0xFF4A1F18)
        ),
        BloomCategoryDefinition(
            displayName = "Roots",
            bloomLabel = "ROOTS",
            parentName = MeadowCategories.ROOT_SYSTEM,
            baseColor = Color(0xFF2F4F7A),
            midColor = Color(0xFF6D9DC5),
            highlightColor = Color(0xFFA8C8E8),
            shadowColor = Color(0xFF1A2D45)
        ),
        BloomCategoryDefinition(
            displayName = "Vines",
            bloomLabel = "VINES",
            parentName = MeadowCategories.VINES,
            baseColor = Color(0xFF6B5B8A),
            midColor = Color(0xFFB8A9C9),
            highlightColor = Color(0xFFD8CCE8),
            shadowColor = Color(0xFF3A3048)
        ),
        BloomCategoryDefinition(
            displayName = "Fertilizer",
            bloomLabel = "FERTILIZER",
            parentName = MeadowCategories.FERTILIZER,
            baseColor = Color(0xFFB8860B),
            midColor = Color(0xFFF2CC8F),
            highlightColor = Color(0xFFFFF1C9),
            shadowColor = Color(0xFF6B4A08)
        ),
        BloomCategoryDefinition(
            displayName = "Pollinators",
            bloomLabel = "POLLINATORS",
            parentName = MeadowCategories.POLLINATORS,
            baseColor = Color(0xFF9E4F57),
            midColor = Color(0xFFE8B4B8),
            highlightColor = Color(0xFFF5D0D3),
            shadowColor = Color(0xFF5A2A30),
            labelAngleOffsetDegrees = -18f
        ),
        BloomCategoryDefinition(
            displayName = "Wildflowers",
            bloomLabel = "WILDFLOWERS",
            parentName = MeadowCategories.WILDFLOWERS,
            baseColor = Color(0xFF2F6B55),
            midColor = Color(0xFF81B29A),
            highlightColor = Color(0xFFB8E0CC),
            shadowColor = Color(0xFF1A3D30),
            labelAngleOffsetDegrees = 18f
        )
    )

    fun fromDisplayName(name: String): BloomCategoryDefinition? {
        return categories.firstOrNull { definition -> definition.displayName == name }
    }

    fun fromParentName(parentName: String): BloomCategoryDefinition? {
        return categories.firstOrNull { definition -> definition.parentName == parentName }
    }

    fun parentNameFor(displayName: String): String? {
        return fromDisplayName(displayName)?.parentName
    }

    fun indexForDisplayName(displayName: String): Int {
        return categories.indexOfFirst { definition -> definition.displayName == displayName }
    }

    fun displayNameAtSliceIndex(index: Int): String {
        return categories[index.coerceIn(0, categories.lastIndex)].displayName
    }
}

data class BloomLayoutSpec(
    val centerX: Float,
    val centerY: Float,
    val maxRadius: Float,
    val petalLength: Float,
    val petalWidth: Float,
    val labelRadius: Float,
    val innerTouchRadius: Float,
    val outerTouchRadius: Float,
    val labelTouchRadius: Float,
    val labelTextSizePx: Float
)

object BloomLayout {
    private const val FLOWER_RADIUS_FACTOR = 0.48f
    private const val UNIFORM_INSET_DP = 12f
    private val labelSamples = BloomCategoryDefinitions.categories.map { definition -> definition.bloomLabel }

    fun compute(canvasWidth: Float, canvasHeight: Float, density: Float): BloomLayoutSpec {
        val uniformInset = UNIFORM_INSET_DP * density
        val drawableWidth = (canvasWidth - uniformInset * 2f).coerceAtLeast(1f)
        val drawableHeight = (canvasHeight - uniformInset * 2f).coerceAtLeast(1f)
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f
        val drawableMin = minOf(drawableWidth, drawableHeight)
        val labelBand = drawableMin * 0.18f
        val maxRadius = ((drawableMin / 2f) - labelBand)
            .coerceAtLeast(drawableMin * 0.2f)
            .coerceAtMost(drawableMin * FLOWER_RADIUS_FACTOR)
        val petalLength = maxRadius * 0.9f
        val petalWidth = maxRadius * 0.56f
        val labelRadius = maxRadius * 1.08f
        val maxLabelWidth = drawableWidth * 0.28f
        val labelTextSizePx = fitLabelTextSize(
            labels = labelSamples,
            maxWidth = maxLabelWidth,
            baseTextSize = 10f * density
        )

        return BloomLayoutSpec(
            centerX = centerX,
            centerY = centerY,
            maxRadius = maxRadius,
            petalLength = petalLength,
            petalWidth = petalWidth,
            labelRadius = labelRadius,
            innerTouchRadius = maxRadius * 0.16f,
            outerTouchRadius = maxRadius * 0.98f,
            labelTouchRadius = (labelRadius + labelTextSizePx * 0.55f)
                .coerceAtMost(minOf(canvasWidth, canvasHeight) * 0.49f),
            labelTextSizePx = labelTextSizePx
        )
    }

    internal fun fitLabelTextSize(
        labels: List<String>,
        maxWidth: Float,
        baseTextSize: Float
    ): Float {
        if (labels.isEmpty() || maxWidth <= 0f) {
            return baseTextSize
        }

        val longestLabelLength = labels.maxOf { label -> label.length }
        val estimatedWidth = longestLabelLength * baseTextSize * 0.62f
        return if (estimatedWidth > maxWidth) {
            baseTextSize * (maxWidth / estimatedWidth)
        } else {
            baseTextSize
        }
    }
}

object BloomTouchMath {
    fun sliceIndexAtPoint(
        tapX: Float,
        tapY: Float,
        centerX: Float,
        centerY: Float,
        innerRadius: Float,
        outerRadius: Float
    ): Int? {
        val deltaX = tapX - centerX
        val deltaY = tapY - centerY
        val distance = sqrt(deltaX * deltaX + deltaY * deltaY)
        if (distance < innerRadius || distance > outerRadius) {
            return null
        }

        val radians = atan2(deltaY, deltaX)
        var degrees = Math.toDegrees(radians.toDouble()).toFloat()
        if (degrees < 0f) {
            degrees += 360f
        }

        val degreesFromTop = (degrees - 270f + 360f) % 360f
        return (degreesFromTop / BloomCategoryDefinitions.SLICE_DEGREES)
            .toInt()
            .coerceIn(0, BloomCategoryDefinitions.SLICE_COUNT - 1)
    }
}
