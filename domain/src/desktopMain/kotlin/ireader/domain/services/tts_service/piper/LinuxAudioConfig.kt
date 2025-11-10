package ireader.domain.services.tts_service.piper

import ireader.core.log.Log
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Mixer
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Linux-specific audio configuration.
 * 
 * Supports both ALSA (Advanced Linux Sound Architecture) and PulseAudio,
 * automatically detecting the available audio server and configuring
 * accordingly.
 * 
 * Key features:
 * - Automatic detection of ALSA vs PulseAudio
 * - Standard buffer size for broad compatibility (8KB)
 * - Fallback to ALSA if PulseAudio unavailable
 * - Support for various Linux distributions
 */
class LinuxAudioConfig : PlatformAudioConfig {
    
    companion object {
        // Linux standard buffer size (balanced for compatibility)
        private const val LINUX_BUFFER_SIZE = 8192
        
        // Audio server types
        private enum class AudioServer {
            PULSEAUDIO,
            ALSA,
            UNKNOWN
        }
        
        // Preferred mixer names for PulseAudio
        private val PULSEAUDIO_MIXER_NAMES = listOf(
            "PulseAudio",
            "PulseAudio Mixer",
            "default"
        )
        
        // Preferred mixer names for ALSA
        private val ALSA_MIXER_NAMES = listOf(
            "default",
            "ALSA",
            "PCM"
        )
    }
    
    private var preferredMixer: Mixer.Info? = null
    private var audioServer: AudioServer = AudioServer.UNKNOWN
    private var distribution: String = "unknown"
    
    override fun getBufferSize(): Int = LINUX_BUFFER_SIZE
    
    override fun getPreferredMixer(): Mixer.Info? = preferredMixer
    
