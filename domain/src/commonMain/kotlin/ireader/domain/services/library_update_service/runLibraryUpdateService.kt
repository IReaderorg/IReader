package ireader.domain.services.library_update_service

import ireader.core.log.Log
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.models.entities.Chapter
import ireader.domain.notification.NotificationsIds
import ireader.domain.notification.NotificationsIds.ID_LIBRARY_PROGRESS
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.utils.NotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

suspend fun runLibraryUpdateService(
    getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases,
    getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase,
    remoteUseCases: RemoteUseCases,
    getLocalCatalog: GetLocalCatalog,
    insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    notificationManager: NotificationManager,
    forceUpdate:Boolean,
    updateProgress: (max: Int,progress: Int, inProgress: Boolean) -> Unit,
    updateTitle:(String) -> Unit,
    updateSubtitle:(String) -> Unit,
    updateNotification: (id: Int) -> Unit,
    onSuccess: (bookSize: Int,skippedBook: Int) -> Unit,
    onCancel: (e: Throwable) -> Unit
) : Boolean{

    val libraryBooks = getBookUseCases.findAllInLibraryBooks()
    var skippedBooks = 0
    var updatedBookSize = 0

    updateProgress(libraryBooks.size, 0, false)
    updateNotification(ID_LIBRARY_PROGRESS)
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
                            updateTitle(book.title)
                            updateSubtitle(index.toString())
                            updateProgress(libraryBooks.size, index, false)
                            updateNotification(NotificationsIds.ID_LIBRARY_PROGRESS)
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
            updateProgress(0, 0, false)
            notificationManager.cancel(NotificationsIds.ID_LIBRARY_PROGRESS)
            onCancel(e)
            return false
        }

        updateProgress(0, 0, false)
        notificationManager.cancel(NotificationsIds.ID_LIBRARY_PROGRESS)
        onSuccess(updatedBookSize,skippedBooks)

    return true
}