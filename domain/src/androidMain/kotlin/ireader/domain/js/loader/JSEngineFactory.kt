package ireader.domain.js.loader

import ireader.core.log.Log
import ireader.domain.js.bridge.JSBridgeService
import ireader.domain.js.bridge.LNReaderPlugin
import ireader.domain.js.bridge.PluginChapter
import ireader.domain.js.bridge.PluginNovel
import ireader.domain.js.bridge.PluginNovelDetails
import ireader.domain.js.engine.NoJSEngineException
import ireader.domain.plugins.PluginClassLoader
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginStatus
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.reflect.Method

/**
 * Android implementation.
 * 
 * NOTE: J2V8 JavaScript engine has been moved to an optional plugin
 * (io.github.ireaderorg.plugins.j2v8-engine) to reduce app size.
 * 
 * The plugin handles its own native library loading internally.
 * This factory just checks if the plugin is ready and uses its ClassLoader.
 */
actual fun createEngine(bridgeService: JSBridgeService): JSEngine {
    // Check if J2V8 is ready
    if (J2V8EngineHelper.isJ2V8Ready()) {
        return J2V8ReflectionEngine(bridgeService)
    }
    
    // Try to initialize J2V8 from plugin
    if (J2V8EngineHelper.tryInitializeJ2V8()) {
        return J2V8ReflectionEngine(bridgeService)
    }
    
    // J2V8 not available
    return StubJSEngine()
}

/**
 * Helper object to manage J2V8 plugin integration.
 * 
 * The J2V8 plugin handles its own native library loading.
 * This helper just checks if the plugin is ready and provides access to its ClassLoader.
 */
object J2V8EngineHelper : KoinComponent {
    private const val J2V8_PLUGIN_ID = "io.github.ireaderorg.plugins.j2v8-engine"
    private const val J2V8_PLUGIN_CLASS = "io.github.ireaderorg.plugins.j2v8engine.J2V8EnginePlugin"
    
    private var j2v8Ready = false
    private var initAttempted = false
    private var j2v8ClassLoader: ClassLoader? = null
    private var j2v8PluginInstance: Any? = null
    
    private val pluginManager: PluginManager by inject()
    
    /**
     * Check if J2V8 is ready to use.
     */
    fun isJ2V8Ready(): Boolean = j2v8Ready
    
    /**
     * Compatibility alias for isJ2V8Ready.
     */
    fun isJ2V8Loaded(): Boolean = j2v8Ready
    
    /**
     * Get the ClassLoader that contains J2V8 classes.
     */
    fun getJ2V8ClassLoader(): ClassLoader? = j2v8ClassLoader
    
    /**
     * Try to initialize J2V8 from the installed plugin.
     * The plugin handles its own native library loading.
     */
    @Synchronized
    fun tryInitializeJ2V8(): Boolean {
        if (j2v8Ready) {
            return true
        }
        
        // Check if plugin's ClassLoader is available
        val pluginClassLoader = PluginClassLoader.getClassLoader(J2V8_PLUGIN_ID)
        if (pluginClassLoader == null) {
            Log.info { "J2V8EngineHelper: J2V8 plugin ClassLoader not available" }
            return false
        }
        
        if (initAttempted) {
            Log.debug { "J2V8EngineHelper: Already attempted initialization" }
            return false
        }
        
        initAttempted = true
        Log.info { "J2V8EngineHelper: Initializing J2V8 from plugin..." }
        
        try {
            // Load the plugin class and check if it's ready
            val pluginClass = pluginClassLoader.loadClass(J2V8_PLUGIN_CLASS)
            
            // Get the plugin instance from PluginManager
            val plugins = pluginManager.pluginsFlow.value
            val j2v8PluginInfo = plugins.find { it.id == J2V8_PLUGIN_ID }
            
            if (j2v8PluginInfo == null) {
                Log.warn { "J2V8EngineHelper: Plugin not found in PluginManager" }
                // Try to create instance directly
                return tryDirectInitialization(pluginClassLoader, pluginClass)
            }
            
            // Check if plugin is enabled
            if (j2v8PluginInfo.status != PluginStatus.ENABLED) {
                Log.info { "J2V8EngineHelper: Plugin not enabled (status: ${j2v8PluginInfo.status})" }
                return false
            }
            
            // Try to get the plugin instance and check isReady()
            return tryDirectInitialization(pluginClassLoader, pluginClass)
            
        } catch (e: Exception) {
            Log.error { "J2V8EngineHelper: Failed to initialize: ${e.message}" }
            return false
        }
    }
    
