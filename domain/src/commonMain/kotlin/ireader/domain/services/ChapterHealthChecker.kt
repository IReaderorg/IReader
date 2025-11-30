package ireader.domain.services

import ireader.core.source.model.Page
import ireader.core.source.model.Text

/**
 * Service for detecting broken or corrupted chapters
 */
class ChapterHealthChecker {
    
    /**
     * Checks if a chapter is broken based on multiple criteria
     */
    fun isChapterBroken(content: List<Page>): Boolean {
        val textContent = extractTextContent(content)
        
        // Empty content is definitely broken
        if (textContent.isBlank()) return true
        
        // For very short content, only check if it's completely empty
        // This avoids false positives for short chapters or chapter titles
        if (textContent.length < MIN_CONTENT_LENGTH) {
            return false // Short content is not necessarily broken
        }
        
        // Check for scrambled text (low letter ratio)
        // This is more lenient to support non-Latin languages
        if (hasLowLetterRatio(textContent)) return true
        
        return false
    }
    
    /**
     * Gets the specific reason why a chapter is broken
     */
    fun getBreakReason(content: List<Page>): BreakReason? {
        val textContent = extractTextContent(content)
        
        return when {
            textContent.isBlank() -> BreakReason.EMPTY_CONTENT
            textContent.length >= MIN_CONTENT_LENGTH && hasLowLetterRatio(textContent) -> BreakReason.SCRAMBLED_TEXT
            else -> null
        }
    }
    
    /**
     * Extracts text content from Page list
     */
    private fun extractTextContent(content: List<Page>): String {
        return content.filterIsInstance<Text>()
            .joinToString(" ") { it.text }
    }
    
    /**
     * Checks if text has a low ratio of letter characters (indicating scrambled content)
     * Uses Character.isLetter() which supports all Unicode letters including CJK
     */
    private fun hasLowLetterRatio(text: String): Boolean {
        if (text.isEmpty()) return true
        
        // Count meaningful characters: letters, digits, whitespace, and common punctuation
        val meaningfulCount = text.count { char ->
            char.isLetterOrDigit() || char.isWhitespace() || isMeaningfulPunctuation(char)
        }
        
        // If most characters are meaningful, it's probably not scrambled
        val meaningfulRatio = meaningfulCount.toFloat() / text.length
        
        // Very low meaningful ratio indicates scrambled/corrupted text
        return meaningfulRatio < MIN_MEANINGFUL_RATIO
    }
    
    /**
     * Checks if a character is meaningful punctuation (supports multiple languages)
     */
    private fun isMeaningfulPunctuation(char: Char): Boolean {
        return when (char) {
            // Common ASCII punctuation
            '.', ',', '!', '?', ';', ':', '\'', '"', '-', '(', ')', '[', ']', '{', '}' -> true
            // CJK punctuation
            '\u3002', '\uFF0C', '\uFF01', '\uFF1F', '\uFF1B', '\uFF1A' -> true // 。，！？；：
            '\u201C', '\u201D', '\u2018', '\u2019' -> true // ""''
            '\uFF08', '\uFF09', '\u3010', '\u3011' -> true // （）【】
            '\u300C', '\u300D', '\u300E', '\u300F' -> true // 「」『』
            else -> false
        }
    }
    
    companion object {
        // Minimum content length before we check for scrambled text
        // This prevents false positives for short chapters
        private const val MIN_CONTENT_LENGTH = 100
        
        // Minimum ratio of meaningful characters (letters, digits, punctuation)
        // Lowered from 0.5 to 0.3 to support languages with more symbols
        private const val MIN_MEANINGFUL_RATIO = 0.3f
    }
}

/**
 * Enum representing reasons why a chapter might be broken
 */
enum class BreakReason {
    LOW_WORD_COUNT,
    EMPTY_CONTENT,
    SCRAMBLED_TEXT,
    HTTP_ERROR
}
