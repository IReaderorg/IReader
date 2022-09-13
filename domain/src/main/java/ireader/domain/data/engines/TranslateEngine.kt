package ireader.domain.data.engines

import ireader.common.resources.UiText

interface TranslateEngine {

    val supportedLanguages: List<Pair<String, String>>

    val id: Long

    suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onSuccess: (List<String>) -> Unit,
        onError:(UiText) -> Unit
    )
}