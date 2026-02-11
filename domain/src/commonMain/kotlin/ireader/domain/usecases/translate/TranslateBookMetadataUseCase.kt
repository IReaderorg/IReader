package ireader.domain.usecases.translate

import ireader.core.log.Log
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.TranslationPreferences
import ireader.i18n.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Use case for translating book metadata (title and description) when browsing.
 * This provides auto-translation of novel names and descriptions for better user experience.
 * 
 * Uses TranslationQueueManager to coordinate with content translation:
 * - Metadata translations are cancelled when content translation starts
 * - Metadata translations are skipped if content translation is active
 */
class TranslateBookMetadataUseCase(
    private val translationEnginesManager: TranslationEnginesManager,
    private val translationPreferences: TranslationPreferences,
    private val readerPreferences: ReaderPreferences,
    private val translationQueueManager: TranslationQueueManager? = null
) {
    companion object {
        private const val TAG = "TranslateBookMetadata"
        // Cache to avoid re-translating the same text
        private val translationCache = mutableMapOf<String, CachedTranslation>()
        // Semaphore to limit concurrent translations (avoid rate limiting)
        private val translationSemaphore = Semaphore(3)
        // Cache expiration time (30 minutes)
        private const val CACHE_EXPIRATION_MS = 30 * 60 * 1000L
    }

    /**
     * Cached translation entry with timestamp
     */
    private data class CachedTranslation(
        val translatedText: String,
        val timestamp: Long
    )

    /**
     * Check if auto-translate is enabled for novel names
     */
    fun isAutoTranslateNamesEnabled(): Boolean {
        return translationPreferences.autoTranslateNovelNames().get()
    }

    /**
     * Check if auto-translate is enabled for descriptions
     */
    fun isAutoTranslateDescriptionsEnabled(): Boolean {
        return translationPreferences.autoTranslateDescriptions().get()
    }

    /**
     * Get the source language from ReaderPreferences (used in reader screen modal sheet)
     */
    fun getSourceLanguage(): String {
        return readerPreferences.translatorOriginLanguage().get()
    }

    /**
     * Get the target language from ReaderPreferences (used in reader screen modal sheet)
     */
    fun getTargetLanguage(): String {
        return readerPreferences.translatorTargetLanguage().get()
    }

    /**
     * Translate a single text string with caching
     * Returns the translated text or the original if translation fails
     * 
     * Will skip translation if content translation is active (reader content has priority)
     */
    @OptIn(ExperimentalTime::class)
    suspend fun translateText(
        text: String,
        sourceLanguage: String? = null,
        targetLanguage: String? = null
    ): String {
        if (text.isBlank()) return text
        
        // Check if content translation is active - skip metadata translation
        if (translationQueueManager?.isContentTranslationActive() == true) {
            Log.info { "$TAG: Skipping metadata translation - content translation is active" }
            return text
        }

        val sourceLang = sourceLanguage ?: getSourceLanguage()
        val targetLang = targetLanguage ?: getTargetLanguage()
        
        // Check cache first
        val cacheKey = "${text}_${sourceLang}_$targetLang"
        val cached = translationCache[cacheKey]
        if (cached != null && (Clock.System.now().toEpochMilliseconds() - cached.timestamp) < CACHE_EXPIRATION_MS) {
            return cached.translatedText
        }

        // Variable to store translation result
        var translatedResult: String? = null
        
        // Use queue manager for coordinated translation
        val result = translationQueueManager?.executeTranslation(
            priority = TranslationPriority.METADATA,
            description = "Metadata: ${text.take(50)}..."
        ) {
            translatedResult = performTranslation(text, sourceLang, targetLang, cacheKey)
        }
        
        return if (result?.isSuccess == true) {
            translatedResult ?: text
        } else {
            text
        }
    }
    
    /**
     * Internal method to perform the actual translation.
     * Separated from translateText to work with queue manager.
     */
    @OptIn(ExperimentalTime::class)
    private suspend fun performTranslation(
        text: String,
        sourceLang: String,
        targetLang: String,
        cacheKey: String
    ): String {
        return try {
            translationSemaphore.withPermit {
                var translatedText: String? = null
                var error: UiText? = null

                translationEnginesManager.translateWithContext(
                    texts = listOf(text),
                    source = sourceLang,
                    target = targetLang,
                    onProgress = { /* No progress tracking for single text */ },
                    onSuccess = { translations ->
                        translatedText = translations.firstOrNull()
                    },
                    onError = { uiText ->
                        error = uiText
                    }
                )

                if (translatedText != null && translatedText != text) {
                    // Cache the result
                    translationCache[cacheKey] = CachedTranslation(translatedText!!, Clock.System.now().toEpochMilliseconds())
                    translatedText!!
                } else {
                    if (error != null) {
                        Log.error { "$TAG: Translation error: $error" }
                    }
                    text
                }
            }
        } catch (e: CancellationException) {
            Log.info { "$TAG: Translation cancelled" }
            text
        } catch (e: Exception) {
            Log.error { "$TAG: Exception during translation: ${e.message}" }
            text
        }
    }

    /**
     * Translate a list of texts in batch with caching
     * Returns a list of translated texts (or originals if translation fails)
     * 
     * Will skip translation if content translation is active (reader content has priority)
     */
    @OptIn(ExperimentalTime::class)
    suspend fun translateTexts(
        texts: List<String>,
        sourceLanguage: String? = null,
        targetLanguage: String? = null
    ): List<String> {
        if (texts.isEmpty()) return texts
        
        // Check if content translation is active - skip metadata translation
        if (translationQueueManager?.isContentTranslationActive() == true) {
            Log.info { "$TAG: Skipping batch metadata translation - content translation is active" }
            return texts
        }

        val sourceLang = sourceLanguage ?: getSourceLanguage()
        val targetLang = targetLanguage ?: getTargetLanguage()
        val results = mutableListOf<String>()
        val uncachedTexts = mutableListOf<String>()
        val uncachedIndices = mutableListOf<Int>()

        // Check cache for each text
        texts.forEachIndexed { index, text ->
            if (text.isBlank()) {
                results.add(index, text)
            } else {
                val cacheKey = "${text}_${sourceLang}_$targetLang"
                val cached = translationCache[cacheKey]
                if (cached != null && (Clock.System.now().toEpochMilliseconds() - cached.timestamp) < CACHE_EXPIRATION_MS) {
                    results.add(index, cached.translatedText)
                } else {
                    results.add(index, text) // Placeholder
                    uncachedTexts.add(text)
                    uncachedIndices.add(index)
                }
            }
        }

        // Translate uncached texts in batch
        if (uncachedTexts.isNotEmpty()) {
            // Use queue manager for coordinated translation
            val result = translationQueueManager?.executeTranslation(
                priority = TranslationPriority.METADATA,
                description = "Batch metadata: ${uncachedTexts.size} texts"
            ) {
                performBatchTranslation(uncachedTexts, uncachedIndices, results, sourceLang, targetLang)
            }
            
            if (result == null) {
                // Translation was skipped, return original texts
                return texts
            }
        }

        return results
    }
    
    /**
     * Internal method to perform batch translation.
     * Separated from translateTexts to work with queue manager.
     */
    @OptIn(ExperimentalTime::class)
    private suspend fun performBatchTranslation(
        uncachedTexts: List<String>,
        uncachedIndices: List<Int>,
        results: MutableList<String>,
        sourceLang: String,
        targetLang: String
    ) {
        try {
            translationSemaphore.withPermit {
                var translatedTexts: List<String>? = null
                var error: UiText? = null

                translationEnginesManager.translateWithContext(
                    texts = uncachedTexts,
                    source = sourceLang,
                    target = targetLang,
                    onProgress = { /* No progress tracking */ },
                    onSuccess = { translations ->
                        translatedTexts = translations
                    },
                    onError = { uiText ->
                        error = uiText
                    }
                )

                if (translatedTexts != null && translatedTexts!!.size == uncachedTexts.size) {
                    // Update results and cache
                    uncachedIndices.forEachIndexed { i, originalIndex ->
                        val translated = translatedTexts!![i]
                        results[originalIndex] = translated
                        val cacheKey = "${uncachedTexts[i]}_${sourceLang}_$targetLang"
                        translationCache[cacheKey] = CachedTranslation(translated, Clock.System.now().toEpochMilliseconds())
                    }
                } else if (error != null) {
                    Log.error { "$TAG: Batch translation error: $error" }
                }
            }
        } catch (e: CancellationException) {
            Log.info { "$TAG: Batch translation cancelled" }
        } catch (e: Exception) {
            Log.error { "$TAG: Exception during batch translation: ${e.message}" }
        }
    }

    /**
     * Translate manga info (title and optionally description)
     * Returns a new MangaInfo with translated fields
     */
    suspend fun translateMangaInfo(
        mangaInfo: MangaInfo,
        translateTitle: Boolean = isAutoTranslateNamesEnabled(),
        translateDescription: Boolean = isAutoTranslateDescriptionsEnabled()
    ): MangaInfo {
        if (!translateTitle && !translateDescription) return mangaInfo

        var translatedTitle = mangaInfo.title
        var translatedDescription = mangaInfo.description

        withContext(Dispatchers.IO) {
            if (translateTitle && mangaInfo.title.isNotBlank()) {
                translatedTitle = translateText(mangaInfo.title)
            }
            if (translateDescription && !mangaInfo.description.isNullOrBlank()) {
                translatedDescription = translateText(mangaInfo.description)
            }
        }

        return mangaInfo.copy(
            title = translatedTitle,
            description = translatedDescription
        )
    }

    /**
     * Translate a MangasPageInfo (list of manga info)
     * Returns a new MangasPageInfo with translated fields
     */
    suspend fun translateMangasPage(
        page: MangasPageInfo,
        translateTitles: Boolean = isAutoTranslateNamesEnabled(),
        translateDescriptions: Boolean = isAutoTranslateDescriptionsEnabled()
    ): MangasPageInfo {
        if (!translateTitles && !translateDescriptions) return page
        if (page.mangas.isEmpty()) return page

        // Collect all titles and descriptions for batch translation
        val titles = if (translateTitles) page.mangas.map { it.title }.filter { it.isNotBlank() } else emptyList()
        val descriptions = if (translateDescriptions) page.mangas.mapNotNull { it.description?.takeIf { it.isNotBlank() } } else emptyList()

        // Batch translate
        val translatedTitles = if (titles.isNotEmpty()) translateTexts(titles) else emptyList()
        val translatedDescriptions = if (descriptions.isNotEmpty()) translateTexts(descriptions) else emptyList()

        // Build new list with translated content
        var titleIndex = 0
        var descIndex = 0
        val translatedMangas = page.mangas.map { manga ->
            val newTitle = if (translateTitles && manga.title.isNotBlank()) {
                translatedTitles.getOrElse(titleIndex++) { manga.title }
            } else manga.title

            val newDescription = if (translateDescriptions && !manga.description.isNullOrBlank()) {
                translatedDescriptions.getOrElse(descIndex++) { manga.description }
            } else manga.description

            manga.copy(title = newTitle, description = newDescription)
        }

        return page.copy(mangas = translatedMangas)
    }

    /**
     * Clear the translation cache
     */
    fun clearCache() {
        translationCache.clear()
        Log.info { "$TAG: Translation cache cleared" }
    }

    /**
     * Get cache statistics
     */
    fun getCacheStats(): Pair<Int, Long> {
        val size = translationCache.size
        val oldestEntry = translationCache.values.minOfOrNull { it.timestamp } ?: 0L
        return Pair(size, oldestEntry)
    }
}
