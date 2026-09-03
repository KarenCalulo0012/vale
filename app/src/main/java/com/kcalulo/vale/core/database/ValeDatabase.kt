package com.kcalulo.vale.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kcalulo.vale.core.database.dao.ItemDao
import com.kcalulo.vale.core.database.dao.UsageDao
import com.kcalulo.vale.core.database.entity.ItemEntity
import com.kcalulo.vale.core.database.entity.UsageEntity

@Database(
    entities = [ItemEntity::class, UsageEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(ValeTypeConverters::class)
abstract class ValeDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun usageDao(): UsageDao

    companion object {
        const val NAME = "vale.db"
    }
}
