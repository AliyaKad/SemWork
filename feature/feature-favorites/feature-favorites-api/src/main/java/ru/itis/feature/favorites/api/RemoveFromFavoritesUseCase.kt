package ru.itis.feature.favorites.api

interface RemoveFromFavoritesUseCase {
    suspend operator fun invoke(author: String, title: String)
}