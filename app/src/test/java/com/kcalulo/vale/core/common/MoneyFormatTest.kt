package com.kcalulo.vale.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatTest {

    @Test
    fun `whole amounts drop decimals`() {
        assertEquals("₱1,200", MoneyFormat.format(120_000))
    }

    @Test
    fun `fractional amounts keep two decimals`() {
        assertEquals("₱62.50", MoneyFormat.format(6_250))
    }

    @Test
    fun `zero formats cleanly`() {
        assertEquals("₱0", MoneyFormat.format(0))
    }

    @Test
    fun `large values group correctly`() {
        assertEquals("₱1,234,567.89", MoneyFormat.format(123_456_789))
    }

    @Test
    fun `per use rounds to nearest centavo`() {
        assertEquals("₱333.33", MoneyFormat.formatPerUse(100_000.0 / 3))
        assertEquals("₱40", MoneyFormat.formatPerUse(4_000.0))
    }

    @Test
    fun `custom symbol is respected`() {
        assertEquals("$25", MoneyFormat.format(2_500, symbol = "$"))
    }
}
