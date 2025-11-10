package ireader.domain.services.tts_service.piper

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import javax.sound.sampled.*
import ireader.core.log.Log

/**
 * Audio playback engine using Java Sound API
 * 
 * Manages audio playback with support for streaming, pause/resume,
 * and position tracking. Uses platform-specific audio configurations
 * for optimal performance on Windows, macOS, and Linux.
 */
class AudioPlaybackEngine : AudioPlayback {
    private var sourceDataLine: SourceDataLine? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isPaused = false
    private var currentPositionMs = 0L
    private val positionLock = Any()
    
    // Platform-specific audio configuration
    private val platformConfig: PlatformAudioConfig = PlatformAudioConfigFactory.getConfig()
    
    companion object {
        private const val PAUSE_CHECK_DELAY_MS = 50L
    }
    
    init {
    }
    
    /**
     * Play audio data synchronously
     */
    override suspend fun play(audioData: AudioData) {
        withContext(Dispatchers.IO) {
            try {
                // Validate audio data
                if (audioData.samples.isEmpty()) {
                    Log.warn { "Audio data is empty, skipping playback" }
                    return@withContext
                }
                
                initializeAudioLine(audioData)
                
                val line = sourceDataLine
                if (line == null) {
                    Log.error { "Failed to initialize audio line" }
                    throw AudioPlaybackException("Audio line initialization failed")
                }
                
                line.start()
                line.write(audioData.samples, 0, audioData.samples.size)
                line.drain()
                
                // Update position
                updatePosition(audioData)
            } catch (e: LineUnavailableException) {
                Log.error { "Audio line unavailable: ${e.message}" }
                throw AudioPlaybackException("Audio device unavailable: ${e.message}", e)
            } catch (e: Exception) {
                Log.error { "Audio playback error: ${e.message}" }
                throw AudioPlaybackException("Audio playback failed: ${e.message}", e)
            }
        }
    }
    
    /**
     * Play audio stream with buffering
     */
    override fun playStream(audioFlow: Flow<AudioChunk>): Job {
        return scope.launch {
            try {
                audioFlow.collect { chunk ->
                    // Wait while paused
                    while (isPaused) {
                        delay(PAUSE_CHECK_DELAY_MS)
                    }
                    
                    // Check if job was cancelled
                    ensureActive()
                    
                    // Initialize audio line on first chunk
                    if (sourceDataLine == null) {
                        initializeAudioLine(chunk.data)
                        sourceDataLine?.start()
                    }
                    
                    // Write audio data
                    sourceDataLine?.write(chunk.data.samples, 0, chunk.data.samples.size)
                    
                    // Update position
                    updatePosition(chunk.data)
                }
                
                // Drain remaining audio
                sourceDataLine?.drain()
            } catch (e: CancellationException) {
                throw e
            } catch (e: LineUnavailableException) {
                Log.error { "Audio line unavailable during streaming: ${e.message}" }
                throw AudioPlaybackException("Audio device unavailable: ${e.message}", e)
            } catch (e: Exception) {
                Log.error { "Audio stream playback error: ${e.message}" }
                throw AudioPlaybackException("Audio stream playback failed: ${e.message}", e)
            }
        }
    }
    
    /**
     * Pause playback (responds within 200ms)
     */
    override fun pause() {
        synchronized(positionLock) {
            isPaused = true
        }
        sourceDataLine?.stop()
    }
    
    /**
     * Resume playback from paused position (responds within 200ms)
     */
    override fun resume() {
        synchronized(positionLock) {
            isPaused = false
        }
        sourceDataLine?.start()
    }
    
    /**
     * Stop playback and clear buffers
     */
    override fun stop() {
        playbackJob?.cancel()
        sourceDataLine?.stop()
        sourceDataLine?.flush()
        sourceDataLine?.close()
        sourceDataLine = null
        
        synchronized(positionLock) {
            currentPositionMs = 0L
            isPaused = false
        }
    }
    
    /**
     * Get current playback position in milliseconds
     */
    override fun getCurrentPosition(): Long {
        synchronized(positionLock) {
            return currentPositionMs
        }
    }
    
    /**
     * Check if audio is currently playing
     */
    override fun isPlaying(): Boolean {
        return sourceDataLine?.isActive == true && !isPaused
    }
    
    /**
     * Initialize audio line with proper format using platform-specific configuration
     */
    private fun initializeAudioLine(audioData: AudioData) {
        // Close existing line if any
        sourceDataLine?.close()
        
        val bitDepth = when (audioData.format) {
            AudioData.AudioFormat.PCM_16 -> 16
            AudioData.AudioFormat.PCM_24 -> 24
            AudioData.AudioFormat.PCM_32 -> 32
        }
        
        // Use platform-specific audio format configuration
        val format = platformConfig.configureAudioFormat(
            audioData.sampleRate.toFloat(),
            bitDepth,
            audioData.channels
        )
        
        val info = DataLine.Info(SourceDataLine::class.java, format)
        
        if (!AudioSystem.isLineSupported(info)) {
            throw AudioPlaybackException("Audio format not supported: $format")
        }
        
        // Get platform-specific mixer if available
        val mixer = platformConfig.getPreferredMixer()
        sourceDataLine = if (mixer != null) {
            try {
                val mixerInstance = AudioSystem.getMixer(mixer)
                mixerInstance.getLine(info) as SourceDataLine
            } catch (e: Exception) {
                Log.warn { "Failed to use preferred mixer, falling back to default: ${e.message}" }
                AudioSystem.getLine(info) as SourceDataLine
            }
        } else {
            AudioSystem.getLine(info) as SourceDataLine
        }
        
        // Use platform-specific buffer size
        val bufferSize = platformConfig.getBufferSize()
        sourceDataLine?.open(format, bufferSize)
    }
    
    /**
     * Update playback position based on audio data
     */
    private fun updatePosition(audioData: AudioData) {
        val bytesPerSample = when (audioData.format) {
            AudioData.AudioFormat.PCM_16 -> 2
            AudioData.AudioFormat.PCM_24 -> 3
            AudioData.AudioFormat.PCM_32 -> 4
        }
        
        val durationMs = (audioData.samples.size.toLong() * 1000L) / 
            (audioData.sampleRate * audioData.channels * bytesPerSample)
        
        synchronized(positionLock) {
            currentPositionMs += durationMs
        }
    }
    
    /**
     * Shutdown the playback engine and release resources
     */
    fun shutdown() {
        stop()
        platformConfig.cleanup()
        scope.cancel()
    }
}

/**
 * Exception thrown when audio playback fails
 */
class AudioPlaybackException(message: String, cause: Throwable? = null) : Exception(message, cause)
