package com.artie.chargemenot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import java.time.Month

enum class MonthlyMeadowFlower(
    val month: Month,
    val displayName: String,
    val primaryColor: Color,
    val accentColor: Color
) {
    JANUARY_SNOWDROP(Month.JANUARY, "Snowdrop", Color(0xFFE8F5E9), Color(0xFF81C784)),
    FEBRUARY_VIOLET(Month.FEBRUARY, "Violet", Color(0xFFCE93D8), Color(0xFF7B1FA2)),
    MARCH_DAFFODIL(Month.MARCH, "Daffodil", Color(0xFFFFF176), Color(0xFFF9A825)),
    APRIL_DAISY(Month.APRIL, "Daisy", Color(0xFFFFFDE7), Color(0xFFFFB300)),
    MAY_ROSE(Month.MAY, "Rose", Color(0xFFE57373), Color(0xFFC62828)),
    JUNE_TULIP(Month.JUNE, "Tulip", Color(0xFFF48FB1), Color(0xFFD81B60)),
    JULY_DANDELION(Month.JULY, "Dandelion", Color(0xFFFFF59D), Color(0xFFFBC02D)),
    AUGUST_SUNFLOWER(Month.AUGUST, "Sunflower", Color(0xFFFFD54F), Color(0xFF6D4C41)),
    SEPTEMBER_ASTER(Month.SEPTEMBER, "Aster", Color(0xFFB39DDB), Color(0xFF512DA8)),
    OCTOBER_MARIGOLD(Month.OCTOBER, "Marigold", Color(0xFFFFB74D), Color(0xFFE65100)),
    NOVEMBER_CHRYSANTHEMUM(Month.NOVEMBER, "Chrysanthemum", Color(0xFFFFAB91), Color(0xFFBF360C)),
    DECEMBER_POINSETTIA(Month.DECEMBER, "Poinsettia", Color(0xFFEF5350), Color(0xFF1B5E20));

    companion object {
        fun forMonth(month: Month): MonthlyMeadowFlower {
            return entries.first { flower -> flower.month == month }
        }
    }
}

@Composable
fun MonthlyMeadowFlowerWatermark(
    month: Month,
    modifier: Modifier = Modifier,
    alpha: Float = 0.18f
) {
    val flower = MonthlyMeadowFlower.forMonth(month)
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width * 0.72f, size.height * 0.42f)
        val scale = size.minDimension * 0.42f
        drawMonthlyFlower(
            flower = flower,
            center = center,
            scale = scale,
            alpha = alpha
        )
    }
}

private fun DrawScope.drawMonthlyFlower(
    flower: MonthlyMeadowFlower,
    center: Offset,
    scale: Float,
    alpha: Float
) {
    when (flower) {
        MonthlyMeadowFlower.JANUARY_SNOWDROP -> drawSnowdrop(center, scale, flower, alpha)
        MonthlyMeadowFlower.FEBRUARY_VIOLET -> drawRadialPetalFlower(center, scale, flower, alpha, petalCount = 5)
        MonthlyMeadowFlower.MARCH_DAFFODIL -> drawDaffodil(center, scale, flower, alpha)
        MonthlyMeadowFlower.APRIL_DAISY -> drawRadialPetalFlower(center, scale, flower, alpha, petalCount = 12)
        MonthlyMeadowFlower.MAY_ROSE -> drawRose(center, scale, flower, alpha)
        MonthlyMeadowFlower.JUNE_TULIP -> drawTulip(center, scale, flower, alpha)
        MonthlyMeadowFlower.JULY_DANDELION -> drawDandelion(center, scale, flower, alpha)
        MonthlyMeadowFlower.AUGUST_SUNFLOWER -> drawSunflower(center, scale, flower, alpha)
        MonthlyMeadowFlower.SEPTEMBER_ASTER -> drawRadialPetalFlower(center, scale, flower, alpha, petalCount = 16)
        MonthlyMeadowFlower.OCTOBER_MARIGOLD -> drawRadialPetalFlower(center, scale, flower, alpha, petalCount = 8)
        MonthlyMeadowFlower.NOVEMBER_CHRYSANTHEMUM -> drawRadialPetalFlower(center, scale, flower, alpha, petalCount = 20)
        MonthlyMeadowFlower.DECEMBER_POINSETTIA -> drawPoinsettia(center, scale, flower, alpha)
    }
}

private fun DrawScope.drawRadialPetalFlower(
    center: Offset,
    scale: Float,
    flower: MonthlyMeadowFlower,
    alpha: Float,
    petalCount: Int
) {
    repeat(petalCount) { index ->
        rotate(degrees = index * (360f / petalCount), pivot = center) {
            val petalPath = Path().apply {
                moveTo(center.x, center.y)
                cubicTo(
                    center.x - scale * 0.08f,
                    center.y - scale * 0.18f,
                    center.x - scale * 0.06f,
                    center.y - scale * 0.34f,
                    center.x,
                    center.y - scale * 0.38f
                )
                cubicTo(
                    center.x + scale * 0.06f,
                    center.y - scale * 0.34f,
                    center.x + scale * 0.08f,
                    center.y - scale * 0.18f,
                    center.x,
                    center.y
                )
                close()
            }
            drawPath(petalPath, flower.primaryColor.copy(alpha = alpha))
        }
    }
    drawCircle(flower.accentColor.copy(alpha = alpha * 1.2f), scale * 0.08f, center)
}

