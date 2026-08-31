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
     * Microsoft Edge TTS - Fast, natural neural voices across dozens of languages
     */
    val EDGE_TTS = GradioTTSConfig(
        id = "edge_tts_cloud",
        name = "Microsoft Edge TTS (Cloud)",
        spaceUrl = "https://r3gm-edge-tts.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.choiceParam("voice", listOf("en-US-AriaNeural", "en-US-GuyNeural", "en-US-JennyNeural", "en-GB-SoniaNeural", "en-GB-RyanNeural"), "en-US-AriaNeural"),
            GradioParam.speedParam("rate", 1.0f, 0.5f, 2.0f)
        ),
        audioOutputIndex = 0,
        description = "Natural multilingual neural voices powered by Microsoft Edge online service.",
        apiType = GradioApiType.AUTO
    )

    /**
     * Kokoro-82M TTS - Lightweight, ultra-fast 82M open neural TTS
     */
    val KOKORO_CLOUD = GradioTTSConfig(
        id = "kokoro_cloud",
        name = "Kokoro 82M (Cloud)",
        spaceUrl = "https://hexgrad-kokoro-tts.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.choiceParam("voice", listOf("af_heart", "af_bella", "af_sarah", "am_adam", "am_michael", "bf_emma", "bm_george"), "af_heart"),
            GradioParam.speedParam("speed", 1.0f, 0.5f, 2.0f)
        ),
        audioOutputIndex = 0,
        description = "Next-generation ultra-realistic neural TTS model (Kokoro-82M).",
        apiType = GradioApiType.AUTO
    )

    /**
     * Coqui XTTS v2 - State-of-the-art multilingual voice cloning model
     */
    val XTTS_V2 = GradioTTSConfig(
        id = "xtts_v2_cloud",
        name = "XTTS v2 (Cloud Multilingual)",
        spaceUrl = "https://coqui-xtts.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.choiceParam("language", listOf("en", "es", "fr", "de", "it", "pt", "pl", "tr", "ru", "nl", "cs", "ar", "zh", "ja", "ko"), "en"),
            GradioParam.speedParam("speed", 1.0f, 0.5f, 2.0f)
        ),
        audioOutputIndex = 0,
        description = "Coqui XTTS v2 supporting 17+ languages with natural pacing and expression.",
        apiType = GradioApiType.AUTO
    )

    /**
     * Suno Bark TTS - Expressive neural TTS with ambient nuance
     */
    val BARK_TTS = GradioTTSConfig(
        id = "bark_cloud",
        name = "Suno Bark (Cloud)",
        spaceUrl = "https://suno-bark.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.choiceParam("speaker", listOf("v2/en_speaker_6", "v2/en_speaker_9", "v2/en_speaker_0"), "v2/en_speaker_6")
        ),
        audioOutputIndex = 0,
        description = "Transformer-based text-to-audio model capable of highly expressive speech.",
        apiType = GradioApiType.AUTO
    )

    /**
     * Parler TTS - High-fidelity customizable speech synthesis
     */
    val PARLER_TTS = GradioTTSConfig(
        id = "parler_cloud",
        name = "Parler TTS (Cloud)",
        spaceUrl = "https://parler-tts-parler-tts-mini.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("prompt"),
            GradioParam.stringParam("description", "A female speaker delivers a clear and expressive reading.")
        ),
        audioOutputIndex = 0,
        description = "Parler Mini model with natural tone and pitch control.",
        apiType = GradioApiType.AUTO
    )
    
    /**
     * Get all available built-in presets.
     * Additional TTS engines are available as plugins.
     */
    fun getAllPresets(): List<GradioTTSConfig> = listOf(
        COQUI_IREADER,
        EDGE_TTS,
        KOKORO_CLOUD,
        XTTS_V2,
        BARK_TTS,
        PARLER_TTS
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
