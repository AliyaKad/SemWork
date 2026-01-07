package ru.itis.core.models

data class User(
    val id: String,
    val email: String,
    val name: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)