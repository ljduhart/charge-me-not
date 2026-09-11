package com.artie.chargemenot.util

import com.artie.chargemenot.domain.model.SupportedCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyParserTest {

    @Test
    fun parseStringToCents_convertsDecimalDollars() {
        assertEquals(1599L, CurrencyParser.parseStringToCents("15.99"))
        assertEquals(1500L, CurrencyParser.parseStringToCents("15"))
        assertEquals(1590L, CurrencyParser.parseStringToCents("15.9"))
        assertEquals(145000L, CurrencyParser.parseStringToCents("1,450.00"))
    }

    @Test
    fun parseStringToCents_ignoresInvalidCharacters() {
        assertEquals(1599L, CurrencyParser.parseStringToCents("$15.99"))
        assertEquals(9417L, CurrencyParser.parseStringToCents("Total: 94.17 USD"))
        assertEquals(0L, CurrencyParser.parseStringToCents("abc"))
        assertEquals(0L, CurrencyParser.parseStringToCents(""))
    }

    @Test
    fun parseStringToCents_handlesNegativeInput() {
        assertEquals(-2599L, CurrencyParser.parseStringToCents("-25.99"))
    }
}

class CurrencyFormatterTest {

    @Test
    fun format_usesMinorUnitsAndSupportedLocales() {
        val usd = CurrencyFormatter.format(1599L, SupportedCurrency.USD)
        val cad = CurrencyFormatter.format(1599L, SupportedCurrency.CAD)
        val mxn = CurrencyFormatter.format(1599L, SupportedCurrency.MXN)
        val eur = CurrencyFormatter.format(1599L, SupportedCurrency.EUR)

        assertTrue(usd.contains("15.99") || usd.contains("15,99"))
        assertTrue(cad.contains("15.99") || cad.contains("15,99"))
        assertTrue(mxn.contains("15.99") || mxn.contains("15,99"))
        assertTrue(eur.contains("15.99") || eur.contains("15,99"))
    }

    @Test
    fun fromCode_mapsUnknownCurrenciesToUsd() {
        assertEquals(SupportedCurrency.USD, SupportedCurrency.fromCode("GBP"))
        assertEquals(SupportedCurrency.CAD, SupportedCurrency.fromCode("cad"))
        assertEquals(SupportedCurrency.MXN, SupportedCurrency.fromCode("MXN"))
        assertEquals(SupportedCurrency.EUR, SupportedCurrency.fromCode("EUR"))
    }
}
