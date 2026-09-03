package com.kcalulo.vale.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kcalulo.vale.core.database.entity.UsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {

    @Insert
    suspend fun insert(usage: UsageEntity): Long

    @Query("DELETE FROM usages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM usages WHERE itemId = :itemId ORDER BY usedAt DESC")
    fun observeUsagesForItem(itemId: Long): Flow<List<UsageEntity>>

    @Query("SELECT COUNT(*) FROM usages WHERE itemId = :itemId")
    fun observeUsageCount(itemId: Long): Flow<Int>
}
