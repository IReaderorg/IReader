package ireader.domain.usecases.services

import ireader.core.util.createICoroutineScope
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.SavedDownload
import ireader.domain.models.entities.buildSavedDownload
import ireader.domain.services.downloaderService.DownloadServiceStateImpl
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.utils.NotificationManager
import ireader.domain.utils.extensions.launchIO
import ireader.domain.utils.mapNotNull
import ireader.i18n.LocalizeHelper
import ireader.i18n.asString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

actual class StartDownloadServicesUseCase(override val di: DI) : DIAware  {

    private val bookRepo: BookRepository by instance()
    private val chapterRepo: ChapterRepository by instance()
    private val remoteUseCases: RemoteUseCases by instance()
    private val localizeHelper: LocalizeHelper by instance()
    private val extensions: CatalogStore by instance()
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases by instance()
    private val downloadUseCases: DownloadUseCases by instance()
    private val downloadServiceState: DownloadServiceStateImpl by instance()

    val workerJob= Job()
    private val notificationManager : NotificationManager by instance()
    val scope = createICoroutineScope(Dispatchers.Main.immediate + workerJob)
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
    actual fun start(bookIds: LongArray?, chapterIds: LongArray?, downloadModes: Boolean) {
        scope.launchIO {
            try {
                val inputtedChapterIds = chapterIds
                val inputtedBooksIds = bookIds
                val inputtedDownloaderMode = downloadModes

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
                downloadUseCases.insertDownload(savedDownload.copy(priority = 0).toDownload())
                downloadServiceState.downloads = emptyList()
                downloadServiceState.isEnable = false
            }

            withContext(Dispatchers.Main) {

                withContext(Dispatchers.IO) {
                    downloadUseCases.insertDownload(savedDownload.copy(priority = 0).toDownload())
                }
            }
            downloadServiceState.downloads = emptyList()
            downloadServiceState.isEnable = false
        }
        }

    actual fun stop() {
        workerJob.cancel()
    }

}