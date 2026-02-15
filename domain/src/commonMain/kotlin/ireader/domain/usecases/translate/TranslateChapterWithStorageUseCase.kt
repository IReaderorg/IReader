package ireader.domain.usecases.translate

import ireader.core.log.Log
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.community.cloudflare.AutoShareTranslationUseCase
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.data.repository.BookRepository
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
    private val applyGlossaryToTextUseCase: ApplyGlossaryToTextUseCase,
    private val autoShareTranslationUseCase: AutoShareTranslationUseCase,
    private val bookRepository: BookRepository? = null
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
            
            // Check if translation already exists locally
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
            
            val originalTexts = textPages.map { it.text }
            val originalContent = originalTexts.joinToString("\n\n")
            
            // Check community for existing translation first (if enabled)
            if (!forceRetranslate) {
                try {
                    val communityResult = autoShareTranslationUseCase.checkExistingTranslation(
                        originalContent = originalContent,
                        targetLanguage = targetLanguage,
                        engineId = engineId
                    )
                    
                    if (communityResult.found && communityResult.content != null) {
                        Log.info { "Found community translation for chapter ${chapter.name}" }
                        onProgress(50)
                        
                        // Parse community content back to pages
                        val communityTexts = communityResult.content.split("\n\n")
                        
                        // Save locally and return
                        handleTranslationSuccess(
                            chapter = chapter,
                            translatedTexts = communityTexts,
                            sourceLanguage = sourceLanguage,
                            targetLanguage = targetLanguage,
                            engineId = engineId,
                            applyGlossary = false, // Already translated
                            originalContent = originalContent,
                            onSuccess = onSuccess,
                            onError = onError
                        )
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.warn { "Failed to check community translations: ${e.message}" }
                    // Continue with normal translation
                }
            }
            
            // Apply glossary BEFORE translation if enabled
            val textsToTranslate = if (applyGlossary) {
                val glossaryMap = getGlossaryAsMapUseCase.execute(chapter.bookId)
                textPages.map { page ->
                    applyGlossaryToTextUseCase.execute(page.text, glossaryMap)
                }
            } else {
                originalTexts
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
                    // Sanitize: remove leftover PARAGRAPH_BREAK markers from dumb AI models
                    val sanitizedTexts = TranslateEngine.sanitizeTranslatedParagraphs(translatedTexts)
                    // Handle success in the same coroutine scope
                    scope.launch {
                        handleTranslationSuccess(
                            chapter = chapter,
                            translatedTexts = sanitizedTexts,
                            sourceLanguage = sourceLanguage,
                            targetLanguage = targetLanguage,
                            engineId = engineId,
                            applyGlossary = applyGlossary,
                            originalContent = originalContent,
                            onSuccess = onSuccess,
                            onError = onError
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
        originalContent: String,
        onSuccess: (TranslatedChapter) -> Unit,
        onError: (UiText) -> Unit
    ) {
        try {
            Log.info { "TranslateChapterWithStorageUseCase: handleTranslationSuccess called for chapter ${chapter.id}" }
            Log.info { "TranslateChapterWithStorageUseCase: translatedTexts size: ${translatedTexts.size}" }
            
            // Sanitize: ensure no PARAGRAPH_BREAK markers leaked into the final text
            val cleanedTexts = translatedTexts.map { TranslateEngine.sanitizeParagraphBreakMarkers(it) }
            // Glossary was already applied before translation
            // Reconstruct pages with translated text
            val translatedPages = mutableListOf<Page>()
            var textIndex = 0
            
            chapter.content.forEach { page ->
                when (page) {
                    is Text -> {
                        if (textIndex < cleanedTexts.size) {
                            translatedPages.add(Text(cleanedTexts[textIndex]))
                            textIndex++
                        } else {
                            translatedPages.add(page)
                        }
                    }
                    else -> translatedPages.add(page)
                }
            }
            
            Log.info { "TranslateChapterWithStorageUseCase: Saving ${translatedPages.size} translated pages to DB" }
            
            // Save translated chapter locally
            saveTranslatedChapterUseCase.execute(
                chapter = chapter,
                translatedContent = translatedPages,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                engineId = engineId
            )
            
            Log.info { "TranslateChapterWithStorageUseCase: Save completed, now retrieving from DB" }
            
            // Auto-share to community if enabled (AI translations only)
            if (autoShareTranslationUseCase.shouldAutoShare(engineId)) {
                try {
                    val book = bookRepository?.findBookById(chapter.bookId)
                    if (book != null) {
                        val translatedContent = translatedTexts.joinToString("\n\n")
                        
                        val shareResult = autoShareTranslationUseCase.shareTranslation(
                            book = book,
                            chapter = chapter,
                            originalContent = originalContent,
                            translatedContent = translatedContent,
                            sourceLanguage = sourceLanguage,
                            targetLanguage = targetLanguage,
                            engineId = engineId
                        )
                        
                        if (shareResult.isSuccess) {
                            Log.info { "Translation shared to community: ${shareResult.getOrNull()}" }
                        } else {
                            Log.warn { "Failed to share translation: ${shareResult.exceptionOrNull()?.message}" }
                        }
                    }
                } catch (e: Exception) {
                    // Don't fail the translation if sharing fails
                    Log.warn { "Auto-share failed: ${e.message}" }
                }
            }
            
            // Retrieve and return the saved translation
            val savedTranslation = getTranslatedChapterUseCase.execute(
                chapter.id,
                targetLanguage,
                engineId
            )
            
            Log.info { "TranslateChapterWithStorageUseCase: Retrieved from DB: ${savedTranslation != null}, content size: ${savedTranslation?.translatedContent?.size ?: 0}" }
            
            if (savedTranslation != null) {
                onSuccess(savedTranslation)
            } else {
                Log.error { "TranslateChapterWithStorageUseCase: Failed to retrieve saved translation!" }
                onError(UiText.MStringResource(Res.string.api_response_error))
            }
        } catch (e: Exception) {
            Log.error { "TranslateChapterWithStorageUseCase: Error in handleTranslationSuccess: ${e.message}" }
            e.printStackTrace()
            onError(UiText.ExceptionString(e))
        }
    }
}
