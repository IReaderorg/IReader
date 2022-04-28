package org.ireader.reader.viewmodel

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ireader.common_extensions.findComponentActivity
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.domain.R
import javax.inject.Inject

interface ReaderUiFunctions {
    fun ReaderScreenViewModel.toggleLocalLoaded(loaded: Boolean)
    fun ReaderScreenViewModel.toggleBookLoaded(loaded: Boolean)
    fun ReaderScreenViewModel.setChapter(chapter: Chapter)
    fun ReaderScreenViewModel.toggleIsAsc(isAsc: Boolean)
    fun ReaderScreenViewModel.clearError()
    fun ReaderScreenViewModel.setStateChapter(book: Book)
    fun ReaderScreenViewModel.toggleRemoteLoading(isLoading: Boolean)
    fun ReaderScreenViewModel.toggleLoading(isLoading: Boolean)
    fun ReaderScreenViewModel.restoreSetting(context: Context, scrollState: LazyListState)
    fun ReaderScreenViewModel.toggleSettingMode(enable: Boolean, returnToMain: Boolean? = null)
    fun ReaderScreenViewModel.reverseChapters()
    fun ReaderScreenViewModel.bookmarkChapter()
}

class ReaderUiFunctionsImpl @Inject constructor() : ReaderUiFunctions {
    override fun ReaderScreenViewModel.toggleLocalLoaded(loaded: Boolean) {
        state.isLocalLoaded = loaded
    }

    override fun ReaderScreenViewModel.toggleBookLoaded(loaded: Boolean) {
        state.isBookLoaded = loaded
    }

    override fun ReaderScreenViewModel.setChapter(chapter: Chapter) {
        state.stateChapter = chapter
    }

    override fun ReaderScreenViewModel.toggleIsAsc(isAsc: Boolean) {
        this.isAsc = isAsc
    }

    override fun ReaderScreenViewModel.clearError() {
        state.error = org.ireader.common_extensions.UiText.StringResource(R.string.no_error)
    }

    override fun ReaderScreenViewModel.setStateChapter(book: Book) {
        state.book = book
    }

    override fun ReaderScreenViewModel.toggleRemoteLoading(isLoading: Boolean) {
        state.isRemoteLoading = isLoading
    }

    override fun ReaderScreenViewModel.toggleLoading(isLoading: Boolean) {
        state.isLoading = isLoading
    }

    override fun ReaderScreenViewModel.restoreSetting(
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
                    insertChapter(chapter.copy(progress = scrollState.firstVisibleItemScrollOffset))
                }
            }
        }
    }

    override fun ReaderScreenViewModel.toggleSettingMode(enable: Boolean, returnToMain: Boolean?) {
        if (returnToMain == null) {
            isSettingModeEnable = enable
            isMainBottomModeEnable = false
        } else {
            isSettingModeEnable = false
            isMainBottomModeEnable = true
        }
    }

    override fun ReaderScreenViewModel.reverseChapters() {
        toggleIsAsc(!isAsc)
    }

    override fun ReaderScreenViewModel.bookmarkChapter() {
        stateChapter?.let { chapter ->
            viewModelScope.launch(Dispatchers.IO) {
                stateChapter = chapter.copy(bookmark = !chapter.bookmark)
                insertUseCases.insertChapter(chapter.copy(bookmark = !chapter.bookmark))
            }
        }
    }
}
