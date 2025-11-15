package ireader.domain.services.tts

import kotlinx.serialization.Serializable

/**
 * Voice configuration for TTS playback
 * Requirements: 5.3, 5.5
 */
@Serializable
data class VoiceConfiguration(
    val voiceId: String,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val volume: Float = 1.0f,
    val enableStreaming: Boolean = true
) {
    companion object {
        val DEFAULT = VoiceConfiguration(
            voiceId = "",
            speed = 1.0f,
            pitch = 1.0f,
            volume = 1.0f,
            enableStreaming = true
        )
        
        // Speed range
        const val MIN_SPEED = 0.5f
        const val MAX_SPEED = 2.0f
        const val DEFAULT_SPEED = 1.0f
        
        // Pitch range
        const val MIN_PITCH = 0.5f
        const val MAX_PITCH = 2.0f
        const val DEFAULT_PITCH = 1.0f
        
        // Volume range
        const val MIN_VOLUME = 0.0f
        const val MAX_VOLUME = 1.0f
        const val DEFAULT_VOLUME = 1.0f
    }
    
    /**
     * Validate and clamp configuration values
     */
    fun validate(): VoiceConfiguration {
        return copy(
            speed = speed.coerceIn(MIN_SPEED, MAX_SPEED),
            pitch = pitch.coerceIn(MIN_PITCH, MAX_PITCH),
            volume = volume.coerceIn(MIN_VOLUME, MAX_VOLUME)
        )
    }
    
    /**
     * Check if configuration is at default values
     */
    fun isDefault(): Boolean {
        return speed == DEFAULT_SPEED &&
                pitch == DEFAULT_PITCH &&
                volume == DEFAULT_VOLUME &&
                enableStreaming
    }
}

/**
 * Voice filter options for voice selection UI
 * Requirements: 5.1
 */
data class VoiceFilter(
    val language: String? = null,
    val gender: String? = null,
    val quality: String? = null,
    val sourceType: VoiceSourceType? = null,
    val searchQuery: String? = null
)

/**
 * Voice source type for filtering
 */
enum class VoiceSourceType {
    BUILT_IN,
    PLUGIN,
    ALL
}

/**
 * Voice selection state for UI
 * Requirements: 5.1, 5.2, 5.3
 */
data class VoiceSelectionState(
    val availableVoices: List<VoiceWithSource> = emptyList(),
    val selectedVoiceId: String? = null,
    val configuration: VoiceConfiguration = VoiceConfiguration.DEFAULT,
    val filter: VoiceFilter = VoiceFilter(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPreviewing: Boolean = false
) {
    /**
     * Get filtered voices based on current filter
     */
    fun getFilteredVoices(): List<VoiceWithSource> {
        var filtered = availableVoices
        
        // Filter by language
        filter.language?.let { lang ->
            filtered = filtered.filter { it.voice.language == lang }
        }
        
        // Filter by gender
        filter.gender?.let { gender ->
            filtered = filtered.filter { it.voice.gender.name == gender }
        }
        
        // Filter by quality
        filter.quality?.let { quality ->
            filtered = filtered.filter { it.voice.quality.name == quality }
        }
        
        // Filter by source type
        filter.sourceType?.let { sourceType ->
            filtered = when (sourceType) {
                VoiceSourceType.BUILT_IN -> filtered.filter { it.source is VoiceSource.BuiltIn }
                VoiceSourceType.PLUGIN -> filtered.filter { it.source is VoiceSource.Plugin }
                VoiceSourceType.ALL -> filtered
            }
        }
        
        // Filter by search query
        filter.searchQuery?.let { query ->
            if (query.isNotBlank()) {
                filtered = filtered.filter { voiceWithSource ->
                    voiceWithSource.voice.name.contains(query, ignoreCase = true) ||
                            voiceWithSource.voice.description.contains(query, ignoreCase = true) ||
                            voiceWithSource.voice.tags.any { it.contains(query, ignoreCase = true) }
                }
            }
        }
        
        return filtered
    }
    
    /**
     * Get the currently selected voice
     */
    fun getSelectedVoice(): VoiceWithSource? {
        return selectedVoiceId?.let { id ->
            availableVoices.find { it.voice.id == id }
        }
    }
    
    /**
     * Get available languages
     */
    fun getAvailableLanguages(): List<String> {
        return availableVoices.map { it.voice.language }.distinct().sorted()
    }
    
    /**
     * Get available source types
     */
    fun getAvailableSourceTypes(): List<VoiceSourceType> {
        val hasBuiltIn = availableVoices.any { it.source is VoiceSource.BuiltIn }
        val hasPlugin = availableVoices.any { it.source is VoiceSource.Plugin }
        
        return buildList {
            add(VoiceSourceType.ALL)
            if (hasBuiltIn) add(VoiceSourceType.BUILT_IN)
            if (hasPlugin) add(VoiceSourceType.PLUGIN)
        }
    }
}
