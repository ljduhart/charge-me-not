package com.artie.chargemenot.ui.navigation

import com.artie.chargemenot.ui.navigation.AppRoutes.DASHBOARD
import com.artie.chargemenot.ui.navigation.AppRoutes.GREENHOUSE_SETTINGS
import com.artie.chargemenot.ui.navigation.AppRoutes.HARVEST_REPORT
import com.artie.chargemenot.ui.navigation.AppRoutes.PETALS_AND_WEEDS
import com.artie.chargemenot.ui.navigation.AppRoutes.RICH_SOIL
import org.junit.Assert.assertEquals
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
}
