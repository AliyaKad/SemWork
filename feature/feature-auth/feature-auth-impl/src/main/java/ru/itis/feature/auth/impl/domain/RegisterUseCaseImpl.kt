package ru.itis.feature.auth.impl.domain

import ru.itis.core.models.User
import ru.itis.feature.auth.api.AuthRepository
import ru.itis.feature.auth.api.RegisterUseCase
import javax.inject.Inject

class RegisterUseCaseImpl @Inject constructor(
    private val repository: AuthRepository
) : RegisterUseCase {

    override suspend fun invoke(email: String, password: String, name: String?): Result<User> {
        return repository.register(email, password, name)
    }
}