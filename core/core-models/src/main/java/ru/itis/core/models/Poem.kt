package ru.itis.core.models

import java.io.Serializable

data class Poem(
    val title: String,
    val author: String,
    val lines: List<String>,
    val linecount: String = ""
) : Serializable