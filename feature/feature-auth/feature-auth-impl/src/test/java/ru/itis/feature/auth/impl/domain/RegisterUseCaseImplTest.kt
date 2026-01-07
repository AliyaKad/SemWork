package ru.itis.feature.auth.impl.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.given
import ru.itis.core.models.User
import ru.itis.feature.auth.api.AuthRepository

class RegisterUseCaseImplTest {

    private lateinit var registerUseCase: RegisterUseCaseImpl
    private lateinit var mockAuthRepository: AuthRepository

    @Before
    fun setUp() {
        mockAuthRepository = mock()
        registerUseCase = RegisterUseCaseImpl(mockAuthRepository)
    }

    @Test
    fun `invoke should call repository register with email and password and return success`() = runTest {
        val email = "newuser@example.com"
        val password = "securePassword123"
        val name = "John Doe"
        val expectedUser = User(
            id = "newUserId",
            email = email,
            name = name
        )

        given(mockAuthRepository.register(email, password, name))
            .willReturn(Result.success(expectedUser))

        val result = registerUseCase.invoke(email, password, name)

        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())

        verify(mockAuthRepository).register(email, password, name)
        verifyNoMoreInteractions(mockAuthRepository)
    }

    @Test
    fun `invoke should call repository register with null name`() = runTest {
        val email = "test@example.com"
        val password = "password"
        val name: String? = null
        val expectedUser = User(
            id = "user123",
            email = email,
            name = null
        )

        given(mockAuthRepository.register(email, password, name))
            .willReturn(Result.success(expectedUser))

        val result = registerUseCase.invoke(email, password, name)

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull()?.name)

        verify(mockAuthRepository).register(email, password, name)
    }

    @Test
    fun `invoke should handle registration failure`() = runTest {
        val email = "existing@example.com"
        val password = "password"
        val name = "Existing User"
        val expectedException = RuntimeException("User already exists")

        given(mockAuthRepository.register(email, password, name))
            .willReturn(Result.failure(expectedException))

        val result = registerUseCase.invoke(email, password, name)

        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())

        verify(mockAuthRepository).register(email, password, name)
    }

    @Test
    fun `invoke with empty name should pass empty string to repository`() = runTest {
        val email = "test@example.com"
        val password = "password"
        val name = ""
        val expectedUser = User(id = "user1", email = email, name = "")

        given(mockAuthRepository.register(email, password, name))
            .willReturn(Result.success(expectedUser))

        val result = registerUseCase.invoke(email, password, name)

        assertTrue(result.isSuccess)
        assertEquals("", result.getOrNull()?.name)

        verify(mockAuthRepository).register(email, password, name)
    }

    @Test
    fun `invoke should handle network errors`() = runTest {
        val email = "test@example.com"
        val password = "password"
        val name = "Test User"
        val networkException = Exception("No internet connection")

        given(mockAuthRepository.register(email, password, name))
            .willReturn(Result.failure(networkException))


        val result = registerUseCase.invoke(email, password, name)

        assertTrue(result.isFailure)
        assertEquals("No internet connection", result.exceptionOrNull()?.message)

        verify(mockAuthRepository).register(email, password, name)
    }

    @Test
    fun `invoke should validate email format through repository`() = runTest {
        val invalidEmail = "not-an-email"
        val password = "password"
        val name = "User"
        val validationException = IllegalArgumentException("Invalid email format")

        given(mockAuthRepository.register(invalidEmail, password, name))
            .willReturn(Result.failure(validationException))

        val result = registerUseCase.invoke(invalidEmail, password, name)

        assertTrue(result.isFailure)
        assertEquals("Invalid email format", result.exceptionOrNull()?.message)

        verify(mockAuthRepository).register(invalidEmail, password, name)
    }

    @Test
    fun `invoke should handle password strength validation through repository`() = runTest {
        val email = "test@example.com"
        val weakPassword = "123"
        val name = "User"
        val passwordException = IllegalArgumentException("Password too weak")

        given(mockAuthRepository.register(email, weakPassword, name))
            .willReturn(Result.failure(passwordException))

        val result = registerUseCase.invoke(email, weakPassword, name)

        assertTrue(result.isFailure)
        assertEquals("Password too weak", result.exceptionOrNull()?.message)

        verify(mockAuthRepository).register(email, weakPassword, name)
    }

    @Test
    fun `invoke should return different users for different emails`() = runTest {
        val email1 = "user1@example.com"
        val email2 = "user2@example.com"
        val password = "samePassword"
        val name = "User"

        val user1 = User(id = "id1", email = email1, name = name)
        val user2 = User(id = "id2", email = email2, name = name)

        given(mockAuthRepository.register(email1, password, name))
            .willReturn(Result.success(user1))
        given(mockAuthRepository.register(email2, password, name))
            .willReturn(Result.success(user2))

        val result1 = registerUseCase.invoke(email1, password, name)
        val result2 = registerUseCase.invoke(email2, password, name)

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        assertEquals("id1", result1.getOrNull()?.id)
        assertEquals("id2", result2.getOrNull()?.id)
        assertNotEquals(result1.getOrNull(), result2.getOrNull())

        verify(mockAuthRepository).register(email1, password, name)
        verify(mockAuthRepository).register(email2, password, name)
    }
}