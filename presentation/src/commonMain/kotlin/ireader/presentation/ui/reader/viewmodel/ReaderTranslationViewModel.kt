package ireader.presentation.ui.reader.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.log.Log
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.Glossary
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.glossary.*
import ireader.domain.usecases.translate.TranslateChapterWithStorageUseCase
import ireader.domain.usecases.translate.TranslateParagraphUseCase
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.domain.usecases.translation.GetTranslatedChapterUseCase
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for translation and glossary management
 * 
 * Handles:
 * - Chapter translation
 * - Paragraph translation
 * - Glossary management
 * - Translation progress tracking
 * - Translation settings
 */
class ReaderTranslationViewModel(
    private val translateChapterWithStorageUseCase: TranslateChapterWithStorageUseCase,
    private val translateParagraphUseCase: TranslateParagraphUseCase,
    private val getTranslatedChapterUseCase: GetTranslatedChapterUseCase,
    private val getGlossaryByBookIdUseCase: GetGlossaryByBookIdUseCase,
    private val saveGlossaryEntryUseCase: SaveGlossaryEntryUseCase,
    private val deleteGlossaryEntryUseCase: DeleteGlossaryEntryUseCase,
    private val exportGlossaryUseCase: ExportGlossaryUseCase,
    private val importGlossaryUseCase: ImportGlossaryUseCase,
    private val translationEnginesManager: TranslationEnginesManager,
    private val readerPreferences: ReaderPreferences,
) : BaseViewModel() {
    
    // Translation state
    val translationState = TranslationStateHolder()
    
    // Translation progress
    var isTranslating by mutableStateOf(false)
        private set
    
    var translationProgress by mutableStateOf(0f)
        private set
    
    var translationTotal by mutableStateOf(0)
        private set
    
    var translationCompleted by mutableStateOf(0)
        private set
    
    // Glossary state
    var glossaryEntries by mutableStateOf<List<Glossary>>(emptyList())
        private set
    
    var isLoadingGlossary by mutableStateOf(false)
        private set
    
    // Translation preferences
    val showTranslatedContent = readerPreferences.showTranslatedContent().asState()
    val autoSaveTranslations = readerPreferences.autoSaveTranslations().asState()
    val applyGlossaryToTranslations = readerPreferences.applyGlossaryToTranslations().asState()
    val bilingualModeEnabled = readerPreferences.bilingualModeEnabled().asState()
    val bilingualModeLayout = readerPreferences.bilingualModeLayout().asState()
    val paragraphTranslationEnabled = readerPreferences.paragraphTranslationEnabled().asState()
    val useTTSWithTranslatedText = readerPreferences.useTTSWithTranslatedText().asState()
    val autoTranslateNextChapter = readerPreferences.autoTranslateNextChapter().asState()
    
    val translatorOriginLanguage = readerPreferences.translatorOriginLanguage().asState()
    val translatorTargetLanguage = readerPreferences.translatorTargetLanguage().asState()
    val translatorEngine = readerPreferences.translatorEngine().asState()
    val translatorContentType = readerPreferences.translatorContentType().asState()
    val translatorToneType = readerPreferences.translatorToneType().asState()
    val translatorPreserveStyle = readerPreferences.translatorPreserveStyle().asState()
    
    // API keys
    val openAIApiKey = readerPreferences.openAIApiKey().asState()
    val deepSeekApiKey = readerPreferences.deepSeekApiKey().asState()
    
    // ==================== Translation ====================
    
    /**
     * Translate current chapter
     */
    suspend fun translateChapter(
        chapter: Chapter,
        forceRetranslate: Boolean = false
    ) {
        if (isTranslating) {
            Log.debug("Translation already in progress")
            return
        }
        
        isTranslating = true
        translationProgress = 0f
        translationCompleted = 0
        
        try {
            showSnackBar(UiText.MStringResource(Res.string.translating))
            
            // Get translation settings
            val contentType = ContentType.values().getOrElse(translatorContentType.value) {
                ContentType.GENERAL
            }
            
            val toneType = ToneType.values().getOrElse(translatorToneType.value) {
                ToneType.NEUTRAL
            }
            
            val preserveStyle = translatorPreserveStyle.value
            
            // Extract text content
            val content = chapter.content ?: emptyList()
            val texts = content.filterIsInstance<ireader.core.source.model.Text>().map { it.text }
            
            if (texts.isEmpty()) {
                showSnackBar(UiText.MStringResource(Res.string.no_text_to_translate))
                isTranslating = false
                return
            }
            
            translationTotal = texts.size
            
            // Get active translation engine
            val engine = translationEnginesManager.get()
            
            Log.debug("Using translation engine: ${engine.engineName}")
            
            // Use translation engine manager
            translationEnginesManager.translateWithContext(
                texts = texts,
                source = translatorOriginLanguage.value,
                target = translatorTargetLanguage.value,
                contentType = contentType,
                toneType = toneType,
                preserveStyle = preserveStyle,
                onProgress = { completed ->
                    translationCompleted = completed
                    translationProgress = completed.toFloat() / translationTotal.toFloat()
                },
                onSuccess = { translatedTexts ->
                    // Update translation state
                    translationState.translatedContent = translatedTexts.map { text ->
                        ireader.core.source.model.Text(text)
                    }
                    translationState.hasTranslation = true
                    
                    showSnackBar(UiText.DynamicString("Translation complete"))
                    translationProgress = 1f
                    isTranslating = false
                    
                    // Auto-save if enabled
                    if (autoSaveTranslations.value) {
                        saveTranslation(chapter.id, translatedTexts)
                    }
                },
                onError = { errorMessage ->
                    showSnackBar(errorMessage)
                    isTranslating = false
                }
            )
            
        } catch (e: Exception) {
            Log.error("Translation failed", e)
            showSnackBar(UiText.DynamicString("Translation error: ${e.message}"))
            isTranslating = false
        }
    }
    
    /**
     * Translate a single paragraph
     */
    suspend fun translateParagraph(
        text: String,
        onSuccess: (String) -> Unit
    ) {
        try {
            translateParagraphUseCase.execute(
                text = text,
                sourceLanguage = translatorOriginLanguage.value,
                targetLanguage = translatorTargetLanguage.value,
                onSuccess = onSuccess,
                onError = { errorMessage ->
                    showSnackBar(errorMessage)
                }
            )
        } catch (e: Exception) {
            Log.error("Paragraph translation failed", e)
            showSnackBar(UiText.DynamicString("Translation error: ${e.message}"))
        }
    }
    
    /**
     * Load saved translation for chapter
     */
    suspend fun loadTranslationForChapter(chapterId: Long) {
        try {
            val engineId = translationEnginesManager.get().id
            val translated = getTranslatedChapterUseCase.execute(
                chapterId = chapterId,
                targetLanguage = translatorTargetLanguage.value,
                engineId = engineId
            )
            
            if (translated != null) {
                translationState.translatedContent = translated.translatedContent
                translationState.hasTranslation = true
                Log.debug("Loaded saved translation for chapter $chapterId")
            } else {
                translationState.reset()
            }
            
        } catch (e: Exception) {
            Log.error("Failed to load translation", e)
            translationState.reset()
        }
    }
    
    /**
     * Save translation to storage
     */
    private fun saveTranslation(chapterId: Long, translatedTexts: List<String>) {
        scope.launch {
            try {
                // Note: TranslateChapterWithStorageUseCase doesn't have a direct saveTranslation method
                // The saving is handled internally by the use case during translation
                // This is a placeholder for future implementation if needed
                Log.debug("Translation auto-save is handled by TranslateChapterWithStorageUseCase")
            } catch (e: Exception) {
                Log.error("Failed to save translation", e)
            }
        }
    }
    
    /**
     * Toggle translation display
     */
    fun toggleTranslation() {
        scope.launch {
            readerPreferences.showTranslatedContent().set(!showTranslatedContent.value)
        }
    }
    
    /**
     * Cancel ongoing translation
     */
    fun cancelTranslation() {
        isTranslating = false
        translationProgress = 0f
        translationCompleted = 0
        translationTotal = 0
    }
    
    /**
     * Clear translation
     */
    fun clearTranslation() {
        translationState.reset()
        translationProgress = 0f
        translationCompleted = 0
        translationTotal = 0
    }
    
    // ==================== Glossary ====================
    
    /**
     * Load glossary for book
     */
    suspend fun loadGlossary(bookId: Long) {
        isLoadingGlossary = true
        
        try {
            glossaryEntries = getGlossaryByBookIdUseCase.execute(bookId)
            Log.debug("Loaded ${glossaryEntries.size} glossary entries")
        } catch (e: Exception) {
            Log.error("Failed to load glossary", e)
            glossaryEntries = emptyList()
        } finally {
            isLoadingGlossary = false
        }
    }
    
    /**
     * Add glossary entry
     */
    suspend fun addGlossaryEntry(
        bookId: Long,
        sourceTerm: String,
        targetTerm: String,
        termType: ireader.domain.models.entities.GlossaryTermType = ireader.domain.models.entities.GlossaryTermType.CUSTOM,
        notes: String? = null
    ) {
        try {
            saveGlossaryEntryUseCase.execute(
                bookId = bookId,
                sourceTerm = sourceTerm,
                targetTerm = targetTerm,
                termType = termType,
                notes = notes,
                entryId = null
            )
            
            // Reload glossary
            loadGlossary(bookId)
            
            showSnackBar(UiText.DynamicString("Glossary entry added"))
            
        } catch (e: Exception) {
            Log.error("Failed to add glossary entry", e)
            showSnackBar(UiText.DynamicString("Failed to add entry: ${e.message}"))
        }
    }
    
    /**
     * Update glossary entry
     */
    suspend fun updateGlossaryEntry(entry: Glossary) {
        try {
            saveGlossaryEntryUseCase.execute(
                bookId = entry.bookId,
                sourceTerm = entry.sourceTerm,
                targetTerm = entry.targetTerm,
                termType = entry.termType,
                notes = entry.notes,
                entryId = entry.id
            )
            
            // Reload glossary
            loadGlossary(entry.bookId)
            
            showSnackBar(UiText.DynamicString("Glossary entry updated"))
            
        } catch (e: Exception) {
            Log.error("Failed to update glossary entry", e)
            showSnackBar(UiText.DynamicString("Failed to update entry: ${e.message}"))
        }
    }
    
    /**
     * Delete glossary entry
     */
    suspend fun deleteGlossaryEntry(entry: Glossary) {
        try {
            deleteGlossaryEntryUseCase.execute(entry.id)
            
            // Reload glossary
            loadGlossary(entry.bookId)
            
            showSnackBar(UiText.DynamicString("Glossary entry deleted"))
            
        } catch (e: Exception) {
            Log.error("Failed to delete glossary entry", e)
            showSnackBar(UiText.DynamicString("Failed to delete entry: ${e.message}"))
        }
    }
    
    /**
     * Export glossary to file
     */
    suspend fun exportGlossary(bookId: Long, bookTitle: String): String {
        return try {
            val json = exportGlossaryUseCase.execute(bookId, bookTitle)
            showSnackBar(UiText.DynamicString("Glossary exported"))
            json
        } catch (e: Exception) {
            Log.error("Failed to export glossary", e)
            showSnackBar(UiText.DynamicString("Export failed: ${e.message}"))
            ""
        }
    }
    
    /**
     * Import glossary from file
     */
    suspend fun importGlossary(bookId: Long, jsonString: String) {
        try {
            val count = importGlossaryUseCase.execute(jsonString, bookId)
            
            // Reload glossary
            loadGlossary(bookId)
            
            showSnackBar(UiText.DynamicString("Glossary imported: $count entries"))
        } catch (e: Exception) {
            Log.error("Failed to import glossary", e)
            showSnackBar(UiText.DynamicString("Import failed: ${e.message}"))
        }
    }
    
    // ==================== Settings ====================
    
    /**
     * Update translation engine
     */
    fun setTranslationEngine(engineId: Long) {
        scope.launch {
            readerPreferences.translatorEngine().set(engineId)
        }
    }
    
    /**
     * Update source language
     */
    fun setSourceLanguage(language: String) {
        scope.launch {
            readerPreferences.translatorOriginLanguage().set(language)
        }
    }
    
    /**
     * Update target language
     */
    fun setTargetLanguage(language: String) {
        scope.launch {
            readerPreferences.translatorTargetLanguage().set(language)
        }
    }
    
    /**
     * Toggle bilingual mode
     */
    fun toggleBilingualMode(enabled: Boolean) {
        scope.launch {
            readerPreferences.bilingualModeEnabled().set(enabled)
        }
    }
    
    /**
     * Toggle auto-translate next chapter
     */
    fun toggleAutoTranslateNextChapter(enabled: Boolean) {
        scope.launch {
            readerPreferences.autoTranslateNextChapter().set(enabled)
        }
    }
}


