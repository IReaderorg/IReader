package ireader.domain.services.download

import ireader.domain.models.entities.SavedDownload
import ireader.domain.services.common.DownloadProgress
import ireader.domain.services.common.DownloadService
import ireader.domain.services.common.DownloadStatus
import ireader.domain.services.common.ServiceResult
import ireader.domain.services.common.ServiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for DownloadService interface contract
 * These tests verify the expected behavior of any DownloadService implementation
 */
class DownloadServiceTest {

    /**
     * Test implementation of DownloadService for unit testing
     */
    private class TestDownloadService : DownloadService {
        private val _state = MutableStateFlow(ServiceState.IDLE)
        override val state: StateFlow<ServiceState> = _state

        private val _downloads = MutableStateFlow<List<SavedDownload>>(emptyList())
        override val downloads: StateFlow<List<SavedDownload>> = _downloads

        private val _downloadProgress = MutableStateFlow<Map<Long, DownloadProgress>>(emptyMap())
        override val downloadProgress: StateFlow<Map<Long, DownloadProgress>> = _downloadProgress

        private var initialized = false
        private var queuedChapterIds = mutableListOf<Long>()
        private var queuedBookIds = mutableListOf<Long>()
        
        // Test helpers
        fun getQueuedChapterIds(): List<Long> = queuedChapterIds.toList()
        fun getQueuedBookIds(): List<Long> = queuedBookIds.toList()
        fun simulateDownloadProgress(chapterId: Long, status: DownloadStatus, progress: Float = 0f) {
            _downloadProgress.value = _downloadProgress.value + (chapterId to DownloadProgress(
                chapterId = chapterId,
                status = status,
                progress = progress
            ))
        }
        fun simulateStateChange(newState: ServiceState) {
            _state.value = newState
        }

        override suspend fun initialize() {
            _state.value = ServiceState.INITIALIZING
            initialized = true
            _state.value = ServiceState.IDLE
        }

        override suspend fun start() {
            if (!initialized) {
                throw IllegalStateException("Service not initialized")
            }
            _state.value = ServiceState.RUNNING
        }

        override suspend fun stop() {
            _state.value = ServiceState.STOPPED
            queuedChapterIds.clear()
            queuedBookIds.clear()
        }

        override fun isRunning(): Boolean = _state.value == ServiceState.RUNNING

        override suspend fun cleanup() {
            stop()
            _downloads.value = emptyList()
            _downloadProgress.value = emptyMap()
            initialized = false
        }

        override suspend fun queueChapters(chapterIds: List<Long>): ServiceResult<Unit> {
            return try {
                if (chapterIds.isEmpty()) {
                    return ServiceResult.Error("No chapters to queue")
                }
                queuedChapterIds.addAll(chapterIds)
                _state.value = ServiceState.RUNNING
                
                // Initialize progress for each chapter
                val progressMap = chapterIds.associateWith { chapterId ->
                    DownloadProgress(
                        chapterId = chapterId,
                        status = DownloadStatus.QUEUED
                    )
                }
                _downloadProgress.value = _downloadProgress.value + progressMap
                
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to queue chapters: ${e.message}", e)
            }
        }

        override suspend fun queueBooks(bookIds: List<Long>): ServiceResult<Unit> {
            return try {
                if (bookIds.isEmpty()) {
                    return ServiceResult.Error("No books to queue")
                }
                queuedBookIds.addAll(bookIds)
                _state.value = ServiceState.RUNNING
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to queue books: ${e.message}", e)
            }
        }

        override suspend fun pause() {
            if (_state.value == ServiceState.RUNNING) {
                _state.value = ServiceState.PAUSED
                // Update all downloading items to paused
                _downloadProgress.value = _downloadProgress.value.mapValues { (_, progress) ->
                    if (progress.status == DownloadStatus.DOWNLOADING) {
                        progress.copy(status = DownloadStatus.PAUSED)
                    } else {
                        progress
                    }
                }
            }
        }

        override suspend fun resume() {
            if (_state.value == ServiceState.PAUSED) {
                _state.value = ServiceState.RUNNING
                // Update all paused items back to downloading
                _downloadProgress.value = _downloadProgress.value.mapValues { (_, progress) ->
                    if (progress.status == DownloadStatus.PAUSED) {
                        progress.copy(status = DownloadStatus.DOWNLOADING)
                    } else {
                        progress
                    }
                }
            }
        }

        override suspend fun cancelDownload(chapterId: Long): ServiceResult<Unit> {
            return try {
                queuedChapterIds.remove(chapterId)
                _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                    put(chapterId, DownloadProgress(
                        chapterId = chapterId,
                        status = DownloadStatus.CANCELLED
                    ))
                }
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to cancel download: ${e.message}", e)
            }
        }

