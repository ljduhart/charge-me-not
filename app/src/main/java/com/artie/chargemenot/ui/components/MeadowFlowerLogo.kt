package com.artie.chargemenot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun MeadowFlowerLogo(
    modifier: Modifier = Modifier,
    petalColor: Color = Color(0xFF2E7D32),
    centerColor: Color = Color(0xFFE8C547)
) {
    Canvas(modifier = modifier.size(44.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val petalLength = size.minDimension * 0.34f
        val petalWidth = size.minDimension * 0.18f

        repeat(6) { index ->
            rotate(degrees = index * 60f, pivot = center) {
                val petalPath = Path().apply {
                    moveTo(center.x, center.y)
                    cubicTo(
                        center.x - petalWidth,
                        center.y - petalLength * 0.35f,
                        center.x - petalWidth * 0.8f,
                        center.y - petalLength,
                        center.x,
                        center.y - petalLength * 1.15f
                    )
                    cubicTo(
                        center.x + petalWidth * 0.8f,
                        center.y - petalLength,
                        center.x + petalWidth,
                        center.y - petalLength * 0.35f,
                        center.x,
                        center.y
                    )
                    close()
                }
                drawPath(path = petalPath, color = petalColor)
            }
        }

        drawCircle(
            color = centerColor,
            radius = size.minDimension * 0.11f,
            center = center
        )
        drawCircle(
            color = Color(0xFF1B3B22).copy(alpha = 0.25f),
            radius = size.minDimension * 0.05f,
            center = center
        )
    }
}
