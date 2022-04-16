package org.ireader.presentation.feature_updates.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.catalog.interactor.GetLocalCatalog
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.UpdateWithInfo
import org.ireader.domain.models.entities.UpdateWithInfo.Companion.toUpdate
import org.ireader.domain.repository.UpdatesRepository
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.utils.launchIO
import org.ireader.domain.services.library_update_service.LibraryUpdatesService
import org.ireader.domain.services.library_update_service.LibraryUpdatesService.Companion.LibraryUpdateTag
import org.ireader.domain.services.downloaderService.DownloadService
import javax.inject.Inject

@HiltViewModel
class UpdatesViewModel @Inject constructor(
    private val updateStateImpl: UpdateStateImpl,
    private val updatesRepository: UpdatesRepository,
    private val remoteUseCases: RemoteUseCases,
    private val getLocalCatalog: GetLocalCatalog,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val getBookUseCases: LocalGetBookUseCases,
    private val insertUseCases: LocalInsertUseCases,
) : BaseViewModel(), UpdateState by updateStateImpl {

    lateinit var updateWork: OneTimeWorkRequest

    init {
        viewModelScope.launch {
            updatesRepository.subscribeAllUpdates().collect {
                updates = it
            }
        }

    }

    fun addUpdate(update: UpdateWithInfo) {
        if (update.id in selection) {
            selection.remove(update.id)
        } else {
            selection.add(update.id)
        }
    }

    fun insertChapters(chapterIds: List<Long>, onChapter: Chapter.() -> Chapter) {
        viewModelScope.launch(Dispatchers.IO) {
            val chapters = getChapterUseCase.findChapterByIdByBatch(chapterIds).map(onChapter)
            insertUseCases.insertChapters(chapters)
        }
    }

    fun downloadChapter(update: UpdateWithInfo) {

        viewModelScope.launchIO {
            val source = getLocalCatalog.get(update.sourceId)?.source
            if (source != null) {
                remoteUseCases.getRemoteReadingContent(
                    chapter = Chapter(
                        id = update.chapterId,
                        bookId = update.bookId,
                        link = update.chapterLink,
                        title = update.chapterTitle,
                        read = update.read,
                        number = update.number,
                    ),
                    source = source,
                    onSuccess = {

                    },
                    onError = {

                    }
                )

            }

        }

    }

    lateinit var downloadWork: OneTimeWorkRequest
    fun downloadChapters(context: Context) {
        viewModelScope.launch {
            val bookIds =
                updates.values.flatMap { it }.filter { it.id in selection }.map { it.bookId }
            val chapterIds =
                updates.values.flatMap { it }.filter { it.id in selection }.map { it.chapterId }
            //  val books = getBookUseCases.findBookByIds(bookIds)
            downloadWork =
                OneTimeWorkRequestBuilder<DownloadService>().apply {
                    setInputData(
                        Data.Builder().apply {
                            putLongArray(DownloadService.DOWNLOADER_Chapters_IDS,
                                chapterIds.toLongArray())
                            putLongArray(DownloadService.DOWNLOADER_BOOKS_IDS,
                                bookIds.toLongArray())
                        }.build()
                    )
                    addTag(DownloadService.DOWNLOADER_SERVICE_NAME)
                }.build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                DownloadService.DOWNLOADER_SERVICE_NAME.plus(
                    "UpdateServiceDownloader"),
                ExistingWorkPolicy.REPLACE,
                downloadWork
            )

        }
    }

    fun refreshUpdate(context: Context) {
        updateWork =
            OneTimeWorkRequestBuilder<LibraryUpdatesService>().apply {
                addTag(LibraryUpdateTag)
            }.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            LibraryUpdateTag,
            ExistingWorkPolicy.REPLACE,
            updateWork
        )
    }

    fun deleteUpdate(update: UpdateWithInfo) {
        viewModelScope.launchIO {
            updatesRepository.deleteUpdate(update.toUpdate())

        }
    }

    fun deleteUpdates(update: List<UpdateWithInfo>) {
        viewModelScope.launchIO {
            updatesRepository.deleteUpdates(update.map { it.toUpdate() })

        }
    }

    fun deleteAllUpdates() {
        viewModelScope.launchIO {
            updatesRepository.deleteAllUpdates()

        }
    }
}