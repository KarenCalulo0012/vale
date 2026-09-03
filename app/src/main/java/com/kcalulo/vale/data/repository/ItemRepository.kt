package com.kcalulo.vale.data.repository

import com.kcalulo.vale.core.database.dao.ItemDao
import com.kcalulo.vale.core.database.dao.ItemWithLastUse
import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.core.database.dao.UsageDao
import com.kcalulo.vale.core.database.entity.ItemEntity
import com.kcalulo.vale.core.database.entity.ItemStatus
import com.kcalulo.vale.core.database.entity.UsageEntity
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/** Single source of truth for items and their usage records. */
interface ItemRepository {
    fun observeItems(): Flow<List<ItemWithUsageCount>>
    fun observeArchivedItems(): Flow<List<ItemWithUsageCount>>
    fun observeItemsByStatus(status: ItemStatus): Flow<List<ItemWithUsageCount>>
    fun observeRecentItems(limit: Int = 5): Flow<List<ItemWithUsageCount>>
    fun observeItem(id: Long): Flow<ItemWithUsageCount?>
    fun searchItems(query: String): Flow<List<ItemWithUsageCount>>
    fun observeCountByStatus(status: ItemStatus): Flow<Int>
    fun observeBoughtItemsWithLastUse(): Flow<List<ItemWithLastUse>>
    suspend fun saveItem(item: ItemEntity): Long
    suspend fun updateItem(item: ItemEntity)
    suspend fun deleteItem(item: ItemEntity)

    fun observeUsages(itemId: Long): Flow<List<UsageEntity>>

    /** Logs one use now (or at [usedAt]); returns the usage id so undo can delete it. */
    suspend fun logUsage(itemId: Long, usedAt: Instant = Instant.now()): Long
    suspend fun removeUsage(usageId: Long)
}

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao,
    private val usageDao: UsageDao,
    private val achievementRepository: AchievementRepository,
) : ItemRepository {

    override fun observeItems() = itemDao.observeItems()

    override fun observeArchivedItems() = itemDao.observeArchivedItems()

    override fun observeItemsByStatus(status: ItemStatus) = itemDao.observeItemsByStatus(status)

    override fun observeRecentItems(limit: Int) = itemDao.observeRecentItems(limit)

    override fun observeItem(id: Long) = itemDao.observeItemWithUses(id)

    override fun searchItems(query: String) = itemDao.searchItems(query)

    override fun observeCountByStatus(status: ItemStatus) = itemDao.observeCountByStatus(status)

    override fun observeBoughtItemsWithLastUse() = itemDao.observeBoughtItemsWithLastUse()

    override suspend fun saveItem(item: ItemEntity): Long {
        val id = itemDao.insert(item)
        achievementRepository.refreshCheck()
        return id
    }

    override suspend fun updateItem(item: ItemEntity) {
        itemDao.update(item)
        achievementRepository.refreshCheck()
    }

    override suspend fun deleteItem(item: ItemEntity) = itemDao.delete(item)

    override fun observeUsages(itemId: Long) = usageDao.observeUsagesForItem(itemId)

    override suspend fun logUsage(itemId: Long, usedAt: Instant): Long {
        val usageId = usageDao.insert(UsageEntity(itemId = itemId, usedAt = usedAt))
        achievementRepository.refreshCheck()
        return usageId
    }

    override suspend fun removeUsage(usageId: Long) = usageDao.deleteById(usageId)
}
