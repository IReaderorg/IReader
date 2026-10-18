package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText

/**
 * Free Google Translate Web API Engine (expect declaration)
 * 
 * Uses the free Google Translate API that Chrome uses.
 * Available on all platforms.
 */
expect class GoogleTranslateFree() : TranslateEngine {
    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    )
}
