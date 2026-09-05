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

        /**
         * Helper function to detect if a pattern contains regex constructs or is meant to be a regex.
         * Distinguishes intentional regex patterns from common literal strings with punctuation.
         */
        fun isRegexPattern(pattern: String): Boolean {
            val trimmed = pattern.trim()
            if (trimmed.isEmpty()) return false

            // Explicit slash-delimited regex: /pattern/ or /pattern/i
            if (trimmed.length >= 2 && trimmed.startsWith("/") && trimmed.lastIndexOf('/') > 0) {
                return true
            }
            // Standard regex character classes: \d, \D, \w, \W, \s, \S, \b, \B
            if (Regex("""\\[dDwWsSbB]""").containsMatchIn(pattern)) {
                return true
            }
            // Regex wildcards and repetition quantifiers: .*, .+, .?, .{, *?, +?
            if (pattern.contains(".*") || pattern.contains(".+") || pattern.contains(".?") ||
                pattern.contains("*?") || pattern.contains("+?")) {
                return true
            }
            // Regex groups and assertions: (?:, (?=, (?!, (?<=, (?<!, (?i
            if (pattern.contains("(?:") || pattern.contains("(?=") || pattern.contains("(?!") ||
                pattern.contains("(?<=") || pattern.contains("(?<!") || pattern.contains("(?i")) {
                return true
            }
            // Start/End line anchors: ^ at start, $ at end (not literal $100 price)
            if (pattern.startsWith("^") || (pattern.endsWith("$") && !pattern.startsWith("$"))) {
                return true
            }
            // Character range inside brackets: e.g. [a-z], [0-9], [A-Z], [^
            if (Regex("""\[\^|\[[a-zA-Z0-9]-[a-zA-Z0-9]\]""").containsMatchIn(pattern)) {
                return true
            }
            // Regex alternation: e.g. foo|bar
            if (pattern.contains("|")) {
                return true
            }
            return false
        }
    }

    /**
     * Pre-compiled replacement rule ready for batch evaluation.
     * Caches compiled Regex so it is not re-created for each paragraph or page.
     */
    data class PreparedReplacement(
        val findText: String,
        val replaceText: String,
        val caseSensitive: Boolean,
        val regex: Regex? = null
    )

    private val cacheLock = Any()
    private val preparedCache = mutableMapOf<Long?, List<PreparedReplacement>>()

    /**
     * Prepare a single replacement rule into a compiled matcher.
     */
    fun prepareReplacement(replacement: TextReplacement): PreparedReplacement {
        val pattern = replacement.findText
        val trimmed = pattern.trim()

        // 1. Check for slash-delimited regex: /pattern/flags
        if (trimmed.length >= 2 && trimmed.startsWith("/") && trimmed.lastIndexOf('/') > 0) {
            val lastSlash = trimmed.lastIndexOf('/')
            val regexBody = trimmed.substring(1, lastSlash)
            val flagsStr = trimmed.substring(lastSlash + 1)
            val options = mutableSetOf<RegexOption>()
            if (flagsStr.contains('i', ignoreCase = true) || !replacement.caseSensitive) {
                options.add(RegexOption.IGNORE_CASE)
            }
            if (flagsStr.contains('m', ignoreCase = true)) {
                options.add(RegexOption.MULTILINE)
            }
            try {
                val regex = Regex(regexBody, options)
                return PreparedReplacement(
                    findText = pattern,
                    replaceText = replacement.replaceText,
                    caseSensitive = replacement.caseSensitive,
                    regex = regex
                )
            } catch (e: Exception) {
                Log.warn { "$TAG: Failed to compile slash-delimited regex '$pattern': ${e.message}" }
            }
        }

        // 2. Check if it's likely a regex pattern
        if (isRegexPattern(pattern)) {
            val options = if (replacement.caseSensitive) {
                emptySet()
            } else {
                setOf(RegexOption.IGNORE_CASE)
            }
            try {
                val regex = Regex(pattern, options)
                return PreparedReplacement(
                    findText = pattern,
                    replaceText = replacement.replaceText,
                    caseSensitive = replacement.caseSensitive,
                    regex = regex
                )
            } catch (e: Exception) {
                Log.warn { "$TAG: Regex compilation failed for '$pattern', falling back to literal: ${e.message}" }
            }
        }

        // 3. Literal replacement
        return PreparedReplacement(
            findText = pattern,
            replaceText = replacement.replaceText,
            caseSensitive = replacement.caseSensitive,
            regex = null
        )
    }

    /**
     * Get pre-compiled replacements for a book, using in-memory cache when available.
     */
    private suspend fun getPreparedReplacements(bookId: Long?): List<PreparedReplacement> {
        synchronized(cacheLock) {
            preparedCache[bookId]?.let { return it }
        }
        val rawReplacements = getEnabledReplacements(bookId)
        val prepared = rawReplacements.map { prepareReplacement(it) }
        synchronized(cacheLock) {
            preparedCache[bookId] = prepared
        }
        return prepared
    }

    /**
     * Apply replacements to a list of Page objects (for Reader screen).
     * Pre-compiles matchers once for all pages instead of recompiling per paragraph.
     */
    suspend fun applyReplacementsToPages(pages: List<Page>, bookId: Long? = null): List<Page> {
        val prepared = getPreparedReplacements(bookId)

        if (prepared.isEmpty()) {
            return pages
        }

        Log.debug { "$TAG: Applying ${prepared.size} precompiled replacements to ${pages.size} pages" }

        return pages.map { page ->
            when (page) {
                is Text -> {
                    val originalText = page.text
                    val replacedText = applyPreparedReplacements(originalText, prepared)
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
     * Apply replacements to a list of strings (for TTS screen).
     */
    suspend fun applyReplacementsToStrings(content: List<String>, bookId: Long? = null): List<String> {
        val prepared = getPreparedReplacements(bookId)

        if (prepared.isEmpty()) {
            return content
        }

        Log.debug { "$TAG: Applying ${prepared.size} precompiled replacements to ${content.size} strings" }

        return content.map { text ->
            applyPreparedReplacements(text, prepared)
        }
    }

    /**
     * Apply replacements to a single string
     */
    suspend fun applyReplacementsToText(text: String, bookId: Long? = null): String {
        val prepared = getPreparedReplacements(bookId)

        if (prepared.isEmpty()) {
            return text
        }

        return applyPreparedReplacements(text, prepared)
    }

    /**
     * Get enabled replacements from repository.
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
     * Apply precompiled replacements to text efficiently.
     * Uses Regex for regex patterns and fast native string replacement for literal rules.
     */
    private fun applyPreparedReplacements(text: String, prepared: List<PreparedReplacement>): String {
        if (prepared.isEmpty() || text.isEmpty()) {
            return text
        }

        var result = text
        for (rep in prepared) {
            result = if (rep.regex != null) {
                rep.regex.replace(result, rep.replaceText)
            } else {
                if (rep.caseSensitive) {
                    result.replace(rep.findText, rep.replaceText)
                } else {
                    result.replace(rep.findText, rep.replaceText, ignoreCase = true)
                }
            }
        }
        return result
    }

    /**
     * Invalidate the replacement cache (call when replacements are modified)
     */
    fun invalidateCache() {
        synchronized(cacheLock) {
            preparedCache.clear()
        }
    }

    /**
     * Test replacements against sample text
     * @return the replaced result
     */
    fun testReplacement(text: String, findText: String, replaceText: String, caseSensitive: Boolean = false): String {
        val prepared = prepareReplacement(
            TextReplacement(
                name = "Test",
                findText = findText,
                replaceText = replaceText,
                caseSensitive = caseSensitive,
                createdAt = 0L,
                updatedAt = 0L
            )
        )
        return applyPreparedReplacements(text, listOf(prepared))
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
