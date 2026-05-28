package ireader.domain.usecases.tts

/**
 * Sanitizes text for Text-to-Speech by removing brackets, special characters,
 * and other elements that shouldn't be read aloud.
 * 
 * **Important**: This sanitizer is ONLY applied when sending text to the TTS engine.
 * The original text with brackets is still displayed in the reader screen.
 * 
 * This ensures TTS engines don't read out:
 * - Round brackets: (note), (TL: translator note)
 * - Square brackets: [TL: translation], [Note: annotation]
 * - Curly braces: {annotation}, {footnote}
 * - Angle brackets: <tag>, <HTML tags>
 * - Asterisks: *, **, ***
 * - Other special formatting characters
 * 
 * The sanitizer preserves:
 * - Regular punctuation (. , ! ? ' " - etc.)
 * - Numbers and letters
 * - Whitespace (normalized)
 * - Unicode characters (CJK, Arabic, etc.)
 * 
 * Usage:
 * ```kotlin
 * val sanitizer = TTSTextSanitizer()
 * val cleanText = sanitizer.sanitize("Hello (world) [note]") // Returns "Hello"
 * ```
 */
class TTSTextSanitizer {
    
    companion object {
        // Regex patterns for different bracket types
        // These handle nested brackets by matching the outermost pair
        
        // Matches content in round brackets: (content)
        // Handles nested brackets: ((nested))
        private val ROUND_BRACKETS = Regex("""\([^()]*(?:\([^()]*\)[^()]*)*\)""")
        
        // Matches content in square brackets: [content]
        // Handles nested brackets: [[nested]]
        private val SQUARE_BRACKETS = Regex("""\[[^\[\]]*(?:\[[^\[\]]*\][^\[\]]*)*\]""")
        
        // Matches content in curly braces: {content}
        // Handles nested braces: {{nested}}
        private val CURLY_BRACES = Regex("""\{[^{}]*(?:\{[^{}]*\}[^{}]*)*\}""")
        
        // Matches content in angle brackets: <content>
        // Handles nested brackets: <nested>
        private val ANGLE_BRACKETS = Regex("""<[^<>]*(?:<[^<>]*>[^<>]*)*>""")
        
        // Remove asterisks (single or multiple)
        private val ASTERISKS = Regex("""\*+""")
        
        // Remove other special formatting characters that shouldn't be read
        // Includes: ~ ` ^ _ | \ / © ® ™ § ¶ † ‡ • … ‰ ′ ″
        private val SPECIAL_CHARS = Regex("""[~`^_|\\/©®™§¶†‡•…‰′″]+""")
        
        // Normalize multiple spaces to single space
        private val MULTIPLE_SPACES = Regex("""\s+""")
        
        // Remove unmatched opening brackets (if any remain after main removal)
        // This catches cases like "Hello (world" where closing bracket is missing
        private val UNMATCHED_OPENING = Regex("""[\(\[\{<].*$""")
        
        // Remove unmatched closing brackets
        // This catches cases like "Hello world)" where opening bracket is missing
        private val UNMATCHED_CLOSING = Regex("""[\)\]\}>]""")
    }
    
    /**
     * Sanitize text for TTS by removing brackets and special characters.
     * 
     * This method:
     * 1. Removes content within brackets (round, square, curly, angle)
     * 2. Removes asterisks and other special formatting characters
     * 3. Cleans up unmatched brackets
     * 4. Normalizes whitespace
     * 
     * **Note**: This is only applied when sending text to TTS engine.
     * The original text with brackets is still displayed in the reader.
     * 
     * @param text The text to sanitize
     * @return Sanitized text safe for TTS engines
     */
    fun sanitize(text: String): String {
        if (text.isEmpty()) return text
        
        var result = text
        
        // Remove brackets and their content (multiple passes for nested brackets)
        // We do multiple passes to handle deeply nested brackets like ((nested))
        for (i in 0 until 3) { // 3 passes should handle most nesting levels
            result = ROUND_BRACKETS.replace(result, "")
            result = SQUARE_BRACKETS.replace(result, "")
            result = CURLY_BRACES.replace(result, "")
            result = ANGLE_BRACKETS.replace(result, "")
        }
        
        // Remove asterisks (used for emphasis or footnotes)
        result = ASTERISKS.replace(result, "")
        
        // Remove other special formatting characters
        result = SPECIAL_CHARS.replace(result, "")
        
        // Clean up any remaining unmatched brackets
        // This handles cases where brackets are not properly closed
        result = UNMATCHED_OPENING.replace(result, "")
        result = UNMATCHED_CLOSING.replace(result, "")
        
        // Normalize whitespace (multiple spaces, tabs, newlines -> single space)
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
