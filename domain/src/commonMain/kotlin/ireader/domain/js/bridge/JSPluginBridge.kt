package ireader.domain.js.bridge

import io.ktor.client.HttpClient
import ireader.core.log.Log
import ireader.domain.js.engine.JSEngine
import ireader.domain.js.engine.JSException
import ireader.domain.js.engine.JSValue
import ireader.domain.js.models.*
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
     * Calls the plugin's popularNovels method.
     */
    suspend fun popularNovels(page: Int, filters: Map<String, Any>): List<JSNovelItem> {
        return withTimeout(30000L) {
            try {
                Log.debug { "[JSPlugin] Calling popularNovels for plugin: $pluginId, page: $page" }
                val startTime = System.currentTimeMillis()
                
                val options = mapOf("filters" to filters)
                val result = engine.callFunction("popularNovels", pluginInstance, page, options)
                
                val novels = parseNovelList(result)
                
                val duration = System.currentTimeMillis() - startTime
                Log.info { "[JSPlugin] popularNovels returned ${novels.size} items in ${duration}ms" }
                
                novels
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                throw JSPluginError.TimeoutError(pluginId, "popularNovels")
            } catch (e: JSException) {
                throw JSPluginError.ExecutionError(pluginId, "popularNovels", e)
            } catch (e: Exception) {
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
                
                val result = engine.callFunction("searchNovels", pluginInstance, searchTerm, page)
                
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
                
                val result = engine.callFunction("parseNovel", pluginInstance, novelPath)
                
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
                
                val result = engine.callFunction("parseChapter", pluginInstance, chapterPath)
                
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
     */
    suspend fun getFilters(): Map<String, FilterDefinition> {
        return try {
            Log.debug { "[JSPlugin] Extracting filters for plugin: $pluginId" }
            
            val pluginMap = pluginInstance.asMap()
            val filtersMap = pluginMap["filters"] as? Map<String, Any> ?: return emptyMap()
            
            val filterDefinitions = mutableMapOf<String, FilterDefinition>()
            
            for ((key, value) in filtersMap) {
                val filterDef = parseFilterDefinition(value)
                if (filterDef != null) {
                    filterDefinitions[key] = filterDef
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
                else -> return null
            }
            
            val type = map["type"]?.toString() ?: return null
            val label = map["label"]?.toString() ?: ""
            
            when (type.lowercase()) {
                "picker" -> {
                    val options = parseFilterOptions(map["options"])
                    val defaultValue = map["defaultValue"]?.toString() ?: ""
                    FilterDefinition.Picker(label, options, defaultValue)
                }
                "textinput", "text" -> {
                    val defaultValue = map["defaultValue"]?.toString() ?: ""
                    FilterDefinition.TextInput(label, defaultValue)
                }
                "checkboxgroup", "checkbox" -> {
                    val options = parseFilterOptions(map["options"])
                    val defaultValues = parseStringList(map["defaultValues"])
                    FilterDefinition.CheckboxGroup(label, options, defaultValues)
                }
                "excludablecheckboxgroup", "excludable" -> {
                    val options = parseFilterOptions(map["options"])
                    val included = parseStringList(map["included"])
                    val excluded = parseStringList(map["excluded"])
                    FilterDefinition.ExcludableCheckboxGroup(label, options, included, excluded)
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.error(e, "[JSPlugin] Failed to parse filter definition")
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
