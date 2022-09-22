package ireader.ui.video

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.lifecycle.viewModelScope
import ireader.common.models.entities.Chapter
import ireader.core.log.Log
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.presentation.ui.util.NavigationArgs
import ireader.ui.component.Controller
import ireader.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.annotation.KoinViewModel

@OptIn(ExperimentalTextApi::class)
@KoinViewModel
class VideoScreenViewModel(
    val getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases,
    val getChapterUseCase: LocalGetChapterUseCase,
    val remoteUseCases: RemoteUseCases,
    val getLocalCatalog: GetLocalCatalog,
    val insertUseCases: LocalInsertUseCases,
    param: Param
) : BaseViewModel() {
    companion object {
        fun createParam(controller: Controller): VideoScreenViewModel.Param {
            return VideoScreenViewModel.Param(
                controller.navBackStackEntry.arguments?.getLong(NavigationArgs.chapterId.name),
            )
        }
    }

    var chapter by mutableStateOf<Chapter?>(null)

    data class Param(val chapterId: Long?)

    init {
        val chapter = runBlocking {
            getChapterUseCase.findChapterById(param.chapterId)
        }
        val book = runBlocking {
            getBookUseCases.findBookById(chapter?.bookId)
        }
        val source = book?.sourceId?.let { getLocalCatalog.get(it) }
        this@VideoScreenViewModel.chapter = chapter
        viewModelScope.launch {
            if (chapter != null && chapter.content.isEmpty()) {
                remoteUseCases.getRemoteReadingContent(chapter, source, onError = {
                    Log.error(it.toString())
                }, onSuccess = {result ->
                    insertUseCases.insertChapter(result)
                    this@VideoScreenViewModel.chapter = result
                },
                    emptyList()
                )
            }

        }


    }


}