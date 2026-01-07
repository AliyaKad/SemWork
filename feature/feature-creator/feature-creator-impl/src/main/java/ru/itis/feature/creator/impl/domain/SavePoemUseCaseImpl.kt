package ru.itis.feature.creator.impl.domain

import ru.itis.core.models.Poem
import ru.itis.feature.creator.api.SavePoemUseCase
import ru.itis.feature.creator.api.UserPoemsRepository
import javax.inject.Inject

class SavePoemUseCaseImpl @Inject constructor(
    private val repository: UserPoemsRepository
) : SavePoemUseCase {

    override suspend operator fun invoke(poem: Poem) {
        repository.savePoem(poem)
    }
}