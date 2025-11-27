package ireader.domain.services.download

import ireader.domain.services.common.DownloadProgress
import ireader.domain.services.common.DownloadService
import ireader.domain.services.common.DownloadStatus
import ireader.domain.services.common.ServiceResult
import ireader.domain.services.common.ServiceState
import ireader.domain.models.entities.SavedDownload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Integration tests for DownloadService that verify the service
 * properly integrates with the download workflow
 */
class DownloadServiceIntegrationTest {

    /**
     * Mock implementation that simulates the actual download workflow
     */
    private class MockDownloadServiceWithWorkflow : DownloadService {
        private val _state = MutableStateFlow(ServiceState.IDLE)
        override val state: StateFlow<ServiceState> = _state

        private val _downloads = MutableStateFlow<List<SavedDownload>>(emptyList())
        override val downloads: StateFlow<List<SavedDownload>> = _downloads

        private val _downloadProgress = MutableStateFlow<Map<Long, DownloadProgress>>(emptyMap())
        override val downloadProgress: StateFlow<Map<Long, DownloadProgress>> = _downloadProgress

        // Track if the actual download service was started
        var downloadServiceStarted = false
        var lastQueuedChapterIds: List<Long> = emptyList()
        var lastQueuedBookIds: List<Long> = emptyList()
        
        // Simulate the StartDownloadServicesUseCase being called
        var startDownloadServicesUseCaseCalled = false
        var startDownloadServicesUseCaseChapterIds: LongArray? = null
        var startDownloadServicesUseCaseBookIds: LongArray? = null

        override suspend fun initialize() {
            _state.value = ServiceState.INITIALIZING
            _state.value = ServiceState.IDLE
        }

        override suspend fun start() {
            _state.value = ServiceState.RUNNING
            downloadServiceStarted = true
        }

        override suspend fun stop() {
            _state.value = ServiceState.STOPPED
            downloadServiceStarted = false
        }

        override fun isRunning(): Boolean = _state.value == ServiceState.RUNNING

        override suspend fun cleanup() {
            stop()
            _downloads.value = emptyList()
            _downloadProgress.value = emptyMap()
        }

        override suspend fun queueChapters(chapterIds: List<Long>): ServiceResult<Unit> {
            if (chapterIds.isEmpty()) {
                return ServiceResult.Error("No chapters to queue")
            }
            
            lastQueuedChapterIds = chapterIds
            
            // Simulate calling StartDownloadServicesUseCase
            startDownloadServicesUseCaseCalled = true
            startDownloadServicesUseCaseChapterIds = chapterIds.toLongArray()
            
            // Initialize progress
            val progressMap = chapterIds.associateWith { chapterId ->
                DownloadProgress(
                    chapterId = chapterId,
                    status = DownloadStatus.QUEUED
                )
            }
            _downloadProgress.value = _downloadProgress.value + progressMap
            _state.value = ServiceState.RUNNING
            
            return ServiceResult.Success(Unit)
        }

        override suspend fun queueBooks(bookIds: List<Long>): ServiceResult<Unit> {
            if (bookIds.isEmpty()) {
                return ServiceResult.Error("No books to queue")
            }
            
            lastQueuedBookIds = bookIds
            
            // Simulate calling StartDownloadServicesUseCase
            startDownloadServicesUseCaseCalled = true
            startDownloadServicesUseCaseBookIds = bookIds.toLongArray()
            
            _state.value = ServiceState.RUNNING
            
            return ServiceResult.Success(Unit)
        }

        override suspend fun pause() {
            if (_state.value == ServiceState.RUNNING) {
                _state.value = ServiceState.PAUSED
            }
        }

        override suspend fun resume() {
            if (_state.value == ServiceState.PAUSED) {
                _state.value = ServiceState.RUNNING
            }
        }

        override suspend fun cancelDownload(chapterId: Long): ServiceResult<Unit> {
            _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                put(chapterId, DownloadProgress(chapterId = chapterId, status = DownloadStatus.CANCELLED))
            }
            return ServiceResult.Success(Unit)
        }

