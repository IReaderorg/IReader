package ireader.presentation.ui.video

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.*
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.exoplayer.ExoPlayer
import ireader.common.models.entities.Chapter
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.core.source.HttpSource
import ireader.core.source.model.MovieUrl
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.ui.video.component.PlayerCreator
import ireader.presentation.ui.video.component.core.MediaState
import ireader.presentation.ui.video.component.cores.player.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.annotation.KoinViewModel
import java.util.*

@OptIn(ExperimentalTextApi::class)
@KoinViewModel
class VideoScreenViewModel(
        val getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases,
        val getChapterUseCase: LocalGetChapterUseCase,
        val remoteUseCases: RemoteUseCases,
        val getLocalCatalog: GetLocalCatalog,
        val insertUseCases: LocalInsertUseCases,
        val playerCreator: PlayerCreator,
        val savedStateHandle: SavedStateHandle,
        httpClient: HttpClients,
        val simpleStorage: GetSimpleStorage,
        val mediaState: MediaState,
) : BaseViewModel() {


    val chapterId: StateFlow<Long> =
            savedStateHandle.getStateFlow(NavigationArgs.chapterId.name, -1L)

    var player: ExoPlayer? by mutableStateOf<ExoPlayer?>(null)


    var source: HttpSource? by mutableStateOf<HttpSource?>(null)
    var initialize: Boolean? by mutableStateOf(false)


    var chapter by mutableStateOf<Chapter?>(null)
    var currentMovie by mutableStateOf<Int?>(null)

    fun subscribeChapter() {
        viewModelScope.launch {
            getChapterUseCase.subscribeChapterById(chapterId.value, null).collect {
                chapter = it
                setMediaItem()
            }

        }
    }


    init {
        val chapter = runBlocking {
            getChapterUseCase.findChapterById(chapterId.value)
        }
        subscribeChapter()
        val book = runBlocking {
            getBookUseCases.findBookById(chapter?.bookId)
        }
        val catalogLocal = book?.sourceId?.let { getLocalCatalog.get(it) }
        val localSource = catalogLocal?.source
        if (localSource is HttpSource) {
            source = localSource
        }
        player = mediaState.createPlayer(source)
        this@VideoScreenViewModel.chapter = chapter
        viewModelScope.launch {
            if (chapter != null && chapter.content.isEmpty()) {
                remoteUseCases.getRemoteReadingContent(chapter, catalogLocal, onError = {
                    Log.error(it.toString())
                }, onSuccess = { result ->
                    insertUseCases.insertChapter(result)
                    this@VideoScreenViewModel.chapter = result
                },
                        emptyList()
                )
            }
            setMediaItem()
        }
    }

    fun setMediaItem(): List<MediaItem> {
        val chapterDate = chapter?.content?.filterIsInstance<MovieUrl>() ?: emptyList()
        val mediaItems = chapterDate.map {
            MediaItem.Builder().setMediaId(it.url).setUri(it.url).apply {
                mediaState?.playerState?.localSubtitles?.map { subtitle ->
                    SubtitleConfiguration.Builder(Uri.parse(subtitle.url)).build()
                }
            }.build()
        }
        currentMovie = chapterDate.indexOfFirst { it is MovieUrl }
        player?.setMediaItems(mediaItems)

        return mediaItems
    }


    override fun onDestroy() {
        mediaState?.player?.stop()
        mediaState?.player?.release()
        super.onDestroy()
    }


}