package ireader.presentation.ui.settings.font_screens

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.i18n.UiText
import ireader.presentation.R
import ireader.i18n.resources.MR


interface FontScreenState {
    var isLoading: Boolean
    var fonts: List<String>
    var uiFonts: List<String>
    val isEmpty: Boolean
    var searchedFonts: List<String>
    var error: UiText
    var inSearchMode: Boolean
    var searchQuery: String
}

open class FontScreenStateImpl : FontScreenState {
    override var isLoading by mutableStateOf<Boolean>(false)
    override var fonts by mutableStateOf<List<String>>(emptyList())
    override var uiFonts by mutableStateOf<List<String>>(emptyList())
    override val isEmpty: Boolean by derivedStateOf { fonts.isEmpty() }
    override var searchedFonts by mutableStateOf<List<String>>(emptyList())
    override var error by mutableStateOf<UiText>(UiText.MStringResource(MR.strings.no_error))
    override var inSearchMode by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String>("")
}
