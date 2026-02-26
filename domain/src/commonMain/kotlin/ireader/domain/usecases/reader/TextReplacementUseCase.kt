package ireader.domain.usecases.reader

import ireader.core.log.Log
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.data.repository.TextReplacementRepository
import ireader.domain.models.entities.TextReplacement
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Use case for applying text replacements to chapter content.
 * 
 * This provides unified replacement logic for both Reader and TTS screens,
 * allowing users to automatically replace text like:
 * - "khan" → "khaaan"
 * - "THIS IS TEXT" → ""
 * 
 * Replacements are stored in a database table for better management.
 * Replacements are applied BEFORE content filtering for efficiency.
 */
class TextReplacementUseCase(
    private val readerPreferences: ReaderPreferences,
    private val repository: TextReplacementRepository? = null
) {
    
    companion object {
        private const val TAG = "TextReplacement"
        
        // Regex metacharacters used to detect if a pattern is regex or literal
        private val REGEX_META_CHARS = setOf('.', '*', '+', '?', '^', '$', '{', '}', '(', ')', '|', '[', ']', '\\')
        
        /**
         * Helper function to detect if a pattern contains regex metacharacters.
         * Extracted to avoid duplication (Issue #17).
         */
        private fun isRegexPattern(pattern: String): Boolean {
            return pattern.any { it in REGEX_META_CHARS }
        }
    }
    
    /**
     * Apply replacements to a list of Page objects (for Reader screen)
     */
    suspend fun applyReplacementsToPages(pages: List<Page>, bookId: Long? = null): List<Page> {
        val replacements = getEnabledReplacements(bookId)
        
        if (replacements.isEmpty()) {
            return pages
        }
        
        Log.debug { "$TAG: Applying ${replacements.size} replacements to ${pages.size} pages" }
        
        return pages.map { page ->
            when (page) {
                is Text -> {
                    val originalText = page.text
                    val replacedText = applyReplacements(originalText, replacements)
                    if (originalText != replacedText) {
                        Log.debug { "$TAG: Text changed from ${originalText.length} to ${replacedText.length} chars" }
                    }
                    Text(replacedText)
                }
                else -> page // Keep non-text pages as-is
            }
        }
    }
    
    /**
     * Apply replacements to a list of strings (for TTS screen)
     */
    suspend fun applyReplacementsToStrings(content: List<String>, bookId: Long? = null): List<String> {
        val replacements = getEnabledReplacements(bookId)
        
        if (replacements.isEmpty()) {
            return content
        }
        
        Log.debug { "$TAG: Applying ${replacements.size} replacements to ${content.size} strings" }
        
        return content.map { text ->
            applyReplacements(text, replacements)
        }
    }
    
    /**
     * Apply replacements to a single string
     */
    suspend fun applyReplacementsToText(text: String, bookId: Long? = null): String {
        val replacements = getEnabledReplacements(bookId)
        
        if (replacements.isEmpty()) {
            return text
        }
        
        return applyReplacements(text, replacements)
    }
    
    /**
     * Get enabled replacements from repository.
     * Fixed Issue #10: Now properly suspend instead of using runBlocking.
     */
    private suspend fun getEnabledReplacements(bookId: Long? = null): List<TextReplacement> {
        val replacements = if (repository != null) {
            try {
                if (bookId != null) {
                    repository.getEnabledReplacementsForBook(bookId)
                } else {
                    repository.getEnabledGlobalReplacements()
                }
            } catch (e: Exception) {
                Log.warn { "$TAG: Failed to load replacements from repository: ${e.message}" }
                emptyList()
            }
        } else {
            emptyList()
        }
        
        return replacements
    }
    
    /**
     * Apply all replacements to text efficiently.
     * Supports both literal string replacement and regex patterns.
     * Fixed Issue #17: Uses extracted isRegexPattern() helper to avoid duplication.
     */
    private fun applyReplacements(text: String, replacements: List<TextReplacement>): String {
        if (replacements.isEmpty() || text.isEmpty()) {
            return text
        }
        
        var result = text
        
        // Apply each replacement in order
        for (replacement in replacements) {
            try {
                // Use extracted helper function (Issue #17 fix)
                val isRegex = isRegexPattern(replacement.findText)
                
                result = if (isRegex) {
                    // Use regex replacement
                    val regexOptions = if (replacement.caseSensitive) {
                        setOf()
                    } else {
                        setOf(RegexOption.IGNORE_CASE)
                    }
                    val regex = Regex(replacement.findText, regexOptions)
                    regex.replace(result, replacement.replaceText)
                } else {
                    // Use literal string replacement
                    if (replacement.caseSensitive) {
                        result.replace(replacement.findText, replacement.replaceText)
                    } else {
                        result.replace(replacement.findText, replacement.replaceText, ignoreCase = true)
                    }
                }
            } catch (e: Exception) {
                // If regex fails, try literal replacement as fallback
                Log.warn { "$TAG: Regex failed for '${replacement.findText}', using literal replacement: ${e.message}" }
                result = if (replacement.caseSensitive) {
                    result.replace(replacement.findText, replacement.replaceText)
                } else {
                    result.replace(replacement.findText, replacement.replaceText, ignoreCase = true)
                }
            }
        }
        
        return result
    }
    
    /**
     * Invalidate the replacement cache (call when replacements are modified)
     * 
     * **Note:** This method is currently a no-op. Caching was removed to ensure real-time updates
     * from the database. The method is kept for API compatibility with existing code that may
     * call it, but it no longer performs any action.
     * 
     * **Background:** Previously, this use case cached replacements in memory for performance.
     * However, this caused issues with stale data when replacements were modified. The caching
     * logic was removed, and now the repository/database layer handles any necessary caching.
     * 
     * **Future:** This method may be removed in a future version once all callers are verified
     * to not depend on it.
     */
    fun invalidateCache() {
        // No-op: caching removed to ensure real-time updates
    }
    
    /**
     * Test replacements against sample text
     * @return the replaced result
     * Fixed Issue #17: Uses extracted isRegexPattern() helper.
     */
    fun testReplacement(text: String, findText: String, replaceText: String, caseSensitive: Boolean = false): String {
        return try {
            // Use extracted helper function (Issue #17 fix)
            val isRegex = isRegexPattern(findText)
            
            if (isRegex) {
                // Use regex replacement
                val regexOptions = if (caseSensitive) {
                    setOf()
                } else {
                    setOf(RegexOption.IGNORE_CASE)
                }
                val regex = Regex(findText, regexOptions)
                regex.replace(text, replaceText)
            } else {
                // Use literal string replacement
                if (caseSensitive) {
                    text.replace(findText, replaceText)
                } else {
                    text.replace(findText, replaceText, ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            // If regex fails, try literal replacement as fallback
            if (caseSensitive) {
                text.replace(findText, replaceText)
            } else {
                text.replace(findText, replaceText, ignoreCase = true)
            }
        }
    }
    
    // ==================== Repository Operations ====================
    
    /**
     * Get all global replacements as a Flow
     */
    fun getGlobalReplacements(): Flow<List<TextReplacement>>? {
        return repository?.getGlobalReplacements()
    }
    
    /**
     * Get replacements for a specific book as a Flow
     */
    fun getReplacementsForBook(bookId: Long): Flow<List<TextReplacement>>? {
        return repository?.getReplacementsForBook(bookId)
    }
    
    /**
     * Add a new replacement
     */
    suspend fun addReplacement(
        name: String,
        findText: String,
        replaceText: String,
        description: String? = null,
        bookId: Long? = null,
        enabled: Boolean = true,
        caseSensitive: Boolean = false
    ): Long? {
        invalidateCache()
        return repository?.insert(
            TextReplacement(
                bookId = bookId,
                name = name,
                findText = findText,
                replaceText = replaceText,
                description = description,
                enabled = enabled,
                caseSensitive = caseSensitive,
                createdAt = 0, // Will be set by repository
                updatedAt = 0
            )
        )
    }
    
    /**
     * Add a new replacement with a specific ID (for default replacements)
     */
    suspend fun addReplacementWithId(
        id: Long,
        name: String,
        findText: String,
        replaceText: String,
        description: String? = null,
        bookId: Long? = null,
        enabled: Boolean = true,
        caseSensitive: Boolean = false
    ) {
        invalidateCache()
        repository?.insertWithId(
            TextReplacement(
                id = id,
                bookId = bookId,
                name = name,
                findText = findText,
                replaceText = replaceText,
                description = description,
                enabled = enabled,
                caseSensitive = caseSensitive,
                createdAt = 0,
                updatedAt = 0
            )
        )
    }
    
    /**
     * Update an existing replacement
     */
    suspend fun updateReplacement(replacement: TextReplacement) {
        invalidateCache()
        repository?.update(replacement)
    }
    
    /**
     * Toggle a replacement's enabled state
     */
    suspend fun toggleReplacement(id: Long) {
        invalidateCache()
        repository?.toggleEnabled(id)
    }
    
    /**
     * Delete a replacement
     */
    suspend fun deleteReplacement(id: Long) {
        invalidateCache()
        repository?.delete(id)
    }
    
    // ==================== Import/Export Operations ====================
    
    /**
     * Export all text replacements to JSON string
     */
    suspend fun exportToJson(): String {
        val replacements = repository?.let { repo ->
            try {
                // Get the first emission from the Flow
                repo.getGlobalReplacements().first()
            } catch (e: Exception) {
                Log.error(e, "Failed to export replacements")
                emptyList()
            }
        } ?: emptyList()
        
        return kotlinx.serialization.json.Json.encodeToString(replacements)
    }
    
    /**
     * Import text replacements from JSON string
     * @return number of replacements imported
     */
    suspend fun importFromJson(jsonString: String): Result<Int> {
        return try {
            val replacements = kotlinx.serialization.json.Json.decodeFromString<List<TextReplacement>>(jsonString)
            
            var count = 0
            replacements.forEach { replacement ->
                // Skip default replacements (negative IDs)
                if (replacement.id >= 0) {
                    addReplacement(
                        name = replacement.name,
                        findText = replacement.findText,
                        replaceText = replacement.replaceText,
                        description = replacement.description,
                        bookId = replacement.bookId,
                        enabled = replacement.enabled,
                        caseSensitive = replacement.caseSensitive
                    )
                    count++
                }
            }
            
            Result.success(count)
        } catch (e: Exception) {
            Log.error(e, "Failed to import replacements")
            Result.failure(e)
        }
    }
}
