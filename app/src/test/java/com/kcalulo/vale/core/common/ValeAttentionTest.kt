package com.kcalulo.vale.core.common

import com.kcalulo.vale.core.database.dao.ItemWithLastUse
import com.kcalulo.vale.core.database.entity.ItemCategory
import com.kcalulo.vale.core.database.entity.ItemEntity
import com.kcalulo.vale.core.database.entity.ItemStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValeAttentionTest {

    private fun item(
        expectedUses: Int = 10,
        purchaseDate: LocalDate? = LocalDate.now(),
    ) = ItemEntity(
        name = "Test Item",
        category = ItemCategory.OTHER,
        originalPriceMinor = 100_000,
        purchaseDate = purchaseDate,
        createdAt = Instant.now(),
        expectedUses = expectedUses,
        targetCostPerUseMinor = ValeCalculations.targetCostPerUse(100_000, expectedUses),
        status = ItemStatus.BOUGHT,
    )

    private fun row(item: ItemEntity, actualUses: Int = 0, lastUsedAt: Instant? = null) =
        ItemWithLastUse(item, actualUses, lastUsedAt)

    @Test
    fun `an item untouched since purchase past the threshold is not used recently`() {
        val today = LocalDate.now()
        val longAgo = today.minusDays(ValeAttention.NOT_USED_RECENTLY_DAYS)
        val items = listOf(row(item(purchaseDate = longAgo), actualUses = 0))

        val attention = ValeAttention.summarize(items, today)
        assertEquals(1, attention.notUsedRecently.size)
    }

    @Test
    fun `an item used recently is not flagged`() {
        val today = LocalDate.now()
        val recentUse = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val items = listOf(row(item(purchaseDate = today.minusDays(30)), actualUses = 5, lastUsedAt = recentUse))

        val attention = ValeAttention.summarize(items, today)
        assertTrue(attention.notUsedRecently.isEmpty())
    }

    @Test
    fun `an item that already reached its target is never flagged as stale`() {
        val today = LocalDate.now()
        val longAgo = today.minusDays(ValeAttention.NOT_USED_RECENTLY_DAYS + 10)
        val items = listOf(row(item(expectedUses = 5, purchaseDate = longAgo), actualUses = 5))

        val attention = ValeAttention.summarize(items, today)
        assertTrue(attention.notUsedRecently.isEmpty())
    }

    @Test
    fun `progress just under the target counts as close to target`() {
        val items = listOf(row(item(expectedUses = 10), actualUses = 8))
        val attention = ValeAttention.summarize(items)
        assertEquals(1, attention.closeToTarget.size)
        assertEquals("8 / 10 uses", attention.closeToTarget.first().detail)
    }

    @Test
    fun `progress below the close-to-target threshold is not flagged`() {
        val items = listOf(row(item(expectedUses = 10), actualUses = 5))
        val attention = ValeAttention.summarize(items)
        assertTrue(attention.closeToTarget.isEmpty())
    }

    @Test
    fun `progress at 100 percent is no longer close to target, it's done`() {
        val items = listOf(row(item(expectedUses = 10), actualUses = 10))
        val attention = ValeAttention.summarize(items)
        assertTrue(attention.closeToTarget.isEmpty())
    }

    @Test
    fun `a fully-used old item is ready for its reality check`() {
        val today = LocalDate.now()
        val purchaseDate = today.minusDays(90)
        val items = listOf(row(item(expectedUses = 5, purchaseDate = purchaseDate), actualUses = 5))

        val attention = ValeAttention.summarize(items, today)
        assertEquals(1, attention.readyForRealityCheck.size)
    }

    @Test
    fun `a still-proving item is not ready for its reality check`() {
        val today = LocalDate.now()
        val items = listOf(row(item(expectedUses = 10, purchaseDate = today.minusDays(5)), actualUses = 3))

        val attention = ValeAttention.summarize(items, today)
        assertTrue(attention.readyForRealityCheck.isEmpty())
    }

    @Test
    fun `no items means an empty attention summary`() {
        val attention = ValeAttention.summarize(emptyList())
        assertTrue(attention.isEmpty)
    }
}
