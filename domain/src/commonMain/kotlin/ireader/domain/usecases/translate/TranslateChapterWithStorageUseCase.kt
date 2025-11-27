package ireader.domain.usecases.translate

import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.TranslatedChapter
import ireader.domain.usecases.glossary.GetGlossaryAsMapUseCase
import ireader.domain.usecases.translation.ApplyGlossaryToTextUseCase
import ireader.domain.usecases.translation.GetTranslatedChapterUseCase
import ireader.domain.usecases.translation.SaveTranslatedChapterUseCase
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TranslateChapterWithStorageUseCase(
    private val translationEnginesManager: TranslationEnginesManager,
    private val saveTranslatedChapterUseCase: SaveTranslatedChapterUseCase,
    private val getTranslatedChapterUseCase: GetTranslatedChapterUseCase,
    private val getGlossaryAsMapUseCase: GetGlossaryAsMapUseCase,
    private val applyGlossaryToTextUseCase: ApplyGlossaryToTextUseCase
) {
    fun execute(
        chapter: Chapter,
        sourceLanguage: String,
        targetLanguage: String,
        contentType: ContentType = ContentType.GENERAL,
        toneType: ToneType = ToneType.NEUTRAL,
        preserveStyle: Boolean = true,
        applyGlossary: Boolean = true,
        forceRetranslate: Boolean = false,
        scope: CoroutineScope,
        onProgress: (Int) -> Unit = {},
        onSuccess: (TranslatedChapter) -> Unit,
        onError: (UiText) -> Unit
    ) {
        scope.launch {
            val engineId = translationEnginesManager.get().id
            
            // Check if translation already exists
            if (!forceRetranslate) {
                val existing = getTranslatedChapterUseCase.execute(
                    chapter.id,
                    targetLanguage,
                    engineId
                )
                
                if (existing != null) {
                    onProgress(100)
                    onSuccess(existing)
                    return@launch
                }
            }
            
            // Extract text from chapter content
            val textPages = chapter.content.filterIsInstance<Text>()
            if (textPages.isEmpty()) {
                onError(UiText.MStringResource(Res.string.no_text_to_translate))
                return@launch
            }
            
            // Apply glossary BEFORE translation if enabled
            val textsToTranslate = if (applyGlossary) {
                val glossaryMap = getGlossaryAsMapUseCase.execute(chapter.bookId)
                textPages.map { page ->
                    applyGlossaryToTextUseCase.execute(page.text, glossaryMap)
                }
            } else {
                textPages.map { it.text }
            }
            
            // Perform translation using the manager
            translationEnginesManager.translateWithContext(
                texts = textsToTranslate,
                source = sourceLanguage,
                target = targetLanguage,
                contentType = contentType,
                toneType = toneType,
                preserveStyle = preserveStyle,
                onProgress = { progress ->
                    // Clamp progress to 0-100 to prevent UI issues
                    onProgress(progress.coerceIn(0, 100))
                },
                onSuccess = { translatedTexts ->
                    // Handle success in the same coroutine scope
                    scope.launch {
                        handleTranslationSuccess(
                            chapter,
                            translatedTexts,
                            sourceLanguage,
                            targetLanguage,
                            engineId,
                            applyGlossary,
                            onSuccess,
                            onError
                        )
                    }
                },
                onError = onError
            )
        }
    }
    
    private suspend fun handleTranslationSuccess(
        chapter: Chapter,
        translatedTexts: List<String>,
        sourceLanguage: String,
        targetLanguage: String,
        engineId: Long,
        applyGlossary: Boolean,
        onSuccess: (TranslatedChapter) -> Unit,
        onError: (UiText) -> Unit
    ) {
        try {
            // Glossary was already applied before translation
            // Reconstruct pages with translated text
            val translatedPages = mutableListOf<Page>()
            var textIndex = 0
            
            chapter.content.forEach { page ->
                when (page) {
                    is Text -> {
                        if (textIndex < translatedTexts.size) {
                            translatedPages.add(Text(translatedTexts[textIndex]))
                            textIndex++
                        } else {
                            translatedPages.add(page)
                        }
                    }
                    else -> translatedPages.add(page)
                }
            }
            
            // Save translated chapter
            saveTranslatedChapterUseCase.execute(
                chapter = chapter,
                translatedContent = translatedPages,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                engineId = engineId
            )
            
            // Retrieve and return the saved translation
            val savedTranslation = getTranslatedChapterUseCase.execute(
                chapter.id,
                targetLanguage,
                engineId
            )
            
            if (savedTranslation != null) {
                onSuccess(savedTranslation)
            } else {
                onError(UiText.MStringResource(Res.string.api_response_error))
            }
        } catch (e: Exception) {
            onError(UiText.ExceptionString(e))
        }
    }
}
