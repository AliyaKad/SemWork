package ru.itis.feature.auth.api

interface LogoutUseCase {
    suspend operator fun invoke()
}