package ru.itis.feature.creator.impl.domain

import ru.itis.core.models.Poem
import ru.itis.feature.creator.api.GetUserPoemsUseCase
import ru.itis.feature.creator.api.UserPoemsRepository
import javax.inject.Inject

class GetUserPoemsUseCaseImpl @Inject constructor(
    private val repository: UserPoemsRepository
) : GetUserPoemsUseCase {

    override suspend operator fun invoke(): List<Poem> {
        return repository.getAllPoems()
    }
}