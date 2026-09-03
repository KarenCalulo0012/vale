package com.kcalulo.vale.core.common

import com.kcalulo.vale.core.database.entity.ItemEntity
import com.kcalulo.vale.core.database.entity.ItemStatus

/**
 * Cost-per-use to show in a list row or details header, given the item's status and
 * live usage count (spec §12–§13). Null for a bought item with zero uses — callers
 * show a friendly zero-use state instead.
 */
fun ItemEntity.displayCostPerUseMinor(actualUses: Int): Double? = when (status) {
    ItemStatus.BOUGHT -> ValeCalculations.currentCostPerUse(originalPriceMinor, actualUses)
    else -> targetCostPerUseMinor
}
