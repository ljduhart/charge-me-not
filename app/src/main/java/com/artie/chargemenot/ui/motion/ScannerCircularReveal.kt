package com.artie.chargemenot.ui.motion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import com.artie.chargemenot.ui.theme.MeadowSky

data class ScannerFabOrigin(
    val windowOffset: Offset,
    val radiusPx: Float
)

@Composable
fun ScannerCircularReveal(
    visible: Boolean,
    origin: ScannerFabOrigin?,
    onExpanded: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MeadowSky
) {
    val transition = updateTransition(targetState = visible, label = "scanner_circular_reveal")
    val progress by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                tween(
                    durationMillis = MeadowMotion.SCANNER_REVEAL_MS,
                    easing = FastOutSlowInEasing
                )
            } else {
                tween(durationMillis = 180, easing = FastOutSlowInEasing)
            }
        },
        label = "scanner_reveal_progress"
    ) { targetVisible ->
        if (targetVisible) 1f else 0f
    }
    val overlayAlpha by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                tween(durationMillis = 80)
            } else {
                tween(durationMillis = 180)
            }
        },
        label = "scanner_reveal_alpha"
    ) { targetVisible ->
        if (targetVisible) 1f else 0f
    }

    var expansionReported by remember { mutableStateOf(false) }
    LaunchedEffect(visible, progress) {
        if (visible && progress >= 0.995f && !expansionReported) {
            expansionReported = true
            onExpanded()
        }
        if (!visible) {
            expansionReported = false
        }
    }

    if (progress <= 0f && overlayAlpha <= 0f) {
        return
    }

    var canvasWindowOrigin by remember { mutableStateOf(Offset.Zero) }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                canvasWindowOrigin = coordinates.positionInWindow()
            }
    ) {
        val fabOrigin = origin ?: ScannerFabOrigin(
            windowOffset = Offset(size.width / 2f, size.height - size.minDimension * 0.12f),
            radiusPx = 32f
        )
        val localOrigin = Offset(
            x = fabOrigin.windowOffset.x - canvasWindowOrigin.x,
            y = fabOrigin.windowOffset.y - canvasWindowOrigin.y
        )
        val maxRadius = MeadowMotion.maxCircularRevealRadius(
            originX = localOrigin.x,
            originY = localOrigin.y,
            width = size.width,
            height = size.height
        )
        val radius = MeadowMotion.circularRevealRadius(
            progress = progress,
            startRadius = fabOrigin.radiusPx,
            maxRadius = maxRadius
        )
        drawCircle(
            color = color.copy(alpha = overlayAlpha),
            radius = radius,
            center = localOrigin
        )
    }
}
