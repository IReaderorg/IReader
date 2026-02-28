package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText

/**
 * Free Google Translate Web API Engine (Desktop stub)
 * Desktop uses the iOS implementation which already has this functionality
 */
actual class GoogleTranslateFree actual constructor() : TranslateEngine() {
    override val id: Long = 11L
    override val engineName: String = "Google Translate (Free)"
    override val requiresInitialization: Boolean = false
    override val requiresApiKey: Boolean = false
    
    override val supportedLanguages: List<Pair<String, String>> = emptyList()
    
    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        onError(UiText.DynamicString("Not implemented on Desktop. Use GoogleTranslateML instead."))
    }
}
