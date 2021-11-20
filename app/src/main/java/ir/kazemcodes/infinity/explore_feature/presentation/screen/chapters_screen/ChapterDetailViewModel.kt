package ir.kazemcodes.infinity.explore_feature.presentation.screen.chapters_screen

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.base_feature.util.Constants.TAG

import ir.kazemcodes.infinity.explore_feature.data.model.Chapter

import ir.kazemcodes.infinity.explore_feature.domain.use_case.GetChaptersUseCase
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.Constants
import javax.inject.Inject


@HiltViewModel
class ChapterDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChaptersUseCase: GetChaptersUseCase
) : ViewModel() {

    private val _state = mutableStateOf<ChapterDetailState>(ChapterDetailState())
    val state: State<ChapterDetailState> = _state

    init {
        savedStateHandle.get<List<Chapter>>(Constants.PARAM_CHAPTERS_DETAIL)?.let { data ->
            val chapters = Gson().toJson(data)
            Log.d(TAG, "chapter: $data")
        }
    }
}

data class ChapterDetailState (
    val isLoading : Boolean = false,
    val chapters : List<Chapter> = emptyList(),
    val error: String = ""
)