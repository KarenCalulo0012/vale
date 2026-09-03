package com.kcalulo.vale.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Business-rule tests (spec §27). Money in minor units: ₱3,000 = 300_000.
 */
class ValeCalculationsTest {

    // targetCostPerUse = purchasePrice / expectedUses

    @Test
    fun `target cost per use divides price by expected uses`() {
        // ₱3,000 / 30 uses = ₱100/use
        assertEquals(10_000.0, ValeCalculations.targetCostPerUse(300_000, 30), 0.0)
    }

    @Test
    fun `target cost per use keeps fractions`() {
        // ₱1,000 / 3 uses = ₱333.33…
        assertEquals(100_000.0 / 3, ValeCalculations.targetCostPerUse(100_000, 3), 0.0001)
    }

    @Test
    fun `target cost per use rejects zero expected uses`() {
        assertThrows(IllegalArgumentException::class.java) {
            ValeCalculations.targetCostPerUse(100_000, 0)
        }
    }

    // currentCostPerUse = effectiveCost / actualUses, null at zero uses

    @Test
    fun `current cost per use divides by actual uses`() {
        // ₱1,200 after 12 uses = ₱100/use
        assertEquals(10_000.0, ValeCalculations.currentCostPerUse(120_000, 12)!!, 0.0)
    }

    @Test
    fun `current cost per use is null at zero uses instead of dividing by zero`() {
        assertNull(ValeCalculations.currentCostPerUse(120_000, 0))
    }

    // progress and remaining uses

    @Test
    fun `progress is actual over expected`() {
        assertEquals(0.4f, ValeCalculations.progress(12, 30), 0.0001f)
    }

    @Test
    fun `progress may exceed one`() {
        assertEquals(1.2f, ValeCalculations.progress(36, 30), 0.0001f)
    }

    @Test
    fun `progress fraction clamps for progress bars`() {
        assertEquals(1f, ValeCalculations.progressFraction(36, 30), 0f)
    }

    @Test
    fun `progress with zero expected uses is zero not NaN`() {
        assertEquals(0f, ValeCalculations.progress(5, 0), 0f)
    }

    @Test
    fun `remaining uses clamps at zero when user lowered expected below actual`() {
        assertEquals(0, ValeCalculations.remainingUses(10, 14))
        assertEquals(18, ValeCalculations.remainingUses(30, 12))
    }

    // targetResalePrice = max(price - targetCostPerUse * actualUses, 0)

    @Test
    fun `target resale price matches spec example`() {
        // ₱3,000, target ₱100/use, 8 uses → ₱2,200
        val result = ValeCalculations.targetResalePriceMinor(
            priceMinor = 300_000,
            targetCostPerUseMinor = 10_000.0,
            actualUses = 8,
        )
        assertEquals(220_000L, result)
    }

    @Test
    fun `target resale price clamps at zero once target usage is passed`() {
        val result = ValeCalculations.targetResalePriceMinor(
            priceMinor = 300_000,
            targetCostPerUseMinor = 10_000.0,
            actualUses = 40,
        )
        assertEquals(0L, result)
    }

    // effectiveCost and finalCostPerUse after sale

    @Test
    fun `sale flow matches spec example`() {
        // Bought ₱3,000, sold ₱2,500 after 8 uses → effective ₱500, ₱62.50/use
        val effective = ValeCalculations.effectiveCostAfterSaleMinor(300_000, 250_000)
        assertEquals(50_000L, effective)
        assertEquals(6_250.0, ValeCalculations.finalCostPerUse(effective, 8)!!, 0.0)
    }

    @Test
    fun `selling above purchase price yields negative effective cost - profit`() {
        val effective = ValeCalculations.effectiveCostAfterSaleMinor(300_000, 350_000)
        assertEquals(-50_000L, effective)
    }

    @Test
    fun `final cost per use is null when sold with zero uses`() {
        assertNull(ValeCalculations.finalCostPerUse(50_000, 0))
    }

    // Result verdicts (deterministic, no AI)

    @Test
    fun `many expected uses is approved`() {
        assertEquals(ResultVerdict.APPROVED, ValeCalculations.resultVerdict(20))
        assertEquals(ResultVerdict.APPROVED, ValeCalculations.resultVerdict(100))
    }

    @Test
    fun `moderate expected uses is neutral`() {
        assertEquals(ResultVerdict.NEUTRAL, ValeCalculations.resultVerdict(5))
        assertEquals(ResultVerdict.NEUTRAL, ValeCalculations.resultVerdict(19))
    }

    @Test
    fun `very few expected uses is questionable`() {
        assertEquals(ResultVerdict.QUESTIONABLE, ValeCalculations.resultVerdict(1))
        assertEquals(ResultVerdict.QUESTIONABLE, ValeCalculations.resultVerdict(4))
    }

    // Reality Check verdicts

    @Test
    fun `worth it when target reached or exceeded`() {
        assertEquals(
            RealityCheckState.WORTH_IT,
            ValeCalculations.realityCheckState(30, 30, daysSincePurchase = 5)
        )
        assertEquals(
            RealityCheckState.WORTH_IT,
            ValeCalculations.realityCheckState(31, 30, daysSincePurchase = 5)
        )
    }

    @Test
    fun `still proving while progressing`() {
        assertEquals(
            RealityCheckState.STILL_PROVING_IT,
            ValeCalculations.realityCheckState(15, 30, daysSincePurchase = 90)
        )
    }

    @Test
    fun `new item far behind is still proving - not yet judged`() {
        assertEquals(
            RealityCheckState.STILL_PROVING_IT,
            ValeCalculations.realityCheckState(1, 30, daysSincePurchase = 10)
        )
    }

    @Test
    fun `old item far behind means the math is not working`() {
        assertEquals(
            RealityCheckState.MATH_ISNT_WORKING,
            ValeCalculations.realityCheckState(2, 30, daysSincePurchase = 90)
        )
    }
}
