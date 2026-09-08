package com.artie.chargemenot.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}
