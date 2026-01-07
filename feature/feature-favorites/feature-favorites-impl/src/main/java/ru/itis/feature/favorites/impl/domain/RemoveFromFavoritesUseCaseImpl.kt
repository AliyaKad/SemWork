package ru.itis.feature.favorites.impl.domain

import ru.itis.feature.favorites.api.FavoritesRepository
import ru.itis.feature.favorites.api.RemoveFromFavoritesUseCase
import javax.inject.Inject

class RemoveFromFavoritesUseCaseImpl @Inject constructor(
    private val repository: FavoritesRepository
) : RemoveFromFavoritesUseCase {

    override suspend operator fun invoke(author: String, title: String) {
        repository.removeFromFavorites(author, title)
    }
}