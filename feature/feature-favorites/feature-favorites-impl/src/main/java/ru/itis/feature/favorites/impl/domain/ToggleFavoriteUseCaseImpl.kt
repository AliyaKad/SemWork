package ru.itis.feature.favorites.impl.domain

import ru.itis.core.models.Poem
import ru.itis.feature.favorites.api.FavoritesRepository
import ru.itis.feature.favorites.api.ToggleFavoriteUseCase
import javax.inject.Inject

class ToggleFavoriteUseCaseImpl @Inject constructor(
    private val repository: FavoritesRepository
) : ToggleFavoriteUseCase {

    override suspend operator fun invoke(poem: Poem) {
        if (repository.isFavorite(poem.author, poem.title)) {
            repository.removeFromFavorites(poem.author, poem.title)
        } else {
            repository.addToFavorites(poem)
        }
    }
}