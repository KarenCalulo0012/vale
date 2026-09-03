package com.kcalulo.vale.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * One unlocked achievement (spec §21). [id] is an [com.kcalulo.vale.core.common.AchievementId]
 * name — presence of a row means it's unlocked, permanently; achievements never re-lock.
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val unlockedAt: Instant,
)
