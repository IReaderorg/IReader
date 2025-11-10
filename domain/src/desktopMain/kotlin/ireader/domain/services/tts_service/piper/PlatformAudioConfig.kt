package ireader.domain.services.tts_service.piper

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Mixer

/**
 * Platform-specific audio configuration interface.
 * 
 * Provides platform-optimized audio settings for low-latency playback
 * and proper audio device handling on Windows, macOS, and Linux.
 */
interface PlatformAudioConfig {
    
    /**
     * Get the recommended buffer size for this platform
     */
    fun getBufferSize(): Int
    
    /**
     * Get platform-specific mixer preferences
     * Returns null to use system default
     */
    fun getPreferredMixer(): Mixer.Info?
    
    /**
     * Configure audio format with platform-specific optimizations
     */
    fun configureAudioFormat(
        sampleRate: Float,
        bitDepth: Int,
        channels: Int
    ): AudioFormat
    
    /**
     * Get platform name for logging
     */
    fun getPlatformName(): String
    
    /**
     * Perform any platform-specific initialization
     */
    fun initialize()
    
    /**
     * Perform any platform-specific cleanup
     */
    fun cleanup()
}

/**
 * Factory to create platform-specific audio configuration
 */
object PlatformAudioConfigFactory {
    
    private var cachedConfig: PlatformAudioConfig? = null
    
    /**
     * Get the appropriate audio configuration for the current platform
     */
    fun getConfig(): PlatformAudioConfig {
        if (cachedConfig != null) {
            return cachedConfig!!
        }
        
        val osName = System.getProperty("os.name").lowercase()
        
        cachedConfig = when {
            osName.contains("win") -> WindowsAudioConfig()
            osName.contains("mac") || osName.contains("darwin") -> MacOSAudioConfig()
            osName.contains("nux") || osName.contains("nix") -> LinuxAudioConfig()
            else -> DefaultAudioConfig() // Fallback
        }
        
        cachedConfig?.initialize()
        return cachedConfig!!
    }
    
    /**
     * Reset cached configuration (useful for testing)
     */
    fun reset() {
        cachedConfig?.cleanup()
        cachedConfig = null
    }
}

/**
 * Default audio configuration (fallback)
 */
class DefaultAudioConfig : PlatformAudioConfig {
    
    override fun getBufferSize(): Int = 8192 // 8KB default
    
    override fun getPreferredMixer(): Mixer.Info? = null
    
    override fun configureAudioFormat(
        sampleRate: Float,
        bitDepth: Int,
        channels: Int
    ): AudioFormat {
        return AudioFormat(
            sampleRate,
            bitDepth,
            channels,
            true, // signed
            false // little-endian
        )
    }
    
    override fun getPlatformName(): String = "Default"
    
    override fun initialize() {
        // No initialization needed
    }
    
    override fun cleanup() {
        // No cleanup needed
    }
}
