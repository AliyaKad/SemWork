package ru.itis.feature.favorites.api

import ru.itis.core.models.Poem
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    suspend fun addToFavorites(poem: Poem)
    suspend fun removeFromFavorites(author: String, title: String)
    suspend fun getFavorites(): List<Poem>
    suspend fun isFavorite(author: String, title: String): Boolean
    fun observeFavorites(): Flow<List<Poem>>
}