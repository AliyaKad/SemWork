package ru.itis.feature.favorites.api

import ru.itis.core.models.Poem

interface ToggleFavoriteUseCase {
    suspend operator fun invoke(poem: Poem)
}