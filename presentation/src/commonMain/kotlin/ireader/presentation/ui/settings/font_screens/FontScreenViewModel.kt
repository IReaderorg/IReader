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
            isLoading = true
            try {
                // Fetch Google Fonts from the API
                fontScreenState.fonts = fontUseCase.getRemoteFonts()
                
                // Add some popular fonts at the top if not already present
                val popularFonts = listOf(
                    "Roboto", "Open Sans", "Lato", "Montserrat", "Poppins",
                    "Raleway", "Merriweather", "PT Serif", "Playfair Display",
                    "Noto Sans", "Ubuntu", "Nunito", "Source Sans Pro"
                )
                
                val allFonts = (popularFonts + fontScreenState.fonts)
                    .distinct()
                    .sorted()
                
                fontScreenState.fonts = allFonts
                
            } catch (e: Exception) {
                ireader.core.log.Log.error("Failed to load fonts", e)
                // Fallback to popular fonts if API fails
                fontScreenState.fonts = listOf(
                    "Roboto", "Open Sans", "Lato", "Montserrat", "Poppins",
                    "Raleway", "Merriweather", "PT Serif", "Playfair Display",
                    "Noto Sans", "Ubuntu", "Nunito", "Source Sans Pro",
                    "Crimson Text", "Libre Baskerville", "Lora"
                )
            } finally {
                isLoading = false
            }
            
            // Setup search filtering
            snapshotFlow {
                fonts.filteredByQuery(searchQuery)
            }.collect {
                uiFonts = it
            }
        }
    }

    private fun List<String>.filteredByQuery(query: String?): List<String> {
        return if (query == null || query.isBlank()) {
            this
        } else {
            filter { it.contains(query, ignoreCase = true) }
        }
    }
}
