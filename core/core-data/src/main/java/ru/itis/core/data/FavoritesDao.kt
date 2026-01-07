package ru.itis.core.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(poem: FavoritePoemEntity)

    @Query("DELETE FROM favorite_poems WHERE userId = :userId AND author = :author AND title = :title")
    suspend fun deleteByUserAuthorAndTitle(userId: String, author: String, title: String)

    @Query("SELECT * FROM favorite_poems WHERE userId = :userId")
    suspend fun getAllByUser(userId: String): List<FavoritePoemEntity>

    @Query("SELECT * FROM favorite_poems WHERE userId = :userId")
    fun observeAllByUser(userId: String): Flow<List<FavoritePoemEntity>>

    @Query("SELECT COUNT(*) FROM favorite_poems WHERE userId = :userId AND author = :author AND title = :title")
    suspend fun countByUserAuthorAndTitle(userId: String, author: String, title: String): Int

    @Query("DELETE FROM favorite_poems WHERE userId = :userId")
    suspend fun clearUserFavorites(userId: String)
}