package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText

/**
 * Gemini Nano - Google's on-device AI translation
 * 
 * Requires Android 14+ (API 34+)
 * Best quality offline translation available
 */
expect class GeminiNano() : TranslateEngine {
    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    )
}
