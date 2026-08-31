package ireader.domain.services.tts_service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import ireader.core.log.Log
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Smart detector for Hugging Face and Gradio TTS Spaces.
 * Automatically normalizes space URLs, queries /config and /info endpoints,
 * and extracts the API schemas, endpoints, parameter types, choices, and audio outputs.
 */
object GradioSpaceDetector {

    private const val TAG = "GradioSpaceDetector"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Normalizes a raw user input into a valid Gradio space URL.
     * Examples:
     * - "hexgrad/Kokoro-TTS" -> "https://hexgrad-kokoro-tts.hf.space"
     * - "https://huggingface.co/spaces/coqui/xtts" -> "https://coqui-xtts.hf.space"
     * - "http://localhost:7860/" -> "http://localhost:7860"
     */
    fun normalizeSpaceUrl(rawInput: String): String {
        var trimmed = rawInput.trim().trimEnd('/')
        if (trimmed.isEmpty()) return ""

        // Handle huggingface.co/spaces/owner/name
        val hfSpaceRegex = """(?:https?://)?huggingface\.co/spaces/([^/]+)/([^/?#]+)""".toRegex(RegexOption.IGNORE_CASE)
        val hfMatch = hfSpaceRegex.find(trimmed)
        if (hfMatch != null) {
            val owner = hfMatch.groupValues[1].replace("_", "-").replace(".", "-").lowercase()
            val spaceName = hfMatch.groupValues[2].replace("_", "-").replace(".", "-").lowercase()
            return "https://$owner-$spaceName.hf.space"
        }

        // Handle owner/space-name (e.g. "hexgrad/Kokoro-TTS")
        val shortIdRegex = """^([a-zA-Z0-9_\-\.]+)/([a-zA-Z0-9_\-\.]+)$""".toRegex()
        val shortMatch = shortIdRegex.find(trimmed)
        if (shortMatch != null && !trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            val owner = shortMatch.groupValues[1].replace("_", "-").replace(".", "-").lowercase()
            val spaceName = shortMatch.groupValues[2].replace("_", "-").replace(".", "-").lowercase()
            return "https://$owner-$spaceName.hf.space"
        }

        // Ensure scheme
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "https://$trimmed"
        }

