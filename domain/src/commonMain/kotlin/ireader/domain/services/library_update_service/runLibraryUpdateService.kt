package ireader.domain.services.library_update_service
import ireader.domain.utils.extensions.ioDispatcher

import ireader.core.log.Log
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.hasStaleSourceOrder
import ireader.domain.models.entities.isSameChapter
import ireader.domain.notification.NotificationsIds
import ireader.domain.notification.NotificationsIds.ID_LIBRARY_PROGRESS
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.notification.PlatformNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
suspend fun runLibraryUpdateService(
    getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases,
    getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase,
    remoteUseCases: RemoteUseCases,
    getLocalCatalog: GetLocalCatalog,
    insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    notificationManager: PlatformNotificationManager,
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
                // A completed book still needs one refresh when legacy downloaded
                // chapters are tied on sourceOrder, otherwise its TOC never matches
                // the website list.
                val needsOrderNormalization = chapters.hasStaleSourceOrder()
                if (book.status == ireader.core.source.model.MangaInfo.COMPLETED &&
                    !forceUpdate && !needsOrderNormalization
                ) {
                    skippedBooks++
                    return@forEachIndexed
                }
                val source = getLocalCatalog.get(book.sourceId)
                if (source != null) {
                    var chaptersToSave = emptyList<Chapter>()
                    remoteUseCases.getRemoteChapters(
                        book, source,
                        onRemoteSuccess = {
                            updateTitle(book.title)
                            updateSubtitle(index.toString())
                            updateProgress(libraryBooks.size, index, false)
                            updateNotification(NotificationsIds.ID_LIBRARY_PROGRESS)
                        },
                        onError = { error ->
                            Log.error { "Failed to update chapters for ${book.title}: $error" }
                        },
                        oldChapters = chapters,
                        onSuccess = { chaptersToSave = it }
                    )

                    // New chapters have no database id yet. Existing ones are still
                    // saved so their source order is refreshed after a source reorders
                    // or renames its chapters.
                    val hasNewChapters = chaptersToSave.any { it.id == 0L }
                    val orderChanged = chaptersToSave.any { saved ->
                        chapters.firstOrNull { it.isSameChapter(saved) }?.sourceOrder != saved.sourceOrder
                    }
                    if (chaptersToSave.isNotEmpty() && (hasNewChapters || orderChanged)) {
                        if (hasNewChapters) updatedBookSize += 1
                        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
                        withContext(ioDispatcher) {
                            insertUseCases.insertChapters(
                                chaptersToSave.map {
                                    it.copy(
                                        bookId = book.id,
                                        dateFetch = if (it.id == 0L) now else it.dateFetch,
                                    )
                                }
                            )
                            if (hasNewChapters) {
                                insertUseCases.updateBook.update(
                                    book.copy(
                                        lastUpdate = now
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            Log.error(e, "getNotifications: Failed to Check for Book Update")
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