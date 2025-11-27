package ireader.domain.services

import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.History
import io.mockk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Comprehensive tests for ReadingProgressManager
 * Tests progress tracking, history updates, and reading session management
 */
class ReadingProgressManagerTest {
    
    private lateinit var readingProgressManager: ReadingProgressManager
    private lateinit var historyRepository: HistoryRepository
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() {
        historyRepository = mockk()
        readingProgressManager = ReadingProgressManager(
            historyRepository = historyRepository,
            coroutineScope = CoroutineScope(testDispatcher)
        )
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `updateReadingProgress should save progress to history`() = runTest(testDispatcher) {
        // Given
        val chapterId = 1L
        val progress = 0.5f
        val readDuration = 60000L
        
        coEvery { historyRepository.upsert(any(), any(), any(), any()) } just Runs
        
        // When
        readingProgressManager.updateReadingProgress(chapterId, progress, readDuration)
        advanceUntilIdle()
        
        // Then
        coVerify {
            historyRepository.upsert(
                chapterId = chapterId,
                readAt = any(),
                readDuration = readDuration,
                progress = progress
            )
        }
    }
    
    @Test
    fun `markChapterCompleted should set progress to 100 percent`() = runTest(testDispatcher) {
        // Given
        val chapterId = 1L
        val readDuration = 120000L
        
        coEvery { historyRepository.upsert(any(), any(), any(), any()) } just Runs
        
        // When
        readingProgressManager.markChapterCompleted(chapterId, readDuration)
        advanceUntilIdle()
        
        // Then
        coVerify {
            historyRepository.upsert(
                chapterId = chapterId,
                readAt = any(),
                readDuration = readDuration,
                progress = 1.0f
            )
        }
    }
    
    @Test
    fun `startReading should initialize progress for new chapter`() = runTest(testDispatcher) {
        // Given
        val chapterId = 1L
        coEvery { historyRepository.findHistoryByChapterId(chapterId) } returns null
        coEvery { historyRepository.upsert(any(), any(), any(), any()) } just Runs
        
        // When
        readingProgressManager.startReading(chapterId)
        advanceUntilIdle()
        
        // Then
        coVerify {
            historyRepository.upsert(
                chapterId = chapterId,
                readAt = any(),
                readDuration = 0L,
                progress = 0.01f
            )
        }
    }
    
    @Test
    fun `startReading should not reinitialize if progress exists`() = runTest(testDispatcher) {
        // Given
        val chapterId = 1L
        val existingHistory = History(
            id = 1L,
            bookId = 1L,
            chapterId = chapterId,
            readAt = System.currentTimeMillis(),
            readDuration = 60000L,
            progress = 0.5f
        )
        coEvery { historyRepository.findHistoryByChapterId(chapterId) } returns existingHistory
        
        // When
        readingProgressManager.startReading(chapterId)
        advanceUntilIdle()
        
        // Then
        coVerify(exactly = 0) { historyRepository.upsert(any(), any(), any(), any()) }
    }
    
    @Test
    fun `updatePageProgress should calculate correct progress percentage`() = runTest(testDispatcher) {
        // Given
        val chapterId = 1L
        val currentPage = 50
        val totalPages = 100
        val expectedProgress = 0.5f
        
        coEvery { historyRepository.upsert(any(), any(), any(), any()) } just Runs
        
        // When
        readingProgressManager.updatePageProgress(chapterId, currentPage, totalPages)
        advanceUntilIdle()
        
        // Then
        coVerify {
            historyRepository.upsert(
                chapterId = chapterId,
                readAt = any(),
                readDuration = 0L,
                progress = expectedProgress
            )
        }
    }
    
    @Test
    fun `updatePageProgress should handle first page correctly`() = runTest(testDispatcher) {
        // Given
        val chapterId = 1L
        val currentPage = 1
        val totalPages = 100
        val expectedProgress = 0.01f
        
        coEvery { historyRepository.upsert(any(), any(), any(), any()) } just Runs
        
        // When
        readingProgressManager.updatePageProgress(chapterId, currentPage, totalPages)
        advanceUntilIdle()
        
        // Then
        coVerify {
            historyRepository.upsert(
                chapterId = chapterId,
                readAt = any(),
                readDuration = 0L,
                progress = expectedProgress
            )
        }
    }
    
    @Test
    fun `updatePageProgress should handle last page correctly`() = runTest(testDispatcher) {
        // Given
        val chapterId = 1L
        val currentPage = 100
        val totalPages = 100
        val expectedProgress = 1.0f
        
        coEvery { historyRepository.upsert(any(), any(), any(), any()) } just Runs
        
        // When
        readingProgressManager.updatePageProgress(chapterId, currentPage, totalPages)
        advanceUntilIdle()
        
        // Then
        coVerify {
            historyRepository.upsert(
                chapterId = chapterId,
                readAt = any(),
                readDuration = 0L,
                progress = expectedProgress
            )
        }
    }
    
    @Test
    fun `updatePageProgress should handle zero total pages`() = runTest(testDispatcher) {
        // Given
        val chapterId = 1L
        val currentPage = 0
        val totalPages = 0
        
        coEvery { historyRepository.upsert(any(), any(), any(), any()) } just Runs
        
        // When
        readingProgressManager.updatePageProgress(chapterId, currentPage, totalPages)
        advanceUntilIdle()
        
        // Then - Should not call upsert
        coVerify(exactly = 0) { historyRepository.upsert(any(), any(), any(), any()) }
    }
    
    @Test
    fun `resetBookProgress should clear history for book`() = runTest(testDispatcher) {
        // Given
        val bookId = 1L
        coEvery { historyRepository.resetHistoryByBookId(bookId) } just Runs
        
        // When
        readingProgressManager.resetBookProgress(bookId)
        advanceUntilIdle()
        
        // Then
        coVerify { historyRepository.resetHistoryByBookId(bookId) }
    }
    
    @Test
    fun `getChapterProgress should return progress from history`() = runTest {
        // Given
        val chapterId = 1L
        val expectedProgress = 0.75f
        val history = History(
            id = 1L,
            bookId = 1L,
            chapterId = chapterId,
            readAt = System.currentTimeMillis(),
            readDuration = 60000L,
            progress = expectedProgress
        )
        coEvery { historyRepository.findHistoryByChapterId(chapterId) } returns history
        
        // When
        val progress = readingProgressManager.getChapterProgress(chapterId)
        
        // Then
        assertEquals(expectedProgress, progress)
    }
    
    @Test
    fun `getChapterProgress should return null when no history exists`() = runTest {
        // Given
        val chapterId = 1L
        coEvery { historyRepository.findHistoryByChapterId(chapterId) } returns null
        
        // When
        val progress = readingProgressManager.getChapterProgress(chapterId)
        
        // Then
        assertNull(progress)
    }
    
    @Test
    fun `getLastReadChapter should return last read chapter ID`() = runTest {
        // Given
        val bookId = 1L
        val expectedChapterId = 5L
        val history = History(
            id = 1L,
            bookId = bookId,
            chapterId = expectedChapterId,
            readAt = System.currentTimeMillis(),
            readDuration = 60000L,
            progress = 0.5f
        )
        coEvery { historyRepository.findHistoryByBookId(bookId) } returns history
        
        // When
        val chapterId = readingProgressManager.getLastReadChapter(bookId)
        
        // Then
        assertEquals(expectedChapterId, chapterId)
    }
    
    @Test
    fun `getLastReadChapter should return null when no history exists`() = runTest {
        // Given
        val bookId = 1L
        coEvery { historyRepository.findHistoryByBookId(bookId) } returns null
        
        // When
        val chapterId = readingProgressManager.getLastReadChapter(bookId)
        
        // Then
        assertNull(chapterId)
    }
    
    @Test
    fun `updateReadingProgress should handle concurrent updates`() = runTest(testDispatcher) {
        // Given
        val chapterId = 1L
        coEvery { historyRepository.upsert(any(), any(), any(), any()) } just Runs
        
        // When - Multiple rapid updates
        readingProgressManager.updateReadingProgress(chapterId, 0.1f)
        readingProgressManager.updateReadingProgress(chapterId, 0.2f)
        readingProgressManager.updateReadingProgress(chapterId, 0.3f)
        advanceUntilIdle()
        
        // Then - All updates should be processed
        coVerify(exactly = 3) { historyRepository.upsert(any(), any(), any(), any()) }
    }
}
