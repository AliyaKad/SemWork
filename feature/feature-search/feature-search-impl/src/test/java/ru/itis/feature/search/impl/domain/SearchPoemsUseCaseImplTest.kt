package ru.itis.feature.search.impl.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.itis.core.models.Poem
import ru.itis.feature.search.api.SearchPoemRepository

@RunWith(MockitoJUnitRunner::class)
class SearchPoemsUseCaseImplTest {

    @Mock
    private lateinit var mockRepository: SearchPoemRepository

    private lateinit var useCase: SearchPoemsUseCaseImpl

    private val testPoems = listOf(
        Poem(
            title = "Test Poem 1",
            author = "Author 1",
            lines = listOf("Line 1", "Line 2"),
            linecount = "2"
        ),
        Poem(
            title = "Test Poem 2",
            author = "Author 2",
            lines = listOf("Line 1", "Line 2", "Line 3"),
            linecount = "3"
        )
    )

    @Before
    fun setUp() {
        useCase = SearchPoemsUseCaseImpl(mockRepository)
    }

    @Test
    fun `invoke with author and title should call repository with both parameters`() = runTest {
        val author = "Test Author"
        val title = "Test Title"
        whenever(mockRepository.search(author, title))
            .thenReturn(Result.success(testPoems))

        val result = useCase(author, title)

        verify(mockRepository).search(author, title)
        assertTrue(result.isSuccess)
        assertEquals(testPoems, result.getOrNull())
    }

    @Test
    fun `invoke with only author should call repository with author and null title`() = runTest {
        val author = "Test Author"
        whenever(mockRepository.search(author, null))
            .thenReturn(Result.success(testPoems))

        val result = useCase(author, null)

        verify(mockRepository).search(author, null)
        assertTrue(result.isSuccess)
        assertEquals(testPoems, result.getOrNull())
    }

    @Test
    fun `invoke with only title should call repository with null author and title`() = runTest {
        val title = "Test Title"
        whenever(mockRepository.search(null, title))
            .thenReturn(Result.success(testPoems))

        val result = useCase(null, title)

        verify(mockRepository).search(null, title)
        assertTrue(result.isSuccess)
        assertEquals(testPoems, result.getOrNull())
    }

    @Test
    fun `invoke with both null parameters should call repository with both null`() = runTest {
        whenever(mockRepository.search(null, null))
            .thenReturn(Result.success(testPoems))

        val result = useCase(null, null)

        verify(mockRepository).search(null, null)
        assertTrue(result.isSuccess)
        assertEquals(testPoems, result.getOrNull())
    }

    @Test
    fun `invoke should return success when repository returns success`() = runTest {
        val author = "Author"
        val title = "Title"
        whenever(mockRepository.search(author, title))
            .thenReturn(Result.success(emptyList()))

        val result = useCase(author, title)

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Poem>(), result.getOrNull())
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        val author = "Author"
        val title = "Title"
        val expectedException = RuntimeException("Network error")
        whenever(mockRepository.search(author, title))
            .thenReturn(Result.failure(expectedException))

        val result = useCase(author, title)

        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    @Test
    fun `invoke should propagate empty list from repository`() = runTest {
        val author = "Non-existent Author"
        whenever(mockRepository.search(author, null))
            .thenReturn(Result.success(emptyList()))

        val result = useCase(author, null)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `invoke with case sensitive parameters should call repository exactly`() = runTest {
        val author = "John Doe"
        val title = "My Poem"
        val expectedResult = listOf(testPoems.first())
        whenever(mockRepository.search(author, title))
            .thenReturn(Result.success(expectedResult))

        val result = useCase(author, title)

        verify(mockRepository).search(author, title)
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
    }
}