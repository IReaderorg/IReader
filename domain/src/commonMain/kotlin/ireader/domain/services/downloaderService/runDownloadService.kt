package ireader.domain.services.downloaderService

import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.SavedDownload
import ireader.domain.models.entities.buildSavedDownload
import ireader.domain.notification.NotificationsIds.ID_DOWNLOAD_CHAPTER_PROGRESS
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.utils.NotificationManager
import ireader.i18n.LocalizeHelper
import ireader.i18n.asString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun runDownloadService(
    inputtedChapterIds: LongArray?,
    inputtedBooksIds: LongArray?,
    inputtedDownloaderMode: Boolean,
    bookRepo: BookRepository,
    chapterRepo: ChapterRepository,
    remoteUseCases: RemoteUseCases,
    localizeHelper: LocalizeHelper,
    extensions: CatalogStore,
    insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    downloadUseCases: DownloadUseCases,
    downloadServiceState: DownloadServiceStateImpl,
    notificationManager : NotificationManager,
    updateProgress:(max: Int, current: Int, inProgress: Boolean) -> Unit,
    updateTitle: (String) -> Unit,
    updateSubtitle: (String) -> Unit,
    onCancel:(error: Throwable,bookName:String) -> Unit,
    onSuccess: (completedDownloads: List<CompletedDownload>) -> Unit,
    updateNotification: (Int) -> Unit,
    downloadDelayMs: Long = 1000L,
    concurrentLimit: Int = 3
) : Boolean {
    // Track completed and failed downloads for notifications
    val completedDownloadsList = mutableListOf<CompletedDownload>()
    val failedDownloadsList = mutableMapOf<Long, FailedDownload>()
    
    var savedDownload: SavedDownload =
        SavedDownload(
            bookId = 0,
            priority = 1,
            chapterName = "",
            chapterKey = "",
            translator = "",
            chapterId = 0,
            bookName = "",
        )

    try {

        var tries = 0

        val chapters: List<Chapter> = when {
            inputtedBooksIds != null -> {
                inputtedBooksIds.flatMap {
                    chapterRepo.findChaptersByBookId(it)
                }
            }
            inputtedChapterIds != null -> {
                inputtedChapterIds.map {
                    chapterRepo.findChapterById(it)
                }.filterNotNull()
            }
            inputtedDownloaderMode -> {
                downloadUseCases.findAllDownloadsUseCase().mapNotNull {
                    chapterRepo.findChapterById(it.chapterId)
                }
            }
            else -> {
                throw Exception("There is no chapter.")
            }
        }
        val distinctBookIds = chapters.map { it.bookId }.distinct()
        val books = distinctBookIds.mapNotNull {
            bookRepo.findBookById(it)
        }
        val distinctSources = books.map { it.sourceId }.distinct().filterNotNull()
        val sources =
            extensions.catalogs.filter { it.sourceId in distinctSources }

        val downloads =
            chapters.filter { chapter ->
                // Check if chapter needs downloading: content is empty or very short (less than 50 chars)
                val contentText = chapter.content.joinToString("")
                contentText.isEmpty() || contentText.length < 50
            }
                .mapNotNull { chapter ->
                    val book = books.find { book -> book.id == chapter.bookId }
                    book?.let { b ->
                        buildSavedDownload(b, chapter)
                    }
                }

        downloadUseCases.insertDownloads(downloads.map { it.toDownload() })
        
        updateProgress(downloads.size, 0, true)
        updateNotification(ID_DOWNLOAD_CHAPTER_PROGRESS)
        downloadServiceState.downloads = downloads
        downloadServiceState.isEnable = true
        
        // Initialize download progress for all chapters
        val initialProgress = downloads.associate { download ->
            download.chapterId to DownloadProgress(
                chapterId = download.chapterId,
                bytesDownloaded = 0,
                totalBytes = 0,
                speed = 0f,
                estimatedTimeRemaining = 0
            )
        }
        downloadServiceState.downloadProgress = initialProgress
        
        // Track unique book names for notification
        val bookNames = downloads.map { it.bookName }.distinct()
        val bookNamesText = when {
            bookNames.isEmpty() -> ""
            bookNames.size == 1 -> bookNames.first()
            bookNames.size <= 3 -> bookNames.joinToString(", ")
            else -> "${bookNames.take(3).joinToString(", ")} +${bookNames.size - 3} more"
        }
        
        // Group downloads by book ID - chapters from same book download sequentially
        val downloadsByBook = downloads.groupBy { it.bookId }
        
        // Semaphore to limit concurrent books (not chapters)
        val semaphore = Semaphore(concurrentLimit)
        var completedCount = 0
        val progressMutex = Mutex()
        
        // Download books concurrently, but chapters within each book sequentially
        coroutineScope {
            downloadsByBook.map { (bookId, bookDownloads) ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        // Download chapters of this book sequentially
                        bookDownloads.forEach { download ->
                            var downloadTries = 0
                            var downloadSuccess = false
                            val maxRetries = 3
                            
                            chapters.find { it.id == download.chapterId }?.let { chapter ->
                                sources.find { it.sourceId == books.find { it.id == chapter.bookId }?.sourceId }
                                    ?.let { source ->
                                        val contentText = chapter.content.joinToString("")
                                        if (contentText.isEmpty() || contentText.length < 50) {
                                            while (downloadTries < maxRetries && !downloadSuccess) {
                                                try {
                                                    downloadTries++
                                                    
                                                    // Update download progress to show it's in progress
                                                    withContext(Dispatchers.Main) {
                                                        progressMutex.withLock {
                                                            val currentProgress = downloadServiceState.downloadProgress[download.chapterId]
                                                            if (currentProgress != null) {
                                                                downloadServiceState.downloadProgress = downloadServiceState.downloadProgress + 
                                                                    (download.chapterId to currentProgress.copy(
                                                                        bytesDownloaded = 0,
                                                                        totalBytes = 100,
                                                                        speed = 0f
                                                                    ))
                                                            }
                                                        }
                                                    }
                                                    
                                                    // Wrap the callback-based suspend function to wait for result
                                                    var downloadedChapter: Chapter? = null
                                                    var downloadError: Exception? = null
                                                    
                                                    remoteUseCases.getRemoteReadingContent(
                                                        chapter = chapter,
                                                        catalog = source,
                                                        onSuccess = { content ->
                                                            downloadedChapter = content
                                                        },
                                                        onError = { message ->
                                                            val errorMsg = message?.asString(localizeHelper) ?: "Download failed"
                                                            downloadError = Exception(errorMsg)
                                                        }
                                                    )
                                                    
                                                    // Check if download was successful
                                                    if (downloadError != null) {
                                                        throw downloadError!!
                                                    }
                                                    
                                                    if (downloadedChapter == null) {
                                                        throw Exception("No content received from source")
                                                    }
                                                    
                                                    val finalChapter = downloadedChapter!!
                                                    
                                                    // Validate chapter content
                                                    val downloadedContent = finalChapter.content.joinToString("")
                                                    if (downloadedContent.isEmpty() || downloadedContent.length < 50) {
                                                        throw Exception("Downloaded content is too short or empty")
                                                    }
                                                    
                                                    // Save the downloaded chapter
                                                    withContext(Dispatchers.IO) {
                                                        insertUseCases.insertChapter(chapter = finalChapter)
                                                    }
                                                    
                                                    // Update progress safely
                                                    progressMutex.withLock {
                                                        completedCount++
                                                        updateTitle("${download.bookName} - ${chapter.name}")
                                                        updateSubtitle("$bookNamesText ($completedCount/${downloads.size})")
                                                        updateProgress(downloads.size, completedCount, false)
                                                        updateNotification(ID_DOWNLOAD_CHAPTER_PROGRESS)
                                                    }
                                                    
                                                    savedDownload = savedDownload.copy(
                                                        bookId = download.bookId,
                                                        priority = 1,
                                                        chapterName = chapter.name,
                                                        chapterKey = chapter.key,
                                                        translator = chapter.translator,
                                                        chapterId = chapter.id,
                                                        bookName = download.bookName,
                                                    )
                                                    
                                                    // Mark as downloaded (priority = 1) only after successful download
                                                    withContext(Dispatchers.IO) {
                                                        downloadUseCases.insertDownload(
                                                            savedDownload.copy(priority = 1).toDownload()
                                                        )
                                                    }
                                                    
                                                    downloadSuccess = true
                                                    
                                                    // Update download progress to completed
                                                    withContext(Dispatchers.Main) {
                                                        progressMutex.withLock {
                                                            val currentProgress = downloadServiceState.downloadProgress[download.chapterId]
                                                            if (currentProgress != null) {
                                                                downloadServiceState.downloadProgress = downloadServiceState.downloadProgress + 
                                                                    (download.chapterId to currentProgress.copy(
                                                                        bytesDownloaded = 100,
                                                                        totalBytes = 100
                                                                    ))
                                                            }
                                                        }
                                                    }
                                                    
                                                    // Add to completed downloads list
                                                    completedDownloadsList.add(
                                                        CompletedDownload(
                                                            chapterId = download.chapterId,
                                                            bookId = download.bookId,
                                                            bookName = download.bookName,
                                                            chapterName = download.chapterName,
                                                            completedAt = System.currentTimeMillis()
                                                        )
                                                    )
                                                    
                                                    // Remove completed chapter from active downloads list (thread-safe, on Main thread)
                                                    withContext(Dispatchers.Main) {
                                                        progressMutex.withLock {
                                                            downloadServiceState.downloads = downloadServiceState.downloads.filter { 
                                                                it.chapterId != download.chapterId 
                                                            }
                                                        }
                                                    }
                                                    
                                                    ireader.core.log.Log.debug { "Successfully downloaded ${download.bookName} chapter ${download.chapterName}" }
                                                    
                                                } catch (e: Exception) {
                                                    ireader.core.log.Log.error { "Download attempt $downloadTries/$maxRetries failed for ${download.chapterName}: ${e.message}" }
                                                    
                                                    // If this was the last retry, mark as failed
                                                    if (downloadTries >= maxRetries) {
                                                        val userFriendlyError = getUserFriendlyErrorMessage(e)
                                                        
                                                        // Add to failed downloads
                                                        val failedDownload = FailedDownload(
                                                            chapterId = download.chapterId,
                                                            errorMessage = userFriendlyError,
                                                            retryCount = downloadTries,
                                                            timestamp = System.currentTimeMillis()
                                                        )
                                                        failedDownloadsList[download.chapterId] = failedDownload
                                                        
                                                        // Update state with failed download
                                                        withContext(Dispatchers.Main) {
                                                            progressMutex.withLock {
                                                                downloadServiceState.failedDownloads = downloadServiceState.failedDownloads + 
                                                                    (download.chapterId to failedDownload)
                                                                
                                                                // Remove from active downloads
                                                                downloadServiceState.downloads = downloadServiceState.downloads.filter { 
                                                                    it.chapterId != download.chapterId 
                                                                }
                                                            }
                                                        }
                                                        
                                                        // Mark as failed in database (priority = 0)
                                                        withContext(Dispatchers.IO) {
                                                            downloadUseCases.insertDownload(
                                                                download.copy(priority = 0).toDownload()
                                                            )
                                                        }
                                                        
                                                        ireader.core.log.Log.error { "Failed to download ${download.chapterName} after $maxRetries attempts: $userFriendlyError" }
                                                    } else {
                                                        // Wait before retrying
                                                        delay(1000L * downloadTries)
                                                    }
                                                }
                                            }
                                        }
                                    }
                            }
                            
                            // Add delay between chapters
                            if (downloadSuccess) {
                                delay(downloadDelayMs)
                            }
                        }
                    }
                }
            }.awaitAll()
        }
        
        // All downloads completed successfully - disable service (on Main thread)
        withContext(Dispatchers.Main) {
            downloadServiceState.downloads = emptyList()
            downloadServiceState.isEnable = false
        }
        
    } catch (e: Throwable) {
        ireader.core.log.Log.error { "Download service error: ${e.message}" }
        notificationManager.cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
        
        val userFriendlyError = getUserFriendlyErrorMessage(e)
        onCancel(Exception(userFriendlyError), savedDownload.bookName)
        
        if (savedDownload.chapterId != 0L) {
            downloadUseCases.insertDownload(savedDownload.copy(priority = 0).toDownload())
        }
        
        // Clear state on error (on Main thread)
        withContext(Dispatchers.Main) {
            downloadServiceState.downloads = emptyList()
            downloadServiceState.isEnable = false
            downloadServiceState.downloadProgress = emptyMap()
        }
        return false
    }

    withContext(Dispatchers.Main) {
        notificationManager.cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
        
        // Update state with failed downloads
        downloadServiceState.failedDownloads = failedDownloadsList
        
        // Call success callback with completed downloads
        onSuccess(completedDownloadsList)
        
        // Clear progress tracking
        downloadServiceState.downloadProgress = emptyMap()
    }
    return true
}

