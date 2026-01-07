package ru.itis.feature.auth.impl.domain

import ru.itis.feature.auth.api.AuthRepository
import ru.itis.feature.auth.api.LogoutUseCase
import javax.inject.Inject

class LogoutUseCaseImpl @Inject constructor(
    private val repository: AuthRepository
) : LogoutUseCase {

    override suspend fun invoke() {
        return repository.logout()
    }
}