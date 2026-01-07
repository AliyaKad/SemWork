package ru.itis.feature.search.api

import ru.itis.core.models.Poem

interface SearchPoemsUseCase {
    suspend operator fun invoke(author: String?, title: String?): Result<List<Poem>>
}