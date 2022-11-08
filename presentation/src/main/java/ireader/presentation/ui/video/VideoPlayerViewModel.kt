package ireader.presentation.ui.video

import androidx.compose.runtime.*
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import ireader.common.models.entities.Chapter
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.core.source.HttpSource
import ireader.core.source.model.MovieUrl
import ireader.core.source.model.Subtitles
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.ui.video.component.PlayerCreator
import ireader.presentation.ui.video.component.core.MediaState
import ireader.presentation.ui.video.component.core.toSubtitleData
import ireader.presentation.ui.video.component.cores.PlayerSubtitleHelper.Companion.toSubtitleMimeType
import ireader.presentation.ui.video.component.cores.SubtitleData
import ireader.presentation.ui.video.component.cores.SubtitleOrigin
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

    var player: State<ExoPlayer?> = derivedStateOf { mediaState.player }


    var source: HttpSource? by mutableStateOf<HttpSource?>(null)
    var initialize: Boolean? by mutableStateOf(false)


    var chapter by mutableStateOf<Chapter?>(null)
    var currentMovie by mutableStateOf<Int?>(null)

    fun subscribeChapter() {
        viewModelScope.launch {
            getChapterUseCase.subscribeChapterById(chapterId.value, null).collect { chapter1 ->
                chapter = chapter1
                chapter1?.content?.let { pages ->
                    val movies = pages.filterIsInstance<MovieUrl>()
                    val subs = pages.filterIsInstance<Subtitles>()
                    mediaState.subtitleHelper.internalSubtitles.value = subs.map { it.toSubtitleData() }.toSet()
                    mediaState.subs = emptyList()
                    mediaState.medias = emptyList()
                    mediaState.subs =subs
                    mediaState.medias =movies

                }
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
        val movieUrl = chapter?.content?.filterIsInstance<MovieUrl>()?.firstOrNull()?.url ?: ""
        chapter?.content?.let {
            val subtitles = it.filterIsInstance<Subtitles>()
            mediaState.subtitleHelper.setActiveSubtitles(subtitles.map { sub ->
                SubtitleData(
                    name = sub.url.substringBeforeLast(".").substringAfterLast("/"),
                    url = sub.url,
                    SubtitleOrigin.URL,
                    sub.url.toSubtitleMimeType(),
                    emptyMap()
                )
            })
            mediaState.subtitleHelper.setAllSubtitles(subtitles.map { sub ->
                SubtitleData(
                    name = sub.url.substringBeforeLast(".").substringAfterLast("/"),
                    url = sub.url,
                    SubtitleOrigin.URL,
                    sub.url.toSubtitleMimeType(),
                    emptyMap()
                )
            })
        }
        val link = if (movieUrl.contains("http")) movieUrl else null
        val data = if (link == null) movieUrl else null
        mediaState.loadPlayer(false, link = link,data = data,null, emptySet(),null,true)
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
        }
    }

//    fun setMediaItem(): List<MediaItem> {
//        val movies = chapter?.content?.filterIsInstance<MovieUrl>() ?: emptyList()
//        val subtitles = chapter?.content?.filterIsInstance<Subtitles>() ?: emptyList()
//        val mediaItems = movies.map {
//            MediaItem.Builder().setMediaId(it.url).setUri(it.url).let { mediaItem ->
//                val subs = mutableListOf<SubtitleConfiguration>()
//                mediaState.subtitleHelper.allSubtitles.map { subtitle ->
//                    subs.add(
//                        SubtitleConfiguration.Builder(Uri.parse(subtitle.url)).setLabel("Local")
//                            .setLanguage("En").setMimeType(MimeTypes.APPLICATION_SUBRIP)
//                            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT).build()
//                    )
//                }
//                subtitles.map { subtitle ->
//                    subs.add(
//                        SubtitleConfiguration.Builder(Uri.parse(subtitle.url)).setLabel("Local")
//                            .setLanguage("En").setMimeType(MimeTypes.APPLICATION_SUBRIP)
//                            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT).build()
//                    )
//                }
//                mediaItem.setSubtitleConfigurations(subs.toImmutableList()).build()
//            }
//        }
//        currentMovie = movies.indexOfFirst { it is MovieUrl }
//        player?.setMediaItems(mediaItems)
//
//        return mediaItems
//    }


    override fun onDestroy() {
        mediaState?.player?.stop()
        mediaState?.player?.release()
        super.onDestroy()
    }


}