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
    onSuccess: () -> Unit,
    updateNotification: (Int) -> Unit,
    downloadDelayMs: Long = 1000L,
    concurrentLimit: Int = 3
) : Boolean {
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
                            
                            chapters.find { it.id == download.chapterId }?.let { chapter ->
                                sources.find { it.sourceId == books.find { it.id == chapter.bookId }?.sourceId }
                                    ?.let { source ->
                                        val contentText = chapter.content.joinToString("")
                                        if (contentText.isEmpty() || contentText.length < 50) {
                                            try {
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
                                                        downloadError = Exception(message?.asString(localizeHelper) ?: "Download failed")
                                                    }
                                                )
                                                
                                                // Check if download was successful
                                                if (downloadError != null) {
                                                    throw downloadError!!
                                                }
                                                
                                                if (downloadedChapter == null) {
                                                    throw Exception("Download failed: no content received")
                                                }
                                                
                                                val finalChapter = downloadedChapter!!
                                                
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
                                                
                                                // Remove completed chapter from active downloads list (thread-safe, on Main thread)
                                                withContext(Dispatchers.Main) {
                                                    progressMutex.withLock {
                                                        downloadServiceState.downloads = downloadServiceState.downloads.filter { 
                                                            it.chapterId != download.chapterId 
                                                        }
                                                    }
                                                }
                                                
                                                ireader.core.log.Log.debug { "getNotifications: Successfully downloaded ${download.bookName} chapter ${download.chapterName}" }
                                                
                                            } catch (e: Exception) {
                                                downloadTries++
                                                ireader.core.log.Log.error { "Download attempt $downloadTries failed: ${e.message}" }
                                                if (downloadTries > 3) {
                                                    throw e
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
        ireader.core.log.Log.error { "getNotifications: Failed to download ${savedDownload.chapterName}" }
        notificationManager.cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
        onCancel(e,savedDownload.bookName)
        downloadUseCases.insertDownload(savedDownload.copy(priority = 0).toDownload())
        
        // Clear state on error (on Main thread)
        withContext(Dispatchers.Main) {
            downloadServiceState.downloads = emptyList()
            downloadServiceState.isEnable = false
        }
        return false
    }

    withContext(Dispatchers.Main) {
        notificationManager.cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
        onSuccess()
    }
    return true
}