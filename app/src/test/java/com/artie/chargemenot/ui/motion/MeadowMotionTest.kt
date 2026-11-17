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

    @Test
    fun gardenPathEdgeProgress_isFullInCenterAndFadesAtTheEdges() {
        assertEquals(
            1f,
            MeadowMotion.gardenPathEdgeProgress(
                itemOffset = 300,
                itemSize = 200,
                viewportStart = 0,
                viewportEnd = 800,
                fadeDistance = 160f
            ),
            0.001f
        )
        assertEquals(
            0.625f,
            MeadowMotion.gardenPathEdgeProgress(
                itemOffset = 0,
                itemSize = 200,
                viewportStart = 0,
                viewportEnd = 800,
                fadeDistance = 160f
            ),
            0.001f
        )
        assertEquals(
            0f,
            MeadowMotion.gardenPathEdgeProgress(
                itemOffset = 700,
                itemSize = 200,
                viewportStart = 0,
                viewportEnd = 800,
                fadeDistance = 160f
            ),
            0.001f
        )
    }

    @Test
    fun gardenPathEdgeScaleAndAlpha_easeFromTheMinimumAtTheRim() {
        assertEquals(MeadowMotion.GARDEN_PATH_EDGE_MIN_SCALE, MeadowMotion.gardenPathEdgeScale(0f), 0.001f)
        assertEquals(1f, MeadowMotion.gardenPathEdgeScale(1f), 0.001f)
        assertEquals(MeadowMotion.GARDEN_PATH_EDGE_MIN_ALPHA, MeadowMotion.gardenPathEdgeAlpha(0f), 0.001f)
        assertEquals(1f, MeadowMotion.gardenPathEdgeAlpha(1f), 0.001f)
        val midScale = MeadowMotion.gardenPathEdgeScale(0.5f)
        assertTrue(midScale > MeadowMotion.GARDEN_PATH_EDGE_MIN_SCALE && midScale < 1f)
    }

    @Test
    fun wateringDroplets_fallAndFadeOut() {
        assertEquals(10f, MeadowMotion.wateringDropletY(0f, 10f, 80f), 0.001f)
        assertEquals(90f, MeadowMotion.wateringDropletY(1f, 10f, 80f), 0.001f)
        assertEquals(1f, MeadowMotion.wateringDropletAlpha(0.2f), 0.001f)
        assertEquals(0f, MeadowMotion.wateringDropletAlpha(1f), 0.001f)
        assertEquals(0f, MeadowMotion.wateringDropletProgress(0.1f, 0.2f), 0.001f)
        assertEquals(1f, MeadowMotion.wateringDropletProgress(1f, 0.2f), 0.001f)
    }

    @Test
    fun weedWhackerSwipe_decidesYesNoAndStackDepth() {
        assertEquals(true, MeadowMotion.weedWhackerSwipeDecision(140f, 120f))
        assertEquals(false, MeadowMotion.weedWhackerSwipeDecision(-140f, 120f))
        assertEquals(null, MeadowMotion.weedWhackerSwipeDecision(40f, 120f))
        assertEquals(0f, MeadowMotion.weedWhackerSwipeRotation(0f), 0.001f)
        assertEquals(
            MeadowMotion.WEED_WHACKER_SWIPE_MAX_ROTATION,
            MeadowMotion.weedWhackerSwipeRotation(400f),
            0.001f
        )
        assertEquals(1f, MeadowMotion.weedWhackerStackScale(0), 0.001f)
        assertEquals(0.94f, MeadowMotion.weedWhackerStackScale(1), 0.001f)
        assertEquals(28f, MeadowMotion.weedWhackerStackOffsetY(2, 14f), 0.001f)
        assertTrue(MeadowMotion.weedWhackerSwipeTintAlpha(60f, 120f) in 0f..0.42f)
    }
}