        override suspend fun cancelAll(): ServiceResult<Unit> {
            return try {
                queuedChapterIds.clear()
                queuedBookIds.clear()
                _downloadProgress.value = _downloadProgress.value.mapValues { (chapterId, _) ->
                    DownloadProgress(chapterId = chapterId, status = DownloadStatus.CANCELLED)
                }
                _state.value = ServiceState.IDLE
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to cancel all downloads: ${e.message}", e)
            }
        }

        override suspend fun retryDownload(chapterId: Long): ServiceResult<Unit> {
            return try {
                val current = _downloadProgress.value[chapterId]
                if (current == null) {
                    return ServiceResult.Error("Download not found")
                }
                if (current.status != DownloadStatus.FAILED) {
                    return ServiceResult.Error("Can only retry failed downloads")
                }
                _downloadProgress.value = _downloadProgress.value + (chapterId to current.copy(
                    status = DownloadStatus.QUEUED,
                    errorMessage = null,
                    retryCount = current.retryCount + 1
                ))
                queuedChapterIds.add(chapterId)
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to retry download: ${e.message}", e)
            }
        }

        override fun getDownloadStatus(chapterId: Long): DownloadStatus? {
            return _downloadProgress.value[chapterId]?.status
        }
    }

    @Test
    fun `initialize should transition state from IDLE to INITIALIZING to IDLE`() = runTest {
        val service = TestDownloadService()
        
        assertEquals(ServiceState.IDLE, service.state.value)
        
        service.initialize()
        
        assertEquals(ServiceState.IDLE, service.state.value)
    }

    @Test
    fun `start should set state to RUNNING after initialization`() = runTest {
        val service = TestDownloadService()
        
        service.initialize()
        service.start()
        
        assertEquals(ServiceState.RUNNING, service.state.value)
        assertTrue(service.isRunning())
    }

    @Test
    fun `stop should set state to STOPPED`() = runTest {
        val service = TestDownloadService()
        
        service.initialize()
        service.start()
        service.stop()
        
        assertEquals(ServiceState.STOPPED, service.state.value)
        assertFalse(service.isRunning())
    }

    @Test
    fun `queueChapters should add chapters to queue and set state to RUNNING`() = runTest {
        val service = TestDownloadService()
        val chapterIds = listOf(1L, 2L, 3L)
        
        service.initialize()
        val result = service.queueChapters(chapterIds)
        
        assertIs<ServiceResult.Success<Unit>>(result)
        assertEquals(ServiceState.RUNNING, service.state.value)
        assertEquals(chapterIds, service.getQueuedChapterIds())
    }

    @Test
    fun `queueChapters should initialize progress for each chapter`() = runTest {
        val service = TestDownloadService()
        val chapterIds = listOf(1L, 2L, 3L)
        
        service.initialize()
        service.queueChapters(chapterIds)
        
        val progress = service.downloadProgress.value
        assertEquals(3, progress.size)
        chapterIds.forEach { chapterId ->
            val chapterProgress = progress[chapterId]
            assertNotNull(chapterProgress)
            assertEquals(DownloadStatus.QUEUED, chapterProgress.status)
        }
    }

    @Test
    fun `queueChapters with empty list should return error`() = runTest {
        val service = TestDownloadService()
        
        service.initialize()
        val result = service.queueChapters(emptyList())
        
        assertIs<ServiceResult.Error>(result)
        assertEquals("No chapters to queue", result.message)
    }

    @Test
    fun `queueBooks should add books to queue and set state to RUNNING`() = runTest {
        val service = TestDownloadService()
        val bookIds = listOf(100L, 200L)
        
        service.initialize()
        val result = service.queueBooks(bookIds)
        
        assertIs<ServiceResult.Success<Unit>>(result)
        assertEquals(ServiceState.RUNNING, service.state.value)
        assertEquals(bookIds, service.getQueuedBookIds())
    }

    @Test
    fun `pause should set state to PAUSED when RUNNING`() = runTest {
        val service = TestDownloadService()
        
        service.initialize()
        service.queueChapters(listOf(1L))
        service.simulateDownloadProgress(1L, DownloadStatus.DOWNLOADING)
        
        service.pause()
        
        assertEquals(ServiceState.PAUSED, service.state.value)
        assertEquals(DownloadStatus.PAUSED, service.getDownloadStatus(1L))
    }

    @Test
    fun `resume should set state to RUNNING when PAUSED`() = runTest {
        val service = TestDownloadService()
        
        service.initialize()
        service.queueChapters(listOf(1L))
        service.simulateDownloadProgress(1L, DownloadStatus.DOWNLOADING)
        service.pause()
        
        service.resume()
        
        assertEquals(ServiceState.RUNNING, service.state.value)
        assertEquals(DownloadStatus.DOWNLOADING, service.getDownloadStatus(1L))
    }

    @Test
    fun `cancelDownload should mark specific download as cancelled`() = runTest {
        val service = TestDownloadService()
        
        service.initialize()
        service.queueChapters(listOf(1L, 2L, 3L))
        
        val result = service.cancelDownload(2L)
        
        assertIs<ServiceResult.Success<Unit>>(result)
        assertEquals(DownloadStatus.CANCELLED, service.getDownloadStatus(2L))
        assertEquals(DownloadStatus.QUEUED, service.getDownloadStatus(1L))
        assertEquals(DownloadStatus.QUEUED, service.getDownloadStatus(3L))
    }

    @Test
    fun `cancelAll should cancel all downloads and set state to IDLE`() = runTest {
        val service = TestDownloadService()
        
        service.initialize()
        service.queueChapters(listOf(1L, 2L, 3L))
        
        val result = service.cancelAll()
        
        assertIs<ServiceResult.Success<Unit>>(result)
        assertEquals(ServiceState.IDLE, service.state.value)
        assertEquals(DownloadStatus.CANCELLED, service.getDownloadStatus(1L))
        assertEquals(DownloadStatus.CANCELLED, service.getDownloadStatus(2L))
        assertEquals(DownloadStatus.CANCELLED, service.getDownloadStatus(3L))
    }

    @Test
    fun `retryDownload should re-queue failed download`() = runTest {
        val service = TestDownloadService()
        
        service.initialize()
        service.queueChapters(listOf(1L))
        service.simulateDownloadProgress(1L, DownloadStatus.FAILED)
        
        val result = service.retryDownload(1L)
        
        assertIs<ServiceResult.Success<Unit>>(result)
        assertEquals(DownloadStatus.QUEUED, service.getDownloadStatus(1L))
    }

    @Test
    fun `retryDownload should fail for non-failed downloads`() = runTest {
        val service = TestDownloadService()
        
        service.initialize()
        service.queueChapters(listOf(1L))
        
        val result = service.retryDownload(1L)
        
        assertIs<ServiceResult.Error>(result)
        assertEquals("Can only retry failed downloads", result.message)
    }

    @Test
    fun `retryDownload should increment retry count`() = runTest {
        val service = TestDownloadService()
        
        service.initialize()
        service.queueChapters(listOf(1L))
        service.simulateDownloadProgress(1L, DownloadStatus.FAILED)
        
        service.retryDownload(1L)
        
        val progress = service.downloadProgress.value[1L]
        assertNotNull(progress)
        assertEquals(1, progress.retryCount)
    }

    @Test
    fun `getDownloadStatus should return null for unknown chapter`() = runTest {
        val service = TestDownloadService()
        
        service.initialize()
        
        assertNull(service.getDownloadStatus(999L))
    }

    @Test
    fun `cleanup should reset all state`() = runTest {
        val service = TestDownloadService()
        
        service.initialize()
        service.queueChapters(listOf(1L, 2L, 3L))
        
        service.cleanup()
        
        assertEquals(ServiceState.STOPPED, service.state.value)
        assertTrue(service.downloads.value.isEmpty())
        assertTrue(service.downloadProgress.value.isEmpty())
    }

    @Test
    fun `download progress should track status transitions`() = runTest {
        val service = TestDownloadService()
        
        service.initialize()
        service.queueChapters(listOf(1L))
        
        // Initial state
        assertEquals(DownloadStatus.QUEUED, service.getDownloadStatus(1L))
        
        // Simulate downloading
        service.simulateDownloadProgress(1L, DownloadStatus.DOWNLOADING, 0.5f)
        assertEquals(DownloadStatus.DOWNLOADING, service.getDownloadStatus(1L))
        
        // Simulate completion
        service.simulateDownloadProgress(1L, DownloadStatus.COMPLETED, 1.0f)
        assertEquals(DownloadStatus.COMPLETED, service.getDownloadStatus(1L))
    }

    @Test
    fun `multiple chapters should be processed independently`() = runTest {
        val service = TestDownloadService()
        
        service.initialize()
        service.queueChapters(listOf(1L, 2L, 3L))
        
        // Simulate different states for each chapter
        service.simulateDownloadProgress(1L, DownloadStatus.COMPLETED, 1.0f)
        service.simulateDownloadProgress(2L, DownloadStatus.DOWNLOADING, 0.5f)
        service.simulateDownloadProgress(3L, DownloadStatus.FAILED)
        
        assertEquals(DownloadStatus.COMPLETED, service.getDownloadStatus(1L))
        assertEquals(DownloadStatus.DOWNLOADING, service.getDownloadStatus(2L))
        assertEquals(DownloadStatus.FAILED, service.getDownloadStatus(3L))
    }
}
