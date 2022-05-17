package org.ireader.reader.viewmodel

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.ireader.common_extensions.async.nextAfter
import org.ireader.common_extensions.async.prevBefore
import org.ireader.common_extensions.findComponentActivity
import org.ireader.common_models.entities.Chapter
import org.ireader.common_resources.LAST_CHAPTER
import org.ireader.common_resources.NO_VALUE
import org.ireader.common_resources.UiText
import org.ireader.core_catalogs.interactor.GetLocalCatalog
import org.ireader.core_ui.preferences.ReaderPreferences
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.history.HistoryUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.ReaderPrefUseCases
import org.ireader.domain.use_cases.remote.RemoteUseCases
import javax.inject.Inject

@HiltViewModel
class ReaderScreenViewModel @OptIn(ExperimentalTextApi::class)
@Inject constructor(
    val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    val getChapterUseCase: LocalGetChapterUseCase,
    val remoteUseCases: RemoteUseCases,
    val insertUseCases: LocalInsertUseCases,
    val historyUseCase: HistoryUseCase,
    val getLocalCatalog: GetLocalCatalog,
    val readerUseCases: ReaderPrefUseCases,
    val prefState: ReaderScreenPreferencesStateImpl,
    val state: ReaderScreenStateImpl,
    val prefFunc: ReaderPrefFunctionsImpl,
    val savedStateHandle: SavedStateHandle,
    val readerPreferences: ReaderPreferences,
    val googleFontProvider : GoogleFont.Provider
) : BaseViewModel(),
    ReaderScreenPreferencesState by prefState,
    ReaderScreenState by state,
    ReaderPrefFunctions by prefFunc {

    val backgroundColor = readerPreferences.backgroundColorReader().asState()
    val textColor = readerPreferences.textColorReader().asState()
    val lineHeight = readerPreferences.lineHeight().asState()
    val paragraphsIndent = readerPreferences.paragraphIndent().asState()
    val showScrollIndicator = readerPreferences.showScrollIndicator().asState()
    val textAlignment = readerPreferences.textAlign().asState()
    val orientation = readerPreferences.orientation().asState()
    val scrollIndicatorWith = readerPreferences.scrollIndicatorWith().asState()
    val scrollIndicatorPadding = readerPreferences.scrollIndicatorPadding().asState()
    val autoScrollOffset = readerPreferences.autoScrollOffset().asState()
    var autoScrollInterval = readerPreferences.autoScrollInterval().asState()
    val autoBrightnessMode = readerPreferences.autoBrightness().asState()
    val immersiveMode = readerPreferences.immersiveMode().asState()
    val isScrollIndicatorDraggable = readerPreferences.isScrollIndicatorDraggable().asState()
    val font = readerPreferences.font().asState()

    val selectableMode = readerPreferences.selectableText().asState()
    val fontSize = readerPreferences.fontSize().asState()
    val distanceBetweenParagraphs = readerPreferences.paragraphDistance().asState()
    val verticalScrolling = readerPreferences.scrollMode().asState()

    init {

        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val chapterId = savedStateHandle.get<Long>(NavigationArgs.chapterId.name)
        val bookId = savedStateHandle.get<Long>(NavigationArgs.bookId.name)

        if (bookId != null && chapterId != null && sourceId != null) {
            val source = getLocalCatalog.get(sourceId)
            if (source != null) {
                state.catalog = source
                subscribeChapters(bookId)
                viewModelScope.launch {
                    state.book = getBookUseCases.findBookById(bookId)
                    setupChapters(bookId, chapterId)
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

    private suspend fun setupChapters(bookId: Long, chapterId: Long) {
        val last = historyUseCase.findHistoryByBookId(bookId)
        if (chapterId != LAST_CHAPTER && chapterId != NO_VALUE) {
            getLocalChapter(chapterId)
        } else if (last != null) {
            getLocalChapter(chapterId = last.chapterId)
        } else {
            val chapters = getChapterUseCase.findChaptersByBookId(bookId)
            if (chapters.isNotEmpty()) {
                getLocalChapter(chapters.first().id)
            }
        }
    }

    fun getLocalChapter(chapterId: Long?) {
        if (chapterId == null) return
        viewModelScope.launch {
            isLoading = true
            val chapter = getChapterUseCase.findChapterById(chapterId)
            stateChapter = chapter
            if (chapter?.isEmpty() == true) {
                state.source?.let { source -> getRemoteChapter(chapter) }
            }
            stateChapter?.let { ch -> getChapterUseCase.updateLastReadTime(ch) }
            val index = stateChapters.indexOfFirst { it.id == chapter?.id }
            if (index != -1) {
                currentChapterIndex = index
            }

            isLoading = false
            initialized = true

        }
    }

    private suspend fun getRemoteChapter(
        chapter: Chapter,
    ) {
        val catalog = catalog
        remoteUseCases.getRemoteReadingContent(
            chapter,
            catalog,
            onSuccess = { result ->
                state.stateChapter = result
            },
            onError = { message ->
                if (message != null) {
                    showSnackBar(message)
                }
            }
        )
    }

    private fun subscribeChapters(bookId: Long) {
        getChapterJob?.cancel()
        getChapterJob = viewModelScope.launch {
            getChapterUseCase.subscribeChaptersByBookId(
                bookId = bookId,
                isAsc = prefState.isAsc, ""
            )
                .collect {
                    stateChapters = it
                }
        }
    }

    var getContentJob: Job? = null
    var getChapterJob: Job? = null

    fun nextChapter(): Chapter {
        val chapter = stateChapter
        val index = stateChapters.indexOfFirst { it.id == chapter?.id }
        if (index != -1) {
            currentChapterIndex = index
            return stateChapters.nextAfter(index)
                ?: throw IllegalAccessException("List doesn't contains ${chapter?.name}")
        }
        throw IllegalAccessException("List doesn't contains ${chapter?.name}")
    }

    fun prevChapter(): Chapter {
        val chapter = stateChapter
        val index = stateChapters.indexOfFirst { it.id == chapter?.id }
        if (index != -1) {
            currentChapterIndex = index
            return stateChapters.prevBefore(index)
                ?: throw IllegalAccessException("List doesn't contains ${chapter?.name}")
        }
        throw IllegalAccessException("List doesn't contains ${chapter?.name}")
    }

    fun restoreSetting(
        context: Context,
        scrollState: LazyListState,
    ) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            val window = activity.window
            val layoutParams: WindowManager.LayoutParams = window.attributes
            showSystemBars(context = context)
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window.attributes = layoutParams
            stateChapter?.let { chapter ->
                activity.lifecycleScope.launch {
                    insertUseCases.insertChapter(chapter.copy(progress = scrollState.firstVisibleItemIndex))
                }
            }
        }
    }

    fun toggleSettingMode(enable: Boolean, returnToMain: Boolean?) {
        if (returnToMain == null) {
            isSettingModeEnable = enable
            isMainBottomModeEnable = false
        } else {
            isSettingModeEnable = false
            isMainBottomModeEnable = true
        }
    }

    fun bookmarkChapter() {
        stateChapter?.let { chapter ->
            viewModelScope.launch(Dispatchers.IO) {
                stateChapter = chapter.copy(bookmark = !chapter.bookmark)
                insertUseCases.insertChapter(chapter.copy(bookmark = !chapter.bookmark))
            }
        }
    }

    override fun onDestroy() {
        getChapterJob?.cancel()
        getContentJob?.cancel()
        super.onDestroy()
    }
}
