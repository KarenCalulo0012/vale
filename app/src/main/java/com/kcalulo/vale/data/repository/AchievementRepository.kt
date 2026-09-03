package com.kcalulo.vale.data.repository

import com.kcalulo.vale.core.common.AchievementId
import com.kcalulo.vale.core.common.ValeAchievements
import com.kcalulo.vale.core.database.dao.AchievementDao
import com.kcalulo.vale.core.database.dao.ItemDao
import com.kcalulo.vale.core.database.entity.AchievementEntity
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Tracks which V1 achievements (spec §21) have unlocked. [refreshCheck] is the single
 * choke point for detecting new unlocks — [ItemRepositoryImpl] calls it after every
 * mutation (save, update, log usage) instead of each of the six feature ViewModels
 * knowing about achievements individually.
 */
interface AchievementRepository {
    fun observeUnlocked(): Flow<Set<AchievementId>>

    /** Emits once per achievement the moment it unlocks — drives the celebration popup. */
    val newlyUnlocked: Flow<AchievementId>

    /** Re-evaluates all achievements against current data and persists any new unlocks. */
    suspend fun refreshCheck()
}

@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val achievementDao: AchievementDao,
    private val itemDao: ItemDao,
) : AchievementRepository {

    private val _newlyUnlocked = MutableSharedFlow<AchievementId>(extraBufferCapacity = 4)
    override val newlyUnlocked = _newlyUnlocked.asSharedFlow()

    override fun observeUnlocked(): Flow<Set<AchievementId>> =
        achievementDao.observeUnlocked().map { unlocked ->
            unlocked.mapNotNull { runCatching { AchievementId.valueOf(it.id) }.getOrNull() }.toSet()
        }

    override suspend fun refreshCheck() {
        val items = itemDao.observeAllItems().first()
        val earned = ValeAchievements.earned(items)
        val alreadyUnlocked = achievementDao.getUnlockedIds().toSet()

        for (id in earned) {
            if (id.name in alreadyUnlocked) continue
            achievementDao.insert(AchievementEntity(id = id.name, unlockedAt = Instant.now()))
            _newlyUnlocked.tryEmit(id)
        }
    }
}
