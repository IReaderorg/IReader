package ireader.domain.services.tts_service.v2

import ireader.core.log.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * TTS Chunk Player - Manages chunk-based playback with caching support
 * 
 * This component:
 * - Merges paragraphs into chunks using TTSTextMergerV2
 * - Checks cache before generating audio
 * - Tracks current chunk and paragraph mapping
 * - Provides chunk navigation
 * 
 * Used with remote TTS engines (Gradio) for efficient playback.
 */
class TTSChunkPlayer(
    private val textMerger: TTSTextMergerV2,
    private val cacheUseCase: TTSCacheUseCase?
) {
    companion object {
        private const val TAG = "TTSChunkPlayer"
    }
    
    /**
     * Chunk playback state
     */
    data class ChunkState(
        val isEnabled: Boolean = false,
        val chunks: List<TTSTextMergerV2.MergedChunk> = emptyList(),
        val currentChunkIndex: Int = 0,
        val paragraphToChunkMap: Map<Int, Int> = emptyMap(),
        val cachedChunks: Set<Int> = emptySet()
    ) {
        val totalChunks: Int get() = chunks.size
        val currentChunk: TTSTextMergerV2.MergedChunk? get() = chunks.getOrNull(currentChunkIndex)
        val hasNextChunk: Boolean get() = currentChunkIndex < chunks.lastIndex
        val hasPreviousChunk: Boolean get() = currentChunkIndex > 0
        val isCurrentChunkCached: Boolean get() = currentChunkIndex in cachedChunks
        val allChunksCached: Boolean get() = cachedChunks.size >= chunks.size
    }
    
    private val _state = MutableStateFlow(ChunkState())
    val state: StateFlow<ChunkState> = _state.asStateFlow()
    
    /**
     * Initialize chunk player with paragraphs
     * 
     * @param paragraphs List of paragraph texts
     * @param chapterId Chapter ID for cache lookup
     * @param targetWordCount Target words per chunk
     */
    fun initialize(
        paragraphs: List<String>,
        chapterId: Long,
        targetWordCount: Int = 50
    ) {
        Log.warn { "$TAG: initialize(paragraphs=${paragraphs.size}, chapterId=$chapterId, targetWordCount=$targetWordCount)" }
        
        if (paragraphs.isEmpty()) {
            _state.value = ChunkState(isEnabled = false)
            return
        }
        
        // Merge paragraphs into chunks
        val mergeResult = textMerger.mergeParagraphs(paragraphs, targetWordCount)
        
        // Check which chunks are cached
        val cachedChunks = cacheUseCase?.getCachedChunkIndices(chapterId) ?: emptySet()
        
        _state.value = ChunkState(
            isEnabled = true,
            chunks = mergeResult.chunks,
            currentChunkIndex = 0,
            paragraphToChunkMap = mergeResult.paragraphToChunkMap,
            cachedChunks = cachedChunks
        )
        
        Log.warn { "$TAG: Initialized with ${mergeResult.chunks.size} chunks, ${cachedChunks.size} cached" }
    }
    
    /**
     * Get the chunk containing a specific paragraph
     */
    fun getChunkForParagraph(paragraphIndex: Int): TTSTextMergerV2.MergedChunk? {
        val chunkIndex = _state.value.paragraphToChunkMap[paragraphIndex]
        return chunkIndex?.let { _state.value.chunks.getOrNull(it) }
    }
    
    /**
     * Get the chunk index for a paragraph
     */
    fun getChunkIndexForParagraph(paragraphIndex: Int): Int {
        return _state.value.paragraphToChunkMap[paragraphIndex] ?: 0
    }
    
    /**
     * Jump to the chunk containing a paragraph
     */
    fun jumpToChunkForParagraph(paragraphIndex: Int): Boolean {
        val chunkIndex = _state.value.paragraphToChunkMap[paragraphIndex] ?: return false
        return jumpToChunk(chunkIndex)
    }
    
    /**
     * Jump to a specific chunk
     */
    fun jumpToChunk(chunkIndex: Int): Boolean {
        val currentState = _state.value
        if (chunkIndex < 0 || chunkIndex >= currentState.chunks.size) return false
        
        Log.warn { "$TAG: jumpToChunk($chunkIndex)" }
        _state.value = currentState.copy(currentChunkIndex = chunkIndex)
        return true
    }
    
    /**
     * Move to next chunk
     */
    fun nextChunk(): Boolean {
        val currentState = _state.value
        if (!currentState.hasNextChunk) return false
        
        Log.warn { "$TAG: nextChunk() -> ${currentState.currentChunkIndex + 1}" }
        _state.value = currentState.copy(currentChunkIndex = currentState.currentChunkIndex + 1)
        return true
    }
    
    /**
     * Move to previous chunk
     */
    fun previousChunk(): Boolean {
        val currentState = _state.value
        if (!currentState.hasPreviousChunk) return false
        
        Log.warn { "$TAG: previousChunk() -> ${currentState.currentChunkIndex - 1}" }
        _state.value = currentState.copy(currentChunkIndex = currentState.currentChunkIndex - 1)
        return true
    }
    
    /**
     * Get cached audio for current chunk
     * @return ByteArray of audio data or null if not cached
     */
    fun getCurrentChunkCachedAudio(chapterId: Long): ByteArray? {
        val currentState = _state.value
        if (!currentState.isEnabled) return null
        
        return cacheUseCase?.getChunkAudio(chapterId, currentState.currentChunkIndex)
    }
    
    /**
     * Check if current chunk is cached
     */
    fun isCurrentChunkCached(chapterId: Long): Boolean {
        val currentState = _state.value
        if (!currentState.isEnabled) return false
        
        return cacheUseCase?.isChunkCached(chapterId, currentState.currentChunkIndex) == true
    }
    
    /**
     * Update cached chunks set (after caching new audio)
     */
    fun updateCachedChunks(chapterId: Long) {
        val cachedChunks = cacheUseCase?.getCachedChunkIndices(chapterId) ?: emptySet()
        _state.value = _state.value.copy(cachedChunks = cachedChunks)
    }
    
    /**
     * Get the starting paragraph index for current chunk
     */
    fun getCurrentChunkStartParagraph(): Int {
        return _state.value.currentChunk?.startParagraph ?: 0
    }
    
    /**
     * Get all paragraph indices in current chunk
     */
    fun getCurrentChunkParagraphs(): List<Int> {
        return _state.value.currentChunk?.paragraphIndices ?: emptyList()
    }
    
    /**
     * Reset the chunk player
     */
    fun reset() {
        Log.warn { "$TAG: reset()" }
        _state.value = ChunkState()
    }
    
    /**
     * Disable chunk mode (use paragraph-by-paragraph playback)
     */
    fun disable() {
        Log.warn { "$TAG: disable()" }
        _state.value = _state.value.copy(isEnabled = false)
    }
}
