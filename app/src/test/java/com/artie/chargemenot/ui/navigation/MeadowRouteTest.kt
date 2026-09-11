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
}
