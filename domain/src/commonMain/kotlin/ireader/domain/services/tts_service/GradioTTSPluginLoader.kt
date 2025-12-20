package ireader.domain.services.tts_service

import ireader.core.log.Log
import ireader.domain.plugins.PluginManager
import ireader.plugin.api.PluginManifest
import ireader.plugin.api.PluginType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Loads Gradio TTS configurations from installed GRADIO_TTS plugins.
 * 
 * Uses manifest metadata (plugin-api >= 1.1.0) for configuration.
 * 
 * Configuration keys:
 * - gradio.spaceUrl: Hugging Face Space URL (required)
 * - gradio.apiName: API endpoint name (e.g., "/predict")
 * - gradio.apiType: API type (AUTO, GRADIO_API, GRADIO_API_CALL, etc.)
 * - gradio.audioOutputIndex: Output index for audio (default: "0")
 * - gradio.params: JSON array of parameter definitions
 * - gradio.languages: Comma-separated language codes (e.g., "en,es,fr")
 * - gradio.supportsVoiceCloning: "true" if voice cloning is supported
 */
object GradioTTSPluginLoader {
    
    private const val TAG = "GradioTTSPluginLoader"
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    /**
     * Load all Gradio TTS configs from installed plugins.
     * Uses manifest metadata (plugin-api >= 1.1.0) for configuration.
     */
    fun loadFromPlugins(pluginManager: PluginManager): List<GradioTTSConfig> {
        return try {
            val gradioPlugins = pluginManager.pluginsFlow.value
                .filter { it.manifest.type == PluginType.GRADIO_TTS }
            
            Log.info { "$TAG: Found ${gradioPlugins.size} GRADIO_TTS plugins" }
            
            gradioPlugins.mapNotNull { pluginInfo ->
                try {
                    Log.debug { "$TAG: Processing plugin ${pluginInfo.id}, metadata keys: ${pluginInfo.manifest.metadata?.keys}" }
                    
                    // Load config from manifest metadata
                    val config = convertFromManifestMetadata(pluginInfo.manifest)
                    if (config != null) {
                        Log.info { "$TAG: Loaded ${pluginInfo.id} from manifest metadata: spaceUrl=${config.spaceUrl}" }
                        config
                    } else {
                        Log.warn { "$TAG: No gradio.spaceUrl in metadata for plugin ${pluginInfo.id}. Metadata: ${pluginInfo.manifest.metadata}" }
                        null
                    }
                } catch (e: Exception) {
                    Log.error { "$TAG: Failed to load config for plugin ${pluginInfo.id}: ${e.message}" }
                    null
                }
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to load plugins: ${e.message}" }
            emptyList()
        }
    }
    
    /**
     * Convert plugin manifest metadata to GradioTTSConfig.
     * Returns null if required metadata is missing.
     */
    private fun convertFromManifestMetadata(manifest: PluginManifest): GradioTTSConfig? {
        val metadata = manifest.metadata
        if (metadata.isNullOrEmpty()) return null
        
        val spaceUrl = metadata["gradio.spaceUrl"]
        if (spaceUrl.isNullOrEmpty()) return null
        
        val apiName = metadata["gradio.apiName"] ?: "/predict"
        val apiTypeStr = metadata["gradio.apiType"] ?: "AUTO"
        val audioOutputIndex = metadata["gradio.audioOutputIndex"]?.toIntOrNull() ?: 0
        val paramsJson = metadata["gradio.params"]
        
        // Parse API type
        val apiType = try {
            GradioApiType.valueOf(apiTypeStr)
        } catch (e: Exception) {
            GradioApiType.AUTO
        }
        
        // Parse parameters from JSON string
        val parameters = parseParametersFromJson(paramsJson)
        
        return GradioTTSConfig(
            id = "plugin_${manifest.id}",
            name = manifest.name,
            spaceUrl = spaceUrl,
            apiName = apiName,
            parameters = parameters,
            audioOutputIndex = audioOutputIndex,
            apiKey = null,
            isCustom = false,
            enabled = true,
            defaultSpeed = 1.0f,
            description = manifest.description,
            apiType = apiType
        )
    }
    
    /**
     * Parse parameters from JSON string (used for manifest metadata).
     */
    private fun parseParametersFromJson(paramsJson: String?): List<GradioParam> {
        if (paramsJson.isNullOrEmpty()) {
            return listOf(GradioParam.textParam())
        }
        
        return try {
            val jsonArray = json.parseToJsonElement(paramsJson).jsonArray
            jsonArray.mapNotNull { element ->
                val obj = element.jsonObject
                val type = obj["type"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                
                when (type) {
                    "text" -> GradioParam.textParam(name)
                    
                    "speed" -> {
                        val default = obj["default"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 1.0f
                        val min = obj["min"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0.5f
                        val max = obj["max"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 2.0f
                        GradioParam.speedParam(name, default, min, max)
                    }
                    
                    "string" -> {
                        val default = obj["default"]?.jsonPrimitive?.content ?: ""
                        GradioParam.stringParam(name, default)
                    }
                    
                    "float" -> {
                        val default = obj["default"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f
                        val min = obj["min"]?.jsonPrimitive?.content?.toFloatOrNull()
                        val max = obj["max"]?.jsonPrimitive?.content?.toFloatOrNull()
                        GradioParam.floatParam(name, default, min, max)
                    }
                    
                    "choice" -> {
                        val choices = obj["choices"]?.jsonArray?.map { 
                            it.jsonPrimitive.content 
                        } ?: emptyList()
                        val default = obj["default"]?.jsonPrimitive?.content ?: choices.firstOrNull() ?: ""
                        GradioParam.choiceParam(name, choices, default)
                    }
                    
                    else -> null
                }
            }.ifEmpty { listOf(GradioParam.textParam()) }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to parse params JSON: ${e.message}" }
            listOf(GradioParam.textParam())
        }
    }
}
