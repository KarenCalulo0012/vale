package com.kcalulo.vale.core.common

import com.kcalulo.vale.core.database.entity.ItemEntity
import com.kcalulo.vale.core.database.entity.ItemStatus
import com.kcalulo.vale.core.database.entity.SkipReason
import java.time.LocalDate

/**
 * Cost-per-use to show in a list row or details header, given the item's status and
 * live usage count (spec §12–§13). Null for a bought item with zero uses — callers
 * show a friendly zero-use state instead.
 */
fun ItemEntity.displayCostPerUseMinor(actualUses: Int): Double? = when (status) {
    ItemStatus.BOUGHT -> ValeCalculations.currentCostPerUse(originalPriceMinor, actualUses)
    ItemStatus.SOLD -> soldPriceMinor?.let { sold ->
        ValeCalculations.finalCostPerUse(
            ValeCalculations.effectiveCostAfterSaleMinor(originalPriceMinor, sold),
            actualUses,
        )
    }
    else -> targetCostPerUseMinor
}

/**
 * One-line status summary for a list row (Home recent items, Things), e.g.
 * "12 / 30 uses", "Skipped — ₱3,500 not spent", "Sold for ₱200". Every [ItemStatus] is
 * handled explicitly (no catch-all "else") so a new status can't silently mislabel itself
 * as Skipped the way a wildcard branch would.
 */
fun ItemEntity.summaryLine(actualUses: Int, symbol: String): String = when (status) {
    ItemStatus.BOUGHT -> "$actualUses / $expectedUses uses"
    ItemStatus.CONSIDERING -> "Planned: $expectedUses uses"
    ItemStatus.SKIPPED -> "Skipped — ${MoneyFormat.format(originalPriceMinor, symbol)} not spent"
    ItemStatus.SOLD -> "Sold for ${soldPriceMinor?.let { MoneyFormat.format(it, symbol) } ?: "—"}"
    ItemStatus.GIVEN_AWAY -> "Given away"
    ItemStatus.COMPLETED -> "Completed"
    ItemStatus.ARCHIVED -> "Archived"
}

/** Considering → Bought (spec §10). Defaults to today; spec §9 allows an earlier purchase date. */
fun ItemEntity.asBought(purchaseDate: LocalDate = LocalDate.now()): ItemEntity =
    copy(status = ItemStatus.BOUGHT, purchaseDate = purchaseDate)

/** Considering → Skipped (spec §10) or the initial Result-screen skip decision (spec §11). */
fun ItemEntity.asSkipped(reason: SkipReason?): ItemEntity =
    copy(status = ItemStatus.SKIPPED, skipReason = reason)

/** Sell flow (spec §18) — stops normal usage tracking, preserves full history. */
fun ItemEntity.asSold(soldPriceMinor: Long): ItemEntity =
    copy(status = ItemStatus.SOLD, soldPriceMinor = soldPriceMinor, soldDate = LocalDate.now())

/** Give Away flow (spec §19) — never treated as profit or savings, just a closed lifecycle. */
fun ItemEntity.asGivenAway(note: String?): ItemEntity =
    copy(status = ItemStatus.GIVEN_AWAY, givenAwayDate = LocalDate.now(), note = note?.takeIf { it.isNotBlank() })
