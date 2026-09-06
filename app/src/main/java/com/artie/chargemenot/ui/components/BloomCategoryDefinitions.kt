package com.artie.chargemenot.ui.components

import androidx.compose.ui.graphics.Color
import com.artie.chargemenot.domain.model.BillCategory
import kotlin.math.atan2
import kotlin.math.sqrt

data class BloomCategoryDefinition(
    val displayName: String,
    val billCategory: BillCategory,
    val baseColor: Color,
    val midColor: Color,
    val highlightColor: Color,
    val shadowColor: Color
)

object BloomCategoryDefinitions {
    const val SLICE_COUNT = 6
    const val SLICE_DEGREES = 360f / SLICE_COUNT

    val categories: List<BloomCategoryDefinition> = listOf(
        BloomCategoryDefinition(
            displayName = "Rent",
            billCategory = BillCategory.RENT,
            baseColor = Color(0xFF8B3A2E),
            midColor = Color(0xFFE07A5F),
            highlightColor = Color(0xFFF4A261),
            shadowColor = Color(0xFF4A1F18)
        ),
        BloomCategoryDefinition(
            displayName = "Health",
            billCategory = BillCategory.HEALTHCARE,
            baseColor = Color(0xFF9E4F57),
            midColor = Color(0xFFE8B4B8),
            highlightColor = Color(0xFFF5D0D3),
            shadowColor = Color(0xFF5A2A30)
        ),
        BloomCategoryDefinition(
            displayName = "Food",
            billCategory = BillCategory.FOOD,
            baseColor = Color(0xFFB8860B),
            midColor = Color(0xFFF2CC8F),
            highlightColor = Color(0xFFFFF1C9),
            shadowColor = Color(0xFF6B4A08)
        ),
        BloomCategoryDefinition(
            displayName = "Subscriptions",
            billCategory = BillCategory.SUBSCRIPTIONS,
            baseColor = Color(0xFF6B5B8A),
            midColor = Color(0xFFB8A9C9),
            highlightColor = Color(0xFFD8CCE8),
            shadowColor = Color(0xFF3A3048)
        ),
        BloomCategoryDefinition(
            displayName = "Utilities",
            billCategory = BillCategory.UTILITIES,
            baseColor = Color(0xFF2F6B55),
            midColor = Color(0xFF81B29A),
            highlightColor = Color(0xFFB8E0CC),
            shadowColor = Color(0xFF1A3D30)
        ),
        BloomCategoryDefinition(
            displayName = "Loans",
            billCategory = BillCategory.TRANSPORTATION,
            baseColor = Color(0xFF2F4F7A),
            midColor = Color(0xFF6D9DC5),
            highlightColor = Color(0xFFA8C8E8),
            shadowColor = Color(0xFF1A2D45)
        )
    )

    fun fromDisplayName(name: String): BloomCategoryDefinition? {
        return categories.firstOrNull { definition -> definition.displayName == name }
    }

    fun billCategoryNameFor(displayName: String): String? {
        return fromDisplayName(displayName)?.billCategory?.name
    }

    fun indexForDisplayName(displayName: String): Int {
        return categories.indexOfFirst { definition -> definition.displayName == displayName }
    }

    fun displayNameAtSliceIndex(index: Int): String {
        return categories[index.coerceIn(0, categories.lastIndex)].displayName
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
