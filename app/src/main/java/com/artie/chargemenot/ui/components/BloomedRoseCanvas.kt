package com.artie.chargemenot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artie.chargemenot.R
import kotlin.math.cos
import kotlin.math.sin

private val RosePetalRed = Color(0xFFD32F2F)
private val RosePetalCrimson = Color(0xFFB71C1C)
private val RoseCenterYellow = Color(0xFFFFD54F)
private val RoseCenterGold = Color(0xFFFFB300)
private val RoseLeafGreen = Color(0xFF388E3C)

@Composable
fun BloomedRoseAnchor(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(58.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(58.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val petalCount = 10
            val petalRadiusX = size.width * 0.22f
            val petalRadiusY = size.height * 0.16f

            for (index in 0 until petalCount) {
                val angle = index * (360f / petalCount)
                rotate(degrees = angle, pivot = center) {
                    drawOval(
                        color = if (index % 2 == 0) RosePetalRed else RosePetalCrimson,
                        topLeft = Offset(center.x - petalRadiusX, center.y - petalRadiusY * 1.6f),
                        size = androidx.compose.ui.geometry.Size(petalRadiusX * 2f, petalRadiusY * 2.2f)
                    )
                }
            }

            drawCircle(
                color = RoseCenterGold,
                radius = size.minDimension * 0.14f,
                center = center
            )
            drawCircle(
                color = RoseCenterYellow,
                radius = size.minDimension * 0.09f,
                center = center
            )

            val leafAngle = -35f
            rotate(degrees = leafAngle, pivot = center) {
                drawOval(
                    color = RoseLeafGreen,
                    topLeft = Offset(center.x - size.width * 0.08f, center.y + size.height * 0.08f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.16f, size.height * 0.10f)
                )
            }
            rotate(degrees = -leafAngle, pivot = center) {
                drawOval(
                    color = RoseLeafGreen.copy(alpha = 0.85f),
                    topLeft = Offset(center.x - size.width * 0.04f, center.y + size.height * 0.12f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.14f, size.height * 0.09f)
                )
            }

            val thornCount = 4
            for (index in 0 until thornCount) {
                val thornAngle = 140f + index * 18f
                val radians = Math.toRadians(thornAngle.toDouble())
                val stemX = center.x + cos(radians).toFloat() * size.width * 0.06f
                val stemY = center.y + sin(radians).toFloat() * size.height * 0.22f
                drawCircle(
                    color = RoseLeafGreen.copy(alpha = 0.7f),
                    radius = 2f,
                    center = Offset(stemX, stemY)
                )
            }
        }

        Text(
            text = stringResource(R.string.petals_and_weeds_paid_stamp),
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
