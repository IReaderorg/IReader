package ireader.domain.usecases.glossary

import ireader.domain.data.repository.GlossaryRepository
import ireader.domain.data.repository.GlobalGlossaryRepository

/**
 * Use case for applying glossary terms to text before or after translation.
 * This ensures consistent translation of character names, places, and other terms.
 */
class ApplyGlossaryToTranslationUseCase(
    private val glossaryRepository: GlossaryRepository,
    private val globalGlossaryRepository: GlobalGlossaryRepository? = null
) {
    /**
     * Apply glossary replacements to text before translation.
     * This replaces source terms with placeholders to preserve them during translation.
     * 
     * @param text The text to process
     * @param bookId The book ID for local glossary lookup
     * @param bookKey Optional book key for global glossary lookup
     * @return Pair of processed text and a map of placeholders to original terms
     */
    suspend fun preProcess(
        text: String,
        bookId: Long? = null,
        bookKey: String? = null
    ): Pair<String, Map<String, String>> {
        val glossaryMap = getGlossaryMap(bookId, bookKey)
        if (glossaryMap.isEmpty()) return text to emptyMap()
        
        var processedText = text
        val placeholders = mutableMapOf<String, String>()
        var placeholderIndex = 0
        
        // Sort by length descending to replace longer terms first
        val sortedTerms = glossaryMap.keys.sortedByDescending { it.length }
        
        for (sourceTerm in sortedTerms) {
            if (processedText.contains(sourceTerm, ignoreCase = true)) {
                val placeholder = "[[GLOSS_${placeholderIndex}]]"
                placeholders[placeholder] = sourceTerm
                processedText = processedText.replace(sourceTerm, placeholder, ignoreCase = true)
                placeholderIndex++
            }
        }
        
        return processedText to placeholders
    }
    
    /**
     * Apply glossary replacements to text after translation.
     * This replaces placeholders with the target terms from the glossary.
     * 
     * @param text The translated text with placeholders
     * @param placeholders Map of placeholders to original source terms
     * @param bookId The book ID for local glossary lookup
     * @param bookKey Optional book key for global glossary lookup
     * @return The text with glossary terms applied
     */
    suspend fun postProcess(
        text: String,
        placeholders: Map<String, String>,
        bookId: Long? = null,
        bookKey: String? = null
    ): String {
        if (placeholders.isEmpty()) return text
        
        val glossaryMap = getGlossaryMap(bookId, bookKey)
        var processedText = text
        
        for ((placeholder, sourceTerm) in placeholders) {
            val targetTerm = glossaryMap[sourceTerm] ?: sourceTerm
            processedText = processedText.replace(placeholder, targetTerm)
        }
        
        return processedText
    }
    
    /**
     * Apply glossary directly to translated text (post-translation replacement).
     * This is simpler but may not preserve context as well.
     * 
     * @param text The translated text
     * @param bookId The book ID for local glossary lookup
     * @param bookKey Optional book key for global glossary lookup
     * @return The text with glossary terms applied
     */
    suspend fun applyDirect(
        text: String,
        bookId: Long? = null,
        bookKey: String? = null
    ): String {
        val glossaryMap = getGlossaryMap(bookId, bookKey)
        if (glossaryMap.isEmpty()) return text
        
        var processedText = text
        
        // Sort by length descending to replace longer terms first
        val sortedEntries = glossaryMap.entries.sortedByDescending { it.key.length }
        
        for ((sourceTerm, targetTerm) in sortedEntries) {
            processedText = processedText.replace(sourceTerm, targetTerm, ignoreCase = true)
        }
        
        return processedText
    }
    
    /**
     * Get combined glossary map from both local and global sources.
     */
    private suspend fun getGlossaryMap(bookId: Long?, bookKey: String?): Map<String, String> {
        val result = mutableMapOf<String, String>()
        
        // Get local glossary
        bookId?.let { id ->
            val localEntries = glossaryRepository.getGlossaryByBookId(id)
            localEntries.forEach { entry ->
                result[entry.sourceTerm] = entry.targetTerm
            }
        }
        
        // Get global glossary (overrides local if same term exists)
        bookKey?.let { key ->
            globalGlossaryRepository?.getByBookKey(key)?.forEach { entry ->
                result[entry.sourceTerm] = entry.targetTerm
            }
        }
        
        return result
    }
}
