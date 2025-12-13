package ireader.domain.services.tts

import ireader.domain.catalogs.VoiceCatalog
import ireader.domain.models.tts.VoiceModel
import ireader.domain.models.tts.VoiceGender
import ireader.domain.models.tts.VoiceQuality
import ireader.domain.plugins.AudioStream
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginType
import ireader.domain.plugins.TTSPlugin
import ireader.domain.plugins.VoiceConfig
import ireader.plugin.api.VoiceModel as PluginVoiceModel
import ireader.plugin.api.VoiceGender as PluginVoiceGender
import kotlinx.coroutines.flow.StateFlow

/**
 * Manager for integrating TTS plugins with the built-in TTS system
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
 */
class PluginTTSManager(
    private val pluginManager: PluginManager
) {
    /**
     * Get all available voices combining built-in and plugin voices
     * Requirements: 5.1, 5.2
     */
    fun getAvailableVoices(): List<VoiceWithSource> {
        val builtInVoices = VoiceCatalog.getAllVoices().map { voice ->
            VoiceWithSource(
                voice = voice,
                source = VoiceSource.BuiltIn
            )
        }
        
        val pluginVoices = getPluginVoices()
        
        return builtInVoices + pluginVoices
    }
    
    /**
     * Get voices from TTS plugins only
     * Requirements: 5.1, 5.2
     */
    fun getPluginVoices(): List<VoiceWithSource> {
        val ttsPlugins = pluginManager.getEnabledPlugins()
            .filterIsInstance<TTSPlugin>()
        
        return ttsPlugins.flatMap { plugin ->
            plugin.getAvailableVoices().map { pluginVoice ->
                VoiceWithSource(
                    voice = pluginVoice.toDomainVoiceModel(),
                    source = VoiceSource.Plugin(
                        pluginId = plugin.manifest.id,
                        pluginName = plugin.manifest.name
                    )
                )
            }
        }
    }
    
    /**
     * Convert plugin VoiceModel to domain VoiceModel
     */
    private fun PluginVoiceModel.toDomainVoiceModel(): VoiceModel {
        return VoiceModel(
            id = this.id,
            name = this.name,
            language = this.language.substringBefore("-"),
            locale = this.language,
            gender = when (this.gender) {
                PluginVoiceGender.MALE -> VoiceGender.MALE
                PluginVoiceGender.FEMALE -> VoiceGender.FEMALE
                PluginVoiceGender.NEUTRAL -> VoiceGender.NEUTRAL
            },
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = this.downloadSize ?: 0L,
            downloadUrl = "",
            configUrl = "",
            checksum = "",
            license = "Plugin",
            description = "Voice from plugin",
            tags = emptyList()
        )
    }
    
    /**
     * Get voices by language
     * Requirements: 5.1
     */
    fun getVoicesByLanguage(language: String): List<VoiceWithSource> {
        return getAvailableVoices().filter { it.voice.language == language }
    }
    
    /**
     * Find the plugin that provides a specific voice
     * Requirements: 5.2
     */
    fun findPluginForVoice(voiceId: String): TTSPlugin? {
        val ttsPlugins = pluginManager.getEnabledPlugins()
            .filterIsInstance<TTSPlugin>()
        
        return ttsPlugins.find { plugin ->
            plugin.getAvailableVoices().any { it.id == voiceId }
        }
    }
    
    /**
     * Speak text using the specified voice
     * Routes to plugin or built-in TTS based on voice source
     * Requirements: 5.2, 5.3, 5.4
     */
    suspend fun speak(
        text: String,
        voiceId: String,
        speed: Float = 1.0f,
        pitch: Float = 1.0f,
        volume: Float = 1.0f
    ): Result<TTSOutput> {
        // Find the voice and its source
        val voiceWithSource = getAvailableVoices().find { it.voice.id == voiceId }
            ?: return Result.failure(Exception("Voice not found: $voiceId"))
        
        return when (val source = voiceWithSource.source) {
            is VoiceSource.BuiltIn -> {
                // Use built-in TTS
                speakWithBuiltIn(text, voiceWithSource.voice, speed, pitch, volume)
            }
            is VoiceSource.Plugin -> {
                // Use plugin TTS
                speakWithPlugin(text, voiceId, source.pluginId, speed, pitch, volume)
            }
        }
    }
    
    /**
     * Speak using a plugin TTS engine
     * Requirements: 5.2, 5.3, 5.4
     */
    private suspend fun speakWithPlugin(
        text: String,
        voiceId: String,
        pluginId: String,
        speed: Float,
        pitch: Float,
        volume: Float
    ): Result<TTSOutput> {
        val plugin = pluginManager.getPlugin(pluginId) as? TTSPlugin
            ?: return Result.failure(Exception("TTS plugin not found: $pluginId"))
        
        val voiceConfig = VoiceConfig(
            voiceId = voiceId,
            speed = speed,
            pitch = pitch,
            volume = volume
        )
        
        return try {
            val audioStream = plugin.speak(text, voiceConfig).getOrThrow()
            val audioFormat = plugin.getAudioFormat()
            val supportsStreaming = plugin.supportsStreaming()
            
            Result.success(
                TTSOutput.Plugin(
                    audioStream = audioStream,
                    audioFormat = audioFormat,
                    supportsStreaming = supportsStreaming,
                    pluginId = pluginId
                )
            )
        } catch (e: Exception) {
            // Fallback to built-in TTS if plugin fails
            val voice = VoiceCatalog.getVoiceById(voiceId)
            if (voice != null) {
                speakWithBuiltIn(text, voice, speed, pitch, volume)
            } else {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Speak using built-in TTS
     * This is a placeholder that should be implemented by platform-specific code
     * Requirements: 5.4
     */
    private suspend fun speakWithBuiltIn(
        text: String,
        voice: VoiceModel,
        speed: Float,
        pitch: Float,
        volume: Float
    ): Result<TTSOutput> {
        // This would delegate to the platform-specific TTS implementation
        // For now, return a failure indicating built-in TTS needs to be implemented
        return Result.failure(
            Exception("Built-in TTS not implemented. Use platform-specific implementation.")
        )
    }
    
    /**
     * Preview a voice by speaking sample text
     * Requirements: 5.3
     */
    suspend fun previewVoice(
        voiceId: String,
        sampleText: String = "Hello, this is a voice preview."
    ): Result<TTSOutput> {
        return speak(
            text = sampleText,
            voiceId = voiceId,
            speed = 1.0f,
            pitch = 1.0f,
            volume = 1.0f
        )
    }
    
    /**
     * Check if a voice supports streaming
     * Requirements: 5.5
     */
    fun supportsStreaming(voiceId: String): Boolean {
        val plugin = findPluginForVoice(voiceId)
        return plugin?.supportsStreaming() ?: false
    }
    
    /**
     * Get all TTS plugins
     */
    fun getTTSPlugins(): List<TTSPlugin> {
        return pluginManager.getPluginsByType(PluginType.TTS)
            .filterIsInstance<TTSPlugin>()
    }
    
    /**
     * Get enabled TTS plugins
     */
    fun getEnabledTTSPlugins(): List<TTSPlugin> {
        return pluginManager.getEnabledPlugins()
            .filterIsInstance<TTSPlugin>()
    }
    
    /**
     * Observe plugin changes
     */
    fun observePlugins(): StateFlow<List<ireader.domain.plugins.PluginInfo>> {
        return pluginManager.pluginsFlow
    }
}

/**
 * Voice with its source information
 */
data class VoiceWithSource(
    val voice: VoiceModel,
    val source: VoiceSource
)

/**
 * Voice source - either built-in or from a plugin
 */
sealed class VoiceSource {
    object BuiltIn : VoiceSource()
    data class Plugin(
        val pluginId: String,
        val pluginName: String
    ) : VoiceSource()
}

/**
 * TTS output - either from plugin or built-in
 */
sealed class TTSOutput {
    data class Plugin(
        val audioStream: AudioStream,
        val audioFormat: ireader.domain.plugins.AudioFormat,
        val supportsStreaming: Boolean,
        val pluginId: String
    ) : TTSOutput()
    
    data class BuiltIn(
        val utteranceId: String
    ) : TTSOutput()
}
