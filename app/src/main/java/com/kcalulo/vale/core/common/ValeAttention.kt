package com.kcalulo.vale.core.common

import com.kcalulo.vale.core.database.dao.ItemWithLastUse
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** One row in an Attention section — a plain string detail, formatted by the caller-free rule (no money here). */
data class AttentionItem(val itemId: Long, val itemName: String, val detail: String)

/** Home's "what needs attention" groups (spec §6). Any group may be empty. */
data class HomeAttention(
    val notUsedRecently: List<AttentionItem>,
    val closeToTarget: List<AttentionItem>,
    val readyForRealityCheck: List<AttentionItem>,
) {
    val isEmpty: Boolean = notUsedRecently.isEmpty() && closeToTarget.isEmpty() && readyForRealityCheck.isEmpty()
}

/**
 * Pure derivation of Home's Attention section (spec §6) from bought items only — nothing
 * here needs Reality Check data to exist, just usage timestamps and the progress ratio
 * that's been available since Phase 0.
 */
object ValeAttention {

    /** Days since last activity (a use, or the purchase if never used) before it's stale. */
    const val NOT_USED_RECENTLY_DAYS = 14L

    /** Progress in [this, 1.0) counts as "close" — already counted once it hits 100%. */
    const val CLOSE_TO_TARGET_MIN_PROGRESS = 0.8f

    fun summarize(items: List<ItemWithLastUse>, today: LocalDate = LocalDate.now()): HomeAttention {
        val notUsedRecently = items.mapNotNull { entry -> notUsedRecently(entry, today) }
        val closeToTarget = items.mapNotNull { entry -> closeToTarget(entry) }
        val readyForRealityCheck = items.mapNotNull { entry -> readyForRealityCheck(entry, today) }
        return HomeAttention(notUsedRecently, closeToTarget, readyForRealityCheck)
    }

    private fun notUsedRecently(entry: ItemWithLastUse, today: LocalDate): AttentionItem? {
        if (ValeCalculations.progress(entry.actualUses, entry.item.expectedUses) >= 1f) return null
        val lastActivity = entry.lastUsedAt?.toLocalDate() ?: entry.item.purchaseDate ?: return null
        val daysSince = ChronoUnit.DAYS.between(lastActivity, today)
        if (daysSince < NOT_USED_RECENTLY_DAYS) return null
        return AttentionItem(entry.item.id, entry.item.name, "Not used in $daysSince days")
    }

    private fun closeToTarget(entry: ItemWithLastUse): AttentionItem? {
        val progress = ValeCalculations.progress(entry.actualUses, entry.item.expectedUses)
        if (progress < CLOSE_TO_TARGET_MIN_PROGRESS || progress >= 1f) return null
        return AttentionItem(entry.item.id, entry.item.name, "${entry.actualUses} / ${entry.item.expectedUses} uses")
    }

    private fun readyForRealityCheck(entry: ItemWithLastUse, today: LocalDate): AttentionItem? {
        val purchaseDate = entry.item.purchaseDate ?: return null
        val daysSince = ChronoUnit.DAYS.between(purchaseDate, today)
        val state = ValeCalculations.realityCheckState(entry.actualUses, entry.item.expectedUses, daysSince)
        val detail = when (state) {
            RealityCheckState.WORTH_IT -> "Worth it — take the Reality Check"
            RealityCheckState.MATH_ISNT_WORKING -> "Math isn't mathing — take the Reality Check"
            RealityCheckState.STILL_PROVING_IT -> return null
        }
        return AttentionItem(entry.item.id, entry.item.name, detail)
    }

    private fun Instant.toLocalDate(): LocalDate = atZone(ZoneId.systemDefault()).toLocalDate()
}
