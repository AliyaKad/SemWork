package ru.itis.core.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPoemsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(poem: UserPoemEntity)

    @Query("DELETE FROM user_poems WHERE id = :poemId AND userId = :userId")
    suspend fun deleteById(userId: String, poemId: String)

    @Query("SELECT * FROM user_poems WHERE userId = :userId")
    suspend fun getAllByUser(userId: String): List<UserPoemEntity>

    @Query("SELECT * FROM user_poems WHERE userId = :userId")
    fun observeAllByUser(userId: String): Flow<List<UserPoemEntity>>

    @Query("SELECT COUNT(*) FROM user_poems WHERE id = :poemId AND userId = :userId")
    suspend fun countById(userId: String, poemId: String): Int

    @Query("DELETE FROM user_poems WHERE userId = :userId")
    suspend fun clearUserPoems(userId: String)
}