package ru.itis.feature.random.impl.data

import ru.itis.core.models.Poem
import ru.itis.core.network.PoetryApi
import ru.itis.feature.random.api.RandomPoemRepository
import javax.inject.Inject

class RandomPoemRepositoryImpl @Inject constructor(
    private val poetryApi: PoetryApi
) : RandomPoemRepository {

    override suspend fun getRandomPoem(): Result<Poem> = runCatching {
        val response = poetryApi.getRandomPoem()
        if (response.isEmpty()) throw IllegalStateException("Empty response from PoetryDB")
        response.first()
    }
}