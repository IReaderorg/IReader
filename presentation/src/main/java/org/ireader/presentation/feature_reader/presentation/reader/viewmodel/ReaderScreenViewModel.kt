package org.ireader.presentation.feature_reader.presentation.reader.viewmodel


import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.ireader.core.utils.UiText
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.services.downloaderService.DefaultNotificationHelper
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.history.HistoryUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.ReaderPrefUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.TextReaderPrefUseCase
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.use_cases.services.ServiceUseCases
import org.ireader.presentation.feature_ttl.TTSState
import org.ireader.presentation.feature_ttl.TTSStateImpl
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
    val serviceUseCases: ServiceUseCases,
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
        kotlin.runCatching {
            val readingParagraph =
                savedStateHandle.get<String>(NavigationArgs.readingParagraph.name)
            val voiceMode = savedStateHandle.get<String>(NavigationArgs.voiceMode.name) != "0L"
            state.isReaderModeEnable = true
            if (readingParagraph != null && readingParagraph.toInt() < ttsState.ttsContent?.value?.lastIndex ?: 0) {
                ttsState.currentReadingParagraph = readingParagraph.toInt()
                ttsState.voiceMode = voiceMode
            }
        }

        if (ttsState.ttsChapter != null && ttsState.ttsBook?.id == bookId) {
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


    var getContentJob: Job? = null


    var getChapterJob: Job? = null


    fun runTTSService(context: Context, command: Int = -1) {
        serviceUseCases.startTTSServicesUseCase(
            chapterId = stateChapter?.id,
            bookId = book?.id,
            command = command
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
        ttsState.player?.stop()
        ttsState.isPlaying = false
        ttsState.currentReadingParagraph = 0
    }


}
