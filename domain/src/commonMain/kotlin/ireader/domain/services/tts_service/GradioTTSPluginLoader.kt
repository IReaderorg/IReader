package ireader.domain.services.tts_service

import ireader.core.log.Log
import ireader.domain.plugins.PluginManager
import ireader.plugin.api.PluginManifest
import ireader.plugin.api.PluginType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Loads Gradio TTS configurations from installed GRADIO_TTS plugins.
 * 
 * Supports two configuration methods:
 * 
 * 1. **Manifest metadata** (recommended for plugin-api >= 1.1.0):
 *    Use manifest.metadata with keys like "gradio.spaceUrl", "gradio.apiName", etc.
 * 
 * 2. **Config file** (fallback for older plugin-api):
 *    Include a `gradio-config.json` file in plugin resources.
 * 
 * Configuration keys (for both methods):
 * - spaceUrl / gradio.spaceUrl: Hugging Face Space URL (required)
 * - apiName / gradio.apiName: API endpoint name (e.g., "/predict")
 * - apiType / gradio.apiType: API type (AUTO, GRADIO_API_CALL, etc.)
 * - audioOutputIndex / gradio.audioOutputIndex: Output index for audio (default: 0)
 * - params / gradio.params: JSON array of parameter definitions
 * - languages / gradio.languages: Supported language codes
 * - supportsVoiceCloning / gradio.supportsVoiceCloning: Voice cloning support
 */
object GradioTTSPluginLoader {
    
    private const val TAG = "GradioTTSPluginLoader"
    private const val CONFIG_FILE = "gradio-config.json"
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    @Serializable
    private data class GradioPluginConfig(
        val spaceUrl: String,
        val apiName: String = "/predict",
        val apiType: String = "AUTO",
        val audioOutputIndex: Int = 0,
        val languages: List<String> = emptyList(),
        val supportsVoiceCloning: Boolean = false,
        val params: List<ParamConfig> = emptyList()
    )
    
    @Serializable
    private data class ParamConfig(
        val type: String,
        val name: String,
        val default: kotlinx.serialization.json.JsonElement? = null,
        val min: Float? = null,
        val max: Float? = null,
        val choices: List<String>? = null
    )
    
    /**
     * Load all Gradio TTS configs from installed plugins.
     * Tries manifest metadata first, then falls back to gradio-config.json file.
     */
    fun loadFromPlugins(pluginManager: PluginManager): List<GradioTTSConfig> {
        return try {
            val gradioPlugins = pluginManager.pluginsFlow.value
                .filter { it.manifest.type == PluginType.GRADIO_TTS }
            
            Log.info { "$TAG: Found ${gradioPlugins.size} GRADIO_TTS plugins" }
            
            gradioPlugins.mapNotNull { pluginInfo ->
                try {
                    // Try manifest metadata first (plugin-api >= 1.1.0)
                    val fromMetadata = convertFromManifestMetadata(pluginInfo.manifest)
                    if (fromMetadata != null) {
                        Log.debug { "$TAG: Loaded ${pluginInfo.id} from manifest metadata" }
                        return@mapNotNull fromMetadata
                    }
                    
                    // Fallback to gradio-config.json file
                    val configJson = loadPluginConfigJson(pluginManager, pluginInfo.id)
                    if (configJson != null) {
                        Log.debug { "$TAG: Loaded ${pluginInfo.id} from gradio-config.json" }
                        convertConfigToGradioTTSConfig(
                            pluginInfo.id, 
                            pluginInfo.manifest.name, 
                            pluginInfo.manifest.description, 
                            configJson
                        )
                    } else {
                        Log.warn { "$TAG: No config found for plugin ${pluginInfo.id}" }
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
        if (metadata.isEmpty()) return null
        
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
    
    /**
     * Load gradio-config.json from a plugin's resources.
     */
    private fun loadPluginConfigJson(pluginManager: PluginManager, pluginId: String): String? {
        return try {
            val plugin = pluginManager.getPlugin(pluginId)
            if (plugin != null) {
                val resourceStream = plugin::class.java.getResourceAsStream("/$CONFIG_FILE")
                    ?: plugin::class.java.classLoader?.getResourceAsStream(CONFIG_FILE)
                resourceStream?.bufferedReader()?.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.debug { "$TAG: Could not load $CONFIG_FILE for $pluginId: ${e.message}" }
            null
        }
    }
    
    /**
     * Convert plugin config JSON file to GradioTTSConfig.
     */
    private fun convertConfigToGradioTTSConfig(
        pluginId: String,
        name: String,
        description: String,
        configJson: String
    ): GradioTTSConfig? {
        return try {
            val config = json.decodeFromString<GradioPluginConfig>(configJson)
            
            if (config.spaceUrl.isEmpty()) {
                Log.warn { "$TAG: Plugin $pluginId has empty spaceUrl" }
                return null
            }
            
            // Parse API type
            val apiType = try {
                GradioApiType.valueOf(config.apiType)
            } catch (e: Exception) {
                GradioApiType.AUTO
            }
            
            // Parse parameters
            val parameters = config.params.mapNotNull { param ->
                parseParam(param)
            }.ifEmpty {
                listOf(GradioParam.textParam())
            }
            
            GradioTTSConfig(
                id = "plugin_$pluginId",
                name = name,
                spaceUrl = config.spaceUrl,
                apiName = config.apiName,
                parameters = parameters,
                audioOutputIndex = config.audioOutputIndex,
                apiKey = null,
                isCustom = false,
                enabled = true,
                defaultSpeed = 1.0f,
                description = description,
                apiType = apiType
            )
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to parse config for $pluginId: ${e.message}" }
            null
        }
    }
    
    /**
     * Parse a parameter config to GradioParam.
     */
    private fun parseParam(param: ParamConfig): GradioParam? {
        return when (param.type) {
            "text" -> GradioParam.textParam(param.name)
            
            "speed" -> {
                val default = param.default?.jsonPrimitive?.content?.toFloatOrNull() ?: 1.0f
                val min = param.min ?: 0.5f
                val max = param.max ?: 2.0f
                GradioParam.speedParam(param.name, default, min, max)
            }
            
            "string" -> {
                val default = param.default?.jsonPrimitive?.content ?: ""
                GradioParam.stringParam(param.name, default)
            }
            
            "float" -> {
                val default = param.default?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f
                GradioParam.floatParam(param.name, default, param.min, param.max)
            }
            
            "choice" -> {
                val choices = param.choices ?: emptyList()
                val default = param.default?.jsonPrimitive?.content ?: choices.firstOrNull() ?: ""
                GradioParam.choiceParam(param.name, choices, default)
            }
            
            else -> null
        }
    }
}
