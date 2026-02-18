package ireader.domain.usecases.tts

/**
 * Sanitizes text for Text-to-Speech by removing brackets, special characters,
 * and other elements that shouldn't be read aloud.
 * 
 * This ensures TTS engines don't read out:
 * - Round brackets: (note)
 * - Square brackets: [TL: translation]
 * - Curly braces: {annotation}
 * - Angle brackets: <tag>
 * - Asterisks: *
 * 
 * The sanitizer preserves:
 * - Regular punctuation (. , ! ? ' " - etc.)
 * - Numbers and letters
 * - Whitespace (normalized)
 */
class TTSTextSanitizer {
    
    companion object {
        // Regex patterns for different bracket types
        // These handle nested brackets by matching the outermost pair
        private val ROUND_BRACKETS = Regex("""\([^()]*(?:\([^()]*\)[^()]*)*\)""")
        private val SQUARE_BRACKETS = Regex("""\[[^\[\]]*(?:\[[^\[\]]*\][^\[\]]*)*\]""")
        private val CURLY_BRACES = Regex("""\{[^{}]*(?:\{[^{}]*\}[^{}]*)*\}""")
        private val ANGLE_BRACKETS = Regex("""<[^<>]*(?:<[^<>]*>[^<>]*)*>""")
        
        // Remove asterisks
        private val ASTERISKS = Regex("""\*+""")
        
        // Normalize multiple spaces to single space
        private val MULTIPLE_SPACES = Regex("""\s+""")
        
        // Remove unmatched opening brackets (if any remain after main removal)
        private val UNMATCHED_OPENING = Regex("""[\(\[\{<].*$""")
        
        // Remove unmatched closing brackets
        private val UNMATCHED_CLOSING = Regex("""[\)\]\}>]""")
    }
    
    /**
     * Sanitize text for TTS by removing brackets and special characters.
     * 
     * @param text The text to sanitize
     * @return Sanitized text safe for TTS engines
     */
    fun sanitize(text: String): String {
        if (text.isEmpty()) return text
        
        var result = text
        
        // Remove brackets and their content (multiple passes for nested brackets)
        // We do multiple passes to handle deeply nested brackets
        for (i in 0 until 3) { // 3 passes should handle most nesting levels
            result = ROUND_BRACKETS.replace(result, "")
            result = SQUARE_BRACKETS.replace(result, "")
            result = CURLY_BRACES.replace(result, "")
            result = ANGLE_BRACKETS.replace(result, "")
        }
        
        // Remove asterisks
        result = ASTERISKS.replace(result, "")
        
        // Clean up any remaining unmatched brackets
        result = UNMATCHED_OPENING.replace(result, "")
        result = UNMATCHED_CLOSING.replace(result, "")
        
        // Normalize whitespace
        result = MULTIPLE_SPACES.replace(result, " ")
        
        // Trim leading/trailing whitespace
        return result.trim()
    }
    
    /**
     * Sanitize a list of text strings for TTS.
     * 
     * @param texts List of texts to sanitize
     * @return List of sanitized texts, with empty strings removed
     */
    fun sanitizeList(texts: List<String>): List<String> {
        return texts
            .map { sanitize(it) }
            .filter { it.isNotBlank() }
    }
}
