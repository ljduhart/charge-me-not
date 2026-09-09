package com.artie.chargemenot.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R

private const val BACKGROUND_PARALLAX_FACTOR = 0.5f
private val GlasshouseCream = Color(0xFFF9F9F4)

@Composable
fun GlasshouseCard(
    parallaxOffset: Pair<Float, Float>,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val (tiltX, tiltY) = parallaxOffset
    val parallaxDistancePx = with(LocalDensity.current) { 12.dp.toPx() }
    val backgroundTranslationX = tiltX * BACKGROUND_PARALLAX_FACTOR * parallaxDistancePx
    val backgroundTranslationY = tiltY * BACKGROUND_PARALLAX_FACTOR * parallaxDistancePx
    val cardShape = RoundedCornerShape(24.dp)
    val forestGreen = colorResource(R.color.greenhouse_forest)
    val midGreen = colorResource(R.color.greenhouse_mid)
    val lightGreen = colorResource(R.color.greenhouse_light)
    val greenhouseBrush = remember(forestGreen, midGreen, lightGreen) {
        Brush.linearGradient(
            colorStops = arrayOf(
                0f to forestGreen,
                0.5f to midGreen,
                1f to lightGreen
            ),
            start = Offset.Zero,
            end = Offset(400f, 400f)
        )
    }
    val supportsNativeBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(
                width = 1.dp,
                color = GlasshouseCream.copy(alpha = 0.92f),
                shape = cardShape
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    translationX = backgroundTranslationX
                    translationY = backgroundTranslationY
                }
                .background(greenhouseBrush)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (supportsNativeBlur) {
                        Modifier.blur(radius = 16.dp)
                    } else {
                        Modifier
                    }
                )
                .background(
                    GlasshouseCream.copy(
                        alpha = if (supportsNativeBlur) 0.6f else 0.72f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            content = content
        )
    }
}
