package org.ireader.settings.setting.font_screens

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.FontEntity
import org.ireader.core_ui.preferences.ReaderPreferences
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.use_cases.fonts.FontUseCase
import javax.inject.Inject

@HiltViewModel
class FontScreenViewModel @Inject constructor(
    private val fontScreenState: FontScreenStateImpl,
    private val fontUseCase: FontUseCase,
    val readerPreferences: ReaderPreferences
) : BaseViewModel(),FontScreenState by fontScreenState  {

    val font = readerPreferences.font().asState()

    init {
        setup()
    }



    private fun setup() {
        viewModelScope.launch {
           fontScreenState.fonts = fontUseCase.findAllFontEntities()
            if (fonts.isEmpty()) {
                try {
                    fontScreenState.fonts =  fontUseCase.getRemoteFonts().items.map { font ->
                        FontEntity(
                            fontName = font.family,
                            category = font.category
                        )
                    }
                }catch (e:Throwable) {

                }

                fontUseCase.insertFonts(fontScreenState.fonts)
            }

            snapshotFlow {
                fonts.filteredByQuery(searchQuery)
            }
                .collect {
                    uiFonts = it
                }
        }

    }





    private fun List<FontEntity>.filteredByQuery(query: String?): List<FontEntity> {
        return if (query == null || query.isBlank()) {
            this
        } else {
            filter { it.fontName.contains(query, true) }
        }
    }

}


