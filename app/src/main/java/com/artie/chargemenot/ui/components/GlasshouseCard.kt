package com.artie.chargemenot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
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
        Image(
            painter = painterResource(R.drawable.bg_greenhouse),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    translationX = backgroundTranslationX
                    translationY = backgroundTranslationY
                }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(radius = 16.dp)
                .background(GlasshouseCream.copy(alpha = 0.6f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            content = content
        )
    }
}
