package com.artie.chargemenot.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.ui.theme.MeadowEarth
import com.artie.chargemenot.ui.theme.MeadowEarthLight
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowSage

@Composable
fun RootSystemCanvas(
    parentBill: BillEntity,
    childBills: List<BillEntity>,
    isParentPruned: Boolean,
    prunedBillIds: Set<Long>,
    modifier: Modifier = Modifier
) {
    val childCount = childBills.size.coerceAtLeast(1)
    val canvasHeight = (72 + childCount * 36).dp

    val vitality by animateFloatAsState(
        targetValue = if (isParentPruned) 0.12f else 1f,
        animationSpec = tween(durationMillis = 450),
        label = "rootVitality_${parentBill.id}"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "rootGlow_${parentBill.id}")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rootGlowPulse_${parentBill.id}"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(canvasHeight)
    ) {
        if (childBills.isEmpty()) {
            return@Canvas
        }

        val parentX = size.width / 2f
        val parentY = 28f
        val childSpacing = (size.width - 48f) / childBills.size.coerceAtLeast(1)
        val childY = size.height - 28f

        childBills.forEachIndexed { index, child ->
            val childX = 24f + childSpacing * index + childSpacing / 2f
            val childPruned = child.id in prunedBillIds || isParentPruned
            val branchVitality = if (childPruned) vitality * 0.5f else vitality

            val controlOffsetY = (childY - parentY) * 0.45f
            val sway = if (isParentPruned) 0f else glowPulse * 8f

            val path = Path().apply {
                moveTo(parentX, parentY)
                cubicTo(
                    parentX - sway,
                    parentY + controlOffsetY,
                    childX + sway,
                    childY - controlOffsetY,
                    childX,
                    childY
                )
            }

            val rootColor = if (childPruned) {
                MeadowEarth.copy(alpha = 0.25f * branchVitality)
            } else {
                MeadowGreenDark.copy(alpha = (0.55f + glowPulse * 0.25f) * branchVitality)
            }

            val glowColor = MeadowEarthLight.copy(alpha = glowPulse * 0.35f * branchVitality)

            drawPath(
                path = path,
                color = glowColor,
                style = Stroke(width = 10f, cap = StrokeCap.Round)
            )

            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MeadowEarth.copy(alpha = 0.85f * branchVitality),
                        MeadowGreenDark.copy(alpha = 0.75f * branchVitality),
                        MeadowSage.copy(alpha = 0.65f * branchVitality)
                    ),
                    start = Offset(parentX, parentY),
                    end = Offset(childX, childY)
                ),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            drawCircle(
                color = rootColor,
                radius = 10f * branchVitality.coerceAtLeast(0.4f),
                center = Offset(parentX, parentY)
            )

            drawCircle(
                color = if (childPruned) {
                    Color.Gray.copy(alpha = 0.35f * branchVitality)
                } else {
                    MeadowSage.copy(alpha = 0.9f * branchVitality)
                },
                radius = 8f * branchVitality.coerceAtLeast(0.35f),
                center = Offset(childX, childY)
            )
        }
    }
}
