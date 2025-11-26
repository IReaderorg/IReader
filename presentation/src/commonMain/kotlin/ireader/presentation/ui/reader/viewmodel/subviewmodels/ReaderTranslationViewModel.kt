package ireader.presentation.ui.reader.viewmodel.subviewmodels

import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.TranslatedChapter
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.glossary.*
import ireader.domain.usecases.translate.TranslateChapterWithStorageUseCase
import ireader.domain.usecases.translate.TranslateParagraphUseCase
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.domain.usecases.translation.GetTranslatedChapterUseCase
import ireader.i18n.UiText
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Sub-ViewModel responsible for translation and glossary management.
 * 
 * Responsibilities:
 * - Translation engine management
 * - Chapter/paragraph translation
 * - Glossary CRUD operations
 * - Translation caching and state
 */
class ReaderTranslationViewModel(
    private val translationEnginesManager: TranslationEnginesManager,
    private val translateChapterWithStorageUseCase: TranslateChapterWithStorageUseCase,
    private val translateParagraphUseCase: TranslateParagraphUseCase,
    private val getTranslatedChapterUseCase: GetTranslatedChapterUseCase,
    private val getGlossaryByBookIdUseCase: GetGlossaryByBookIdUseCase,
    private val saveGlossaryEntryUseCase: SaveGlossaryEntryUseCase,
    private val deleteGlossaryEntryUseCase: DeleteGlossaryEntryUseCase,
    private val exportGlossaryUseCase: ExportGlossaryUseCase,
    private val importGlossaryUseCase: ImportGlossaryUseCase,
    private val readerPreferences: ReaderPreferences,
) : BaseViewModel() {

    // Translation State
    private val _translationState = MutableStateFlow(TranslationState())
    val translationState: StateFlow<TranslationState> = _translationState.asStateFlow()

    // Glossary State
    private val _glossaryEntries = MutableStateFlow<List<Glossary>>(emptyList())
    val glossaryEntries: StateFlow<List<Glossary>> = _glossaryEntries.asStateFlow()

    // Paragraph Translation State
    private val _paragraphTranslation = MutableStateFlow<String?>(null)
    val paragraphTranslation: StateFlow<String?> = _paragraphTranslation.asStateFlow()

    private val _isTranslatingParagraph = MutableStateFlow(false)
    val isTranslatingParagraph: StateFlow<Boolean> = _isTranslatingParagraph.asStateFlow()

    data class TranslationState(
        val translatedChapter: TranslatedChapter? = null,
        val hasTranslation: Boolean = false,
        val isShowingTranslation: Boolean = false,
        val isTranslating: Boolean = false,
        val translationProgress: Float = 0f,
        val translationError: String? = null
    )

    /**
     * Load translation for a chapter if available
     */
    suspend fun loadTranslationForChapter(
        chapterId: Long,
        targetLanguage: String,
        engineId: Long
    ) {
        resetTranslationState()

        try {
            val translation = getTranslatedChapterUseCase.execute(
                chapterId = chapterId,
                targetLanguage = targetLanguage,
                engineId = engineId
            )

            _translationState.value = _translationState.value.copy(
                translatedChapter = translation,
                hasTranslation = translation != null,
                isShowingTranslation = readerPreferences.showTranslatedContent().get() && translation != null
            )
        } catch (e: Exception) {
            // Silently fail if table doesn't exist yet (migration not run)
            if (e.message?.contains("no such table") == true) {
                ireader.core.log.Log.debug("Translation table not yet created, skipping translation load")
            } else {
                ireader.core.log.Log.error("Error loading translation: ${e.message}")
            }
            _translationState.value = _translationState.value.copy(hasTranslation = false)
        }
    }

    /**
     * Translate a chapter with storage
     */
    fun translateChapter(
        chapter: Chapter,
        sourceLanguage: String,
        targetLanguage: String,
        contentType: ContentType,
        toneType: ToneType,
        preserveStyle: Boolean,
        applyGlossary: Boolean,
        forceRetranslate: Boolean = false,
        onSuccess: (TranslatedChapter) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        _translationState.value = _translationState.value.copy(
            isTranslating = true,
            translationError = null,
            translationProgress = 0f
        )

        translateChapterWithStorageUseCase.execute(
            chapter = chapter,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            contentType = contentType,
            toneType = toneType,
            preserveStyle = preserveStyle,
            applyGlossary = applyGlossary,
            forceRetranslate = forceRetranslate,
            scope = scope,
            onProgress = { progress ->
                _translationState.value = _translationState.value.copy(
                    translationProgress = progress / 100f
                )
            },
            onSuccess = { translatedChapter ->
                _translationState.value = _translationState.value.copy(
                    translatedChapter = translatedChapter,
                    hasTranslation = true,
                    isShowingTranslation = true,
                    isTranslating = false,
                    translationProgress = 1f
                )
                onSuccess(translatedChapter)
            },
            onError = { error ->
                val errorMessage = error.toString()
                _translationState.value = _translationState.value.copy(
                    isTranslating = false,
                    translationError = errorMessage,
                    translationProgress = 0f
                )
                onError(errorMessage)
            }
        )
    }

    /**
     * Translate a single paragraph
     */
    fun translateParagraph(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (text.isBlank()) return

        _isTranslatingParagraph.value = true
        _paragraphTranslation.value = null

        scope.launch {
            try {
                translateParagraphUseCase.execute(
                    text = text,
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    onSuccess = { result ->
                        _paragraphTranslation.value = result
                        _isTranslatingParagraph.value = false
                        onSuccess(result)
                    },
                    onError = { errorMessage ->
                        _isTranslatingParagraph.value = false
                        ireader.core.log.Log.error("Paragraph translation failed: $errorMessage")
                        onError(errorMessage.toString())
                    }
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                _isTranslatingParagraph.value = false
                ireader.core.log.Log.error("Paragraph translation failed: $errorMessage")
                onError(errorMessage)
            }
        }
    }

    /**
     * Toggle between original and translated content
     */
    fun toggleTranslation() {
        if (_translationState.value.hasTranslation) {
            val newValue = !_translationState.value.isShowingTranslation
            _translationState.value = _translationState.value.copy(isShowingTranslation = newValue)
            readerPreferences.showTranslatedContent().set(newValue)
        }
    }

    /**
     * Reset translation state
     */
    fun resetTranslationState() {
        _translationState.value = TranslationState()
    }

    /**
     * Get current chapter content (original or translated)
     */
    fun getCurrentChapterContent(originalContent: List<ireader.core.source.model.Page>): List<ireader.core.source.model.Page> {
        return try {
            val state = _translationState.value
            if (state.isShowingTranslation &&
                state.translatedChapter != null &&
                state.translatedChapter.translatedContent.isNotEmpty()
            ) {
                state.translatedChapter.translatedContent
            } else {
                originalContent
            }
        } catch (e: Exception) {
            ireader.core.log.Log.error("Error getting chapter content", e)
            originalContent
        }
    }

    /**
     * Check if API key is required for current engine
     */
    fun isApiKeyRequired(): Boolean {
        val engine = translationEnginesManager.get()
        return engine.requiresApiKey
    }

    /**
     * Check if API key is set for current engine
     */
    fun isApiKeySet(openAIKey: String, deepSeekKey: String): Boolean {
        val engine = translationEnginesManager.get()
        if (!engine.requiresApiKey) return true

        return when (engine.id) {
            2L -> openAIKey.isNotBlank() // OpenAI
            3L -> deepSeekKey.isNotBlank() // DeepSeek
            else -> true
        }
    }

    /**
     * Get current engine name
     */
    fun getCurrentEngineName(): String {
        return translationEnginesManager.get().engineName
    }

    /**
     * Test translation API connection
     */
    fun testTranslationConnection(
        targetLanguage: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                translationEnginesManager.translateWithContext(
                    texts = listOf("Hello"),
                    source = "en",
                    target = targetLanguage,
                    onProgress = { },
                    onSuccess = { translatedTexts ->
                        if (translatedTexts.isNotEmpty()) {
                            onSuccess("Connection successful! Translation engine is working correctly.")
                        } else {
                            onError("Connection test failed: Empty response")
                        }
                    },
                    onError = { error ->
                        onError("Connection test failed: $error")
                    }
                )
            } catch (e: Exception) {
                onError("Connection test failed: ${e.message ?: "Unknown error"}")
            }
        }
    }

    // ==================== Glossary Methods ====================

    /**
     * Load glossary entries for a book
     */
    fun loadGlossary(bookId: Long) {
        scope.launch {
            try {
                getGlossaryByBookIdUseCase.subscribe(bookId)
                    .collect { entries ->
                        _glossaryEntries.value = entries
                    }
            } catch (e: Exception) {
                ireader.core.log.Log.error("Error loading glossary: ${e.message}")
            }
        }
    }

    /**
     * Add or update a glossary entry
     */
    fun saveGlossaryEntry(
        bookId: Long,
        sourceTerm: String,
        targetTerm: String,
        termType: ireader.domain.models.entities.GlossaryTermType,
        notes: String? = null,
        entryId: Long? = null,
        onSuccess: () -> Unit = {}
    ) {
        scope.launch {
            try {
                saveGlossaryEntryUseCase.execute(
                    bookId = bookId,
                    sourceTerm = sourceTerm,
                    targetTerm = targetTerm,
                    termType = termType,
                    notes = notes,
                    entryId = entryId
                )
                onSuccess()
                ireader.core.log.Log.debug("Glossary entry saved: $sourceTerm")
            } catch (e: Exception) {
                ireader.core.log.Log.error("Error saving glossary entry: ${e.message}")
            }
        }
    }

    /**
     * Delete a glossary entry
     */
    fun deleteGlossaryEntry(entryId: Long, onSuccess: () -> Unit = {}) {
        scope.launch {
            try {
                deleteGlossaryEntryUseCase.execute(entryId)
                onSuccess()
                ireader.core.log.Log.debug("Glossary entry deleted: $entryId")
            } catch (e: Exception) {
                ireader.core.log.Log.error("Error deleting glossary entry: ${e.message}")
            }
        }
    }

    /**
     * Export glossary to JSON
     */
    fun exportGlossary(bookId: Long, bookTitle: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        scope.launch {
            try {
                val json = exportGlossaryUseCase.execute(bookId, bookTitle)
                onSuccess(json)
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                ireader.core.log.Log.error("Error exporting glossary: $errorMessage")
                onError(errorMessage)
            }
        }
    }

    /**
     * Import glossary from JSON
     */
    fun importGlossary(jsonString: String, targetBookId: Long?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        scope.launch {
            try {
                importGlossaryUseCase.execute(jsonString, targetBookId)
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                ireader.core.log.Log.error("Error importing glossary: $errorMessage")
                onError(errorMessage)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        resetTranslationState()
    }
}
