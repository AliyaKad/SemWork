package ru.itis.feature.creator.impl.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import ru.itis.core.models.Poem
import ru.itis.feature.creator.api.UserPoemsRepository

@RunWith(MockitoJUnitRunner::class)
class SavePoemUseCaseImplTest {

    @Mock
    private lateinit var mockRepository: UserPoemsRepository

    private lateinit var useCase: SavePoemUseCaseImpl

    private val testPoem = Poem(
        title = "Test Poem Title",
        author = "Test Author",
        lines = listOf("Line 1", "Line 2", "Line 3"),
        linecount = "3"
    )

    @Before
    fun setUp() {
        useCase = SavePoemUseCaseImpl(mockRepository)
    }

    @Test
    fun `invoke should call repository savePoem with correct poem`() = runTest {
        useCase(testPoem)
        verify(mockRepository).savePoem(testPoem)
    }

    @Test
    fun `invoke should call repository savePoem exactly once`() = runTest {
        useCase(testPoem)
        verify(mockRepository, times(1)).savePoem(testPoem)
    }

    @Test
    fun `invoke should propagate exceptions from repository`() = runTest {
        val exception = RuntimeException("Database error")
        doThrow(exception).`when`(mockRepository).savePoem(testPoem)

        try {
            useCase(testPoem)
            fail("Should have thrown exception")
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
        }
    }

    @Test
    fun `invoke should work with poem having empty fields`() = runTest {
        val emptyPoem = Poem(
            title = "",
            author = "",
            lines = emptyList(),
            linecount = ""
        )
        useCase(emptyPoem)
        verify(mockRepository).savePoem(emptyPoem)
    }

    @Test
    fun `invoke should work with poem having special characters`() = runTest {
        val specialPoem = Poem(
            title = "Poem's Title & \"Quotes\" ©",
            author = "Author-O'Connor",
            lines = listOf("Line with special chars: €, £, ¥"),
            linecount = "1"
        )
        useCase(specialPoem)
        verify(mockRepository).savePoem(specialPoem)
    }

    @Test
    fun `invoke should work when called multiple times with same poem`() = runTest {
        repeat(3) {
            useCase(testPoem)
        }
        verify(mockRepository, times(3)).savePoem(testPoem)
    }

    @Test
    fun `invoke should work with different poems`() = runTest {
        val poems = listOf(
            Poem(title = "Title 1", author = "Author 1", lines = listOf("Line 1"), linecount = "1"),
            Poem(title = "Title 2", author = "Author 2", lines = listOf("Line 1", "Line 2"), linecount = "2"),
            Poem(title = "Title 3", author = "Author 3", lines = listOf("Line 1", "Line 2", "Line 3"), linecount = "3")
        )

        poems.forEach { poem ->
            useCase(poem)
        }

        poems.forEach { poem ->
            verify(mockRepository).savePoem(poem)
        }
    }

    @Test
    fun `invoke should work with many lines`() = runTest {
        val manyLinesPoem = Poem(
            title = "Long Poem",
            author = "Author",
            lines = List(100) { "Line ${it + 1}" },
            linecount = "100"
        )
        useCase(manyLinesPoem)
        verify(mockRepository).savePoem(manyLinesPoem)
    }

    @Test
    fun `invoke should preserve all poem properties`() = runTest {
        val complexPoem = Poem(
            title = "Complex Title",
            author = "Complex Author",
            lines = listOf("First line", "Second line", "Third line"),
            linecount = "3"
        )
        useCase(complexPoem)
        verify(mockRepository).savePoem(complexPoem)
    }

    @Test
    fun `invoke should work with single line poem`() = runTest {
        val singleLinePoem = Poem(
            title = "Haiku",
            author = "Author",
            lines = listOf("Single line poem"),
            linecount = "1"
        )
        useCase(singleLinePoem)
        verify(mockRepository).savePoem(singleLinePoem)
    }

    @Test
    fun `invoke should work with multiline poem`() = runTest {
        val multilinePoem = Poem(
            title = "Multiline",
            author = "Poet",
            lines = listOf(
                "Roses are red",
                "Violets are blue",
                "Sugar is sweet",
                "And so are you"
            ),
            linecount = "4"
        )
        useCase(multilinePoem)
        verify(mockRepository).savePoem(multilinePoem)
    }

    @Test
    fun `invoke should work with poem having numeric linecount`() = runTest {
        val poem = testPoem.copy(linecount = "42")
        useCase(poem)
        verify(mockRepository).savePoem(poem)
    }

    @Test
    fun `invoke should work with poem having whitespace in title`() = runTest {
        val poem = testPoem.copy(title = "   Title with spaces   ")
        useCase(poem)
        verify(mockRepository).savePoem(poem)
    }
}