package ireader.domain.services.tts_service.piper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ireader.core.log.Log

/**
 * Implementation of SpeechSynthesizer using the Piper TTS engine.
 * This class provides text-to-speech synthesis using locally stored ONNX models.
 * 
 * Now uses subprocess approach for better reliability.
 */
class PiperSpeechSynthesizer : SpeechSynthesizer {
    
    /**
     * Subprocess-based Piper synthesizer
     */
    private val subprocessSynthesizer = PiperSubprocessSynthesizer()
    
    /**
     * Coroutine scope for async operations.
     */
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Sample rate of the loaded model (cached after initialization).
     */
    private var sampleRate: Int = 22050 // Default Piper sample rate
    
    /**
     * Track initialization status
     */
    private var initialized: Boolean = false
    
    /**
     * Check if the synthesizer is initialized and ready to use.
     * 
     * @return true if initialized, false otherwise
     */
    fun isInitialized(): Boolean {
        return initialized
    }
    
    /**
     * Get the native instance handle (deprecated, kept for compatibility).
     * 
     * @return Always returns 1 if initialized, 0 otherwise
     */
    fun getInstance(): Long {
        return if (initialized) 1L else 0L
    }
    
    override suspend fun initialize(modelPath: String, configPath: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Shutdown existing instance if any
                if (isInitialized()) {
                    shutdown()
                }
                
                Log.info { "Initializing Piper subprocess with model: $modelPath" }
                
                // Initialize using subprocess
                val success = subprocessSynthesizer.initialize(modelPath, configPath)
                
                if (!success) {
                    return@withContext Result.failure(
                        IllegalStateException("Failed to initialize Piper subprocess")
                    )
                }
                
                initialized = true
                sampleRate = subprocessSynthesizer.getSampleRate()
                
                Log.info { "Piper subprocess initialized successfully with sample rate: $sampleRate Hz" }
                Result.success(Unit)
                
            } catch (e: Exception) {
                Log.error(e, "Failed to initialize Piper speech synthesizer")
                initialized = false
                Result.failure(e)
            }
        }
    }
    
    override suspend fun synthesize(text: String): Result<AudioData> {
        return withContext(Dispatchers.IO) {
            try {
                // Validate state
                if (!isInitialized()) {
                    return@withContext Result.failure(
                        IllegalStateException("Synthesizer not initialized. Call initialize() first.")
                    )
                }
                
                if (text.isBlank()) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Text cannot be blank")
                    )
                }
                
                Log.debug { "Synthesizing text: ${text.take(50)}..." }
                
                // Call subprocess synthesis
                val audioBytes = subprocessSynthesizer.synthesize(text)
                
                // Create AudioData object
                val audioData = AudioData(
                    samples = audioBytes,
                    sampleRate = sampleRate,
                    channels = 1, // Piper produces mono audio
                    format = AudioData.AudioFormat.PCM_16 // Piper uses 16-bit PCM
                )
                
                Log.debug { "Synthesis complete: ${audioBytes.size} bytes generated" }
                Result.success(audioData)
                
            } catch (e: Exception) {
                Log.error(e, "Failed to synthesize text")
                Result.failure(e)
            }
        }
    }
    
    override fun synthesizeStream(text: String): Flow<AudioChunk> = flow {
        // Validate state
        if (!isInitialized()) {
            throw IllegalStateException("Synthesizer not initialized. Call initialize() first.")
        }
        
        if (text.isBlank()) {
            return@flow
        }
        
        Log.debug { "Starting streaming synthesis for text: ${text.take(50)}..." }
        
        // Split text into sentences for progressive generation
        // This regex splits on sentence-ending punctuation followed by whitespace
        val sentences = text.split(Regex("(?<=[.!?])\\s+"))
            .filter { it.isNotBlank() }
        
        if (sentences.isEmpty()) {
            return@flow
        }
        
        Log.debug { "Split text into ${sentences.size} sentences for streaming" }
        
        // Process each sentence
        sentences.forEachIndexed { index, sentence ->
            val trimmedSentence = sentence.trim()
            
            if (trimmedSentence.isEmpty()) {
                return@forEachIndexed
            }
            
            Log.debug { "Synthesizing sentence ${index + 1}/${sentences.size}" }
            
            // Synthesize this sentence
            val result = synthesize(trimmedSentence)
            
            result.onSuccess { audioData ->
                // Emit audio chunk
                val chunk = AudioChunk(
                    data = audioData,
                    text = trimmedSentence,
                    isLast = (index == sentences.size - 1)
                )
                emit(chunk)
                
                Log.debug { "Emitted audio chunk ${index + 1}/${sentences.size}" }
            }.onFailure { error ->
                Log.error(error, "Failed to synthesize sentence: $trimmedSentence")
                // Continue with next sentence instead of failing the entire stream
            }
        }
        
        Log.debug { "Streaming synthesis complete" }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun getWordBoundaries(text: String): List<WordBoundary> {
        return withContext(Dispatchers.IO) {
            if (text.isBlank()) {
                return@withContext emptyList()
            }
            
            Log.debug { "Calculating word boundaries for text: ${text.take(50)}..." }
            
            // Split text into words using whitespace and punctuation
            val wordPattern = Regex("\\b\\w+\\b")
            val matches = wordPattern.findAll(text)
            
            val boundaries = mutableListOf<WordBoundary>()
            var currentTimeMs = 0L
            
            // Average phoneme duration in milliseconds (based on typical speech rates)
            // This is an estimation since Piper doesn't provide exact phoneme timings
            val avgPhonemeMs = 80L
            
            matches.forEach { match ->
                val word = match.value
                val startOffset = match.range.first
                val endOffset = match.range.last + 1
                
                // Estimate duration based on word length
                // Longer words generally take more time to speak
                val estimatedDurationMs = word.length * avgPhonemeMs
                
                val boundary = WordBoundary(
                    word = word,
                    startOffset = startOffset,
                    endOffset = endOffset,
                    startTimeMs = currentTimeMs,
                    endTimeMs = currentTimeMs + estimatedDurationMs
                )
                
                boundaries.add(boundary)
                currentTimeMs += estimatedDurationMs
                
                // Add a small pause between words (typical inter-word gap)
                currentTimeMs += 50L
            }
            
            Log.debug { "Calculated ${boundaries.size} word boundaries" }
            boundaries
        }
    }
    
    override fun setSpeechRate(rate: Float) {
        subprocessSynthesizer.setSpeechRate(rate)
        Log.debug { "Speech rate set to: $rate" }
    }
    
    override fun shutdown() {
        if (isInitialized()) {
            try {
                Log.info { "Shutting down Piper speech synthesizer" }
                
                // Shutdown subprocess
                subprocessSynthesizer.shutdown()
                
                // Reset state
                initialized = false
                sampleRate = 22050
                
                Log.info { "Piper shutdown complete - all resources released" }
            } catch (e: Exception) {
                Log.error(e, "Error during Piper shutdown")
                // Ensure state is reset even if shutdown fails
                initialized = false
                sampleRate = 22050
            }
        } else {
            Log.debug { "Shutdown called but synthesizer was not initialized" }
        }
    }
    
    /**
     * Ensure resources are cleaned up when the object is garbage collected.
     */
    protected fun finalize() {
        if (isInitialized()) {
            Log.warn { "PiperSpeechSynthesizer finalized without explicit shutdown - cleaning up" }
            shutdown()
        }
    }
}
