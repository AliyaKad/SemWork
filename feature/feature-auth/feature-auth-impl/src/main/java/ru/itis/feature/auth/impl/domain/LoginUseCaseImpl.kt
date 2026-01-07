package ru.itis.feature.auth.impl.domain

import ru.itis.core.models.User
import ru.itis.feature.auth.api.AuthRepository
import ru.itis.feature.auth.api.LoginUseCase
import javax.inject.Inject

class LoginUseCaseImpl @Inject constructor(
    private val repository: AuthRepository
) : LoginUseCase {

    override suspend fun invoke(email: String, password: String): Result<User> {
        return repository.login(email, password)
    }
}