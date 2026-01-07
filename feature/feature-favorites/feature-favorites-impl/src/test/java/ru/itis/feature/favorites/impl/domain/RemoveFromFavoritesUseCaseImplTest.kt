package ru.itis.feature.favorites.impl.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import ru.itis.feature.favorites.api.FavoritesRepository

@RunWith(MockitoJUnitRunner::class)
class RemoveFromFavoritesUseCaseImplTest {

    @Mock
    private lateinit var mockRepository: FavoritesRepository

    private lateinit var useCase: RemoveFromFavoritesUseCaseImpl

    private val testAuthor = "Test Author"
    private val testTitle = "Test Title"

    @Before
    fun setUp() {
        useCase = RemoveFromFavoritesUseCaseImpl(mockRepository)
    }

    @Test
    fun `invoke should call repository removeFromFavorites with correct parameters`() = runTest {
        useCase(testAuthor, testTitle)
        verify(mockRepository).removeFromFavorites(testAuthor, testTitle)
    }

    @Test
    fun `invoke should call repository removeFromFavorites exactly once`() = runTest {
        useCase(testAuthor, testTitle)
        verify(mockRepository, times(1)).removeFromFavorites(testAuthor, testTitle)
    }

    @Test
    fun `invoke should propagate exceptions from repository`() = runTest {
        val exception = RuntimeException("Database error")
        doThrow(exception).`when`(mockRepository).removeFromFavorites(testAuthor, testTitle)

        try {
            useCase(testAuthor, testTitle)
            fail("Should have thrown exception")
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
        }
    }

    @Test
    fun `invoke should work with empty strings`() = runTest {
        val emptyAuthor = ""
        val emptyTitle = ""
        useCase(emptyAuthor, emptyTitle)
        verify(mockRepository).removeFromFavorites(emptyAuthor, emptyTitle)
    }

    @Test
    fun `invoke should work with special characters`() = runTest {
        val specialAuthor = "Author's Name & \"Quotes\""
        val specialTitle = "Title-Dash O'Connor"
        useCase(specialAuthor, specialTitle)
        verify(mockRepository).removeFromFavorites(specialAuthor, specialTitle)
    }

    @Test
    fun `invoke should work when called multiple times with same parameters`() = runTest {
        repeat(3) {
            useCase(testAuthor, testTitle)
        }
        verify(mockRepository, times(3)).removeFromFavorites(testAuthor, testTitle)
    }

    @Test
    fun `invoke should work when called with different parameters`() = runTest {
        val parameters = listOf(
            "Author 1" to "Title 1",
            "Author 2" to "Title 2",
            "Author 3" to "Title 3"
        )

        parameters.forEach { (author, title) ->
            useCase(author, title)
        }

        parameters.forEach { (author, title) ->
            verify(mockRepository).removeFromFavorites(author, title)
        }
    }

    @Test
    fun `invoke should handle case sensitivity correctly`() = runTest {
        useCase("AUTHOR", "TiTlE")
        useCase("author", "TiTlE")
        verify(mockRepository).removeFromFavorites("AUTHOR", "TiTlE")
        verify(mockRepository).removeFromFavorites("author", "TiTlE")
    }

    @Test
    fun `invoke should work with long strings`() = runTest {
        val longAuthor = "A".repeat(100)
        val longTitle = "T".repeat(100)
        useCase(longAuthor, longTitle)
        verify(mockRepository).removeFromFavorites(longAuthor, longTitle)
    }

    @Test
    fun `invoke should work with whitespace strings`() = runTest {
        useCase("   ", "\t\n")
        verify(mockRepository).removeFromFavorites("   ", "\t\n")
    }

    @Test
    fun `invoke should preserve parameter order`() = runTest {
        useCase("First Author", "Second Title")
        verify(mockRepository).removeFromFavorites("First Author", "Second Title")
    }
}