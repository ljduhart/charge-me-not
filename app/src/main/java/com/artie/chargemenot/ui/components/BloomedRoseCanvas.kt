package com.artie.chargemenot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artie.chargemenot.R
import kotlin.math.cos
import kotlin.math.sin

private val RosePetalRed = Color(0xFFE53935)
private val RosePetalOrange = Color(0xFFFF6F00)
private val RosePetalCrimson = Color(0xFFC62828)
private val RoseCenterYellow = Color(0xFFFFEB3B)
private val RoseCenterGold = Color(0xFFFFB300)
private val RoseLeafGreen = Color(0xFF2E7D32)
private val RoseStemGreen = Color(0xFF388E3C)

@Composable
fun BloomedRoseAnchor(
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val roseSize = if (large) 88.dp else 64.dp
    val stampFontSize = if (large) 12.sp else 9.sp

    Box(
        modifier = modifier.size(roseSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(roseSize)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val petalCount = 12
            val petalRadiusX = size.width * 0.24f
            val petalRadiusY = size.height * 0.18f

            drawCircle(
                color = Color(0x334DD0E1),
                radius = size.minDimension * 0.48f,
                center = center
            )

            for (index in 0 until petalCount) {
                val angle = index * (360f / petalCount)
                rotate(degrees = angle, pivot = center) {
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(RosePetalOrange, RosePetalRed, RosePetalCrimson),
                            center = Offset(center.x, center.y - petalRadiusY),
                            radius = petalRadiusY * 2.5f
                        ),
                        topLeft = Offset(center.x - petalRadiusX, center.y - petalRadiusY * 1.7f),
                        size = androidx.compose.ui.geometry.Size(petalRadiusX * 2f, petalRadiusY * 2.4f)
                    )
                }
            }

            for (index in 0 until 6) {
                val innerAngle = index * 60f + 15f
                rotate(degrees = innerAngle, pivot = center) {
                    drawOval(
                        color = RosePetalRed.copy(alpha = 0.85f),
                        topLeft = Offset(center.x - petalRadiusX * 0.6f, center.y - petalRadiusY * 1.1f),
                        size = androidx.compose.ui.geometry.Size(petalRadiusX * 1.2f, petalRadiusY * 1.5f)
                    )
                }
            }

            drawCircle(
                color = RoseCenterGold,
                radius = size.minDimension * 0.16f,
                center = center
            )
            drawCircle(
                color = RoseCenterYellow,
                radius = size.minDimension * 0.10f,
                center = center
            )

            rotate(degrees = -30f, pivot = center) {
                drawOval(
                    color = RoseLeafGreen,
                    topLeft = Offset(center.x - size.width * 0.10f, center.y + size.height * 0.10f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.18f, size.height * 0.11f)
                )
            }
            rotate(degrees = 30f, pivot = center) {
                drawOval(
                    color = RoseStemGreen.copy(alpha = 0.9f),
                    topLeft = Offset(center.x - size.width * 0.02f, center.y + size.height * 0.14f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.16f, size.height * 0.10f)
                )
            }

            drawLine(
                color = RoseStemGreen,
                start = Offset(center.x, center.y + size.height * 0.18f),
                end = Offset(center.x, center.y + size.height * 0.42f),
                strokeWidth = 3f
            )

            for (index in 0 until 3) {
                val thornAngle = 155f + index * 20f
                val radians = Math.toRadians(thornAngle.toDouble())
                val thornX = center.x + cos(radians).toFloat() * size.width * 0.05f
                val thornY = center.y + size.height * 0.28f + sin(radians).toFloat() * size.height * 0.06f
                drawCircle(
                    color = RoseLeafGreen.copy(alpha = 0.8f),
                    radius = 2.5f,
                    center = Offset(thornX, thornY)
                )
            }
        }

        Box(
            modifier = Modifier
                .background(
                    color = Color.White.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.petals_and_weeds_paid_stamp),
                color = Color(0xFF5D4037),
                fontSize = stampFontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
