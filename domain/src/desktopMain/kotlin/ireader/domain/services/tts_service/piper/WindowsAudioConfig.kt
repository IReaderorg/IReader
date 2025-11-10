package ireader.domain.services.tts_service.piper

import ireader.core.log.Log
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Mixer

/**
 * Windows-specific audio configuration.
 * 
 * Optimized for Windows Audio Session API (WASAPI) when available,
 * providing low-latency audio playback on Windows platforms.
 * 
 * Key features:
 * - Smaller buffer sizes for lower latency (4KB vs 8KB default)
 * - Preference for DirectSound mixers when available
 * - Support for Windows-specific audio formats
 */
class WindowsAudioConfig : PlatformAudioConfig {
    
    companion object {
        // Windows-optimized buffer size for low latency
        // WASAPI typically works well with 4KB buffers
        private const val WINDOWS_BUFFER_SIZE = 4096
        
        // Preferred mixer names (in order of preference)
        private val PREFERRED_MIXER_NAMES = listOf(
            "Primary Sound Driver",
            "DirectSound",
            "Windows DirectSound"
        )
    }
    
    private var preferredMixer: Mixer.Info? = null
    
    override fun getBufferSize(): Int = WINDOWS_BUFFER_SIZE
    
    override fun getPreferredMixer(): Mixer.Info? = preferredMixer
    
    override fun configureAudioFormat(
        sampleRate: Float,
        bitDepth: Int,
        channels: Int
    ): AudioFormat {
        // Windows prefers little-endian format
        return AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sampleRate,
            bitDepth,
            channels,
            (bitDepth / 8) * channels, // frame size
            sampleRate,
            false // little-endian (Windows native)
        )
    }
    
    override fun getPlatformName(): String = "Windows (WASAPI)"
    
    override fun initialize() {
        Log.debug { "Initializing Windows audio configuration" }
        
        // Find the best available mixer for Windows
        preferredMixer = findBestMixer()
        
        if (preferredMixer != null) {
            Log.info { "Windows audio: Using mixer '${preferredMixer!!.name}'" }
        } else {
            Log.warn { "Windows audio: No preferred mixer found, using system default" }
        }
        
        // Log available mixers for debugging
        logAvailableMixers()
    }
    
    override fun cleanup() {
        Log.debug { "Cleaning up Windows audio configuration" }
        preferredMixer = null
    }
    
    /**
     * Find the best available audio mixer for Windows.
     * Prefers DirectSound mixers for better performance.
     */
    private fun findBestMixer(): Mixer.Info? {
        val mixers = AudioSystem.getMixerInfo()
        
        // Try to find preferred mixers in order
        for (preferredName in PREFERRED_MIXER_NAMES) {
            val mixer = mixers.firstOrNull { 
                it.name.contains(preferredName, ignoreCase = true) 
            }
            if (mixer != null) {
                return mixer
            }
        }
        
        // If no preferred mixer found, look for any DirectSound mixer
        val directSoundMixer = mixers.firstOrNull { 
            it.name.contains("DirectSound", ignoreCase = true) ||
            it.name.contains("Direct Sound", ignoreCase = true)
        }
        
        return directSoundMixer
    }
    
    /**
     * Log available audio mixers for debugging purposes
     */
    private fun logAvailableMixers() {
        try {
            val mixers = AudioSystem.getMixerInfo()
            Log.debug { "Available Windows audio mixers (${mixers.size}):" }
            mixers.forEachIndexed { index, mixer ->
                Log.debug { "  [$index] ${mixer.name} - ${mixer.description}" }
            }
        } catch (e: Exception) {
            Log.warn { "Failed to enumerate audio mixers: ${e.message}" }
        }
    }
}
