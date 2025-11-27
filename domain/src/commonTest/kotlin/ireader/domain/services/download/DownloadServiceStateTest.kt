package ireader.domain.services.download

import ireader.domain.services.downloaderService.DownloadProgress
import ireader.domain.services.downloaderService.DownloadStateHolder
import ireader.domain.services.downloaderService.DownloadStatus
import ireader.domain.services.downloaderService.DownloadServiceConstants
import ireader.domain.models.entities.SavedDownload
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for DownloadStateHolder to ensure proper state management
 * This tests the shared state implementation that both Android and Desktop
 * download services integrate with.
 */
class DownloadServiceStateTest {

    @Test
    fun `initial state should be empty and not running`() = runTest {
        val state = DownloadStateHolder()
        
        assertFalse(state.isRunning.value)
        assertFalse(state.isPaused.value)
        assertTrue(state.downloads.value.isEmpty())
        assertTrue(state.downloadProgress.value.isEmpty())
    }

    @Test
    fun `setRunning should update isRunning state`() = runTest {
        val state = DownloadStateHolder()
        
        state.setRunning(true)
        assertTrue(state.isRunning.value)
        
        state.setRunning(false)
        assertFalse(state.isRunning.value)
    }

    @Test
    fun `setPaused should update isPaused state`() = runTest {
        val state = DownloadStateHolder()
        
        state.setPaused(true)
        assertTrue(state.isPaused.value)
        
        state.setPaused(false)
        assertFalse(state.isPaused.value)
    }

    @Test
    fun `setDownloads should update downloads list`() = runTest {
        val state = DownloadStateHolder()
        val downloads = listOf(
            SavedDownload(
                bookId = 1L,
                priority = 1,
                chapterName = "Chapter 1",
                chapterKey = "ch1",
                translator = "",
                chapterId = 100L,
                bookName = "Test Book"
            ),
            SavedDownload(
                bookId = 1L,
                priority = 2,
                chapterName = "Chapter 2",
                chapterKey = "ch2",
                translator = "",
                chapterId = 101L,
                bookName = "Test Book"
            )
        )
        
        state.setDownloads(downloads)
        
        assertEquals(2, state.downloads.value.size)
        assertEquals("Chapter 1", state.downloads.value[0].chapterName)
        assertEquals("Chapter 2", state.downloads.value[1].chapterName)
    }

    @Test
    fun `setDownloadProgress should update progress map`() = runTest {
        val state = DownloadStateHolder()
        val progressMap = mapOf(
            100L to DownloadProgress(
                chapterId = 100L,
                status = DownloadStatus.DOWNLOADING,
                progress = 0.5f
            ),
            101L to DownloadProgress(
                chapterId = 101L,
                status = DownloadStatus.QUEUED
            )
        )
        
        state.setDownloadProgress(progressMap)
        
        assertEquals(2, state.downloadProgress.value.size)
        assertEquals(DownloadStatus.DOWNLOADING, state.downloadProgress.value[100L]?.status)
        assertEquals(0.5f, state.downloadProgress.value[100L]?.progress)
        assertEquals(DownloadStatus.QUEUED, state.downloadProgress.value[101L]?.status)
    }

    @Test
    fun `download progress should track status transitions`() = runTest {
        val state = DownloadStateHolder()
        
        // Initial queued state
        state.setDownloadProgress(mapOf(
            100L to DownloadProgress(chapterId = 100L, status = DownloadStatus.QUEUED)
        ))
        assertEquals(DownloadStatus.QUEUED, state.downloadProgress.value[100L]?.status)
        
        // Transition to downloading
        state.setDownloadProgress(mapOf(
            100L to DownloadProgress(chapterId = 100L, status = DownloadStatus.DOWNLOADING, progress = 0.25f)
        ))
        assertEquals(DownloadStatus.DOWNLOADING, state.downloadProgress.value[100L]?.status)
        assertEquals(0.25f, state.downloadProgress.value[100L]?.progress)
        
        // Transition to completed
        state.setDownloadProgress(mapOf(
            100L to DownloadProgress(chapterId = 100L, status = DownloadStatus.COMPLETED, progress = 1.0f)
        ))
        assertEquals(DownloadStatus.COMPLETED, state.downloadProgress.value[100L]?.status)
        assertEquals(1.0f, state.downloadProgress.value[100L]?.progress)
    }

