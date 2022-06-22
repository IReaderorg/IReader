package org.ireader.settings.setting.font_screens

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.ireader.core_ui.preferences.ReaderPreferences
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.use_cases.fonts.FontUseCase
import javax.inject.Inject

@HiltViewModel
class FontScreenViewModel @OptIn(ExperimentalTextApi::class)
@Inject constructor(
    private val fontScreenState: FontScreenStateImpl,
    private val fontUseCase: FontUseCase,
    val readerPreferences: ReaderPreferences,
    val googleFontProvider: GoogleFont.Provider
) : BaseViewModel(),FontScreenState by fontScreenState  {

    val font = readerPreferences.font().asState()
    val previewMode = mutableStateOf(false)

    init {
        setup()
    }



    private fun setup() {
        viewModelScope.launch {
            fontScreenState.fonts =  fontUseCase.getRemoteFonts()
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


