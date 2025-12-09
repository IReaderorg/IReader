package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

actual class GoogleTranslateML : TranslateEngine() {

    actual override val requiresInitialization: Boolean
        get() = false // Not available on desktop
    
    actual override suspend fun initialize(
        sourceLanguage: String,
        targetLanguage: String,
        onProgress: (Int) -> Unit,
        onSuccess: (String) -> Unit,
        onError: (UiText) -> Unit
    ) {
        // Google ML Kit is not available on desktop
        onError(UiText.DynamicString("Google ML Kit translation is only available on Android"))
    }

    actual override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        // Validate inputs
        if (texts.isNullOrEmpty()) {
            onError(UiText.MStringResource(Res.string.no_text_to_translate))
            return
        }
        
        // No implementation for desktop, just pass through the texts
        onProgress(100)
        onSuccess(texts)
    }

}