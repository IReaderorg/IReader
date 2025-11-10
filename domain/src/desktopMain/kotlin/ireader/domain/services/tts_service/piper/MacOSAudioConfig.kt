package ireader.domain.services.tts_service.piper

import ireader.core.log.Log
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Mixer

/**
 * macOS-specific audio configuration.
 * 
 * Optimized for Core Audio framework on macOS, providing low-latency
 * audio playback on both Intel and Apple Silicon Macs.
 * 
 * Key features:
 * - Optimized buffer size for Core Audio (4KB)
 * - Native big-endian format support (macOS preference)
 * - Automatic detection of Core Audio mixers
 * - Support for both x64 (Intel) and arm64 (Apple Silicon)
 */
class MacOSAudioConfig : PlatformAudioConfig {
    
    companion object {
        // macOS-optimized buffer size for low latency with Core Audio
        private const val MACOS_BUFFER_SIZE = 4096
        
        // Core Audio mixer identifiers
        private val CORE_AUDIO_MIXER_NAMES = listOf(
            "Default Audio Device",
            "Built-in Output",
            "Core Audio"
        )
    }
    
    private var preferredMixer: Mixer.Info? = null
    private var architecture: String = "unknown"
    
    override fun getBufferSize(): Int = MACOS_BUFFER_SIZE
    
    override fun getPreferredMixer(): Mixer.Info? = preferredMixer
    
    override fun configureAudioFormat(
        sampleRate: Float,
        bitDepth: Int,
        channels: Int
    ): AudioFormat {
        // macOS Core Audio traditionally prefers big-endian, but modern
        // versions work well with little-endian too. We'll use little-endian
        // for consistency with Piper's output format.
        return AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sampleRate,
            bitDepth,
            channels,
            (bitDepth / 8) * channels, // frame size
            sampleRate,
            false // little-endian for consistency
        )
    }
    
    override fun getPlatformName(): String {
        return "macOS Core Audio ($architecture)"
    }
    
    override fun initialize() {
        Log.debug { "Initializing macOS audio configuration" }
        
        // Detect architecture
        architecture = detectArchitecture()
        Log.info { "macOS architecture detected: $architecture" }
        
        // Find the best available mixer for macOS
        preferredMixer = findBestMixer()
        
        if (preferredMixer != null) {
            Log.info { "macOS audio: Using mixer '${preferredMixer!!.name}'" }
        } else {
            Log.warn { "macOS audio: No preferred mixer found, using system default" }
        }
        
        // Check for Gatekeeper issues
        checkGatekeeperStatus()
        
        // Log available mixers for debugging
        logAvailableMixers()
    }
    
    override fun cleanup() {
        Log.debug { "Cleaning up macOS audio configuration" }
        preferredMixer = null
    }
    
    /**
     * Detect the macOS architecture (Intel x64 or Apple Silicon arm64)
     */
    private fun detectArchitecture(): String {
        val osArch = System.getProperty("os.arch").lowercase()
        return when {
            osArch.contains("aarch64") || osArch.contains("arm") -> "arm64 (Apple Silicon)"
            osArch.contains("x86_64") || osArch.contains("amd64") -> "x64 (Intel)"
            else -> osArch
        }
    }
    
    /**
     * Find the best available audio mixer for macOS.
     * Prefers Core Audio mixers for optimal performance.
     */
    private fun findBestMixer(): Mixer.Info? {
        val mixers = AudioSystem.getMixerInfo()
        
        // Try to find Core Audio mixers in order of preference
        for (preferredName in CORE_AUDIO_MIXER_NAMES) {
            val mixer = mixers.firstOrNull { 
                it.name.contains(preferredName, ignoreCase = true) 
            }
            if (mixer != null) {
                return mixer
            }
        }
        
        // If no preferred mixer found, look for any output mixer
        val outputMixer = mixers.firstOrNull { 
            it.name.contains("Output", ignoreCase = true) ||
            it.description.contains("playback", ignoreCase = true)
        }
        
        return outputMixer
    }
    
    /**
     * Check for potential Gatekeeper issues with native libraries
     */
    private fun checkGatekeeperStatus() {
        try {
            // Try to detect if we're running in a signed/notarized app
            val javaHome = System.getProperty("java.home")
            Log.debug { "Java home: $javaHome" }
            
            // Log a warning about Gatekeeper if this appears to be a distributed app
            if (javaHome.contains("/Applications/")) {
                Log.info { 
                    "Running from Applications folder. Ensure native libraries are " +
                    "properly signed for Gatekeeper compatibility."
                }
            }
        } catch (e: Exception) {
            Log.debug { "Could not check Gatekeeper status: ${e.message}" }
        }
    }
    
    /**
     * Log available audio mixers for debugging purposes
     */
    private fun logAvailableMixers() {
        try {
            val mixers = AudioSystem.getMixerInfo()
            Log.debug { "Available macOS audio mixers (${mixers.size}):" }
            mixers.forEachIndexed { index, mixer ->
                Log.debug { "  [$index] ${mixer.name} - ${mixer.description}" }
                
                // Log additional details for Core Audio devices
                try {
                    val mixerInstance = AudioSystem.getMixer(mixer)
                    val sourceLines = mixerInstance.sourceLineInfo.size
                    val targetLines = mixerInstance.targetLineInfo.size
                    Log.debug { "        Source lines: $sourceLines, Target lines: $targetLines" }
                } catch (e: Exception) {
                    // Ignore errors when querying mixer details
                }
            }
        } catch (e: Exception) {
            Log.warn { "Failed to enumerate audio mixers: ${e.message}" }
        }
    }
}
