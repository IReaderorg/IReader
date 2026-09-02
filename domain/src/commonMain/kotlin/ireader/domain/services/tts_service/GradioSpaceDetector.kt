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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Smart detector for Hugging Face and Gradio TTS Spaces.
 * Automatically normalizes space URLs, queries /gradio_api/info, /info, and /config endpoints,
 * and extracts the API schemas, endpoints, parameter types, choices, and audio outputs.
 * Robustly handles Gradio 3.x, 4.x, and 5.x spaces (e.g., BreezeBlue, OmniVoice, Kokoro, F5-TTS, CosyVoice).
 */
object GradioSpaceDetector {

    private const val TAG = "GradioSpaceDetector"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Normalizes a raw user input into a valid Gradio space URL.
     * Examples:
     * - "hexgrad/Kokoro-TTS" -> "https://hexgrad-kokoro-tts.hf.space"
     * - "https://huggingface.co/spaces/BreezeBlue/breeze-tts-2-demo" -> "https://breezeblue-breeze-tts-2-demo.hf.space"
     * - "https://huggingface.co/spaces/k2-fsa/OmniVoice" -> "https://k2-fsa-omnivoice.hf.space"
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
     * Prioritizes official Gradio 4/5 /gradio_api/info schema to discover all named endpoints.
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

        // Try modern official API info first, then legacy /config
        val endpointsToTry = listOf(
            "$normalizedUrl/gradio_api/info",
            "$normalizedUrl/info",
            "$normalizedUrl/gradio_api/config",
            "$normalizedUrl/config"
        )

