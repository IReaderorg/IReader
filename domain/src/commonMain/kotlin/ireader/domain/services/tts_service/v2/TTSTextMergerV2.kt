package ireader.domain.services.tts_service.v2

import ireader.core.log.Log

/**
 * TTS Text Merger v2 - Merges paragraphs into chunks for remote TTS
 * 
 * This is a simple, focused use case that:
 * - Merges consecutive paragraphs into chunks based on word count
 * - Tracks which original paragraphs are in each chunk
 * - Provides chunk-to-paragraph mapping for navigation
 * 
 * Used with remote TTS engines (Gradio) where sending larger chunks
 * is more efficient than individual paragraphs.
 */
class TTSTextMergerV2 {
    
    companion object {
        private const val TAG = "TTSTextMergerV2"
    }
    
    /**
     * A merged chunk containing text from one or more paragraphs
     */
    data class MergedChunk(
        val index: Int,                    // Chunk index (0-based)
        val text: String,                  // Combined text
        val paragraphIndices: List<Int>,   // Original paragraph indices
        val startParagraph: Int,           // First paragraph index
        val endParagraph: Int              // Last paragraph index (inclusive)
    ) {
        val wordCount: Int get() = text.split(Regex("\\s+")).size
    }
    
    /**
     * Result of merging paragraphs
     */
    data class MergeResult(
        val chunks: List<MergedChunk>,
        val paragraphToChunkMap: Map<Int, Int>  // paragraph index -> chunk index
    )
    
    /**
     * Merge paragraphs into chunks based on target word count
     * 
     * @param paragraphs List of paragraph texts
     * @param targetWordCount Target words per chunk (default 50)
     * @return MergeResult with chunks and mapping
     */
    fun mergeParagraphs(
        paragraphs: List<String>,
        targetWordCount: Int = 50
    ): MergeResult {
        if (paragraphs.isEmpty()) {
            return MergeResult(emptyList(), emptyMap())
        }
        
        val chunks = mutableListOf<MergedChunk>()
        val paragraphToChunk = mutableMapOf<Int, Int>()
        
        var currentText = StringBuilder()
        var currentParagraphs = mutableListOf<Int>()
        var currentWordCount = 0
        var chunkIndex = 0
        
        paragraphs.forEachIndexed { index, paragraph ->
            val paragraphWords = paragraph.split(Regex("\\s+")).size
            
            // If adding this paragraph would exceed target and we have content,
            // finalize current chunk first
            if (currentWordCount > 0 && currentWordCount + paragraphWords > targetWordCount * 1.5) {
                // Finalize current chunk
                chunks.add(MergedChunk(
                    index = chunkIndex,
                    text = currentText.toString().trim(),
                    paragraphIndices = currentParagraphs.toList(),
                    startParagraph = currentParagraphs.first(),
                    endParagraph = currentParagraphs.last()
                ))
                
                // Map paragraphs to this chunk
                currentParagraphs.forEach { paragraphToChunk[it] = chunkIndex }
                
                chunkIndex++
                currentText = StringBuilder()
                currentParagraphs = mutableListOf()
                currentWordCount = 0
            }
            
            // Add paragraph to current chunk
            if (currentText.isNotEmpty()) {
                currentText.append(" ")
            }
            currentText.append(paragraph)
            currentParagraphs.add(index)
            currentWordCount += paragraphWords
            
            // If we've reached target, finalize chunk
            if (currentWordCount >= targetWordCount) {
                chunks.add(MergedChunk(
                    index = chunkIndex,
                    text = currentText.toString().trim(),
                    paragraphIndices = currentParagraphs.toList(),
                    startParagraph = currentParagraphs.first(),
                    endParagraph = currentParagraphs.last()
                ))
                
                currentParagraphs.forEach { paragraphToChunk[it] = chunkIndex }
                
                chunkIndex++
                currentText = StringBuilder()
                currentParagraphs = mutableListOf()
                currentWordCount = 0
            }
        }
        
        // Don't forget the last chunk
        if (currentParagraphs.isNotEmpty()) {
            chunks.add(MergedChunk(
                index = chunkIndex,
                text = currentText.toString().trim(),
                paragraphIndices = currentParagraphs.toList(),
                startParagraph = currentParagraphs.first(),
                endParagraph = currentParagraphs.last()
            ))
            currentParagraphs.forEach { paragraphToChunk[it] = chunkIndex }
        }
        
        Log.warn { "$TAG: Merged ${paragraphs.size} paragraphs into ${chunks.size} chunks (target: $targetWordCount words)" }
        
        return MergeResult(chunks, paragraphToChunk)
    }
    
    /**
     * Find the chunk containing a specific paragraph
     */
    fun findChunkForParagraph(result: MergeResult, paragraphIndex: Int): MergedChunk? {
        val chunkIndex = result.paragraphToChunkMap[paragraphIndex] ?: return null
        return result.chunks.getOrNull(chunkIndex)
    }
    
    /**
     * Get the starting paragraph for a chunk
     */
    fun getChunkStartParagraph(result: MergeResult, chunkIndex: Int): Int {
        return result.chunks.getOrNull(chunkIndex)?.startParagraph ?: 0
    }
}
