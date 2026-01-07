package ru.itis.feature.search.impl.domain

import ru.itis.core.models.Poem
import ru.itis.feature.search.api.SearchPoemRepository
import ru.itis.feature.search.api.SearchPoemsUseCase
import javax.inject.Inject

class SearchPoemsUseCaseImpl @Inject constructor(
    private val repository: SearchPoemRepository
) : SearchPoemsUseCase {

    override suspend operator fun invoke(author: String?, title: String?): Result<List<Poem>> {
        return repository.search(author, title)
    }
}