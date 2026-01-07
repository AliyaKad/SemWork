package ru.itis.feature.auth.api

import ru.itis.core.models.User

interface GetCurrentUserUseCase {
    suspend operator fun invoke(): User?
}