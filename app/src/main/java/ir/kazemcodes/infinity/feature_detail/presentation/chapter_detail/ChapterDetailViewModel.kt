package ir.kazemcodes.infinity.feature_detail.presentation.chapter_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.data.network.utils.launchIO
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetChapterUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalInsertUseCases
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.feature_activity.presentation.NavigationArgs
import ir.kazemcodes.infinity.feature_sources.sources.Extensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class ChapterDetailViewModel @Inject constructor(
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val savedStateHandle: SavedStateHandle,
    extensions: Extensions
) : ViewModel() {

    private val _state =
        mutableStateOf(ChapterDetailState(source = extensions.mappingSourceNameToSource(0)))
    val state: State<ChapterDetailState> = _state

    private val _chapters = MutableStateFlow<PagingData<Chapter>>(PagingData.empty())
    val chapters = _chapters

    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val bookId = savedStateHandle.get<Int>(NavigationArgs.bookId.name)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                sourceId?.let { _state.value = state.value.copy(source = extensions.mappingSourceNameToSource(it)) }
                bookId?.let { _state.value = state.value.copy(book = state.value.book.copy(id = it)) }
                getLocalChapters()
                getLocalChaptersByPaging()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    sourceId?.let { _state.value = state.value.copy(source = extensions.mappingSourceNameToSource(it)) }
                    bookId?.let { _state.value = state.value.copy(book = state.value.book.copy(id = it)) }
                    getLocalChapters()
                    getLocalChaptersByPaging()
                }
            }
        }
    }

    fun onEvent(event: ChapterDetailEvent) {
        when (event) {
            is ChapterDetailEvent.ToggleOrder -> {
                _state.value = state.value.copy(
                    localChapters = state.value.localChapters.reversed(),
                    isAsc = !state.value.isAsc
                )
                getLocalChaptersByPaging()
            }

        }
    }

    private fun getLocalChaptersByPaging() {
        viewModelScope.launch(Dispatchers.IO) {
            getChapterUseCase.getLocalChaptersByPaging(bookId = state.value.book.id, isAsc = state.value.isAsc)
                .cachedIn(viewModelScope)
                .collect { snapshot ->
                    _chapters.value = snapshot
                }
        }
    }

    private fun getLocalChapters() {
        viewModelScope.launchIO {
            getChapterUseCase.getChaptersByBookId(bookId = state.value.book.id,isAsc =state.value.isAsc)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                _state.value = state.value.copy(
                                    chapters = result.data)
                            }
                        }
                        is Resource.Error -> {

                        }
                    }
                }
        }
    }

    fun getIndexOfChapter(chapter: Chapter): Int {
        val ch = state.value.chapters.indexOf(chapter)
        return if (ch != -1) ch else 0
    }

    override fun onCleared() {
        super.onCleared()
    }

}

