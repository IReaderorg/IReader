package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText

/**
 * Gemini Nano stub for Desktop
 * Not available on Desktop platform
 */
actual class GeminiNano actual constructor() : TranslateEngine() {
    override val id: Long = 12L
    override val engineName: String = "Gemini Nano (Android Only)"
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
        onError(UiText.DynamicString("Gemini Nano is only available on Android 14+"))
    }
}
