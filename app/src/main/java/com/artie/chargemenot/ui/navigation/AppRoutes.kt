package com.artie.chargemenot.ui.navigation

object AppRoutes {
    const val MEADOW_HUB = "meadow_hub"
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val PETALS_AND_WEEDS = "petals_and_weeds"
    const val RICH_SOIL = "rich_soil"
    const val HARVEST_REPORT = "harvest_report"
    const val GREENHOUSE_SETTINGS = "greenhouse_settings"
    const val SCANNER = "scanner"
    const val PRUNING_SIMULATOR = "pruning_simulator"
    const val WEED_WHACKER = "weed_whacker"
    const val COMPOST_BIN = "compost_bin"

    val overlayPreservingRoutes: Set<String> = setOf(
        DASHBOARD,
        PETALS_AND_WEEDS,
        RICH_SOIL,
        HARVEST_REPORT,
        GREENHOUSE_SETTINGS
    )

    val navigableRoutes: Set<String> = setOf(
        MEADOW_HUB,
        DASHBOARD,
        PETALS_AND_WEEDS,
        RICH_SOIL,
        HARVEST_REPORT,
        GREENHOUSE_SETTINGS,
        SCANNER,
        PRUNING_SIMULATOR,
        WEED_WHACKER,
        COMPOST_BIN
    )

    fun shouldReturnToGardenHub(didPopBackStack: Boolean): Boolean = !didPopBackStack

    fun shouldConsumePendingNavigation(currentRoute: String?): Boolean {
        return currentRoute != null && currentRoute != ONBOARDING
    }

    fun isAllowedPendingNavigationRoute(route: String?): Boolean {
        return route != null && route in navigableRoutes
    }

    fun meadowRouteNavSpec(targetRoute: String, currentRoute: String?): MeadowRouteNavSpec {
        val goingToGardenHub = targetRoute == DASHBOARD
        return MeadowRouteNavSpec(
            usesPopBackStackToDashboard = goingToGardenHub,
            popDashboardInclusively = false,
            saveState = !goingToGardenHub && currentRoute in overlayPreservingRoutes,
            restoreState = !goingToGardenHub && targetRoute in overlayPreservingRoutes
        )
    }
}

data class MeadowRouteNavSpec(
    val usesPopBackStackToDashboard: Boolean,
    val popDashboardInclusively: Boolean,
    val saveState: Boolean,
    val restoreState: Boolean
)
