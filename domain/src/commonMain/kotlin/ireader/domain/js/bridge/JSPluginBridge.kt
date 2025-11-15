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
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json

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
    
    private val pluginInstance: JSValue = JSValue.from(pluginInstanceRaw)
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    /**
     * Extracts plugin metadata from the plugin instance.
     */
    suspend fun getPluginMetadata(): PluginMetadata {
        return withTimeout(30000L) {
            try {
                Log.debug { "[JSPlugin] Extracting metadata for plugin: $pluginId" }
                val startTime = System.currentTimeMillis()
                
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
                
                val duration = System.currentTimeMillis() - startTime
                Log.debug { "[JSPlugin] Metadata extracted in ${duration}ms: ${metadata.name}" }
                
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
        Log.debug { "[JSPlugin] Calling method: $methodName with ${args.size} arguments" }
        args.forEachIndexed { index, arg ->
            Log.debug { "[JSPlugin] Arg[$index]: ${arg?.javaClass?.simpleName} = $arg" }
        }
        
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
        val resultVar = "__pluginResult_${System.currentTimeMillis()}"
        val errorVar = "__pluginError_${System.currentTimeMillis()}"
        val doneVar = "__pluginDone_${System.currentTimeMillis()}"
        
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
        
        Log.debug { "[JSPlugin] Evaluating script for $methodName" }
        
        return try {
            val initialResult = engine.evaluateScript(script)
            
            if (initialResult == "SYNC_ERROR") {
                val error = engine.getGlobalObject(errorVar)
                Log.error { "[JSPlugin] Synchronous error in $methodName: $error" }
                throw JSException("Synchronous error in $methodName: $error")
            } else if (initialResult == "PROMISE_PENDING") {
                Log.debug { "[JSPlugin] Waiting for Promise to resolve..." }
                
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
                        
                        val result = engine.getGlobalObject(resultVar)
                        Log.debug { "[JSPlugin] Promise resolved after ${attempts * 100}ms" }
                        return result
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
     * Calls the plugin's popularNovels method.
     */
    suspend fun popularNovels(page: Int, filters: Map<String, Any>): List<JSNovelItem> {
        return withTimeout(30000L) {
            try {
                Log.debug { "[JSPlugin] Calling popularNovels for plugin: $pluginId, page: $page" }
                Log.debug { "[JSPlugin] Filters received: $filters" }
                Log.debug { "[JSPlugin] Filters keys: ${filters.keys}" }
                filters.forEach { (key, value) ->
                    Log.debug { "[JSPlugin] Filter '$key': $value (type: ${value.javaClass.simpleName})" }
                    if (value is Map<*, *>) {
                        value.forEach { (k, v) ->
                            Log.debug { "[JSPlugin]   '$key'.$k = $v (${v?.javaClass?.simpleName})" }
                        }
                    }
                }
                val startTime = System.currentTimeMillis()
                
                // Convert filters to JSON and keep them in JavaScript context
                val filtersJson = buildString {
                    append("{")
                    filters.entries.forEachIndexed { index, (key, value) ->
                        if (index > 0) append(",")
                        append("\"$key\":")
                        when (value) {
                            is Map<*, *> -> {
                                append("{")
                                value.entries.forEachIndexed { vIndex, (vKey, vValue) ->
                                    if (vIndex > 0) append(",")
                                    append("\"$vKey\":")
                                    when (vValue) {
                                        is String -> append("\"${vValue.replace("\"", "\\\"")}\"")
                                        is List<*> -> {
                                            append("[")
                                            vValue.forEachIndexed { i, item ->
                                                if (i > 0) append(",")
                                                append("\"$item\"")
                                            }
                                            append("]")
                                        }
                                        else -> append("\"$vValue\"")
                                    }
                                }
                                append("}")
                            }
                            else -> append("\"$value\"")
                        }
                    }
                    append("}")
                }
                
                Log.debug { "[JSPlugin] Filters JSON: $filtersJson" }
                
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
                            if (!plugin) throw new Error('Plugin not found in global context');
                            const result = await plugin.popularNovels($page, options);
                            globalThis.__promiseResult_${sanitizedId} = { success: true, data: result };
                        } catch (error) {
                            globalThis.__promiseResult_${sanitizedId} = { success: false, error: error.toString() };
                        }
                    })();
                """.trimIndent()
                
                // Start the async operation
                engine.evaluateScript(callScript)
                
                // Poll for the result
                var attempts = 0
                val maxAttempts = 300 // 30 seconds with 100ms intervals
                var result: Any? = null
                
                while (attempts < maxAttempts) {
                    Thread.sleep(100)
                    val checkScript = "globalThis.__promiseResult_${sanitizedId}"
                    val promiseResult = engine.evaluateScript(checkScript)
                    
                    if (promiseResult is Map<*, *>) {
                        val success = promiseResult["success"] as? Boolean ?: false
                        if (success) {
                            result = promiseResult["data"]
                            Log.debug { "[JSPlugin] Promise resolved successfully after ${attempts * 100}ms" }
                            break
                        } else {
                            val error = promiseResult["error"]
                            Log.error { "[JSPlugin] Promise rejected: $error" }
                            throw JSException("Promise rejected: $error")
                        }
                    }
                    attempts++
                }
                
                if (result == null && attempts >= maxAttempts) {
                    Log.error { "[JSPlugin] Promise timeout after ${maxAttempts * 100}ms" }
                    throw JSException("Promise timeout")
                }
                
                // Clean up
                engine.evaluateScript("delete globalThis.__promiseResult_${sanitizedId};")
                
                Log.debug { "[JSPlugin] popularNovels result type: ${result?.javaClass?.simpleName}" }
                Log.debug { "[JSPlugin] popularNovels raw result: $result" }
                
                if (result is List<*>) {
                    Log.debug { "[JSPlugin] Result is a list with ${result.size} items" }
                    if (result.isNotEmpty()) {
                        Log.debug { "[JSPlugin] First item: ${result.first()}" }
                    }
                } else if (result is Map<*, *>) {
                    Log.debug { "[JSPlugin] Result is a map with keys: ${result.keys}" }
                    Log.debug { "[JSPlugin] Map contents: $result" }
                } else if (result == null) {
                    Log.warn { "[JSPlugin] popularNovels returned null for plugin: $pluginId" }
                    return@withTimeout emptyList()
                }
                
                val novels = parseNovelList(result)
                Log.debug { "[JSPlugin] Parsed ${novels.size} novels from result" }
                if (novels.isEmpty() && result != null) {
                    Log.warn { "[JSPlugin] parseNovelList returned empty list from non-null result" }
                }
                
                val duration = System.currentTimeMillis() - startTime
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
    
    /**
     * Calls the plugin's searchNovels method.
     */
    suspend fun searchNovels(searchTerm: String, page: Int): List<JSNovelItem> {
        return withTimeout(30000L) {
            try {
                Log.debug { "[JSPlugin] Calling searchNovels for plugin: $pluginId, query: $searchTerm, page: $page" }
                val startTime = System.currentTimeMillis()
                
                val result = callPluginMethod("searchNovels", searchTerm, page)
                
                val novels = parseNovelList(result)
                
                val duration = System.currentTimeMillis() - startTime
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
    
    /**
     * Calls the plugin's parseNovel method.
     */
    suspend fun parseNovel(novelPath: String): JSSourceNovel {
        return withTimeout(30000L) {
            try {
                Log.debug { "[JSPlugin] Calling parseNovel for plugin: $pluginId, path: $novelPath" }
                val startTime = System.currentTimeMillis()
                
                val result = callPluginMethod("parseNovel", novelPath)
                
                val novel = parseSourceNovel(result)
                
                val duration = System.currentTimeMillis() - startTime
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
    
    /**
     * Calls the plugin's parseChapter method.
     */
    suspend fun parseChapter(chapterPath: String): String {
        return withTimeout(30000L) {
            try {
                Log.debug { "[JSPlugin] Calling parseChapter for plugin: $pluginId, path: $chapterPath" }
                val startTime = System.currentTimeMillis()
                
                val result = callPluginMethod("parseChapter", chapterPath)
                
                val content = result?.toString() ?: ""
                
                val duration = System.currentTimeMillis() - startTime
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
     */
    suspend fun getFilters(): Map<String, FilterDefinition> {
        return try {
            Log.debug { "[JSPlugin] Extracting filters for plugin: $pluginId" }
            
            val pluginMap = pluginInstance.asMap()
            Log.debug { "[JSPlugin] Plugin instance keys: ${pluginMap.keys}" }
            
            val filtersObj = pluginMap["filters"]
            if (filtersObj == null) {
                Log.warn { "[JSPlugin] No filters property found in plugin" }
                return emptyMap()
            }
            
            Log.debug { "[JSPlugin] Filters object type: ${filtersObj.javaClass.simpleName}" }
            
            // Get filter keys
            val filterKeys = when (filtersObj) {
                is Map<*, *> -> filtersObj.keys.mapNotNull { it?.toString() }
                is JSValue -> filtersObj.asMap().keys.mapNotNull { it?.toString() }
                else -> return emptyMap()
            }
            
            Log.debug { "[JSPlugin] Found ${filterKeys.size} filter entries: $filterKeys" }
            
            val filterDefinitions = mutableMapOf<String, FilterDefinition>()
            
            for (key in filterKeys) {
                Log.debug { "[JSPlugin] Parsing filter '$key'" }
                
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
                        Log.warn { "[JSPlugin] Filter '$key' is not a map: ${filterObj.javaClass.simpleName}" }
                        continue
                    }
                }
                
                // The type property is null because it's a reference to FilterTypes constant
                // We need to evaluate it in JavaScript context
                val typeValue = filterMap["type"]
                Log.debug { "[JSPlugin] Filter '$key' raw type value: $typeValue (class: ${typeValue?.javaClass?.name})" }
                
                val typeString = if (typeValue == null) {
                    // Type is null, need to evaluate it in JS context using the stored plugin reference
                    try {
                        // First check what the type value actually is
                        val checkScript = """
                            (function() {
                                const plugin = globalThis.__plugin_${pluginId};
                                if (!plugin) return 'NO_PLUGIN';
                                if (!plugin.filters) return 'NO_FILTERS';
                                const filter = plugin.filters['$key'];
                                if (!filter) return 'NO_FILTER_$key';
                                
                                // Check type value - don't use if (!filter.type) because it might be falsy but exist
                                const typeVal = filter.type;
                                const typeType = typeof typeVal;
                                const typeStr = typeVal === null ? 'null' : 
                                               typeVal === undefined ? 'undefined' : 
                                               String(typeVal);
                                
                                return 'type: ' + typeType + ', value: ' + typeStr + ', hasOwnProperty: ' + filter.hasOwnProperty('type');
                            })()
                        """.trimIndent()
                        val checkResult = engine.evaluateScript(checkScript)
                        Log.debug { "[JSPlugin] Check result for '$key': $checkResult" }
                        
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
                        Log.debug { "[JSPlugin] Evaluated type from JS for '$key': $result (${result?.javaClass?.simpleName})" }
                        result?.toString()
                    } catch (e: Exception) {
                        Log.error(e, "[JSPlugin] Failed to evaluate type for filter '$key': ${e.message}" )
                        null
                    }
                } else {
                    when (typeValue) {
                        is String -> typeValue
                        is JSValue -> typeValue.toString()
                        else -> typeValue.toString()
                    }
                }
                
                Log.debug { "[JSPlugin] Filter '$key' resolved type: $typeString" }
                
                // Create a new map with the resolved type
                val resolvedFilterMap = filterMap.toMutableMap()
                resolvedFilterMap["type"] = typeString
                
                Log.debug { "[JSPlugin] Filter '$key' resolved data: $resolvedFilterMap" }
                
                val filterDef = parseFilterDefinition(resolvedFilterMap)
                if (filterDef != null) {
                    filterDefinitions[key] = filterDef
                    Log.debug { "[JSPlugin] Successfully parsed filter '$key': $filterDef" }
                } else {
                    Log.warn { "[JSPlugin] Failed to parse filter '$key'" }
                }
            }
            
            Log.debug { "[JSPlugin] Extracted ${filterDefinitions.size} filters" }
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
        if (result == null) return emptyList()
        
        return when (result) {
            is List<*> -> result.mapNotNull { parseNovelItem(it) }
            is JSValue -> {
                if (!result.isNull() && !result.isUndefined()) {
                    result.asList().mapNotNull { parseNovelItem(it) }
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }
    
    /**
     * Parses a single novel item from JavaScript object.
     */
    private fun parseNovelItem(item: Any?): JSNovelItem? {
        if (item == null) return null
        
        return try {
            val map = when (item) {
                is Map<*, *> -> item as Map<String, Any?>
                is JSValue -> item.asMap()
                else -> return null
            }
            
            JSNovelItem(
                name = map["name"]?.toString() ?: return null,
                path = map["path"]?.toString() ?: return null,
                cover = map["cover"]?.toString()
            )
        } catch (e: Exception) {
            Log.error(e, "[JSPlugin] Failed to parse novel item")
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
                    Log.warn { "[JSPlugin] Filter value is not a map: ${value.javaClass.simpleName}" }
                    return null
                }
            }
            
            val type = map["type"]?.toString()
            if (type == null) {
                Log.warn { "[JSPlugin] Filter has no type property" }
                return null
            }
            
            val label = map["label"]?.toString() ?: ""
            Log.debug { "[JSPlugin] Parsing filter with type='$type', label='$label'" }
            
            when (type.lowercase()) {
                "picker" -> {
                    val options = parseFilterOptions(map["options"])
                    // Check both 'value' (LNReader style) and 'defaultValue' for compatibility
                    val defaultValue = map["value"]?.toString() ?: map["defaultValue"]?.toString() ?: ""
                    FilterDefinition.Picker(label, options, defaultValue)
                }
                "textinput", "text" -> {
                    val defaultValue = map["value"]?.toString() ?: map["defaultValue"]?.toString() ?: ""
                    FilterDefinition.TextInput(label, defaultValue)
                }
                "checkboxgroup", "checkbox" -> {
                    Log.debug { "[JSPlugin] Parsing CheckboxGroup filter" }
                    val options = parseFilterOptions(map["options"])
                    Log.debug { "[JSPlugin] Parsed ${options.size} options" }
                    // Check both 'value' and 'defaultValues' for compatibility
                    val defaultValues = parseStringList(map["value"]) ?: parseStringList(map["defaultValues"])
                    Log.debug { "[JSPlugin] Default values: $defaultValues" }
                    FilterDefinition.CheckboxGroup(label, options, defaultValues)
                }
                "excludablecheckboxgroup", "excludable", "xcheckbox" -> {
                    val options = parseFilterOptions(map["options"])
                    val included = parseStringList(map["included"])
                    val excluded = parseStringList(map["excluded"])
                    FilterDefinition.ExcludableCheckboxGroup(label, options, included, excluded)
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
