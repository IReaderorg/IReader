package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText

class TranslateParagraphUseCase(
    private val translationEnginesManager: TranslationEnginesManager
) {
    suspend fun execute(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        onSuccess: (String) -> Unit,
        onError: (UiText) -> Unit
    ) {
        val engine = translationEnginesManager.get()
        
        // Translate single text as a list with one item
        engine.translate(
            texts = listOf(text),
            source = sourceLanguage,
            target = targetLanguage,
            onProgress = { },
            onSuccess = { translatedTexts ->
                if (translatedTexts.isNotEmpty()) {
                    onSuccess(translatedTexts.first())
                } else {
                    onError(UiText.DynamicString("Translation returned empty result"))
                }
            },
            onError = onError
        )
    }
}
