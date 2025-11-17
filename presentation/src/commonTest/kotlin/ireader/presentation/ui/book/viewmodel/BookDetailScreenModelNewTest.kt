package ireader.presentation.ui.book.viewmodel

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.book.GetBook
import ireader.domain.usecases.book.GetChapters
import ireader.domain.usecases.book.ToggleFavorite
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BookDetailScreenModelNewTest {
    
    private lateinit var getBook: GetBook
    private lateinit var getChapters: GetChapters
    private lateinit var toggleFavorite: ToggleFavorite
    private lateinit var screenModel: BookDetailScreenModelNew
    
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getBook = mockk()
        getChapters = mockk()
        toggleFavorite = mockk()
    }
    
    @Test
    fun `initial state is loading`() = runTest {
        // Given
        val bookId = 1L
        coEvery { getBook.subscribe(bookId) } returns flowOf(null)
        coEvery { getChapters.subscribe(bookId) } returns flowOf(emptyList())
        
        // When
        screenModel = BookDetailScreenModelNew(bookId, getBook, getChapters, toggleFavorite)
        
        // Then
        val initialState = screenModel.state.value
        assertTrue(initialState.isLoading)
        assertNull(initialState.book)
        assertTrue(initialState.chapters.isEmpty())
        assertNull(initialState.error)
    }
    
    @Test
    fun `loads book and chapters successfully`() = runTest {
        // Given
        val bookId = 1L
        val book = createTestBook(id = bookId)
        val chapters = listOf(createTestChapter(bookId = bookId))
        
        coEvery { getBook.subscribe(bookId) } returns flowOf(book)
        coEvery { getChapters.subscribe(bookId) } returns flowOf(chapters)
        
        // When
        screenModel = BookDetailScreenModelNew(bookId, getBook, getChapters, toggleFavorite)
        advanceUntilIdle()
        
        // Then
        val state = screenModel.state.value
        assertFalse(state.isLoading)
        assertEquals(book, state.book)
        assertEquals(chapters, state.chapters)
        assertNull(state.error)
    }
    
    @Test
    fun `sets error when book not found`() = runTest {
        // Given
        val bookId = 1L
        coEvery { getBook.subscribe(bookId) } returns flowOf(null)
        coEvery { getChapters.subscribe(bookId) } returns flowOf(emptyList())
        
        // When
        screenModel = BookDetailScreenModelNew(bookId, getBook, getChapters, toggleFavorite)
        advanceUntilIdle()
        
        // Then
        val state = screenModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.book)
        assertEquals("Book not found", state.error)
    }
    
    @Test
    fun `refresh loads book data`() = runTest {
        // Given
        val bookId = 1L
        val book = createTestBook(id = bookId)
        val chapters = listOf(createTestChapter(bookId = bookId))
        
        coEvery { getBook.subscribe(bookId) } returns flowOf(book)
        coEvery { getChapters.subscribe(bookId) } returns flowOf(chapters)
        coEvery { getBook.await(bookId) } returns book
        coEvery { getChapters.await(bookId) } returns chapters
        
        screenModel = BookDetailScreenModelNew(bookId, getBook, getChapters, toggleFavorite)
        advanceUntilIdle()
        
        // When
        screenModel.refresh()
        advanceUntilIdle()
        
        // Then
        val state = screenModel.state.value
        assertFalse(state.isRefreshing)
        assertEquals(book, state.book)
        assertEquals(chapters, state.chapters)
        coVerify { getBook.await(bookId) }
        coVerify { getChapters.await(bookId) }
    }
    
    @Test
    fun `toggleBookFavorite calls toggle favorite use case`() = runTest {
        // Given
        val bookId = 1L
        val book = createTestBook(id = bookId, favorite = false)
        
        coEvery { getBook.subscribe(bookId) } returns flowOf(book)
        coEvery { getChapters.subscribe(bookId) } returns flowOf(emptyList())
        coEvery { toggleFavorite.await(book) } returns true
        
        screenModel = BookDetailScreenModelNew(bookId, getBook, getChapters, toggleFavorite)
        advanceUntilIdle()
        
        // When
        screenModel.toggleBookFavorite()
        advanceUntilIdle()
        
        // Then
        val state = screenModel.state.value
        assertFalse(state.isTogglingFavorite)
        assertNull(state.error)
        coVerify { toggleFavorite.await(book) }
    }
    
    @Test
    fun `toggleBookFavorite sets error when toggle fails`() = runTest {
        // Given
        val bookId = 1L
        val book = createTestBook(id = bookId, favorite = false)
        
        coEvery { getBook.subscribe(bookId) } returns flowOf(book)
        coEvery { getChapters.subscribe(bookId) } returns flowOf(emptyList())
        coEvery { toggleFavorite.await(book) } returns false
        
        screenModel = BookDetailScreenModelNew(bookId, getBook, getChapters, toggleFavorite)
        advanceUntilIdle()
        
        // When
        screenModel.toggleBookFavorite()
        advanceUntilIdle()
        
        // Then
        val state = screenModel.state.value
        assertFalse(state.isTogglingFavorite)
        assertEquals("Failed to update favorite status", state.error)
    }
    
    @Test
    fun `clearError removes error from state`() = runTest {
        // Given
        val bookId = 1L
        coEvery { getBook.subscribe(bookId) } returns flowOf(null)
        coEvery { getChapters.subscribe(bookId) } returns flowOf(emptyList())
        
        screenModel = BookDetailScreenModelNew(bookId, getBook, getChapters, toggleFavorite)
        advanceUntilIdle()
        
        // Verify error is set
        assertEquals("Book not found", screenModel.state.value.error)
        
        // When
        screenModel.clearError()
        
        // Then
        assertNull(screenModel.state.value.error)
    }
    
    private fun createTestBook(
        id: Long = 1L,
        sourceId: Long = 1L,
        title: String = "Test Book",
        key: String = "test-key",
        favorite: Boolean = false
    ) = Book(
        id = id,
        sourceId = sourceId,
        title = title,
        key = key,
        favorite = favorite
    )
    
    private fun createTestChapter(
        id: Long = 1L,
        bookId: Long = 1L,
        name: String = "Test Chapter",
        key: String = "test-chapter-key"
    ) = Chapter(
        id = id,
        bookId = bookId,
        name = name,
        key = key
    )
}