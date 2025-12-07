package ireader.domain.services.tts_service

import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Predefined configurations for popular Gradio TTS spaces.
 * Users can select from these presets or create custom configurations.
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
        apiName = "/text_to_speech",  // Function name for /gradio_api/call/{fn_name}
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.speedParam("speed", 1.0f, 0.5f, 2.0f)
        ),
        audioOutputIndex = 0,
        description = "High-quality English TTS powered by Coqui (fast_pitch model)",
        apiType = GradioApiType.GRADIO_API_CALL  // Use /gradio_api/call/ endpoint
    )
    
    /**
     * Persian TTS using Piper
     * Example: gyroing/persian-tts-piper
     */
    val PERSIAN_PIPER = GradioTTSConfig(
        id = "persian_piper",
        name = "Persian TTS (Piper)",
        spaceUrl = "https://gyroing-persian-tts-piper.hf.space",
        apiName = "/synthesize_speech",
        parameters = listOf(
            GradioParam.textParam("text")
        ),
        audioOutputIndex = 0,
        description = "Persian language TTS using Piper voices"
    )
    
    /**
     * Edge TTS - Microsoft Edge's TTS service
     * Many spaces provide Edge TTS wrappers
     */
    val EDGE_TTS = GradioTTSConfig(
        id = "edge_tts",
        name = "Edge TTS",
        spaceUrl = "https://innoai-edge-tts.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.choiceParam(
                name = "voice",
                choices = listOf(
                    "en-US-AriaNeural",
                    "en-US-GuyNeural",
                    "en-GB-SoniaNeural",
                    "en-AU-NatashaNeural"
                ),
                defaultValue = "en-US-AriaNeural"
            ),
            GradioParam.speedParam("rate", 1.0f, 0.5f, 2.0f)
        ),
        audioOutputIndex = 0,
        description = "Microsoft Edge TTS with multiple voices"
    )
    
    /**
     * Bark TTS - Suno's text-to-audio model
     */
    val BARK_TTS = GradioTTSConfig(
        id = "bark_tts",
        name = "Bark TTS",
        spaceUrl = "https://suno-bark.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.choiceParam(
                name = "voice_preset",
                choices = listOf(
                    "v2/en_speaker_0",
                    "v2/en_speaker_1",
                    "v2/en_speaker_2",
                    "v2/en_speaker_3"
                ),
                defaultValue = "v2/en_speaker_0"
            )
        ),
        audioOutputIndex = 0,
        description = "High-quality generative TTS by Suno"
    )
    
    /**
     * XTTS v2 - Coqui's latest multilingual model
     */
    val XTTS_V2 = GradioTTSConfig(
        id = "xtts_v2",
        name = "XTTS v2",
        spaceUrl = "https://coqui-xtts.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.choiceParam(
                name = "language",
                choices = listOf("en", "es", "fr", "de", "it", "pt", "pl", "tr", "ru", "nl", "cs", "ar", "zh-cn", "ja", "ko"),
                defaultValue = "en"
            )
        ),
        audioOutputIndex = 0,
        description = "Coqui's latest multilingual TTS with voice cloning"
    )
    
    /**
     * Parler TTS - Describe the voice you want
     */
    val PARLER_TTS = GradioTTSConfig(
        id = "parler_tts",
        name = "Parler TTS",
        spaceUrl = "https://parler-tts-parler-tts-mini.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.stringParam(
                name = "description",
                defaultValue = "A female speaker with a clear and pleasant voice"
            )
        ),
        audioOutputIndex = 0,
        description = "Describe the voice style you want"
    )
    
    /**
     * MMS TTS - Meta's Massively Multilingual Speech
     */
    val MMS_TTS = GradioTTSConfig(
        id = "mms_tts",
        name = "MMS TTS (Meta)",
        spaceUrl = "https://facebook-mms-tts.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.choiceParam(
                name = "language",
                choices = listOf("eng", "fra", "deu", "spa", "ita", "por", "rus", "ara", "hin", "jpn", "kor", "cmn"),
                defaultValue = "eng"
            )
        ),
        audioOutputIndex = 0,
        description = "Meta's multilingual TTS supporting 1000+ languages"
    )
    
    /**
     * StyleTTS 2 - High-quality expressive TTS
     */
    val STYLE_TTS_2 = GradioTTSConfig(
        id = "style_tts_2",
        name = "StyleTTS 2",
        spaceUrl = "https://styletts2-styletts2.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.floatParam(
                name = "alpha",
                defaultValue = 0.3f,
                min = 0.0f,
                max = 1.0f
            ),
            GradioParam.floatParam(
                name = "beta",
                defaultValue = 0.7f,
                min = 0.0f,
                max = 1.0f
            ),
            GradioParam.floatParam(
                name = "diffusion_steps",
                defaultValue = 5f,
                min = 1f,
                max = 20f
            )
        ),
        audioOutputIndex = 0,
        description = "High-quality expressive TTS with style control"
    )
    
    /**
     * Tortoise TTS - Slow but high quality
     */
    val TORTOISE_TTS = GradioTTSConfig(
        id = "tortoise_tts",
        name = "Tortoise TTS",
        spaceUrl = "https://jbetker-tortoise-tts.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.choiceParam(
                name = "voice",
                choices = listOf("random", "angie", "deniro", "freeman", "halle", "lj", "myself", "pat", "snakes", "tom", "train_atkins", "train_daws", "train_dotrice", "train_dreams", "train_empire", "train_grace", "train_kennard", "train_lescault", "train_mouse", "weaver", "william"),
                defaultValue = "random"
            ),
            GradioParam.choiceParam(
                name = "preset",
                choices = listOf("ultra_fast", "fast", "standard", "high_quality"),
                defaultValue = "fast"
            )
        ),
        audioOutputIndex = 0,
        description = "High-quality TTS with many voice options (slower)"
    )
    
    /**
     * Silero TTS - Fast and lightweight
     */
    val SILERO_TTS = GradioTTSConfig(
        id = "silero_tts",
        name = "Silero TTS",
        spaceUrl = "https://silero-silero-tts.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.choiceParam(
                name = "language",
                choices = listOf("en", "de", "es", "fr", "ru", "ua", "uz", "xal", "indic"),
                defaultValue = "en"
            ),
            GradioParam.choiceParam(
                name = "speaker",
                choices = listOf("en_0", "en_1", "en_2", "en_3", "en_4"),
                defaultValue = "en_0"
            )
        ),
        audioOutputIndex = 0,
        description = "Fast and lightweight TTS with multiple languages"
    )
    
    /**
     * OpenVoice - Voice cloning TTS
     */
    val OPEN_VOICE = GradioTTSConfig(
        id = "open_voice",
        name = "OpenVoice",
        spaceUrl = "https://myshell-ai-openvoice.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.choiceParam(
                name = "style",
                choices = listOf("default", "whispering", "shouting", "excited", "cheerful", "terrified", "angry", "sad", "friendly"),
                defaultValue = "default"
            ),
            GradioParam.choiceParam(
                name = "language",
                choices = listOf("EN", "ES", "FR", "ZH", "JP", "KR"),
                defaultValue = "EN"
            )
        ),
        audioOutputIndex = 0,
        description = "Voice cloning TTS with emotion control"
    )
    
    /**
     * Fish Speech - Fast multilingual TTS
     */
    val FISH_SPEECH = GradioTTSConfig(
        id = "fish_speech",
        name = "Fish Speech",
        spaceUrl = "https://fishaudio-fish-speech-1.hf.space",
        apiName = "/predict",
        parameters = listOf(
            GradioParam.textParam("text"),
            GradioParam.choiceParam(
                name = "language",
                choices = listOf("en", "zh", "ja", "ko"),
                defaultValue = "en"
            )
        ),
        audioOutputIndex = 0,
        description = "Fast multilingual TTS with natural prosody"
    )
    
    /**
     * Get all available presets
     */
    fun getAllPresets(): List<GradioTTSConfig> = listOf(
        COQUI_IREADER,
        EDGE_TTS,
        XTTS_V2,
        STYLE_TTS_2,
        SILERO_TTS,
        OPEN_VOICE,
        FISH_SPEECH,
        PARLER_TTS,
        MMS_TTS,
        TORTOISE_TTS,
        BARK_TTS,
        PERSIAN_PIPER
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
