package com.artie.chargemenot.ui.motion

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.gardenPathEdgeFade(
    listState: LazyListState,
    layoutIndex: Int
): Modifier {
    val fadeDistancePx = with(LocalDensity.current) {
        MeadowMotion.GARDEN_PATH_EDGE_FADE_DP.dp.toPx()
    }
    val layoutInfo = listState.layoutInfo
    val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { item ->
        item.index == layoutIndex
    }
    val progress = if (itemInfo == null) {
        1f
    } else {
        MeadowMotion.gardenPathEdgeProgress(
            itemOffset = itemInfo.offset,
            itemSize = itemInfo.size,
            viewportStart = layoutInfo.viewportStartOffset,
            viewportEnd = layoutInfo.viewportEndOffset,
            fadeDistance = fadeDistancePx
        )
    }
    val scale = MeadowMotion.gardenPathEdgeScale(progress)
    val alpha = MeadowMotion.gardenPathEdgeAlpha(progress)
    return graphicsLayer {
        this.alpha = alpha
        scaleX = scale
        scaleY = scale
    }
}
