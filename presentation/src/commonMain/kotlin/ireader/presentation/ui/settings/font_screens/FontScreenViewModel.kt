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
    
    fun importFont(fontName: String, uri: ireader.domain.models.common.Uri) {
        fontManagementUseCase?.let { useCase ->
            scope.launch {
                try {
                    val result = useCase.importFontFromUri(uri, fontName)
                    result.onSuccess { font ->
                        // Reload fonts to show the newly imported font
                        loadCustomFonts()
                        // Optionally select the newly imported font
                        onFontSelected(font.id)
                        showSnackBar(ireader.i18n.UiText.DynamicString("Font imported successfully"))
                    }
                    result.onFailure { error ->
                        // Handle error - could show a snackbar or error message
                        showSnackBar(ireader.i18n.UiText.DynamicString("Failed to import font: ${error.message}"))
                        ireader.core.log.Log.error("Failed to import font", error)
                    }
                } catch (e: Exception) {
                    ireader.core.log.Log.error("Failed to import font", e)
                }
            }
        }
    }
    
    fun deleteFont(fontId: String) {
        fontManagementUseCase?.let { useCase ->
            scope.launch {
                try {
                    val result = useCase.deleteFont(fontId)
                    result.onSuccess {
                        loadCustomFonts()
                        // If deleted font was selected, reset to default
                        if (selectedFontId.value == fontId) {
                            selectedFontId.value = ""
                            readerPreferences.selectedFontId().set("")
                        }
                        showSnackBar(ireader.i18n.UiText.DynamicString("Font deleted successfully"))
                    }.onFailure { error ->
                        showSnackBar(ireader.i18n.UiText.DynamicString("Failed to delete font: ${error.message}"))
                        ireader.core.log.Log.error("Failed to delete font", error)
                    }
                } catch (e: Exception) {
                    showSnackBar(ireader.i18n.UiText.DynamicString("Failed to delete font: ${e.message}"))
                    ireader.core.log.Log.error("Failed to delete font", e)
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
