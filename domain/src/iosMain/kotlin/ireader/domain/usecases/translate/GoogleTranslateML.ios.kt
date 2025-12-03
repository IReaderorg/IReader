package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText

/**
 * iOS implementation of GoogleTranslateML
 * 
 * TODO: Full implementation using Apple's Translation framework (iOS 17.4+)
 * or a third-party translation API
 */
actual class GoogleTranslateML actual constructor() : TranslateEngine() {
    
    override val id: Long = TranslateEngine.GOOGLE
    override val engineName: String = "Google ML Kit (iOS)"
    
    actual override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        // TODO: Implement using Apple Translation framework or Google Cloud Translation API
        // For now, return original texts
        onSuccess(texts)
    }
}
