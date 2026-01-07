package ru.itis.feature.creator.impl.data

import com.google.gson.Gson
import ru.itis.core.data.UserPoemEntity
import ru.itis.core.models.Poem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPoemMapper @Inject constructor(
    private val gson: Gson
) {

    fun toEntity(poem: Poem, userId: String): UserPoemEntity {
        return UserPoemEntity(
            id = generateId(userId, poem.author, poem.title),
            userId = userId,
            title = poem.title,
            author = poem.author,
            lines = gson.toJson(poem.lines)
        )
    }

    fun fromEntity(entity: UserPoemEntity): Poem {
        val lines: List<String> = try {
            gson.fromJson(entity.lines, Array<String>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }

        return Poem(
            title = entity.title,
            author = entity.author,
            lines = lines,
            linecount = lines.size.toString()
        )
    }

    private fun generateId(userId: String, author: String, title: String): String {
        return "${userId}_${author}_${title}".hashCode().toString()
    }
}