package com.kcalulo.vale.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

/** Item lifecycle status (spec §26). */
enum class ItemStatus {
    CONSIDERING,
    BOUGHT,
    SKIPPED,
    SOLD,
    GIVEN_AWAY,
    COMPLETED,
    ARCHIVED,
}

/** Optional item category — a Should Have, but the column exists from day one. */
enum class ItemCategory(val label: String, val emoji: String) {
    CLOTHING("Clothing", "👗"),
    BEAUTY("Beauty", "💄"),
    TECH("Tech", "📱"),
    HOME("Home", "🏠"),
    KITCHEN("Kitchen", "🍳"),
    FITNESS("Fitness", "🏋️"),
    HOBBY("Hobby", "🎨"),
    OTHER("Other", "🛍️"),
}

/** Why a purchase was skipped (spec §11). */
enum class SkipReason(val label: String) {
    TOO_EXPENSIVE("Too expensive"),
    DONT_NEED_IT("Don't need it"),
    HAVE_SIMILAR("Have something similar"),
    CHANGED_MY_MIND("Changed my mind"),
    OTHER("Other"),
}

/**
 * One evaluated item (spec §26). Money lives in minor currency units.
 * Derived values (current cost/use, progress) are computed, not persisted;
 * [targetCostPerUseMinor] is stored because it's the frozen original promise —
 * it must survive later price edits.
 */
@Entity(
    tableName = "items",
    indices = [Index("status"), Index("createdAt")]
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: ItemCategory? = null,
    val imageUri: String? = null,
    val originalPriceMinor: Long,
    val purchaseDate: LocalDate? = null,
    val createdAt: Instant,
    val expectedUses: Int,
    val targetCostPerUseMinor: Double,
    val status: ItemStatus,
    val skipReason: SkipReason? = null,
    val soldPriceMinor: Long? = null,
    val soldDate: LocalDate? = null,
    val givenAwayDate: LocalDate? = null,
    val note: String? = null,
    val isArchived: Boolean = false,
)
