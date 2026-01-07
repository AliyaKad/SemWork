package ru.itis.core.network

import retrofit2.Response
import ru.itis.core.models.Poem
import retrofit2.http.GET
import retrofit2.http.Path

interface PoetryApi {
    @GET("random")
    suspend fun getRandomPoem(): List<Poem>

    @GET("author/{author}")
    suspend fun searchByAuthorRaw(@Path("author") author: String): Response<String>

    @GET("title/{title}")
    suspend fun searchByTitleRaw(@Path("title") title: String): Response<String>

    @GET("author,title/{author};{title}")
    suspend fun searchByAuthorAndTitleRaw(
        @Path("author") author: String,
        @Path("title") title: String
    ): Response<String>
}

