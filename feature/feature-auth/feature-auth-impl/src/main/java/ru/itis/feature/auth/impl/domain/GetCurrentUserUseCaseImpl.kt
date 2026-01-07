package ru.itis.feature.auth.impl.domain

import ru.itis.core.models.User
import ru.itis.feature.auth.api.AuthRepository
import ru.itis.feature.auth.api.GetCurrentUserUseCase
import javax.inject.Inject

class GetCurrentUserUseCaseImpl @Inject constructor(
    private val repository: AuthRepository
) : GetCurrentUserUseCase {

    override suspend fun invoke(): User? {
        return repository.getCurrentUser()
    }
}