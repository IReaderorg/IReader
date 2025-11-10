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
        
        return when {
            textContent.isBlank() -> true
            getWordCount(textContent) < MIN_WORD_COUNT -> true
            hasLowAlphaRatio(textContent) -> true
            else -> false
        }
    }
    
    /**
     * Gets the specific reason why a chapter is broken
     */
    fun getBreakReason(content: List<Page>): BreakReason? {
        val textContent = extractTextContent(content)
        
        return when {
            textContent.isBlank() -> BreakReason.EMPTY_CONTENT
            getWordCount(textContent) < MIN_WORD_COUNT -> BreakReason.LOW_WORD_COUNT
            hasLowAlphaRatio(textContent) -> BreakReason.SCRAMBLED_TEXT
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
     * Counts words in text content
     */
    private fun getWordCount(text: String): Int {
        return text.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .size
    }
    
    /**
     * Checks if text has a low ratio of alphabetic characters (indicating scrambled content)
     */
    private fun hasLowAlphaRatio(text: String): Boolean {
        if (text.isEmpty()) return true
        
        val alphaCount = text.count { it.isLetter() }
        val alphaRatio = alphaCount.toFloat() / text.length
        
        return alphaRatio < MIN_ALPHA_RATIO
    }
    
    companion object {
        private const val MIN_WORD_COUNT = 50
        private const val MIN_ALPHA_RATIO = 0.5f
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
