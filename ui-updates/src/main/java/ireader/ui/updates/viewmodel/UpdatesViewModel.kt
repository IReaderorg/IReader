package ireader.ui.updates.viewmodel

import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ireader.common.models.entities.Chapter
import ireader.common.models.entities.UpdateWithInfo
import ireader.core.catalogs.interactor.GetLocalCatalog
import ireader.core.ui.preferences.UiPreferences
import ireader.core.ui.viewmodel.BaseViewModel
import ireader.domain.use_cases.local.LocalGetChapterUseCase
import ireader.domain.use_cases.local.LocalInsertUseCases
import ireader.domain.use_cases.remote.RemoteUseCases
import ireader.domain.use_cases.services.ServiceUseCases
import ireader.domain.use_cases.updates.UpdateUseCases

import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class UpdatesViewModel(
    private val updateStateImpl: UpdateStateImpl,
    val updateUseCases: UpdateUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    private val serviceUseCases: ServiceUseCases,
    private val uiPreferences: UiPreferences
) : BaseViewModel(), UpdateState by updateStateImpl {

    val relativeFormat by uiPreferences.relativeTime().asState()
    init {
        viewModelScope.launch {
            updateUseCases.subscribeUpdates().collect {
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

            val chapters = chapterIds.mapNotNull {
                getChapterUseCase.findChapterById(it)
            }.map(onChapter)
            insertUseCases.insertChapters(chapters)
        }
    }

    fun downloadChapters() {
        viewModelScope.launch {
            val chapterIds =
                updates.values.flatMap { it }.filter { it.id in selection }.map { it.chapterId }
            serviceUseCases.startDownloadServicesUseCase(
                chapterIds = chapterIds.toLongArray()
            )
        }
    }
    fun refreshUpdate() {
        serviceUseCases.startLibraryUpdateServicesUseCase()
    }
}
