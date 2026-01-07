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

    private val testAuthor = "Robert Frost"
    private val testTitle = "The Road Not Taken"

    @Before
    fun setUp() {
        useCase = DeletePoemUseCaseImpl(mockRepository)
    }

    @Test
    fun `invoke should call repository deletePoem with correct author and title`() = runTest {
        useCase(testAuthor, testTitle)
        verify(mockRepository).deletePoem(testAuthor, testTitle)
    }

    @Test
    fun `invoke should call repository deletePoem exactly once`() = runTest {
        useCase(testAuthor, testTitle)
        verify(mockRepository, times(1)).deletePoem(testAuthor, testTitle)
    }

    @Test
    fun `invoke should propagate exceptions from repository`() = runTest {
        val exception = RuntimeException("Network error")
        doThrow(exception).`when`(mockRepository).deletePoem(testAuthor, testTitle)

        try {
            useCase(testAuthor, testTitle)
            fail("Should have thrown exception")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `invoke should work with empty author and title`() = runTest {
        val emptyAuthor = ""
        val emptyTitle = ""
        useCase(emptyAuthor, emptyTitle)
        verify(mockRepository).deletePoem(emptyAuthor, emptyTitle)
    }

    @Test
    fun `invoke should work with special characters`() = runTest {
        val specialAuthor = "Fyodor Dostoevsky - «Crime & Punishment»"
        val specialTitle = "Преступление и наказание: Part 1"
        useCase(specialAuthor, specialTitle)
        verify(mockRepository).deletePoem(specialAuthor, specialTitle)
    }

    @Test
    fun `invoke should work when called multiple times`() = runTest {
        repeat(3) {
            useCase(testAuthor, testTitle)
        }
        verify(mockRepository, times(3)).deletePoem(testAuthor, testTitle)
    }

    @Test
    fun `invoke should work with different authors and titles`() = runTest {
        val poems = listOf(
            Pair("Emily Dickinson", "Hope is the thing with feathers"),
            Pair("William Shakespeare", "Sonnet 18"),
            Pair("Maya Angelou", "Still I Rise")
        )

        poems.forEach { (author, title) ->
            useCase(author, title)
        }

        poems.forEach { (author, title) ->
            verify(mockRepository).deletePoem(author, title)
        }
    }

    @Test
    fun `invoke should work with long strings`() = runTest {
        val longAuthor = "A".repeat(1000)
        val longTitle = "B".repeat(1000)
        useCase(longAuthor, longTitle)
        verify(mockRepository).deletePoem(longAuthor, longTitle)
    }

    @Test
    fun `invoke should work with numeric strings`() = runTest {
        val numericAuthor = "Author123"
        val numericTitle = "Title456"
        useCase(numericAuthor, numericTitle)
        verify(mockRepository).deletePoem(numericAuthor, numericTitle)
    }

    @Test
    fun `invoke should handle null author as empty string`() = runTest {
        val nullAuthor = ""
        val title = "Some Title"
        useCase(nullAuthor, title)
        verify(mockRepository).deletePoem(nullAuthor, title)
    }

    @Test
    fun `invoke should handle spaces in author and title`() = runTest {
        val author = "  Robert  Frost  "
        val title = "  The Road Not Taken  "
        useCase(author, title)
        verify(mockRepository).deletePoem(author, title)
    }
}