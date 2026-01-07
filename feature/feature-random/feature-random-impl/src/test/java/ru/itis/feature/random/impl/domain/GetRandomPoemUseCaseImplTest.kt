package ru.itis.feature.random.impl.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import ru.itis.core.models.Poem
import ru.itis.feature.random.api.RandomPoemRepository
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class GetRandomPoemUseCaseImplTest {

    @Mock
    private lateinit var mockRepository: RandomPoemRepository

    private lateinit var useCase: GetRandomPoemUseCaseImpl

    private val testPoem = Poem(
        title = "Тестовое стихотворение",
        author = "Тестовый автор",
        lines = listOf("Строка 1", "Строка 2"),
        linecount = "2"
    )

    @Before
    fun setUp() {
        useCase = GetRandomPoemUseCaseImpl(mockRepository)
    }


    @Test
    fun `should return success when repository returns poem`() = runTest {
        whenever(mockRepository.getRandomPoem()).thenReturn(Result.success(testPoem))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(testPoem, result.getOrNull())
        verify(mockRepository, times(1)).getRandomPoem()
    }


    @Test
    fun `should return failure when repository returns failure`() = runTest {
        val exception = Exception("Test error")
        whenever(mockRepository.getRandomPoem()).thenReturn(Result.failure(exception))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository, times(1)).getRandomPoem()
    }


    @Test
    fun `should handle network error`() = runTest {
        val networkError = IOException("Network unavailable")
        whenever(mockRepository.getRandomPoem()).thenReturn(Result.failure(networkError))

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals("Network unavailable", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should handle empty poem`() = runTest {
        val emptyPoem = Poem(
            title = "",
            author = "",
            lines = emptyList(),
            linecount = "0"
        )
        whenever(mockRepository.getRandomPoem()).thenReturn(Result.success(emptyPoem))

        val result = useCase()

        assertTrue(result.isSuccess)
        val poem = result.getOrNull()
        assertEquals("", poem?.title)
        assertEquals("", poem?.author)
        assertTrue(poem?.lines?.isEmpty() == true)
    }


    @Test
    fun `should be suspending function`() = runTest {
        whenever(mockRepository.getRandomPoem()).thenReturn(Result.success(testPoem))

        val result = useCase()

        assertTrue(result.isSuccess)
    }
}