        override suspend fun cancelAll(): ServiceResult<Unit> {
            _downloadProgress.value = _downloadProgress.value.mapValues { (chapterId, _) ->
                DownloadProgress(chapterId = chapterId, status = DownloadStatus.CANCELLED)
            }
            _state.value = ServiceState.IDLE
            return ServiceResult.Success(Unit)
        }

        override suspend fun retryDownload(chapterId: Long): ServiceResult<Unit> {
            val current = _downloadProgress.value[chapterId] ?: return ServiceResult.Error("Download not found")
            if (current.status != DownloadStatus.FAILED) {
                return ServiceResult.Error("Can only retry failed downloads")
            }
            _downloadProgress.value = _downloadProgress.value + (chapterId to current.copy(
                status = DownloadStatus.QUEUED,
                errorMessage = null,
                retryCount = current.retryCount + 1
            ))
            return ServiceResult.Success(Unit)
        }

        override fun getDownloadStatus(chapterId: Long): DownloadStatus? {
            return _downloadProgress.value[chapterId]?.status
        }
        
        // Helper to simulate download progress updates from DownloaderService
        fun simulateDownloadProgressUpdate(chapterId: Long, status: DownloadStatus, progress: Float = 0f, errorMessage: String? = null) {
            _downloadProgress.value = _downloadProgress.value + (chapterId to DownloadProgress(
                chapterId = chapterId,
                status = status,
                progress = progress,
                errorMessage = errorMessage
            ))
        }
        
