package com.artie.chargemenot.ui.navigation

import com.artie.chargemenot.ui.navigation.AppRoutes
import com.artie.chargemenot.ui.navigation.AppRoutes.DASHBOARD
import com.artie.chargemenot.ui.navigation.AppRoutes.GREENHOUSE_SETTINGS
import com.artie.chargemenot.ui.navigation.AppRoutes.HARVEST_REPORT
import com.artie.chargemenot.ui.navigation.AppRoutes.PETALS_AND_WEEDS
import com.artie.chargemenot.ui.navigation.AppRoutes.RICH_SOIL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MeadowRouteTest {

    @Test
    fun fromNavRoute_mapsKnownRoutes() {
        assertEquals(MeadowRoute.GardenHub, MeadowRoute.fromNavRoute(DASHBOARD))
        assertEquals(MeadowRoute.PetalsAndWeeds, MeadowRoute.fromNavRoute(PETALS_AND_WEEDS))
        assertEquals(MeadowRoute.RichSoil, MeadowRoute.fromNavRoute(RICH_SOIL))
        assertEquals(MeadowRoute.HarvestReport, MeadowRoute.fromNavRoute(HARVEST_REPORT))
        assertEquals(MeadowRoute.GreenhouseSettings, MeadowRoute.fromNavRoute(GREENHOUSE_SETTINGS))
    }

    @Test
    fun fromNavRouteOrNull_returnsNullForNonMeadowRoutes() {
        assertEquals(null, MeadowRoute.fromNavRouteOrNull("scanner"))
        assertEquals(null, MeadowRoute.fromNavRouteOrNull("onboarding"))
        assertEquals(null, MeadowRoute.fromNavRouteOrNull(null))
    }

    @Test
    fun fromNavRouteOrNull_mapsDrawerDestinations() {
        assertEquals(MeadowRoute.CompostBin, MeadowRoute.fromNavRouteOrNull(AppRoutes.COMPOST_BIN))
        assertEquals(MeadowRoute.PruningSkills, MeadowRoute.fromNavRouteOrNull(AppRoutes.PRUNING_SIMULATOR))
        assertEquals(MeadowRoute.WeedWhacker, MeadowRoute.fromNavRouteOrNull(AppRoutes.WEED_WHACKER))
    }

    @Test
    fun fromNavRoute_defaultsToGardenHubForUnknownRoutes() {
        assertEquals(MeadowRoute.GardenHub, MeadowRoute.fromNavRoute("scanner"))
        assertEquals(MeadowRoute.GardenHub, MeadowRoute.fromNavRoute(null))
    }

    @Test
    fun routeGroups_partitionAllDestinations() {
        val grouped = MeadowRoute.coreDailyRoutes + MeadowRoute.analyticalRoutes
        assertEquals(MeadowRoute.entries.size, grouped.size)
        assertEquals(MeadowRoute.entries.toSet(), grouped.toSet())
    }

    @Test
    fun fromNavRouteOrNull_mapsMeadowHubGraphToGardenHub() {
        assertEquals(MeadowRoute.GardenHub, MeadowRoute.fromNavRouteOrNull(AppRoutes.MEADOW_HUB))
    }

    @Test
    fun overlayPreservingRoutes_staySyncedWithGardenHubSurfaces() {
        assertEquals(
            setOf(
                DASHBOARD,
                PETALS_AND_WEEDS,
                RICH_SOIL,
                HARVEST_REPORT,
                GREENHOUSE_SETTINGS
            ),
            AppRoutes.overlayPreservingRoutes
        )
        assertFalse(AppRoutes.overlayPreservingRoutes.contains(AppRoutes.SCANNER))
        assertFalse(AppRoutes.overlayPreservingRoutes.contains(AppRoutes.ONBOARDING))
        assertFalse(AppRoutes.overlayPreservingRoutes.contains(AppRoutes.COMPOST_BIN))
    }

    @Test
    fun shouldReturnToGardenHub_whenBackStackDidNotPop() {
        assertTrue(AppRoutes.shouldReturnToGardenHub(didPopBackStack = false))
        assertFalse(AppRoutes.shouldReturnToGardenHub(didPopBackStack = true))
    }

    @Test
    fun meadowRouteNavSpec_neverPopsDashboardInclusively() {
        val fromHubSurfaces = AppRoutes.overlayPreservingRoutes + setOf(
            AppRoutes.SCANNER,
            AppRoutes.COMPOST_BIN,
            AppRoutes.PRUNING_SIMULATOR,
            AppRoutes.WEED_WHACKER
        )
        val targets = MeadowRoute.entries.map { meadowRoute -> meadowRoute.route }

        fromHubSurfaces.forEach { currentRoute ->
            targets.forEach { targetRoute ->
                val spec = AppRoutes.meadowRouteNavSpec(targetRoute, currentRoute)
                assertFalse(
                    "inclusive dashboard pop would destroy meadow_hub ViewModels: $currentRoute -> $targetRoute",
                    spec.popDashboardInclusively
                )
            }
        }
    }

    @Test
    fun meadowRouteNavSpec_gardenHubPopsBackToExistingDashboard() {
        val fromSettings = AppRoutes.meadowRouteNavSpec(
            targetRoute = AppRoutes.DASHBOARD,
            currentRoute = AppRoutes.GREENHOUSE_SETTINGS
        )
        val fromScanner = AppRoutes.meadowRouteNavSpec(
            targetRoute = AppRoutes.DASHBOARD,
            currentRoute = AppRoutes.SCANNER
        )

        assertTrue(fromSettings.usesPopBackStackToDashboard)
        assertTrue(fromScanner.usesPopBackStackToDashboard)
        assertFalse(fromSettings.saveState)
        assertFalse(fromSettings.restoreState)
        assertFalse(fromScanner.saveState)
        assertFalse(fromScanner.restoreState)
    }

    @Test
    fun meadowRouteNavSpec_preservesStateOnlyAcrossGardenHubSurfaces() {
        val settingsToPetals = AppRoutes.meadowRouteNavSpec(
            targetRoute = AppRoutes.PETALS_AND_WEEDS,
            currentRoute = AppRoutes.GREENHOUSE_SETTINGS
        )
        val scannerToPetals = AppRoutes.meadowRouteNavSpec(
            targetRoute = AppRoutes.PETALS_AND_WEEDS,
            currentRoute = AppRoutes.SCANNER
        )
        val dashboardToCompost = AppRoutes.meadowRouteNavSpec(
            targetRoute = AppRoutes.COMPOST_BIN,
            currentRoute = AppRoutes.DASHBOARD
        )

        assertFalse(settingsToPetals.usesPopBackStackToDashboard)
        assertTrue(settingsToPetals.saveState)
        assertTrue(settingsToPetals.restoreState)

        assertFalse(scannerToPetals.saveState)
        assertTrue(scannerToPetals.restoreState)

        assertTrue(dashboardToCompost.saveState)
        assertFalse(dashboardToCompost.restoreState)
    }

    @Test
    fun shouldConsumePendingNavigation_waitsUntilOnboardingLeaves() {
        assertFalse(AppRoutes.shouldConsumePendingNavigation(currentRoute = null))
        assertFalse(AppRoutes.shouldConsumePendingNavigation(AppRoutes.ONBOARDING))
        assertTrue(AppRoutes.shouldConsumePendingNavigation(AppRoutes.DASHBOARD))
        assertTrue(AppRoutes.shouldConsumePendingNavigation(AppRoutes.MEADOW_HUB))
        assertTrue(AppRoutes.shouldConsumePendingNavigation(AppRoutes.WEED_WHACKER))
        assertTrue(AppRoutes.shouldConsumePendingNavigation(AppRoutes.SCANNER))
    }
}
