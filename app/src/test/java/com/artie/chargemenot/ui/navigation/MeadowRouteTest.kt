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
    fun fromNavRouteOrNull_mapsAdminAndTabDestinations() {
        assertEquals(MeadowRoute.CompostBin, MeadowRoute.fromNavRouteOrNull(AppRoutes.COMPOST_BIN))
        assertEquals(MeadowRoute.PruningSkills, MeadowRoute.fromNavRouteOrNull(AppRoutes.PRUNING_SIMULATOR))
        assertEquals(MeadowRoute.WeedWhacker, MeadowRoute.fromNavRouteOrNull(AppRoutes.WEED_WHACKER))
    }

    @Test
    fun bottomBarRoutes_flattenPrimaryTabsAroundTheScannerFab() {
        assertEquals(
            listOf(
                MeadowRoute.PetalsAndWeeds,
                MeadowRoute.RichSoil,
                MeadowRoute.WeedWhacker,
                MeadowRoute.HarvestReport
            ),
            MeadowRoute.bottomBarRoutes
        )
        assertEquals(
            listOf(MeadowRoute.PetalsAndWeeds, MeadowRoute.RichSoil),
            MeadowRoute.bottomBarLeadingRoutes
        )
        assertEquals(
            listOf(MeadowRoute.WeedWhacker, MeadowRoute.HarvestReport),
            MeadowRoute.bottomBarTrailingRoutes
        )
        assertFalse(MeadowRoute.bottomBarRoutes.contains(MeadowRoute.GardenHub))
        assertFalse(MeadowRoute.bottomBarRoutes.contains(MeadowRoute.GreenhouseSettings))
        assertFalse(MeadowRoute.bottomBarRoutes.contains(MeadowRoute.CompostBin))
        assertFalse(MeadowRoute.bottomBarRoutes.contains(MeadowRoute.PruningSkills))
        assertEquals(MeadowRoute.PetalsAndWeeds, MeadowRoute.selectedBottomBarRoute(PETALS_AND_WEEDS))
        assertEquals(MeadowRoute.WeedWhacker, MeadowRoute.selectedBottomBarRoute(AppRoutes.WEED_WHACKER))
        assertEquals(null, MeadowRoute.selectedBottomBarRoute(DASHBOARD))
        assertEquals(null, MeadowRoute.selectedBottomBarRoute(AppRoutes.COMPOST_BIN))
        assertEquals(null, MeadowRoute.selectedBottomBarRoute(AppRoutes.SCANNER))
    }

    @Test
    fun adminMenuRoutes_houseDisplacedGardenTools() {
        assertEquals(
            listOf(
                MeadowRoute.GreenhouseSettings,
                MeadowRoute.PruningSkills,
                MeadowRoute.CompostBin
            ),
            MeadowRoute.adminMenuRoutes
        )
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
                GREENHOUSE_SETTINGS,
                AppRoutes.WEED_WHACKER,
                AppRoutes.COMPOST_BIN,
                AppRoutes.PRUNING_SIMULATOR
            ),
            AppRoutes.overlayPreservingRoutes
        )
        assertFalse(AppRoutes.overlayPreservingRoutes.contains(AppRoutes.SCANNER))
        assertFalse(AppRoutes.overlayPreservingRoutes.contains(AppRoutes.ONBOARDING))
        assertTrue(AppRoutes.showsMeadowOverlays(DASHBOARD))
        assertTrue(AppRoutes.showsMeadowOverlays(AppRoutes.WEED_WHACKER))
        assertTrue(AppRoutes.showsMeadowOverlays(AppRoutes.COMPOST_BIN))
        assertFalse(AppRoutes.showsMeadowOverlays(AppRoutes.SCANNER))
        assertFalse(AppRoutes.showsMeadowOverlays(AppRoutes.ONBOARDING))
        assertFalse(AppRoutes.shouldClearOverlayState(DASHBOARD))
        assertFalse(AppRoutes.shouldClearOverlayState(AppRoutes.SCANNER))
        assertTrue(AppRoutes.shouldClearOverlayState(AppRoutes.ONBOARDING))
    }

    @Test
    fun showsMeadowBottomBar_hidesOnOnboardingAndScanner() {
        assertTrue(AppRoutes.showsMeadowBottomBar(DASHBOARD))
        assertTrue(AppRoutes.showsMeadowBottomBar(PETALS_AND_WEEDS))
        assertTrue(AppRoutes.showsMeadowBottomBar(AppRoutes.WEED_WHACKER))
        assertTrue(AppRoutes.showsMeadowBottomBar(AppRoutes.COMPOST_BIN))
        assertTrue(AppRoutes.showsMeadowBottomBar(AppRoutes.PRUNING_SIMULATOR))
        assertFalse(AppRoutes.showsMeadowBottomBar(null))
        assertFalse(AppRoutes.showsMeadowBottomBar(AppRoutes.ONBOARDING))
        assertFalse(AppRoutes.showsMeadowBottomBar(AppRoutes.SCANNER))
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
        assertTrue(dashboardToCompost.restoreState)
    }

    @Test
    fun shouldConsumePendingNavigation_waitsUntilOnboardingLeaves() {
        assertFalse(AppRoutes.shouldConsumePendingNavigation(currentRoute = null))
        assertFalse(AppRoutes.shouldConsumePendingNavigation(AppRoutes.ONBOARDING))
        assertTrue(AppRoutes.shouldConsumePendingNavigation(AppRoutes.DASHBOARD))
        assertTrue(AppRoutes.shouldConsumePendingNavigation(AppRoutes.MEADOW_HUB))
        assertTrue(AppRoutes.shouldConsumePendingNavigation(AppRoutes.PETALS_AND_WEEDS))
        assertTrue(AppRoutes.shouldConsumePendingNavigation(AppRoutes.WEED_WHACKER))
        assertTrue(AppRoutes.shouldConsumePendingNavigation(AppRoutes.SCANNER))
    }

    @Test
    fun meadowRouteNavSpec_fromPetalsPopsBackToGardenHubWithoutDestroyingHubState() {
        val petalsToHub = AppRoutes.meadowRouteNavSpec(
            targetRoute = AppRoutes.DASHBOARD,
            currentRoute = AppRoutes.PETALS_AND_WEEDS
        )
        val hubToPetals = AppRoutes.meadowRouteNavSpec(
            targetRoute = AppRoutes.PETALS_AND_WEEDS,
            currentRoute = AppRoutes.DASHBOARD
        )

        assertTrue(petalsToHub.usesPopBackStackToDashboard)
        assertFalse(petalsToHub.popDashboardInclusively)
        assertFalse(petalsToHub.saveState)
        assertFalse(petalsToHub.restoreState)

        assertFalse(hubToPetals.usesPopBackStackToDashboard)
        assertFalse(hubToPetals.popDashboardInclusively)
        assertTrue(hubToPetals.saveState)
        assertTrue(hubToPetals.restoreState)
    }

    @Test
    fun isAllowedPendingNavigationRoute_acceptsKnownDestinationsOnly() {
        assertTrue(AppRoutes.isAllowedPendingNavigationRoute(AppRoutes.WEED_WHACKER))
        assertTrue(AppRoutes.isAllowedPendingNavigationRoute(AppRoutes.PETALS_AND_WEEDS))
        assertTrue(AppRoutes.isAllowedPendingNavigationRoute(AppRoutes.DASHBOARD))
        assertTrue(AppRoutes.isAllowedPendingNavigationRoute(AppRoutes.MEADOW_HUB))
        assertTrue(AppRoutes.isAllowedPendingNavigationRoute(AppRoutes.COMPOST_BIN))
        assertFalse(AppRoutes.isAllowedPendingNavigationRoute(AppRoutes.ONBOARDING))
        assertFalse(AppRoutes.isAllowedPendingNavigationRoute(null))
        assertFalse(AppRoutes.isAllowedPendingNavigationRoute(""))
        assertFalse(AppRoutes.isAllowedPendingNavigationRoute("not_a_real_route"))
    }
}
