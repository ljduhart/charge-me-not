package com.artie.chargemenot.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BloomTouchMathTest {

    @Test
    fun sliceIndexAtPoint_returnsCanopyForTopSlice() {
        val index = BloomTouchMath.sliceIndexAtPoint(
            tapX = 200f,
            tapY = 80f,
            centerX = 200f,
            centerY = 200f,
            innerRadius = 30f,
            outerRadius = 180f
        )

        assertEquals(0, index)
        assertEquals("Canopy", BloomCategoryDefinitions.displayNameAtSliceIndex(index!!))
    }

    @Test
    fun sliceIndexAtPoint_returnsNullInsideCenterHole() {
        val index = BloomTouchMath.sliceIndexAtPoint(
            tapX = 200f,
            tapY = 200f,
            centerX = 200f,
            centerY = 200f,
            innerRadius = 40f,
            outerRadius = 180f
        )

        assertNull(index)
    }

    @Test
    fun sliceIndexAtPoint_returnsPollinatorsForLeftSideSlice() {
        val index = BloomTouchMath.sliceIndexAtPoint(
            tapX = 80f,
            tapY = 200f,
            centerX = 200f,
            centerY = 200f,
            innerRadius = 30f,
            outerRadius = 180f
        )

        assertEquals(4, index)
        assertEquals("Pollinators", BloomCategoryDefinitions.displayNameAtSliceIndex(index!!))
    }

    @Test
    fun bloomLayout_usesEqualPetalDimensions() {
        val layout = BloomLayout.compute(
            canvasWidth = 1080f,
            canvasHeight = 1500f,
            density = 3f
        )

        assertEquals(layout.petalLength, layout.petalLength)
        assertTrue(layout.petalLength > 0f)
        assertTrue(layout.petalWidth > 0f)
        assertEquals(layout.petalLength, layout.maxRadius * 0.9f)
        assertEquals(layout.petalWidth, layout.maxRadius * 0.56f)
    }

    @Test
    fun bloomLayout_fitsLongestCategoryLabel() {
        val layout = BloomLayout.compute(
            canvasWidth = 360f,
            canvasHeight = 500f,
            density = 2f
        )
        val longestLabelLength = BloomCategoryDefinitions.categories.maxOf { definition ->
            definition.bloomLabel.length
        }
        val estimatedWidth = longestLabelLength * layout.labelTextSizePx * 0.62f
        val maxLabelWidth = (360f - 56f) * 0.34f

        assertTrue(estimatedWidth <= maxLabelWidth + 1f)
    }

    @Test
    fun fitLabelTextSize_scalesDownWhenLabelsAreTooWide() {
        val fitted = BloomLayout.fitLabelTextSize(
            labels = listOf("POLLINATORS", "WILDFLOWERS", "FERTILIZER"),
            maxWidth = 40f,
            baseTextSize = 24f
        )

        assertTrue(fitted < 24f)
    }
}
