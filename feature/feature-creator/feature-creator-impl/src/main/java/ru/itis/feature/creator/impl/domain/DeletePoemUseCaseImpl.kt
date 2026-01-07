package ru.itis.feature.creator.impl.domain

import ru.itis.feature.creator.api.DeletePoemUseCase
import ru.itis.feature.creator.api.UserPoemsRepository
import javax.inject.Inject

class DeletePoemUseCaseImpl @Inject constructor(
    private val repository: UserPoemsRepository
) : DeletePoemUseCase {

    override suspend operator fun invoke(poemId: String) {
        repository.deletePoem(poemId)
    }
}