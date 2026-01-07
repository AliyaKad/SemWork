package ru.itis.feature.random.api

import ru.itis.core.models.Poem

interface GetRandomPoemUseCase {
    suspend operator fun invoke(): Result<Poem>
}