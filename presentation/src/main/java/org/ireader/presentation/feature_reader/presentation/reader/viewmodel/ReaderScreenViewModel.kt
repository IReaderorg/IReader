package org.ireader.presentation.feature_reader.presentation.reader.viewmodel


import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.ireader.core.R
import org.ireader.core.utils.UiText
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.history.HistoryUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.ReaderPrefUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.TextReaderPrefUseCase
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.presentation.feature_services.notification.DefaultNotificationHelper
import org.ireader.presentation.feature_ttl.TTSService
import org.ireader.presentation.feature_ttl.TTSService.Companion.COMMAND
import org.ireader.presentation.feature_ttl.TTSService.Companion.TTS_BOOK_ID
import org.ireader.presentation.feature_ttl.TTSService.Companion.TTS_Chapter_ID
import org.ireader.presentation.feature_ttl.TTSService.Companion.ttsWork
import org.ireader.presentation.feature_ttl.TTSState
import org.ireader.presentation.feature_ttl.TTSStateImpl
import org.ireader.core_api.source.Source
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
    val prefFunc: ReaderPrefFunctionsImpl,
    val uiFunc: ReaderUiFunctionsImpl,
    val mainFunc: ReaderMainFunctionsImpl,
    val speechPrefUseCases: TextReaderPrefUseCase,
    val savedStateHandle: SavedStateHandle,
    val defaultNotificationHelper: DefaultNotificationHelper,
    val ttsState: TTSStateImpl,
) : BaseViewModel(),
    ReaderScreenPreferencesState by prefState,
    ReaderScreenState by state,
    ReaderPrefFunctions by prefFunc,
    ReaderUiFunctions by uiFunc,
    ReaderMainFunctions by mainFunc,
    TTSState by ttsState {



    init {

        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val chapterId = savedStateHandle.get<Long>(NavigationArgs.chapterId.name)
        val bookId = savedStateHandle.get<Long>(NavigationArgs.bookId.name)
        state.isReaderModeEnable = true
        if (ttsState.ttsChapter != null && ttsState.ttsBook?.id == bookId) {
            //ttsState.voiceMode = true
            state.stateChapter = ttsChapter
            state.book = ttsBook
            state.stateChapters = ttsChapters
            isLocalLoaded = true
            stateChapter?.let { updateLastReadTime(it) }
        } else {
            if (bookId != null && chapterId != null && sourceId != null) {
                val source = catalogStore.get(sourceId)?.source
                if (source != null) {
                    ttsState.ttsSource = source
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


    fun runTTSService(context: Context, command: Int = -1) {
        ttsWork =
            OneTimeWorkRequestBuilder<TTSService>().apply {
                stateChapter?.let { chapter ->
                    book?.let { book ->
                        setInputData(
                            Data.Builder().apply {
                                putLong(TTS_Chapter_ID, chapter.id)
                                putLong(TTS_BOOK_ID, book.id)
                                putInt(COMMAND, command)
                            }.build()
                        )

                    }
                }
                addTag(TTSService.TTS_SERVICE_NAME)

            }.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            TTSService.TTS_SERVICE_NAME,
            ExistingWorkPolicy.REPLACE,
            ttsWork
        )
    }

    override fun onDestroy() {
        enable = false
        getChapterJob?.cancel()
        getContentJob?.cancel()
        // ttsStateImpl.tts.shutdown()
        super.onDestroy()

    }


    fun onNextVoice() {
        ttsState.tts?.stop()
        ttsState.isPlaying = false
        ttsState.currentReadingParagraph = 0
    }


    fun onNext(
        scrollState: LazyListState,
        chapters: List<Chapter>,
        source: Source,
        currentIndex: Int,
    ) {
        if (currentIndex < chapters.lastIndex) {
            uiFunc.apply {
                updateChapterSliderIndex(currentIndex, true)
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
