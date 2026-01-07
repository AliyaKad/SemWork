package ru.itis.feature.creator.impl.domain

import kotlinx.coroutines.flow.Flow
import ru.itis.core.models.Poem
import ru.itis.feature.creator.api.ObserveUserPoemsUseCase
import ru.itis.feature.creator.api.UserPoemsRepository
import javax.inject.Inject

class ObserveUserPoemsUseCaseImpl @Inject constructor(
    private val repository: UserPoemsRepository
) : ObserveUserPoemsUseCase {

    override operator fun invoke(): Flow<List<Poem>> {
        return repository.observeAllPoems()
    }
}