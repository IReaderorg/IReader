package ireader.domain.services.download

import ireader.core.log.Log
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.download.Download
import ireader.domain.models.download.DownloadState
import ireader.domain.models.download.DownloadStatus
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.SavedDownload
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Central manager for all download operations.
 * Coordinates Downloader, DownloadStore, and DownloadCache.
 * Replaces DownloadStateHolder with enhanced functionality.
 */
class DownloadManager(
    private val downloader: Downloader,
    private val downloadStore: DownloadStore,
    private val downloadCache: DownloadCache,
    private val downloadUseCases: DownloadUseCases,
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository
) {
    
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    
    // Queue of downloads with their state
    private val _queue = MutableStateFlow<List<DownloadState>>(emptyList())
    val queue: StateFlow<List<DownloadState>> = _queue.asStateFlow()
    
    // Running state
    val isRunning: StateFlow<Boolean> = downloader.isRunning
    val isPaused: StateFlow<Boolean> = downloader.isPaused
    val isPausedDueToNetwork: StateFlow<Boolean> = downloader.isPausedDueToNetwork
    val isPausedDueToDiskSpace: StateFlow<Boolean> = downloader.isPausedDueToDiskSpace
    val currentDownload: StateFlow<Download?> = downloader.currentDownload
    
    // Statistics
    private val _completedCount = MutableStateFlow(0)
    val completedCount: StateFlow<Int> = _completedCount.asStateFlow()
    
    private val _failedCount = MutableStateFlow(0)
    val failedCount: StateFlow<Int> = _failedCount.asStateFlow()
    
    private var initialized = false
    
    /**
     * Initialize the manager and restore queue from persistence.
     * Should be called on app startup.
     */
    suspend fun init() {
        if (initialized) return
        
        try {
            // Restore queue from persistence
            val savedItems = downloadStore.restoreQueue()
            if (savedItems.isNotEmpty()) {
                val downloads = savedItems.mapNotNull { item ->
                    createDownloadFromQueueItem(item)
                }
                _queue.value = downloads.map { DownloadState(it) }
                Log.debug { "DownloadManager: Restored ${downloads.size} downloads from queue" }
            }
            
            // Initialize cache
            if (!downloadCache.isInitialized()) {
                downloadCache.refresh()
            }
            
            initialized = true
            Log.debug { "DownloadManager: Initialized" }
        } catch (e: Exception) {
            Log.error { "DownloadManager: Failed to initialize - ${e.message}" }
        }
    }
    
    /**
     * Add chapters to the download queue.
     */
    suspend fun addToQueue(chapters: List<Chapter>, books: Map<Long, ireader.domain.models.entities.Book>) {
        val newDownloads = chapters.mapNotNull { chapter ->
            val book = books[chapter.bookId] ?: return@mapNotNull null
            
            // Skip if already downloaded
            if (downloadCache.isChapterDownloaded(book.id, chapter.id)) {
                Log.debug { "DownloadManager: Skipping already downloaded chapter ${chapter.name}" }
                return@mapNotNull null
            }
            
            // Skip if already in queue
            if (_queue.value.any { it.download.chapterId == chapter.id }) {
                Log.debug { "DownloadManager: Skipping chapter already in queue ${chapter.name}" }
                return@mapNotNull null
            }
            
            Download(
                chapterId = chapter.id,
                bookId = book.id,
                sourceId = book.sourceId,
                chapterName = chapter.name,
                bookTitle = book.title,
                coverUrl = book.cover,
                status = DownloadStatus.QUEUE
            )
        }
        
        if (newDownloads.isEmpty()) return
        
        val newStates = newDownloads.map { DownloadState(it) }
        _queue.value = _queue.value + newStates
        
        // Persist queue
        persistQueue()
        
        // Also save to database for UI - we need to get chapter info from the original chapters list
        val chapterMap = chapters.associateBy { it.id }
        val savedDownloads = newDownloads.mapNotNull { download ->
            val chapter = chapterMap[download.chapterId] ?: return@mapNotNull null
            SavedDownload(
                bookId = download.bookId,
                chapterId = download.chapterId,
                priority = 1,
                chapterName = download.chapterName,
                chapterKey = chapter.key,
                translator = chapter.translator,
                bookName = download.bookTitle
            )
        }
        downloadUseCases.insertDownloads(savedDownloads.map { it.toDownload() })
        
        Log.debug { "DownloadManager: Added ${newDownloads.size} chapters to queue" }
    }
    
    /**
     * Add a single chapter to the download queue.
     */
    suspend fun addToQueue(chapter: Chapter, book: ireader.domain.models.entities.Book) {
        addToQueue(listOf(chapter), mapOf(book.id to book))
    }
    
    /**
     * Remove a download from the queue.
     */
    suspend fun removeFromQueue(chapterId: Long) {
        _queue.value = _queue.value.filter { it.download.chapterId != chapterId }
        persistQueue()
        
        // Remove from database - find the download first
        val download = _queue.value.find { it.download.chapterId == chapterId }?.download
        if (download != null) {
            downloadUseCases.deleteSavedDownload(
                ireader.domain.models.entities.Download(
                    chapterId = download.chapterId,
                    bookId = download.bookId,
                    priority = 0
                )
            )
        }
        
        Log.debug { "DownloadManager: Removed chapter $chapterId from queue" }
    }
    
    /**
     * Clear the entire queue.
     */
    suspend fun clearQueue() {
        downloader.stop()
        _queue.value = emptyList()
        _completedCount.value = 0
        _failedCount.value = 0
        downloadStore.clear()
        
        // Clear from database
        downloadUseCases.deleteAllSavedDownload()
        
        Log.debug { "DownloadManager: Cleared queue" }
    }
    
    /**
     * Start downloading the queue.
     */
    fun startDownloads() {
        if (_queue.value.isEmpty()) {
            Log.debug { "DownloadManager: Queue is empty, nothing to download" }
            return
        }
        
        // Filter to only pending downloads
        val pendingDownloads = _queue.value.filter { 
            it.download.status == DownloadStatus.QUEUE || 
            it.download.status == DownloadStatus.ERROR 
        }
        
        if (pendingDownloads.isEmpty()) {
            Log.debug { "DownloadManager: No pending downloads" }
            return
        }
        
        downloader.start(
            queue = pendingDownloads,
            onProgress = { download ->
                updateDownloadInQueue(download)
                scope.launch { persistQueue() }
            },
            onComplete = {
                Log.debug { "DownloadManager: All downloads completed" }
                scope.launch { cleanupCompletedDownloads() }
            },
            onError = { download, error ->
                Log.error { "DownloadManager: Download failed for ${download.chapterName}: $error" }
                _failedCount.value++
            }
        )
    }
    
    /**
     * Pause all downloads.
     */
    fun pauseDownloads() {
        downloader.pause()
    }
    
    /**
     * Resume paused downloads.
     */
    fun resumeDownloads() {
        downloader.resume()
    }
    
    /**
     * Cancel all downloads and stop the downloader.
     */
    fun cancelDownloads() {
        downloader.stop()
    }
    
    /**
     * Retry a failed download.
     */
    suspend fun retryDownload(chapterId: Long) {
        val downloadState = _queue.value.find { it.download.chapterId == chapterId }
        if (downloadState != null && downloadState.download.status == DownloadStatus.ERROR) {
            downloadState.update { download ->
                download.copy(
                    status = DownloadStatus.QUEUE,
                    errorMessage = null,
                    retryCount = 0
                )
            }
            updateDownloadInQueue(downloadState.download)
            persistQueue()
            
            // Restart downloads if not running
            if (!isRunning.value) {
                startDownloads()
            }
        }
    }
    
    /**
     * Retry all failed downloads.
     */
    suspend fun retryAllFailed() {
        _queue.value.filter { it.download.status == DownloadStatus.ERROR }.forEach { downloadState ->
            downloadState.update { download ->
                download.copy(
                    status = DownloadStatus.QUEUE,
                    errorMessage = null,
                    retryCount = 0
                )
            }
        }
        _failedCount.value = 0
        persistQueue()
        
        // Restart downloads if not running
        if (!isRunning.value) {
            startDownloads()
        }
    }
    
    /**
     * Clear completed downloads from the queue.
     */
    suspend fun clearCompleted() {
        _queue.value = _queue.value.filter { it.download.status != DownloadStatus.DOWNLOADED }
        _completedCount.value = 0
        persistQueue()
    }
    
    /**
     * Clear failed downloads from the queue.
     */
    suspend fun clearFailed() {
        val failedDownloads = _queue.value
            .filter { it.download.status == DownloadStatus.ERROR }
        
        _queue.value = _queue.value.filter { it.download.status != DownloadStatus.ERROR }
        _failedCount.value = 0
        persistQueue()
        
        // Remove from database
        failedDownloads.forEach { downloadState ->
            downloadUseCases.deleteSavedDownload(
                ireader.domain.models.entities.Download(
                    chapterId = downloadState.download.chapterId,
                    bookId = downloadState.download.bookId,
                    priority = 0
                )
            )
        }
    }
    
    /**
     * Check if a chapter is downloaded.
     */
    fun isChapterDownloaded(bookId: Long, chapterId: Long): Boolean {
        return downloadCache.isChapterDownloaded(bookId, chapterId)
    }
    
    /**
     * Get all downloaded chapter IDs for a book.
     */
    fun getDownloadedChapterIds(bookId: Long): Set<Long> {
        return downloadCache.getDownloadedChapterIds(bookId)
    }
    
    /**
     * Reorder downloads in the queue.
     */
    fun reorderDownloads(fromIndex: Int, toIndex: Int) {
        val mutableQueue = _queue.value.toMutableList()
        if (fromIndex in mutableQueue.indices && toIndex in mutableQueue.indices) {
            val item = mutableQueue.removeAt(fromIndex)
            mutableQueue.add(toIndex, item)
            _queue.value = mutableQueue
            scope.launch { persistQueue() }
        }
    }
    
    private fun updateDownloadInQueue(download: Download) {
        val index = _queue.value.indexOfFirst { it.download.chapterId == download.chapterId }
        if (index >= 0) {
            val mutableQueue = _queue.value.toMutableList()
            mutableQueue[index] = DownloadState(download)
            _queue.value = mutableQueue
            
            if (download.status == DownloadStatus.DOWNLOADED) {
                _completedCount.value++
            }
        }
    }
    
    private suspend fun persistQueue() {
        val items = _queue.value.map { state ->
            DownloadQueueItem.fromDownload(state.download)
        }
        downloadStore.saveQueue(items)
    }
    
    private suspend fun cleanupCompletedDownloads() {
        // Remove completed downloads from database
        val completedDownloads = _queue.value
            .filter { it.download.status == DownloadStatus.DOWNLOADED }
        
        completedDownloads.forEach { downloadState ->
            downloadUseCases.deleteSavedDownload(
                ireader.domain.models.entities.Download(
                    chapterId = downloadState.download.chapterId,
                    bookId = downloadState.download.bookId,
                    priority = 0
                )
            )
        }
    }
    
    private suspend fun createDownloadFromQueueItem(item: DownloadQueueItem): Download? {
        return try {
            item.toDownload()
        } catch (e: Exception) {
            Log.error { "DownloadManager: Failed to create download from queue item - ${e.message}" }
            null
        }
    }
}
