package ru.itis.feature.favorites.impl.domain

import kotlinx.coroutines.flow.Flow
import ru.itis.core.models.Poem
import ru.itis.feature.favorites.api.FavoritesRepository
import ru.itis.feature.favorites.api.ObserveFavoritesUseCase
import javax.inject.Inject

class ObserveFavoritesUseCaseImpl @Inject constructor(
    private val repository: FavoritesRepository
) : ObserveFavoritesUseCase {

    override operator fun invoke(): Flow<List<Poem>> {
        return repository.observeFavorites()
    }
}