    /**
     * Try direct initialization by checking if V8 class is loadable.
     */
    private fun tryDirectInitialization(classLoader: ClassLoader, pluginClass: Class<*>): Boolean {
        try {
            // Check if V8 class is available
            val v8Class = classLoader.loadClass("com.eclipsesource.v8.V8")
            Log.info { "J2V8EngineHelper: V8 class found: ${v8Class.name}" }
            
            // Try to create a V8 runtime to verify native library is loaded
            val createMethod = v8Class.getMethod("createV8Runtime")
            val runtime = createMethod.invoke(null)
            
            // Release the test runtime
            val releaseMethod = v8Class.getMethod("release")
            releaseMethod.invoke(runtime)
            
            // Success! Native library is loaded and working
            j2v8ClassLoader = classLoader
            j2v8Ready = true
            Log.info { "J2V8EngineHelper: J2V8 initialized successfully!" }
            return true
            
        } catch (e: UnsatisfiedLinkError) {
            Log.warn { "J2V8EngineHelper: Native library not loaded: ${e.message}" }
            return false
        } catch (e: Exception) {
            Log.warn { "J2V8EngineHelper: Initialization failed: ${e.message}" }
            return false
        }
    }
    
    /**
     * Compatibility alias for tryInitializeJ2V8.
     */
    fun tryLoadJ2V8FromPlugin(): Boolean = tryInitializeJ2V8()
    
    /**
     * Reset the initialization state.
     */
    fun reset() {
        initAttempted = false
        j2v8Ready = false
        j2v8ClassLoader = null
        j2v8PluginInstance = null
        Log.info { "J2V8EngineHelper: Reset - will retry on next request" }
    }
    
    /**
     * Check if J2V8 plugin is available.
     */
    fun isJ2V8PluginAvailable(): Boolean {
        if (PluginClassLoader.getClassLoader(J2V8_PLUGIN_ID) != null) {
            return true
        }
        val plugins = pluginManager.pluginsFlow.value
        val j2v8Plugin = plugins.find { it.id == J2V8_PLUGIN_ID }
        return j2v8Plugin != null && j2v8Plugin.status == PluginStatus.ENABLED
    }
    
    /**
     * Called when the J2V8 plugin becomes available.
     */
    fun onJ2V8PluginAvailable() {
        if (!j2v8Ready) {
            Log.info { "J2V8EngineHelper: J2V8 plugin became available, resetting for retry" }
            reset()
        }
    }
}

/**
 * Stub engine that throws NoJSEngineException.
 */
private class StubJSEngine : JSEngine {
    override suspend fun loadPlugin(jsCode: String, pluginId: String): LNReaderPlugin {
        throw NoJSEngineException(
            "JavaScript engine not available. Please install the 'J2V8 JavaScript Engine' " +
            "plugin from the Feature Store to use JavaScript-based sources."
        )
    }
    
    override fun close() {}
    override fun isLoaded(): Boolean = false
}

/**
 * J2V8-based JavaScript engine using reflection.
 */
