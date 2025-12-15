package ireader.domain.community.cloudflare

import kotlin.math.min

/**
 * Text compression utility for community translations.
 * Uses a simple but effective compression for text content.
 * 
 * Strategy:
 * 1. Dictionary-based compression for common words/phrases
 * 2. Run-length encoding for repeated characters
 * 3. Base64 encoding for transport
 */
object TextCompressor {
    
    // Common words/phrases in translations (will be replaced with short codes)
    private val DICTIONARY = listOf(
        "the", "and", "that", "have", "for", "not", "with", "you", "this", "but",
        "his", "from", "they", "were", "been", "have", "said", "each", "which", "their",
        "will", "would", "there", "could", "other", "into", "more", "some", "time", "very",
        "when", "come", "made", "find", "here", "many", "make", "like", "back", "only",
        "over", "such", "year", "take", "also", "just", "know", "people", "than", "first",
        // Common HTML/formatting
        "<p>", "</p>", "<br>", "<br/>", "<div>", "</div>", "<span>", "</span>",
        "\n\n", "  ", "\t",
        // Common punctuation patterns
        ". ", ", ", "! ", "? ", ": ", "; ",
    )
    
    // Escape character for dictionary codes
    private const val ESCAPE_CHAR = '\u0001'
    
    /**
     * Compress text content.
     * Returns compressed bytes that can be stored in R2.
     */
    fun compress(text: String): ByteArray {
        if (text.isEmpty()) return ByteArray(0)
        
        // Step 1: Dictionary replacement
        var compressed = text
        DICTIONARY.forEachIndexed { index, word ->
            val code = "$ESCAPE_CHAR${index.toChar()}"
            compressed = compressed.replace(word, code)
        }
        
        // Step 2: Simple run-length encoding for repeated chars (3+ repeats)
        compressed = runLengthEncode(compressed)
        
        // Return as UTF-8 bytes
        return compressed.encodeToByteArray()
    }
    
    /**
     * Decompress content back to original text.
     */
    fun decompress(data: ByteArray): String {
        if (data.isEmpty()) return ""
        
        var text = data.decodeToString()
        
        // Step 1: Reverse run-length encoding
        text = runLengthDecode(text)
        
        // Step 2: Reverse dictionary replacement
        DICTIONARY.forEachIndexed { index, word ->
            val code = "$ESCAPE_CHAR${index.toChar()}"
            text = text.replace(code, word)
        }
        
        return text
    }
    
    /**
     * Calculate compression ratio.
     * Returns value between 0 and 1 (lower is better compression).
     */
    fun compressionRatio(original: String, compressed: ByteArray): Float {
        if (original.isEmpty()) return 1f
        val originalSize = original.encodeToByteArray().size
        return compressed.size.toFloat() / originalSize.toFloat()
    }
    
    /**
     * Generate content hash for deduplication.
     * Uses a simple but effective hash for text content.
     */
    fun contentHash(text: String): String {
        // Simple hash combining multiple factors
        val normalized = text.trim().lowercase()
        var hash = 0L
        for (char in normalized) {
            hash = 31 * hash + char.code
        }
        // Add length to reduce collisions
        hash = hash xor (normalized.length.toLong() shl 32)
        return hash.toString(16).padStart(16, '0')
    }
    
    /**
     * Generate book hash for grouping translations.
     */
    fun bookHash(title: String, author: String): String {
        val normalized = "${title.trim().lowercase()}|${author.trim().lowercase()}"
        var hash = 0L
        for (char in normalized) {
            hash = 31 * hash + char.code
        }
        return hash.toString(16).padStart(16, '0')
    }
    
    // Run-length encoding for repeated characters
    private fun runLengthEncode(text: String): String {
        if (text.length < 4) return text
        
        val result = StringBuilder()
        var i = 0
        
        while (i < text.length) {
            val char = text[i]
            var count = 1
            
            // Count consecutive same characters
            while (i + count < text.length && text[i + count] == char && count < 255) {
                count++
            }
            
            if (count >= 4) {
                // Encode as: ESCAPE_CHAR + count + char
                result.append('\u0002')
                result.append(count.toChar())
                result.append(char)
            } else {
                // Keep original
                repeat(count) { result.append(char) }
            }
            
            i += count
        }
        
        return result.toString()
    }
    
    // Run-length decoding
    private fun runLengthDecode(text: String): String {
        val result = StringBuilder()
        var i = 0
        
        while (i < text.length) {
            if (text[i] == '\u0002' && i + 2 < text.length) {
                val count = text[i + 1].code
                val char = text[i + 2]
                repeat(count) { result.append(char) }
                i += 3
            } else {
                result.append(text[i])
                i++
            }
        }
        
        return result.toString()
    }
}
