package ru.itis.feature.random.api

import ru.itis.core.models.Poem

interface RandomPoemRepository {
    suspend fun getRandomPoem(): Result<Poem>
}