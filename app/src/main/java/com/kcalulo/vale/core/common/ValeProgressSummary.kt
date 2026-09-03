package com.kcalulo.vale.core.common

import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.core.database.entity.ItemStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Lifecycle counts across every item, regardless of month (spec §20 Overview). */
data class ProgressOverview(
    val thingsChecked: Int,
    val bought: Int,
    val skipped: Int,
    val provenWorthIt: Int,
    val stillProving: Int,
    val sold: Int,
    val givenAway: Int,
)

/**
 * This-month counts (spec §20 Monthly snapshot). [mathValidated] is all-time, not
 * monthly — VALE doesn't persist *when* an item reached its target, only its current
 * usage count, so it can't be scoped to a month without adding a stored "reached at"
 * date. Flagged in docs/BACKLOG.md rather than guessing.
 */
data class ProgressMonthlySnapshot(
    val calculationsMade: Int,
    val purchasesMade: Int,
    val purchasesSkipped: Int,
    val usageLogs: Int,
    val mathValidated: Int,
)

/**
 * One highlight card. [value] means something different per highlight: minor-unit
 * cost/use for [ProgressHighlights.bestMath], a 0f+ progress ratio for
 * [ProgressHighlights.questionableMath], minor-unit price for [ProgressHighlights.bestSkip].
 * Callers format each one appropriately for its own card.
 */
data class ProgressHighlight(
    val itemId: Long,
    val itemName: String,
    val value: Double,
)

/** Deterministic Progress highlights (spec §20). Null when no item qualifies yet. */
data class ProgressHighlights(
    val bestMath: ProgressHighlight?,
    val questionableMath: ProgressHighlight?,
    val bestSkip: ProgressHighlight?,
)

/**
 * Pure aggregation over the full item history (spec §20, §27). Reward-safe by
 * construction: nothing here counts a purchase itself as an achievement of any kind,
 * only usage, skipping, selling, and giving away.
 */
object ValeProgressSummary {

    fun overview(items: List<ItemWithUsageCount>): ProgressOverview {
        val provenWorthIt = items.count { reachedTarget(it) }
        return ProgressOverview(
            thingsChecked = items.size,
            bought = items.count { it.item.status == ItemStatus.BOUGHT },
            skipped = items.count { it.item.status == ItemStatus.SKIPPED },
            provenWorthIt = provenWorthIt,
            stillProving = items.count { it.item.status == ItemStatus.BOUGHT && !reachedTarget(it) },
            sold = items.count { it.item.status == ItemStatus.SOLD },
            givenAway = items.count { it.item.status == ItemStatus.GIVEN_AWAY },
        )
    }

    fun monthlySnapshot(
        items: List<ItemWithUsageCount>,
        usageLogsThisMonth: Int,
        now: LocalDate = LocalDate.now(),
    ): ProgressMonthlySnapshot {
        fun inMonth(date: LocalDate?) = date != null && date.year == now.year && date.month == now.month
        fun inMonth(instant: Instant) = inMonth(instant.atZone(ZoneId.systemDefault()).toLocalDate())

        return ProgressMonthlySnapshot(
            calculationsMade = items.count { inMonth(it.item.createdAt) },
            purchasesMade = items.count { inMonth(it.item.purchaseDate) },
            purchasesSkipped = items.count {
                it.item.status == ItemStatus.SKIPPED && inMonth(it.item.createdAt)
            },
            usageLogs = usageLogsThisMonth,
            mathValidated = items.count { reachedTarget(it) },
        )
    }

    fun highlights(items: List<ItemWithUsageCount>, today: LocalDate = LocalDate.now()): ProgressHighlights {
        val bestMath = items
            .asSequence()
            .filter { it.actualUses > 0 && it.item.status in COST_TRACKED_STATUSES }
            .mapNotNull { entry -> entry.item.displayCostPerUseMinor(entry.actualUses)?.let { entry to it } }
            .minByOrNull { it.second }
            ?.let { (entry, cost) -> ProgressHighlight(entry.item.id, entry.item.name, cost) }

        val questionableMath = items
            .asSequence()
            .filter { it.item.status == ItemStatus.BOUGHT && it.item.purchaseDate != null }
            .filter { entry ->
                val daysSince = ChronoUnit.DAYS.between(entry.item.purchaseDate, today)
                ValeCalculations.realityCheckState(entry.actualUses, entry.item.expectedUses, daysSince) ==
                    RealityCheckState.MATH_ISNT_WORKING
            }
            .minByOrNull { ValeCalculations.progress(it.actualUses, it.item.expectedUses) }
            ?.let { entry ->
                ProgressHighlight(
                    entry.item.id,
                    entry.item.name,
                    ValeCalculations.progress(entry.actualUses, entry.item.expectedUses).toDouble(),
                )
            }

        val bestSkip = items
            .filter { it.item.status == ItemStatus.SKIPPED }
            .maxByOrNull { it.item.originalPriceMinor }
            ?.let { entry -> ProgressHighlight(entry.item.id, entry.item.name, entry.item.originalPriceMinor.toDouble()) }

        return ProgressHighlights(bestMath, questionableMath, bestSkip)
    }

    private val COST_TRACKED_STATUSES = setOf(ItemStatus.BOUGHT, ItemStatus.SOLD)

    private fun reachedTarget(entry: ItemWithUsageCount): Boolean =
        ValeCalculations.progress(entry.actualUses, entry.item.expectedUses) >= 1f
}
