package ireader.data.repository.consolidated

import ireader.data.core.DatabaseHandler
import ireader.domain.models.entities.Chapter
import ireader.domain.models.updates.ChapterUpdate
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ChapterRepositoryTest {
    
    private lateinit var repository: ChapterRepositoryImpl
    private lateinit var handler: DatabaseHandler
    
    @BeforeTest
    fun setup() {
        handler = mockk()
        repository = ChapterRepositoryImpl(handler)
    }
    
    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `getChapterById returns chapter when found`() = runTest {
        // Given
        val chapterId = 1L
        val expectedChapter = createTestChapter(id = chapterId)
        coEvery { handler.awaitOneOrNull<Chapter>(any()) } returns expectedChapter
        
        // When
        val result = repository.getChapterById(chapterId)
        
        // Then
        assertEquals(expectedChapter, result)
        coVerify { handler.awaitOneOrNull<Chapter>(any()) }
    }
    
    @Test
    fun `getChapterById returns null when not found`() = runTest {
        // Given
        val chapterId = 1L
        coEvery { handler.awaitOneOrNull<Chapter>(any()) } returns null
        
        // When
        val result = repository.getChapterById(chapterId)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `getChapterById returns null when database fails`() = runTest {
        // Given
        val chapterId = 1L
        coEvery { handler.awaitOneOrNull<Chapter>(any()) } throws RuntimeException("Database error")
        
        // When
        val result = repository.getChapterById(chapterId)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `getChapterByIdAsFlow returns flow of chapter`() = runTest {
        // Given
        val chapterId = 1L
        val expectedChapter = createTestChapter(id = chapterId)
        every { handler.subscribeToOneOrNull<Chapter>(any()) } returns flowOf(expectedChapter)
        
        // When
        val result = repository.getChapterByIdAsFlow(chapterId)
        
        // Then
        result.collect { chapter ->
            assertEquals(expectedChapter, chapter)
        }
    }
    
    @Test
    fun `getChaptersByBookId returns chapters for book`() = runTest {
        // Given
        val bookId = 1L
        val chapters = listOf(
            createTestChapter(id = 1L, bookId = bookId),
            createTestChapter(id = 2L, bookId = bookId)
        )
        coEvery { handler.awaitList<Chapter>(any()) } returns chapters
        
        // When
        val result = repository.getChaptersByBookId(bookId)
        
        // Then
        assertEquals(chapters, result)
    }
    
    @Test
    fun `getChaptersByBookId returns empty list when database fails`() = runTest {
        // Given
        val bookId = 1L
        coEvery { handler.awaitList<Chapter>(any()) } throws RuntimeException("Database error")
        
        // When
        val result = repository.getChaptersByBookId(bookId)
        
        // Then
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `getChaptersByBookIdAsFlow returns flow of chapters`() = runTest {
        // Given
        val bookId = 1L
        val chapters = listOf(
            createTestChapter(id = 1L, bookId = bookId),
            createTestChapter(id = 2L, bookId = bookId)
        )
        every { handler.subscribeToList<Chapter>(any()) } returns flowOf(chapters)
        
        // When
        val result = repository.getChaptersByBookIdAsFlow(bookId)
        
        // Then
        result.collect { chapterList ->
            assertEquals(chapters, chapterList)
        }
    }
    
    @Test
    fun `getLastReadChapter returns last read chapter`() = runTest {
        // Given
        val bookId = 1L
        val lastReadChapter = createTestChapter(id = 1L, bookId = bookId, read = true)
        coEvery { handler.awaitOneOrNull<Chapter>(any()) } returns lastReadChapter
        
        // When
        val result = repository.getLastReadChapter(bookId)
        
        // Then
        assertEquals(lastReadChapter, result)
    }
    
    @Test
    fun `addAll returns inserted chapters with IDs`() = runTest {
        // Given
        val chapters = listOf(
            createTestChapter(id = 0L, name = "Chapter 1"),
            createTestChapter(id = 0L, name = "Chapter 2")
        )
        coEvery { handler.await<Unit>(inTransaction = true, any()) } returns Unit
        
        // When
        val result = repository.addAll(chapters)
        
        // Then
        assertEquals(chapters.size, result.size)
        coVerify { handler.await<Unit>(inTransaction = true, any()) }
    }
    
    @Test
    fun `addAll returns empty list when fails`() = runTest {
        // Given
        val chapters = listOf(
            createTestChapter(id = 0L, name = "Chapter 1"),
            createTestChapter(id = 0L, name = "Chapter 2")
        )
        coEvery { handler.await<Unit>(inTransaction = true, any()) } throws RuntimeException("Insert failed")
        
        // When
        val result = repository.addAll(chapters)
        
        // Then
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `update returns true when successful`() = runTest {
        // Given
        val update = ChapterUpdate(id = 1L, name = "Updated Chapter")
        coEvery { handler.await<Unit>(any()) } returns Unit
        
        // When
        val result = repository.update(update)
        
        // Then
        assertTrue(result)
        coVerify { handler.await<Unit>(any()) }
    }
    
    @Test
    fun `update returns false when fails`() = runTest {
        // Given
        val update = ChapterUpdate(id = 1L, name = "Updated Chapter")
        coEvery { handler.await<Unit>(any()) } throws RuntimeException("Update failed")
        
        // When
        val result = repository.update(update)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `updateAll returns true when all updates successful`() = runTest {
        // Given
        val updates = listOf(
            ChapterUpdate(id = 1L, name = "Chapter 1"),
            ChapterUpdate(id = 2L, name = "Chapter 2")
        )
        coEvery { handler.await<Unit>(inTransaction = true, any()) } returns Unit
        
        // When
        val result = repository.updateAll(updates)
        
        // Then
        assertTrue(result)
        coVerify { handler.await<Unit>(inTransaction = true, any()) }
    }
    
    @Test
    fun `updateAll returns false when transaction fails`() = runTest {
        // Given
        val updates = listOf(
            ChapterUpdate(id = 1L, name = "Chapter 1"),
            ChapterUpdate(id = 2L, name = "Chapter 2")
        )
        coEvery { handler.await<Unit>(inTransaction = true, any()) } throws RuntimeException("Transaction failed")
        
        // When
        val result = repository.updateAll(updates)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `removeChaptersWithIds returns true when successful`() = runTest {
        // Given
        val chapterIds = listOf(1L, 2L, 3L)
        coEvery { handler.await<Unit>(inTransaction = true, any()) } returns Unit
        
        // When
        val result = repository.removeChaptersWithIds(chapterIds)
        
        // Then
        assertTrue(result)
        coVerify { handler.await<Unit>(inTransaction = true, any()) }
    }
    
    @Test
    fun `removeChaptersWithIds returns false when fails`() = runTest {
        // Given
        val chapterIds = listOf(1L, 2L, 3L)
        coEvery { handler.await<Unit>(inTransaction = true, any()) } throws RuntimeException("Delete failed")
        
        // When
        val result = repository.removeChaptersWithIds(chapterIds)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `removeChaptersByBookId returns true when successful`() = runTest {
        // Given
        val bookId = 1L
        coEvery { handler.await<Unit>(any()) } returns Unit
        
        // When
        val result = repository.removeChaptersByBookId(bookId)
        
        // Then
        assertTrue(result)
        coVerify { handler.await<Unit>(any()) }
    }
    
    @Test
    fun `removeChaptersByBookId returns false when fails`() = runTest {
        // Given
        val bookId = 1L
        coEvery { handler.await<Unit>(any()) } throws RuntimeException("Delete failed")
        
        // When
        val result = repository.removeChaptersByBookId(bookId)
        
        // Then
        assertFalse(result)
    }
    
    private fun createTestChapter(
        id: Long = 1L,
        bookId: Long = 1L,
        url: String = "test-url",
        name: String = "Test Chapter",
        scanlator: String? = null,
        read: Boolean = false,
        bookmark: Boolean = false,
        lastPageRead: Long = 0L,
        chapterNumber: Float = 1.0f,
        sourceOrder: Long = 1L,
        dateFetch: Long = 0L,
        dateUpload: Long = 0L
    ): Chapter {
        return Chapter(
            id = id,
            bookId = bookId,
            url = url,
            name = name,
            scanlator = scanlator,
            read = read,
            bookmark = bookmark,
            lastPageRead = lastPageRead,
            chapterNumber = chapterNumber,
            sourceOrder = sourceOrder,
            dateFetch = dateFetch,
            dateUpload = dateUpload
        )
    }
}