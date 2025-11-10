package ireader.domain.services.tts_service.piper

/**
 * Represents word boundary information for text highlighting during TTS playback
 * 
 * @property word The word text
 * @property startOffset Character offset where the word starts in the original text
 * @property endOffset Character offset where the word ends in the original text
 * @property startTimeMs Time in milliseconds when the word starts being spoken
 * @property endTimeMs Time in milliseconds when the word finishes being spoken
 */
data class WordBoundary(
    val word: String,
    val startOffset: Int,
    val endOffset: Int,
    val startTimeMs: Long,
    val endTimeMs: Long
)
