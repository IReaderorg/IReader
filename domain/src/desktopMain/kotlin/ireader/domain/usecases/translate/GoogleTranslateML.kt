package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText

/**
 * Desktop implementation of GoogleTranslateML.
 * Seamlessly delegates to GoogleTranslateFree on Desktop platforms.
 */
actual class GoogleTranslateML : TranslateEngine() {

    private val delegate by lazy { GoogleTranslateFree() }

    override val id: Long = 0L
    override val engineName: String = "Google Translate (Desktop)"

    actual override val requiresInitialization: Boolean
        get() = false
    
    actual override suspend fun initialize(
        sourceLanguage: String,
        targetLanguage: String,
        onProgress: (Int) -> Unit,
        onSuccess: (String) -> Unit,
        onError: (UiText) -> Unit
    ) {
        onProgress(100)
        onSuccess("Desktop Google Translate is ready")
    }

    actual override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        if (texts.isNullOrEmpty()) {
            onError(TranslationError.NoTextToTranslate.toUiText())
            return
        }
        
        delegate.translate(
            texts = texts,
            source = source,
            target = target,
            onProgress = onProgress,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}