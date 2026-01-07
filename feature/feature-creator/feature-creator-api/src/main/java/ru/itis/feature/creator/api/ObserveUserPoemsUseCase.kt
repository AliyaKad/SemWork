package ru.itis.feature.creator.api

import kotlinx.coroutines.flow.Flow
import ru.itis.core.models.Poem

interface ObserveUserPoemsUseCase {
    operator fun invoke(): Flow<List<Poem>>
}
