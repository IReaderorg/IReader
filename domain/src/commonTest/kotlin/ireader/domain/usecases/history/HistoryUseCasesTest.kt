package ireader.domain.usecases.history

import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.History
import io.mockk.*
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
            createTestHistory(1L, chapterId = 1L),
            createTestHistory(2L, chapterId = 2L),
            createTestHistory(3L, chapterId = 3L)
        )
        coEvery { historyRepository.findHistories() } returns histories
        
        // When
        val result = getHistoryUseCase()
        
        // Then
        assertEquals(3, result.size)
    }
    
    @Test
    fun `updateHistory should record reading progress`() = runTest {
        // Given
        val chapterId = 5L
        val history = createTestHistory(0L, chapterId = chapterId)
        coEvery { historyRepository.insertHistory(any()) } just Runs
        
        // When
        updateHistoryUseCase(history)
        
        // Then
        coVerify {
            historyRepository.insertHistory(match { h ->
                h.chapterId == chapterId
            })
        }
    }
    
    @Test
    fun `deleteHistory should remove specific history entry`() = runTest {
        // Given
        val historyId = 1L
        coEvery { historyRepository.deleteHistory(historyId) } just Runs
        
        // When
        deleteHistoryUseCase(historyId)
        
        // Then
        coVerify { historyRepository.deleteHistory(historyId) }
    }
    
    @Test
    fun `clearHistory should remove all history entries`() = runTest {
        // Given
        coEvery { historyRepository.deleteAllHistories() } just Runs
        
        // When
        clearHistoryUseCase()
        
        // Then
        coVerify { historyRepository.deleteAllHistories() }
    }
    
    private fun createTestHistory(id: Long, chapterId: Long): History {
        return History(
            id = id,
            chapterId = chapterId,
            readAt = System.currentTimeMillis(),
            readDuration = 300000L,
            progress = 0.5f
        )
    }
}
