package com.kcalulo.vale.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kcalulo.vale.core.database.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    /** Ignored if already unlocked — achievements never re-lock or overwrite their timestamp. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements ORDER BY unlockedAt ASC")
    fun observeUnlocked(): Flow<List<AchievementEntity>>

    @Query("SELECT id FROM achievements")
    suspend fun getUnlockedIds(): List<String>
}
