package ireader.domain.services.download

import ireader.core.log.Log
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.download.Download
import ireader.domain.models.download.DownloadState
import ireader.domain.models.download.DownloadStatus
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.DownloadPreferences
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.utils.extensions.ioDispatcher
import ireader.i18n.LocalizeHelper
import ireader.i18n.asString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.pow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Mihon-style Downloader that handles the actual download logic.
 * 
 * IMPORTANT: Downloads are processed SEQUENTIALLY (one chapter at a time)
 * to prevent rate limiting and server overload.
 * 
 * Features:
 * - Sequential downloads (one chapter at a time)
 * - Configurable delay between downloads (default 1000ms)
 * - Exponential backoff retry (2s, 4s, 8s)
 * - Network-aware downloads (WiFi-only option)
 * - Disk space validation
 * - Rate limit compliance
 */
class Downloader(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val catalogStore: CatalogStore,
    private val remoteUseCases: RemoteUseCases,
    private val insertUseCases: LocalInsertUseCases,
    private val localizeHelper: LocalizeHelper,
    private val downloadPreferences: DownloadPreferences,
    private val downloadProvider: DownloadProvider,
    private val downloadCache: DownloadCache,
    private val networkStateProvider: NetworkStateProvider
) {
    
    private var scope: CoroutineScope? = null
    private var downloadJob: Job? = null
    
    // Mutex to ensure only one download runs at a time (sequential processing)
    private val downloadMutex = Mutex()
    
    // Track last download completion time for rate limiting
    private var lastDownloadTime: Long = 0
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()
    
    private val _isPausedDueToNetwork = MutableStateFlow(false)
    val isPausedDueToNetwork: StateFlow<Boolean> = _isPausedDueToNetwork.asStateFlow()
    
    private val _isPausedDueToDiskSpace = MutableStateFlow(false)
    val isPausedDueToDiskSpace: StateFlow<Boolean> = _isPausedDueToDiskSpace.asStateFlow()
    
    // Current active download for UI display
    private val _currentDownload = MutableStateFlow<Download?>(null)
    val currentDownload: StateFlow<Download?> = _currentDownload.asStateFlow()
    
    /**
     * Start downloading the given queue SEQUENTIALLY (one chapter at a time).
     */
    fun start(
        queue: List<DownloadState>,
        onProgress: (Download) -> Unit,
        onComplete: () -> Unit,
        onError: (Download, String) -> Unit
    ) {
        if (_isRunning.value) {
            Log.debug { "Downloader: Already running, ignoring start request" }
            return
        }
        
        scope = CoroutineScope(SupervisorJob() + ioDispatcher)
        _isRunning.value = true
        _isPaused.value = false
        _isPausedDueToNetwork.value = false
        _isPausedDueToDiskSpace.value = false
        _currentDownload.value = null
        
        downloadJob = scope?.launch {
            try {
                processQueueSequentially(queue, onProgress, onComplete, onError)
            } catch (e: Exception) {
                Log.error { "Downloader: Fatal error in download loop - ${e.message}" }
            } finally {
                _isRunning.value = false
                _currentDownload.value = null
            }
        }
    }
    
    fun pause() {
        _isPaused.value = true
        Log.debug { "Downloader: Paused" }
    }
    
    fun resume() {
        _isPaused.value = false
        _isPausedDueToNetwork.value = false
        _isPausedDueToDiskSpace.value = false
        Log.debug { "Downloader: Resumed" }
    }
    
    fun stop() {
        downloadJob?.cancel()
        scope?.cancel()
        scope = null
        _isRunning.value = false
        _isPaused.value = false
        _isPausedDueToNetwork.value = false
        _isPausedDueToDiskSpace.value = false
        _currentDownload.value = null
        lastDownloadTime = 0
        Log.debug { "Downloader: Stopped" }
    }

    
    /**
     * Process the download queue SEQUENTIALLY - one chapter at a time.
     * This prevents rate limiting and server overload.
     */
    @OptIn(ExperimentalTime::class)
    private suspend fun processQueueSequentially(
        queue: List<DownloadState>,
        onProgress: (Download) -> Unit,
        onComplete: () -> Unit,
        onError: (Download, String) -> Unit
    ) {
        val downloadDelay = downloadPreferences.downloadDelayMs().get()
        val wifiOnly = downloadPreferences.downloadOnlyOnWifi().get()
        val minDiskSpace = downloadPreferences.minimumDiskSpaceMb().get() * 1024 * 1024
        val maxRetries = downloadPreferences.maxRetryCount().get()
        val autoRetry = downloadPreferences.autoRetryFailed().get()
        
        Log.info { "Downloader: Starting sequential download of ${queue.size} chapters with ${downloadDelay}ms delay" }
        
        // Process downloads one at a time using mutex
        downloadMutex.withLock {
            for (downloadState in queue) {
                if (!isActive()) {
                    Log.debug { "Downloader: Stopping - not active" }
                    break
                }
                
                // Wait while paused
                while (_isPaused.value && _isRunning.value) {
                    delay(500)
                    if (!isActive()) break
                }
                
                if (!isActive()) break
                
                // Check network
                if (!checkNetwork(wifiOnly)) {
                    _isPausedDueToNetwork.value = true
                    Log.info { "Downloader: Waiting for WiFi connection..." }
                    while (!networkStateProvider.shouldAllowDownload(wifiOnly) && _isRunning.value) {
                        delay(1000)
                        if (!isActive()) break
                    }
                    _isPausedDueToNetwork.value = false
                }
                
                if (!isActive()) break
                
                // Check disk space
                if (!checkDiskSpace(minDiskSpace)) {
                    _isPausedDueToDiskSpace.value = true
                    onError(downloadState.download, "Not enough disk space")
                    Log.warn { "Downloader: Paused due to low disk space" }
                    continue
                }
                
                // Wait for rate limit delay since last download
                waitForRateLimit(downloadDelay)
                
                // Update current download for UI
                _currentDownload.value = downloadState.download
                
                // Download the chapter
                downloadWithRetry(
                    downloadState = downloadState,
                    maxRetries = if (autoRetry) maxRetries else 1,
                    onProgress = onProgress,
                    onError = onError
                )
                
                // Update last download time for rate limiting
                lastDownloadTime = Clock.System.now().toEpochMilliseconds()
                
                Log.debug { "Downloader: Completed ${downloadState.download.chapterName}, waiting ${downloadDelay}ms before next" }
            }
        }
        
        _currentDownload.value = null
        
        if (_isRunning.value) {
            Log.info { "Downloader: All downloads completed" }
            onComplete()
        }
    }
    
    /**
     * Wait for the configured delay since the last download completed.
     * This ensures proper rate limiting between requests.
     */
    @OptIn(ExperimentalTime::class)
    private suspend fun waitForRateLimit(delayMs: Long) {
        if (lastDownloadTime == 0L) return
        
        val now = Clock.System.now().toEpochMilliseconds()
        val elapsed = now - lastDownloadTime
        
        if (elapsed < delayMs) {
            val waitTime = delayMs - elapsed
            Log.debug { "Downloader: Rate limiting - waiting ${waitTime}ms" }
            delay(waitTime)
        }
    }
    
    private suspend fun downloadWithRetry(
        downloadState: DownloadState,
        maxRetries: Int,
        onProgress: (Download) -> Unit,
        onError: (Download, String) -> Unit
    ) {
        var attempt = 0
        var lastError: String? = null
        
        while (attempt < maxRetries && isActive()) {
            attempt++
            
            downloadState.update { download ->
                download.copy(status = DownloadStatus.DOWNLOADING, retryCount = attempt - 1)
            }
            onProgress(downloadState.download)
            
            try {
                val success = downloadChapter(downloadState, onProgress)
                if (success) {
                    downloadState.update { download ->
                        download.copy(status = DownloadStatus.DOWNLOADED, progress = 100)
                    }
                    onProgress(downloadState.download)
                    
                    downloadCache.addDownloadedChapter(
                        downloadState.download.bookId,
                        downloadState.download.chapterId
                    )
                    
                    Log.debug { "Downloader: Successfully downloaded ${downloadState.download.chapterName}" }
                    return
                } else {
                    lastError = "Download returned no content"
                }
            } catch (e: Exception) {
                lastError = getUserFriendlyErrorMessage(e)
                Log.error { "Downloader: Attempt $attempt/$maxRetries failed - $lastError" }
            }
            
            if (attempt < maxRetries && isActive()) {
                val backoffDelay = calculateBackoffDelay(attempt)
                Log.debug { "Downloader: Waiting ${backoffDelay}ms before retry" }
                delay(backoffDelay)
            }
        }
        
        downloadState.update { download ->
            download.copy(status = DownloadStatus.ERROR, errorMessage = lastError, retryCount = attempt)
        }
        onError(downloadState.download, lastError ?: "Unknown error")
    }

    
    private suspend fun downloadChapter(
        downloadState: DownloadState,
        onProgress: (Download) -> Unit
    ): Boolean {
        val download = downloadState.download
        
        val chapter = chapterRepository.findChapterById(download.chapterId)
            ?: throw Exception("Chapter not found")
        
        val existingContent = chapter.content.joinToString("")
        if (existingContent.isNotEmpty() && existingContent.length >= 50) {
            Log.debug { "Downloader: Chapter already downloaded, skipping" }
            return true
        }
        
        val book = bookRepository.findBookById(download.bookId)
            ?: throw Exception("Book not found")
        
        val source = catalogStore.catalogs.find { it.sourceId == book.sourceId }
            ?: throw Exception("Source not found")
        
        var downloadedChapter: Chapter? = null
        var downloadError: Exception? = null
        
        remoteUseCases.getRemoteReadingContent(
            chapter = chapter,
            catalog = source,
            onSuccess = { content -> downloadedChapter = content },
            onError = { message ->
                val errorMsg = message?.asString(localizeHelper) ?: "Download failed"
                downloadError = Exception(errorMsg)
            }
        )
        
        if (downloadError != null) throw downloadError!!
        if (downloadedChapter == null) throw Exception("No content received from source")
        
        val downloadedContent = downloadedChapter?.content?.joinToString("") ?: ""
        if (downloadedContent.isEmpty() || downloadedContent.length < 50) {
            throw Exception("Downloaded content is too short or empty")
        }
        
        val finalChapter = chapter.copy(content = downloadedChapter?.content ?: emptyList())
        insertUseCases.insertChapter(chapter = finalChapter)
        
        return true
    }
    
    private fun calculateBackoffDelay(attempt: Int): Long {
        // Exponential backoff: 2s, 4s, 8s for attempts 1, 2, 3
        return (2.0.pow(attempt.toDouble()) * 1000).toLong()
    }
    
    private fun checkNetwork(wifiOnly: Boolean): Boolean {
        return networkStateProvider.shouldAllowDownload(wifiOnly)
    }
    
    private fun checkDiskSpace(minBytes: Long): Boolean {
        val available = downloadProvider.getAvailableSpace()
        return available >= minBytes
    }
    
    private fun isActive(): Boolean {
        return _isRunning.value && scope?.isActive == true
    }
    
    companion object {
        fun getUserFriendlyErrorMessage(error: Throwable): String {
            val message = error.message ?: "Unknown error"
            
            return when {
                message.contains("Unable to resolve host", ignoreCase = true) ||
                message.contains("No address associated with hostname", ignoreCase = true) ->
                    "No internet connection"
                
                message.contains("timeout", ignoreCase = true) ->
                    "Connection timed out"
                
                message.contains("connection refused", ignoreCase = true) ->
                    "Cannot connect to server"
                
                message.contains("content is too short", ignoreCase = true) ||
                message.contains("no content received", ignoreCase = true) ->
                    "Chapter content not available"
                
                message.contains("404", ignoreCase = true) ->
                    "Chapter not found"
                
                message.contains("403", ignoreCase = true) ->
                    "Access denied"
                
                message.contains("500", ignoreCase = true) ||
                message.contains("502", ignoreCase = true) ||
                message.contains("503", ignoreCase = true) ->
                    "Source server error"
                
                message.contains("429", ignoreCase = true) ->
                    "Too many requests"
                
                else -> "Download failed: ${message.take(100)}"
            }
        }
    }
}
