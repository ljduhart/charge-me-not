package com.artie.chargemenot.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.graphics.vector.ImageVector
import com.artie.chargemenot.R

enum class MeadowRouteGroup {
    CoreDaily,
    AnalyticalAdmin
}

enum class MeadowRoute(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val group: MeadowRouteGroup
) {
    GardenHub(
        route = AppRoutes.DASHBOARD,
        titleRes = R.string.meadow_route_garden_hub,
        icon = Icons.Default.LocalFlorist,
        group = MeadowRouteGroup.CoreDaily
    ),
    PetalsAndWeeds(
        route = AppRoutes.PETALS_AND_WEEDS,
        titleRes = R.string.meadow_route_petals_and_weeds,
        icon = Icons.Default.Spa,
        group = MeadowRouteGroup.CoreDaily
    ),
    RichSoil(
        route = AppRoutes.RICH_SOIL,
        titleRes = R.string.meadow_route_rich_soil,
        icon = Icons.Default.Agriculture,
        group = MeadowRouteGroup.CoreDaily
    ),
    CompostBin(
        route = AppRoutes.COMPOST_BIN,
        titleRes = R.string.meadow_route_compost_bin,
        icon = Icons.Default.Recycling,
        group = MeadowRouteGroup.CoreDaily
    ),
    PruningSkills(
        route = AppRoutes.PRUNING_SIMULATOR,
        titleRes = R.string.meadow_route_pruning_skills,
        icon = Icons.Default.ContentCut,
        group = MeadowRouteGroup.AnalyticalAdmin
    ),
    WeedWhacker(
        route = AppRoutes.WEED_WHACKER,
        titleRes = R.string.meadow_route_weed_whacker,
        icon = Icons.Default.Grass,
        group = MeadowRouteGroup.AnalyticalAdmin
    ),
    HarvestReport(
        route = AppRoutes.HARVEST_REPORT,
        titleRes = R.string.meadow_route_harvest_report,
        icon = Icons.Default.Cloud,
        group = MeadowRouteGroup.AnalyticalAdmin
    ),
    GreenhouseSettings(
        route = AppRoutes.GREENHOUSE_SETTINGS,
        titleRes = R.string.meadow_route_greenhouse_settings,
        icon = Icons.Default.Settings,
        group = MeadowRouteGroup.AnalyticalAdmin
    );

    companion object {
        val coreDailyRoutes: List<MeadowRoute> = entries.filter { route ->
            route.group == MeadowRouteGroup.CoreDaily
        }

        val analyticalRoutes: List<MeadowRoute> = entries.filter { route ->
            route.group == MeadowRouteGroup.AnalyticalAdmin
        }

        fun fromNavRoute(route: String?): MeadowRoute {
            return fromNavRouteOrNull(route) ?: GardenHub
        }

        fun fromNavRouteOrNull(route: String?): MeadowRoute? {
            if (route == AppRoutes.MEADOW_HUB) {
                return GardenHub
            }
            return entries.firstOrNull { meadowRoute -> meadowRoute.route == route }
        }
    }
}
