package ru.itis.feature.favorites.api

import kotlinx.coroutines.flow.Flow
import ru.itis.core.models.Poem

interface ObserveFavoritesUseCase {
    operator fun invoke(): Flow<List<Poem>>
}