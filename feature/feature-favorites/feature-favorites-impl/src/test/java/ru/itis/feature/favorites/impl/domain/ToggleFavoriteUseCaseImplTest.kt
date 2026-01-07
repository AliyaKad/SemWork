package ru.itis.feature.favorites.impl.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import ru.itis.core.models.Poem
import ru.itis.feature.favorites.api.FavoritesRepository

@RunWith(MockitoJUnitRunner::class)
class ToggleFavoriteUseCaseImplTest {

    @Mock
    private lateinit var mockRepository: FavoritesRepository

    private lateinit var useCase: ToggleFavoriteUseCaseImpl

    private val testPoem = Poem(
        title = "Test Poem",
        author = "Test Author",
        lines = listOf("Line 1", "Line 2", "Line 3"),
        linecount = "3"
    )

    @Before
    fun setUp() {
        useCase = ToggleFavoriteUseCaseImpl(mockRepository)
    }

    @Test
    fun `invoke when poem is not favorite should add to favorites`() = runTest {
        whenever(mockRepository.isFavorite(testPoem.author, testPoem.title))
            .thenReturn(false)

        useCase(testPoem)

        verify(mockRepository).isFavorite(testPoem.author, testPoem.title)
        verify(mockRepository).addToFavorites(testPoem)
        verify(mockRepository, never()).removeFromFavorites(any(), any())
    }

    @Test
    fun `invoke when poem is favorite should remove from favorites`() = runTest {
        whenever(mockRepository.isFavorite(testPoem.author, testPoem.title))
            .thenReturn(true)

        useCase(testPoem)

        verify(mockRepository).isFavorite(testPoem.author, testPoem.title)
        verify(mockRepository).removeFromFavorites(testPoem.author, testPoem.title)
        verify(mockRepository, never()).addToFavorites(any())
    }

    @Test
    fun `invoke should check isFavorite with correct parameters`() = runTest {
        val author = "John Keats"
        val title = "Ode to a Nightingale"
        val poem = Poem(
            title = title,
            author = author,
            lines = listOf("My heart aches, and a drowsy numbness pains"),
            linecount = "1"
        )
        whenever(mockRepository.isFavorite(author, title))
            .thenReturn(false)

        useCase(poem)

        verify(mockRepository).isFavorite(author, title)
    }

    @Test
    fun `invoke should add poem when not favorite with correct poem object`() = runTest {
        val expectedPoem = testPoem.copy()
        whenever(mockRepository.isFavorite(eq(testPoem.author), eq(testPoem.title)))
            .thenReturn(false)

        useCase(testPoem)

        argumentCaptor<Poem>().apply {
            verify(mockRepository).addToFavorites(capture())
            assertTrue(firstValue == expectedPoem)
        }
    }

    @Test
    fun `invoke should remove poem when favorite with correct parameters`() = runTest {
        val expectedAuthor = "Test Author"
        val expectedTitle = "Test Poem"
        whenever(mockRepository.isFavorite(expectedAuthor, expectedTitle))
            .thenReturn(true)

        useCase(testPoem)

        argumentCaptor<String>().apply {
            verify(mockRepository).removeFromFavorites(capture(), capture())
            assertTrue(firstValue == expectedAuthor)
            assertTrue(secondValue == expectedTitle)
        }
    }

    @Test
    fun `invoke should handle multiple toggles correctly`() = runTest {

        whenever(mockRepository.isFavorite(testPoem.author, testPoem.title))
            .thenReturn(false)
            .thenReturn(true)

        useCase(testPoem)

        verify(mockRepository, times(1)).addToFavorites(testPoem)
        verify(mockRepository, never()).removeFromFavorites(any(), any())

        useCase(testPoem)

        verify(mockRepository, times(1)).removeFromFavorites(testPoem.author, testPoem.title)
        verify(mockRepository, times(1)).addToFavorites(testPoem)
    }

    @Test
    fun `invoke should work with empty title and author`() = runTest {
        val emptyPoem = Poem(
            title = "",
            author = "",
            lines = emptyList(),
            linecount = "0"
        )
        whenever(mockRepository.isFavorite("", ""))
            .thenReturn(false)

        useCase(emptyPoem)

        verify(mockRepository).isFavorite("", "")
        verify(mockRepository).addToFavorites(emptyPoem)
    }

    @Test
    fun `invoke should work with poem containing special characters`() = runTest {
        val specialPoem = Poem(
            title = "Poem's Title & More",
            author = "Author-Dash",
            lines = listOf("Line with 'quotes'"),
            linecount = "1"
        )
        whenever(mockRepository.isFavorite("Author-Dash", "Poem's Title & More"))
            .thenReturn(true)

        useCase(specialPoem)

        verify(mockRepository).isFavorite("Author-Dash", "Poem's Title & More")
        verify(mockRepository).removeFromFavorites("Author-Dash", "Poem's Title & More")
    }

    @Test
    fun `invoke should not call repository methods if isFavorite check fails`() = runTest {
        whenever(mockRepository.isFavorite(testPoem.author, testPoem.title))
            .thenAnswer { throw RuntimeException("Database error") }

        try {
            useCase(testPoem)
        } catch (e: RuntimeException) {
        }

        verify(mockRepository).isFavorite(testPoem.author, testPoem.title)
        verify(mockRepository, never()).addToFavorites(any())
        verify(mockRepository, never()).removeFromFavorites(any(), any())
    }
}