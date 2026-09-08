package com.artie.chargemenot.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Park
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.artie.chargemenot.domain.model.MeadowCategories

fun meadowParentColor(parentName: String): Color = when (parentName) {
    MeadowCategories.CANOPY -> MeadowRose
    MeadowCategories.ROOT_SYSTEM -> MeadowEarth
    MeadowCategories.VINES -> MeadowLavender
    MeadowCategories.FERTILIZER -> MeadowSunflower
    MeadowCategories.POLLINATORS -> MeadowBlush
    MeadowCategories.WILDFLOWERS -> MeadowSage
    else -> MeadowGreen
}

fun meadowParentIcon(parentName: String): ImageVector = when (parentName) {
    MeadowCategories.CANOPY -> Icons.Default.Park
    MeadowCategories.ROOT_SYSTEM -> Icons.Default.Forest
    MeadowCategories.VINES -> Icons.Default.Eco
    MeadowCategories.FERTILIZER -> Icons.Default.Grass
    MeadowCategories.POLLINATORS -> Icons.Default.Air
    MeadowCategories.WILDFLOWERS -> Icons.Default.LocalFlorist
    else -> Icons.Default.LocalFlorist
}
