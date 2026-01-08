package ireader.domain.services.common

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.SavedDownload
import ireader.domain.models.entities.buildSavedDownload
import ireader.domain.services.downloaderService.DownloadServiceConstants
import ireader.domain.services.downloaderService.DownloadServiceConstants.DOWNLOADER_SERVICE_NAME
import ireader.domain.services.downloaderService.DownloadStateHolder
import ireader.domain.services.downloaderService.DownloaderService
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.services.downloaderService.DownloadStatus as LegacyDownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of DownloadService using WorkManager.
 * 
 * This service uses the legacy DownloadStateHolder system which is proven to work correctly.
 * The DownloaderService (WorkManager worker) uses runDownloadService() which manages
 * downloads sequentially with proper pause/resume support.
 * 
 * Key features:
 * - Sequential downloads (one chapter at a time)
 * - Configurable delay between downloads
 * - Pause/resume support via DownloadStateHolder
 * - WorkManager for background execution
 * - Proper notification management
 */
class AndroidDownloadService(
    private val context: Context
) : DownloadService, KoinComponent {
    
    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    // Legacy state holder - the working system
    private val downloadServiceState: DownloadStateHolder by inject()
    private val bookRepository: BookRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val downloadUseCases: DownloadUseCases by inject()
    
    private val _state = MutableStateFlow<ServiceState>(ServiceState.IDLE)
    override val state: StateFlow<ServiceState> = _state.asStateFlow()
    
    // Bridge downloads from legacy state
    override val downloads: StateFlow<List<SavedDownload>> = downloadServiceState.downloads
    
    private val _downloadProgress = MutableStateFlow<Map<Long, DownloadProgress>>(emptyMap())
    override val downloadProgress: StateFlow<Map<Long, DownloadProgress>> = _downloadProgress.asStateFlow()
    
    init {
        // Observe legacy state and update service state
        scope.launch {
            combine(
                downloadServiceState.isRunning,
                downloadServiceState.isPaused
            ) { isRunning, isPaused ->
                when {
                    isPaused -> ServiceState.PAUSED
                    isRunning -> ServiceState.RUNNING
                    else -> ServiceState.IDLE
                }
            }.collect { newState ->
                _state.value = newState
            }
        }
        
        // Bridge legacy progress to our progress
        scope.launch {
            downloadServiceState.downloadProgress.collect { legacyProgress ->
                val newProgress = legacyProgress.mapValues { (chapterId, legacy) ->
                    // Find the download info for this chapter
                    val download = downloadServiceState.downloads.value.find { it.chapterId == chapterId }
                    DownloadProgress(
                        chapterId = chapterId,
                        chapterName = download?.chapterName ?: "",
                        bookName = download?.bookName ?: "",
                        status = mapLegacyStatusToServiceStatus(legacy.status),
                        progress = legacy.progress,
                        errorMessage = legacy.errorMessage,
                        retryCount = legacy.retryCount,
                        totalRetries = 3
                    )
                }
                _downloadProgress.value = newProgress
            }
        }
    }
    
    private fun mapLegacyStatusToServiceStatus(status: LegacyDownloadStatus): DownloadStatus {
        return when (status) {
            LegacyDownloadStatus.QUEUED -> DownloadStatus.QUEUED
            LegacyDownloadStatus.DOWNLOADING -> DownloadStatus.DOWNLOADING
            LegacyDownloadStatus.PAUSED -> DownloadStatus.PAUSED
            LegacyDownloadStatus.COMPLETED -> DownloadStatus.COMPLETED
            LegacyDownloadStatus.FAILED -> DownloadStatus.FAILED
        }
    }
    
    override suspend fun initialize() {
        _state.value = ServiceState.IDLE
    }
    
    override suspend fun start() {
        _state.value = ServiceState.RUNNING
        // Start is handled by queueChapters/queueBooks which call startWorkManager
    }
    
    override suspend fun stop() {
        _state.value = ServiceState.STOPPED
        workManager.cancelAllWorkByTag(DOWNLOADER_SERVICE_NAME)
        downloadServiceState.setRunning(false)
        downloadServiceState.setPaused(false)
    }
    
    override fun isRunning(): Boolean {
        return _state.value == ServiceState.RUNNING ||
               downloadServiceState.isRunning.value
    }
    
    override suspend fun cleanup() {
        downloadServiceState.setDownloadProgress(emptyMap())
        downloadServiceState.setDownloads(emptyList())
        _downloadProgress.value = emptyMap()
    }

    override suspend fun queueChapters(chapterIds: List<Long>): ServiceResult<Unit> {
        if (chapterIds.isEmpty()) return ServiceResult.Error("Empty queue")
        
        return try {
            val chaptersToDownload = withContext(Dispatchers.IO) {
                chapterIds.mapNotNull { chapterId ->
                    val chapter = chapterRepository.findChapterById(chapterId) ?: return@mapNotNull null
                    val contentText = chapter.content.joinToString("")
                    
                    // Skip already downloaded chapters
                    if (contentText.isNotEmpty() && contentText.length >= 50) {
                        return@mapNotNull null
                    }
                    val book = bookRepository.findBookById(chapter.bookId) ?: return@mapNotNull null
                    chapter to book
                }
            }
            
            if (chaptersToDownload.isEmpty()) {
                // All chapters already downloaded - clean up the download queue
                withContext(Dispatchers.IO) {
                    chapterIds.forEach { chapterId ->
                        val chapter = chapterRepository.findChapterById(chapterId)
                        if (chapter != null) {
                            val book = bookRepository.findBookById(chapter.bookId)
                            if (book != null) {
                                val savedDownload = buildSavedDownload(book, chapter)
                                downloadUseCases.deleteSavedDownload(savedDownload.toDownload())
                            }
                        }
                    }
                }
                return ServiceResult.Success(Unit)
            }
            
            // Create saved downloads for legacy state
            val savedDownloads = chaptersToDownload.map { (chapter, book) ->
                buildSavedDownload(book, chapter)
            }
            
            // Insert into database
            withContext(Dispatchers.IO) {
                downloadUseCases.insertDownloads(savedDownloads.map { it.toDownload() })
            }
            
            // Update legacy state
            val initialProgress = savedDownloads.associate { download ->
                download.chapterId to ireader.domain.services.downloaderService.DownloadProgress(
                    chapterId = download.chapterId,
                    status = LegacyDownloadStatus.QUEUED
                )
            }
            downloadServiceState.setDownloadProgress(initialProgress)
            downloadServiceState.setDownloads(savedDownloads)
            
            // Start WorkManager for background execution
            startWorkManager()
            
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to queue chapters: ${e.message}", e)
        }
    }
    
    override suspend fun queueBooks(bookIds: List<Long>): ServiceResult<Unit> {
        if (bookIds.isEmpty()) return ServiceResult.Error("Empty queue")
        
        return try {
            val chaptersToDownload = withContext(Dispatchers.IO) {
                bookIds.flatMap { bookId ->
                    val book = bookRepository.findBookById(bookId) ?: return@flatMap emptyList()
                    chapterRepository.findChaptersByBookId(bookId)
                        .filter { it.content.joinToString("").let { c -> c.isEmpty() || c.length < 50 } }
                        .map { it to book }
                }
            }
            
            if (chaptersToDownload.isEmpty()) return ServiceResult.Success(Unit)
            
            // Create saved downloads for legacy state
            val savedDownloads = chaptersToDownload.map { (chapter, book) ->
                buildSavedDownload(book, chapter)
            }
            
            // Insert into database
            withContext(Dispatchers.IO) {
                downloadUseCases.insertDownloads(savedDownloads.map { it.toDownload() })
            }
            
            // Update legacy state
            val initialProgress = savedDownloads.associate { download ->
                download.chapterId to ireader.domain.services.downloaderService.DownloadProgress(
                    chapterId = download.chapterId,
                    status = LegacyDownloadStatus.QUEUED
                )
            }
            downloadServiceState.setDownloadProgress(initialProgress)
            downloadServiceState.setDownloads(savedDownloads)
            
            // Start WorkManager for background execution
            startWorkManager()
            
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to queue books: ${e.message}", e)
        }
    }

    override suspend fun pause() {
        downloadServiceState.setPaused(true)
        
        val currentProgress = downloadServiceState.downloadProgress.value
        val updatedProgress = currentProgress.mapValues { (_, progress) ->
            if (progress.status == LegacyDownloadStatus.DOWNLOADING) {
                progress.copy(status = LegacyDownloadStatus.PAUSED)
            } else {
                progress
            }
        }
        downloadServiceState.setDownloadProgress(updatedProgress)
    }
    
    override suspend fun resume() {
        downloadServiceState.setPaused(false)
        
        val currentProgress = downloadServiceState.downloadProgress.value
        val updatedProgress = currentProgress.mapValues { (_, progress) ->
            if (progress.status == LegacyDownloadStatus.PAUSED) {
                progress.copy(status = LegacyDownloadStatus.QUEUED)
            } else {
                progress
            }
        }
        downloadServiceState.setDownloadProgress(updatedProgress)
        
        // Check if there are pending downloads that need to be restarted
        val pendingChapterIds = currentProgress
            .filter { 
                it.value.status == LegacyDownloadStatus.QUEUED ||
                it.value.status == LegacyDownloadStatus.PAUSED
            }
            .keys
            .toList()
        
        if (pendingChapterIds.isNotEmpty() && !downloadServiceState.isRunning.value) {
            // Restart WorkManager
            startWorkManager()
        }
    }
    
    override suspend fun cancelDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
            val currentProgress = downloadServiceState.downloadProgress.value
            val current = currentProgress[chapterId]
            if (current != null) {
                val updatedProgress = currentProgress.toMutableMap()
                updatedProgress[chapterId] = current.copy(
                    status = LegacyDownloadStatus.FAILED,
                    errorMessage = "Cancelled by user"
                )
                downloadServiceState.setDownloadProgress(updatedProgress)
            }
            
            // Remove from database
            val download = downloadServiceState.downloads.value.find { it.chapterId == chapterId }
            if (download != null) {
                withContext(Dispatchers.IO) {
                    downloadUseCases.deleteSavedDownload(download.toDownload())
                }
            }
            
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel download: ${e.message}", e)
        }
    }
    
    override suspend fun cancelAll(): ServiceResult<Unit> {
        return try {
            workManager.cancelAllWorkByTag(DOWNLOADER_SERVICE_NAME)
            
            // Clear legacy state
            downloadServiceState.setRunning(false)
            downloadServiceState.setPaused(false)
            downloadServiceState.setDownloadProgress(emptyMap())
            downloadServiceState.setDownloads(emptyList())
            
            withContext(Dispatchers.IO) {
                downloadUseCases.deleteAllSavedDownload()
            }
            
            _state.value = ServiceState.IDLE
            
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel download: ${e.message}", e)
        }
    }
    
    override suspend fun retryDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
            val currentProgress = downloadServiceState.downloadProgress.value
            val current = currentProgress[chapterId]
            if (current != null && current.status == LegacyDownloadStatus.FAILED) {
                val updatedProgress = currentProgress.toMutableMap()
                updatedProgress[chapterId] = current.copy(
                    status = LegacyDownloadStatus.QUEUED,
                    errorMessage = null,
                    retryCount = current.retryCount + 1
                )
                downloadServiceState.setDownloadProgress(updatedProgress)
                
                // Restart downloads if not running
                if (!downloadServiceState.isRunning.value) {
                    startWorkManager()
                }
            }
            
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to retry download: ${e.message}", e)
        }
    }
    
    override fun getDownloadStatus(chapterId: Long): DownloadStatus? {
        return downloadServiceState.downloadProgress.value[chapterId]?.status?.let { 
            mapLegacyStatusToServiceStatus(it) 
        }
    }
    
    /**
     * Retry all failed downloads.
     */
    suspend fun retryAllFailed(): ServiceResult<Unit> {
        return try {
            val currentProgress = downloadServiceState.downloadProgress.value.toMutableMap()
            var hasFailedDownloads = false
            
            currentProgress.forEach { (chapterId, progress) ->
                if (progress.status == LegacyDownloadStatus.FAILED) {
                    currentProgress[chapterId] = progress.copy(
                        status = LegacyDownloadStatus.QUEUED,
                        errorMessage = null,
                        retryCount = 0
                    )
                    hasFailedDownloads = true
                }
            }
            
            if (hasFailedDownloads) {
                downloadServiceState.setDownloadProgress(currentProgress)
                
                // Restart downloads if not running
                if (!downloadServiceState.isRunning.value) {
                    startWorkManager()
                }
            }
            
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to retry all downloads: ${e.message}", e)
        }
    }
    
    /**
     * Clear completed downloads from the queue.
     */
    suspend fun clearCompleted(): ServiceResult<Unit> {
        return try {
            val currentProgress = downloadServiceState.downloadProgress.value.toMutableMap()
            val completedChapterIds = currentProgress
                .filter { it.value.status == LegacyDownloadStatus.COMPLETED }
                .keys
            
            completedChapterIds.forEach { chapterId ->
                currentProgress.remove(chapterId)
            }
            downloadServiceState.setDownloadProgress(currentProgress)
            
            // Update downloads list
            val updatedDownloads = downloadServiceState.downloads.value.filter { 
                it.chapterId !in completedChapterIds 
            }
            downloadServiceState.setDownloads(updatedDownloads)
            
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to clear completed downloads: ${e.message}", e)
        }
    }
    
    /**
     * Clear failed downloads from the queue.
     */
    suspend fun clearFailed(): ServiceResult<Unit> {
        return try {
            val currentProgress = downloadServiceState.downloadProgress.value.toMutableMap()
            val failedChapterIds = currentProgress
                .filter { it.value.status == LegacyDownloadStatus.FAILED }
                .keys
            
            failedChapterIds.forEach { chapterId ->
                currentProgress.remove(chapterId)
                
                // Also remove from database
                val download = downloadServiceState.downloads.value.find { it.chapterId == chapterId }
                if (download != null) {
                    withContext(Dispatchers.IO) {
                        downloadUseCases.deleteSavedDownload(download.toDownload())
                    }
                }
            }
            downloadServiceState.setDownloadProgress(currentProgress)
            
            // Update downloads list
            val updatedDownloads = downloadServiceState.downloads.value.filter { 
                it.chapterId !in failedChapterIds 
            }
            downloadServiceState.setDownloads(updatedDownloads)
            
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to clear failed downloads: ${e.message}", e)
        }
    }
    
    private fun startWorkManager() {
        val workData = Data.Builder()
            .putBoolean(DownloadServiceConstants.DOWNLOADER_MODE, true)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<DownloaderService>()
            .setInputData(workData)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .addTag(DOWNLOADER_SERVICE_NAME)
            .build()
        
        downloadServiceState.setRunning(true)
        downloadServiceState.setPaused(false)
        
        workManager.enqueueUniqueWork(
            DOWNLOADER_SERVICE_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
