package ru.itis.feature.favorites.api

interface IsFavoriteUseCase {
    suspend operator fun invoke(author: String, title: String): Boolean
}