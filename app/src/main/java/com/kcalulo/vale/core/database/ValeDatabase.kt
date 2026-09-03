package com.kcalulo.vale.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kcalulo.vale.core.database.dao.AchievementDao
import com.kcalulo.vale.core.database.dao.ItemDao
import com.kcalulo.vale.core.database.dao.UsageDao
import com.kcalulo.vale.core.database.entity.AchievementEntity
import com.kcalulo.vale.core.database.entity.ItemEntity
import com.kcalulo.vale.core.database.entity.UsageEntity

@Database(
    entities = [ItemEntity::class, UsageEntity::class, AchievementEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(ValeTypeConverters::class)
abstract class ValeDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun usageDao(): UsageDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        const val NAME = "vale.db"

        /** Added [AchievementEntity] to persist unlocked milestones (spec §21). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `achievements` (`id` TEXT NOT NULL, " +
                        "`unlockedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
    }
}
