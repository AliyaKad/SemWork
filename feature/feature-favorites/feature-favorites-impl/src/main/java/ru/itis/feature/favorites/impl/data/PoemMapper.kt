package ru.itis.feature.favorites.impl.data

import com.google.gson.Gson
import ru.itis.core.data.FavoritePoemEntity
import ru.itis.core.models.Poem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoemMapper @Inject constructor(
    private val gson: Gson
) {

    fun toEntity(poem: Poem, userId: String): FavoritePoemEntity {
        return FavoritePoemEntity(
            userId = userId,
            author = poem.author ?: "",
            title = poem.title,
            lines = gson.toJson(poem.lines),
            linecount = poem.linecount
        )
    }

    fun fromEntity(entity: FavoritePoemEntity): Poem {
        val lines: List<String> = try {
            gson.fromJson(entity.lines, Array<String>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }

        return Poem(
            title = entity.title,
            author = entity.author,
            lines = lines,
            linecount = entity.linecount
        )
    }
}