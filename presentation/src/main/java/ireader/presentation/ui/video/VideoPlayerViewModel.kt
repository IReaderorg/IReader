package ireader.presentation.ui.video

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import ireader.common.models.entities.Chapter
import ireader.core.log.Log
import ireader.core.source.findInstance
import ireader.core.source.model.MovieUrl
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.ui.video.component.PlayerCreator
import kotlinx.coroutines.flow.StateFlow
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
    private val playerCreator: PlayerCreator,
    val savedStateHandle: SavedStateHandle,
) : BaseViewModel() {
    val chapterId : StateFlow<Long> = savedStateHandle.getStateFlow(NavigationArgs.chapterId.name, -1L)

    lateinit var player: Player


    var chapter by mutableStateOf<Chapter?>(null)
    val videoUri : State<String?> =  derivedStateOf { chapter?.content?.findInstance<MovieUrl>()?.url }


    val mediaItem = derivedStateOf { videoUri.value?.let { MediaItem.Builder().setMediaId(it).setUri(videoUri.value).build() } }


    init {
        val chapter = runBlocking {
            getChapterUseCase.findChapterById(chapterId.value)
        }
        val book = runBlocking {
            getBookUseCases.findBookById(chapter?.bookId)
        }
        player  = playerCreator.init()
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