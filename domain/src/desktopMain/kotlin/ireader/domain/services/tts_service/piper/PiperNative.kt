package ireader.domain.services.tts_service.piper

/**
 * JNI wrapper for the Piper TTS C++ library.
 * This object provides native function declarations for interfacing with Piper.
 * 
 * All native methods correspond to C++ implementations in the piper_jni library.
 * The library must be loaded via [NativeLibraryLoader] before calling any native methods.
 * 
 * @see NativeLibraryLoader
 * @see PiperInitializer
 */
object PiperNative {
    
    /**
     * Initialize Piper with a voice model and configuration.
     * 
     * This method loads the ONNX model and configuration file into memory and creates
     * a new Piper voice instance. The returned instance handle must be used for all
     * subsequent operations and should be released with [shutdown] when no longer needed.
     * 
     * @param modelPath Absolute path to the ONNX model file (.onnx)
     * @param configPath Absolute path to the model configuration file (.json)
     * @return Native pointer to the Piper instance (positive value on success, 0 on failure)
     * @throws PiperException.InitializationException if initialization fails
     * @throws PiperException.ModelLoadException if model files cannot be loaded
     * @throws IllegalArgumentException if paths are invalid or files don't exist
     * @throws UnsatisfiedLinkError if the native library is not loaded
     */
    external fun initialize(modelPath: String, configPath: String): Long
    
    /**
     * Synthesize text to audio using the initialized Piper instance.
     * 
     * Converts the input text to speech using the loaded voice model. The audio is returned
     * as raw PCM data (16-bit signed little-endian) at the model's sample rate.
     * 
     * For long texts (>1000 characters), consider using streaming synthesis to avoid
     * memory issues and improve responsiveness.
     * 
     * @param instance Native pointer to the Piper instance (returned by [initialize])
     * @param text The text to synthesize into speech (max recommended: 10,000 characters)
     * @return PCM audio data as a byte array (16-bit signed little-endian)
     * @throws PiperException.SynthesisException if synthesis fails
     * @throws IllegalArgumentException if instance is invalid or text is empty
     * @throws OutOfMemoryError if audio buffer allocation fails
     * @throws UnsatisfiedLinkError if the native library is not loaded
     */
    external fun synthesize(instance: Long, text: String): ByteArray
    
    /**
     * Set the speech rate for synthesis.
     * 
     * Adjusts the speed of speech synthesis. This setting takes effect immediately
     * and applies to all subsequent synthesis calls.
     * 
     * @param instance Native pointer to the Piper instance
     * @param rate Speech rate multiplier:
     *   - 0.5 = half speed (slower)
     *   - 1.0 = normal speed
     *   - 2.0 = double speed (faster)
     *   - Valid range: 0.5 to 2.0
     * @throws PiperException.InvalidParameterException if rate is out of valid range
     * @throws IllegalArgumentException if instance is invalid
     * @throws UnsatisfiedLinkError if the native library is not loaded
     */
    external fun setSpeechRate(instance: Long, rate: Float)
    
    /**
     * Set the noise scale for synthesis quality.
     * 
     * Controls the amount of variation in the synthesized speech. Lower values produce
     * more consistent but potentially robotic speech, while higher values add natural
     * variation but may reduce clarity.
     * 
     * @param instance Native pointer to the Piper instance
     * @param noiseScale Noise scale value:
     *   - 0.0 = most consistent (robotic)
     *   - 0.667 = default (balanced)
     *   - 1.0 = most variation (natural)
     *   - Valid range: 0.0 to 1.0
     * @throws PiperException.InvalidParameterException if noiseScale is out of valid range
     * @throws IllegalArgumentException if instance is invalid
     * @throws UnsatisfiedLinkError if the native library is not loaded
     */
    external fun setNoiseScale(instance: Long, noiseScale: Float)
    
    /**
     * Set the length scale for phoneme duration.
     * 
     * Adjusts the duration of phonemes, affecting the overall pacing of speech.
     * This is different from speech rate as it affects the timing of individual sounds.
     * 
     * @param instance Native pointer to the Piper instance
     * @param lengthScale Length scale multiplier:
     *   - < 1.0 = shorter phonemes (faster pacing)
     *   - 1.0 = normal duration
     *   - > 1.0 = longer phonemes (slower pacing)
     *   - Valid range: 0.5 to 2.0
     * @throws PiperException.InvalidParameterException if lengthScale is out of valid range
     * @throws IllegalArgumentException if instance is invalid
     * @throws UnsatisfiedLinkError if the native library is not loaded
     */
    external fun setLengthScale(instance: Long, lengthScale: Float)
    
    /**
     * Set the noise weight for speech variation.
     * 
     * Controls the weight of noise in the synthesis process, affecting speech naturalness.
     * 
     * @param instance Native pointer to the Piper instance
     * @param noiseW Noise weight value:
     *   - 0.0 = no variation
     *   - 0.8 = default (natural variation)
     *   - 1.0 = maximum variation
     *   - Valid range: 0.0 to 1.0
     * @throws PiperException.InvalidParameterException if noiseW is out of valid range
     * @throws IllegalArgumentException if instance is invalid
     * @throws UnsatisfiedLinkError if the native library is not loaded
     */
    external fun setNoiseW(instance: Long, noiseW: Float)
    
