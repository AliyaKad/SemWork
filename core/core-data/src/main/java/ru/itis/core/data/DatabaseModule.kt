package ru.itis.core.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFavoritesDao(database: AppDatabase) = database.favoritesDao()

    @Provides
    @Singleton
    fun provideUserPoemsDao(database: AppDatabase) = database.userPoemsDao()

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase) = database.userDao()
}