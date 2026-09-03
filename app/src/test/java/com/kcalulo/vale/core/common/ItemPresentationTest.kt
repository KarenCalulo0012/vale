package com.kcalulo.vale.core.common

import com.kcalulo.vale.core.database.entity.ItemCategory
import com.kcalulo.vale.core.database.entity.ItemEntity
import com.kcalulo.vale.core.database.entity.ItemStatus
import com.kcalulo.vale.core.database.entity.SkipReason
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemPresentationTest {

    private fun considering(price: Long = 300_000, expectedUses: Int = 30) = ItemEntity(
        name = "Test Item",
        category = ItemCategory.TECH,
        originalPriceMinor = price,
        createdAt = Instant.now(),
        expectedUses = expectedUses,
        targetCostPerUseMinor = ValeCalculations.targetCostPerUse(price, expectedUses),
        status = ItemStatus.CONSIDERING,
    )

    @Test
    fun `asBought sets status and today's purchase date`() {
        val bought = considering().asBought()
        assertEquals(ItemStatus.BOUGHT, bought.status)
        assertEquals(java.time.LocalDate.now(), bought.purchaseDate)
    }

    @Test
    fun `asSkipped preserves the reason`() {
        val skipped = considering().asSkipped(SkipReason.TOO_EXPENSIVE)
        assertEquals(ItemStatus.SKIPPED, skipped.status)
        assertEquals(SkipReason.TOO_EXPENSIVE, skipped.skipReason)
    }

    @Test
    fun `asSold sets status, sold price, and today's sold date`() {
        val sold = considering().asSold(250_000)
        assertEquals(ItemStatus.SOLD, sold.status)
        assertEquals(250_000L, sold.soldPriceMinor)
        assertEquals(java.time.LocalDate.now(), sold.soldDate)
    }

    @Test
    fun `asGivenAway blanks out a whitespace-only note`() {
        val givenAway = considering().asGivenAway("   ")
        assertEquals(ItemStatus.GIVEN_AWAY, givenAway.status)
        assertNull(givenAway.note)
        assertTrue(givenAway.givenAwayDate != null)
    }

    @Test
    fun `asGivenAway keeps a real note`() {
        val givenAway = considering().asGivenAway("Gave it to my sister")
        assertEquals("Gave it to my sister", givenAway.note)
    }

    @Test
    fun `displayCostPerUseMinor for bought uses current cost per use`() {
        val bought = considering(price = 300_000, expectedUses = 30).asBought()
        assertEquals(50_000.0, bought.displayCostPerUseMinor(6)!!, 0.0)
    }

    @Test
    fun `displayCostPerUseMinor for sold uses final cost per use after sale`() {
        // spec §18 example: bought 3000, sold 2500 after 8 uses -> effective 500 -> 62.50/use
        val sold = considering(price = 300_000, expectedUses = 30).asSold(250_000)
        assertEquals(6_250.0, sold.displayCostPerUseMinor(8)!!, 0.0)
    }

    @Test
    fun `displayCostPerUseMinor for sold with zero uses is null`() {
        val sold = considering().asSold(250_000)
        assertNull(sold.displayCostPerUseMinor(0))
    }

    @Test
    fun `summaryLine covers every status distinctly - no status silently reads as skipped`() {
        val base = considering(price = 350_000, expectedUses = 10)
        assertEquals("6 / 10 uses", base.asBought().copy().let { it.summaryLine(6, "₱") })
        assertEquals("Planned: 10 uses", base.summaryLine(0, "₱"))
        assertEquals("Skipped — ₱3,500 not spent", base.asSkipped(null).summaryLine(0, "₱"))
        assertEquals("Sold for ₱2,000", base.asSold(200_000).summaryLine(6, "₱"))
        assertEquals("Given away", base.asGivenAway(null).summaryLine(0, "₱"))
    }

    @Test
    fun `displayCostPerUseMinor for considering and skipped falls back to target`() {
        val item = considering(price = 300_000, expectedUses = 30)
        assertEquals(item.targetCostPerUseMinor, item.displayCostPerUseMinor(0))
        assertEquals(item.targetCostPerUseMinor, item.asSkipped(null).displayCostPerUseMinor(0))
    }
}
