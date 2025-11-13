package ireader.presentation.ui.reader.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import ireader.domain.models.fonts.CustomFont
import ireader.domain.usecases.fonts.FontManagementUseCase
import kotlinx.coroutines.launch

/**
 * Wrapper around SelectableTranslatableText that applies custom fonts
 */
@Composable
fun ReaderTextWithCustomFont(
    text: String,
    selectedFontId: String,
    fontManagementUseCase: FontManagementUseCase?,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    defaultFontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    selectable: Boolean = true,
    paragraphTranslationEnabled: Boolean = false,
    onTextSelected: (String) -> Unit = {},
    onTranslateRequest: (String) -> Unit = {}
) {
    var customFont by remember { mutableStateOf<CustomFont?>(null) }
    val scope = rememberCoroutineScope()
    
    // Load custom font when selectedFontId changes
    LaunchedEffect(selectedFontId) {
        if (selectedFontId.isNotEmpty() && fontManagementUseCase != null) {
            scope.launch {
                try {
                    customFont = fontManagementUseCase.getFontById(selectedFontId)
                } catch (e: Exception) {
                    // If font loading fails, use default
                    customFont = null
                }
            }
        } else {
            customFont = null
        }
    }
    
    // Determine which font family to use
    val fontFamily = when {
        customFont != null -> loadFontFamily(customFont)
        defaultFontFamily != null -> defaultFontFamily
        else -> FontFamily.Default
    }
    
    SelectableTranslatableText(
        text = text,
        modifier = modifier,
        fontSize = fontSize,
        fontFamily = fontFamily,
        textAlign = textAlign,
        color = color,
        lineHeight = lineHeight,
        letterSpacing = letterSpacing,
        fontWeight = fontWeight,
        selectable = selectable,
        paragraphTranslationEnabled = paragraphTranslationEnabled,
        onTextSelected = onTextSelected,
        onTranslateRequest = onTranslateRequest
    )
}
