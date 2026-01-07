package ru.itis.feature.search.api

import ru.itis.core.models.Poem


interface SearchPoemRepository {
    suspend fun search(author: String?, title: String?): Result<List<Poem>>
}