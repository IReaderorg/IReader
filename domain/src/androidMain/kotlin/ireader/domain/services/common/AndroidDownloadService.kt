package ireader.domain.services.common

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import ireader.core.log.Log
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
 * Rebuilt from scratch for complete synchronization, non-destructive pause/resume,
 * and reliable background execution.
 */
class AndroidDownloadService(
    private val context: Context
) : DownloadService, KoinComponent {

    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val downloadServiceState: DownloadStateHolder by inject()
    private val bookRepository: BookRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val downloadUseCases: DownloadUseCases by inject()

    private val _state = MutableStateFlow<ServiceState>(ServiceState.IDLE)
    override val state: StateFlow<ServiceState> = _state.asStateFlow()

    override val downloads: StateFlow<List<SavedDownload>> = downloadServiceState.downloads

    private val _downloadProgress = MutableStateFlow<Map<Long, DownloadProgress>>(emptyMap())
    override val downloadProgress: StateFlow<Map<Long, DownloadProgress>> = _downloadProgress.asStateFlow()

    @Volatile
    private var isWorkManagerActive = false

    init {
        // 1. Observe WorkManager work status to know if background job is alive
        scope.launch {
            try {
                workManager.getWorkInfosByTagFlow(DOWNLOADER_SERVICE_NAME).collect { workInfos ->
                    val active = workInfos.any { 
                        it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED 
                    }
                    isWorkManagerActive = active
                    
                    // If work completed or cancelled in WorkManager and we were running
                    if (!active && downloadServiceState.isRunning.value && !downloadServiceState.isPaused.value) {
                        downloadServiceState.setRunning(false)
                    }
                }
            } catch (e: Exception) {
                Log.error { "AndroidDownloadService: Error observing WorkInfos: ${e.message}" }
            }
        }

        // 2. Synchronize service state with DownloadStateHolder
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

        // 3. Bridge legacy progress to our typed progress
        scope.launch {
            downloadServiceState.downloadProgress.collect { legacyProgress ->
                val downloadsList = downloadServiceState.downloads.value
                val newProgress = legacyProgress.mapValues { (chapterId, legacy) ->
                    val download = downloadsList.find { it.chapterId == chapterId }
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
        if (!downloadServiceState.isRunning.value && !downloadServiceState.isPaused.value) {
            _state.value = ServiceState.IDLE
        }
    }

    override suspend fun start() {
        Log.info { "AndroidDownloadService: start() called" }
        downloadServiceState.setRunning(true)
        downloadServiceState.setPaused(false)
        _state.value = ServiceState.RUNNING
        startWorkManager()
    }

    override suspend fun stop() {
        Log.info { "AndroidDownloadService: stop() called" }
        _state.value = ServiceState.STOPPED
        try {
            workManager.cancelAllWorkByTag(DOWNLOADER_SERVICE_NAME)
        } catch (e: Exception) {
            Log.error { "AndroidDownloadService: Error cancelling work: ${e.message}" }
        }
        downloadServiceState.setRunning(false)
        downloadServiceState.setPaused(false)
    }

    override fun isRunning(): Boolean {
        return _state.value == ServiceState.RUNNING || downloadServiceState.isRunning.value || isWorkManagerActive
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
            downloadServiceState.updateDownloadProgress { current -> current + initialProgress }
            downloadServiceState.updateDownloads { current -> (current + savedDownloads).distinctBy { it.chapterId } }

            startWorkManager()

            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            Log.error { "AndroidDownloadService: Failed to queue chapters: ${e.message}" }
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

            if (chaptersToDownload.isEmpty()) {
                return ServiceResult.Success(Unit)
            }

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
            downloadServiceState.updateDownloadProgress { current -> current + initialProgress }
            downloadServiceState.updateDownloads { current -> (current + savedDownloads).distinctBy { it.chapterId } }

            startWorkManager()

            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            Log.error { "AndroidDownloadService: Failed to queue books: ${e.message}" }
            ServiceResult.Error("Failed to queue books: ${e.message}", e)
        }
    }

    override suspend fun pause() {
        Log.info { "AndroidDownloadService: pause() called" }
        downloadServiceState.setPaused(true)
        _state.value = ServiceState.PAUSED

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
        Log.info { "AndroidDownloadService: resume() called" }
        downloadServiceState.setPaused(false)
        downloadServiceState.setRunning(true)
        _state.value = ServiceState.RUNNING

        val currentProgress = downloadServiceState.downloadProgress.value
        val updatedProgress = currentProgress.mapValues { (_, progress) ->
            if (progress.status == LegacyDownloadStatus.PAUSED) {
                progress.copy(status = LegacyDownloadStatus.QUEUED)
            } else {
                progress
            }
        }
        downloadServiceState.setDownloadProgress(updatedProgress)

        if (!isWorkManagerActive) {
            startWorkManager()
        }
    }

    override suspend fun cancelDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
            downloadServiceState.updateDownloadProgress { current ->
                current.filterKeys { it != chapterId }
            }

            downloadServiceState.updateDownloads { current ->
                current.filter { it.chapterId != chapterId }
            }

            withContext(Dispatchers.IO) {
                downloadUseCases.deleteSavedDownload(
                    ireader.domain.models.entities.Download(
                        chapterId = chapterId,
                        bookId = 0,
                        priority = 0
                    )
                )
            }

            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            Log.error { "AndroidDownloadService: Failed to cancel download $chapterId: ${e.message}" }
            ServiceResult.Error("Failed to cancel download: ${e.message}", e)
        }
    }

    override suspend fun cancelAll(): ServiceResult<Unit> {
        return try {
            Log.info { "AndroidDownloadService: cancelAll() called" }
            try {
                workManager.cancelAllWorkByTag(DOWNLOADER_SERVICE_NAME)
            } catch (e: Exception) {
                Log.error { "AndroidDownloadService: Error cancelling WorkManager tag: ${e.message}" }
            }

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
            Log.error { "AndroidDownloadService: Failed to cancel all downloads: ${e.message}" }
            ServiceResult.Error("Failed to cancel all downloads: ${e.message}", e)
        }
    }

    override suspend fun retryDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
            val current = downloadServiceState.downloadProgress.value[chapterId]
            if (current != null && current.status == LegacyDownloadStatus.FAILED) {
                downloadServiceState.updateChapterProgress(
                    chapterId,
                    current.copy(
                        status = LegacyDownloadStatus.QUEUED,
                        errorMessage = null,
                        retryCount = current.retryCount + 1
                    )
                )

                if (!downloadServiceState.isRunning.value) {
                    start()
                }
            }

            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            Log.error { "AndroidDownloadService: Failed to retry download $chapterId: ${e.message}" }
            ServiceResult.Error("Failed to retry download: ${e.message}", e)
        }
    }

    override fun getDownloadStatus(chapterId: Long): DownloadStatus? {
        return downloadServiceState.downloadProgress.value[chapterId]?.status?.let {
            mapLegacyStatusToServiceStatus(it)
        }
    }

    override suspend fun retryAllFailed(): ServiceResult<Unit> {
        return try {
            var hasFailedDownloads = false
            downloadServiceState.updateDownloadProgress { current ->
                current.mapValues { (_, progress) ->
                    if (progress.status == LegacyDownloadStatus.FAILED) {
                        hasFailedDownloads = true
                        progress.copy(
                            status = LegacyDownloadStatus.QUEUED,
                            errorMessage = null,
                            retryCount = 0
                        )
                    } else progress
                }
            }

            if (hasFailedDownloads && !downloadServiceState.isRunning.value) {
                start()
            }

            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            Log.error { "AndroidDownloadService: Failed to retry all failed: ${e.message}" }
            ServiceResult.Error("Failed to retry all downloads: ${e.message}", e)
        }
    }

    override suspend fun clearCompleted(): ServiceResult<Unit> {
        return try {
            val completedChapterIds = downloadServiceState.downloadProgress.value
                .filter { it.value.status == LegacyDownloadStatus.COMPLETED }
                .keys
                .toSet()

            downloadServiceState.updateDownloadProgress { current ->
                current.filterKeys { it !in completedChapterIds }
            }

            downloadServiceState.updateDownloads { current ->
                current.filter { it.chapterId !in completedChapterIds }
            }

            withContext(Dispatchers.IO) {
                completedChapterIds.forEach { chapterId ->
                    downloadUseCases.deleteSavedDownload(
                        ireader.domain.models.entities.Download(chapterId = chapterId, bookId = 0, priority = 0)
                    )
                }
            }

            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            Log.error { "AndroidDownloadService: Failed to clear completed: ${e.message}" }
            ServiceResult.Error("Failed to clear completed downloads: ${e.message}", e)
        }
    }

    override suspend fun clearFailed(): ServiceResult<Unit> {
        return try {
            val failedChapterIds = downloadServiceState.downloadProgress.value
                .filter { it.value.status == LegacyDownloadStatus.FAILED }
                .keys
                .toSet()

            downloadServiceState.updateDownloadProgress { current ->
                current.filterKeys { it !in failedChapterIds }
            }

            downloadServiceState.updateDownloads { current ->
                current.filter { it.chapterId !in failedChapterIds }
            }

            withContext(Dispatchers.IO) {
                failedChapterIds.forEach { chapterId ->
                    downloadUseCases.deleteSavedDownload(
                        ireader.domain.models.entities.Download(chapterId = chapterId, bookId = 0, priority = 0)
                    )
                }
            }

            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            Log.error { "AndroidDownloadService: Failed to clear failed: ${e.message}" }
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
        _state.value = ServiceState.RUNNING

        workManager.enqueueUniqueWork(
            DOWNLOADER_SERVICE_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
