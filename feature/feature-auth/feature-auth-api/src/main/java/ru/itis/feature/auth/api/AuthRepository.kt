package ru.itis.feature.auth.api

import ru.itis.core.models.User

interface AuthRepository {
    suspend fun register(email: String, password: String, name: String?): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    suspend fun isUserLoggedIn(): Boolean
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteAccount(): Result<Unit>
}