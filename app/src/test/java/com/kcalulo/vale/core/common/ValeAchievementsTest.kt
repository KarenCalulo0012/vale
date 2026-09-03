package com.kcalulo.vale.core.common

import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.core.database.entity.ItemCategory
import com.kcalulo.vale.core.database.entity.ItemEntity
import com.kcalulo.vale.core.database.entity.ItemStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValeAchievementsTest {

    private fun item(
        status: ItemStatus,
        expectedUses: Int = 10,
        price: Long = 100_000,
    ) = ItemEntity(
        name = "Test Item",
        category = ItemCategory.OTHER,
        originalPriceMinor = price,
        createdAt = Instant.now(),
        expectedUses = expectedUses,
        targetCostPerUseMinor = ValeCalculations.targetCostPerUse(price, expectedUses),
        status = status,
    )

    private fun row(item: ItemEntity, actualUses: Int = 0) = ItemWithUsageCount(item, actualUses)

    @Test
    fun `no items unlocks nothing`() {
        assertTrue(ValeAchievements.earned(emptyList()).isEmpty())
    }

    @Test
    fun `any item unlocks First Calc`() {
        val earned = ValeAchievements.earned(listOf(row(item(ItemStatus.CONSIDERING))))
        assertTrue(AchievementId.FIRST_CALC in earned)
    }

    @Test
    fun `a skipped item unlocks Resisted but not Recovered or Let It Go`() {
        val earned = ValeAchievements.earned(listOf(row(item(ItemStatus.SKIPPED))))
        assertTrue(AchievementId.RESISTED in earned)
        assertFalse(AchievementId.RECOVERED in earned)
        assertFalse(AchievementId.LET_IT_GO in earned)
    }

    @Test
    fun `usage below the milestone does not unlock Actually Used It`() {
        val earned = ValeAchievements.earned(
            listOf(row(item(ItemStatus.BOUGHT), actualUses = ValeAchievements.USAGE_MILESTONE - 1))
        )
        assertFalse(AchievementId.ACTUALLY_USED_IT in earned)
    }

    @Test
    fun `usage at the milestone unlocks Actually Used It`() {
        val earned = ValeAchievements.earned(
            listOf(row(item(ItemStatus.BOUGHT), actualUses = ValeAchievements.USAGE_MILESTONE))
        )
        assertTrue(AchievementId.ACTUALLY_USED_IT in earned)
    }

    @Test
    fun `reaching expected uses unlocks Math Validated`() {
        val earned = ValeAchievements.earned(
            listOf(row(item(ItemStatus.BOUGHT, expectedUses = 10), actualUses = 10))
        )
        assertTrue(AchievementId.MATH_VALIDATED in earned)
    }

    @Test
    fun `a sold item unlocks Recovered`() {
        val earned = ValeAchievements.earned(listOf(row(item(ItemStatus.SOLD))))
        assertTrue(AchievementId.RECOVERED in earned)
    }

    @Test
    fun `a given-away item unlocks Let It Go`() {
        val earned = ValeAchievements.earned(listOf(row(item(ItemStatus.GIVEN_AWAY))))
        assertTrue(AchievementId.LET_IT_GO in earned)
    }

    @Test
    fun `buying alone never unlocks anything except First Calc`() {
        val earned = ValeAchievements.earned(listOf(row(item(ItemStatus.BOUGHT), actualUses = 0)))
        assertEquals(setOf(AchievementId.FIRST_CALC), earned)
    }

    @Test
    fun `cumulative activity at the threshold unlocks Consistent Queen`() {
        val items = (1..ValeAchievements.CONSISTENT_QUEEN_ACTIVITY_THRESHOLD).map {
            row(item(ItemStatus.CONSIDERING))
        }
        assertTrue(AchievementId.CONSISTENT_QUEEN in ValeAchievements.earned(items))
    }

    @Test
    fun `cumulative activity below the threshold does not unlock Consistent Queen`() {
        val items = (1 until ValeAchievements.CONSISTENT_QUEEN_ACTIVITY_THRESHOLD).map {
            row(item(ItemStatus.CONSIDERING))
        }
        assertFalse(AchievementId.CONSISTENT_QUEEN in ValeAchievements.earned(items))
    }
}
