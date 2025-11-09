package ireader.presentation.ui.settings.font_screens

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.ExperimentalTextApi
import ireader.domain.models.fonts.CustomFont
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.fonts.FontManagementUseCase
import ireader.domain.usecases.fonts.FontUseCase
import kotlinx.coroutines.launch


@OptIn(ExperimentalTextApi::class)

class FontScreenViewModel(
        private val fontScreenState: FontScreenStateImpl,
        private val fontUseCase: FontUseCase,
        private val fontManagementUseCase: FontManagementUseCase? = null,
        val readerPreferences: ReaderPreferences,
        val androidUiPreferences: PlatformUiPreferences,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), FontScreenState by fontScreenState {

    val font = androidUiPreferences.font()?.asState()
    val previewMode = mutableStateOf(false)
    
    // Custom fonts state
    val customFonts = mutableStateListOf<CustomFont>()
    val systemFonts = mutableStateListOf<CustomFont>()
    val selectedFontId = mutableStateOf("")

    init {
        setup()
        loadCustomFonts()
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
    
    private fun loadCustomFonts() {
        fontManagementUseCase?.let { useCase ->
            scope.launch {
                try {
                    val allCustomFonts = useCase.getCustomFonts()
                    customFonts.clear()
                    customFonts.addAll(allCustomFonts)
                    
                    val allSystemFonts = useCase.getSystemFonts()
                    systemFonts.clear()
                    systemFonts.addAll(allSystemFonts)
                    
                    // Load selected font
                    selectedFontId.value = readerPreferences.selectedFontId().get()
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
    
    fun onFontSelected(fontId: String) {
        selectedFontId.value = fontId
        readerPreferences.selectedFontId().set(fontId)
    }
    
    fun importFont(fontName: String) {
        // This will be called after file picker selection
        // The actual file path will be provided by platform-specific code
    }
    
    fun deleteFont(fontId: String) {
        fontManagementUseCase?.let { useCase ->
            scope.launch {
                try {
                    useCase.deleteFont(fontId)
                    loadCustomFonts()
                } catch (e: Exception) {
                    // Handle error
                }
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
