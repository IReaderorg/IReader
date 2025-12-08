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
    private val fileSystemService: ireader.domain.services.platform.FileSystemService,
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
    
    /**
     * Pick a custom font file from the file system using platform service
     * Supports TTF and OTF font formats
     */
    fun pickFontFile() {
        scope.launch {
            when (val result = fileSystemService.pickFile(
                fileTypes = listOf("ttf", "otf"),
                title = "Select Font File"
            )) {
                is ireader.domain.services.common.ServiceResult.Success -> {
                    // Store the custom font - create a FontType with the font name
                    val fontPath = result.data.toString()
                    val fontName = fontPath.substringAfterLast("/").substringBeforeLast(".")
                    val fontType = ireader.domain.preferences.models.FontType(
                        name = fontName,
                        fontFamily = ireader.domain.models.common.FontFamilyModel.Default
                    )
                    androidUiPreferences.font()?.set(fontType)
                    showSnackBar(ireader.i18n.UiText.DynamicString("Custom font selected: $fontName"))
                }
                is ireader.domain.services.common.ServiceResult.Error -> {
                    showSnackBar(ireader.i18n.UiText.DynamicString("Font selection cancelled"))
                }
                else -> {}
            }
        }
    }
}