        return trimmed
    }

    /**
     * Derives a friendly display name from a space URL or raw input
     */
    fun deriveFriendlyName(rawInput: String, detectedTitle: String?): String {
        if (!detectedTitle.isNullOrBlank() && detectedTitle != "Gradio" && detectedTitle != "Interface") {
            return detectedTitle.trim()
        }
        val clean = rawInput.trim()
            .replace("https://", "")
            .replace("http://", "")
            .replace(".hf.space", "")
            .replace("huggingface.co/spaces/", "")
            .trimEnd('/')

        return clean.split('/', '-', '_')
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            .ifBlank { "Custom TTS Engine" }
    }

    /**
     * Connects to the space and automatically builds a GradioTTSConfig.
     */
    suspend fun detectSpace(
        httpClient: HttpClient,
        rawUrl: String,
        apiKey: String? = null
    ): Result<GradioTTSConfig> {
        val normalizedUrl = normalizeSpaceUrl(rawUrl)
        if (normalizedUrl.isEmpty()) {
            return Result.failure(IllegalArgumentException("Space URL or ID cannot be empty."))
        }

        Log.info { "$TAG: Auto-detecting Gradio space schema for: $normalizedUrl" }

        val endpointsToTry = listOf(
            "$normalizedUrl/config",
            "$normalizedUrl/gradio_api/config",
            "$normalizedUrl/info",
            "$normalizedUrl/gradio_api/info"
        )

        for (endpoint in endpointsToTry) {
            try {
                val response = httpClient.get(endpoint) {
                    if (!apiKey.isNullOrBlank()) {
                        header("Authorization", "Bearer $apiKey")
                    }
                }

                if (response.status.isSuccess()) {
                    val body = response.bodyAsText()
                    val config = parseGradioSchema(body, normalizedUrl, apiKey, rawUrl)
                    if (config != null) {
                        Log.info { "$TAG: Successfully detected schema for $normalizedUrl: ${config.name}, apiName=${config.apiName}, ${config.parameters.size} params" }
                        return Result.success(config)
                    }
                }
            } catch (e: Exception) {
                Log.warn { "$TAG: Failed to query $endpoint: ${e.message}" }
            }
        }

        // Fallback: If network inspect failed, return a smart default config matching the URL
        val friendlyName = deriveFriendlyName(rawUrl, null)
        val fallbackConfig = GradioTTSConfig(
            id = "custom_${currentTimeToLong()}",
            name = friendlyName,
            spaceUrl = normalizedUrl,
            apiName = "/predict",
            parameters = listOf(
                GradioParam.textParam("text"),
                GradioParam.speedParam("speed", 1.0f, 0.5f, 2.0f)
            ),
            audioOutputIndex = 0,
            apiKey = apiKey,
            isCustom = true,
            description = "Custom Gradio TTS Space: $friendlyName",
            apiType = GradioApiType.AUTO
        )
        return Result.success(fallbackConfig)
    }

    /**
     * Parses Gradio config or info JSON into a GradioTTSConfig
     */
    private fun parseGradioSchema(
        jsonString: String,
        spaceUrl: String,
        apiKey: String?,
        originalInput: String
    ): GradioTTSConfig? {
        return try {
            val root = json.parseToJsonElement(jsonString).jsonObject
            val title = root["title"]?.jsonPrimitive?.contentOrNull

            // Case 1: Gradio /config structure with components and dependencies
            if (root.containsKey("components") && root.containsKey("dependencies")) {
                return parseComponentsAndDependencies(root, spaceUrl, apiKey, originalInput, title)
            }

            // Case 2: Gradio /info structure with named_endpoints
            if (root.containsKey("named_endpoints")) {
                return parseNamedEndpoints(root, spaceUrl, apiKey, originalInput, title)
            }

            null
        } catch (e: Exception) {
            Log.error { "$TAG: Error parsing schema JSON: ${e.message}" }
            null
        }
    }

    private fun parseComponentsAndDependencies(
        root: JsonObject,
        spaceUrl: String,
        apiKey: String?,
        originalInput: String,
        title: String?
    ): GradioTTSConfig? {
        val componentsArray = root["components"]?.jsonArray ?: return null
        val dependenciesArray = root["dependencies"]?.jsonArray ?: return null

        // Map component ID to component JsonObject
        val componentMap = mutableMapOf<Int, JsonObject>()
        componentsArray.forEach { elem ->
            val obj = elem.jsonObject
            val id = obj["id"]?.jsonPrimitive?.intOrNull
            if (id != null) {
                componentMap[id] = obj
            }
        }

        // Find candidate dependency (endpoint)
        var selectedDep: JsonObject? = null
        var selectedApiName = "/predict"
        var selectedAudioIndex = 0

        // Prioritize dependencies whose outputs contain an audio component
        for (depElem in dependenciesArray) {
            val dep = depElem.jsonObject
            val apiName = dep["api_name"]?.jsonPrimitive?.contentOrNull ?: ""
            val outputs = dep["outputs"]?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()

            val hasAudioOutput = outputs.any { outId ->
                val comp = componentMap[outId]
                val type = comp?.get("type")?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
                type == "audio"
            }

            if (hasAudioOutput) {
                selectedDep = dep
                selectedApiName = if (apiName.startsWith("/")) apiName else if (apiName.isNotBlank()) "/$apiName" else "/predict"
                val audioIdx = outputs.indexOfFirst { outId ->
                    val comp = componentMap[outId]
                    val type = comp?.get("type")?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
                    type == "audio"
                }
                selectedAudioIndex = if (audioIdx >= 0) audioIdx else 0
                break
            }
        }

        if (selectedDep == null && dependenciesArray.isNotEmpty()) {
            selectedDep = dependenciesArray[0].jsonObject
            val apiName = selectedDep["api_name"]?.jsonPrimitive?.contentOrNull ?: "predict"
            selectedApiName = if (apiName.startsWith("/")) apiName else "/$apiName"
        }

        val inputs = selectedDep?.get("inputs")?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()
        val parameters = mutableListOf<GradioParam>()

        inputs.forEachIndexed { index, compId ->
            val comp = componentMap[compId]
            val param = extractParamFromComponent(comp, index, parameters.none { it.isTextInput })
            parameters.add(param)
        }

        if (parameters.isEmpty()) {
            parameters.add(GradioParam.textParam("text"))
        }

        val name = deriveFriendlyName(originalInput, title)

        return GradioTTSConfig(
            id = "custom_${currentTimeToLong()}",
            name = name,
            spaceUrl = spaceUrl,
            apiName = selectedApiName,
            parameters = parameters,
            audioOutputIndex = selectedAudioIndex,
            apiKey = apiKey,
            isCustom = true,
            description = "Auto-detected Gradio TTS Engine ($name)",
            apiType = GradioApiType.AUTO
        )
    }

    private fun extractParamFromComponent(comp: JsonObject?, index: Int, needTextInput: Boolean): GradioParam {
        if (comp == null) return GradioParam.stringParam("param$index", "")

        val type = comp["type"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: "textbox"
        val props = comp["props"]?.jsonObject ?: JsonObject(emptyMap())
        val label = props["label"]?.jsonPrimitive?.contentOrNull?.ifBlank { null } ?: "param$index"
        val labelLower = label.lowercase()

        val defaultValue = props["value"]?.jsonPrimitive?.contentOrNull ?: ""

        when {
            type == "textbox" || labelLower.contains("text") || labelLower.contains("prompt") || labelLower.contains("sentence") -> {
                return GradioParam(
                    name = label,
                    type = GradioParamType.STRING,
                    defaultValue = defaultValue,
                    isTextInput = needTextInput || labelLower.contains("text") || labelLower.contains("prompt")
                )
            }
            type == "dropdown" || type == "radio" || props.containsKey("choices") -> {
                val choicesList = props["choices"]?.jsonArray?.mapNotNull {
                    it.jsonPrimitive.contentOrNull ?: it.toString()
                } ?: emptyList()
                return GradioParam(
                    name = label,
                    type = GradioParamType.CHOICE,
                    defaultValue = defaultValue.ifBlank { choicesList.firstOrNull() ?: "" },
                    choices = choicesList
                )
            }
            type == "slider" || type == "number" || labelLower.contains("speed") || labelLower.contains("rate") || labelLower.contains("pitch") -> {
                val minVal = props["minimum"]?.jsonPrimitive?.floatOrNull ?: 0.5f
                val maxVal = props["maximum"]?.jsonPrimitive?.floatOrNull ?: 2.0f
                val isSpeed = labelLower.contains("speed") || labelLower.contains("rate") || labelLower.contains("tempo")
                val floatDef = props["value"]?.jsonPrimitive?.floatOrNull ?: (if (isSpeed) 1.0f else minVal)

                return GradioParam(
                    name = label,
                    type = GradioParamType.FLOAT,
                    defaultValue = floatDef.toString(),
                    isSpeedInput = isSpeed,
                    minValue = minVal,
                    maxValue = maxVal
                )
            }
            type == "checkbox" -> {
                return GradioParam(
                    name = label,
                    type = GradioParamType.BOOLEAN,
                    defaultValue = if (props["value"]?.jsonPrimitive?.contentOrNull == "true") "true" else "false"
                )
            }
            else -> {
                return GradioParam(
                    name = label,
                    type = GradioParamType.STRING,
                    defaultValue = defaultValue
                )
            }
        }
    }

    private fun parseNamedEndpoints(
        root: JsonObject,
        spaceUrl: String,
        apiKey: String?,
        originalInput: String,
        title: String?
    ): GradioTTSConfig? {
        val namedEndpoints = root["named_endpoints"]?.jsonObject ?: return null
        val endpointKeys = namedEndpoints.keys

        val targetKey = endpointKeys.firstOrNull { it.contains("predict") || it.contains("tts") || it.contains("speech") || it.contains("generate") }
            ?: endpointKeys.firstOrNull()
            ?: "/predict"

        val endpointObj = namedEndpoints[targetKey]?.jsonObject
        val paramsArray = endpointObj?.get("parameters")?.jsonArray ?: JsonArray(emptyList())

        val parameters = mutableListOf<GradioParam>()
        var foundTextInput = false

        paramsArray.forEachIndexed { index, paramElem ->
            val pObj = paramElem.jsonObject
            val label = pObj["label"]?.jsonPrimitive?.contentOrNull ?: "param$index"
            val labelLower = label.lowercase()
            val pType = pObj["type"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: "string"
            val defVal = pObj["default"]?.jsonPrimitive?.contentOrNull ?: ""

            when {
                pType == "string" && (!foundTextInput || labelLower.contains("text") || labelLower.contains("prompt")) -> {
                    foundTextInput = true
                    parameters.add(GradioParam.textParam(label))
                }
                pObj.containsKey("enum") -> {
                    val choices = pObj["enum"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
                    parameters.add(GradioParam.choiceParam(label, choices, defVal.ifBlank { choices.firstOrNull() ?: "" }))
                }
                pType == "number" -> {
                    val isSpeed = labelLower.contains("speed") || labelLower.contains("rate")
                    parameters.add(GradioParam(
                        name = label,
                        type = GradioParamType.FLOAT,
                        defaultValue = defVal.ifBlank { if (isSpeed) "1.0" else "0.0" },
                        isSpeedInput = isSpeed,
                        minValue = if (isSpeed) 0.5f else null,
                        maxValue = if (isSpeed) 2.0f else null
                    ))
                }
                pType == "boolean" -> {
                    parameters.add(GradioParam(name = label, type = GradioParamType.BOOLEAN, defaultValue = defVal))
                }
                else -> {
                    parameters.add(GradioParam.stringParam(label, defVal))
                }
            }
        }

        if (parameters.isEmpty()) {
            parameters.add(GradioParam.textParam("text"))
        }

        val name = deriveFriendlyName(originalInput, title)

        return GradioTTSConfig(
            id = "custom_${currentTimeToLong()}",
            name = name,
            spaceUrl = spaceUrl,
            apiName = if (targetKey.startsWith("/")) targetKey else "/$targetKey",
            parameters = parameters,
            audioOutputIndex = 0,
            apiKey = apiKey,
            isCustom = true,
            description = "Auto-detected Gradio TTS ($name)",
            apiType = GradioApiType.AUTO
        )
    }
}
