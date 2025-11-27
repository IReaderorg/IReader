package ireader.domain.usecases.download

import ireader.domain.services.common.DownloadService
import ireader.domain.services.common.ServiceResult
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Unit tests for download use cases
 * Tests critical download functionality including single/batch downloads and queue management
 */
class DownloadUseCasesTest {
    
    private lateinit var downloadChapterUseCase: DownloadChapterUseCase
    private lateinit var downloadService: DownloadService
    
    @BeforeTest
    fun setup() {
        downloadService = mockk()
        downloadChapterUseCase = DownloadChapterUseCase(downloadService)
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `downloadChapter should queue single chapter successfully`() = runTest {
        // Given
        val chapterId = 1L
        coEvery { downloadService.queueChapters(listOf(chapterId)) } returns ServiceResult.Success(Unit)
        
        // When
        val result = downloadChapterUseCase(chapterId)
        
        // Then
        assertTrue(result is ServiceResult.Success)
        coVerify { downloadService.queueChapters(listOf(chapterId)) }
    }
    
    @Test
    fun `downloadChapter should handle download failure`() = runTest {
        // Given
        val chapterId = 1L
        val error = Exception("Network error")
        coEvery { downloadService.queueChapters(listOf(chapterId)) } returns ServiceResult.Error(error)
        
        // When
        val result = downloadChapterUseCase(chapterId)
        
        // Then
        assertTrue(result is ServiceResult.Error)
        assertEquals(error, (result as ServiceResult.Error).error)
    }
    
    @Test
    fun `downloadChapter should handle invalid chapter ID`() = runTest {
        // Given
        val invalidChapterId = -1L
        coEvery { downloadService.queueChapters(listOf(invalidChapterId)) } returns 
            ServiceResult.Error(IllegalArgumentException("Invalid chapter ID"))
        
        // When
        val result = downloadChapterUseCase(invalidChapterId)
        
        // Then
        assertTrue(result is ServiceResult.Error)
    }
}

/**
 * Unit tests for DownloadManagerUseCase
 * Tests download queue management and batch operations
 */
class DownloadManagerUseCaseTest {
    
    private lateinit var downloadManagerUseCase: DownloadManagerUseCase
    private lateinit var downloadService: DownloadService
    
    @BeforeTest
    fun setup() {
        downloadService = mockk()
        downloadManagerUseCase = DownloadManagerUseCase(downloadService)
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `queueChapters should add multiple chapters to download queue`() = runTest {
        // Given
        val chapterIds = listOf(1L, 2L, 3L, 4L, 5L)
        coEvery { downloadService.queueChapters(chapterIds) } returns ServiceResult.Success(Unit)
        
        // When
        val result = downloadManagerUseCase.queueChapters(chapterIds)
        
        // Then
        assertTrue(result is ServiceResult.Success)
        coVerify { downloadService.queueChapters(chapterIds) }
    }
    
    @Test
    fun `queueChapters should handle empty list`() = runTest {
        // Given
        val emptyList = emptyList<Long>()
        coEvery { downloadService.queueChapters(emptyList) } returns ServiceResult.Success(Unit)
        
        // When
        val result = downloadManagerUseCase.queueChapters(emptyList)
        
        // Then
        assertTrue(result is ServiceResult.Success)
    }
    
    @Test
    fun `cancelDownload should remove chapter from queue`() = runTest {
        // Given
        val chapterId = 1L
        coEvery { downloadService.cancelDownload(chapterId) } returns ServiceResult.Success(Unit)
        
        // When
        val result = downloadManagerUseCase.cancelDownload(chapterId)
        
        // Then
        assertTrue(result is ServiceResult.Success)
        coVerify { downloadService.cancelDownload(chapterId) }
    }
    
    @Test
    fun `pauseDownload should pause active download`() = runTest {
        // Given
        val chapterId = 1L
        coEvery { downloadService.pauseDownload(chapterId) } returns ServiceResult.Success(Unit)
        
        // When
        val result = downloadManagerUseCase.pauseDownload(chapterId)
        
        // Then
        assertTrue(result is ServiceResult.Success)
        coVerify { downloadService.pauseDownload(chapterId) }
    }
    
    @Test
    fun `resumeDownload should resume paused download`() = runTest {
        // Given
        val chapterId = 1L
        coEvery { downloadService.resumeDownload(chapterId) } returns ServiceResult.Success(Unit)
        
        // When
        val result = downloadManagerUseCase.resumeDownload(chapterId)
        
        // Then
        assertTrue(result is ServiceResult.Success)
        coVerify { downloadService.resumeDownload(chapterId) }
    }
    
    @Test
    fun `clearQueue should remove all pending downloads`() = runTest {
        // Given
        coEvery { downloadService.clearQueue() } returns ServiceResult.Success(Unit)
        
        // When
        val result = downloadManagerUseCase.clearQueue()
        
        // Then
        assertTrue(result is ServiceResult.Success)
        coVerify { downloadService.clearQueue() }
    }
    
    @Test
    fun `getDownloadStatus should return current download state`() = runTest {
        // Given
        val chapterId = 1L
        val expectedStatus = "Downloading"
        coEvery { downloadService.getDownloadStatus(chapterId) } returns expectedStatus
        
        // When
        val status = downloadManagerUseCase.getDownloadStatus(chapterId)
        
        // Then
        assertEquals(expectedStatus, status)
        coVerify { downloadService.getDownloadStatus(chapterId) }
    }
}

/**
 * Unit tests for batch download operations
 */
class DownloadChaptersUseCaseTest {
    
