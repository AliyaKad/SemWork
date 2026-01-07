package ru.itis.feature.favorites.impl.domain

import ru.itis.feature.favorites.api.FavoritesRepository
import ru.itis.feature.favorites.api.IsFavoriteUseCase
import javax.inject.Inject

class IsFavoriteUseCaseImpl @Inject constructor(
    private val repository: FavoritesRepository
) : IsFavoriteUseCase {

    override suspend operator fun invoke(author: String, title: String): Boolean {
        return repository.isFavorite(author, title)
    }
}