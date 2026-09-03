package com.kcalulo.vale.core.common

import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.core.database.entity.ItemCategory
import com.kcalulo.vale.core.database.entity.ItemEntity
import com.kcalulo.vale.core.database.entity.ItemStatus
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValeProgressSummaryTest {

    private fun item(
        status: ItemStatus,
        expectedUses: Int = 10,
        price: Long = 300_000,
        purchaseDate: LocalDate? = null,
        createdAt: Instant = Instant.now(),
    ) = ItemEntity(
        name = "Test Item",
        category = ItemCategory.OTHER,
        originalPriceMinor = price,
        purchaseDate = purchaseDate,
        createdAt = createdAt,
        expectedUses = expectedUses,
        targetCostPerUseMinor = ValeCalculations.targetCostPerUse(price, expectedUses),
        status = status,
    )

    private fun row(item: ItemEntity, actualUses: Int = 0) = ItemWithUsageCount(item, actualUses)

    @Test
    fun `overview tallies each lifecycle status once`() {
        val items = listOf(
            row(item(ItemStatus.BOUGHT, expectedUses = 10), actualUses = 3), // still proving
            row(item(ItemStatus.BOUGHT, expectedUses = 10), actualUses = 10), // proven worth it
            row(item(ItemStatus.SKIPPED)),
            row(item(ItemStatus.SOLD)),
            row(item(ItemStatus.GIVEN_AWAY)),
        )
        val overview = ValeProgressSummary.overview(items)

        assertEquals(5, overview.thingsChecked)
        assertEquals(2, overview.bought)
        assertEquals(1, overview.skipped)
        assertEquals(1, overview.provenWorthIt) // only the fully-used BOUGHT item
        assertEquals(1, overview.stillProving)
        assertEquals(1, overview.sold)
        assertEquals(1, overview.givenAway)
    }

    @Test
    fun `best skip picks the highest-priced skipped item`() {
        val items = listOf(
            row(item(ItemStatus.SKIPPED, price = 100_000)),
            row(item(ItemStatus.SKIPPED, price = 500_000)),
            row(item(ItemStatus.BOUGHT, price = 900_000)), // not skipped, ignored
        )
        val bestSkip = ValeProgressSummary.highlights(items).bestSkip
        assertEquals(500_000.0, bestSkip!!.value, 0.0)
    }

    @Test
    fun `best math picks the lowest current cost per use among bought or sold items`() {
        val items = listOf(
            row(item(ItemStatus.BOUGHT, price = 300_000, expectedUses = 30), actualUses = 6), // 50000/use
            row(item(ItemStatus.BOUGHT, price = 100_000, expectedUses = 10), actualUses = 10), // 10000/use
            row(item(ItemStatus.CONSIDERING, price = 1, expectedUses = 1), actualUses = 0), // ignored, no uses
        )
        val bestMath = ValeProgressSummary.highlights(items).bestMath
        assertEquals(10_000.0, bestMath!!.value, 0.0)
    }

    @Test
    fun `questionable math finds an old item far behind its target`() {
        val longAgo = LocalDate.now().minusDays(120)
        val items = listOf(
            row(item(ItemStatus.BOUGHT, expectedUses = 30, purchaseDate = longAgo), actualUses = 1),
        )
        val questionable = ValeProgressSummary.highlights(items).questionableMath
        assertEquals("Test Item", questionable?.itemName)
    }

    @Test
    fun `questionable math is null when nothing is behind`() {
        val items = listOf(
            row(item(ItemStatus.BOUGHT, expectedUses = 10, purchaseDate = LocalDate.now()), actualUses = 10),
        )
        assertNull(ValeProgressSummary.highlights(items).questionableMath)
    }

    @Test
    fun `monthly snapshot counts only this month's calculations and skips`() {
        val now = LocalDate.now()
        val thisMonth = now.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        val lastMonth = now.minusMonths(2).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()

        val items = listOf(
            row(item(ItemStatus.CONSIDERING, createdAt = thisMonth)),
            row(item(ItemStatus.CONSIDERING, createdAt = lastMonth)),
            row(item(ItemStatus.SKIPPED, createdAt = thisMonth)),
            row(item(ItemStatus.SKIPPED, createdAt = lastMonth)),
        )
        val snapshot = ValeProgressSummary.monthlySnapshot(items, usageLogsThisMonth = 4, now = now)

        assertEquals(2, snapshot.calculationsMade)
        assertEquals(1, snapshot.purchasesSkipped)
        assertEquals(4, snapshot.usageLogs)
    }
}