    private lateinit var downloadChaptersUseCase: DownloadChaptersUseCase
    private lateinit var downloadService: DownloadService
    
    @BeforeTest
    fun setup() {
        downloadService = mockk()
        downloadChaptersUseCase = DownloadChaptersUseCase(downloadService)
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `downloadChapters should queue all chapters for a book`() = runTest {
        // Given
        val bookId = 1L
        val chapterIds = listOf(1L, 2L, 3L, 4L, 5L)
        coEvery { downloadService.queueChaptersForBook(bookId) } returns ServiceResult.Success(chapterIds.size)
        
        // When
        val result = downloadChaptersUseCase(bookId)
        
        // Then
        assertTrue(result is ServiceResult.Success)
        assertEquals(5, (result as ServiceResult.Success).data)
        coVerify { downloadService.queueChaptersForBook(bookId) }
    }
    
    @Test
    fun `downloadChapters should handle book with no chapters`() = runTest {
        // Given
        val bookId = 1L
        coEvery { downloadService.queueChaptersForBook(bookId) } returns ServiceResult.Success(0)
        
        // When
        val result = downloadChaptersUseCase(bookId)
        
        // Then
        assertTrue(result is ServiceResult.Success)
        assertEquals(0, (result as ServiceResult.Success).data)
    }
    
    @Test
    fun `downloadChapters should handle network errors`() = runTest {
        // Given
        val bookId = 1L
        val error = Exception("Network unavailable")
        coEvery { downloadService.queueChaptersForBook(bookId) } returns ServiceResult.Error(error)
        
        // When
        val result = downloadChaptersUseCase(bookId)
        
        // Then
        assertTrue(result is ServiceResult.Error)
        assertEquals(error, (result as ServiceResult.Error).error)
    }
    
    @Test
    fun `downloadUnreadChapters should only queue unread chapters`() = runTest {
        // Given
        val bookId = 1L
        val unreadChapterIds = listOf(3L, 4L, 5L)
        coEvery { downloadService.queueUnreadChapters(bookId) } returns ServiceResult.Success(unreadChapterIds.size)
        
        // When
        val result = downloadChaptersUseCase.downloadUnreadOnly(bookId)
        
        // Then
        assertTrue(result is ServiceResult.Success)
        assertEquals(3, (result as ServiceResult.Success).data)
        coVerify { downloadService.queueUnreadChapters(bookId) }
    }
}
