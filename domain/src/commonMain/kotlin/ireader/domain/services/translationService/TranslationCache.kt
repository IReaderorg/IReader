package ireader.domain.services.translationService

import ireader.core.utils.LruCache

/**
 * Cache for translated paragraphs to avoid re-translating the same text.
 * Uses LRU eviction policy with a maximum size of 1000 entries.
 */
class TranslationCache(private val maxSize: Int = 1000) {
    
    private val cache = LruCache<String, String>(maxSize)
    
    /**
     * Get cached translation for a paragraph.
     * Returns null if not cached.
     */
    suspend fun get(sourceText: String, sourceLang: String, targetLang: String): String? {
        val key = buildKey(sourceText, sourceLang, targetLang)
        return cache.get(key)
    }
    
    /**
     * Store a translation in the cache.
     */
    suspend fun put(sourceText: String, sourceLang: String, targetLang: String, translatedText: String) {
        val key = buildKey(sourceText, sourceLang, targetLang)
        cache.put(key, translatedText)
    }
    
    /**
     * Check if a translation is cached.
     */
    suspend fun contains(sourceText: String, sourceLang: String, targetLang: String): Boolean {
        val key = buildKey(sourceText, sourceLang, targetLang)
        return cache.get(key) != null
    }
    
    /**
     * Clear all cached translations.
     */
    suspend fun clear() {
        cache.clear()
    }
    
    /**
     * Get the number of cached entries.
     */
    suspend fun size(): Int = cache.size()
    
    /**
     * Build a cache key from source text and language pair.
     * Includes a portion of the source text to reduce hash collision risk.
     */
    private fun buildKey(sourceText: String, sourceLang: String, targetLang: String): String {
        // Use hash of source text combined with a prefix to reduce collision risk
        val textHash = sourceText.hashCode().toString()
        val textPrefix = sourceText.take(50).replace("\n", " ")
        return "${sourceLang}_${targetLang}_${textPrefix}_${textHash}"
    }
}
