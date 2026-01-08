package ireader.domain.services.common

import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.SavedDownload
import ireader.domain.models.entities.buildSavedDownload
import ireader.domain.services.download.DownloadManager
import ireader.domain.services.downloaderService.DownloadStateHolder
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.services.StartDownloadServicesUseCase
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
import ireader.domain.models.download.DownloadStatus as NewDownloadStatus
import ireader.domain.services.downloaderService.DownloadStatus as LegacyDownloadStatus

/**
 * Desktop implementation of DownloadService using coroutines.
 * 
 * This service now delegates to the new DownloadManager for core logic while
 * maintaining backward compatibility with legacy state.
 * 
 * The DownloadManager provides:
 * - Queue persistence across app restarts
 * - Filesystem cache for fast "is downloaded" checks
 * - Parallel downloads per source
 * - Exponential backoff retry
 * - Network-aware downloads
 * - Disk space validation
 */
class DesktopDownloadService : DownloadService, KoinComponent {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // New DownloadManager for enhanced functionality
    private val downloadManager: DownloadManager by inject()
    
    // Legacy state holder for backward compatibility
    private val downloadServiceState: DownloadStateHolder by inject()
    
    // Inject the use case for starting downloads (legacy)
    private val startDownloadServicesUseCase: StartDownloadServicesUseCase by inject()
    
    // Repositories for chapter/book lookup
    private val bookRepository: BookRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val downloadUseCases: DownloadUseCases by inject()
    
    // Map legacy state to new ServiceState
    private val _state = MutableStateFlow<ServiceState>(ServiceState.IDLE)
    override val state: StateFlow<ServiceState> = _state.asStateFlow()
    
    // Expose downloads from the shared state
    override val downloads: StateFlow<List<SavedDownload>> = downloadServiceState.downloads
    
    // Map legacy DownloadProgress to new DownloadProgress format
    private val _downloadProgress = MutableStateFlow<Map<Long, DownloadProgress>>(emptyMap())
    override val downloadProgress: StateFlow<Map<Long, DownloadProgress>> = _downloadProgress.asStateFlow()
    
