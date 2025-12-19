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
        
        // Hardcoded exact strings to always remove (common navigation hints)
        private val HARDCODED_EXACT_STRINGS = listOf(
            "Use arrow keys (or A / D) to PREV/NEXT chapter",
            "Use arrow keys (or A/D) to PREV/NEXT chapter",
            "Use arrow keys to PREV/NEXT chapter",
            "← Previous Chapter",
            "Next Chapter →",
            "Previous Chapter | Next Chapter",
            "PREV | NEXT",
            "« Previous Chapter",
            "Next Chapter »"
        )
        
        // Regex patterns for more flexible matching of common navigation hints
        private val HARDCODED_REGEX_PATTERNS = listOf(
            // Matches "Use arrow keys (or A / D) to PREV/NEXT chapter" with various spacing
            Regex("Use\\s+arrow\\s+keys\\s*\\(?or\\s*A\\s*/\\s*D\\)?\\s*to\\s*PREV\\s*/\\s*NEXT\\s*chapter", RegexOption.IGNORE_CASE),
            // Matches any "arrow keys" navigation hint
            Regex("Use\\s+arrow\\s+keys.*?(?:PREV|NEXT|chapter)", RegexOption.IGNORE_CASE),
            // Matches "← Previous" or "Next →" patterns
            Regex("[←«]\\s*Previous\\s*(?:Chapter)?", RegexOption.IGNORE_CASE),
            Regex("Next\\s*(?:Chapter)?\\s*[→»]", RegexOption.IGNORE_CASE)
        )
    }
    
    // Cache compiled patterns for performance  
    private var cachedPatterns: List<Regex>? = null
    private var cachedExactStrings: List<String>? = null
    private var cacheBookId: Long? = null
    
    /**
     * Filter a list of Page objects (for Reader screen)
     * Note: Hardcoded exact strings are ALWAYS applied regardless of contentFilterEnabled setting
     */
    fun filterPages(pages: List<Page>, bookId: Long? = null): List<Page> {
        val filterEnabled = readerPreferences.contentFilterEnabled().get()
        
        // Get patterns - hardcoded strings are always included
        val (patterns, exactStrings) = getCompiledPatternsAndStrings(bookId, forceHardcoded = true)
        
        // If filter is disabled, only apply hardcoded exact strings
        val effectivePatterns = if (filterEnabled) patterns else emptyList()
        val effectiveExactStrings = if (filterEnabled) exactStrings else HARDCODED_EXACT_STRINGS.toList()
        
        if (effectivePatterns.isEmpty() && effectiveExactStrings.isEmpty()) {
            return pages
        }
        
        Log.debug { "$TAG: Filtering ${pages.size} pages with ${effectivePatterns.size} patterns and ${effectiveExactStrings.size} exact strings" }
        
        return pages.mapNotNull { page ->
            when (page) {
                is Text -> {
                    val originalText = page.text
                    val filteredText = applyFilters(originalText, effectivePatterns, effectiveExactStrings)
                    if (originalText != filteredText) {
                        Log.debug { "$TAG: Filtered text changed from ${originalText.length} to ${filteredText.length} chars" }
                    }
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
     * Note: Hardcoded exact strings are ALWAYS applied regardless of contentFilterEnabled setting
     */
    fun filterStrings(content: List<String>, bookId: Long? = null): List<String> {
        val filterEnabled = readerPreferences.contentFilterEnabled().get()
        
        // Get patterns - hardcoded strings are always included
        val (patterns, exactStrings) = getCompiledPatternsAndStrings(bookId, forceHardcoded = true)
        
        // If filter is disabled, only apply hardcoded exact strings
        val effectivePatterns = if (filterEnabled) patterns else emptyList()
        val effectiveExactStrings = if (filterEnabled) exactStrings else HARDCODED_EXACT_STRINGS.toList()
        
        if (effectivePatterns.isEmpty() && effectiveExactStrings.isEmpty()) {
            return content
        }
        
        Log.debug { "$TAG: Filtering ${content.size} strings with ${effectivePatterns.size} patterns and ${effectiveExactStrings.size} exact strings" }
        
        return content.mapNotNull { text ->
            val filteredText = applyFilters(text, effectivePatterns, effectiveExactStrings)
            if (filteredText.isBlank()) {
                null // Remove empty paragraphs after filtering
            } else {
                filteredText
            }
        }
    }
    
    /**
     * Filter a single string
     * Note: Hardcoded exact strings are ALWAYS applied regardless of contentFilterEnabled setting
     */
    fun filterText(text: String, bookId: Long? = null): String {
        val filterEnabled = readerPreferences.contentFilterEnabled().get()
        
        // Get patterns - hardcoded strings are always included
        val (patterns, exactStrings) = getCompiledPatternsAndStrings(bookId, forceHardcoded = true)
        
        // If filter is disabled, only apply hardcoded exact strings
        val effectivePatterns = if (filterEnabled) patterns else emptyList()
        val effectiveExactStrings = if (filterEnabled) exactStrings else HARDCODED_EXACT_STRINGS.toList()
        
        if (effectivePatterns.isEmpty() && effectiveExactStrings.isEmpty()) {
            return text
        }
        
        return applyFilters(text, effectivePatterns, effectiveExactStrings)
    }
    
    /**
     * Get compiled regex patterns and exact strings from repository or preferences (fallback)
     * Returns a Pair of (regex patterns, exact strings to remove)
     * @param forceHardcoded If true, always include hardcoded strings even if cache exists
     */
    private fun getCompiledPatternsAndStrings(bookId: Long? = null, forceHardcoded: Boolean = false): Pair<List<Regex>, List<String>> {
        // Check cache first (but always include hardcoded strings)
        if (cachedPatterns != null && cachedExactStrings != null && cacheBookId == bookId && !forceHardcoded) {
            return Pair(cachedPatterns!!, cachedExactStrings!!)
        }
        
        val regexPatterns = mutableListOf<Regex>()
        val exactStrings = mutableListOf<String>()
        
        // Always add hardcoded exact strings
        exactStrings.addAll(HARDCODED_EXACT_STRINGS)
        Log.debug { "$TAG: Added ${HARDCODED_EXACT_STRINGS.size} hardcoded exact strings" }
        
        if (repository != null) {
            // Use repository if available
            try {
                val filters = runBlocking {
                    if (bookId != null) {
                        repository.getEnabledPatternsForBook(bookId)
                    } else {
                        repository.getEnabledGlobalPatterns()
                    }
                }
                for (filter in filters) {
                    try {
                        regexPatterns.add(Regex(filter.pattern, RegexOption.IGNORE_CASE))
                    } catch (e: Exception) {
                        // If regex is invalid, treat it as exact string match
                        Log.warn { "$TAG: Invalid regex, using as exact string: ${filter.pattern}" }
                        exactStrings.add(filter.pattern)
                    }
                }
            } catch (e: Exception) {
                Log.warn { "$TAG: Failed to load patterns from repository: ${e.message}" }
            }
        } else {
            // Fallback to preferences (legacy)
            val patternsString = readerPreferences.contentFilterPatterns().get()
            if (patternsString.isNotBlank()) {
                patternsString
                    .split("\n")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .forEach { pattern ->
                        try {
                            regexPatterns.add(Regex(pattern, RegexOption.IGNORE_CASE))
                        } catch (e: Exception) {
                            // If regex is invalid, treat it as exact string match
                            Log.warn { "$TAG: Invalid regex, using as exact string: $pattern" }
                            exactStrings.add(pattern)
                        }
                    }
            }
        }
        
        // Update cache
        cachedPatterns = regexPatterns
        cachedExactStrings = exactStrings
        cacheBookId = bookId
        
        return Pair(regexPatterns, exactStrings)
    }
    
    /**
     * Invalidate the pattern cache (call when patterns are modified)
     */
    fun invalidateCache() {
        cachedExactStrings = null
        cachedPatterns = null
        cacheBookId = null
    }
    
    /**
     * Apply all filter patterns and exact strings to text
     */
    private fun applyFilters(text: String, patterns: List<Regex>, exactStrings: List<String> = emptyList()): String {
        var result = text
        
        // First apply hardcoded regex patterns (most flexible matching)
        for (pattern in HARDCODED_REGEX_PATTERNS) {
            result = pattern.replace(result, "")
        }
        
        // Then apply exact string removal (case-insensitive)
        // Also handle variations in whitespace by converting exact strings to flexible regex
        for (exactString in exactStrings) {
            // Direct replacement first
            result = result.replace(exactString, "", ignoreCase = true)
            
            // Also try regex with flexible whitespace (handles multiple spaces, tabs, etc.)
            try {
                val flexiblePattern = Regex.escape(exactString)
                    .replace("\\ ", "\\s+")  // Allow any whitespace between words
                    .replace("\\(", "\\(?")  // Make parentheses optional
                    .replace("\\)", "\\)?")
                val flexibleRegex = Regex(flexiblePattern, RegexOption.IGNORE_CASE)
                result = flexibleRegex.replace(result, "")
            } catch (e: Exception) {
                // Ignore regex errors, direct replacement already attempted
            }
        }
        
        // Then apply user-defined regex patterns
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
        val regexPatterns = mutableListOf<Regex>()
        val exactStrings = mutableListOf<String>()
        
        // Add hardcoded strings
        exactStrings.addAll(HARDCODED_EXACT_STRINGS)
        
        patterns
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { pattern ->
                try {
                    regexPatterns.add(Regex(pattern, RegexOption.IGNORE_CASE))
                } catch (e: Exception) {
                    // Invalid regex, use as exact string
                    exactStrings.add(pattern)
                }
            }
        
        return applyFilters(text, regexPatterns, exactStrings)
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
