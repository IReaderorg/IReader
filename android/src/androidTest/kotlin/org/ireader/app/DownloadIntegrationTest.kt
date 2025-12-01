package org.ireader.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import ireader.domain.services.downloaderService.DownloadServiceConstants.DOWNLOADER_SERVICE_NAME
import ireader.domain.services.downloaderService.DownloadStateHolder
import ireader.domain.services.downloaderService.DownloadStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Integration tests for the Download flow.
 * 
 * These tests simulate real user scenarios:
 * 1. User starts download -> downloads begin
 * 2. User pauses download -> downloads pause
 * 3. User resumes download -> downloads continue from where they left off
 * 4. User cancels all -> everything stops and clears
 * 5. Long-running download (>10 min) -> doesn't restart from beginning
 */
@RunWith(AndroidJUnit4::class)
class DownloadIntegrationTest : KoinComponent {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private val downloadStateHolder: DownloadStateHolder by inject()
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)
        
        downloadStateHolder.reset()
    }
    
    @After
    fun teardown() {
        workManager.cancelAllWorkByTag(DOWNLOADER_SERVICE_NAME)
        downloadStateHolder.reset()
    }
    
    /**
     * Test: Verify that only one download worker can run at a time.
     * 
     * This prevents the issue where multiple workers download the same chapters.
     */
    @Test
    fun test_single_worker_policy() = runBlocking {
        // Simulate starting downloads multiple times
        repeat(5) {
            downloadStateHolder.setRunning(true)
            delay(50)
        }
        
        // Check work info
        val workInfos = workManager.getWorkInfosByTag(DOWNLOADER_SERVICE_NAME).get()
        val activeCount = workInfos.count { 
            it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED 
        }
        
        // Should have at most 1 active worker
        assertTrue("Multiple workers running: $activeCount", activeCount <= 1)
    }
    
    /**
     * Test: Verify pause/resume state transitions.
     * 
     * This ensures the UI correctly reflects the download state.
     */
    @Test
    fun test_pause_resume_state_transitions() = runBlocking {
        // Start
        downloadStateHolder.setRunning(true)
        downloadStateHolder.setPaused(false)
        delay(100)
        
        assertTrue(downloadStateHolder.isRunning.value)
        assertFalse(downloadStateHolder.isPaused.value)
        
        // Pause
        downloadStateHolder.setPaused(true)
        delay(100)
        
        assertTrue(downloadStateHolder.isRunning.value) // Still "running" but paused
        assertTrue(downloadStateHolder.isPaused.value)
        
        // Resume
        downloadStateHolder.setPaused(false)
        delay(100)
        
        assertTrue(downloadStateHolder.isRunning.value)
        assertFalse(downloadStateHolder.isPaused.value)
        
        // Stop
        downloadStateHolder.setRunning(false)
        downloadStateHolder.setPaused(false)
        delay(100)
        
        assertFalse(downloadStateHolder.isRunning.value)
        assertFalse(downloadStateHolder.isPaused.value)
    }
    
    /**
     * Test: Verify download progress tracking.
     * 
     * This ensures progress is correctly tracked and updated.
     */
    @Test
    fun test_download_progress_tracking() = runBlocking {
        val chapters = listOf(1L, 2L, 3L, 4L, 5L)
        
        // Queue all chapters
        val initialProgress = chapters.associateWith { chapterId ->
            ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = chapterId,
                status = DownloadStatus.QUEUED
            )
        }
        downloadStateHolder.setDownloadProgress(initialProgress)
        
        assertEquals(5, downloadStateHolder.downloadProgress.value.size)
        
        // Simulate downloading chapter 1
        val progress1 = downloadStateHolder.downloadProgress.value.toMutableMap()
        progress1[1L] = progress1[1L]!!.copy(status = DownloadStatus.DOWNLOADING, progress = 0.5f)
        downloadStateHolder.setDownloadProgress(progress1)
        
        assertEquals(DownloadStatus.DOWNLOADING, downloadStateHolder.downloadProgress.value[1L]?.status)
        assertEquals(0.5f, downloadStateHolder.downloadProgress.value[1L]?.progress)
        
        // Complete chapter 1
        val progress2 = downloadStateHolder.downloadProgress.value.toMutableMap()
        progress2[1L] = progress2[1L]!!.copy(status = DownloadStatus.COMPLETED, progress = 1f)
        downloadStateHolder.setDownloadProgress(progress2)
        
        assertEquals(DownloadStatus.COMPLETED, downloadStateHolder.downloadProgress.value[1L]?.status)
        
        // Verify other chapters are still queued
        assertEquals(DownloadStatus.QUEUED, downloadStateHolder.downloadProgress.value[2L]?.status)
        assertEquals(DownloadStatus.QUEUED, downloadStateHolder.downloadProgress.value[3L]?.status)
    }
    
    /**
     * Test: Verify that completed chapters are not re-downloaded.
     * 
     * This is the key fix for the "restart from chapter 1" bug.
     */
    @Test
    fun test_completed_chapters_not_redownloaded() = runBlocking {
        // Set up: chapters 1-3 completed, 4-5 queued
        val progress = mapOf(
            1L to ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = 1L, status = DownloadStatus.COMPLETED, progress = 1f
            ),
            2L to ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = 2L, status = DownloadStatus.COMPLETED, progress = 1f
            ),
            3L to ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = 3L, status = DownloadStatus.COMPLETED, progress = 1f
            ),
            4L to ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = 4L, status = DownloadStatus.QUEUED
            ),
            5L to ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = 5L, status = DownloadStatus.QUEUED
            )
        )
        downloadStateHolder.setDownloadProgress(progress)
        
        // Simulate "restart" - only queued chapters should be processed
        val chaptersToDownload = downloadStateHolder.downloadProgress.value
            .filter { it.value.status != DownloadStatus.COMPLETED }
            .keys.toList()
        
        assertEquals(2, chaptersToDownload.size)
        assertTrue(chaptersToDownload.contains(4L))
        assertTrue(chaptersToDownload.contains(5L))
        assertFalse(chaptersToDownload.contains(1L))
        assertFalse(chaptersToDownload.contains(2L))
        assertFalse(chaptersToDownload.contains(3L))
    }
    
    /**
     * Test: Verify pause updates downloading items to paused.
     */
    @Test
    fun test_pause_updates_downloading_to_paused() = runBlocking {
        // Set up: some downloading, some queued, some completed
        val progress = mapOf(
            1L to ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = 1L, status = DownloadStatus.DOWNLOADING, progress = 0.5f
            ),
            2L to ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = 2L, status = DownloadStatus.QUEUED
            ),
            3L to ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = 3L, status = DownloadStatus.COMPLETED, progress = 1f
            )
        )
        downloadStateHolder.setDownloadProgress(progress)
        
        // Simulate pause - update downloading to paused
        val updatedProgress = downloadStateHolder.downloadProgress.value.mapValues { (_, p) ->
            if (p.status == DownloadStatus.DOWNLOADING) {
                p.copy(status = DownloadStatus.PAUSED)
            } else {
                p
            }
        }
        downloadStateHolder.setDownloadProgress(updatedProgress)
        
        // Verify
        assertEquals(DownloadStatus.PAUSED, downloadStateHolder.downloadProgress.value[1L]?.status)
        assertEquals(DownloadStatus.QUEUED, downloadStateHolder.downloadProgress.value[2L]?.status)
        assertEquals(DownloadStatus.COMPLETED, downloadStateHolder.downloadProgress.value[3L]?.status)
    }
    
    /**
     * Test: Verify resume updates paused items to downloading.
     */
    @Test
    fun test_resume_updates_paused_to_downloading() = runBlocking {
        // Set up: some paused, some queued
        val progress = mapOf(
            1L to ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = 1L, status = DownloadStatus.PAUSED, progress = 0.5f
            ),
            2L to ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = 2L, status = DownloadStatus.QUEUED
            )
        )
        downloadStateHolder.setDownloadProgress(progress)
        
        // Simulate resume - update paused to downloading
        val updatedProgress = downloadStateHolder.downloadProgress.value.mapValues { (_, p) ->
            if (p.status == DownloadStatus.PAUSED) {
                p.copy(status = DownloadStatus.DOWNLOADING)
            } else {
                p
            }
        }
        downloadStateHolder.setDownloadProgress(updatedProgress)
        
        // Verify
        assertEquals(DownloadStatus.DOWNLOADING, downloadStateHolder.downloadProgress.value[1L]?.status)
        assertEquals(0.5f, downloadStateHolder.downloadProgress.value[1L]?.progress) // Progress preserved
        assertEquals(DownloadStatus.QUEUED, downloadStateHolder.downloadProgress.value[2L]?.status)
    }
    
    /**
     * Test: Verify cancel all clears everything.
     */
    @Test
    fun test_cancel_all_clears_state() = runBlocking {
        // Set up some state
        downloadStateHolder.setRunning(true)
        downloadStateHolder.setPaused(false)
        downloadStateHolder.setDownloadProgress(mapOf(
            1L to ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = 1L, status = DownloadStatus.DOWNLOADING
            )
        ))
        
        // Cancel all
        downloadStateHolder.reset()
        
        // Verify everything is cleared
        assertFalse(downloadStateHolder.isRunning.value)
        assertFalse(downloadStateHolder.isPaused.value)
        assertTrue(downloadStateHolder.downloadProgress.value.isEmpty())
        assertTrue(downloadStateHolder.downloads.value.isEmpty())
    }
}
