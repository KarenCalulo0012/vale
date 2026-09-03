package com.kcalulo.vale.core.common

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Friendly currency formatting from minor units.
 * Whole amounts drop decimals ("₱1,200"); fractional ones keep two ("₱62.50").
 */
object MoneyFormat {

    fun format(amountMinor: Long, symbol: String = "₱"): String {
        val major = amountMinor / 100
        val cents = abs(amountMinor % 100)
        val grouped = NumberFormat.getIntegerInstance(Locale.US).format(major)
        return if (cents == 0L) "$symbol$grouped"
        else "$symbol$grouped.${cents.toString().padStart(2, '0')}"
    }

    /** Formats a cost-per-use (Double minor units) rounded to the nearest centavo, e.g. 4000.0 → "₱40". */
    fun formatPerUse(amountMinorPerUse: Double, symbol: String = "₱"): String =
        format(Math.round(amountMinorPerUse), symbol)
}
