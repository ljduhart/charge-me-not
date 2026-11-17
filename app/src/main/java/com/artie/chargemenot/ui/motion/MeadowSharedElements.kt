package com.artie.chargemenot.ui.motion

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedFinancialBloom(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?
): Modifier {
    if (sharedTransitionScope == null || animatedVisibilityScope == null) {
        return this
    }
    return with(sharedTransitionScope) {
        this@sharedFinancialBloom.sharedBounds(
            sharedContentState = rememberSharedContentState(key = MeadowMotion.FINANCIAL_BLOOM),
            animatedVisibilityScope = animatedVisibilityScope,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = MeadowMotion.SHARED_BOUNDS_MS,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = MeadowMotion.SHARED_BOUNDS_MS,
                    easing = FastOutSlowInEasing
                )
            ),
            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )
        )
    }
}