    @Test
    fun `download progress should handle failures with error messages`() = runTest {
        val state = DownloadStateHolder()
        
        state.setDownloadProgress(mapOf(
            100L to DownloadProgress(
                chapterId = 100L,
                status = DownloadStatus.FAILED,
                errorMessage = "Network error",
                retryCount = 2
            )
        ))
        
        val progress = state.downloadProgress.value[100L]
        assertEquals(DownloadStatus.FAILED, progress?.status)
        assertEquals("Network error", progress?.errorMessage)
        assertEquals(2, progress?.retryCount)
    }

    @Test
    fun `pause state should be independent of running state`() = runTest {
        val state = DownloadStateHolder()
        
        // Can be running and paused at the same time
        state.setRunning(true)
        state.setPaused(true)
        
        assertTrue(state.isRunning.value)
        assertTrue(state.isPaused.value)
        
        // Resume (unpause) while still running
        state.setPaused(false)
        
        assertTrue(state.isRunning.value)
        assertFalse(state.isPaused.value)
    }

    @Test
    fun `clearing progress should not affect running state`() = runTest {
        val state = DownloadStateHolder()
        
        state.setRunning(true)
        state.setDownloadProgress(mapOf(
            100L to DownloadProgress(chapterId = 100L, status = DownloadStatus.DOWNLOADING)
        ))
        
        // Clear progress
        state.setDownloadProgress(emptyMap())
        
        // Running state should be unchanged
        assertTrue(state.isRunning.value)
        assertTrue(state.downloadProgress.value.isEmpty())
    }

    @Test
    fun `multiple downloads should be tracked independently`() = runTest {
        val state = DownloadStateHolder()
        
        val progressMap = mapOf(
            100L to DownloadProgress(chapterId = 100L, status = DownloadStatus.COMPLETED, progress = 1.0f),
            101L to DownloadProgress(chapterId = 101L, status = DownloadStatus.DOWNLOADING, progress = 0.5f),
            102L to DownloadProgress(chapterId = 102L, status = DownloadStatus.FAILED, errorMessage = "Error"),
            103L to DownloadProgress(chapterId = 103L, status = DownloadStatus.QUEUED),
            104L to DownloadProgress(chapterId = 104L, status = DownloadStatus.PAUSED)
        )
        
        state.setDownloadProgress(progressMap)
        
        assertEquals(5, state.downloadProgress.value.size)
        assertEquals(DownloadStatus.COMPLETED, state.downloadProgress.value[100L]?.status)
        assertEquals(DownloadStatus.DOWNLOADING, state.downloadProgress.value[101L]?.status)
        assertEquals(DownloadStatus.FAILED, state.downloadProgress.value[102L]?.status)
        assertEquals(DownloadStatus.QUEUED, state.downloadProgress.value[103L]?.status)
        assertEquals(DownloadStatus.PAUSED, state.downloadProgress.value[104L]?.status)
    }

    @Test
    fun `constants should be correct`() {
        assertEquals("DOWNLOAD_SERVICE", DownloadServiceConstants.DOWNLOADER_SERVICE_NAME)
        assertEquals("chapterIds", DownloadServiceConstants.DOWNLOADER_CHAPTERS_IDS)
        assertEquals("downloader_mode", DownloadServiceConstants.DOWNLOADER_MODE)
        assertEquals("booksIds", DownloadServiceConstants.DOWNLOADER_BOOKS_IDS)
    }
    
    @Test
    fun `reset should clear all state`() = runTest {
        val state = DownloadStateHolder()
        
        // Set some state
        state.setRunning(true)
        state.setPaused(true)
        state.setDownloads(listOf(
            SavedDownload(
                bookId = 1L,
                priority = 1,
                chapterName = "Chapter 1",
                chapterKey = "ch1",
                translator = "",
                chapterId = 100L,
                bookName = "Test Book"
            )
        ))
        state.setDownloadProgress(mapOf(
            100L to DownloadProgress(chapterId = 100L, status = DownloadStatus.DOWNLOADING)
        ))
        
        // Reset
        state.reset()
        
        // Verify all state is cleared
        assertFalse(state.isRunning.value)
        assertFalse(state.isPaused.value)
        assertTrue(state.downloads.value.isEmpty())
        assertTrue(state.downloadProgress.value.isEmpty())
    }
}
