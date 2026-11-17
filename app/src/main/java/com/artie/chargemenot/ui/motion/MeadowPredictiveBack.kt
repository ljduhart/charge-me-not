package com.artie.chargemenot.ui.motion

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException

@Composable
fun MeadowPredictiveBackContainer(
    enabled: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var backProgress by remember { mutableFloatStateOf(0f) }

    PredictiveBackHandler(enabled = enabled) { backEvents ->
        try {
            backEvents.collect { backEvent ->
                backProgress = backEvent.progress
            }
            backProgress = 1f
            onBack()
        } catch (cancelled: CancellationException) {
            backProgress = 0f
            throw cancelled
        }
    }

    val scale = MeadowMotion.predictiveBackScale(backProgress)
    val alpha = MeadowMotion.predictiveBackAlpha(backProgress)
    val cornerDp = MeadowMotion.predictiveBackCornerDp(backProgress)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                transformOrigin = TransformOrigin.Center
                clip = backProgress > 0f
                shape = RoundedCornerShape(cornerDp.dp)
            }
            .then(
                if (backProgress > 0f) {
                    Modifier.clip(RoundedCornerShape(cornerDp.dp))
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}
