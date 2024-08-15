package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText

expect class GoogleTranslateML() : TranslateEngine {
     override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    )
}