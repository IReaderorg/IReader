package org.ireader.presentation.feature_reader.presentation.reader.viewmodel


import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.ireader.core.R
import org.ireader.core.utils.UiText
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.feature_services.notification.DefaultNotificationHelper
import org.ireader.domain.feature_services.notification.NotificationStates
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.history.HistoryUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.ReaderPrefUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.TextReaderPrefUseCase
import org.ireader.domain.use_cases.remote.RemoteUseCases
import tachiyomi.source.Source
import javax.inject.Inject


@HiltViewModel
class ReaderScreenViewModel @Inject constructor(
    val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    val getChapterUseCase: LocalGetChapterUseCase,
    val remoteUseCases: RemoteUseCases,
    val insertUseCases: LocalInsertUseCases,
    val historyUseCase: HistoryUseCase,
    private val catalogStore: CatalogStore,
    val readerUseCases: ReaderPrefUseCases,
    val prefState: ReaderScreenPreferencesStateImpl,
    val state: ReaderScreenStateImpl,
    val textReaderManager: TextReaderManager,
    val speakerState: TextReaderScreenStateImpl,
    val prefFunc: ReaderPrefFunctionsImpl,
    val uiFunc: ReaderUiFunctionsImpl,
    val mainFunc: ReaderMainFunctionsImpl,
    val speechPrefUseCases: TextReaderPrefUseCase,
    val savedStateHandle: SavedStateHandle,
    val defaultNotificationHelper: DefaultNotificationHelper,
    val notificationStates: NotificationStates,
) : BaseViewModel(),
    ReaderScreenPreferencesState by prefState,
    ReaderScreenState by state,
    ReaderPrefFunctions by prefFunc,
    ReaderUiFunctions by uiFunc,
    ReaderMainFunctions by mainFunc,
    TextReaderScreenState by speakerState {


    fun mediaSessionCompat(context: Context) =
        MediaSessionCompat(context, "mediaPlayer", null, null)

    init {

        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val chapterId = savedStateHandle.get<Long>(NavigationArgs.chapterId.name)
        val bookId = savedStateHandle.get<Long>(NavigationArgs.bookId.name)
        if (bookId != null && chapterId != null && sourceId != null) {
            val source = catalogStore.get(sourceId)?.source
            if (source != null) {
                this.source = source
                viewModelScope.launch {
                    getLocalBookById(bookId, chapterId, source = source)
                    readPreferences()
                }



            } else {
                viewModelScope.launch {
                    showSnackBar(UiText.StringResource(org.ireader.core.R.string.the_source_is_not_found))
                }
            }
        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(org.ireader.core.R.string.something_is_wrong_with_this_book))
            }
        }
    }


    fun onEvent(event: ReaderEvent) {
        when (event) {
            is ReaderEvent.ChangeBrightness -> {
                saveBrightness(event.brightness, event.context)
            }
            is ReaderEvent.ChangeFontSize -> {
                saveFontSize(event.fontSizeEvent)
            }
            is ReaderEvent.ChangeFont -> {
                saveFont(event.index)
            }
            is ReaderEvent.ToggleReaderMode -> {
                toggleReaderMode(event.enable)
            }
            else -> {}
        }
    }

    var getContentJob: Job? = null


    var getChapterJob: Job? = null




    override fun onDestroy() {
        enable = false
        getChapterJob?.cancel()
        getContentJob?.cancel()
        speaker?.shutdown()
        super.onDestroy()

    }


    fun onNextVoice() {
        speaker?.stop()
        isPlaying = false
        currentReadingParagraph = 0
    }

    fun onNext(
        scrollState: LazyListState,
        chapters: List<Chapter>,
        source: Source,
        currentIndex: Int,
    ) {
        if (currentIndex < chapters.lastIndex) {
            uiFunc.apply {
                updateChapterSliderIndex(currentIndex + 1)
            }
            mainFunc.apply {
                uiFunc.apply {
                    scope.launch {
                        getChapter(getCurrentChapterByIndex().id,
                            source = source)
                        scrollState.animateScrollToItem(0, 0)
                    }
                }
            }

        } else {
            scope.launch {
                showSnackBar(UiText.StringResource(R.string.this_is_last_chapter))

            }
        }
    }


}