        for (endpoint in endpointsToTry) {
            try {
                val response = httpClient.get(endpoint) {
                    if (!apiKey.isNullOrBlank()) {
                        header("Authorization", "Bearer $apiKey")
                    }
                    header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) IReader")
                }

                if (response.status.isSuccess()) {
                    val body = response.bodyAsText().trim()
                    // Must be valid JSON object (avoid HTML 200 SPA fallbacks)
                    if (body.startsWith("{")) {
                        val config = parseGradioSchema(body, normalizedUrl, apiKey, rawUrl)
                        if (config != null) {
                            Log.info { "$TAG: Successfully detected schema for $normalizedUrl: ${config.name}, apiName=${config.apiName}, ${config.parameters.size} params, ${config.availableEndpoints.size} endpoints" }
                            return Result.success(config)
                        }
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
            apiType = GradioApiType.AUTO,
            availableEndpoints = listOf("/predict")
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

            // Priority 1: Gradio /gradio_api/info or /info structure with named_endpoints
            if (root.containsKey("named_endpoints")) {
                val parsed = parseNamedEndpoints(root, spaceUrl, apiKey, originalInput, title)
                if (parsed != null) return parsed
            }

            // Priority 2: Gradio /config structure with components and dependencies
            if (root.containsKey("components") && root.containsKey("dependencies")) {
                val parsed = parseComponentsAndDependencies(root, spaceUrl, apiKey, originalInput, title)
                if (parsed != null) return parsed
            }

            null
        } catch (e: Exception) {
            Log.error { "$TAG: Error parsing schema JSON: ${e.message}" }
            null
        }
    }

    /**
     * Parses modern Gradio named_endpoints from /gradio_api/info.
     * Evaluates all endpoints, scoring them to find the optimal Text-to-Speech generation endpoint.
     */
    private fun parseNamedEndpoints(
        root: JsonObject,
        spaceUrl: String,
        apiKey: String?,
        originalInput: String,
        title: String?
    ): GradioTTSConfig? {
        val namedEndpoints = root["named_endpoints"]?.jsonObject ?: return null
        val endpointKeys = namedEndpoints.keys.toList()
        if (endpointKeys.isEmpty()) return null

        // Score each endpoint to pick the best TTS synthesis endpoint
        data class ScoredEndpoint(
            val key: String,
            val score: Int,
            val endpointObj: JsonObject,
            val hasAudio: Boolean,
            val hasText: Boolean
        )

        val scored = endpointKeys.map { key ->
            val epObj = namedEndpoints[key]?.jsonObject ?: JsonObject(emptyMap())
            val keyLower = key.lowercase()
            val params = epObj["parameters"]?.jsonArray ?: JsonArray(emptyList())
            val returns = epObj["returns"]?.jsonArray ?: JsonArray(emptyList())

            var score = 0
            var hasAudioReturn = false
            var hasTextParam = false
            var requiresAudioInput = false

            // Check returns for Audio component or audio file
            for (retElem in returns) {
                val rObj = retElem.jsonObject
                val comp = rObj["component"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
                val pyType = rObj["python_type"]?.jsonObject?.get("type")?.jsonPrimitive?.contentOrNull?.lowercase()
                    ?: rObj["python_type"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
                val label = rObj["label"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""

                if (comp == "audio" || pyType == "filepath" || label.contains("audio") || label.contains("speech") || label.contains("wav")) {
                    hasAudioReturn = true
                    score += 100
                    break
                }
            }

            // Check parameters for text input and mandatory audio
            for (pElem in params) {
                val pObj = pElem.jsonObject
                val comp = pObj["component"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
                val pName = pObj["parameter_name"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
                val label = pObj["label"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
                val hasDefault = pObj["parameter_has_default"]?.jsonPrimitive?.contentOrNull == "true"

                if (comp == "textbox" || pName in listOf("text", "prompt", "input_text", "sentence") ||
                    label.contains("text") || label.contains("prompt") || label.contains("sentence")
                ) {
                    hasTextParam = true
                }

                if (comp == "audio" && !hasDefault) {
                    requiresAudioInput = true
                }
            }

            if (hasTextParam) score += 50
            if (requiresAudioInput) score -= 30 // Cloning requires user sample, voice design generates directly

            // Endpoint name bonuses / penalties
            when {
                keyLower.contains("design") -> score += 40
                keyLower.contains("voice") -> score += 30
                keyLower.contains("tts") -> score += 35
                keyLower.contains("speech") -> score += 30
                keyLower.contains("synthesize") -> score += 30
                keyLower.contains("generate") -> score += 25
                keyLower.contains("predict") -> score += 20
                keyLower.contains("infer") -> score += 20
            }

            if (keyLower.contains("transcribe") || keyLower.contains("example") || keyLower.contains("load") || keyLower.contains("clear")) {
                score -= 100
            }

            ScoredEndpoint(key, score, epObj, hasAudioReturn, hasTextParam)
        }.sortedByDescending { it.score }

        val best = scored.firstOrNull { it.hasAudio && it.hasText } ?: scored.firstOrNull { it.hasAudio } ?: scored.first()
        val targetKey = best.key
        val endpointObj = best.endpointObj
        val paramsArray = endpointObj["parameters"]?.jsonArray ?: JsonArray(emptyList())

        // Extract parameters with intelligent defaults
        val parameters = mutableListOf<GradioParam>()
        var foundTextInput = false

        paramsArray.forEachIndexed { index, paramElem ->
            val pObj = paramElem.jsonObject
            val label = pObj["label"]?.jsonPrimitive?.contentOrNull?.ifBlank { null }
                ?: pObj["parameter_name"]?.jsonPrimitive?.contentOrNull?.ifBlank { null }
                ?: "param$index"
            val labelLower = label.lowercase()
            val pName = pObj["parameter_name"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
            val comp = pObj["component"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
            val pType = pObj["type"]?.jsonObject?.get("type")?.jsonPrimitive?.contentOrNull?.lowercase()
                ?: pObj["type"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: "string"
            val defVal = pObj["parameter_default"]?.jsonPrimitive?.contentOrNull
                ?: pObj["default"]?.jsonPrimitive?.contentOrNull ?: ""
            val exampleInput = pObj["example_input"]?.jsonPrimitive?.contentOrNull ?: ""

            when {
                // Text input for synthesis
                comp == "textbox" && (!foundTextInput || pName in listOf("text", "prompt", "input_text", "sentence") ||
                    labelLower.contains("text to") || labelLower.contains("prompt to") ||
                    (labelLower.contains("text") && !labelLower.contains("description") && !labelLower.contains("instruction"))) -> {
                    foundTextInput = true
                    parameters.add(GradioParam.textParam(label))
                }

                // Voice description / instruction textbox (e.g. BreezeBlue)
                comp == "textbox" && (labelLower.contains("description") || labelLower.contains("instruction") || labelLower.contains("style")) -> {
                    val promptDef = defVal.ifBlank { exampleInput.ifBlank { "A clear and pleasant natural speaking voice" } }
                    parameters.add(GradioParam.stringParam(label, promptDef))
                }

                // Choices / Dropdown
                comp in listOf("dropdown", "radio") || pObj.containsKey("choices") || pObj.containsKey("enum") -> {
                    val choices = pObj["choices"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                        ?: pObj["enum"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                        ?: emptyList()
                    val effectiveDef = defVal.ifBlank { choices.firstOrNull() ?: "Auto" }
                    parameters.add(GradioParam.choiceParam(label, choices, effectiveDef))
                }

                // Numbers / Sliders
                comp in listOf("slider", "number") || pType == "number" || pType == "integer" -> {
                    val isSpeed = labelLower.contains("speed") || labelLower.contains("rate") || labelLower.contains("tempo")
                    val effectiveVal = defVal.ifBlank {
                        if (exampleInput.isNotBlank()) exampleInput else if (isSpeed) "1.0" else "0"
                    }
                    val minVal = pObj["minimum"]?.jsonPrimitive?.floatOrNull ?: if (isSpeed) 0.5f else null
                    val maxVal = pObj["maximum"]?.jsonPrimitive?.floatOrNull ?: if (isSpeed) 2.0f else null

                    parameters.add(
                        GradioParam(
                            name = label,
                            type = if (pType == "integer") GradioParamType.INT else GradioParamType.FLOAT,
                            defaultValue = effectiveVal,
                            isSpeedInput = isSpeed,
                            minValue = minVal,
                            maxValue = maxVal
                        )
                    )
                }

                // Boolean / Checkbox
                comp == "checkbox" || pType == "boolean" -> {
                    val effectiveVal = if (defVal.equals("true", ignoreCase = true)) "true" else "false"
                    parameters.add(GradioParam(name = label, type = GradioParamType.BOOLEAN, defaultValue = effectiveVal))
                }

                // Fallback string / other
                else -> {
                    parameters.add(GradioParam.stringParam(label, defVal.ifBlank { exampleInput }))
                }
            }
        }

        if (!foundTextInput) {
            parameters.add(0, GradioParam.textParam("text"))
        }

        // Determine Audio Output Index
        val returns = endpointObj["returns"]?.jsonArray ?: JsonArray(emptyList())
        var audioIndex = 0
        returns.forEachIndexed { idx, retElem ->
            val rObj = retElem.jsonObject
            val comp = rObj["component"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
            val pyType = rObj["python_type"]?.jsonObject?.get("type")?.jsonPrimitive?.contentOrNull?.lowercase()
                ?: rObj["python_type"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
            val label = rObj["label"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""

            if (comp == "audio" || pyType == "filepath" || label.contains("audio") || label.contains("speech") || label.contains("wav")) {
                audioIndex = idx
                return@forEachIndexed
            }
        }

        val name = deriveFriendlyName(originalInput, title)
        val endpointDescription = endpointObj["description"]?.jsonPrimitive?.contentOrNull ?: "Gradio TTS ($name)"

        return GradioTTSConfig(
            id = "custom_${currentTimeToLong()}",
            name = name,
            spaceUrl = spaceUrl,
            apiName = if (targetKey.startsWith("/")) targetKey else "/$targetKey",
            parameters = parameters,
            audioOutputIndex = audioIndex,
            apiKey = apiKey,
            isCustom = true,
            description = endpointDescription,
            apiType = GradioApiType.AUTO,
            availableEndpoints = endpointKeys
        )
    }

    /**
     * Fallback parser for older Gradio /config structure
     */
    private fun parseComponentsAndDependencies(
        root: JsonObject,
        spaceUrl: String,
        apiKey: String?,
        originalInput: String,
        title: String?
    ): GradioTTSConfig? {
        val componentsArray = root["components"]?.jsonArray ?: return null
        val dependenciesArray = root["dependencies"]?.jsonArray ?: return null

        val componentMap = mutableMapOf<Int, JsonObject>()
        componentsArray.forEach { elem ->
            val obj = elem.jsonObject
            val id = obj["id"]?.jsonPrimitive?.intOrNull
            if (id != null) {
                componentMap[id] = obj
            }
        }

        // Filter and score dependencies
        data class ScoredDep(
            val dep: JsonObject,
            val apiName: String,
            val score: Int,
            val audioOutputIndex: Int
        )

        val scoredDeps = dependenciesArray.mapNotNull { depElem ->
            val dep = depElem.jsonObject
            val apiName = dep["api_name"]?.jsonPrimitive?.contentOrNull ?: ""
            val showApi = dep["show_api"]?.jsonPrimitive?.contentOrNull != "false"
            val apiNameLower = apiName.lowercase()

            // Skip internal UI callbacks (like load_example)
            if (apiNameLower.contains("example") || apiNameLower.contains("load") || apiNameLower.contains("clear") || apiNameLower.contains("transcribe")) {
                return@mapNotNull null
            }

            val inputs = dep["inputs"]?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()
            val outputs = dep["outputs"]?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()

            // Must have audio output
            val audioIdx = outputs.indexOfFirst { outId ->
                val comp = componentMap[outId]
                val type = comp?.get("type")?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
                type == "audio"
            }
            if (audioIdx < 0) return@mapNotNull null

            var score = 100
            if (showApi) score += 50

            // Check if inputs have at least one text component
            val hasTextInput = inputs.any { inId ->
                val comp = componentMap[inId]
                val type = comp?.get("type")?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
                type == "textbox"
            }
            if (hasTextInput) score += 50

            when {
                apiNameLower.contains("design") -> score += 40
                apiNameLower.contains("voice") -> score += 30
                apiNameLower.contains("tts") -> score += 35
                apiNameLower.contains("speech") -> score += 30
                apiNameLower.contains("predict") -> score += 20
                apiNameLower.contains("generate") -> score += 20
            }

            ScoredDep(dep, if (apiName.startsWith("/")) apiName else if (apiName.isNotBlank()) "/$apiName" else "/predict", score, audioIdx)
        }.sortedByDescending { it.score }

        val bestDep = scoredDeps.firstOrNull() ?: return null
        val inputs = bestDep.dep["inputs"]?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()
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
        val allEndpoints = scoredDeps.map { it.apiName }.distinct()

        return GradioTTSConfig(
            id = "custom_${currentTimeToLong()}",
            name = name,
            spaceUrl = spaceUrl,
            apiName = bestDep.apiName,
            parameters = parameters,
            audioOutputIndex = bestDep.audioOutputIndex,
            apiKey = apiKey,
            isCustom = true,
            description = "Auto-detected Gradio TTS Engine ($name)",
            apiType = GradioApiType.AUTO,
            availableEndpoints = allEndpoints
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
            type == "textbox" && (needTextInput || labelLower.contains("text to") || (labelLower.contains("text") && !labelLower.contains("description"))) -> {
                return GradioParam(
                    name = label,
                    type = GradioParamType.STRING,
                    defaultValue = defaultValue,
                    isTextInput = true
                )
            }
            type == "textbox" && (labelLower.contains("description") || labelLower.contains("instruction")) -> {
                return GradioParam.stringParam(label, defaultValue.ifBlank { "A clear and natural speaking voice" })
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
            type == "slider" || type == "number" || labelLower.contains("speed") || labelLower.contains("rate") || labelLower.contains("tempo") -> {
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
}
