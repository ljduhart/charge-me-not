package com.artie.chargemenot.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
            canvasHeight = 1050f,
            density = 3f
        )

        assertEquals(540f, layout.centerX, 0.1f)
        assertEquals(525f, layout.centerY, 0.1f)
        assertEquals(layout.petalLength, layout.maxRadius * 0.9f)
        assertEquals(layout.petalWidth, layout.maxRadius * 0.56f)
        assertTrue(layout.labelTouchRadius >= layout.outerTouchRadius)
        assertTrue(layout.maxRadius > 1080f * 0.24f)
    }

    @Test
    fun bloomLayout_fitsLongestCategoryLabel() {
        val layout = BloomLayout.compute(
            canvasWidth = 360f,
            canvasHeight = 350f,
            density = 2f
        )
        val longestLabelLength = BloomCategoryDefinitions.categories.maxOf { definition ->
            definition.bloomLabel.length
        }
        val estimatedWidth = longestLabelLength * layout.labelTextSizePx * 0.62f
        val maxLabelWidth = (360f - 48f) * 0.28f

        assertTrue(estimatedWidth <= maxLabelWidth + 1f)
    }

    @Test
    fun bloomCategoryDefinitions_separatesBottomLabelAngles() {
        val pollinators = BloomCategoryDefinitions.fromDisplayName("Pollinators")
        val wildflowers = BloomCategoryDefinitions.fromDisplayName("Wildflowers")

        assertNotNull(pollinators)
        assertNotNull(wildflowers)
        assertTrue(pollinators!!.labelAngleOffsetDegrees < 0f)
        assertTrue(wildflowers!!.labelAngleOffsetDegrees > 0f)
        assertTrue(
            kotlin.math.abs(
                pollinators.labelAngleOffsetDegrees - wildflowers.labelAngleOffsetDegrees
            ) >= 30f
        )
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

    @Test
    fun sliceIndexAtPoint_acceptsLabelRadiusTap() {
        val layout = BloomLayout.compute(
            canvasWidth = 360f,
            canvasHeight = 350f,
            density = 2f
        )
        val labelAngleRadians = Math.toRadians(-90.0)
        val tapX = layout.centerX + kotlin.math.cos(labelAngleRadians).toFloat() * layout.labelTouchRadius
        val tapY = layout.centerY + kotlin.math.sin(labelAngleRadians).toFloat() * layout.labelTouchRadius

        val index = BloomTouchMath.sliceIndexAtPoint(
            tapX = tapX,
            tapY = tapY,
            centerX = layout.centerX,
            centerY = layout.centerY,
            innerRadius = layout.innerTouchRadius,
            outerRadius = layout.labelTouchRadius
        )

        assertEquals(0, index)
        assertEquals("Canopy", BloomCategoryDefinitions.displayNameAtSliceIndex(index!!))
    }

    @Test
    fun sliceIndexAtPoint_rejectsTapOutsideLabelRadius() {
        val layout = BloomLayout.compute(
            canvasWidth = 360f,
            canvasHeight = 350f,
            density = 2f
        )

        val index = BloomTouchMath.sliceIndexAtPoint(
            tapX = layout.centerX,
            tapY = layout.centerY + layout.labelTouchRadius + 40f,
            centerX = layout.centerX,
            centerY = layout.centerY,
            innerRadius = layout.innerTouchRadius,
            outerRadius = layout.labelTouchRadius
        )

        assertNull(index)
    }
}
