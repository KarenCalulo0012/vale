package com.kcalulo.vale.core.common

import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.core.database.entity.ItemStatus

/**
 * V1 achievements (spec §21). Achievements live inside Progress, never their own
 * destination; unlocking never rewards purchasing itself (spec §22 principle applies
 * here too — none of these fire from buying).
 */
enum class AchievementId(val title: String, val shortCopy: String, val emoji: String) {
    FIRST_CALC("First Calc", "You did the math. That's the whole point.", "🧮"),
    RESISTED("Resisted", "YOU RESISTED. 🥹", "✋"),
    ACTUALLY_USED_IT("Actually Used It", "Ten logs in — that's real proof of use.", "🔥"),
    MATH_VALIDATED("Math Validated", "The math actually mathed.", "✅"),
    RECOVERED("Recovered", "Sold, not sunk cost.", "💰"),
    LET_IT_GO("Let It Go", "Space recovered. Someone else gets to use it. 💜", "🎁"),
    CONSISTENT_QUEEN("Consistent Queen", "Showing up, over and over. That's the whole game.", "👑"),
}

/**
 * Pure achievement evaluation (spec §21, §27). Deterministic and re-run-safe: given the
 * same items it always earns the same set, so callers can diff against what's already
 * persisted in [com.kcalulo.vale.core.database.dao.AchievementDao] to find only the
 * newly-unlocked ones instead of tracking unlock triggers ad hoc at every call site.
 */
object ValeAchievements {

    /** Total "+ I used it" logs across all items before Actually Used It unlocks. */
    const val USAGE_MILESTONE = 10

    /**
     * Cumulative intentional actions (calculations + usage logs + sales + give-aways)
     * before Consistent Queen unlocks. A proxy for real streak tracking (spec §22,
     * deliberately deferred past this phase) — revisit once streaks land.
     */
    const val CONSISTENT_QUEEN_ACTIVITY_THRESHOLD = 25

    fun earned(items: List<ItemWithUsageCount>): Set<AchievementId> {
        if (items.isEmpty()) return emptySet()

        val skippedCount = items.count { it.item.status == ItemStatus.SKIPPED }
        val soldCount = items.count { it.item.status == ItemStatus.SOLD }
        val givenAwayCount = items.count { it.item.status == ItemStatus.GIVEN_AWAY }
        val totalUsages = items.sumOf { it.actualUses }
        val reachedTargetCount = items.count { entry ->
            ValeCalculations.progress(entry.actualUses, entry.item.expectedUses) >= 1f
        }
        val activity = items.size + totalUsages + soldCount + givenAwayCount

        return buildSet {
            add(AchievementId.FIRST_CALC)
            if (skippedCount >= 1) add(AchievementId.RESISTED)
            if (totalUsages >= USAGE_MILESTONE) add(AchievementId.ACTUALLY_USED_IT)
            if (reachedTargetCount >= 1) add(AchievementId.MATH_VALIDATED)
            if (soldCount >= 1) add(AchievementId.RECOVERED)
            if (givenAwayCount >= 1) add(AchievementId.LET_IT_GO)
            if (activity >= CONSISTENT_QUEEN_ACTIVITY_THRESHOLD) add(AchievementId.CONSISTENT_QUEEN)
        }
    }
}
