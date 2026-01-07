package ru.itis.feature.creator.api

import kotlinx.coroutines.flow.Flow
import ru.itis.core.models.Poem

interface UserPoemsRepository {
    suspend fun savePoem(poem: Poem)
    suspend fun deletePoem(author: String, title: String)
    suspend fun getAllPoems(): List<Poem>
    fun observeAllPoems(): Flow<List<Poem>>
    suspend fun getPoemsCount(): Int
}