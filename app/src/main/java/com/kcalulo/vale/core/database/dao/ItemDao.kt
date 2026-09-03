package com.kcalulo.vale.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kcalulo.vale.core.database.entity.ItemEntity
import com.kcalulo.vale.core.database.entity.ItemStatus
import kotlinx.coroutines.flow.Flow

/** Item row joined with its live usage count — derived, never persisted. */
data class ItemWithUsageCount(
    @Embedded val item: ItemEntity,
    val actualUses: Int,
)

private const val ITEM_WITH_USES =
    "SELECT items.*, (SELECT COUNT(*) FROM usages WHERE usages.itemId = items.id) AS actualUses FROM items"

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity)

    @Delete
    suspend fun delete(item: ItemEntity)

    @Query("SELECT * FROM items WHERE id = :id")
    fun observeItem(id: Long): Flow<ItemEntity?>

    @Query("$ITEM_WITH_USES WHERE items.id = :id")
    fun observeItemWithUses(id: Long): Flow<ItemWithUsageCount?>

    @Query("$ITEM_WITH_USES WHERE items.isArchived = 0 ORDER BY items.createdAt DESC")
    fun observeItems(): Flow<List<ItemWithUsageCount>>

    @Query("$ITEM_WITH_USES WHERE items.isArchived = 1 ORDER BY items.createdAt DESC")
    fun observeArchivedItems(): Flow<List<ItemWithUsageCount>>

    /** Every item regardless of archive status — Progress/Achievements read the whole history. */
    @Query("$ITEM_WITH_USES ORDER BY items.createdAt DESC")
    fun observeAllItems(): Flow<List<ItemWithUsageCount>>

    @Query("$ITEM_WITH_USES WHERE items.status = :status AND items.isArchived = 0 ORDER BY items.createdAt DESC")
    fun observeItemsByStatus(status: ItemStatus): Flow<List<ItemWithUsageCount>>

    @Query("$ITEM_WITH_USES WHERE items.isArchived = 0 ORDER BY items.createdAt DESC LIMIT :limit")
    fun observeRecentItems(limit: Int): Flow<List<ItemWithUsageCount>>

    @Query(
        "$ITEM_WITH_USES WHERE items.name LIKE '%' || :query || '%' AND items.isArchived = 0 " +
            "ORDER BY items.createdAt DESC"
    )
    fun searchItems(query: String): Flow<List<ItemWithUsageCount>>

    @Query("SELECT COUNT(*) FROM items WHERE status = :status")
    fun observeCountByStatus(status: ItemStatus): Flow<Int>
}
