package org.ireader.presentation.feature_updates.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.catalog.interactor.GetLocalCatalog
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.UpdateWithInfo
import org.ireader.domain.repository.UpdatesRepository
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.use_cases.services.ServiceUseCases
import org.ireader.domain.utils.launchIO
import javax.inject.Inject

@HiltViewModel
class UpdatesViewModel @Inject constructor(
    private val updateStateImpl: UpdateStateImpl,
    val updatesRepository: UpdatesRepository,
    private val remoteUseCases: RemoteUseCases,
    private val getLocalCatalog: GetLocalCatalog,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    private val serviceUseCases: ServiceUseCases,
) : BaseViewModel(), UpdateState by updateStateImpl {


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

    /**
     * update chapter using updateitem
     */
    fun updateChapters(onChapter: Chapter.() -> Chapter) {
        val updateIds =
            this.updates.values.flatten().map { it.id }.filter { it in selection }
        val chapterIds = this.updates.values.flatten().filter { it.id in updateIds }
            .map { it.chapterId }
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
    fun downloadChapters() {
        viewModelScope.launch {
            val bookIds =
                updates.values.flatMap { it }.filter { it.id in selection }.map { it.bookId }
            val chapterIds =
                updates.values.flatMap { it }.filter { it.id in selection }.map { it.chapterId }
            //  val books = getBookUseCases.findBookByIds(bookIds)
            serviceUseCases.startDownloadServicesUseCase(
                bookIds = bookIds.toLongArray(),
                chapterIds =  chapterIds.toLongArray()
            )
        }
    }

    fun refreshUpdate() {
       serviceUseCases.startLibraryUpdateServicesUseCase()
    }
}