package ru.itis.feature.search.impl.data

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import ru.itis.core.models.Poem
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoemParser @Inject constructor() {

    private val gson = GsonBuilder()
        .registerTypeAdapter(Poem::class.java, PoemDeserializer())
        .create()

    private val listType = object : TypeToken<List<Poem>>() {}.type

    fun parsePoems(json: String): List<Poem> {
        return try {
            val list: List<Poem> = gson.fromJson(json, listType)
            list
        } catch (e: Exception) {
            try {
                val poem = gson.fromJson(json, Poem::class.java)
                listOf(poem)
            } catch (ex: Exception) {
                emptyList()
            }
        }
    }

    private class PoemDeserializer : JsonDeserializer<Poem> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Poem {
            val obj = json.asJsonObject
            val title = obj.get("title")?.asString ?: ""
            val author = obj.get("author")?.asString ?: ""
            val linecount = obj.get("linecount")?.asString ?: ""
            val lines = mutableListOf<String>()

            if (obj.has("lines") && obj.get("lines").isJsonArray) {
                for (element in obj.get("lines").asJsonArray) {
                    if (element.isJsonNull) {
                        lines.add("")
                    } else {
                        lines.add(element.asString)
                    }
                }
            }

            return Poem(
                title = title,
                author = author,
                lines = lines,
                linecount = linecount
            )
        }
    }
}