package org.ireader.app

import ireader.domain.services.downloaderService.DownloadProgress
import ireader.domain.services.downloaderService.DownloadStateHolder
import ireader.domain.services.downloaderService.DownloadStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DownloadStateHolder.
 * 
 * These tests verify the state management logic without requiring Android framework.
 */
class DownloadStateHolderTest {

    private lateinit var stateHolder: DownloadStateHolder

    @Before
    fun setup() {
        stateHolder = DownloadStateHolder()
    }

    @Test
    fun `initial state is idle`() = runBlocking {
        assertFalse(stateHolder.isRunning.value)
        assertFalse(stateHolder.isPaused.value)
        assertTrue(stateHolder.downloads.value.isEmpty())
        assertTrue(stateHolder.downloadProgress.value.isEmpty())
    }

    @Test
    fun `setRunning updates isRunning state`() = runBlocking {
        stateHolder.setRunning(true)
        assertTrue(stateHolder.isRunning.value)
        
        stateHolder.setRunning(false)
        assertFalse(stateHolder.isRunning.value)
    }

    @Test
    fun `setPaused updates isPaused state`() = runBlocking {
        stateHolder.setPaused(true)
        assertTrue(stateHolder.isPaused.value)
        
        stateHolder.setPaused(false)
        assertFalse(stateHolder.isPaused.value)
    }

    @Test
    fun `setDownloadProgress updates progress map`() = runBlocking {
        val progress = mapOf(
            1L to DownloadProgress(chapterId = 1L, status = DownloadStatus.QUEUED),
            2L to DownloadProgress(chapterId = 2L, status = DownloadStatus.DOWNLOADING, progress = 0.5f)
        )
        
        stateHolder.setDownloadProgress(progress)
        
        assertEquals(2, stateHolder.downloadProgress.value.size)
        assertEquals(DownloadStatus.QUEUED, stateHolder.downloadProgress.value[1L]?.status)
        assertEquals(DownloadStatus.DOWNLOADING, stateHolder.downloadProgress.value[2L]?.status)
        assertEquals(0.5f, stateHolder.downloadProgress.value[2L]?.progress)
    }

    @Test
    fun `reset clears all state`() = runBlocking {
        // Set some state
        stateHolder.setRunning(true)
        stateHolder.setPaused(true)
        stateHolder.setDownloadProgress(mapOf(
            1L to DownloadProgress(chapterId = 1L, status = DownloadStatus.DOWNLOADING)
        ))
        
        // Reset
        stateHolder.reset()
        
        // Verify all state is cleared
        assertFalse(stateHolder.isRunning.value)
        assertFalse(stateHolder.isPaused.value)
        assertTrue(stateHolder.downloads.value.isEmpty())
        assertTrue(stateHolder.downloadProgress.value.isEmpty())
    }

    @Test
    fun `download progress can be updated incrementally`() = runBlocking {
        // Add first chapter
        stateHolder.setDownloadProgress(mapOf(
            1L to DownloadProgress(chapterId = 1L, status = DownloadStatus.QUEUED)
        ))
        
        // Add second chapter while keeping first
        val currentProgress = stateHolder.downloadProgress.value
        stateHolder.setDownloadProgress(currentProgress + mapOf(
            2L to DownloadProgress(chapterId = 2L, status = DownloadStatus.QUEUED)
        ))
        
        assertEquals(2, stateHolder.downloadProgress.value.size)
        assertNotNull(stateHolder.downloadProgress.value[1L])
        assertNotNull(stateHolder.downloadProgress.value[2L])
    }

    @Test
    fun `download progress can be updated for single chapter`() = runBlocking {
        // Set initial progress
        stateHolder.setDownloadProgress(mapOf(
            1L to DownloadProgress(chapterId = 1L, status = DownloadStatus.QUEUED),
            2L to DownloadProgress(chapterId = 2L, status = DownloadStatus.QUEUED)
        ))
        
        // Update only chapter 1
        val currentProgress = stateHolder.downloadProgress.value
        stateHolder.setDownloadProgress(currentProgress + mapOf(
            1L to DownloadProgress(chapterId = 1L, status = DownloadStatus.DOWNLOADING, progress = 0.5f)
        ))
        
        // Chapter 1 should be updated
        assertEquals(DownloadStatus.DOWNLOADING, stateHolder.downloadProgress.value[1L]?.status)
        assertEquals(0.5f, stateHolder.downloadProgress.value[1L]?.progress)
        
        // Chapter 2 should remain unchanged
        assertEquals(DownloadStatus.QUEUED, stateHolder.downloadProgress.value[2L]?.status)
    }

    @Test
    fun `paused state with running true indicates paused downloads`() = runBlocking {
        stateHolder.setRunning(true)
        stateHolder.setPaused(true)
        
        // This combination means downloads are paused but not stopped
        assertTrue(stateHolder.isRunning.value)
        assertTrue(stateHolder.isPaused.value)
    }

    @Test
    fun `running false with paused false indicates idle state`() = runBlocking {
        stateHolder.setRunning(false)
        stateHolder.setPaused(false)
        
        // This combination means no downloads are active
        assertFalse(stateHolder.isRunning.value)
        assertFalse(stateHolder.isPaused.value)
    }

    @Test
    fun `download progress status transitions are valid`() = runBlocking {
        val chapterId = 1L
        
        // QUEUED -> DOWNLOADING
        stateHolder.setDownloadProgress(mapOf(
            chapterId to DownloadProgress(chapterId = chapterId, status = DownloadStatus.QUEUED)
        ))
        assertEquals(DownloadStatus.QUEUED, stateHolder.downloadProgress.value[chapterId]?.status)
        
        stateHolder.setDownloadProgress(mapOf(
            chapterId to DownloadProgress(chapterId = chapterId, status = DownloadStatus.DOWNLOADING, progress = 0.5f)
        ))
        assertEquals(DownloadStatus.DOWNLOADING, stateHolder.downloadProgress.value[chapterId]?.status)
        
        // DOWNLOADING -> COMPLETED
        stateHolder.setDownloadProgress(mapOf(
            chapterId to DownloadProgress(chapterId = chapterId, status = DownloadStatus.COMPLETED, progress = 1f)
        ))
        assertEquals(DownloadStatus.COMPLETED, stateHolder.downloadProgress.value[chapterId]?.status)
    }

    @Test
    fun `download progress can track retry count`() = runBlocking {
        val chapterId = 1L
        
        // First attempt fails
        stateHolder.setDownloadProgress(mapOf(
            chapterId to DownloadProgress(
                chapterId = chapterId, 
                status = DownloadStatus.FAILED,
                errorMessage = "Network error",
                retryCount = 1
            )
        ))
        
        assertEquals(DownloadStatus.FAILED, stateHolder.downloadProgress.value[chapterId]?.status)
        assertEquals(1, stateHolder.downloadProgress.value[chapterId]?.retryCount)
        assertEquals("Network error", stateHolder.downloadProgress.value[chapterId]?.errorMessage)
        
        // Retry - back to queued with incremented retry count
        stateHolder.setDownloadProgress(mapOf(
            chapterId to DownloadProgress(
                chapterId = chapterId, 
                status = DownloadStatus.QUEUED,
                retryCount = 2
            )
        ))
        
        assertEquals(DownloadStatus.QUEUED, stateHolder.downloadProgress.value[chapterId]?.status)
        assertEquals(2, stateHolder.downloadProgress.value[chapterId]?.retryCount)
        assertNull(stateHolder.downloadProgress.value[chapterId]?.errorMessage)
    }
}
