package ru.itis.feature.auth.api

import ru.itis.core.models.User

interface RegisterUseCase {
    suspend operator fun invoke(email: String, password: String, name: String?): Result<User>
}