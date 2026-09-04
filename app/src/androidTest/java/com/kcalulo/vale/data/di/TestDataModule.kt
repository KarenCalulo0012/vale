package com.kcalulo.vale.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.kcalulo.vale.core.database.ValeDatabase
import com.kcalulo.vale.core.database.dao.AchievementDao
import com.kcalulo.vale.core.database.dao.ItemDao
import com.kcalulo.vale.core.database.dao.UsageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Replaces [DatabaseModule] for instrumented tests: an in-memory Room database (fresh per
 * process, never touches the real vale.db) and a DataStore backed by a throwaway file, so UI
 * tests never read or pollute a developer's real app data.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ValeDatabase =
        Room.inMemoryDatabaseBuilder(context, ValeDatabase::class.java)
            .allowMainThreadQueries()
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
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile("vale_test_preferences") }
        )
}
