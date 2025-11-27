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
 */
class AndroidDownloadService(
    private val context: Context
) : DownloadService, KoinComponent {
    
    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val downloadServiceState: DownloadStateHolder by inject()
    private val bookRepository: BookRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val downloadUseCases: DownloadUseCases by inject()
    
    private val _state = MutableStateFlow<ServiceState>(ServiceState.IDLE)
    override val state: StateFlow<ServiceState> = _state.asStateFlow()
    
    override val downloads: StateFlow<List<SavedDownload>> = downloadServiceState.downloads
    
    private val _downloadProgress = MutableStateFlow<Map<Long, DownloadProgress>>(emptyMap())
    override val downloadProgress: StateFlow<Map<Long, DownloadProgress>> = _downloadProgress.asStateFlow()
    
    init {
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
        
        scope.launch {
            downloadServiceState.downloadProgress.collect { legacyProgress ->
                _downloadProgress.value = legacyProgress.mapValues { (chapterId, legacy) ->
                    DownloadProgress(
                        chapterId = chapterId,
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
    
    private fun mapLegacyStatus(legacyStatus: ireader.domain.services.downloaderService.DownloadStatus): DownloadStatus {
        return when (legacyStatus) {
            ireader.domain.services.downloaderService.DownloadStatus.QUEUED -> DownloadStatus.QUEUED
            ireader.domain.services.downloaderService.DownloadStatus.DOWNLOADING -> DownloadStatus.DOWNLOADING
            ireader.domain.services.downloaderService.DownloadStatus.PAUSED -> DownloadStatus.PAUSED
            ireader.domain.services.downloaderService.DownloadStatus.COMPLETED -> DownloadStatus.COMPLETED
            ireader.domain.services.downloaderService.DownloadStatus.FAILED -> DownloadStatus.FAILED
        }
    }
    
    override suspend fun initialize() {
        _state.value = ServiceState.IDLE
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
        if (chapterIds.isEmpty()) return ServiceResult.Error("No chapters to queue")
        
        return try {
            val chaptersToDownload = withContext(Dispatchers.IO) {
                chapterIds.mapNotNull { chapterId ->
                    val chapter = chapterRepository.findChapterById(chapterId) ?: return@mapNotNull null
                    val book = bookRepository.findBookById(chapter.bookId) ?: return@mapNotNull null
                    buildSavedDownload(book, chapter)
                }
            }
            
            if (chaptersToDownload.isEmpty()) return ServiceResult.Error("No valid chapters found")
            
            withContext(Dispatchers.IO) {
                downloadUseCases.insertDownloads(chaptersToDownload.map { it.toDownload() })
            }
            
            val initialProgress = chaptersToDownload.associate { download ->
                download.chapterId to ireader.domain.services.downloaderService.DownloadProgress(
                    chapterId = download.chapterId,
                    status = ireader.domain.services.downloaderService.DownloadStatus.QUEUED
                )
            }
            downloadServiceState.setDownloadProgress(downloadServiceState.downloadProgress.value + initialProgress)
            downloadServiceState.setDownloads(chaptersToDownload)
            
            val workName = "${DOWNLOADER_SERVICE_NAME}_chapters_${chapterIds.hashCode()}_${System.currentTimeMillis()}"
            val workData = Data.Builder().putLongArray(DOWNLOADER_CHAPTERS_IDS, chapterIds.toLongArray()).build()
            val workRequest = OneTimeWorkRequestBuilder<DownloaderService>()
                .setInputData(workData)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .addTag(DOWNLOADER_SERVICE_NAME)
                .build()
            
            if (isRunning()) workManager.enqueue(workRequest)
            else workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.KEEP, workRequest)
            
            _state.value = ServiceState.RUNNING
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to queue chapters: ${e.message}", e)
        }
    }
    
    override suspend fun queueBooks(bookIds: List<Long>): ServiceResult<Unit> {
        if (bookIds.isEmpty()) return ServiceResult.Error("No books to queue")
        
        return try {
            val chaptersToDownload = withContext(Dispatchers.IO) {
                bookIds.flatMap { bookId ->
                    val book = bookRepository.findBookById(bookId) ?: return@flatMap emptyList()
                    chapterRepository.findChaptersByBookId(bookId)
                        .filter { it.content.joinToString("").let { c -> c.isEmpty() || c.length < 50 } }
                        .map { buildSavedDownload(book, it) }
                }
            }
            
            if (chaptersToDownload.isEmpty()) return ServiceResult.Error("No chapters need downloading")
            
            withContext(Dispatchers.IO) {
                downloadUseCases.insertDownloads(chaptersToDownload.map { it.toDownload() })
            }
            
            val initialProgress = chaptersToDownload.associate { download ->
                download.chapterId to ireader.domain.services.downloaderService.DownloadProgress(
                    chapterId = download.chapterId,
                    status = ireader.domain.services.downloaderService.DownloadStatus.QUEUED
                )
            }
            downloadServiceState.setDownloadProgress(downloadServiceState.downloadProgress.value + initialProgress)
            downloadServiceState.setDownloads(chaptersToDownload)
            
            val workName = "${DOWNLOADER_SERVICE_NAME}_books_${bookIds.hashCode()}_${System.currentTimeMillis()}"
            val workData = Data.Builder().putLongArray(DOWNLOADER_BOOKS_IDS, bookIds.toLongArray()).build()
            val workRequest = OneTimeWorkRequestBuilder<DownloaderService>()
                .setInputData(workData)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .addTag(DOWNLOADER_SERVICE_NAME)
                .build()
            
            if (isRunning()) workManager.enqueue(workRequest)
            else workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.KEEP, workRequest)
            
            _state.value = ServiceState.RUNNING
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to queue books: ${e.message}", e)
        }
    }

    override suspend fun pause() {
        downloadServiceState.setPaused(true)
        _state.value = ServiceState.PAUSED
        val currentProgress = downloadServiceState.downloadProgress.value
        val updatedProgress = currentProgress.mapValues { (_, progress) ->
            if (progress.status == ireader.domain.services.downloaderService.DownloadStatus.DOWNLOADING) {
                progress.copy(status = ireader.domain.services.downloaderService.DownloadStatus.PAUSED)
            } else progress
        }
        downloadServiceState.setDownloadProgress(updatedProgress)
    }
    
    override suspend fun resume() {
        downloadServiceState.setPaused(false)
        _state.value = ServiceState.RUNNING
        val currentProgress = downloadServiceState.downloadProgress.value
        val updatedProgress = currentProgress.mapValues { (_, progress) ->
            if (progress.status == ireader.domain.services.downloaderService.DownloadStatus.PAUSED) {
                progress.copy(status = ireader.domain.services.downloaderService.DownloadStatus.DOWNLOADING)
            } else progress
        }
        downloadServiceState.setDownloadProgress(updatedProgress)
    }
    
    override suspend fun cancelDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
            val currentProgress = downloadServiceState.downloadProgress.value.toMutableMap()
            currentProgress[chapterId] = ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = chapterId,
                status = ireader.domain.services.downloaderService.DownloadStatus.FAILED,
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
            workManager.cancelAllWorkByTag(DOWNLOADER_SERVICE_NAME)
            withContext(Dispatchers.IO) { downloadUseCases.deleteAllSavedDownload() }
            downloadServiceState.setRunning(false)
            downloadServiceState.setPaused(false)
            downloadServiceState.setDownloadProgress(emptyMap())
            downloadServiceState.setDownloads(emptyList())
            _state.value = ServiceState.IDLE
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel all downloads: ${e.message}", e)
        }
    }
    
    override suspend fun retryDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
            val current = downloadServiceState.downloadProgress.value[chapterId]
                ?: return ServiceResult.Error("Download not found")
            if (current.status != ireader.domain.services.downloaderService.DownloadStatus.FAILED) {
                return ServiceResult.Error("Can only retry failed downloads")
            }
            val updatedProgress = downloadServiceState.downloadProgress.value.toMutableMap()
            updatedProgress[chapterId] = current.copy(
                status = ireader.domain.services.downloaderService.DownloadStatus.QUEUED,
                errorMessage = null,
                retryCount = current.retryCount + 1
            )
            downloadServiceState.setDownloadProgress(updatedProgress)
            queueChapters(listOf(chapterId))
        } catch (e: Exception) {
            ServiceResult.Error("Failed to retry download: ${e.message}", e)
        }
    }
    
    override fun getDownloadStatus(chapterId: Long): DownloadStatus? {
        return downloadServiceState.downloadProgress.value[chapterId]?.status?.let { mapLegacyStatus(it) }
    }
}