/**
 * Convert technical error messages to user-friendly messages
 */
private fun getUserFriendlyErrorMessage(error: Throwable): String {
    val message = error.message ?: "Unknown error"
    
    return when {
        // Network errors
        message.contains("Unable to resolve host", ignoreCase = true) ||
        message.contains("No address associated with hostname", ignoreCase = true) ||
        message.contains("nodename nor servname provided", ignoreCase = true) ->
            "No internet connection. Please check your network."
        
        message.contains("timeout", ignoreCase = true) ||
        message.contains("timed out", ignoreCase = true) ->
            "Connection timed out. Please try again."
        
        message.contains("connection refused", ignoreCase = true) ||
        message.contains("failed to connect", ignoreCase = true) ->
            "Cannot connect to server. Please try again later."
        
        // Content errors
        message.contains("content is too short", ignoreCase = true) ||
        message.contains("content is empty", ignoreCase = true) ||
        message.contains("no content received", ignoreCase = true) ->
            "Chapter content not available from source."
        
        message.contains("404", ignoreCase = true) ||
        message.contains("not found", ignoreCase = true) ->
            "Chapter not found on source."
        
        // Authentication/Authorization errors
        message.contains("401", ignoreCase = true) ||
        message.contains("unauthorized", ignoreCase = true) ->
            "Authentication required. Please check source settings."
        
        message.contains("403", ignoreCase = true) ||
        message.contains("forbidden", ignoreCase = true) ->
            "Access denied by source."
        
        // Server errors
        message.contains("500", ignoreCase = true) ||
        message.contains("502", ignoreCase = true) ||
        message.contains("503", ignoreCase = true) ||
        message.contains("server error", ignoreCase = true) ->
            "Source server error. Please try again later."
        
        // Rate limiting
        message.contains("429", ignoreCase = true) ||
        message.contains("too many requests", ignoreCase = true) ||
        message.contains("rate limit", ignoreCase = true) ->
            "Too many requests. Please wait and try again."
        
        // Storage errors
        message.contains("no space", ignoreCase = true) ||
        message.contains("storage full", ignoreCase = true) ->
            "Not enough storage space."
        
        message.contains("permission denied", ignoreCase = true) ->
            "Storage permission denied."
        
        // Default fallback
        else -> "Download failed: ${message.take(100)}"
    }
}