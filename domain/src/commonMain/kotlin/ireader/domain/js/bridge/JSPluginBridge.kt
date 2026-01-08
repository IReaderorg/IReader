package ireader.domain.js.bridge

import io.ktor.client.HttpClient
import ireader.core.log.Log
import ireader.domain.js.engine.JSEngine
import ireader.domain.js.engine.JSException
import ireader.domain.js.engine.JSValue
import ireader.domain.js.models.FilterDefinition
import ireader.domain.js.models.FilterOption
import ireader.domain.js.models.JSChapterItem
import ireader.domain.js.models.JSNovelItem
import ireader.domain.js.models.JSPluginError
import ireader.domain.js.models.JSSourceNovel
import ireader.domain.js.models.PluginMetadata
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Bridge between JavaScript plugin API and Kotlin domain models.
 * Translates plugin method calls and converts JavaScript objects to Kotlin data classes.
 */
class JSPluginBridge(
    private val engine: JSEngine,
    private val pluginInstanceRaw: Any?,
    private val httpClient: HttpClient,
    private val pluginId: String
) {

    // Detect if running on Android/QuickJS (pluginInstanceRaw is a Map with __quickjs_plugin marker)
    private val isAndroidQuickJS = pluginInstanceRaw is Map<*, *> && pluginInstanceRaw["__quickjs_plugin"] == true

    private val pluginInstance: JSValue = if (isAndroidQuickJS) {
        // On Android, create a dummy JSValue since we'll use evaluateScript instead
        JSValue.from(mapOf<String, Any>())
    } else {
        JSValue.from(pluginInstanceRaw)
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Cache for filter definitions to avoid repeated parsing
    private var cachedFilters: Map<String, FilterDefinition>? = null

    // Cache for plugin metadata
    private var cachedMetadata: PluginMetadata? = null

    // Dispatcher for JS engine operations
    // On JVM (Desktop), this should be a single-threaded context for GraalVM
    // On other platforms, use Default dispatcher
    private val jsDispatcher: CoroutineDispatcher = Dispatchers.Default

    /**
     * Extracts plugin metadata from the plugin instance.
     * Results are cached after first call.
     */
    suspend fun getPluginMetadata(): PluginMetadata {
        // Return cached metadata if available
        cachedMetadata?.let { return it }

        return withTimeout(30000L) {
            try {
                if (isAndroidQuickJS) {
                    // Android/QuickJS - extract via JavaScript evaluation
                    val metadataJson = engine.evaluateScript("""
                        (function() {
                            var plugin = exports.default || exports;
                            return JSON.stringify({
                                id: plugin.id || '',
                                name: plugin.name || 'Unknown',
                                site: plugin.site || '',
                                version: plugin.version || '1.0.0',
                                icon: plugin.icon || '',
                                lang: plugin.lang || 'en'
                            });
                        })();
                    """.trimIndent())
                    
                    val jsonElement = json.parseToJsonElement(metadataJson.toString())
                    val jsonMap = (jsonElement as kotlinx.serialization.json.JsonObject)
                    
                    val metadata = PluginMetadata(
                        id = jsonMap["id"]?.toString()?.trim('"') ?: pluginId,
                        name = jsonMap["name"]?.toString()?.trim('"') ?: "Unknown",
                        site = jsonMap["site"]?.toString()?.trim('"') ?: "",
                        version = jsonMap["version"]?.toString()?.trim('"') ?: "1.0.0",
                        icon = jsonMap["icon"]?.toString()?.trim('"') ?: "",
                        lang = jsonMap["lang"]?.toString()?.trim('"') ?: "en"
                    )
                    
                    cachedMetadata = metadata
                    return@withTimeout metadata
                }
                
                // Desktop/GraalVM - use JSValue
                val pluginMap = pluginInstance.asMap()

                val metadata = PluginMetadata(
                    id = pluginMap["id"]?.toString() ?: pluginId,
                    name = pluginMap["name"]?.toString() ?: "Unknown",
                    icon = pluginMap["icon"]?.toString() ?: "",
                    site = pluginMap["site"]?.toString() ?: "",
                    version = pluginMap["version"]?.toString() ?: "1.0.0",
                    lang = pluginMap["lang"]?.toString() ?: "en",
                    imageRequestInit = pluginMap["imageRequestInit"] as? Map<String, Any>,
                    filters = pluginMap["filters"] as? Map<String, Any>
                )

                // Cache the metadata
                cachedMetadata = metadata
                metadata
            } catch (e: Exception) {
                throw JSPluginError.ExecutionError(pluginId, "getPluginMetadata", e)
            }
        }
    }

    /**
     * Calls a method on the plugin instance by evaluating a script.
     * This is necessary because methods are on the prototype, not as own properties.
     */
    private suspend fun callPluginMethod(methodName: String, vararg args: Any?): Any? {
        // Convert arguments to JSON
        val argsJson = args.joinToString(", ") { arg ->
            when (arg) {
                null -> "null"
                is String -> "\"${arg.replace("\"", "\\\"").replace("\n", "\\n")}\""
                is Number -> arg.toString()
                is Boolean -> arg.toString()
                is Map<*, *> -> convertMapToJson(arg)
                else -> "\"${arg.toString().replace("\"", "\\\"").replace("\n", "\\n")}\""
            }
        }

        // Use a synchronous wrapper that waits for Promise resolution
        val resultVar = "__pluginResult_${currentTimeToLong()}"
        val errorVar = "__pluginError_${currentTimeToLong()}"
        val doneVar = "__pluginDone_${currentTimeToLong()}"

        val script = """
            (function() {
                var plugin = exports.default;
                if (typeof plugin.$methodName !== 'function') {
                    throw new Error('Method $methodName is not a function on plugin');
                }
                
                globalThis.$doneVar = false;
                globalThis.$resultVar = null;
                globalThis.$errorVar = null;
                
                var result;
                try {
                    result = plugin.$methodName($argsJson);
                } catch (syncError) {
                    var errorMsg = syncError && syncError.message ? syncError.message : String(syncError);
                    var errorStack = syncError && syncError.stack ? syncError.stack : '';
                    globalThis.$errorVar = errorMsg + (errorStack ? '\n' + errorStack : '');
                    globalThis.$doneVar = true;
                    return 'SYNC_ERROR';
                }
                
                if (result && typeof result.then === 'function') {
                    result.then(function(value) {
                        globalThis.$resultVar = value;
                        globalThis.$doneVar = true;
                    }).catch(function(error) {
                        var errorMsg = error && error.message ? error.message : String(error);
                        var errorStack = error && error.stack ? error.stack : '';
                        globalThis.$errorVar = errorMsg + (errorStack ? '\n' + errorStack : '');
                        globalThis.$doneVar = true;
                    });
                    return 'PROMISE_PENDING';
                } else {
                    globalThis.$resultVar = result;
                    globalThis.$doneVar = true;
                    return result;
                }
            })()
        """.trimIndent()

        return try {
            val initialResult = engine.evaluateScript(script)

            if (initialResult == "SYNC_ERROR") {
                val error = engine.getGlobalObject(errorVar)
                Log.error { "[JSPlugin] Synchronous error in $methodName: $error" }
                throw JSException("Synchronous error in $methodName: $error")
            } else if (initialResult == "PROMISE_PENDING") {
                var attempts = 0
                val maxAttempts = 300

                while (attempts < maxAttempts) {
                    kotlinx.coroutines.delay(100)

                    val done = engine.getGlobalObject(doneVar) as? Boolean ?: false
                    if (done) {
                        val error = engine.getGlobalObject(errorVar)
                        if (error != null) {
                            Log.error { "[JSPlugin] Promise rejected for $methodName: $error" }
                            throw JSException("Promise rejected in $methodName: $error")
                        }

                        return engine.getGlobalObject(resultVar)
                    }

                    attempts++
                }

                throw JSException("Promise timeout after ${maxAttempts * 100}ms")
            } else {
                initialResult
            }
        } catch (e: Exception) {
            Log.error { "[JSPlugin] Failed to call method $methodName" }
            throw e
        }
    }

    private fun convertMapToJson(map: Map<*, *>): String {
        val entries = map.entries.joinToString(", ") { (key, value) ->
            val keyStr = key.toString()
            val valueStr = when (value) {
                null -> "null"
                is String -> "\"${value.replace("\"", "\\\"")}\""
                is Number -> value.toString()
                is Boolean -> value.toString()
                is Map<*, *> -> convertMapToJson(value)
                else -> "\"${value.toString().replace("\"", "\\\"")}\""
            }
            "\"$keyStr\": $valueStr"
        }
        return "{$entries}"
    }

    /**
     * Converts filter map to JSON string for JavaScript consumption.
     * Handles nested maps, arrays, and properly escapes strings.
     */
    private fun convertFiltersToJson(filters: Map<String, Any>): String {
        return buildString {
            append("{")
            filters.entries.forEachIndexed { index, (key, value) ->
                if (index > 0) append(",")
                append("\"$key\":")
                appendJsonValue(value)
            }
            append("}")
        }
    }

    /**
     * Recursively appends a value as JSON to the StringBuilder.
     */
    private fun StringBuilder.appendJsonValue(value: Any?) {
        when (value) {
            null -> append("null")
            is String -> append(
                "\"${
                    value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
                        .replace("\r", "\\r").replace("\t", "\\t")
                }\""
            )

            is Number -> append(value.toString())
            is Boolean -> append(value.toString())
            is Map<*, *> -> {
                append("{")
                value.entries.forEachIndexed { index, (k, v) ->
                    if (index > 0) append(",")
                    append("\"${k.toString()}\":")
                    appendJsonValue(v)
                }
                append("}")
            }

            is List<*> -> {
                append("[")
                value.forEachIndexed { index, item ->
                    if (index > 0) append(",")
                    appendJsonValue(item)
                }
                append("]")
            }

            is Array<*> -> {
                append("[")
                value.forEachIndexed { index, item ->
                    if (index > 0) append(",")
                    appendJsonValue(item)
                }
                append("]")
            }

            else -> append("\"${value.toString().replace("\"", "\\\"")}\"")
        }
    }

    /**
     * Validates and normalizes filter values to ensure they match the expected structure.
     * JS plugins expect filters to have a specific structure: { filterName: { value: ... } }
     */
    private fun validateAndNormalizeFilters(filters: Map<String, Any>): Map<String, Any> {
        return filters.mapValues { (key, value) ->
            when (value) {
                // If already a map with "value" key, keep it as is
                is Map<*, *> -> {
                    if (value.containsKey("value")) {
                        value
                    } else {
                        // Wrap in value property
                        mapOf("value" to value)
                    }
                }
                // For primitives and lists, wrap in a map with "value" key
                else -> mapOf("value" to value)
            }
        }
    }

    /**
     * Executes an async JavaScript function and polls for the result.
     * This is a common pattern used across all plugin methods.
     *
     * @param methodName The name of the method being called (for logging/errors)
     * @param jsCode The JavaScript code to execute
     * @param resultVarName The global variable name where the result will be stored
     * @param maxAttempts Maximum number of polling attempts
     * @return The result from JavaScript execution
     */
    private suspend fun executeAsyncJS(
        methodName: String,
        jsCode: String,
        resultVarName: String,
        maxAttempts: Int = 150
    ): Any? {
        // Execute the JavaScript code
        engine.evaluateScript(jsCode)

        // Poll for result with exponential backoff
        var attempts = 0
        var result: Any? = null
        var waitTime = 10L

        while (attempts < maxAttempts) {
            kotlinx.coroutines.delay(waitTime)

            // Check result periodically to reduce engine calls
            if (attempts % 5 == 0 || attempts < 10) {
                val checkScript = "globalThis.$resultVarName"
                val jsResult = engine.evaluateScript(checkScript)

                if (jsResult is Map<*, *>) {
                    val success = jsResult["success"] as? Boolean ?: false
                    if (success) {
                        result = jsResult["data"]
                        break
                    } else {
                        val error = jsResult["error"]
                        Log.error { "[JSPlugin] $methodName failed: $error" }
                        throw JSException("$methodName failed: $error")
                    }
                }
            }

            attempts++
            // Exponential backoff up to 200ms
            if (waitTime < 200) {
                waitTime = (waitTime * 1.2).toLong().coerceAtMost(200)
            }
        }

        if (result == null && attempts >= maxAttempts) {
            Log.error { "[JSPlugin] $methodName timeout after ${maxAttempts * waitTime}ms" }
            throw JSException("$methodName timeout")
        }

        // Clean up global variable
        try {
            engine.evaluateScript("delete globalThis.$resultVarName;")
        } catch (e: Exception) {
            // Ignore cleanup errors
        }

        return result
    }

    /**
     * Escapes a string for safe use in JavaScript code.
     */
    private fun escapeForJS(str: String): String {
        return str
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }


    /**
     * Calls the plugin's popularNovels method.
     * @param page The page number to fetch (1-indexed)
     * @param filters Map of filter values where each entry should have a "value" property
     */
    suspend fun popularNovels(page: Int, filters: Map<String, Any>): List<JSNovelItem> {
        return kotlinx.coroutines.withContext(jsDispatcher) {
            withTimeout(30000L) {
                try {
                    val startTime = currentTimeToLong()

                    // Validate and normalize filters - ensure all filter values are properly structured
                    val normalizedFilters = validateAndNormalizeFilters(filters)

                    // Convert filters to JSON - plugins expect filters.filterName.value structure
                    val filtersJson = convertFiltersToJson(normalizedFilters)

                    // Store filters in JavaScript and call the method directly in JS
                    // Sanitize plugin ID for JavaScript variable name
                    val sanitizedId = pluginId.replace(Regex("[^a-zA-Z0-9_]"), "_")

                    // Store the result in a global variable after the promise resolves
                    val callScript = """
                    (async function() {
                        try {
                            const filters = JSON.parse('${filtersJson.replace("'", "\\'")}');
                            const options = { filters: filters, showLatestNovels: false };
                            const plugin = globalThis.__plugin_${sanitizedId};
                            if (!plugin) {
                                throw new Error('Plugin not found in global context. Plugin may not be loaded correctly.');
                            }
                            if (typeof plugin.popularNovels !== 'function') {
                                throw new Error('popularNovels method not found on plugin');
                            }
                            const result = await plugin.popularNovels($page, options);
                            globalThis.__promiseResult_${sanitizedId} = { success: true, data: result };
                        } catch (error) {
                            const errorDetails = {
                                message: error.message || error.toString(),
                                stack: error.stack || 'No stack trace available',
                                name: error.name || 'Error',
                                type: error.constructor ? error.constructor.name : 'Unknown'
                            };
                            globalThis.__promiseResult_${sanitizedId} = { 
                                success: false, 
                                error: JSON.stringify(errorDetails)
                            };
                        }
                    })();
                """.trimIndent()

                    // Start the async operation
                    engine.evaluateScript(callScript)

                    // Poll for the result with exponential backoff
                    var attempts = 0
                    val maxAttempts = 150 // 30 seconds total
                    var result: Any? = null
                    var waitTime = 10L // Start with 10ms

                    while (attempts < maxAttempts) {
                        kotlinx.coroutines.delay(waitTime)

                        // Only check every few attempts to reduce statement count
                        if (attempts % 5 == 0 || attempts < 10) {
                            val checkScript = "globalThis.__promiseResult_${sanitizedId}"
                            val promiseResult = engine.evaluateScript(checkScript)

                            if (promiseResult is Map<*, *>) {
                                val success = promiseResult["success"] as? Boolean ?: false
                                if (success) {
                                    result = promiseResult["data"]
                                    break
                                } else {
                                    val error = promiseResult["error"]
                                    Log.error { "[JSPlugin] Promise rejected: $error" }
                                    throw JSException("Promise rejected: $error")
                                }
                            }
                        }

                        attempts++
                        // Exponential backoff up to 200ms
                        if (waitTime < 200) {
                            waitTime = (waitTime * 1.2).toLong().coerceAtMost(200)
                        }
                    }

                    if (result == null && attempts >= maxAttempts) {
                        Log.error { "[JSPlugin] Promise timeout after checking ${maxAttempts} times" }
                        throw JSException("Promise timeout")
                    }

                    // Clean up
                    try {
                        engine.evaluateScript("delete globalThis.__promiseResult_${sanitizedId};")
                    } catch (e: Exception) {
                        // Ignore cleanup errors
                    }

                    if (result == null) {
                        Log.warn { "[JSPlugin] popularNovels returned null for plugin: $pluginId" }
                        return@withTimeout emptyList()
                    }

                    val novels = parseNovelList(result)

                    val duration = currentTimeToLong() - startTime
                    Log.info { "[JSPlugin] popularNovels returned ${novels.size} items in ${duration}ms" }

                    novels
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    throw JSPluginError.TimeoutError(pluginId, "popularNovels")
                } catch (e: JSException) {
                    Log.error { "[JSPlugin] JSException in popularNovels: ${e.message}" }
                    throw JSPluginError.ExecutionError(pluginId, "popularNovels", e)
                } catch (e: Exception) {
                    Log.error { "[JSPlugin] Exception in popularNovels: ${e.message}" }
                    throw JSPluginError.ExecutionError(pluginId, "popularNovels", e)
                }
            }
        }
    }

    /**
     * Calls the plugin's searchNovels method.
     * @param searchTerm The search query string
     * @param page The page number to fetch (1-indexed)
     */
    suspend fun searchNovels(searchTerm: String, page: Int): List<JSNovelItem> {
        return kotlinx.coroutines.withContext(jsDispatcher) {
            withTimeout(30000L) {
                try {
                    // Validate search term
                    if (searchTerm.isBlank()) {
                        Log.warn { "[JSPlugin] Empty search term provided for plugin: $pluginId" }
                        return@withTimeout emptyList()
                    }

                    val startTime = currentTimeToLong()

                    // Use async execution for better performance
                    val sanitizedId = pluginId.replace(Regex("[^a-zA-Z0-9_]"), "_")
                    val escapedSearchTerm = searchTerm.replace("'", "\\'").replace("\"", "\\\"")
                        .replace("\n", "\\n")

                    val callScript = """
                    (async function() {
                        try {
                            const plugin = globalThis.__plugin_${sanitizedId};
                            if (!plugin) {
                                throw new Error('Plugin not found in global context');
                            }
                            if (typeof plugin.searchNovels !== 'function') {
                                throw new Error('searchNovels method not found on plugin');
                            }
                            console.log('[JSPlugin] Calling searchNovels with term: $escapedSearchTerm, page: $page');
                            const result = await plugin.searchNovels('$escapedSearchTerm', $page);
                            console.log('[JSPlugin] searchNovels returned:', result);
                            console.log('[JSPlugin] Result type:', typeof result);
                            console.log('[JSPlugin] Result is array:', Array.isArray(result));
                            if (Array.isArray(result)) {
                                console.log('[JSPlugin] Result length:', result.length);
                                if (result.length > 0) {
                                    console.log('[JSPlugin] First item:', JSON.stringify(result[0]));
                                }
                            }
                            globalThis.__searchResult_${sanitizedId} = { success: true, data: result };
                        } catch (error) {
                            console.error('[JSPlugin] searchNovels error:', error);
                            const errorDetails = {
                                message: error.message || error.toString(),
                                stack: error.stack || 'No stack trace available',
                                name: error.name || 'Error',
                                type: error.constructor ? error.constructor.name : 'Unknown'
                            };
                            globalThis.__searchResult_${sanitizedId} = { 
                                success: false, 
                                error: JSON.stringify(errorDetails)
                            };
                        }
                    })();
                """.trimIndent()

                    engine.evaluateScript(callScript)

                    // Poll for result
                    var attempts = 0
                    val maxAttempts = 150
                    var result: Any? = null
                    var waitTime = 10L

                    while (attempts < maxAttempts) {
                        kotlinx.coroutines.delay(waitTime)

                        if (attempts % 5 == 0 || attempts < 10) {
                            val checkScript = "globalThis.__searchResult_${sanitizedId}"
                            val searchResult = engine.evaluateScript(checkScript)

                            if (searchResult is Map<*, *>) {
                                val success = searchResult["success"] as? Boolean ?: false
                                if (success) {
                                    result = searchResult["data"]
                                    break
                                } else {
                                    val error = searchResult["error"]
                                    Log.error { "[JSPlugin] Search failed: $error" }
                                    throw JSException("Search failed: $error")
                                }
                            }
                        }

                        attempts++
                        if (waitTime < 200) {
                            waitTime = (waitTime * 1.2).toLong().coerceAtMost(200)
                        }
                    }

                    if (result == null && attempts >= maxAttempts) {
                        throw JSException("Search timeout")
                    }

                    // Clean up
                    try {
                        engine.evaluateScript("delete globalThis.__searchResult_${sanitizedId};")
                    } catch (e: Exception) {
                        // Ignore cleanup errors
                    }

                    val novels = parseNovelList(result)

                    val duration = currentTimeToLong() - startTime
                    Log.info { "[JSPlugin] searchNovels returned ${novels.size} items in ${duration}ms" }

                    novels
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    throw JSPluginError.TimeoutError(pluginId, "searchNovels")
                } catch (e: JSException) {
                    throw JSPluginError.ExecutionError(pluginId, "searchNovels", e)
                } catch (e: Exception) {
                    throw JSPluginError.ExecutionError(pluginId, "searchNovels", e)
                }
            }
        }
    }

    /**
     * Calls the plugin's parseNovel method.
     * @param novelPath The path/URL to the novel page
     */
    suspend fun parseNovel(novelPath: String): JSSourceNovel {
        return kotlinx.coroutines.withContext(jsDispatcher) {
            withTimeout(30000L) {
                try {
                    if (novelPath.isBlank()) {
                        throw IllegalArgumentException("Novel path cannot be blank")
                    }

                    val startTime = currentTimeToLong()
                    val sanitizedId = pluginId.replace(Regex("[^a-zA-Z0-9_]"), "_")
                    val escapedPath = novelPath.replace("'", "\\'").replace("\"", "\\\"")
                        .replace("\n", "\\n")

                    val callScript = """
                    (async function() {
                        try {
                            const plugin = globalThis.__plugin_${sanitizedId};
                            if (!plugin) {
                                throw new Error('Plugin not found in global context');
                            }
                            if (typeof plugin.parseNovel !== 'function') {
                                throw new Error('parseNovel method not found on plugin');
                            }
                            const result = await plugin.parseNovel('$escapedPath');
                            globalThis.__parseNovelResult_${sanitizedId} = { success: true, data: result };
                        } catch (error) {
                            const errorDetails = {
                                message: error.message || error.toString(),
                                stack: error.stack || 'No stack trace available',
                                name: error.name || 'Error'
                            };
                            globalThis.__parseNovelResult_${sanitizedId} = { 
                                success: false, 
                                error: JSON.stringify(errorDetails)
                            };
                        }
                    })();
                """.trimIndent()

                    engine.evaluateScript(callScript)

                    // Poll for result with longer timeout for novel parsing
                    var attempts = 0
                    val maxAttempts = 200 // 40 seconds for novel parsing
                    var result: Any? = null
                    var waitTime = 10L

                    while (attempts < maxAttempts) {
                        kotlinx.coroutines.delay(waitTime)

                        if (attempts % 5 == 0 || attempts < 10) {
                            val checkScript = "globalThis.__parseNovelResult_${sanitizedId}"
                            val parseResult = engine.evaluateScript(checkScript)

                            if (parseResult is Map<*, *>) {
                                val success = parseResult["success"] as? Boolean ?: false
                                if (success) {
                                    result = parseResult["data"]
                                    break
                                } else {
                                    val error = parseResult["error"]
                                    Log.error { "[JSPlugin] parseNovel failed: $error" }
                                    throw JSException("parseNovel failed: $error")
                                }
                            }
                        }

                        attempts++
                        if (waitTime < 200) {
                            waitTime = (waitTime * 1.2).toLong().coerceAtMost(200)
                        }
                    }

                    if (result == null && attempts >= maxAttempts) {
                        throw JSException("parseNovel timeout")
                    }

                    // Clean up
                    try {
                        engine.evaluateScript("delete globalThis.__parseNovelResult_${sanitizedId};")
                    } catch (e: Exception) {
                        // Ignore cleanup errors
                    }

                    val novel = parseSourceNovel(result)

                    val duration = currentTimeToLong() - startTime
                    Log.info { "[JSPlugin] parseNovel returned novel with ${novel.chapters.size} chapters in ${duration}ms" }

                    novel
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    throw JSPluginError.TimeoutError(pluginId, "parseNovel")
                } catch (e: JSException) {
                    throw JSPluginError.ExecutionError(pluginId, "parseNovel", e)
                } catch (e: Exception) {
                    throw JSPluginError.ExecutionError(pluginId, "parseNovel", e)
                }
            }
        }
    }

    /**
     * Calls the plugin's parseChapter method.
     * @param chapterPath The path/URL to the chapter page
     */
    suspend fun parseChapter(chapterPath: String): String {
        return withTimeout(30000L) {
            try {
                if (chapterPath.isBlank()) {
                    throw IllegalArgumentException("Chapter path cannot be blank")
                }

                val startTime = currentTimeToLong()
                val sanitizedId = pluginId.replace(Regex("[^a-zA-Z0-9_]"), "_")
                val escapedPath = chapterPath.replace("'", "\\'").replace("\"", "\\\"")
                    .replace("\n", "\\n")

                val callScript = """
                    (async function() {
                        try {
                            const plugin = globalThis.__plugin_${sanitizedId};
                            if (!plugin) {
                                throw new Error('Plugin not found in global context');
                            }
                            if (typeof plugin.parseChapter !== 'function') {
                                throw new Error('parseChapter method not found on plugin');
                            }
                            const result = await plugin.parseChapter('$escapedPath');
                            globalThis.__parseChapterResult_${sanitizedId} = { success: true, data: result };
                        } catch (error) {
                            const errorDetails = {
                                message: error.message || error.toString(),
                                stack: error.stack || 'No stack trace available',
                                name: error.name || 'Error'
                            };
                            globalThis.__parseChapterResult_${sanitizedId} = { 
                                success: false, 
                                error: JSON.stringify(errorDetails)
                            };
                        }
                    })();
                """.trimIndent()

                engine.evaluateScript(callScript)

                // Poll for result
                var attempts = 0
                val maxAttempts = 150
                var result: Any? = null
                var waitTime = 10L

                while (attempts < maxAttempts) {
                    kotlinx.coroutines.delay(waitTime)

                    if (attempts % 5 == 0 || attempts < 10) {
                        val checkScript =
                            "globalThis.__parseChapterResult_${sanitizedId}"
                        val parseResult = engine.evaluateScript(checkScript)

                        if (parseResult is Map<*, *>) {
                            val success = parseResult["success"] as? Boolean ?: false
                            if (success) {
                                result = parseResult["data"]
                                break
                            } else {
                                val error = parseResult["error"]
                                Log.error { "[JSPlugin] parseChapter failed: $error" }
                                throw JSException("parseChapter failed: $error")
                            }
                        }
                    }

                    attempts++
                    if (waitTime < 200) {
                        waitTime = (waitTime * 1.2).toLong().coerceAtMost(200)
                    }
                }

                if (result == null && attempts >= maxAttempts) {
                    throw JSException("parseChapter timeout")
                }

                // Clean up
                try {
                    engine.evaluateScript("delete globalThis.__parseChapterResult_${sanitizedId};")
                } catch (e: Exception) {
                    // Ignore cleanup errors
                }

                val content = result?.toString() ?: ""

                val duration = currentTimeToLong() - startTime
                Log.info { "[JSPlugin] parseChapter returned ${content.length} characters in ${duration}ms" }

                content
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                throw JSPluginError.TimeoutError(pluginId, "parseChapter")
            } catch (e: JSException) {
                throw JSPluginError.ExecutionError(pluginId, "parseChapter", e)
            } catch (e: Exception) {
                throw JSPluginError.ExecutionError(pluginId, "parseChapter", e)
            }
        }
    }

    /**
     * Extracts filter definitions from the plugin.
     * Manually reads each filter property to handle JavaScript constants.
     * Results are cached after first call.
     */
    suspend fun getFilters(): Map<String, FilterDefinition> {
        // Return cached filters if available
        cachedFilters?.let { return it }

        return try {
            val pluginMap = pluginInstance.asMap()

            val filtersObj = pluginMap["filters"]
            if (filtersObj == null) {
                return emptyMap()
            }

            // Get filter keys
            val filterKeys = when (filtersObj) {
                is Map<*, *> -> filtersObj.keys.mapNotNull { it?.toString() }
                is JSValue -> filtersObj.asMap().keys.mapNotNull { it?.toString() }
                else -> return emptyMap()
            }

            val filterDefinitions = mutableMapOf<String, FilterDefinition>()

            for (key in filterKeys) {
                // Get the filter object directly from the filtersObj map
                val filterObj = when (filtersObj) {
                    is Map<*, *> -> filtersObj[key]
                    is JSValue -> filtersObj.asMap()[key]
                    else -> null
                }

                if (filterObj == null) {
                    Log.warn { "[JSPlugin] Filter '$key' object is null" }
                    continue
                }

                // Convert filter object to map and resolve the type property
                val filterMap = when (filterObj) {
                    is Map<*, *> -> filterObj as Map<String, Any?>
                    is JSValue -> filterObj.asMap()
                    else -> {
                        Log.warn { "[JSPlugin] Filter '$key' is not a map: ${filterObj::class.simpleName}" }
                        continue
                    }
                }

                // The type property is null because it's a reference to FilterTypes constant
                // We need to evaluate it in JavaScript context
                val typeValue = filterMap["type"]

                val typeString = if (typeValue == null) {
                    // Type is null, need to evaluate it in JS context using the stored plugin reference
                    try {
                        // Access the filter's type property through the globally stored plugin instance
                        // Sanitize plugin ID for JavaScript variable name
                        val sanitizedId = pluginId.replace(Regex("[^a-zA-Z0-9_]"), "_")
                        val script = """
                            (function() {
                                const plugin = globalThis.__plugin_${sanitizedId};
                                if (!plugin || !plugin.filters) return null;
                                const filter = plugin.filters['$key'];
                                if (!filter || !filter.type) return null;
                                return String(filter.type);
                            })()
                        """.trimIndent()
                        val result = engine.evaluateScript(script)
                        result?.toString()
                    } catch (e: Exception) {
                        Log.error(
                            e,
                            "[JSPlugin] Failed to evaluate type for filter '$key': ${e.message}"
                        )
                        null
                    }
                } else {
                    when (typeValue) {
                        is String -> typeValue
                        is JSValue -> typeValue.toString()
                        else -> typeValue.toString()
                    }
                }

                // Create a new map with the resolved type
                val resolvedFilterMap = filterMap.toMutableMap()
                resolvedFilterMap["type"] = typeString

                val filterDef = parseFilterDefinition(resolvedFilterMap)
                if (filterDef != null) {
                    filterDefinitions[key] = filterDef
                } else {
                    Log.warn { "[JSPlugin] Failed to parse filter '$key'" }
                }
            }

            // Cache the filter definitions
            cachedFilters = filterDefinitions
            filterDefinitions
        } catch (e: Exception) {
            Log.error(e, "[JSPlugin] Failed to extract filters for plugin: $pluginId")
            emptyMap()
        }
    }

    /**
     * Parses a list of novel items from JavaScript result.
     */
    private fun parseNovelList(result: Any?): List<JSNovelItem> {
        if (result == null) {
            Log.warn { "[JSPlugin] parseNovelList received null result" }
            return emptyList()
        }

        return when (result) {
            is List<*> -> {
                result.mapNotNull { parseNovelItem(it) }
            }

            is JSValue -> {
                if (!result.isNull() && !result.isUndefined()) {
                    val list = result.asList()
                    list.mapNotNull { parseNovelItem(it) }
                } else {
                    Log.warn { "[JSPlugin] Result is JSValue but null or undefined" }
                    emptyList()
                }
            }

            else -> {
                Log.warn { "[JSPlugin] Result is unexpected type: ${result::class.qualifiedName}" }
                emptyList()
            }
        }
    }

    /**
     * Parses a single novel item from JavaScript object.
     */
    private fun parseNovelItem(item: Any?): JSNovelItem? {
        if (item == null) {
            return null
        }

        return try {
            val map = when (item) {
                is Map<*, *> -> {
                    item as Map<String, Any?>
                }

                is JSValue -> {
                    item.asMap()
                }

                else -> {
                    Log.warn { "[JSPlugin] Item is unexpected type: ${item::class.qualifiedName}" }
                    return null
                }
            }

            val name = map["name"]?.toString()
            val path = map["path"]?.toString()
            val cover = map["cover"]?.toString()

            if (name == null) {
                Log.warn { "[JSPlugin] Item missing 'name' field" }
                return null
            }
            if (path == null) {
                Log.warn { "[JSPlugin] Item missing 'path' field" }
                return null
            }

            JSNovelItem(
                name = name,
                path = path,
                cover = cover
            )
        } catch (e: Exception) {
            Log.error(e, "[JSPlugin] Failed to parse novel item: ${e.message}")
            null
        }
    }

    /**
     * Parses a source novel from JavaScript result.
     */
    private fun parseSourceNovel(result: Any?): JSSourceNovel {
        if (result == null) {
            throw IllegalArgumentException("parseNovel returned null")
        }

        val map = when (result) {
            is Map<*, *> -> result as Map<String, Any?>
            is JSValue -> result.asMap()
            else -> throw IllegalArgumentException("Invalid result type from parseNovel")
        }

        val chaptersList = map["chapters"] as? List<*> ?: emptyList<Any?>()
        val chapters = chaptersList.mapNotNull { parseChapterItem(it) }

        return JSSourceNovel(
            name = map["name"]?.toString() ?: "",
            path = map["path"]?.toString() ?: "",
            cover = map["cover"]?.toString(),
            genres = map["genres"]?.toString(),
            summary = map["summary"]?.toString(),
            author = map["author"]?.toString(),
            artist = map["artist"]?.toString(),
            status = map["status"]?.toString(),
            chapters = chapters
        )
    }

    /**
     * Parses a chapter item from JavaScript object.
     */
    private fun parseChapterItem(item: Any?): JSChapterItem? {
        if (item == null) return null

        return try {
            val map = when (item) {
                is Map<*, *> -> item as Map<String, Any?>
                is JSValue -> item.asMap()
                else -> return null
            }

            JSChapterItem(
                name = map["name"]?.toString() ?: return null,
                path = map["path"]?.toString() ?: return null,
                chapterNumber = map["chapterNumber"]?.toString()?.toIntOrNull(),
                releaseTime = map["releaseTime"]?.toString()
            )
        } catch (e: Exception) {
            Log.error(e, "[JSPlugin] Failed to parse chapter item")
            null
        }
    }

    /**
     * Parses a filter definition from JavaScript object.
     */
    private fun parseFilterDefinition(value: Any?): FilterDefinition? {
        if (value == null) return null

        return try {
            val map = when (value) {
                is Map<*, *> -> value as Map<String, Any?>
                is JSValue -> value.asMap()
                else -> {
                    Log.warn { "[JSPlugin] Filter value is not a map: ${value::class.simpleName}" }
                    return null
                }
            }

            val type = map["type"]?.toString()
            if (type == null) {
                Log.warn { "[JSPlugin] Filter has no type property" }
                return null
            }

            val label = map["label"]?.toString() ?: ""

            when (type.lowercase()) {
                "picker" -> {
                    val options = parseFilterOptions(map["options"])
                    // Check both 'value' (LNReader style) and 'defaultValue' for compatibility
                    val defaultValue =
                        map["value"]?.toString() ?: map["defaultValue"]?.toString()
                        ?: ""
                    FilterDefinition.Picker(label, options, defaultValue)
                }

                "textinput", "text" -> {
                    val defaultValue =
                        map["value"]?.toString() ?: map["defaultValue"]?.toString()
                        ?: ""
                    FilterDefinition.TextInput(label, defaultValue)
                }

                "checkboxgroup", "checkbox" -> {
                    val options = parseFilterOptions(map["options"])
                    // Check both 'value' and 'defaultValues' for compatibility
                    val defaultValues = parseStringList(map["value"])
                        ?: parseStringList(map["defaultValues"])
                    FilterDefinition.CheckboxGroup(label, options, defaultValues)
                }

                "excludablecheckboxgroup", "excludable", "xcheckbox" -> {
                    val options = parseFilterOptions(map["options"])
                    val included = parseStringList(map["included"])
                    val excluded = parseStringList(map["excluded"])
                    FilterDefinition.ExcludableCheckboxGroup(
                        label,
                        options,
                        included,
                        excluded
                    )
                }

                else -> {
                    Log.warn { "[JSPlugin] Unknown filter type: $type" }
                    null
                }
            }
        } catch (e: Exception) {
            Log.error(e, "[JSPlugin] Failed to parse filter definition: ${e.message}")
            null
        }
    }

    /**
     * Parses filter options from JavaScript array.
     */
    private fun parseFilterOptions(value: Any?): List<FilterOption> {
        if (value == null) return emptyList()

        val list = when (value) {
            is List<*> -> value
            is JSValue -> value.asList()
            else -> return emptyList()
        }

        return list.mapNotNull { item ->
            try {
                val map = when (item) {
                    is Map<*, *> -> item as Map<String, Any?>
                    is JSValue -> item.asMap()
                    else -> return@mapNotNull null
                }

                FilterOption(
                    label = map["label"]?.toString() ?: return@mapNotNull null,
                    value = map["value"]?.toString() ?: return@mapNotNull null
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Parses a list of strings from JavaScript array.
     */
    private fun parseStringList(value: Any?): List<String> {
        if (value == null) return emptyList()

        val list = when (value) {
            is List<*> -> value
            is JSValue -> value.asList()
            else -> return emptyList()
        }

        return list.mapNotNull { it?.toString() }
    }
}


