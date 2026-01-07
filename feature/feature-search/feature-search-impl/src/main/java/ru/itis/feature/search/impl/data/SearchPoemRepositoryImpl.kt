package ru.itis.feature.search.impl.data

import ru.itis.core.network.PoetryApi
import ru.itis.feature.search.api.SearchPoemRepository
import ru.itis.core.models.Poem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchPoemRepositoryImpl @Inject constructor(
    private val api: PoetryApi,
    private val poemParser: PoemParser
) : SearchPoemRepository {

    override suspend fun search(author: String?, title: String?): Result<List<Poem>> = runCatching {
        val response = when {
            !author.isNullOrBlank() && !title.isNullOrBlank() ->
                api.searchByAuthorAndTitleRaw(author.trim(), title.trim())
            !author.isNullOrBlank() ->
                api.searchByAuthorRaw(author.trim())
            !title.isNullOrBlank() ->
                api.searchByTitleRaw(title.trim())
            else -> return@runCatching emptyList()
        }

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code()}: ${response.message()}")
        }

        val json = response.body() ?: return@runCatching emptyList()
        if (json.contains("\"status\":404") || json.contains("\"reason\":\"Not found\"")) {
            return@runCatching emptyList()
        }

        poemParser.parsePoems(json)
    }
}