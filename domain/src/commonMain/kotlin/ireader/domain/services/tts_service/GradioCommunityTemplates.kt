package ireader.domain.services.tts_service

import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Community templates for popular Hugging Face & Gradio TTS architectures.
 * Allows 1-click schema scaffolding for user-added spaces.
 */
object GradioCommunityTemplates {

    val TEMPLATES = listOf(
        GradioTTSConfig(
            id = "template_kokoro",
            name = "Kokoro 82M Neural TTS",
            spaceUrl = "https://hexgrad-kokoro-tts.hf.space",
            apiName = "/predict",
            parameters = listOf(
                GradioParam.textParam("text"),
                GradioParam.choiceParam(
                    name = "voice",
                    choices = listOf(
                        "af_heart", "af_bella", "af_sarah", "af_nicole", "af_sky",
                        "am_adam", "am_michael", "am_george", "am_eric",
                        "bf_emma", "bf_isabella", "bf_alice", "bf_lily",
                        "bm_george", "bm_lewis", "bm_daniel"
                    ),
                    defaultValue = "af_heart"
                ),
                GradioParam.speedParam("speed", 1.0f, 0.5f, 2.0f)
            ),
            audioOutputIndex = 0,
            isCustom = true,
            description = "Lightweight 82M ultra-fast open neural TTS with rich voice variants.",
            apiType = GradioApiType.AUTO
        ),
        GradioTTSConfig(
            id = "template_xtts",
            name = "XTTS v2 Multilingual Cloning",
            spaceUrl = "https://coqui-xtts.hf.space",
            apiName = "/predict",
            parameters = listOf(
                GradioParam.textParam("text"),
                GradioParam.choiceParam(
                    name = "language",
                    choices = listOf("en", "es", "fr", "de", "it", "pt", "pl", "tr", "ru", "nl", "cs", "ar", "zh", "ja", "ko", "hi"),
                    defaultValue = "en"
                ),
                GradioParam.speedParam("speed", 1.0f, 0.5f, 2.0f)
            ),
            audioOutputIndex = 0,
            isCustom = true,
            description = "Coqui XTTS v2 multilingual voice cloning model supporting 17+ languages.",
            apiType = GradioApiType.AUTO
        ),
        GradioTTSConfig(
            id = "template_edge",
            name = "Microsoft Edge TTS",
            spaceUrl = "https://r3gm-edge-tts.hf.space",
            apiName = "/predict",
            parameters = listOf(
                GradioParam.textParam("text"),
                GradioParam.choiceParam(
                    name = "voice",
                    choices = listOf(
                        "en-US-AriaNeural", "en-US-GuyNeural", "en-US-JennyNeural", "en-US-ChristopherNeural",
                        "en-GB-SoniaNeural", "en-GB-RyanNeural", "es-ES-ElviraNeural", "fr-FR-DeniseNeural",
                        "de-DE-KatjaNeural", "ja-JP-NanamiNeural", "zh-CN-XiaoxiaoNeural"
                    ),
                    defaultValue = "en-US-AriaNeural"
                ),
                GradioParam.speedParam("rate", 1.0f, 0.5f, 2.0f)
            ),
            audioOutputIndex = 0,
            isCustom = true,
            description = "Natural multilingual online neural voices from Microsoft Edge.",
            apiType = GradioApiType.AUTO
        ),
        GradioTTSConfig(
            id = "template_bark",
            name = "Suno Bark Expressive TTS",
            spaceUrl = "https://suno-bark.hf.space",
            apiName = "/predict",
            parameters = listOf(
                GradioParam.textParam("text"),
                GradioParam.choiceParam(
                    name = "speaker",
                    choices = listOf(
                        "v2/en_speaker_6", "v2/en_speaker_9", "v2/en_speaker_0",
                        "v2/en_speaker_1", "v2/en_speaker_2", "v2/en_speaker_3",
                        "v2/en_speaker_4", "v2/en_speaker_5", "v2/en_speaker_7", "v2/en_speaker_8"
                    ),
                    defaultValue = "v2/en_speaker_6"
                )
            ),
            audioOutputIndex = 0,
            isCustom = true,
            description = "Highly expressive transformer text-to-audio model with ambient nuances.",
            apiType = GradioApiType.AUTO
        ),
        GradioTTSConfig(
            id = "template_parler",
            name = "Parler TTS Mini",
            spaceUrl = "https://parler-tts-parler-tts-mini.hf.space",
            apiName = "/predict",
            parameters = listOf(
                GradioParam.textParam("prompt"),
                GradioParam.stringParam("description", "A female speaker delivers a clear and expressive reading.")
            ),
            audioOutputIndex = 0,
            isCustom = true,
            description = "Customizable tone and pitch text-to-speech controlled by natural language descriptions.",
            apiType = GradioApiType.AUTO
        ),
        GradioTTSConfig(
            id = "template_standard",
            name = "Standard Gradio Space",
            spaceUrl = "",
            apiName = "/predict",
            parameters = listOf(
                GradioParam.textParam("text"),
                GradioParam.speedParam("speed", 1.0f, 0.5f, 2.0f)
            ),
            audioOutputIndex = 0,
            isCustom = true,
            description = "Standard Gradio text-to-speech with text input and speed multiplier.",
            apiType = GradioApiType.AUTO
        )
    )

    fun createFromTemplate(template: GradioTTSConfig): GradioTTSConfig {
        return template.copy(
            id = "custom_${currentTimeToLong()}",
            isCustom = true
        )
    }
}
