package com.kcalulo.vale.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.kcalulo.vale.core.database.ValeDatabase
import com.kcalulo.vale.core.database.dao.AchievementDao
import com.kcalulo.vale.core.database.dao.ItemDao
import com.kcalulo.vale.core.database.dao.UsageDao
import com.kcalulo.vale.data.repository.AchievementRepository
import com.kcalulo.vale.data.repository.AchievementRepositoryImpl
import com.kcalulo.vale.data.repository.ItemRepository
import com.kcalulo.vale.data.repository.ItemRepositoryImpl
import com.kcalulo.vale.data.repository.ProgressRepository
import com.kcalulo.vale.data.repository.ProgressRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.valeDataStore: DataStore<Preferences> by preferencesDataStore(name = "vale_preferences")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ValeDatabase =
        Room.databaseBuilder(context, ValeDatabase::class.java, ValeDatabase.NAME)
            // Pre-release: no installs to preserve yet. Replace with a real Migration
            // before release (tracked in docs/BACKLOG.md, spec §32 Phase 5).
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideItemDao(db: ValeDatabase): ItemDao = db.itemDao()

    @Provides
    fun provideUsageDao(db: ValeDatabase): UsageDao = db.usageDao()

    @Provides
    fun provideAchievementDao(db: ValeDatabase): AchievementDao = db.achievementDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.valeDataStore
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository

    @Binds
    abstract fun bindAchievementRepository(impl: AchievementRepositoryImpl): AchievementRepository

    @Binds
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository
}
