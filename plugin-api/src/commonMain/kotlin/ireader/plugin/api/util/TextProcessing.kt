package ireader.plugin.api.util

/**
 * Text processing utilities for plugins.
 * Useful for TTS, translation, and AI plugins.
 */
object TextProcessing {
    
    /**
     * Split text into sentences.
     */
    fun splitIntoSentences(text: String): List<String> {
        // Handle common sentence endings
        val pattern = Regex("""(?<=[.!?])\s+(?=[A-Z])""")
        return text.split(pattern).filter { it.isNotBlank() }
    }
    
    /**
     * Split text into paragraphs.
     */
    fun splitIntoParagraphs(text: String): List<String> {
        return text.split(Regex("""\n\s*\n""")).filter { it.isNotBlank() }
    }
    
    /**
     * Split text into chunks of approximately the specified size.
     * Tries to split at sentence boundaries.
     */
    fun splitIntoChunks(text: String, maxChunkSize: Int): List<String> {
        if (text.length <= maxChunkSize) return listOf(text)
        
        val sentences = splitIntoSentences(text)
        val chunks = mutableListOf<String>()
        var currentChunk = StringBuilder()
        
        for (sentence in sentences) {
            if (currentChunk.length + sentence.length > maxChunkSize && currentChunk.isNotEmpty()) {
                chunks.add(currentChunk.toString().trim())
                currentChunk = StringBuilder()
            }
            currentChunk.append(sentence).append(" ")
        }
        
        if (currentChunk.isNotBlank()) {
            chunks.add(currentChunk.toString().trim())
        }
        
        return chunks
    }
    
    /**
     * Estimate word count.
     */
    fun wordCount(text: String): Int {
        return text.split(Regex("""\s+""")).count { it.isNotBlank() }
    }
    
    /**
     * Estimate character count (excluding whitespace).
     */
    fun characterCount(text: String): Int {
        return text.count { !it.isWhitespace() }
    }
    
    /**
     * Estimate reading time in minutes.
     * Based on average reading speed of 200 words per minute.
     */
    fun estimateReadingTimeMinutes(text: String, wordsPerMinute: Int = 200): Int {
        val words = wordCount(text)
        return (words / wordsPerMinute).coerceAtLeast(1)
    }
    
    /**
     * Extract keywords from text (simple implementation).
     */
    fun extractKeywords(text: String, maxKeywords: Int = 10): List<String> {
        val stopWords = setOf(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "as", "is", "was", "are", "were", "been",
            "be", "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "must", "shall", "can", "need", "dare", "ought",
            "used", "it", "its", "this", "that", "these", "those", "i", "you", "he",
            "she", "we", "they", "what", "which", "who", "whom", "whose", "where",
            "when", "why", "how", "all", "each", "every", "both", "few", "more",
            "most", "other", "some", "such", "no", "nor", "not", "only", "own",
            "same", "so", "than", "too", "very", "just", "also"
        )
        
        return text.lowercase()
            .replace(Regex("""[^\w\s]"""), "")
            .split(Regex("""\s+"""))
            .filter { it.length > 3 && it !in stopWords }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(maxKeywords)
            .map { it.key }
    }
    
    /**
     * Normalize whitespace in text.
     */
    fun normalizeWhitespace(text: String): String {
        return text.replace(Regex("""\s+"""), " ").trim()
    }
    
    /**
     * Remove diacritics/accents from text.
     */
    fun removeDiacritics(text: String): String {
        val diacritics = mapOf(
            'á' to 'a', 'à' to 'a', 'â' to 'a', 'ä' to 'a', 'ã' to 'a', 'å' to 'a',
            'é' to 'e', 'è' to 'e', 'ê' to 'e', 'ë' to 'e',
            'í' to 'i', 'ì' to 'i', 'î' to 'i', 'ï' to 'i',
            'ó' to 'o', 'ò' to 'o', 'ô' to 'o', 'ö' to 'o', 'õ' to 'o',
            'ú' to 'u', 'ù' to 'u', 'û' to 'u', 'ü' to 'u',
            'ñ' to 'n', 'ç' to 'c'
        )
        return text.map { diacritics[it.lowercaseChar()] ?: it }.joinToString("")
    }
}

/**
 * Extension functions for String
 */
fun String.sentences(): List<String> = TextProcessing.splitIntoSentences(this)
fun String.paragraphs(): List<String> = TextProcessing.splitIntoParagraphs(this)
fun String.chunks(maxSize: Int): List<String> = TextProcessing.splitIntoChunks(this, maxSize)
fun String.wordCount(): Int = TextProcessing.wordCount(this)
fun String.readingTimeMinutes(): Int = TextProcessing.estimateReadingTimeMinutes(this)
fun String.keywords(max: Int = 10): List<String> = TextProcessing.extractKeywords(this, max)
fun String.normalizeSpaces(): String = TextProcessing.normalizeWhitespace(this)
