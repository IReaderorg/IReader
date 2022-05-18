package org.ireader.settings.setting.font_screens

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ireader.common_models.entities.FontEntity
import org.ireader.common_resources.UiText
import javax.inject.Inject
import javax.inject.Singleton

interface FontScreenState {
    var isLoading: Boolean
    var fonts: List<FontEntity>
    var uiFonts: List<FontEntity>
    val isEmpty: Boolean
    var searchedFonts: List<FontEntity>
    var error: UiText
    var inSearchMode: Boolean
    var searchQuery: String

}
@Singleton
open class FontScreenStateImpl @Inject constructor() : FontScreenState {
    override var isLoading by mutableStateOf<Boolean>(false)
    override var fonts by mutableStateOf<List<FontEntity>>(emptyList())
    override var uiFonts by mutableStateOf<List<FontEntity>>(emptyList())
    override val isEmpty: Boolean by derivedStateOf { fonts.isEmpty() }
    override var searchedFonts by mutableStateOf<List<FontEntity>>(emptyList())
    override var error by mutableStateOf<UiText>(UiText.StringResource(org.ireader.core.R.string.no_error))
    override var inSearchMode by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String>("")
}
