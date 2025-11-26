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
import ireader.domain.notification.PlatformNotificationManager
import ireader.i18n.LocalizeHelper
import ireader.i18n.asString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive

/**
 * Main download service function with proper pause/resume support
 */
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
    notificationManager: PlatformNotificationManager,
    updateProgress: (max: Int, current: Int, inProgress: Boolean) -> Unit,
    updateTitle: (String) -> Unit,
    updateSubtitle: (String) -> Unit,
    onCancel: (error: Throwable, bookName: String) -> Unit,
    onSuccess: () -> Unit,
    updateNotification: (Int) -> Unit,
    downloadDelayMs: Long = 1000L,
    concurrentLimit: Int = 1, // Sequential downloads for simplicity
    checkCancellation: () -> Boolean = { false }
): Boolean {
    try {
        // Get chapters to download
        val chapters: List<Chapter> = withContext(Dispatchers.IO) {
            when {
                inputtedBooksIds != null -> {
                    inputtedBooksIds.toList().flatMap { bookId ->
                        chapterRepo.findChaptersByBookId(bookId)
                    }
                }
                inputtedChapterIds != null -> {
                    inputtedChapterIds.toList().mapNotNull { chapterId ->
                        chapterRepo.findChapterById(chapterId)
                    }
                }
                inputtedDownloaderMode -> {
                    val downloads = downloadUseCases.findAllDownloadsUseCase()
                    downloads.mapNotNull { download ->
                        chapterRepo.findChapterById(download.chapterId)
                    }
                }
                else -> {
                    throw Exception("No chapters specified for download")
                }
            }
        }

        if (chapters.isEmpty()) {
            withContext(Dispatchers.Main) {
                downloadServiceState.isRunning = false
                downloadServiceState.isPaused = false
            }
            return true
        }

        // Get books and sources
        val distinctBookIds = chapters.map { it.bookId }.distinct()
        val books = withContext(Dispatchers.IO) {
            distinctBookIds.mapNotNull { bookId -> bookRepo.findBookById(bookId) }
        }
        val distinctSources = books.mapNotNull { it.sourceId }.distinct()
        val sources = extensions.catalogs.filter { it.sourceId in distinctSources }

        // Filter chapters that need downloading
        val downloads = chapters
            .filter { chapter ->
                val contentText = chapter.content.joinToString("")
                contentText.isEmpty() || contentText.length < 50
            }
            .mapNotNull { chapter ->
                val book = books.find { it.id == chapter.bookId }
                book?.let { buildSavedDownload(it, chapter) }
            }

        if (downloads.isEmpty()) {
            withContext(Dispatchers.Main) {
                downloadServiceState.isRunning = false
                downloadServiceState.isPaused = false
            }
            return true
        }

        // Initialize download state
        withContext(Dispatchers.Main) {
            downloadServiceState.downloads = downloads
            downloadServiceState.isRunning = true
            downloadServiceState.isPaused = false
            downloadServiceState.downloadProgress = downloads.associate {
                it.chapterId to DownloadProgress(
                    chapterId = it.chapterId,
                    status = DownloadStatus.QUEUED
                )
            }
        }

        // Insert downloads into database
        downloadUseCases.insertDownloads(downloads.map { it.toDownload() })
        updateProgress(downloads.size, 0, true)
        updateNotification(ID_DOWNLOAD_CHAPTER_PROGRESS)

        var completedCount = 0

        // Download chapters sequentially
        for (download in downloads) {
            // Check if service is still running, coroutine is cancelled, or work is stopped
            if (!downloadServiceState.isRunning || !withContext(Dispatchers.Main) { isActive } || checkCancellation()) {
                break
            }

            // Wait while paused
            while (downloadServiceState.isPaused && downloadServiceState.isRunning) {
                delay(500)
                // Check if cancelled during pause
                if (!withContext(Dispatchers.Main) { isActive } || checkCancellation()) {
                    break
                }
            }

            // Check again after pause
            if (!downloadServiceState.isRunning || !withContext(Dispatchers.Main) { isActive } || checkCancellation()) {
                break
            }

            val chapter = chapters.find { it.id == download.chapterId } ?: continue
            val book = books.find { it.id == chapter.bookId } ?: continue
            val source = sources.find { it.sourceId == book.sourceId } ?: continue

            // Update status to downloading
            withContext(Dispatchers.Main) {
                downloadServiceState.downloadProgress = downloadServiceState.downloadProgress + 
                    (download.chapterId to DownloadProgress(
                        chapterId = download.chapterId,
                        status = DownloadStatus.DOWNLOADING,
                        progress = 0f
                    ))
            }

            updateTitle("${download.bookName} - ${chapter.name}")
            updateSubtitle("${completedCount + 1}/${downloads.size}")
            updateNotification(ID_DOWNLOAD_CHAPTER_PROGRESS)

            // Try downloading with retries
            var success = false
            var lastError: Exception? = null
            val maxRetries = 3

            for (attempt in 1..maxRetries) {
                // Check if still running, cancelled, or work is stopped
                if (!downloadServiceState.isRunning || !withContext(Dispatchers.Main) { isActive } || checkCancellation()) {
                    break
                }

                // Wait while paused
                while (downloadServiceState.isPaused && downloadServiceState.isRunning) {
                    delay(500)
                    // Check if cancelled during pause
                    if (!withContext(Dispatchers.Main) { isActive } || checkCancellation()) {
                        break
                    }
                }

                try {
                    // Download chapter content
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

                    // Check if cancelled immediately after network call
                    if (!downloadServiceState.isRunning || !withContext(Dispatchers.Main) { isActive } || checkCancellation()) {
                        break
                    }

                    // Check result
                    if (downloadError != null) {
                        throw downloadError!!
                    }

                    if (downloadedChapter == null) {
                        throw Exception("No content received from source")
                    }

                    val finalChapter = downloadedChapter!!
                    val downloadedContent = finalChapter.content.joinToString("")
                    
                    if (downloadedContent.isEmpty() || downloadedContent.length < 50) {
                        throw Exception("Downloaded content is too short or empty")
                    }

                    // Save chapter
                    withContext(Dispatchers.IO) {
                        insertUseCases.insertChapter(chapter = finalChapter)
                    }

                    // Mark as completed
                    withContext(Dispatchers.IO) {
                        downloadUseCases.insertDownload(
                            download.copy(priority = 1).toDownload()
                        )
                    }

                    // Update progress
                    completedCount++
                    updateProgress(downloads.size, completedCount, false)
                    updateNotification(ID_DOWNLOAD_CHAPTER_PROGRESS)

                    // Update status to completed
                    withContext(Dispatchers.Main) {
                        downloadServiceState.downloadProgress = downloadServiceState.downloadProgress + 
                            (download.chapterId to DownloadProgress(
                                chapterId = download.chapterId,
                                status = DownloadStatus.COMPLETED,
                                progress = 1f
                            ))
                    }

                    success = true
                    ireader.core.log.Log.debug { "Successfully downloaded ${download.bookName} - ${download.chapterName}" }
                    break

                } catch (e: Exception) {
                    lastError = e
                    ireader.core.log.Log.error { "Download attempt $attempt/$maxRetries failed: ${e.message}" }

                    if (attempt < maxRetries) {
                        // Update retry count
                        withContext(Dispatchers.Main) {
                            downloadServiceState.downloadProgress = downloadServiceState.downloadProgress + 
                                (download.chapterId to DownloadProgress(
                                    chapterId = download.chapterId,
                                    status = DownloadStatus.DOWNLOADING,
                                    progress = 0f,
                                    retryCount = attempt
                                ))
                        }
                        delay(1000L * attempt)
                    }
                }
            }

            // Handle failure
            if (!success) {
                val errorMessage = getUserFriendlyErrorMessage(lastError ?: Exception("Unknown error"))
                
                withContext(Dispatchers.Main) {
                    downloadServiceState.downloadProgress = downloadServiceState.downloadProgress + 
                        (download.chapterId to DownloadProgress(
                            chapterId = download.chapterId,
                            status = DownloadStatus.FAILED,
                            progress = 0f,
                            errorMessage = errorMessage,
                            retryCount = maxRetries
                        ))
                }

                withContext(Dispatchers.IO) {
                    downloadUseCases.insertDownload(
                        download.copy(priority = 0).toDownload()
                    )
                }

                ireader.core.log.Log.error { "Failed to download ${download.chapterName}: $errorMessage" }
            }

            // Delay between downloads
            if (success) {
                delay(downloadDelayMs)
            }
        }

        // Clean up
        withContext(Dispatchers.Main) {
            downloadServiceState.isRunning = false
            downloadServiceState.isPaused = false
        }

        // Check if we were cancelled - if so, don't call onSuccess
        val wasCancelled = checkCancellation()
        
        notificationManager.cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
        
        if (!wasCancelled) {
            onSuccess()
        } else {
            ireader.core.log.Log.info { "Download service was cancelled, skipping onSuccess callback" }
        }
        
        return !wasCancelled

    } catch (e: Throwable) {
        ireader.core.log.Log.error { "Download service error: ${e.message}" }
        
        withContext(Dispatchers.Main) {
            downloadServiceState.isRunning = false
            downloadServiceState.isPaused = false
            downloadServiceState.downloadProgress = emptyMap()
        }

        notificationManager.cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
        onCancel(e, "")
        return false
    }
}

