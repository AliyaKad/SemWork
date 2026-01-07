package ru.itis.core.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoritePoemEntity::class, UserPoemEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun userPoemsDao(): UserPoemsDao
    abstract fun userDao(): UserDao
}