    override suspend fun initialize() {
        _state.value = ServiceState.INITIALIZING
        
        // Initialize DownloadManager
        downloadManager.init()
        
        // Observe DownloadManager state and bridge to service state
        scope.launch {
            combine(
                downloadManager.isRunning,
                downloadManager.isPaused,
                downloadManager.isPausedDueToNetwork,
                downloadManager.isPausedDueToDiskSpace
            ) { isRunning, isPaused, isPausedNetwork, isPausedDisk ->
                when {
                    isPausedNetwork || isPausedDisk -> ServiceState.PAUSED
                    isPaused -> ServiceState.PAUSED
                    isRunning -> ServiceState.RUNNING
                    else -> ServiceState.IDLE
                }
            }.collect { newState ->
                _state.value = newState
            }
        }
        
        // Bridge DownloadManager queue to download progress
        scope.launch {
            downloadManager.queue.collect { queue ->
                val progressMap = queue.associate { downloadState ->
                    val download = downloadState.download
                    download.chapterId to DownloadProgress(
                        chapterId = download.chapterId,
                        chapterName = download.chapterName,
                        bookName = download.bookTitle,
                        status = mapNewStatusToServiceStatus(download.status),
                        progress = download.progressFloat,
                        errorMessage = download.errorMessage,
                        retryCount = download.retryCount,
                        totalRetries = 3
                    )
                }
                _downloadProgress.value = progressMap
            }
        }
        
        // Also observe legacy state for backward compatibility
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
                // Only update if DownloadManager is not running
                if (!downloadManager.isRunning.value) {
                    _state.value = newState
                }
            }
        }
        
        // Bridge legacy progress to our progress
        scope.launch {
            downloadServiceState.downloadProgress.collect { legacyProgress ->
                // Merge legacy progress with new progress
                val newProgress = _downloadProgress.value.toMutableMap()
                legacyProgress.forEach { (chapterId, legacy) ->
                    if (!newProgress.containsKey(chapterId)) {
                        newProgress[chapterId] = DownloadProgress(
                            chapterId = chapterId,
                            chapterName = "",
                            bookName = "",
                            status = mapLegacyStatusToServiceStatus(legacy.status),
                            progress = legacy.progress,
                            errorMessage = legacy.errorMessage,
                            retryCount = legacy.retryCount,
                            totalRetries = 3
                        )
                    }
                }
                _downloadProgress.value = newProgress
            }
        }
        
        _state.value = ServiceState.IDLE
    }
    
    private fun mapNewStatusToServiceStatus(status: NewDownloadStatus): DownloadStatus {
        return when (status) {
            NewDownloadStatus.NOT_DOWNLOADED -> DownloadStatus.QUEUED
            NewDownloadStatus.QUEUE -> DownloadStatus.QUEUED
            NewDownloadStatus.DOWNLOADING -> DownloadStatus.DOWNLOADING
            NewDownloadStatus.DOWNLOADED -> DownloadStatus.COMPLETED
            NewDownloadStatus.ERROR -> DownloadStatus.FAILED
            else -> DownloadStatus.FAILED
        }
    }
    
    private fun mapLegacyStatusToServiceStatus(legacyStatus: LegacyDownloadStatus): DownloadStatus {
        return when (legacyStatus) {
            LegacyDownloadStatus.QUEUED -> DownloadStatus.QUEUED
            LegacyDownloadStatus.DOWNLOADING -> DownloadStatus.DOWNLOADING
            LegacyDownloadStatus.PAUSED -> DownloadStatus.PAUSED
            LegacyDownloadStatus.COMPLETED -> DownloadStatus.COMPLETED
            LegacyDownloadStatus.FAILED -> DownloadStatus.FAILED
        }
    }
    
    override suspend fun start() {
        _state.value = ServiceState.RUNNING
        downloadManager.startDownloads()
    }
    
    override suspend fun stop() {
        _state.value = ServiceState.STOPPED
        startDownloadServicesUseCase.stop()
        downloadManager.cancelDownloads()
        downloadServiceState.setRunning(false)
        downloadServiceState.setPaused(false)
    }
    
    override fun isRunning(): Boolean {
        return _state.value == ServiceState.RUNNING || 
               downloadManager.isRunning.value ||
               downloadServiceState.isRunning.value
    }
    
    override suspend fun cleanup() {
        stop()
        downloadManager.clearQueue()
        downloadServiceState.setDownloadProgress(emptyMap())
    }
    
    override suspend fun queueChapters(chapterIds: List<Long>): ServiceResult<Unit> {
        if (chapterIds.isEmpty()) {
            return ServiceResult.Error("No chapters to queue")
        }
        
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
                return ServiceResult.Success(Unit)
            }
            
            // Add to DownloadManager queue
            val chapters = chaptersToDownload.map { it.first }
            val books = chaptersToDownload.associate { (chapter, book) -> chapter.bookId to book }
            downloadManager.addToQueue(chapters, books)
            
            // Also update legacy state for backward compatibility
            val savedDownloads = chaptersToDownload.map { (chapter, book) ->
                buildSavedDownload(book, chapter)
            }
            
            withContext(Dispatchers.IO) {
                downloadUseCases.insertDownloads(savedDownloads.map { it.toDownload() })
            }
            
            val initialProgress = savedDownloads.associate { download ->
                download.chapterId to ireader.domain.services.downloaderService.DownloadProgress(
                    chapterId = download.chapterId,
                    status = LegacyDownloadStatus.QUEUED
                )
            }
            downloadServiceState.setDownloadProgress(
                downloadServiceState.downloadProgress.value + initialProgress
            )
            downloadServiceState.setDownloads(
                downloadServiceState.downloads.value + savedDownloads
            )
            
            // Start downloads via legacy use case (for now)
            startDownloadServicesUseCase.start(
                bookIds = null,
                chapterIds = chaptersToDownload.map { it.first.id }.toLongArray(),
                downloadModes = false
            )
            
            downloadServiceState.setRunning(true)
            downloadServiceState.setPaused(false)
            
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to queue chapters: ${e.message}", e)
        }
    }
    
    override suspend fun queueBooks(bookIds: List<Long>): ServiceResult<Unit> {
        if (bookIds.isEmpty()) {
            return ServiceResult.Error("No books to queue")
        }
        
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
            
            // Add to DownloadManager queue
            val chapters = chaptersToDownload.map { it.first }
            val books = chaptersToDownload.associate { (chapter, book) -> chapter.bookId to book }
            downloadManager.addToQueue(chapters, books)
            
            // Also update legacy state for backward compatibility
            val savedDownloads = chaptersToDownload.map { (chapter, book) ->
                buildSavedDownload(book, chapter)
            }
            
            withContext(Dispatchers.IO) {
                downloadUseCases.insertDownloads(savedDownloads.map { it.toDownload() })
            }
            
            val initialProgress = savedDownloads.associate { download ->
                download.chapterId to ireader.domain.services.downloaderService.DownloadProgress(
                    chapterId = download.chapterId,
                    status = LegacyDownloadStatus.QUEUED
                )
            }
            downloadServiceState.setDownloadProgress(
                downloadServiceState.downloadProgress.value + initialProgress
            )
            downloadServiceState.setDownloads(
                downloadServiceState.downloads.value + savedDownloads
            )
            
            // Start downloads via legacy use case
            startDownloadServicesUseCase.start(
                bookIds = bookIds.toLongArray(),
                chapterIds = null,
                downloadModes = false
            )
            
            downloadServiceState.setRunning(true)
            downloadServiceState.setPaused(false)
            
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to queue books: ${e.message}", e)
        }
    }
    
    override suspend fun pause() {
        // Pause via DownloadManager
        downloadManager.pauseDownloads()
        
        // Also update legacy state for backward compatibility
        downloadServiceState.setPaused(true)
        downloadServiceState.setRunning(true)
        
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
        // Resume via DownloadManager
        downloadManager.resumeDownloads()
        
        // Also update legacy state for backward compatibility
        downloadServiceState.setPaused(false)
        downloadServiceState.setRunning(true)
        
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
                it.value.status == LegacyDownloadStatus.PAUSED ||
                it.value.status == LegacyDownloadStatus.QUEUED 
            }
            .keys.toList()
        
        if (pendingChapterIds.isNotEmpty()) {
            // Restart downloads if not running
            if (!downloadManager.isRunning.value) {
                downloadManager.startDownloads()
            }
        }
    }
    
    override suspend fun cancelDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
            // Remove from DownloadManager queue
            downloadManager.removeFromQueue(chapterId)
            
            // Also update legacy state
            val currentProgress = downloadServiceState.downloadProgress.value.toMutableMap()
            currentProgress[chapterId] = ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = chapterId,
                status = LegacyDownloadStatus.FAILED,
                errorMessage = "Cancelled by user"
            )
            downloadServiceState.setDownloadProgress(currentProgress)
            
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel download: ${e.message}", e)
        }
    }
    
    override suspend fun cancelAll(): ServiceResult<Unit> {
        return try {
            // Clear DownloadManager queue
            downloadManager.clearQueue()
            
            // Stop the legacy download service
            startDownloadServicesUseCase.stop()
            
            // Reset state
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
            ServiceResult.Error("Failed to cancel all downloads: ${e.message}", e)
        }
    }
    
    override suspend fun retryDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
            // Retry via DownloadManager
            downloadManager.retryDownload(chapterId)
            
            // Also update legacy state
            val current = downloadServiceState.downloadProgress.value[chapterId]
            if (current != null && current.status == LegacyDownloadStatus.FAILED) {
                val updatedProgress = downloadServiceState.downloadProgress.value.toMutableMap()
                updatedProgress[chapterId] = current.copy(
                    status = LegacyDownloadStatus.QUEUED,
                    errorMessage = null,
                    retryCount = current.retryCount + 1
                )
                downloadServiceState.setDownloadProgress(updatedProgress)
                
                // Restart downloads if not running
                if (!downloadManager.isRunning.value) {
                    queueChapters(listOf(chapterId))
                }
            }
            
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to retry download: ${e.message}", e)
        }
    }
    
    override fun getDownloadStatus(chapterId: Long): DownloadStatus? {
        // Check DownloadManager first
        val managerStatus = downloadManager.queue.value
            .find { it.download.chapterId == chapterId }
            ?.download?.status
        
        if (managerStatus != null) {
            return mapNewStatusToServiceStatus(managerStatus)
        }
        
        // Fall back to legacy state
        return downloadServiceState.downloadProgress.value[chapterId]?.status?.let { 
            mapLegacyStatusToServiceStatus(it) 
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // NEW METHODS - Enhanced functionality from DownloadManager
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Check if a chapter is downloaded using the new DownloadCache.
     */
    fun isChapterDownloaded(bookId: Long, chapterId: Long): Boolean {
        return downloadManager.isChapterDownloaded(bookId, chapterId)
    }
    
    /**
     * Get all downloaded chapter IDs for a book using the new DownloadCache.
     */
    fun getDownloadedChapterIds(bookId: Long): Set<Long> {
        return downloadManager.getDownloadedChapterIds(bookId)
    }
    
    /**
     * Check if downloads are paused due to network conditions.
     */
    fun isPausedDueToNetwork(): Boolean {
        return downloadManager.isPausedDueToNetwork.value
    }
    
    /**
     * Check if downloads are paused due to low disk space.
     */
    fun isPausedDueToDiskSpace(): Boolean {
        return downloadManager.isPausedDueToDiskSpace.value
    }
    
    /**
     * Retry all failed downloads.
     */
    suspend fun retryAllFailed(): ServiceResult<Unit> {
        return try {
            downloadManager.retryAllFailed()
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
            downloadManager.clearCompleted()
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
            downloadManager.clearFailed()
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to clear failed downloads: ${e.message}", e)
        }
    }
}