/**
 * Convert technical error messages to user-friendly messages
 */
private fun getUserFriendlyErrorMessage(error: Throwable): String {
    val message = error.message ?: "Unknown error"
    
    return when {
        message.contains("Unable to resolve host", ignoreCase = true) ||
        message.contains("No address associated with hostname", ignoreCase = true) ||
        message.contains("nodename nor servname provided", ignoreCase = true) ->
            "No internet connection"
        
        message.contains("timeout", ignoreCase = true) ||
        message.contains("timed out", ignoreCase = true) ->
            "Connection timed out"
        
        message.contains("connection refused", ignoreCase = true) ||
        message.contains("failed to connect", ignoreCase = true) ->
            "Cannot connect to server"
        
        message.contains("content is too short", ignoreCase = true) ||
        message.contains("content is empty", ignoreCase = true) ||
        message.contains("no content received", ignoreCase = true) ->
            "Chapter content not available"
        
        message.contains("404", ignoreCase = true) ||
        message.contains("not found", ignoreCase = true) ->
            "Chapter not found"
        
        message.contains("401", ignoreCase = true) ||
        message.contains("unauthorized", ignoreCase = true) ->
            "Authentication required"
        
        message.contains("403", ignoreCase = true) ||
        message.contains("forbidden", ignoreCase = true) ->
            "Access denied"
        
        message.contains("500", ignoreCase = true) ||
        message.contains("502", ignoreCase = true) ||
        message.contains("503", ignoreCase = true) ||
        message.contains("server error", ignoreCase = true) ->
            "Source server error"
        
        message.contains("429", ignoreCase = true) ||
        message.contains("too many requests", ignoreCase = true) ||
        message.contains("rate limit", ignoreCase = true) ->
            "Too many requests"
        
        message.contains("no space", ignoreCase = true) ||
        message.contains("storage full", ignoreCase = true) ->
            "Not enough storage space"
        
        message.contains("permission denied", ignoreCase = true) ->
            "Storage permission denied"
        
        else -> "Download failed: ${message.take(100)}"
    }
}
