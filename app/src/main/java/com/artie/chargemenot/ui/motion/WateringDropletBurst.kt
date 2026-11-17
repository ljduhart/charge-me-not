package com.artie.chargemenot.ui.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.artie.chargemenot.ui.theme.MeadowSky

private data class WateringDropletSeed(
    val x: Float,
    val startY: Float,
    val travel: Float,
    val radius: Float,
    val delay: Float,
    val alpha: Float
)

private val WateringDropletSeeds = listOf(
    WateringDropletSeed(x = 0.18f, startY = 0.08f, travel = 0.72f, radius = 0.045f, delay = 0.00f, alpha = 0.95f),
    WateringDropletSeed(x = 0.32f, startY = 0.00f, travel = 0.80f, radius = 0.038f, delay = 0.08f, alpha = 0.80f),
    WateringDropletSeed(x = 0.46f, startY = 0.12f, travel = 0.68f, radius = 0.050f, delay = 0.04f, alpha = 1.00f),
    WateringDropletSeed(x = 0.58f, startY = 0.02f, travel = 0.78f, radius = 0.034f, delay = 0.14f, alpha = 0.78f),
    WateringDropletSeed(x = 0.70f, startY = 0.10f, travel = 0.70f, radius = 0.042f, delay = 0.06f, alpha = 0.90f),
    WateringDropletSeed(x = 0.82f, startY = 0.04f, travel = 0.74f, radius = 0.036f, delay = 0.18f, alpha = 0.72f),
    WateringDropletSeed(x = 0.26f, startY = 0.18f, travel = 0.62f, radius = 0.028f, delay = 0.22f, alpha = 0.66f),
    WateringDropletSeed(x = 0.64f, startY = 0.16f, travel = 0.64f, radius = 0.030f, delay = 0.12f, alpha = 0.70f)
)

private val DropletCyan = Color(0xFF4FC3F7)

@Composable
fun WateringDropletBurst(
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(active) {
        if (!active) {
            progress.snapTo(0f)
            return@LaunchedEffect
        }
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = MeadowMotion.WATERING_BURST_MS,
                easing = FastOutSlowInEasing
            )
        )
    }

    if (!active && progress.value <= 0f) {
        return
    }

    Canvas(modifier = modifier) {
        val overall = progress.value
        WateringDropletSeeds.forEach { seed ->
            val dropletProgress = MeadowMotion.wateringDropletProgress(overall, seed.delay)
            if (dropletProgress <= 0f) {
                return@forEach
            }
            val radius = size.minDimension * seed.radius
            val center = Offset(
                x = size.width * seed.x,
                y = MeadowMotion.wateringDropletY(
                    progress = dropletProgress,
                    startY = size.height * seed.startY,
                    travel = size.height * seed.travel
                )
            )
            val alpha = MeadowMotion.wateringDropletAlpha(dropletProgress) * seed.alpha
            drawCircle(
                color = DropletCyan.copy(alpha = alpha),
                radius = radius,
                center = center
            )
            drawCircle(
                color = MeadowSky.copy(alpha = alpha * 0.55f),
                radius = radius * 0.55f,
                center = Offset(center.x - radius * 0.18f, center.y - radius * 0.22f)
            )
        }
    }
}
