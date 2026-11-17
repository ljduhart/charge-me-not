package com.artie.chargemenot.ui.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MeadowMotionTest {

    @Test
    fun circularRevealRadius_startsAndEndsOnTheFabAndScreen() {
        assertEquals(32f, MeadowMotion.circularRevealRadius(0f, 32f, 800f), 0.001f)
        assertEquals(800f, MeadowMotion.circularRevealRadius(1f, 32f, 800f), 0.001f)
        assertEquals(416f, MeadowMotion.circularRevealRadius(0.5f, 32f, 800f), 0.001f)
    }

    @Test
    fun circularRevealRadius_clampsProgressAndNonPositiveStart() {
        assertEquals(32f, MeadowMotion.circularRevealRadius(-2f, 32f, 800f), 0.001f)
        assertEquals(800f, MeadowMotion.circularRevealRadius(4f, 32f, 800f), 0.001f)
        assertEquals(0f, MeadowMotion.circularRevealRadius(0f, -8f, 100f), 0.001f)
    }

    @Test
    fun maxCircularRevealRadius_coversTheFarthestCorner() {
        val fromBottomCenter = MeadowMotion.maxCircularRevealRadius(
            originX = 180f,
            originY = 640f,
            width = 360f,
            height = 640f
        )
        assertEquals(
            kotlin.math.hypot(180.0, 640.0).toFloat(),
            fromBottomCenter,
            0.001f
        )

        val fromCorner = MeadowMotion.maxCircularRevealRadius(
            originX = 0f,
            originY = 0f,
            width = 3f,
            height = 4f
        )
        assertEquals(5f, fromCorner, 0.001f)
    }

    @Test
    fun predictiveBackScaleAndAlpha_easeTowardTheSettledFrame() {
        assertEquals(1f, MeadowMotion.predictiveBackScale(0f), 0.001f)
        assertEquals(MeadowMotion.PREDICTIVE_BACK_MIN_SCALE, MeadowMotion.predictiveBackScale(1f), 0.001f)
        assertEquals(1f, MeadowMotion.predictiveBackAlpha(0f), 0.001f)
        assertEquals(MeadowMotion.PREDICTIVE_BACK_MIN_ALPHA, MeadowMotion.predictiveBackAlpha(1f), 0.001f)
        assertEquals(0f, MeadowMotion.predictiveBackCornerDp(0f), 0.001f)
        assertEquals(
            MeadowMotion.PREDICTIVE_BACK_MAX_CORNER_DP,
            MeadowMotion.predictiveBackCornerDp(1f),
            0.001f
        )
        val midScale = MeadowMotion.predictiveBackScale(0.5f)
        assertTrue(midScale < 1f && midScale > MeadowMotion.PREDICTIVE_BACK_MIN_SCALE)
    }

    @Test
    fun sharedElementKey_staysStableForBloomMorph() {
        assertEquals("financial_bloom", MeadowMotion.FINANCIAL_BLOOM)
    }
}