    /**
     * Set the sentence silence duration.
     * 
     * Controls the pause duration between sentences in the synthesized speech.
     * 
     * @param instance Native pointer to the Piper instance
     * @param sentenceSilence Silence duration in seconds:
     *   - 0.0 = no pause
     *   - 0.2 = default (natural pause)
     *   - 1.0 = long pause
     *   - Valid range: 0.0 to 2.0
     * @throws PiperException.InvalidParameterException if sentenceSilence is out of valid range
     * @throws IllegalArgumentException if instance is invalid
     * @throws UnsatisfiedLinkError if the native library is not loaded
     */
    external fun setSentenceSilence(instance: Long, sentenceSilence: Float)
    
    /**
     * Get the sample rate of the audio produced by this model.
     * 
     * The sample rate is determined by the voice model and cannot be changed.
     * Common values are 16000, 22050, or 44100 Hz.
     * 
     * @param instance Native pointer to the Piper instance
     * @return Sample rate in Hz (typically 22050)
     * @throws IllegalArgumentException if instance is invalid
     * @throws UnsatisfiedLinkError if the native library is not loaded
     */
    external fun getSampleRate(instance: Long): Int
    
    /**
     * Get the number of audio channels produced by this model.
     * 
     * Piper models typically produce mono audio (1 channel).
     * 
     * @param instance Native pointer to the Piper instance
     * @return Number of audio channels (typically 1 for mono)
     * @throws IllegalArgumentException if instance is invalid
     * @throws UnsatisfiedLinkError if the native library is not loaded
     */
    external fun getChannels(instance: Long): Int
    
    /**
     * Get the version of the Piper library.
     * 
     * Returns version information about the native Piper library.
     * Useful for debugging and compatibility checking.
     * 
     * @return Version string (e.g., "1.2.0")
     * @throws UnsatisfiedLinkError if the native library is not loaded
     */
    external fun getVersion(): String
    
    /**
     * Shutdown the Piper instance and free all native resources.
     * 
     * This method must be called when the voice instance is no longer needed to prevent
     * memory leaks. After calling this method, the instance pointer becomes invalid and
     * must not be used in any subsequent calls.
     * 
     * It is safe to call this method multiple times with the same instance - subsequent
     * calls will be ignored.
     * 
     * @param instance Native pointer to the Piper instance
     * @throws UnsatisfiedLinkError if the native library is not loaded
     */
    external fun shutdown(instance: Long)
    
    /**
     * Check if the native library is loaded and functional.
     * 
     * This method verifies that the native library has been successfully loaded
     * and is ready to use. It should be called before attempting to use any
     * native methods.
     * 
     * @return true if the library is loaded and functional, false otherwise
     */
    fun isLoaded(): Boolean {
        return try {
            // Try to call a simple native method to verify the library is loaded
            // This will throw if the library isn't loaded
            NativeLibraryLoader.isLibraryLoaded()
        } catch (e: UnsatisfiedLinkError) {
            false
        }
    }
    
    /**
     * Validate that an instance handle is valid (non-zero).
     * 
     * @param instance The instance handle to validate
     * @throws IllegalArgumentException if the instance is invalid (zero or negative)
     */
    internal fun validateInstance(instance: Long) {
        require(instance > 0) { "Invalid Piper instance handle: $instance" }
    }
    
    /**
     * Validate that a parameter is within the specified range.
     * 
     * @param value The value to validate
     * @param min Minimum valid value (inclusive)
     * @param max Maximum valid value (inclusive)
     * @param paramName Name of the parameter (for error messages)
     * @throws IllegalArgumentException if the value is out of range
     */
    internal fun validateRange(value: Float, min: Float, max: Float, paramName: String) {
        require(value in min..max) {
            "$paramName must be between $min and $max, got $value"
        }
    }
    
    /**
     * Validate and sanitize text input before synthesis.
     * 
     * @param text The text to validate
     * @return Sanitized text
     * @throws IllegalArgumentException if text is invalid
     */
    internal fun validateAndSanitizeText(text: String): String {
        val sanitizer = InputSanitizer()
        val result = sanitizer.validateText(text)
        
        if (!result.isValid) {
            throw IllegalArgumentException(result.errorMessage ?: "Invalid text input")
        }
        
        // Log warnings if any
        if (result.warnings.isNotEmpty()) {
            result.warnings.forEach { println("Warning: $it") }
        }
        
        return result.sanitizedValue ?: text
    }
    
    /**
     * Validate model and config file paths.
     * 
     * @param modelPath Path to the model file
     * @param configPath Path to the config file
     * @throws IllegalArgumentException if paths are invalid
     */
    internal fun validatePaths(modelPath: String, configPath: String) {
        val sanitizer = InputSanitizer()
        
        // Validate model path
        val modelResult = sanitizer.validateModelPath(modelPath)
        if (!modelResult.isValid) {
            throw IllegalArgumentException("Invalid model path: ${modelResult.errorMessage}")
        }
        
        // Validate config path
        val configResult = sanitizer.validateConfigPath(configPath)
        if (!configResult.isValid) {
            throw IllegalArgumentException("Invalid config path: ${configResult.errorMessage}")
        }
    }
}
