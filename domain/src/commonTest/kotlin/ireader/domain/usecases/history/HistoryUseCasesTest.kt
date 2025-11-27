package ireader.domain.usecases.history

import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.History
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Unit tests for history management use cases
 */
class HistoryUseCasesTest {
    
    private lateinit var getHistoryUseCase: GetHistoryUseCase
    private lateinit var updateHistoryUseCase: UpdateHistoryUseCase
    private lateinit var deleteHistoryUseCase: DeleteHistoryUseCase
    private lateinit var clearHistoryUseCase: ClearHistoryUseCase
    private lateinit var historyRepository: HistoryRepository
    
    @BeforeTest
    fun setup() {
        historyRepository = mockk()
        getHistoryUseCase = GetHistoryUseCase(historyRepository)
        updateHistoryUseCase = UpdateHistoryUseCase(historyRepository)
        deleteHistoryUseCase = DeleteHistoryUseCase(historyRepository)
        clearHistoryUseCase = ClearHistoryUseCase(historyRepository)
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `getHistory should return reading history`() = runTest {
        // Given
        val histories = listOf(
            createTestHistory(1L, bookId = 1L),
            createTestHistory(2L, bookId = 2L),
            createTestHistory(3L, bookId = 3L)
        )
        coEvery { historyRepository.findHistories() } returns flowOf(histories)
        
        // When
        val result = mutableListOf<List<History>>()
        getHistoryUseCase().collect { result.add(it) }
        
        // Then
        assertEquals(1, result.size)
        assertEquals(3, result.first().size)
    }
    
    @Test
    fun `updateHistory should record reading progress`() = runTest {
        // Given
        val bookId = 1L
        val chapterId = 5L
        coEvery { historyRepository.upsert(any()) } just Runs
        
        // When
        updateHistoryUseCase(bookId, chapterId)
        
        // Then
        coVerify {
            historyRepository.upsert(match { history ->
                history.bookId == bookId && history.chapterId == chapterId
            })
        }
    }
    
    @Test
    fun `deleteHistory should remove specific history entry`() = runTest {
        // Given
        val historyId = 1L
        coEvery { historyRepository.delete(historyId) } just Runs
        
        // When
        deleteHistoryUseCase(historyId)
        
        // Then
        coVerify { historyRepository.delete(historyId) }
    }
    
    @Test
    fun `clearHistory should remove all history entries`() = runTest {
        // Given
        coEvery { historyRepository.deleteAll() } just Runs
        
        // When
        clearHistoryUseCase()
        
        // Then
        coVerify { historyRepository.deleteAll() }
    }
    
    private fun createTestHistory(id: Long, bookId: Long): History {
        return History(
            id = id,
            bookId = bookId,
            chapterId = 1L,
            readAt = System.currentTimeMillis(),
            readDuration = 300000L
        )
    }
}
