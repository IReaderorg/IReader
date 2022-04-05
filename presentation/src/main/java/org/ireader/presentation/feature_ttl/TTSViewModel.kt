//package org.ireader.presentation.feature_ttl
//
//import android.content.Context
//import android.speech.tts.TextToSpeech
//import androidx.lifecycle.viewModelScope
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.launch
//import kotlinx.datetime.Clock
//import org.ireader.core.utils.Constants
//import org.ireader.core.utils.UiText
//import org.ireader.core.utils.currentTimeToLong
//import org.ireader.core.utils.toast
//import org.ireader.core_ui.viewmodel.BaseViewModel
//import org.ireader.domain.R
//import org.ireader.domain.models.entities.Book
//import org.ireader.domain.models.entities.Chapter
//import org.ireader.domain.models.entities.History
//import org.ireader.domain.use_cases.history.HistoryUseCase
//import org.ireader.domain.use_cases.local.LocalGetBookUseCases
//import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
//import org.ireader.domain.use_cases.local.LocalInsertUseCases
//import org.ireader.domain.use_cases.remote.RemoteUseCases
//import org.ireader.domain.utils.withIOContext
//import tachiyomi.source.Source
//import java.util.*
//import javax.inject.Inject
//
//@HiltViewModel
//class TTSViewModel @Inject constructor(
//    private val state: TTLStateImpl,
//    private val LocalGetChapterUseCase: LocalGetBookUseCases,
//    private val getChapterUseCase: LocalGetChapterUseCase,
//    private val remoteUseCases: RemoteUseCases,
//    private val historyUseCase: HistoryUseCase,
//    private val insertUseCases: LocalInsertUseCases
//) : BaseViewModel(), TTLState by state {
//
//    var speaker: TextToSpeech? = null
//    fun readText(context: Context) {
//        speaker = TextToSpeech(context) { status ->
//            if (status != TextToSpeech.ERROR && speaker != null) {
//                speaker?.let { ttl ->
//                    ttl.language = Locale.US
//                    stateChapter?.let { chapter ->
//                        chapter.content.forEach { str ->
//                            ttl.speak(str, TextToSpeech.QUEUE_ADD, null, "")
//                        }
//
//                    }
//                }
//
//
//            } else {
//                context.toast("Not Initialized")
//            }
//        }
//    }
//    private suspend fun getLocalBookById(bookId: Long, chapterId: Long, source: Source) {
//        viewModelScope.launch {
//            val book = getBookUseCases.findBookById(id = bookId)
//            if (book != null) {
//                setStateChapter(book)
//                toggleBookLoaded(true)
//                getLocalChaptersByPaging(bookId)
//                val last = historyUseCase.findHistoryByBookId(bookId)
//                if (chapterId != Constants.LAST_CHAPTER && chapterId != Constants.NO_VALUE) {
//                    getChapter(chapterId, source = source)
//                } else if (last != null) {
//                    getChapter(chapterId = last.chapterId, source = source)
//                } else {
//                    val chapters = getChapterUseCase.findChaptersByBookId(bookId)
//                    if (chapters.isNotEmpty()) {
//                        getChapter(chapters.first().id, source = source)
//                    }
//                }
//                if (stateChapters.isNotEmpty()) {
//                    state.currentChapterIndex =
//                        stateChapters.indexOfFirst { state.stateChapter?.id == it.id }
//
////                    if (stateChapter == null && state.stateChapters.isNotEmpty()) {
////                        getChapter(state.stateChapters.first().id, source = source)
////                    }
//                }
//
//
//            }
//        }
//
//    }
//
//    suspend fun getChapter(
//        chapterId: Long,
//        source: Source,
//    ) {
//        toggleLoading(true)
//        viewModelScope.launch {
//            val resultChapter = getChapterUseCase.findChapterById(
//                chapterId = chapterId,
//                state.book?.id,
//            )
//            if (resultChapter != null) {
//                clearError()
//                setChapter(resultChapter.copy(content = resultChapter.content,
//                    read = true,
//                    readAt = Clock.System.now().toEpochMilliseconds()))
//                val chapter = state.stateChapter
//                if (
//                    chapter != null &&
//                    chapter.content.joinToString()
//                        .isBlank() &&
//                    !state.isLoading
//                ) {
//                    getReadingContentRemotely(chapter = chapter, source = source)
//                }
//                updateLastReadTime(resultChapter)
//            } else {
//                toggleLoading(false)
//
//            }
//        }
//
//    }
//    var getContentJob: Job? = null
//    fun getReadingContentRemotely(chapter: Chapter, source: Source) {
//        clearError()
//        toggleLoading(true)
//        getContentJob?.cancel()
//        getContentJob = viewModelScope.launch(Dispatchers.IO) {
//            remoteUseCases.getRemoteReadingContent(
//                chapter,
//                source = source,
//                onSuccess = { content ->
//                    insertChapter(content.copy(
//                        dateFetch = Clock.System.now()
//                            .toEpochMilliseconds(),
//                    ))
//                    setChapter(content)
//                    toggleLoading(false)
//
//                    clearError()
//                    getChapter(chapter.id, source = source)
//
//                },
//                onError = { message ->
//                    toggleLoading(false)
//                    if (message != null) {
//                        showSnackBar(message)
//                    }
//                }
//            )
//        }
//
//
//    }
//
//
//    override fun onDestroy() {
//        speaker?.shutdown()
//        super.onDestroy()
//    }
//    var getChapterJob: Job? = null
//    fun getLocalChaptersByPaging(bookId: Long) {
//        getChapterJob?.cancel()
//        getChapterJob = viewModelScope.launch {
//            getChapterUseCase.subscribeChaptersByBookId(bookId = bookId,
//                isAsc = prefState.isAsc, "")
//                .collect {
//                    stateChapters = it
//                }
//        }
//
//    }
//
//    private suspend fun insertChapter(chapter: Chapter) {
//        withIOContext {
//            insertUseCases.insertChapter(chapter)
//        }
//    }
//    private fun updateLastReadTime(chapter: Chapter) {
//        viewModelScope.launch(Dispatchers.IO) {
//            insertUseCases.insertChapter(
//                chapter = chapter.copy(read = true,
//                    readAt = Clock.System.now().toEpochMilliseconds())
//            )
//            historyUseCase.insertHistory(History(
//                bookId = chapter.bookId,
//                chapterId = chapter.id,
//                readAt = currentTimeToLong()))
//        }
//
//    }
//
//    private fun setChapter(chapter: Chapter) {
//        state.stateChapter = chapter
//    }
//
//    private fun toggleLoading(isLoading: Boolean) {
//        state.isLoading = isLoading
//    }
//
//    private fun setStateChapter(book: Book) {
//        state.book = book
//    }
//
//    private fun clearError() {
//        state.error = UiText.StringResource(R.string.no_error)
//    }
//
//}