package ru.itis.feature.auth.api

import ru.itis.core.models.User

interface LoginUseCase {
    suspend operator fun invoke(email: String, password: String): Result<User>
}