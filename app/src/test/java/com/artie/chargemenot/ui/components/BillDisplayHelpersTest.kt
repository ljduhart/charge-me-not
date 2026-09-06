package com.artie.chargemenot.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class BillDisplayHelpersTest {

    @Test
    fun billInitial_returnsUppercaseFirstCharacter() {
        assertEquals("P", billInitial("Pacific Gas & Electric"))
    }

    @Test
    fun billInitial_returnsFallbackForBlankName() {
        assertEquals("?", billInitial(""))
        assertEquals("?", billInitial("   "))
    }
}
