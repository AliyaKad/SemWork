package ru.itis.core.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_poems",
    primaryKeys = ["userId", "author", "title"]
)
data class FavoritePoemEntity(
    @ColumnInfo(name = "userId")
    val userId: String,

    @ColumnInfo(name = "author")
    val author: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "lines")
    val lines: String,

    @ColumnInfo(name = "linecount")
    val linecount: String,

    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis()
)