package ru.itis.feature.creator.impl.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import ru.itis.feature.creator.api.UserPoemsRepository

@RunWith(MockitoJUnitRunner::class)
class DeletePoemUseCaseImplTest {

    @Mock
    private lateinit var mockRepository: UserPoemsRepository

    private lateinit var useCase: DeletePoemUseCaseImpl

    private val testPoemId = "test-poem-id-123"

    @Before
    fun setUp() {
        useCase = DeletePoemUseCaseImpl(mockRepository)
    }

    @Test
    fun `invoke should call repository deletePoem with correct id`() = runTest {
        useCase(testPoemId)
        verify(mockRepository).deletePoem(testPoemId)
    }

    @Test
    fun `invoke should call repository deletePoem exactly once`() = runTest {
        useCase(testPoemId)
        verify(mockRepository, times(1)).deletePoem(testPoemId)
    }

    @Test
    fun `invoke should propagate exceptions from repository`() = runTest {
        val exception = RuntimeException("Network error")
        doThrow(exception).`when`(mockRepository).deletePoem(testPoemId)

        try {
            useCase(testPoemId)
            fail("Should have thrown exception")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `invoke should work with empty id`() = runTest {
        val emptyId = ""
        useCase(emptyId)
        verify(mockRepository).deletePoem(emptyId)
    }

    @Test
    fun `invoke should work with special characters in id`() = runTest {
        val specialId = "id-with-special-chars-123_!@#$%^&*()"
        useCase(specialId)
        verify(mockRepository).deletePoem(specialId)
    }

    @Test
    fun `invoke should work when called multiple times`() = runTest {
        repeat(3) {
            useCase(testPoemId)
        }
        verify(mockRepository, times(3)).deletePoem(testPoemId)
    }

    @Test
    fun `invoke should work with different ids`() = runTest {
        val ids = listOf("id1", "id2", "id3")

        ids.forEach { id ->
            useCase(id)
        }

        ids.forEach { id ->
            verify(mockRepository).deletePoem(id)
        }
    }

    @Test
    fun `invoke should work with long id`() = runTest {
        val longId = "x".repeat(1000)
        useCase(longId)
        verify(mockRepository).deletePoem(longId)
    }

    @Test
    fun `invoke should work with numeric id`() = runTest {
        val numericId = "1234567890"
        useCase(numericId)
        verify(mockRepository).deletePoem(numericId)
    }
}