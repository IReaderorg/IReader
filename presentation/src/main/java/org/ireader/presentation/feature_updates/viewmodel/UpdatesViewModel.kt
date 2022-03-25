package org.ireader.presentation.feature_updates.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.catalog.interactor.GetLocalCatalog
import org.ireader.domain.feature_services.CheckBookUpdatesService
import org.ireader.domain.feature_services.CheckBookUpdatesService.Companion.LibraryUpdateTag
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.Update
import org.ireader.domain.repository.UpdatesRepository
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.utils.launchIO
import javax.inject.Inject

@HiltViewModel
class UpdatesViewModel @Inject constructor(
    private val updateStateImpl: UpdateStateImpl,
    private val updatesRepository: UpdatesRepository,
    private val remoteUseCases: RemoteUseCases,
    private val getLocalCatalog: GetLocalCatalog,
) : BaseViewModel(), UpdateState by updateStateImpl {

    lateinit var work: OneTimeWorkRequest

    init {
        viewModelScope.launch {
            updatesRepository.subscribeAllUpdates().collect {
                updates = it
            }
        }

    }

    fun addUpdate(update: Update) {
        if (update.id in selection) {
            selection.remove(update.id)
        } else {
            selection.add(update.id)
        }
    }

    fun downloadChapter(update: Update) {

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


    fun refreshUpdate(context: Context) {
        work =
            OneTimeWorkRequestBuilder<CheckBookUpdatesService>().apply {
                addTag(LibraryUpdateTag)
            }.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            LibraryUpdateTag,
            ExistingWorkPolicy.REPLACE,
            work
        )
    }
}