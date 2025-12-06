package ireader.domain.services.tts_service

/**
 * TTS Text Merger - Merges multiple paragraphs into larger chunks for better TTS performance
 * 
 * Benefits:
 * - Fewer requests to remote TTS servers
 * - More natural reading flow
 * - Better audio continuity
 * 
 * The merger tracks which original paragraphs are included in each merged chunk
 * so the UI can highlight the correct paragraph during playback.
 */
object TTSTextMerger {
    
    /**
     * Result of merging paragraphs
     * @param mergedText The combined text to send to TTS engine
     * @param originalParagraphIndices List of original paragraph indices included in this chunk
     * @param wordCount Total word count in the merged text
     */
    data class MergedChunk(
        val mergedText: String,
        val originalParagraphIndices: List<Int>,
        val wordCount: Int
    ) {
        val startParagraph: Int get() = originalParagraphIndices.firstOrNull() ?: 0
        val endParagraph: Int get() = originalParagraphIndices.lastOrNull() ?: 0
        val paragraphCount: Int get() = originalParagraphIndices.size
    }
    
    /**
     * Merge paragraphs into chunks based on target word count
     * 
     * Each paragraph belongs to exactly ONE chunk to ensure proper navigation.
     * Chunks may slightly exceed targetWordCount to keep paragraphs intact.
     * 
     * @param paragraphs List of original paragraphs
     * @param targetWordCount Target number of words per merged chunk (0 = no merging)
     * @param includeRemainder Deprecated - kept for API compatibility but ignored
     * @return List of merged chunks with tracking info
     */
    fun mergeParagraphs(
        paragraphs: List<String>,
        targetWordCount: Int,
        @Suppress("UNUSED_PARAMETER") includeRemainder: Boolean = false
    ): List<MergedChunk> {
        if (targetWordCount <= 0 || paragraphs.isEmpty()) {
            // No merging - return each paragraph as its own chunk
            return paragraphs.mapIndexed { index, text ->
                MergedChunk(
                    mergedText = text,
                    originalParagraphIndices = listOf(index),
                    wordCount = countWords(text)
                )
            }
        }
        
        val result = mutableListOf<MergedChunk>()
        var currentText = StringBuilder()
        var currentIndices = mutableListOf<Int>()
        var currentWordCount = 0
        
        for ((index, paragraph) in paragraphs.withIndex()) {
            val paragraphWords = countWords(paragraph)
            
            // If adding this paragraph would exceed target AND we already have content,
            // finalize current chunk first (keep paragraphs intact - no splitting)
            if (currentWordCount > 0 && currentWordCount + paragraphWords > targetWordCount) {
                // Finalize current chunk
                result.add(MergedChunk(
                    mergedText = currentText.toString(),
                    originalParagraphIndices = currentIndices.toList(),
                    wordCount = currentWordCount
                ))
                
                // Start new chunk with this paragraph
                currentText = StringBuilder(paragraph)
                currentIndices = mutableListOf(index)
                currentWordCount = paragraphWords
            } else {
                // Add paragraph to current chunk
                if (currentText.isNotEmpty()) currentText.append("\n\n")
                currentText.append(paragraph)
                currentIndices.add(index)
                currentWordCount += paragraphWords
            }
        }
        
        // Don't forget the last chunk
        if (currentText.isNotEmpty()) {
            result.add(MergedChunk(
                mergedText = currentText.toString(),
                originalParagraphIndices = currentIndices.toList(),
                wordCount = currentWordCount
            ))
        }
        
        return result
    }
    
    /**
     * Find which merged chunk contains a specific original paragraph index
     */
    fun findChunkForParagraph(chunks: List<MergedChunk>, paragraphIndex: Int): Int {
        return chunks.indexOfFirst { paragraphIndex in it.originalParagraphIndices }
            .takeIf { it >= 0 } ?: 0
    }
    
    /**
     * Find the original paragraph index for a position within merged text
     * Uses word position estimation
     */
    fun findOriginalParagraph(
        chunk: MergedChunk,
        originalParagraphs: List<String>,
        progressInChunk: Float
    ): Int {
        if (chunk.originalParagraphIndices.size <= 1) {
            return chunk.startParagraph
        }
        
        // Calculate cumulative word counts
        var cumulativeWords = 0
        val targetWords = (chunk.wordCount * progressInChunk).toInt()
        
        for (index in chunk.originalParagraphIndices) {
            val paragraphWords = countWords(originalParagraphs.getOrNull(index) ?: "")
            cumulativeWords += paragraphWords
            if (cumulativeWords >= targetWords) {
                return index
            }
        }
        
        return chunk.endParagraph
    }
    
    /**
     * Count words in text
     */
    fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        var count = 0
        var inWord = false
        
        for (char in text) {
            if (char.isWhitespace()) {
                if (inWord) {
                    count++
                    inWord = false
                }
            } else {
                inWord = true
            }
        }
        
        if (inWord) count++
        return count
    }
    
    /**
     * Split a paragraph by word count
     * Returns (first N words, remaining words)
     */
    private fun splitParagraphByWords(text: String, wordCount: Int): Pair<String, String> {
        if (wordCount <= 0) return "" to text
        
        val words = text.split(Regex("\\s+"))
        if (words.size <= wordCount) return text to ""
        
        val firstPart = words.take(wordCount).joinToString(" ")
        val secondPart = words.drop(wordCount).joinToString(" ")
        
        return firstPart to secondPart
    }
}
