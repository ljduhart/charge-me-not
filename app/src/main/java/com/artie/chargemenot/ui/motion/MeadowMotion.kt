package com.artie.chargemenot.ui.motion

import kotlin.math.hypot
import kotlin.math.max

object MeadowMotion {
    const val FINANCIAL_BLOOM = "financial_bloom"
    const val SCANNER_REVEAL_MS = 420
    const val SHARED_BOUNDS_MS = 420
    const val PREDICTIVE_BACK_MS = 280
    const val PREDICTIVE_BACK_MIN_SCALE = 0.86f
    const val PREDICTIVE_BACK_MIN_ALPHA = 0.72f
    const val PREDICTIVE_BACK_MAX_CORNER_DP = 28f

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
}
