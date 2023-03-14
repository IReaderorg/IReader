package ireader.presentation.ui.settings.font_screens

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.ExperimentalTextApi
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.fonts.FontUseCase
import kotlinx.coroutines.launch


@OptIn(ExperimentalTextApi::class)

class FontScreenViewModel(
        private val fontScreenState: FontScreenStateImpl,
        private val fontUseCase: FontUseCase,
        val readerPreferences: ReaderPreferences,
        val androidUiPreferences: PlatformUiPreferences,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), FontScreenState by fontScreenState {

    val font = androidUiPreferences.font()?.asState()
    val previewMode = mutableStateOf(false)

    init {
        setup()
    }

    private fun setup() {
        scope.launch {
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
