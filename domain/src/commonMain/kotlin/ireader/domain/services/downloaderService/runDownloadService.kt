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
    updateNotification: (Int) -> Unit
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
            chapters.filter { it.content.joinToString().length < 10 }
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
        downloads.forEachIndexed { index, download ->
            chapters.find { it.id == download.chapterId }?.let { chapter ->
                sources.find { it.sourceId == books.find { it.id == chapter.bookId }?.sourceId }
                    ?.let { source ->
                        if (chapter.content.joinToString().length < 10) {
                            remoteUseCases.getRemoteReadingContent(
                                chapter = chapter,
                                catalog = source,
                                onSuccess = { content ->
                                    withContext(Dispatchers.IO) {
                                        insertUseCases.insertChapter(chapter = content)
                                    }
                                    updateTitle(chapter.name)
                                    updateSubtitle(index.toString())
                                    updateProgress(downloads.size, index, false)
                                    updateNotification(ID_DOWNLOAD_CHAPTER_PROGRESS)
                                    savedDownload = savedDownload.copy(
                                        bookId = download.bookId,
                                        priority = 1,
                                        chapterName = chapter.name,
                                        chapterKey = chapter.key,
                                        translator = chapter.translator,
                                        chapterId = chapter.id,
                                        bookName = download.bookName,
                                    )
                                    withContext(Dispatchers.IO) {
                                        downloadUseCases.insertDownload(
                                            savedDownload.copy(
                                                priority = 1
                                            ).toDownload()
                                        )
                                    }
                                    tries = 0
                                },
                                onError = { message ->
                                    tries++
                                    if (tries > 3) {
                                        throw Exception(message?.asString(localizeHelper))
                                    }

                                }
                            )
                        }
                    }
            }
            ireader.core.log.Log.debug { "getNotifications: Successfully to downloaded ${savedDownload.bookName} chapter ${savedDownload.chapterName}" }
            delay(1000)
        }
    } catch (e: Throwable) {
        ireader.core.log.Log.error { "getNotifications: Failed to download ${savedDownload.chapterName}" }
        notificationManager.cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
        onCancel(e,savedDownload.bookName)
        downloadUseCases.insertDownload(savedDownload.copy(priority = 0).toDownload())
        downloadServiceState.downloads = emptyList()
        downloadServiceState.isEnable = false
        return false
    }

    withContext(Dispatchers.Main) {
        notificationManager.cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
        withContext(Dispatchers.IO) {
            downloadUseCases.insertDownload(savedDownload.copy(priority = 0).toDownload())
        }
        onSuccess()
    }
    downloadServiceState.downloads = emptyList()
    downloadServiceState.isEnable = false
    return true
}