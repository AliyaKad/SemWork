package ru.itis.feature.favorites.impl.domain

import ru.itis.core.models.Poem
import ru.itis.feature.favorites.api.AddToFavoritesUseCase
import ru.itis.feature.favorites.api.FavoritesRepository
import javax.inject.Inject

class AddToFavoritesUseCaseImpl @Inject constructor(
    private val repository: FavoritesRepository
) : AddToFavoritesUseCase {

    override suspend operator fun invoke(poem: Poem) {
        repository.addToFavorites(poem)
    }
}