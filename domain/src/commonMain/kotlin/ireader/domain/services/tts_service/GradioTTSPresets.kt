package ireader.domain.services.tts_service

import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Predefined configurations for Gradio TTS spaces.
 * 
 * Note: Most TTS engines have been moved to plugins in IReader-plugins.
 * Install plugins from the Feature Store for additional TTS options:
 * - Edge TTS (Gradio)
 * - Persian TTS (Edge, Chatterbox, Piper, XTTS)
 * - XTTS v2
 * - Bark TTS
 * - Parler TTS
 * - StyleTTS 2
 * - Tortoise TTS
 * - Silero TTS
 * - OpenVoice
 * - Fish Speech
 */
object GradioTTSPresets {
    
    /**
     * Coqui TTS - High quality multilingual TTS
     * Default IReader Hugging Face Space
     * Uses gr.Interface with text_to_speech function
     * API: /gradio_api/call/text_to_speech (Gradio 4.x with SSE)
     */
    val COQUI_IREADER = GradioTTSConfig(
        id = "coqui_ireader",
        name = "Coqui TTS (IReader)",
        spaceUrl = "https://kazemcodes-ireader.hf.space",
        apiName = "/text_to_speech",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.speedParam("speed", 1.0f, 0.5f, 2.0f)
        ),
        audioOutputIndex = 0,
        description = "High-quality English TTS powered by Coqui (fast_pitch model). Default IReader TTS engine.",
        apiType = GradioApiType.GRADIO_API_CALL
    )
    
    /**
     * Get all available built-in presets.
     * Additional TTS engines are available as plugins.
     */
    fun getAllPresets(): List<GradioTTSConfig> = listOf(
        COQUI_IREADER
    )
    
    /**
     * Get preset by ID
     */
    fun getPresetById(id: String): GradioTTSConfig? = getAllPresets().find { it.id == id }
    
    /**
     * Create a blank custom config template
     */
    fun createCustomTemplate(
        id: String = "custom_${currentTimeToLong()}",
        name: String = "Custom TTS"
    ) = GradioTTSConfig(
        id = id,
        name = name,
        spaceUrl = "",
        apiName = "/predict",
        parameters = listOf(GradioParam.textParam()),
        isCustom = true,
        description = "User-defined Gradio TTS configuration"
    )
}
