package com.artie.chargemenot.ui.motion

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

object MeadowMotion {
    const val FINANCIAL_BLOOM = "financial_bloom"
    const val SCANNER_REVEAL_MS = 420
    const val SHARED_BOUNDS_MS = 420
    const val PREDICTIVE_BACK_MS = 280
    const val PREDICTIVE_BACK_MIN_SCALE = 0.86f
    const val PREDICTIVE_BACK_MIN_ALPHA = 0.72f
    const val PREDICTIVE_BACK_MAX_CORNER_DP = 28f
    const val PAID_ROSE_CROSSFADE_MS = 420
    const val WATERING_BURST_MS = 520
    const val GARDEN_PATH_EDGE_FADE_DP = 160f
    const val GARDEN_PATH_EDGE_MIN_SCALE = 0.92f
    const val GARDEN_PATH_EDGE_MIN_ALPHA = 0.42f
    const val WEED_WHACKER_SWIPE_MAX_ROTATION = 16f
    const val WEED_WHACKER_STACK_SCALE_STEP = 0.06f
    const val WEED_WHACKER_STACK_MIN_SCALE = 0.86f

    fun circularRevealRadius(
        progress: Float,
        startRadius: Float,
        maxRadius: Float
    ): Float {
        val clampedProgress = progress.coerceIn(0f, 1f)
        val safeStart = startRadius.coerceAtLeast(0f)
        val safeMax = maxRadius.coerceAtLeast(safeStart)
        return safeStart + (safeMax - safeStart) * clampedProgress
    }

    fun maxCircularRevealRadius(
        originX: Float,
        originY: Float,
        width: Float,
        height: Float
    ): Float {
        val dx = max(originX, width - originX)
        val dy = max(originY, height - originY)
        return hypot(dx, dy)
    }

    fun predictiveBackScale(progress: Float): Float {
        val clampedProgress = progress.coerceIn(0f, 1f)
        return 1f - ((1f - PREDICTIVE_BACK_MIN_SCALE) * clampedProgress)
    }

    fun predictiveBackAlpha(progress: Float): Float {
        val clampedProgress = progress.coerceIn(0f, 1f)
        return 1f - ((1f - PREDICTIVE_BACK_MIN_ALPHA) * clampedProgress)
    }

    fun predictiveBackCornerDp(progress: Float): Float {
        return PREDICTIVE_BACK_MAX_CORNER_DP * progress.coerceIn(0f, 1f)
    }

    fun gardenPathEdgeProgress(
        itemOffset: Int,
        itemSize: Int,
        viewportStart: Int,
        viewportEnd: Int,
        fadeDistance: Float = GARDEN_PATH_EDGE_FADE_DP
    ): Float {
        if (itemSize <= 0) {
            return 1f
        }
        val safeFade = fadeDistance.coerceAtLeast(1f)
        val itemCenter = itemOffset + itemSize / 2f
        val fromTop = ((itemCenter - viewportStart) / safeFade).coerceIn(0f, 1f)
        val fromBottom = ((viewportEnd - itemCenter) / safeFade).coerceIn(0f, 1f)
        return min(fromTop, fromBottom)
    }

    fun gardenPathEdgeScale(progress: Float): Float {
        val clampedProgress = progress.coerceIn(0f, 1f)
        return GARDEN_PATH_EDGE_MIN_SCALE +
            ((1f - GARDEN_PATH_EDGE_MIN_SCALE) * clampedProgress)
    }

    fun gardenPathEdgeAlpha(progress: Float): Float {
        val clampedProgress = progress.coerceIn(0f, 1f)
        return GARDEN_PATH_EDGE_MIN_ALPHA +
            ((1f - GARDEN_PATH_EDGE_MIN_ALPHA) * clampedProgress)
    }

    fun wateringDropletY(progress: Float, startY: Float, travel: Float): Float {
        return startY + (travel * progress.coerceIn(0f, 1f))
    }

    fun wateringDropletAlpha(progress: Float): Float {
        val clampedProgress = progress.coerceIn(0f, 1f)
        return if (clampedProgress < 0.7f) {
            1f
        } else {
            (1f - ((clampedProgress - 0.7f) / 0.3f)).coerceIn(0f, 1f)
        }
    }

    fun wateringDropletProgress(overall: Float, delay: Float): Float {
        val safeDelay = delay.coerceIn(0f, 0.9f)
        val span = (1f - safeDelay).coerceAtLeast(0.01f)
        return ((overall.coerceIn(0f, 1f) - safeDelay) / span).coerceIn(0f, 1f)
    }

    fun weedWhackerSwipeRotation(offsetX: Float): Float {
        return (offsetX / 22f).coerceIn(
            -WEED_WHACKER_SWIPE_MAX_ROTATION,
            WEED_WHACKER_SWIPE_MAX_ROTATION
        )
    }

    fun weedWhackerSwipeDecision(offsetX: Float, threshold: Float): Boolean? {
        val safeThreshold = threshold.coerceAtLeast(1f)
        return when {
            offsetX >= safeThreshold -> true
            offsetX <= -safeThreshold -> false
            else -> null
        }
    }

    fun weedWhackerSwipeTintAlpha(offsetX: Float, threshold: Float): Float {
        val safeThreshold = threshold.coerceAtLeast(1f)
        return (abs(offsetX) / safeThreshold).coerceIn(0f, 0.42f)
    }

    fun weedWhackerStackScale(depth: Int): Float {
        val safeDepth = depth.coerceAtLeast(0)
        return (1f - (safeDepth * WEED_WHACKER_STACK_SCALE_STEP))
            .coerceAtLeast(WEED_WHACKER_STACK_MIN_SCALE)
    }

    fun weedWhackerStackOffsetY(depth: Int, spacing: Float): Float {
        return depth.coerceAtLeast(0) * spacing.coerceAtLeast(0f)
    }
}
