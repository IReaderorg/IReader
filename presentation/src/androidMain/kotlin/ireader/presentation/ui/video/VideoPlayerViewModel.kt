package ireader.presentation.ui.video

import androidx.compose.runtime.*
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import ireader.domain.models.entities.Chapter
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.core.source.HttpSource
import ireader.core.source.model.MovieUrl
import ireader.core.source.model.Subtitle
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.utils.extensions.withUIContext
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.ui.video.component.PlayerCreator
import ireader.presentation.ui.video.component.core.MediaState
import ireader.presentation.ui.video.component.core.toSubtitleData
import ireader.presentation.ui.video.component.cores.PlayerSubtitleHelper.Companion.toSubtitleMimeType
import ireader.presentation.ui.video.component.cores.SubtitleData
import ireader.presentation.ui.video.component.cores.SubtitleOrigin
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.annotation.KoinViewModel

@OptIn(ExperimentalTextApi::class)

class VideoScreenViewModel(
    val getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases,
    val getChapterUseCase: LocalGetChapterUseCase,
    val remoteUseCases: RemoteUseCases,
    val getLocalCatalog: GetLocalCatalog,
    val insertUseCases: LocalInsertUseCases,
    val playerCreator: PlayerCreator,
    httpClient: HttpClients,
    val simpleStorage: GetSimpleStorage,
    val mediaState: MediaState,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {


    data class Param(
        val chapterId: Long
    )
    val chapterId: State<Long> by mutableStateOf(chapterId)

    var player: State<ExoPlayer?> = derivedStateOf { mediaState.player }


    var source: HttpSource? by mutableStateOf<HttpSource?>(null)
    var initialize: Boolean? by mutableStateOf(false)


    var chapter by mutableStateOf<Chapter?>(null)
    var currentMovie by mutableStateOf<Int?>(null)

    var appUrl by mutableStateOf<String?>(null)

    fun subscribeChapter() {
        scope.launch {
            getChapterUseCase.subscribeChapterById(chapterId.value, null).collect { chapter1 ->
                chapter = chapter1
                chapter1?.content?.let { pages ->
                    val movies = pages.filterIsInstance<MovieUrl>()
                    val subs = pages.filterIsInstance<Subtitle>()
                    mediaState.subtitleHelper.internalSubtitles.value =
                        subs.map { it.toSubtitleData() }.toSet()
                    mediaState.subs = emptyList()
                    mediaState.medias = emptyList()
                    mediaState.subs = subs
                    mediaState.medias = movies
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
        scope.launch {
            if (chapter != null && chapter.content.isEmpty() && catalogLocal != null) {
                getRemoteChapter(chapter, catalogLocal)
            } else {
                loadMedia(chapter)
            }


        }
    }

    suspend fun getRemoteChapter(chapter: Chapter, catalogLocal: CatalogLocal) {
        remoteUseCases.getRemoteReadingContent(chapter, catalogLocal, onError = {
            Log.error(it.toString())
        }, onSuccess = { result ->
            withUIContext {
                insertUseCases.insertChapter(result)
                loadMedia(result)
            }

        },
            emptyList()
        )
    }

    fun loadMedia(chapter: Chapter?) {
        val movieUrl = chapter?.content?.filterIsInstance<MovieUrl>()?.firstOrNull()?.url ?: ""
        chapter?.content?.let {
            val subtitles = it.filterIsInstance<Subtitle>()
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
        mediaState.loadPlayer(false, link = link, data = data, null, emptySet(), null, true)

    }


    override fun onDestroy() {
        mediaState.player?.stop()
        mediaState.player?.release()
        super.onDestroy()
    }


}