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
        // Remove bracket delimiter characters while preserving their inner content
        private val BRACKET_DELIMITERS = Regex("""[()\[\]{}<>]""")
        
        // Remove asterisks (single or multiple)
        private val ASTERISKS = Regex("""\*+""")
        
        // Remove other special formatting characters that shouldn't be read
        // Includes: ~ ` ^ _ | \ © ® ™ § ¶ † ‡ • … ‰ ′ ″
        private val SPECIAL_CHARS = Regex("""[~`^_|\\©®™§¶†‡•…‰′″]+""")
        
        // Normalize multiple spaces to single space
        private val MULTIPLE_SPACES = Regex("""\s+""")
    }
    
    /**
     * Sanitize text for TTS by removing asterisks, special formatting characters,
     * and bracket delimiters, while PRESERVING the text inside brackets.
     * 
     * @param text The text to sanitize
     * @return Sanitized text safe for TTS engines with bracketed content preserved
     */
    fun sanitize(text: String): String {
        if (text.isEmpty()) return text
        
        var result = text
        
        // Replace bracket delimiter characters with a space so words remain separated
        // e.g. "[Skill: Fireball]" -> " Skill: Fireball " -> "Skill: Fireball"
        result = BRACKET_DELIMITERS.replace(result, " ")
        
        // Remove asterisks (used for emphasis or footnotes)
        result = ASTERISKS.replace(result, " ")
        
        // Remove other special formatting characters
        result = SPECIAL_CHARS.replace(result, " ")
        
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
