package ireader.domain.usecases.services

import ireader.core.log.Log
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.utils.NotificationManager
import ireader.domain.utils.extensions.launchIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

actual class StartLibraryUpdateServicesUseCase(override val di: DI) : DIAware {
    private val getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases by instance()
    private val getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase by instance()
    private val remoteUseCases: RemoteUseCases by instance()
    private val getLocalCatalog: GetLocalCatalog by instance()
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases by instance()
    private val notificationManager: NotificationManager by instance()
    private val workerJob = Job()
    val scope = CoroutineScope(Dispatchers.Main.immediate + workerJob)
    actual fun start(forceUpdate: Boolean) {
        scope.launchIO {
            val libraryBooks = getBookUseCases.findAllInLibraryBooks()
            var skippedBooks = 0


            var updatedBookSize = 0

            try {
                libraryBooks.forEachIndexed { index, book ->
                    val chapters = getChapterUseCase.findChaptersByBookId(bookId = book.id)
                    if (chapters.any { !it.read } && chapters.isNotEmpty() && !forceUpdate) {
                        skippedBooks++
                        return@forEachIndexed
                    }
                    val source = getLocalCatalog.get(book.sourceId)
                    if (source != null) {
                        val remoteChapters = mutableListOf<Chapter>()
                        remoteUseCases.getRemoteChapters(
                            book, source,
                            onRemoteSuccess = {
                                remoteChapters.addAll(it)
                            },
                            onError = {},
                            oldChapters = chapters,
                            onSuccess = {}
                        )

                        val newChapters =
                            remoteChapters.filter { chapter -> chapter.key !in chapters.map { it.key } }

                        if (newChapters.isNotEmpty()) {
                            updatedBookSize += 1
                        }
                        withContext(Dispatchers.IO) {

                            insertUseCases.insertChapters(
                                newChapters.map {
                                    it.copy(
                                        bookId = book.id,
                                        dateFetch = Clock.System.now().toEpochMilliseconds(),
                                    )
                                }
                            )
                            insertUseCases.updateBook.update(
                                book.copy(
                                    lastUpdate = Clock.System.now()
                                        .toEpochMilliseconds()
                                )
                            )
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.error { "getNotifications: Failed to Check for Book Update" }
            }
        }
    }

    actual fun stop() {
        workerJob.cancel()
    }

}