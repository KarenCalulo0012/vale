package com.kcalulo.vale.core.common

import kotlin.math.roundToLong

/**
 * Reality Check verdict states (spec §16).
 */
enum class RealityCheckState {
    /** Target usage reached or exceeded. */
    WORTH_IT,

    /** Item is still progressing toward the target. */
    STILL_PROVING_IT,

    /** Usage behavior indicates the original assumption is significantly behind. */
    MATH_ISNT_WORKING,
}

/**
 * Deterministic Result verdict (spec §8). Copy and mascot vary by verdict;
 * it never pretends cost/use alone decides whether an item is good.
 */
enum class ResultVerdict {
    /** A solid usage commitment — the math looks believable. */
    APPROVED,

    /** Nothing suspicious, but the item still has to prove itself. */
    NEUTRAL,

    /** Very few expected uses — is this purchase really about the math? */
    QUESTIONABLE,
}

/**
 * Pure business rules for VALE (spec §27). All money values are in minor
 * currency units (e.g. centavos) as Long; cost-per-use values are Double
 * minor units so fractions like ₱62.50 survive.
 *
 * Serious data underneath. Silly experience on top.
 */
object ValeCalculations {

    /** Days after purchase before a badly-behind item is called out. */
    const val MATH_NOT_WORKING_MIN_AGE_DAYS: Long = 60

    /** Progress below this fraction (after the min age) means the math isn't mathing. */
    const val MATH_NOT_WORKING_MAX_PROGRESS: Float = 1f / 3f

    /** `targetCostPerUse = purchasePrice / expectedUses` */
    fun targetCostPerUse(priceMinor: Long, expectedUses: Int): Double {
        require(expectedUses > 0) { "expectedUses must be > 0" }
        return priceMinor.toDouble() / expectedUses
    }

    /**
     * `currentCostPerUse = effectiveCost / actualUses`; null when there are no
     * uses yet — callers show a friendly zero-use state instead of dividing by zero.
     */
    fun currentCostPerUse(effectiveCostMinor: Long, actualUses: Int): Double? =
        if (actualUses <= 0) null else effectiveCostMinor.toDouble() / actualUses

    /** `progress = actualUses / expectedUses`, uncapped (120% is real information). */
    fun progress(actualUses: Int, expectedUses: Int): Float =
        if (expectedUses <= 0) 0f else actualUses.toFloat() / expectedUses

    /** Progress clamped to 0..1 for progress bars. */
    fun progressFraction(actualUses: Int, expectedUses: Int): Float =
        progress(actualUses, expectedUses).coerceIn(0f, 1f)

    /** `remainingUses = max(expectedUses - actualUses, 0)` */
    fun remainingUses(expectedUses: Int, actualUses: Int): Int =
        (expectedUses - actualUses).coerceAtLeast(0)

    /**
     * `targetResalePrice = max(purchasePrice - targetCostPerUse × actualUses, 0)`
     * Never presented as market value — VALE doesn't know the market.
     */
    fun targetResalePriceMinor(
        priceMinor: Long,
        targetCostPerUseMinor: Double,
        actualUses: Int,
    ): Long =
        (priceMinor - targetCostPerUseMinor * actualUses)
            .roundToLong()
            .coerceAtLeast(0L)

    /**
     * `effectiveCost = purchasePrice - soldPrice`. May be negative when the item
     * sold above its purchase price — that's profit, and the UI celebrates it
     * rather than hiding it.
     */
    fun effectiveCostAfterSaleMinor(priceMinor: Long, soldPriceMinor: Long): Long =
        priceMinor - soldPriceMinor

    /** `finalCostPerUse = effectiveCost / actualUses`; null when there are no uses. */
    fun finalCostPerUse(effectiveCostMinor: Long, actualUses: Int): Double? =
        currentCostPerUse(effectiveCostMinor, actualUses)

    /** Expected uses at or above this reads as a real commitment. */
    const val VERDICT_APPROVED_MIN_USES: Int = 20

    /** Expected uses below this looks like wishful math. */
    const val VERDICT_QUESTIONABLE_MAX_USES: Int = 5

    /**
     * Deterministic Result verdict from the planned usage commitment (no AI in V1).
     * A simple, honest heuristic: many planned uses → approved, very few → questionable.
     */
    fun resultVerdict(expectedUses: Int): ResultVerdict = when {
        expectedUses >= VERDICT_APPROVED_MIN_USES -> ResultVerdict.APPROVED
        expectedUses < VERDICT_QUESTIONABLE_MAX_USES -> ResultVerdict.QUESTIONABLE
        else -> ResultVerdict.NEUTRAL
    }

    /**
     * Deterministic Reality Check verdict (no AI in V1, spec §8/§16):
     * worth it once the target is reached; "math isn't working" only when the
     * item is old enough AND far enough behind; otherwise still proving it.
     */
    fun realityCheckState(
        actualUses: Int,
        expectedUses: Int,
        daysSincePurchase: Long,
    ): RealityCheckState {
        if (expectedUses > 0 && actualUses >= expectedUses) return RealityCheckState.WORTH_IT
        val behind = progress(actualUses, expectedUses) < MATH_NOT_WORKING_MAX_PROGRESS
        return if (behind && daysSincePurchase >= MATH_NOT_WORKING_MIN_AGE_DAYS) {
            RealityCheckState.MATH_ISNT_WORKING
        } else {
            RealityCheckState.STILL_PROVING_IT
        }
    }
}
