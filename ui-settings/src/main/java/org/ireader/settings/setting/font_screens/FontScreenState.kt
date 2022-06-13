package org.ireader.settings.setting.font_screens

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ireader.common_resources.UiText
import javax.inject.Inject

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

open class FontScreenStateImpl @Inject constructor() : FontScreenState {
    override var isLoading by mutableStateOf<Boolean>(false)
    override var fonts by mutableStateOf<List<String>>(emptyList())
    override var uiFonts by mutableStateOf<List<String>>(emptyList())
    override val isEmpty: Boolean by derivedStateOf { fonts.isEmpty() }
    override var searchedFonts by mutableStateOf<List<String>>(emptyList())
    override var error by mutableStateOf<UiText>(UiText.StringResource(org.ireader.core.R.string.no_error))
    override var inSearchMode by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String>("")
}
