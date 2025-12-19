package ireader.domain.usecases.reader

import ireader.core.log.Log
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.data.repository.ContentFilterRepository
import ireader.domain.models.entities.ContentFilter
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

/**
 * Use case for filtering unwanted content from chapters using regex patterns.
 * 
 * This provides unified filtering logic for both Reader and TTS screens,
 * allowing users to remove annoying text like:
 * - Navigation hints ("Use arrow keys to PREV/NEXT chapter")
 * - Website promotions ("Read more at example.com")
 * - Translator notes in brackets
 * - Any other repetitive unwanted text
 * 
 * Patterns are stored in a database table for better management.
 */
class ContentFilterUseCase(
    private val readerPreferences: ReaderPreferences,
    private val repository: ContentFilterRepository? = null
) {
    
    companion object {
        private const val TAG = "ContentFilter"
    }
    
    // Cache compiled patterns for performance
    private var cachedPatterns: List<Regex>? = null
    private var cacheBookId: Long? = null
    
    /**
     * Filter a list of Page objects (for Reader screen)
     */
    fun filterPages(pages: List<Page>, bookId: Long? = null): List<Page> {
        if (!readerPreferences.contentFilterEnabled().get()) {
            return pages
        }
        
        val patterns = getCompiledPatterns(bookId)
        if (patterns.isEmpty()) {
            return pages
        }
        
        return pages.mapNotNull { page ->
            when (page) {
                is Text -> {
                    val filteredText = applyFilters(page.text, patterns)
                    if (filteredText.isBlank()) {
                        null // Remove empty paragraphs after filtering
                    } else {
                        Text(filteredText)
                    }
                }
                else -> page // Keep non-text pages as-is
            }
        }
    }
    
    /**
     * Filter a list of strings (for TTS screen)
     */
    fun filterStrings(content: List<String>, bookId: Long? = null): List<String> {
        if (!readerPreferences.contentFilterEnabled().get()) {
            return content
        }
        
        val patterns = getCompiledPatterns(bookId)
        if (patterns.isEmpty()) {
            return content
        }
        
        return content.mapNotNull { text ->
            val filteredText = applyFilters(text, patterns)
            if (filteredText.isBlank()) {
                null // Remove empty paragraphs after filtering
            } else {
                filteredText
            }
        }
    }
    
    /**
     * Filter a single string
     */
    fun filterText(text: String, bookId: Long? = null): String {
        if (!readerPreferences.contentFilterEnabled().get()) {
            return text
        }
        
        val patterns = getCompiledPatterns(bookId)
        if (patterns.isEmpty()) {
            return text
        }
        
        return applyFilters(text, patterns)
    }
    
    /**
     * Get compiled regex patterns from repository or preferences (fallback)
     */
    private fun getCompiledPatterns(bookId: Long? = null): List<Regex> {
        // Check cache first
        if (cachedPatterns != null && cacheBookId == bookId) {
            return cachedPatterns!!
        }
        
        val patterns = if (repository != null) {
            // Use repository if available
            try {
                val filters = runBlocking {
                    if (bookId != null) {
                        repository.getEnabledPatternsForBook(bookId)
                    } else {
                        repository.getEnabledGlobalPatterns()
                    }
                }
                filters.mapNotNull { filter ->
                    try {
                        Regex(filter.pattern, RegexOption.IGNORE_CASE)
                    } catch (e: Exception) {
                        Log.warn { "$TAG: Invalid regex pattern: ${filter.pattern} - ${e.message}" }
                        null
                    }
                }
            } catch (e: Exception) {
                Log.warn { "$TAG: Failed to load patterns from repository: ${e.message}" }
                emptyList()
            }
        } else {
            // Fallback to preferences (legacy)
            val patternsString = readerPreferences.contentFilterPatterns().get()
            if (patternsString.isBlank()) {
                emptyList()
            } else {
                patternsString
                    .split("\n")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .mapNotNull { pattern ->
                        try {
                            Regex(pattern, RegexOption.IGNORE_CASE)
                        } catch (e: Exception) {
                            Log.warn { "$TAG: Invalid regex pattern: $pattern - ${e.message}" }
                            null
                        }
                    }
            }
        }
        
        // Update cache
        cachedPatterns = patterns
        cacheBookId = bookId
        
        return patterns
    }
    
    /**
     * Invalidate the pattern cache (call when patterns are modified)
     */
    fun invalidateCache() {
        cachedPatterns = null
        cacheBookId = null
    }
    
    /**
     * Apply all filter patterns to text
     */
    private fun applyFilters(text: String, patterns: List<Regex>): String {
        var result = text
        for (pattern in patterns) {
            result = pattern.replace(result, "")
        }
        return result.trim()
    }
    
    /**
     * Validate a regex pattern
     * @return null if valid, error message if invalid
     */
    fun validatePattern(pattern: String): String? {
        return try {
            Regex(pattern)
            null
        } catch (e: Exception) {
            e.message ?: "Invalid regex pattern"
        }
    }
    
    /**
     * Test patterns against sample text
     * @return the filtered result
     */
    fun testPatterns(text: String, patterns: String): String {
        val compiledPatterns = patterns
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { pattern ->
                try {
                    Regex(pattern, RegexOption.IGNORE_CASE)
                } catch (e: Exception) {
                    null
                }
            }
        
        return applyFilters(text, compiledPatterns)
    }
    
    // ==================== Repository Operations ====================
    
    /**
     * Get all global patterns as a Flow
     */
    fun getGlobalPatterns(): Flow<List<ContentFilter>>? {
        return repository?.getGlobalPatterns()
    }
    
    /**
     * Get patterns for a specific book as a Flow
     */
    fun getPatternsForBook(bookId: Long): Flow<List<ContentFilter>>? {
        return repository?.getPatternsForBook(bookId)
    }
    
    /**
     * Add a new filter pattern
     */
    suspend fun addPattern(
        name: String,
        pattern: String,
        description: String? = null,
        bookId: Long? = null,
        enabled: Boolean = true
    ): Long? {
        invalidateCache()
        return repository?.insert(
            ContentFilter(
                bookId = bookId,
                name = name,
                pattern = pattern,
                description = description,
                enabled = enabled,
                createdAt = 0, // Will be set by repository
                updatedAt = 0
            )
        )
    }
    
    /**
     * Update an existing filter pattern
     */
    suspend fun updatePattern(filter: ContentFilter) {
        invalidateCache()
        repository?.update(filter)
    }
    
    /**
     * Toggle a pattern's enabled state
     */
    suspend fun togglePattern(id: Long) {
        invalidateCache()
        repository?.toggleEnabled(id)
    }
    
    /**
     * Delete a filter pattern
     */
    suspend fun deletePattern(id: Long) {
        invalidateCache()
        repository?.delete(id)
    }
    
    /**
     * Initialize preset patterns
     */
    suspend fun initializePresets() {
        repository?.initializePresets()
    }
}
