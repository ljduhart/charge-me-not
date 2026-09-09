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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val GlasshouseCream = Color(0xFFF9F9F4)
private val GlasshouseBorder = Color.White.copy(alpha = 0.4f)

val GlasshouseForestGreen = Color(0xFF1B3B22)

@Composable
fun GlasshouseCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardShape = RoundedCornerShape(24.dp)
    val supportsNativeBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(width = 1.dp, color = GlasshouseBorder, shape = cardShape)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (supportsNativeBlur) {
                        Modifier.blur(
                            radius = 32.dp,
                            edgeTreatment = BlurredEdgeTreatment.Rectangle
                        )
                    } else {
                        Modifier
                    }
                )
                .background(
                    GlasshouseCream.copy(
                        alpha = if (supportsNativeBlur) 0.55f else 0.72f
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
