package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText

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
        onError(TranslationError.EngineNotAvailable("Google ML Kit").toUiText())
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
            onError(TranslationError.NoTextToTranslate.toUiText())
            return
        }
        
        // Google ML Kit is not available on desktop - inform user
        onError(TranslationError.EngineNotAvailable("Google ML Kit").toUiText())
    }

}