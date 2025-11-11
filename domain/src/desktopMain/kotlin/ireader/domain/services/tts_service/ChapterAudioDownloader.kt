package ireader.domain.services.tts_service

import ireader.core.log.Log
import ireader.domain.models.entities.Chapter
import ireader.domain.services.tts_service.piper.AudioData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Chapter Audio Downloader
 * Pre-generates and saves entire chapter audio for offline listening
 * 
 * Features:
 * - Parallel paragraph generation for speed
 * - Progress tracking
 * - Saves as single WAV file
 * - Supports all TTS engines
 */
class ChapterAudioDownloader(
    private val audioDir: File
) {
    init {
        audioDir.mkdirs()
    }
    
    /**
     * Download entire chapter audio
     * 
     * @param chapter Chapter to download
     * @param synthesizer Function to synthesize text to audio
     * @param onProgress Progress callback (current, total)
     * @return Path to saved audio file
     */
    suspend fun downloadChapter(
        chapter: Chapter,
        synthesizer: suspend (String) -> Result<AudioData>,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            Log.info { "Downloading chapter audio: ${chapter.name}" }
            
            // Extract text paragraphs
            val paragraphs = chapter.content
                .mapNotNull { content ->
                    (content as? ireader.core.source.model.Text)?.text
                }
                .filter { it.isNotBlank() }
                .map { it.trim() }
            
            if (paragraphs.isEmpty()) {
                return@withContext Result.failure(Exception("Chapter has no text content"))
            }
            
            Log.info { "Processing ${paragraphs.size} paragraphs..." }
            
            // Chunk paragraphs into groups to avoid text length limits
            // Most TTS engines have limits (Kokoro ~1000 chars, Piper ~5000 chars)
            val maxCharsPerChunk = 1000
            val chunks = mutableListOf<String>()
            var currentChunk = StringBuilder()
            
            paragraphs.forEach { paragraph ->
                if (currentChunk.length + paragraph.length > maxCharsPerChunk && currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk.toString())
                    currentChunk = StringBuilder()
                }
                if (currentChunk.isNotEmpty()) {
                    currentChunk.append("\n\n")
                }
                currentChunk.append(paragraph)
            }
            if (currentChunk.isNotEmpty()) {
                chunks.add(currentChunk.toString())
            }
            
            Log.info { "Split into ${chunks.size} chunks for synthesis..." }
            
            // Generate audio for each chunk with realistic progress
            val audioSegments = mutableListOf<AudioData>()
            val startTime = System.currentTimeMillis()
            
            chunks.forEachIndexed { index, chunkText ->
                Log.info { "Generating chunk ${index + 1}/${chunks.size} (${chunkText.length} chars)..." }
                
                // Report progress before synthesis
                onProgress(index, chunks.size)
                
                // Estimate time based on text length (roughly 100 chars per second for synthesis)
                val estimatedSeconds = (chunkText.length / 100.0).coerceAtLeast(1.0)
                Log.info { "Estimated synthesis time: ${estimatedSeconds.toInt()}s" }
                
                val result = synthesizer(chunkText)
                
                result.onSuccess { audioData ->
                    audioSegments.add(audioData)
                    val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
                    Log.info { "Chunk ${index + 1} completed in ${elapsed.toInt()}s (${audioData.samples.size / 1024} KB)" }
                }.onFailure { error ->
                    Log.error { "Failed to generate chunk ${index + 1}: ${error.message}" }
                    return@withContext Result.failure(error)
                }
            }
            
            // Report completion
            onProgress(chunks.size, chunks.size)
            
            val totalTime = (System.currentTimeMillis() - startTime) / 1000.0
            Log.info { "All chunks generated in ${totalTime.toInt()}s" }
            
            Log.info { "Generated ${audioSegments.size} audio segments" }
            
            // Merge audio segments
            val mergedAudio = mergeAudioSegments(audioSegments)
            
            // Save to file
            val filename = sanitizeFilename("${chapter.bookId}_${chapter.id}_${chapter.name}.wav")
            val outputFile = File(audioDir, filename)
            
            saveAsWav(mergedAudio, outputFile)
            
            Log.info { "Chapter audio saved: ${outputFile.absolutePath} (${outputFile.length() / 1024 / 1024} MB)" }
            
            Result.success(outputFile)
            
        } catch (e: Exception) {
            Log.error { "Failed to download chapter audio: ${e.message}" }
            Result.failure(e)
        }
    }
    
    /**
     * Get list of downloaded chapter audio files
     */
    fun getDownloadedChapters(): List<ChapterAudioInfo> {
        return audioDir.listFiles()
            ?.filter { it.extension == "wav" }
            ?.map { file ->
                // Parse filename: bookId_chapterId_chapterName.wav
                val parts = file.nameWithoutExtension.split("_", limit = 3)
                ChapterAudioInfo(
                    file = file,
                    bookId = parts.getOrNull(0)?.toLongOrNull() ?: 0,
                    chapterId = parts.getOrNull(1)?.toLongOrNull() ?: 0,
                    chapterName = parts.getOrNull(2) ?: file.nameWithoutExtension,
                    sizeBytes = file.length(),
                    createdTime = file.lastModified()
                )
            } ?: emptyList()
    }
    
    /**
     * Delete downloaded chapter audio
     */
    fun deleteChapterAudio(chapterId: Long): Boolean {
        val files = audioDir.listFiles()
            ?.filter { it.name.contains("_${chapterId}_") }
        
        return files?.all { it.delete() } ?: false
    }
    
    /**
     * Get total size of downloaded audio
     */
    fun getTotalSize(): Long {
        return audioDir.listFiles()
            ?.filter { it.extension == "wav" }
            ?.sumOf { it.length() } ?: 0
    }
    
    /**
     * Clear all downloaded audio
     */
    fun clearAll() {
        audioDir.listFiles()?.forEach { it.delete() }
        Log.info { "Cleared all downloaded chapter audio" }
    }
    
    /**
     * Merge multiple audio segments into one
     */
    private fun mergeAudioSegments(segments: List<AudioData>): AudioData {
        if (segments.isEmpty()) {
            throw IllegalArgumentException("No audio segments to merge")
        }
        
        // Use first segment's properties
        val first = segments.first()
        val sampleRate = first.sampleRate
        val channels = first.channels
        val format = first.format
        
        // Calculate total size
        val totalSize = segments.sumOf { it.samples.size }
        
        // Merge samples
        val mergedSamples = ByteArray(totalSize)
        var offset = 0
        
        segments.forEach { segment ->
            System.arraycopy(segment.samples, 0, mergedSamples, offset, segment.samples.size)
            offset += segment.samples.size
        }
        
        return AudioData(
            samples = mergedSamples,
            sampleRate = sampleRate,
            channels = channels,
            format = format
        )
    }
    
    /**
     * Save audio data as WAV file
     */
    private fun saveAsWav(audioData: AudioData, outputFile: File) {
        FileOutputStream(outputFile).use { fos ->
            // WAV header
            val dataSize = audioData.samples.size
            val bitsPerSample = when (audioData.format) {
                AudioData.AudioFormat.PCM_16 -> 16
                AudioData.AudioFormat.PCM_24 -> 24
                AudioData.AudioFormat.PCM_32 -> 32
            }
            val byteRate = audioData.sampleRate * audioData.channels * (bitsPerSample / 8)
            val blockAlign = audioData.channels * (bitsPerSample / 8)
            
            // RIFF header
            fos.write("RIFF".toByteArray())
            fos.write(intToBytes(36 + dataSize))
            fos.write("WAVE".toByteArray())
            
            // fmt chunk
            fos.write("fmt ".toByteArray())
            fos.write(intToBytes(16)) // Chunk size
            fos.write(shortToBytes(1)) // Audio format (PCM)
            fos.write(shortToBytes(audioData.channels.toShort()))
            fos.write(intToBytes(audioData.sampleRate))
            fos.write(intToBytes(byteRate))
            fos.write(shortToBytes(blockAlign.toShort()))
            fos.write(shortToBytes(bitsPerSample.toShort()))
            
            // data chunk
            fos.write("data".toByteArray())
            fos.write(intToBytes(dataSize))
            fos.write(audioData.samples)
        }
    }
    
    /**
     * Convert int to little-endian bytes
     */
    private fun intToBytes(value: Int): ByteArray {
        return ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(value)
            .array()
    }
    
    /**
     * Convert short to little-endian bytes
     */
    private fun shortToBytes(value: Short): ByteArray {
        return ByteBuffer.allocate(2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(value)
            .array()
    }
    
    /**
     * Sanitize filename for filesystem
     */
    private fun sanitizeFilename(filename: String): String {
        return filename
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(200) // Limit length
    }
    
    data class ChapterAudioInfo(
        val file: File,
        val bookId: Long,
        val chapterId: Long,
        val chapterName: String,
        val sizeBytes: Long,
        val createdTime: Long
    )
}
