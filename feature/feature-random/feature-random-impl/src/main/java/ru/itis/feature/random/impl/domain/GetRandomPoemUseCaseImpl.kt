package ru.itis.feature.random.impl.domain

import ru.itis.core.models.Poem
import ru.itis.feature.random.api.GetRandomPoemUseCase
import ru.itis.feature.random.api.RandomPoemRepository
import javax.inject.Inject

class GetRandomPoemUseCaseImpl @Inject constructor(
    private val repository: RandomPoemRepository
) : GetRandomPoemUseCase {

    override suspend operator fun invoke(): Result<Poem> = repository.getRandomPoem()
}