    override fun configureAudioFormat(
        sampleRate: Float,
        bitDepth: Int,
        channels: Int
    ): AudioFormat {
        // Linux (both ALSA and PulseAudio) works well with little-endian
        return AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sampleRate,
            bitDepth,
            channels,
            (bitDepth / 8) * channels, // frame size
            sampleRate,
            false // little-endian
        )
    }
    
    override fun getPlatformName(): String {
        val serverName = when (audioServer) {
            AudioServer.PULSEAUDIO -> "PulseAudio"
            AudioServer.ALSA -> "ALSA"
            AudioServer.UNKNOWN -> "Unknown"
        }
        return "Linux $serverName ($distribution)"
    }
    
    override fun initialize() {
        Log.debug { "Initializing Linux audio configuration" }
        
        // Detect Linux distribution
        distribution = detectDistribution()
        Log.info { "Linux distribution detected: $distribution" }
        
        // Detect audio server
        audioServer = detectAudioServer()
        Log.info { "Audio server detected: $audioServer" }
        
        // Find the best available mixer
        preferredMixer = findBestMixer()
        
        if (preferredMixer != null) {
            Log.info { "Linux audio: Using mixer '${preferredMixer!!.name}'" }
        } else {
            Log.warn { "Linux audio: No preferred mixer found, using system default" }
        }
        
        // Check audio server status
        checkAudioServerStatus()
        
        // Log available mixers for debugging
        logAvailableMixers()
    }
    
    override fun cleanup() {
        Log.debug { "Cleaning up Linux audio configuration" }
        preferredMixer = null
    }
    
    /**
     * Detect the Linux distribution
     */
    private fun detectDistribution(): String {
        try {
            // Try to read /etc/os-release
            val osRelease = java.io.File("/etc/os-release")
            if (osRelease.exists()) {
                osRelease.readLines().forEach { line ->
                    if (line.startsWith("PRETTY_NAME=")) {
                        return line.substringAfter("=").trim('"')
                    }
                }
            }
            
            // Fallback: try lsb_release command
            val process = Runtime.getRuntime().exec("lsb_release -d")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine()
            if (output != null) {
                return output.substringAfter(":").trim()
            }
        } catch (e: Exception) {
            Log.debug { "Could not detect Linux distribution: ${e.message}" }
        }
        
        return "Linux"
    }
    
    /**
     * Detect the active audio server (PulseAudio or ALSA)
     */
    private fun detectAudioServer(): AudioServer {
        // Check for PulseAudio
        if (isPulseAudioRunning()) {
            return AudioServer.PULSEAUDIO
        }
        
        // Check for ALSA devices
        if (isAlsaAvailable()) {
            return AudioServer.ALSA
        }
        
        return AudioServer.UNKNOWN
    }
    
    /**
     * Check if PulseAudio is running
     */
    private fun isPulseAudioRunning(): Boolean {
        try {
            // Check if pulseaudio process is running
            val process = Runtime.getRuntime().exec(arrayOf("pgrep", "-x", "pulseaudio"))
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Log.debug { "PulseAudio process detected" }
                return true
            }
            
            // Alternative: check if pactl command works
            val pactlProcess = Runtime.getRuntime().exec(arrayOf("pactl", "info"))
            val pactlExitCode = pactlProcess.waitFor()
            if (pactlExitCode == 0) {
                Log.debug { "PulseAudio detected via pactl" }
                return true
            }
        } catch (e: Exception) {
            Log.debug { "PulseAudio detection failed: ${e.message}" }
        }
        
        return false
    }
    
    /**
     * Check if ALSA is available
     */
    private fun isAlsaAvailable(): Boolean {
        try {
            // Check for ALSA device files
            val alsaDevices = java.io.File("/dev/snd")
            if (alsaDevices.exists() && alsaDevices.isDirectory) {
                val devices = alsaDevices.listFiles()
                if (devices != null && devices.isNotEmpty()) {
                    Log.debug { "ALSA devices found in /dev/snd" }
                    return true
                }
            }
            
            // Alternative: try aplay command
            val process = Runtime.getRuntime().exec(arrayOf("aplay", "-l"))
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Log.debug { "ALSA detected via aplay" }
                return true
            }
        } catch (e: Exception) {
            Log.debug { "ALSA detection failed: ${e.message}" }
        }
        
        return false
    }
    
    /**
     * Find the best available audio mixer for Linux
     */
    private fun findBestMixer(): Mixer.Info? {
        val mixers = AudioSystem.getMixerInfo()
        
        // Choose mixer names based on detected audio server
        val preferredNames = when (audioServer) {
            AudioServer.PULSEAUDIO -> PULSEAUDIO_MIXER_NAMES
            AudioServer.ALSA -> ALSA_MIXER_NAMES
            AudioServer.UNKNOWN -> PULSEAUDIO_MIXER_NAMES + ALSA_MIXER_NAMES
        }
        
        // Try to find preferred mixers in order
        for (preferredName in preferredNames) {
            val mixer = mixers.firstOrNull { 
                it.name.contains(preferredName, ignoreCase = true) 
            }
            if (mixer != null) {
                return mixer
            }
        }
        
        // If no preferred mixer found, look for any playback mixer
        val playbackMixer = mixers.firstOrNull { 
            it.description.contains("playback", ignoreCase = true) ||
            it.description.contains("output", ignoreCase = true)
        }
        
        return playbackMixer
    }
    
    /**
     * Check audio server status and log warnings if needed
     */
    private fun checkAudioServerStatus() {
        when (audioServer) {
            AudioServer.PULSEAUDIO -> {
                Log.info { "PulseAudio is active - using PulseAudio backend" }
            }
            AudioServer.ALSA -> {
                Log.info { "Using ALSA backend (PulseAudio not detected)" }
            }
            AudioServer.UNKNOWN -> {
                Log.warn { 
                    "Could not detect audio server. Audio playback may not work correctly. " +
                    "Please ensure ALSA or PulseAudio is installed and running."
                }
            }
        }
        
        // Check if user is in audio group (common permission issue)
        checkAudioGroupMembership()
    }
    
    /**
     * Check if the current user is in the audio group
     */
    private fun checkAudioGroupMembership() {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("groups"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val groups = reader.readLine()
            
            if (groups != null && !groups.contains("audio")) {
                Log.warn { 
                    "User is not in 'audio' group. This may cause audio playback issues. " +
                    "Run: sudo usermod -a -G audio \$USER"
                }
            }
        } catch (e: Exception) {
            Log.debug { "Could not check audio group membership: ${e.message}" }
        }
    }
    
    /**
     * Log available audio mixers for debugging purposes
     */
    private fun logAvailableMixers() {
        try {
            val mixers = AudioSystem.getMixerInfo()
            Log.debug { "Available Linux audio mixers (${mixers.size}):" }
            mixers.forEachIndexed { index, mixer ->
                Log.debug { "  [$index] ${mixer.name} - ${mixer.description}" }
                
                // Log additional details
                try {
                    val mixerInstance = AudioSystem.getMixer(mixer)
                    val sourceLines = mixerInstance.sourceLineInfo.size
                    val targetLines = mixerInstance.targetLineInfo.size
                    Log.debug { "        Source lines: $sourceLines, Target lines: $targetLines" }
                } catch (e: Exception) {
                    // Ignore errors when querying mixer details
                }
            }
            
            // Log audio server specific information
            logAudioServerInfo()
        } catch (e: Exception) {
            Log.warn { "Failed to enumerate audio mixers: ${e.message}" }
        }
    }
    
    /**
     * Log audio server specific information
     */
    private fun logAudioServerInfo() {
        when (audioServer) {
            AudioServer.PULSEAUDIO -> {
                try {
                    val process = Runtime.getRuntime().exec(arrayOf("pactl", "info"))
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    Log.debug { "PulseAudio server information:" }
                    reader.lines().forEach { line ->
                        if (line.contains("Server Name") || 
                            line.contains("Server Version") ||
                            line.contains("Default Sink")) {
                            Log.debug { "  $line" }
                        }
                    }
                } catch (e: Exception) {
                    Log.debug { "Could not get PulseAudio info: ${e.message}" }
                }
            }
            AudioServer.ALSA -> {
                try {
                    val process = Runtime.getRuntime().exec(arrayOf("aplay", "-l"))
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    Log.debug { "ALSA devices:" }
                    var lineCount = 0
                    reader.lines().forEach { line ->
                        if (lineCount < 5) { // Limit output
                            Log.debug { "  $line" }
                            lineCount++
                        }
                    }
                } catch (e: Exception) {
                    Log.debug { "Could not get ALSA info: ${e.message}" }
                }
            }
            AudioServer.UNKNOWN -> {
                // No additional info to log
            }
        }
    }
}
