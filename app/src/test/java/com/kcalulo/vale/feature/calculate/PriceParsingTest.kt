package com.kcalulo.vale.feature.calculate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PriceParsingTest {

    @Test
    fun `plain integer parses to minor units`() {
        assertEquals(120_000L, parsePriceMinor("1200"))
    }

    @Test
    fun `grouped and decorated input parses`() {
        assertEquals(120_000L, parsePriceMinor("1,200"))
        assertEquals(120_000L, parsePriceMinor("₱1,200"))
        assertEquals(120_050L, parsePriceMinor("1,200.50"))
    }

    @Test
    fun `decimals parse exactly without float drift`() {
        assertEquals(6_250L, parsePriceMinor("62.50"))
        assertEquals(1L, parsePriceMinor("0.01"))
    }

    @Test
    fun `garbage and blanks return null`() {
        assertNull(parsePriceMinor(""))
        assertNull(parsePriceMinor("  "))
        assertNull(parsePriceMinor("abc"))
        assertNull(parsePriceMinor("12.3.4"))
    }

    @Test
    fun `unreasonably large values return null instead of overflowing`() {
        // spec §7 "large numeric values" — this would overflow Long once converted to minor units
        assertNull(parsePriceMinor("99999999999999999999"))
        assertNull(parsePriceMinor("92233720368547759")) // × 100 exceeds Long.MAX_VALUE
    }

    @Test
    fun `values just under the Long boundary still parse`() {
        assertEquals(100_000_000_000L, parsePriceMinor("1000000000"))
    }
}