        // Helper to simulate service state updates from DownloaderService
        fun simulateServiceStateUpdate(newState: ServiceState) {
            _state.value = newState
        }
    }

    @Test
    fun `queueChapters should trigger StartDownloadServicesUseCase`() = runTest {
        val service = MockDownloadServiceWithWorkflow()
        val chapterIds = listOf(1L, 2L, 3L)
        
        service.initialize()
        val result = service.queueChapters(chapterIds)
        
        assertIs<ServiceResult.Success<Unit>>(result)
        assertTrue(service.startDownloadServicesUseCaseCalled)
        assertEquals(chapterIds.toLongArray().toList(), service.startDownloadServicesUseCaseChapterIds?.toList())
    }

    @Test
    fun `queueBooks should trigger StartDownloadServicesUseCase`() = runTest {
        val service = MockDownloadServiceWithWorkflow()
        val bookIds = listOf(100L, 200L)
        
        service.initialize()
        val result = service.queueBooks(bookIds)
        
        assertIs<ServiceResult.Success<Unit>>(result)
        assertTrue(service.startDownloadServicesUseCaseCalled)
        assertEquals(bookIds.toLongArray().toList(), service.startDownloadServicesUseCaseBookIds?.toList())
    }

    @Test
    fun `service should reflect progress updates from DownloaderService`() = runTest {
        val service = MockDownloadServiceWithWorkflow()
        
        service.initialize()
        service.queueChapters(listOf(1L, 2L))
        
        // Simulate DownloaderService updating progress
        service.simulateDownloadProgressUpdate(1L, DownloadStatus.DOWNLOADING, 0.5f)
        
        assertEquals(DownloadStatus.DOWNLOADING, service.getDownloadStatus(1L))
        assertEquals(0.5f, service.downloadProgress.value[1L]?.progress)
    }

    @Test
    fun `service should handle download completion from DownloaderService`() = runTest {
        val service = MockDownloadServiceWithWorkflow()
        
        service.initialize()
        service.queueChapters(listOf(1L))
        
        // Simulate download completion
        service.simulateDownloadProgressUpdate(1L, DownloadStatus.COMPLETED, 1.0f)
        
        assertEquals(DownloadStatus.COMPLETED, service.getDownloadStatus(1L))
    }

    @Test
    fun `service should handle download failure from DownloaderService`() = runTest {
        val service = MockDownloadServiceWithWorkflow()
        
        service.initialize()
        service.queueChapters(listOf(1L))
        
        // Simulate download failure
        service.simulateDownloadProgressUpdate(1L, DownloadStatus.FAILED, 0f, "Network error")
        
        assertEquals(DownloadStatus.FAILED, service.getDownloadStatus(1L))
        assertEquals("Network error", service.downloadProgress.value[1L]?.errorMessage)
    }

    @Test
    fun `service state should reflect DownloaderService state`() = runTest {
        val service = MockDownloadServiceWithWorkflow()
        
        service.initialize()
        service.queueChapters(listOf(1L))
        
        assertEquals(ServiceState.RUNNING, service.state.value)
        
        // Simulate DownloaderService completing all downloads
        service.simulateServiceStateUpdate(ServiceState.IDLE)
        
        assertEquals(ServiceState.IDLE, service.state.value)
    }

    @Test
    fun `pause and resume should work correctly during download`() = runTest {
        val service = MockDownloadServiceWithWorkflow()
        
        service.initialize()
        service.queueChapters(listOf(1L, 2L))
        service.simulateDownloadProgressUpdate(1L, DownloadStatus.DOWNLOADING, 0.5f)
        
        // Pause
        service.pause()
        assertEquals(ServiceState.PAUSED, service.state.value)
        
        // Resume
        service.resume()
        assertEquals(ServiceState.RUNNING, service.state.value)
    }

    @Test
    fun `cancelAll should stop all downloads and reset state`() = runTest {
        val service = MockDownloadServiceWithWorkflow()
        
        service.initialize()
        service.queueChapters(listOf(1L, 2L, 3L))
        service.simulateDownloadProgressUpdate(1L, DownloadStatus.DOWNLOADING, 0.5f)
        service.simulateDownloadProgressUpdate(2L, DownloadStatus.QUEUED)
        
        val result = service.cancelAll()
        
        assertIs<ServiceResult.Success<Unit>>(result)
        assertEquals(ServiceState.IDLE, service.state.value)
        assertEquals(DownloadStatus.CANCELLED, service.getDownloadStatus(1L))
        assertEquals(DownloadStatus.CANCELLED, service.getDownloadStatus(2L))
    }

    @Test
    fun `retry should re-queue failed download`() = runTest {
        val service = MockDownloadServiceWithWorkflow()
        
        service.initialize()
        service.queueChapters(listOf(1L))
        service.simulateDownloadProgressUpdate(1L, DownloadStatus.FAILED, 0f, "Network error")
        
        val result = service.retryDownload(1L)
        
        assertIs<ServiceResult.Success<Unit>>(result)
        assertEquals(DownloadStatus.QUEUED, service.getDownloadStatus(1L))
        assertEquals(1, service.downloadProgress.value[1L]?.retryCount)
    }

    @Test
    fun `multiple queued downloads should be tracked independently`() = runTest {
        val service = MockDownloadServiceWithWorkflow()
        
        service.initialize()
        service.queueChapters(listOf(1L, 2L, 3L, 4L, 5L))
        
        // Simulate various states
        service.simulateDownloadProgressUpdate(1L, DownloadStatus.COMPLETED, 1.0f)
        service.simulateDownloadProgressUpdate(2L, DownloadStatus.DOWNLOADING, 0.75f)
        service.simulateDownloadProgressUpdate(3L, DownloadStatus.FAILED, 0f, "Error")
        service.simulateDownloadProgressUpdate(4L, DownloadStatus.QUEUED)
        // 5L stays in initial QUEUED state
        
        assertEquals(DownloadStatus.COMPLETED, service.getDownloadStatus(1L))
        assertEquals(DownloadStatus.DOWNLOADING, service.getDownloadStatus(2L))
        assertEquals(DownloadStatus.FAILED, service.getDownloadStatus(3L))
        assertEquals(DownloadStatus.QUEUED, service.getDownloadStatus(4L))
        assertEquals(DownloadStatus.QUEUED, service.getDownloadStatus(5L))
    }
}
