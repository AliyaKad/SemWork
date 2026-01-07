package ru.itis.feature.creator.api

import ru.itis.core.models.Poem

interface GetUserPoemsUseCase {
    suspend operator fun invoke(): List<Poem>
}