package ireader.domain.services.tts_service

import kotlinx.serialization.Serializable

/**
 * Configuration for a Gradio-based TTS engine.
 * Supports any Gradio TTS space with customizable API parameters.
 */
@Serializable
data class GradioTTSConfig(
    /** Unique identifier for this configuration */
    val id: String,
    /** Display name shown in UI */
    val name: String,
    /** Hugging Face Space URL or self-hosted Gradio URL */
    val spaceUrl: String,
    /** API endpoint name (e.g., "/synthesize_speech", "/text_to_speech") */
    val apiName: String,
    /** Input parameters for the API */
    val parameters: List<GradioParam> = listOf(GradioParam.textParam()),
    /** Which output index contains the audio file (0-based) */
    val audioOutputIndex: Int = 0,
    /** Optional API key for private spaces */
    val apiKey: String? = null,
    /** Whether this is a user-created custom config */
    val isCustom: Boolean = false,
    /** Whether this config is enabled */
    val enabled: Boolean = true,
    /** Speech speed multiplier (if supported) */
    val defaultSpeed: Float = 1.0f,
    /** Description of the TTS engine */
    val description: String = ""
)

/**
 * Represents a parameter for a Gradio API call.
 */
@Serializable
data class GradioParam(
    /** Parameter name (for display/documentation) */
    val name: String,
    /** Parameter type */
    val type: GradioParamType,
    /** Default value (as string, will be converted based on type) */
    val defaultValue: String? = null,
    /** Whether this parameter receives the text to synthesize */
    val isTextInput: Boolean = false,
    /** Whether this parameter receives the speed value */
    val isSpeedInput: Boolean = false,
    /** Min value for numeric types */
    val minValue: Float? = null,
    /** Max value for numeric types */
    val maxValue: Float? = null,
    /** Available options for choice type */
    val choices: List<String>? = null
) {
    companion object {
        /** Create a text input parameter */
        fun textParam(name: String = "text") = GradioParam(
            name = name,
            type = GradioParamType.STRING,
            isTextInput = true
        )
        
        /** Create a speed parameter */
        fun speedParam(
            name: String = "speed",
            defaultValue: Float = 1.0f,
            min: Float = 0.5f,
            max: Float = 2.0f
        ) = GradioParam(
            name = name,
            type = GradioParamType.FLOAT,
            defaultValue = defaultValue.toString(),
            isSpeedInput = true,
            minValue = min,
            maxValue = max
        )
        
        /** Create a string parameter with default value */
        fun stringParam(name: String, defaultValue: String) = GradioParam(
            name = name,
            type = GradioParamType.STRING,
            defaultValue = defaultValue
        )
        
        /** Create a float parameter */
        fun floatParam(
            name: String,
            defaultValue: Float,
            min: Float? = null,
            max: Float? = null
        ) = GradioParam(
            name = name,
            type = GradioParamType.FLOAT,
            defaultValue = defaultValue.toString(),
            minValue = min,
            maxValue = max
        )
        
        /** Create a choice parameter */
        fun choiceParam(name: String, choices: List<String>, defaultValue: String) = GradioParam(
            name = name,
            type = GradioParamType.CHOICE,
            defaultValue = defaultValue,
            choices = choices
        )
    }
}

/**
 * Supported parameter types for Gradio APIs.
 */
@Serializable
enum class GradioParamType {
    STRING,
    FLOAT,
    INT,
    BOOLEAN,
    CHOICE
}