private class J2V8ReflectionEngine(
    private val bridgeService: JSBridgeService
) : JSEngine {
    
    private var v8Runtime: Any? = null
    private var isEngineLoaded = false
    
    private var v8Class: Class<*>? = null
    private var createRuntimeMethod: Method? = null
    private var executeVoidScriptMethod: Method? = null
    private var executeScriptMethod: Method? = null
    private var executeStringScriptMethod: Method? = null
    private var releaseMethod: Method? = null
    
    init {
        try {
            val classLoader = J2V8EngineHelper.getJ2V8ClassLoader()
                ?: throw IllegalStateException("J2V8 ClassLoader not available")
            
            v8Class = classLoader.loadClass("com.eclipsesource.v8.V8")
            createRuntimeMethod = v8Class?.getMethod("createV8Runtime")
            executeVoidScriptMethod = v8Class?.getMethod("executeVoidScript", String::class.java)
            executeScriptMethod = v8Class?.getMethod("executeScript", String::class.java)
            executeStringScriptMethod = v8Class?.getMethod("executeStringScript", String::class.java)
            releaseMethod = v8Class?.getMethod("release")
        } catch (e: Exception) {
            Log.error { "J2V8ReflectionEngine: Failed to initialize: ${e.message}" }
        }
    }
    
    override suspend fun loadPlugin(jsCode: String, pluginId: String): LNReaderPlugin {
        try {
            val runtime = createRuntimeMethod?.invoke(null)
                ?: throw ireader.domain.js.engine.PluginLoadException("Failed to create V8 runtime")
            v8Runtime = runtime
            
            executeVoidScriptMethod?.invoke(runtime, jsCode)
            
            val plugin = J2V8ReflectionPluginWrapper(runtime, bridgeService, pluginId, this)
            isEngineLoaded = true
            
            return plugin
        } catch (e: Exception) {
            close()
            val cause = e.cause ?: e
            throw ireader.domain.js.engine.PluginLoadException("Failed to load JS plugin: ${cause.message}", cause)
        }
    }
    
    fun executeScript(script: String): Any? {
        return try {
            executeScriptMethod?.invoke(v8Runtime, script)
        } catch (e: Exception) {
            Log.warn { "J2V8ReflectionEngine: executeScript error: ${e.cause?.message ?: e.message}" }
            null
        }
    }
    
    fun executeStringScript(script: String): String? {
        return try {
            executeStringScriptMethod?.invoke(v8Runtime, script) as? String
        } catch (e: Exception) {
            Log.warn { "J2V8ReflectionEngine: executeStringScript error: ${e.cause?.message ?: e.message}" }
            null
        }
    }
    
    override fun close() {
        try {
            v8Runtime?.let { releaseMethod?.invoke(it) }
        } catch (e: Exception) {}
        v8Runtime = null
        isEngineLoaded = false
    }
    
    override fun isLoaded(): Boolean = isEngineLoaded
}

/**
 * Wrapper that adapts J2V8 runtime to LNReaderPlugin interface.
 */
private class J2V8ReflectionPluginWrapper(
    private val runtime: Any,
    private val bridgeService: JSBridgeService,
    private val pluginId: String,
    private val engine: J2V8ReflectionEngine
) : LNReaderPlugin {
    
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    
    override suspend fun getId(): String = callStringFunction("getId") ?: pluginId
    override suspend fun getName(): String = callStringFunction("getName") ?: "Unknown"
    override suspend fun getSite(): String = callStringFunction("getSite") ?: ""
    override suspend fun getVersion(): String = callStringFunction("getVersion") ?: "1.0.0"
    override suspend fun getLang(): String = callStringFunction("getLang") ?: "en"
    override suspend fun getIcon(): String = callStringFunction("getIcon") ?: ""
    
    override suspend fun popularNovels(page: Int, filters: Map<String, Any>): List<PluginNovel> {
        val jsonStr = callAsyncFunction("popularNovels", page) ?: "[]"
        return try {
            json.decodeFromString<List<PluginNovel>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun latestNovels(page: Int): List<PluginNovel> {
        val jsonStr = callAsyncFunction("latestNovels", page) ?: "[]"
        return try {
            json.decodeFromString<List<PluginNovel>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun searchNovels(query: String, page: Int): List<PluginNovel> {
        val jsonStr = callAsyncFunction("searchNovels", query, page) ?: "[]"
        return try {
            json.decodeFromString<List<PluginNovel>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getNovelDetails(url: String): PluginNovelDetails {
        val jsonStr = callAsyncFunction("getNovelDetails", url) ?: "{}"
        return try {
            json.decodeFromString<PluginNovelDetails>(jsonStr)
        } catch (e: Exception) {
            PluginNovelDetails(name = "Unknown", url = url)
        }
    }
    
    override suspend fun getChapters(url: String): List<PluginChapter> {
        val jsonStr = callAsyncFunction("getChapters", url) ?: "[]"
        return try {
            json.decodeFromString<List<PluginChapter>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getChapterContent(url: String): String {
        return callAsyncFunction("getChapterContent", url) ?: ""
    }
    
    override fun getFilters(): Map<String, Any> = emptyMap()
    
    private fun callStringFunction(name: String): String? {
        return try {
            val script = "typeof $name === 'function' ? $name() : (typeof $name !== 'undefined' ? String($name) : null)"
            val result = engine.executeStringScript(script)
            if (result == "null" || result == "undefined") null else result
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun callAsyncFunction(name: String, vararg args: Any?): String? {
        return try {
            val argsJson = args.joinToString(", ") { arg ->
                when (arg) {
                    is String -> "\"${arg.replace("\\", "\\\\").replace("\"", "\\\"")}\""
                    is Number -> arg.toString()
                    null -> "null"
                    else -> arg.toString()
                }
            }
            engine.executeStringScript("JSON.stringify($name($argsJson))")
        } catch (e: Exception) {
            null
        }
    }
}
