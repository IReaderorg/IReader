package ireader.presentation.ui.settings.font_screens

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.domain.usecases.fonts.FontUseCase
import org.koin.android.annotation.KoinViewModel

@OptIn(ExperimentalTextApi::class)
@KoinViewModel
class FontScreenViewModel(
    private val fontScreenState: FontScreenStateImpl,
    private val fontUseCase: FontUseCase,
    val readerPreferences: ReaderPreferences,
) : BaseViewModel(), FontScreenState by fontScreenState {

    val font = readerPreferences.font().asState()
    val previewMode = mutableStateOf(false)

    init {
        setup()
    }

    private fun setup() {
        viewModelScope.launch {
            fontScreenState.fonts = fontUseCase.getRemoteFonts()
            snapshotFlow {
                fonts.filteredByQuery(searchQuery)
            }
                .collect {
                    uiFonts = it
                }
        }
    }

    private fun List<String>.filteredByQuery(query: String?): List<String> {
        return if (query == null || query.isBlank()) {
            this
        } else {
            filter { it.contains(query, true) }
        }
    }
}
