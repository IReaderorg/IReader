package ireader.domain.js.loader

import ireader.core.http.CookieSynchronizer
import ireader.domain.js.bridge.JSBridgeService
import ireader.domain.js.bridge.FetchOptions
import ireader.domain.js.bridge.LNReaderPlugin
import ireader.domain.js.bridge.PluginChapter
import ireader.domain.js.bridge.PluginNovel
import ireader.domain.js.bridge.PluginNovelDetails
import platform.JavaScriptCore.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

/**
 * iOS implementation of JS engine creation using JavaScriptCore
 */
actual fun createEngine(bridgeService: JSBridgeService): JSEngine {
    return IosJSEngine(bridgeService)
}

/**
 * iOS implementation of cookie synchronizer
 */
actual fun createPlatformCookieSynchronizer(): CookieSynchronizer {
    return CookieSynchronizer()
}

/**
 * iOS JS Engine implementation using JavaScriptCore
 */
@OptIn(ExperimentalForeignApi::class)
private class IosJSEngine(private val bridgeService: JSBridgeService) : JSEngine {
    
    private var jsContext: JSContext? = null
    private var isLoaded = false
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    init {
        setupContext()
    }
    
    private fun setupContext() {
        jsContext = JSContext().apply {
            // Set up exception handler
            exceptionHandler = { _, exception ->
                println("[IosJSEngine] JS Error: ${exception?.toString() ?: "Unknown"}")
            }
            
            // Set up console and Promise polyfills
            evaluateScript("""
                var console = {
                    log: function() { /* no-op */ },
                    error: function() { /* no-op */ },
                    warn: function() { /* no-op */ }
                };
            """)
        }
    }
    
    override suspend fun loadPlugin(jsCode: String, pluginId: String): LNReaderPlugin {
        val ctx = jsContext ?: throw IllegalStateException("JSContext not initialized")
        
        // Evaluate the plugin code
        ctx.evaluateScript(jsCode)
        
        // Check for errors
        ctx.exception?.let { exception ->
            val errorMsg = exception.toString()
            ctx.exception = null
            throw RuntimeException("Failed to load plugin: $errorMsg")
        }
        
        isLoaded = true
        return IosLNReaderPlugin(ctx, pluginId, scope)
    }
    
    override fun close() {
        scope.cancel()
        jsContext = null
        isLoaded = false
    }
    
    override fun isLoaded(): Boolean = isLoaded
}

/**
 * iOS LNReader Plugin implementation
 */
@OptIn(ExperimentalForeignApi::class)
private class IosLNReaderPlugin(
    private val context: JSContext,
    private val pluginId: String,
    private val scope: CoroutineScope
) : LNReaderPlugin {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun getId(): String = callPluginMethod("id") ?: pluginId
    override suspend fun getName(): String = callPluginMethod("name") ?: "Unknown"
    override suspend fun getSite(): String = callPluginMethod("site") ?: ""
    override suspend fun getVersion(): String = callPluginMethod("version") ?: "1.0.0"
    override suspend fun getLang(): String = callPluginMethod("lang") ?: "en"
    override suspend fun getIcon(): String = callPluginMethod("icon") ?: ""
    
    override fun getFilters(): Map<String, Any> {
        val result = context.evaluateScript("JSON.stringify(plugin.filters || {})")
        return try {
            json.decodeFromString<Map<String, String>>(result?.toString() ?: "{}").mapValues { it.value as Any }
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    override suspend fun popularNovels(page: Int, filters: Map<String, Any>): List<PluginNovel> {
        return callPluginMethodForNovels("popularNovels", page)
    }
    
    override suspend fun searchNovels(query: String, page: Int): List<PluginNovel> {
        return callPluginMethodForNovels("searchNovels", query, page)
    }
    
    override suspend fun latestNovels(page: Int): List<PluginNovel> {
        return callPluginMethodForNovels("latestNovels", page)
    }
    
    override suspend fun getNovelDetails(url: String): PluginNovelDetails {
        val result = callPluginMethod("parseNovel", url)
        return try {
            if (result != null) {
                json.decodeFromString<PluginNovelDetails>(result)
            } else {
                PluginNovelDetails(
                    name = "",
                    url = url,
                    cover = "",
                    author = null,
                    description = null,
                    genres = emptyList(),
                    status = null
                )
            }
        } catch (e: Exception) {
            PluginNovelDetails(
                name = "",
                url = url,
                cover = "",
                author = null,
                description = null,
                genres = emptyList(),
                status = null
            )
        }
    }
    
    override suspend fun getChapters(url: String): List<PluginChapter> {
        val result = callPluginMethod("parseChapters", url)
        return try {
            if (result != null) {
                json.decodeFromString<List<PluginChapter>>(result)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getChapterContent(url: String): String {
        return callPluginMethod("parseChapter", url) ?: ""
    }
    
    private fun callPluginMethod(method: String, vararg args: Any?): String? {
        val argsStr = args.joinToString(", ") { arg ->
            when (arg) {
                is String -> "'${arg.replace("'", "\\'")}'"
                is Number -> arg.toString()
                is Map<*, *> -> {
                    try {
                        val mapStr = (arg as Map<String, Any>).entries.joinToString(",") { (k, v) ->
                            "\"$k\":\"$v\""
                        }
                        "JSON.parse('{$mapStr}')"
                    } catch (e: Exception) {
                        "null"
                    }
                }
                else -> "null"
            }
        }
        
        val script = if (args.isEmpty()) {
            "plugin.$method"
        } else {
            "plugin.$method($argsStr)"
        }
        
        val result = context.evaluateScript(script)
        return result?.toString()
    }
    
    private fun callPluginMethodForNovels(method: String, vararg args: Any?): List<PluginNovel> {
        val result = callPluginMethod(method, *args)
        return try {
            if (result != null) {
                json.decodeFromString<List<PluginNovel>>(result)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
