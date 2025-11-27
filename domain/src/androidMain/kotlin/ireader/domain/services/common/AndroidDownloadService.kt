package ireader.domain.services.common

import android.content.Context
import androidx.work.*
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.SavedDownload
import ireader.domain.models.entities.buildSavedDownload
import ireader.domain.services.downloaderService.DownloadServiceConstants.DOWNLOADER_BOOKS_IDS
import ireader.domain.services.downloaderService.DownloadServiceConstants.DOWNLOADER_CHAPTERS_IDS
import ireader.domain.services.downloaderService.DownloadServiceConstants.DOWNLOADER_SERVICE_NAME
import ireader.domain.services.downloaderService.DownloadStateHolder
import ireader.domain.services.downloaderService.DownloaderService
import ireader.domain.usecases.download.DownloadUseCases
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
 * Android implementation of DownloadService using WorkManager
 *
 * This service integrates with the existing DownloaderService (WorkManager worker)
 * and DownloadStateHolder to provide a unified download management interface.
 *
 * Key responsibilities:
 * - Queue downloads via WorkManager using DownloaderService
 * - Observe and expose download state from DownloadStateHolder
 * - Provide pause/resume/cancel functionality
 */
class AndroidDownloadService(
    private val context: Context
) : DownloadService, KoinComponent {

    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Inject the shared download state that DownloaderService also uses
    private val downloadServiceState: DownloadStateHolder by inject()
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

    // Set up observers immediately when service is created
    init {
        // Observe legacy state and map to new state
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

        // Observe legacy download progress and map to new format
        scope.launch {
            downloadServiceState.downloadProgress.collect { legacyProgress ->
                _downloadProgress.value = legacyProgress.mapValues { (chapterId, legacy) ->
                    DownloadProgress(
                        chapterId = chapterId,
                        chapterName = "",
                        bookName = "",
                        status = mapLegacyStatus(legacy.status),
                        progress = legacy.progress,
                        errorMessage = legacy.errorMessage,
                        retryCount = legacy.retryCount,
                        totalRetries = 3
                    )
                }
            }
        }
    }

    override suspend fun initialize() {
        _state.value = ServiceState.INITIALIZING

        // Observers are now set up in init block, so just set state to IDLE
        _state.value = ServiceState.IDLE
    }

    /**
     * Map legacy DownloadStatus to new DownloadStatus
     */
    private fun mapLegacyStatus(legacyStatus: ireader.domain.services.downloaderService.DownloadStatus): DownloadStatus {
        return when (legacyStatus) {
            ireader.domain.services.downloaderService.DownloadStatus.QUEUED -> DownloadStatus.QUEUED
            ireader.domain.services.downloaderService.DownloadStatus.DOWNLOADING -> DownloadStatus.DOWNLOADING
            ireader.domain.services.downloaderService.DownloadStatus.PAUSED -> DownloadStatus.PAUSED
            ireader.domain.services.downloaderService.DownloadStatus.COMPLETED -> DownloadStatus.COMPLETED
            ireader.domain.services.downloaderService.DownloadStatus.FAILED -> DownloadStatus.FAILED
        }
    }

    override suspend fun start() {
        _state.value = ServiceState.RUNNING
    }

    override suspend fun stop() {
        _state.value = ServiceState.STOPPED
        workManager.cancelAllWorkByTag(DOWNLOADER_SERVICE_NAME)
        downloadServiceState.setRunning(false)
        downloadServiceState.setPaused(false)
    }

    override fun isRunning(): Boolean {
        return _state.value == ServiceState.RUNNING || downloadServiceState.isRunning.value
    }

    override suspend fun cleanup() {
        stop()
        downloadServiceState.setDownloadProgress(emptyMap())
    }

    override suspend fun queueChapters(chapterIds: List<Long>): ServiceResult<Unit> {
        if (chapterIds.isEmpty()) {
            return ServiceResult.Error("No chapters to queue")
        }

        return try {
            // Get chapter and book info to insert into database immediately
            val chaptersToDownload = withContext(Dispatchers.IO) {
                val allChapters = mutableListOf<SavedDownload>()
                for (chapterId in chapterIds) {
                    val chapter = chapterRepository.findChapterById(chapterId) ?: continue
                    val book = bookRepository.findBookById(chapter.bookId) ?: continue
                    allChapters.add(buildSavedDownload(book, chapter))
                }
                allChapters
            }

            if (chaptersToDownload.isEmpty()) {
                return ServiceResult.Error("No valid chapters found")
            }

            // Insert downloads into database immediately so they appear in download screen
            withContext(Dispatchers.IO) {
                downloadUseCases.insertDownloads(chaptersToDownload.map { it.toDownload() })
            }

            // Initialize progress for queued chapters
            val initialProgress = chaptersToDownload.associate { download ->
                download.chapterId to ireader.domain.services.downloaderService.DownloadProgress(
                    chapterId = download.chapterId,
                    status = ireader.domain.services.downloaderService.DownloadStatus.QUEUED
                )
            }
            downloadServiceState.setDownloadProgress(
                downloadServiceState.downloadProgress.value + initialProgress
            )
            downloadServiceState.setDownloads(chaptersToDownload)

            // Create unique work name
            val workName = "${DOWNLOADER_SERVICE_NAME}_chapters_${chapterIds.hashCode()}_${System.currentTimeMillis()}"

            val workData = Data.Builder()
                .putLongArray(DOWNLOADER_CHAPTERS_IDS, chapterIds.toLongArray())
                .build()

            val workRequest = OneTimeWorkRequestBuilder<DownloaderService>()
                .setInputData(workData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(DOWNLOADER_SERVICE_NAME)
                .build()

            if (isRunning()) {
                workManager.enqueue(workRequest)
            } else {
                workManager.enqueueUniqueWork(
                    workName,
                    ExistingWorkPolicy.KEEP,
                    workRequest
                )
            }

            _state.value = ServiceState.RUNNING
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to queue chapters: \${e.message}", e)
        }
    }

    override suspend fun queueBooks(bookIds: List<Long>): ServiceResult<Unit> {
        if (bookIds.isEmpty()) {
            return ServiceResult.Error("No books to queue")
        }

        return try {
            // Get chapters for all books and insert them into the database immediately
            // so they appear in the download screen right away
            val chaptersToDownload = withContext(Dispatchers.IO) {
                val allChapters = mutableListOf<SavedDownload>()
                for (bookId in bookIds) {
                    val book = bookRepository.findBookById(bookId) ?: continue
                    val chapters = chapterRepository.findChaptersByBookId(bookId)

                    // Filter chapters that need downloading (empty or very short content)
                    val needsDownload = chapters.filter { chapter ->
                        val contentText = chapter.content.joinToString("")
                        contentText.isEmpty() || contentText.length < 50
                    }

                    // Create SavedDownload entries
                    needsDownload.forEach { chapter ->
                        allChapters.add(buildSavedDownload(book, chapter))
                    }
                }
                allChapters
            }

            if (chaptersToDownload.isEmpty()) {
                return ServiceResult.Error("No chapters need downloading - all chapters already have content")
            }

            // Insert downloads into database immediately so they appear in download screen
            withContext(Dispatchers.IO) {
                downloadUseCases.insertDownloads(chaptersToDownload.map { it.toDownload() })
            }

            // Initialize progress for queued chapters
            val initialProgress = chaptersToDownload.associate { download ->
                download.chapterId to ireader.domain.services.downloaderService.DownloadProgress(
                    chapterId = download.chapterId,
                    status = ireader.domain.services.downloaderService.DownloadStatus.QUEUED
                )
            }
            downloadServiceState.setDownloadProgress(
                downloadServiceState.downloadProgress.value + initialProgress
            )
            downloadServiceState.setDownloads(chaptersToDownload)

            // Create unique work name
            val workName = "${DOWNLOADER_SERVICE_NAME}_books_\${bookIds.hashCode()}_\${System.currentTimeMillis()}"

            val workData = Data.Builder()
                .putLongArray(DOWNLOADER_BOOKS_IDS, bookIds.toLongArray())
                .build()

            val workRequest = OneTimeWorkRequestBuilder<DownloaderService>()
                .setInputData(workData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(DOWNLOADER_SERVICE_NAME)
                .build()

            if (isRunning()) {
                workManager.enqueue(workRequest)
            } else {
                workManager.enqueueUniqueWork(
                    workName,
                    ExistingWorkPolicy.KEEP,
                    workRequest
                )
            }

            _state.value = ServiceState.RUNNING
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to queue books: \${e.message}", e)
        }
    }

    override suspend fun pause() {
        downloadServiceState.setPaused(true)
        _state.value = ServiceState.PAUSED

        // Update all downloading items to paused status
        val currentProgress = downloadServiceState.downloadProgress.value
        val updatedProgress = currentProgress.mapValues { (chapterId, progress) ->
            if (progress.status == ireader.domain.services.downloaderService.DownloadStatus.DOWNLOADING) {
                progress.copy(status = ireader.domain.services.downloaderService.DownloadStatus.PAUSED)
            } else {
                progress
            }
        }
        downloadServiceState.setDownloadProgress(updatedProgress)
    }

    override suspend fun resume() {
        downloadServiceState.setPaused(false)
        _state.value = ServiceState.RUNNING

        // Update all paused items back to downloading status
        val currentProgress = downloadServiceState.downloadProgress.value
        val updatedProgress = currentProgress.mapValues { (chapterId, progress) ->
            if (progress.status == ireader.domain.services.downloaderService.DownloadStatus.PAUSED) {
                progress.copy(status = ireader.domain.services.downloaderService.DownloadStatus.DOWNLOADING)
            } else {
                progress
            }
        }
        downloadServiceState.setDownloadProgress(updatedProgress)
    }

    override suspend fun cancelDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
            // Update status to cancelled in progress map
            val currentProgress = downloadServiceState.downloadProgress.value.toMutableMap()
            currentProgress[chapterId] = ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = chapterId,
                status = ireader.domain.services.downloaderService.DownloadStatus.FAILED,
                errorMessage = "Cancelled by user"
            )
            downloadServiceState.setDownloadProgress(currentProgress)

            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel download: \${e.message}", e)
        }
    }

    override suspend fun cancelAll(): ServiceResult<Unit> {
        return try {
            // Get current download info for notification before clearing
            val totalDownloads = downloadServiceState.downloads.value.size
            val completedCount = downloadServiceState.downloadProgress.value.values
                .count { it.status == ireader.domain.services.downloaderService.DownloadStatus.COMPLETED }

            // Cancel all WorkManager jobs - this will trigger the worker's onStopped callback
            workManager.cancelAllWorkByTag(DOWNLOADER_SERVICE_NAME)

            // Delete all pending downloads from database
            withContext(Dispatchers.IO) {
                downloadUseCases.deleteAllSavedDownload()
            }

            // Reset state
            downloadServiceState.setRunning(false)
            downloadServiceState.setPaused(false)
            downloadServiceState.setDownloadProgress(emptyMap())
            downloadServiceState.setDownloads(emptyList())

            _state.value = ServiceState.IDLE
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel all downloads: \${e.message}", e)
        }
    }

    override suspend fun retryDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
            val current = downloadServiceState.downloadProgress.value[chapterId]
            if (current == null) {
                return ServiceResult.Error("Download not found")
            }
            if (current.status != ireader.domain.services.downloaderService.DownloadStatus.FAILED) {
                return ServiceResult.Error("Can only retry failed downloads")
            }

            // Update status to queued with incremented retry count
            val updatedProgress = downloadServiceState.downloadProgress.value.toMutableMap()
            updatedProgress[chapterId] = current.copy(
                status = ireader.domain.services.downloaderService.DownloadStatus.QUEUED,
                errorMessage = null,
                retryCount = current.retryCount + 1
            )
            downloadServiceState.setDownloadProgress(updatedProgress)

            // Re-queue the chapter
            queueChapters(listOf(chapterId))
        } catch (e: Exception) {
            ServiceResult.Error("Failed to retry download: \${e.message}", e)
        }
    }

    override fun getDownloadStatus(chapterId: Long): DownloadStatus? {
        val legacyStatus = downloadServiceState.downloadProgress.value[chapterId]?.status
        return legacyStatus?.let { mapLegacyStatus(it) }
    }

}