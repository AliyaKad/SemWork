package ru.itis.feature.favorites.impl.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import ru.itis.core.models.Poem
import ru.itis.feature.favorites.api.FavoritesRepository

@RunWith(MockitoJUnitRunner::class)
class AddToFavoritesUseCaseImplTest {

    @Mock
    private lateinit var mockRepository: FavoritesRepository

    private lateinit var useCase: AddToFavoritesUseCaseImpl

    private val testPoem = Poem(
        title = "Test Poem",
        author = "Test Author",
        lines = listOf("Line 1", "Line 2", "Line 3"),
        linecount = "3"
    )

    @Before
    fun setUp() {
        useCase = AddToFavoritesUseCaseImpl(mockRepository)
    }

    @Test
    fun `invoke should call repository addToFavorites with correct poem`() = runTest {
        useCase(testPoem)

        verify(mockRepository).addToFavorites(testPoem)
    }

    @Test
    fun `invoke should call repository addToFavorites exactly once`() = runTest {
        useCase(testPoem)

        verify(mockRepository, times(1)).addToFavorites(testPoem)
        verify(mockRepository, never()).removeFromFavorites(anyString(), anyString())
        verify(mockRepository, never()).isFavorite(anyString(), anyString())
    }

    @Test
    fun `invoke should propagate exceptions from repository`() = runTest {
        val exception = RuntimeException("Database error")
        doThrow(exception).`when`(mockRepository).addToFavorites(testPoem)

        try {
            useCase(testPoem)
            fail("Should have thrown exception")
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
        }

        verify(mockRepository).addToFavorites(testPoem)
    }

    @Test
    fun `invoke should work with poem containing empty strings`() = runTest {
        val emptyPoem = Poem(
            title = "",
            author = "",
            lines = emptyList(),
            linecount = "0"
        )

        useCase(emptyPoem)

        verify(mockRepository).addToFavorites(emptyPoem)
    }

    @Test
    fun `invoke should work with poem containing special characters`() = runTest {
        val specialPoem = Poem(
            title = "Poem's Title & \"Quotes\"",
            author = "Author-Dash O'Connor",
            lines = listOf("Line with 'single' and \"double\" quotes"),
            linecount = "1"
        )

        useCase(specialPoem)

        verify(mockRepository).addToFavorites(specialPoem)
    }

    @Test
    fun `invoke should work when called multiple times with same poem`() = runTest {
        useCase(testPoem)
        useCase(testPoem)
        useCase(testPoem)

        verify(mockRepository, times(3)).addToFavorites(testPoem)
    }

    @Test
    fun `invoke should work when called with different poems`() = runTest {
        val poem1 = Poem(title = "Poem 1", author = "Author 1", lines = listOf("A"), linecount = "1")
        val poem2 = Poem(title = "Poem 2", author = "Author 2", lines = listOf("B"), linecount = "1")
        val poem3 = Poem(title = "Poem 3", author = "Author 3", lines = listOf("C"), linecount = "1")

        useCase(poem1)
        useCase(poem2)
        useCase(poem3)

        verify(mockRepository, times(1)).addToFavorites(poem1)
        verify(mockRepository, times(1)).addToFavorites(poem2)
        verify(mockRepository, times(1)).addToFavorites(poem3)
    }

    @Test
    fun `invoke should verify poem equality by content, not reference`() = runTest {
        val poemCopy = testPoem.copy()

        useCase(poemCopy)

        verify(mockRepository).addToFavorites(testPoem)
    }

    @Test
    fun `invoke should not call any other repository methods`() = runTest {
        useCase(testPoem)

        verify(mockRepository).addToFavorites(testPoem)
        verifyNoMoreInteractions(mockRepository)
    }
}