private fun DrawScope.drawSnowdrop(
    center: Offset,
    scale: Float,
    flower: MonthlyMeadowFlower,
    alpha: Float
) {
    repeat(3) { index ->
        rotate(degrees = index * 120f - 30f, pivot = center) {
            val petalPath = Path().apply {
                moveTo(center.x, center.y + scale * 0.05f)
                quadraticTo(
                    center.x - scale * 0.12f,
                    center.y - scale * 0.2f,
                    center.x,
                    center.y - scale * 0.42f
                )
                quadraticTo(
                    center.x + scale * 0.12f,
                    center.y - scale * 0.2f,
                    center.x,
                    center.y + scale * 0.05f
                )
                close()
            }
            drawPath(petalPath, flower.primaryColor.copy(alpha = alpha))
        }
    }
    drawCircle(flower.accentColor.copy(alpha = alpha), scale * 0.05f, center)
}

private fun DrawScope.drawDaffodil(
    center: Offset,
    scale: Float,
    flower: MonthlyMeadowFlower,
    alpha: Float
) {
    drawRadialPetalFlower(center, scale * 0.9f, flower, alpha * 0.9f, petalCount = 6)
    drawCircle(flower.accentColor.copy(alpha = alpha * 1.3f), scale * 0.14f, center)
}

private fun DrawScope.drawRose(
    center: Offset,
    scale: Float,
    flower: MonthlyMeadowFlower,
    alpha: Float
) {
    repeat(3) { ring ->
        val ringScale = scale * (0.85f - ring * 0.12f)
        repeat(6) { index ->
            rotate(degrees = index * 60f + ring * 18f, pivot = center) {
                val petalPath = Path().apply {
                    moveTo(center.x, center.y)
                    cubicTo(
                        center.x - ringScale * 0.1f,
                        center.y - ringScale * 0.15f,
                        center.x - ringScale * 0.08f,
                        center.y - ringScale * 0.28f,
                        center.x,
                        center.y - ringScale * 0.32f
                    )
                    cubicTo(
                        center.x + ringScale * 0.08f,
                        center.y - ringScale * 0.28f,
                        center.x + ringScale * 0.1f,
                        center.y - ringScale * 0.15f,
                        center.x,
                        center.y
                    )
                    close()
                }
                drawPath(petalPath, flower.primaryColor.copy(alpha = alpha * (1f - ring * 0.15f)))
            }
        }
    }
    drawCircle(flower.accentColor.copy(alpha = alpha), scale * 0.06f, center)
}

private fun DrawScope.drawTulip(
    center: Offset,
    scale: Float,
    flower: MonthlyMeadowFlower,
    alpha: Float
) {
    repeat(3) { index ->
        rotate(degrees = index * 120f, pivot = center) {
            val petalPath = Path().apply {
                moveTo(center.x, center.y + scale * 0.08f)
                cubicTo(
                    center.x - scale * 0.16f,
                    center.y - scale * 0.05f,
                    center.x - scale * 0.12f,
                    center.y - scale * 0.36f,
                    center.x,
                    center.y - scale * 0.42f
                )
                cubicTo(
                    center.x + scale * 0.12f,
                    center.y - scale * 0.36f,
                    center.x + scale * 0.16f,
                    center.y - scale * 0.05f,
                    center.x,
                    center.y + scale * 0.08f
                )
                close()
            }
            drawPath(petalPath, flower.primaryColor.copy(alpha = alpha))
        }
    }
    drawLine(
        color = flower.accentColor.copy(alpha = alpha * 1.2f),
        start = Offset(center.x, center.y + scale * 0.08f),
        end = Offset(center.x, center.y + scale * 0.35f),
        strokeWidth = scale * 0.04f
    )
}

private fun DrawScope.drawDandelion(
    center: Offset,
    scale: Float,
    flower: MonthlyMeadowFlower,
    alpha: Float
) {
    repeat(24) { index ->
        rotate(degrees = index * 15f, pivot = center) {
            drawLine(
                color = flower.primaryColor.copy(alpha = alpha),
                start = center,
                end = Offset(center.x, center.y - scale * 0.38f),
                strokeWidth = scale * 0.018f
            )
        }
    }
    drawCircle(flower.accentColor.copy(alpha = alpha * 1.4f), scale * 0.1f, center)
}

private fun DrawScope.drawSunflower(
    center: Offset,
    scale: Float,
    flower: MonthlyMeadowFlower,
    alpha: Float
) {
    drawRadialPetalFlower(center, scale, flower, alpha, petalCount = 14)
    drawCircle(flower.accentColor.copy(alpha = alpha * 1.4f), scale * 0.16f, center)
}

private fun DrawScope.drawPoinsettia(
    center: Offset,
    scale: Float,
    flower: MonthlyMeadowFlower,
    alpha: Float
) {
    repeat(8) { index ->
        rotate(degrees = index * 45f, pivot = center) {
            val leafPath = Path().apply {
                moveTo(center.x, center.y)
                cubicTo(
                    center.x - scale * 0.14f,
                    center.y - scale * 0.08f,
                    center.x - scale * 0.1f,
                    center.y - scale * 0.32f,
                    center.x,
                    center.y - scale * 0.36f
                )
                cubicTo(
                    center.x + scale * 0.1f,
                    center.y - scale * 0.32f,
                    center.x + scale * 0.14f,
                    center.y - scale * 0.08f,
                    center.x,
                    center.y
                )
                close()
            }
            drawPath(leafPath, flower.primaryColor.copy(alpha = alpha))
        }
    }
    drawCircle(flower.accentColor.copy(alpha = alpha * 1.5f), scale * 0.07f, center)
}
