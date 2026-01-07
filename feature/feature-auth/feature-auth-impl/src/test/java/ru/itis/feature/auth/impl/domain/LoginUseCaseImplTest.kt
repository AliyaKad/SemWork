package ru.itis.feature.auth.impl.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import ru.itis.core.models.User
import ru.itis.feature.auth.api.AuthRepository


class LoginUseCaseImplTest {

    private lateinit var loginUseCase: LoginUseCaseImpl
    private lateinit var mockAuthRepository: AuthRepository

    @Before
    fun setUp() {
        mockAuthRepository = mock()
        loginUseCase = LoginUseCaseImpl(mockAuthRepository)
    }

    @Test
    fun `invoke should call repository login and return success result`() = runTest {

        val email = "test@example.com"
        val password = "password123"
        val expectedUser = User(
            id = "user123",
            email = email,
            name = "Test User"
        )

        given(mockAuthRepository.login(email, password))
            .willReturn(Result.success(expectedUser))

        val result = loginUseCase.invoke(email, password)

        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())

        verify(mockAuthRepository).login(email, password)
        verifyNoMoreInteractions(mockAuthRepository)
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        val email = "test@example.com"
        val password = "wrongpassword"
        val expectedException = RuntimeException("Invalid credentials")

        given(mockAuthRepository.login(email, password))
            .willReturn(Result.failure(expectedException))

        val result = loginUseCase.invoke(email, password)

        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())

        verify(mockAuthRepository).login(email, password)
    }

    @Test
    fun `invoke should propagate repository failure`() = runTest {
        val email = "test@example.com"
        val password = "password"
        val networkException = Exception("Network error")

        given(mockAuthRepository.login(email, password))
            .willReturn(Result.failure(networkException))

        val result = loginUseCase.invoke(email, password)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)

        verify(mockAuthRepository).login(email, password)
    }

    @Test
    fun `invoke with empty email should still call repository`() = runTest {
        val email = ""
        val password = "password"
        val expectedUser = User(id = "user1", email = email, name = null)

        given(mockAuthRepository.login(email, password))
            .willReturn(Result.success(expectedUser))

        val result = loginUseCase.invoke(email, password)

        assertTrue(result.isSuccess)

        verify(mockAuthRepository).login(email, password)
    }

    @Test
    fun `invoke with empty password should still call repository`() = runTest {
        val email = "test@example.com"
        val password = ""
        val expectedException = RuntimeException("Password cannot be empty")

        given(mockAuthRepository.login(email, password))
            .willReturn(Result.failure(expectedException))

        val result = loginUseCase.invoke(email, password)

        assertTrue(result.isFailure)

        verify(mockAuthRepository).login(email, password)
    }

}