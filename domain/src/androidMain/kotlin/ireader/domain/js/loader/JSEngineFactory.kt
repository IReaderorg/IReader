package ireader.domain.js.loader

import ireader.core.log.Log
import ireader.domain.js.bridge.FetchOptions
import ireader.domain.js.bridge.JSBridgeService
import ireader.domain.js.bridge.LNReaderPlugin
import ireader.domain.js.bridge.PluginChapter
import ireader.domain.js.bridge.PluginNovel
import ireader.domain.js.bridge.PluginNovelDetails
import ireader.domain.js.engine.NoJSEngineException
import ireader.domain.plugins.PluginClassLoader
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.reflect.Method
import java.util.concurrent.Executors

/**
 * Android implementation using J2V8 loaded from plugin.
 */
actual fun createEngine(bridgeService: JSBridgeService): JSEngine {
    if (J2V8EngineHelper.isJ2V8Ready()) {
        return J2V8ReflectionEngine(bridgeService)
    }
    
    if (J2V8EngineHelper.tryInitializeJ2V8()) {
        return J2V8ReflectionEngine(bridgeService)
    }
    
    return StubJSEngine()
}

/**
 * Helper object to manage J2V8 plugin integration.
 */
object J2V8EngineHelper : KoinComponent {
    private const val J2V8_PLUGIN_ID = "io.github.ireaderorg.plugins.j2v8-engine"
    
    private var j2v8Ready = false
    private var initAttempted = false
    private var j2v8ClassLoader: ClassLoader? = null
    
    private val pluginManager: PluginManager by inject()
    
    fun isJ2V8Ready(): Boolean = j2v8Ready
    fun isJ2V8Loaded(): Boolean = j2v8Ready
    fun getJ2V8ClassLoader(): ClassLoader? = j2v8ClassLoader
    
    @Synchronized
    fun tryInitializeJ2V8(): Boolean {
        Log.info { "J2V8EngineHelper: tryInitializeJ2V8 called, j2v8Ready=$j2v8Ready, initAttempted=$initAttempted" }
        
        if (j2v8Ready) return true
        
        val pluginClassLoader = PluginClassLoader.getClassLoader(J2V8_PLUGIN_ID)
        if (pluginClassLoader == null) {
            Log.info { "J2V8EngineHelper: Plugin ClassLoader not available yet" }
            return false
        }
        
        if (initAttempted) {
            Log.info { "J2V8EngineHelper: Already attempted initialization and failed" }
            return false
        }
        
        Log.info { "J2V8EngineHelper: *** FIRST INITIALIZATION ATTEMPT ***" }
        
        try {
            // Load V8 class - this triggers native library loading via DexClassLoader's native path
            val v8Class = pluginClassLoader.loadClass("com.eclipsesource.v8.V8")
            Log.info { "J2V8EngineHelper: V8 class loaded: ${v8Class.name}" }
            
            // Test creating a runtime
            val createMethod = v8Class.getMethod("createV8Runtime")
            val runtime = createMethod.invoke(null)
            Log.info { "J2V8EngineHelper: V8 runtime created successfully!" }
            
            val releaseMethod = v8Class.getMethod("release")
            releaseMethod.invoke(runtime)
            
            j2v8ClassLoader = pluginClassLoader
            j2v8Ready = true
            initAttempted = true
            Log.info { "J2V8EngineHelper: J2V8 initialized successfully!" }
            return true
            
        } catch (e: Exception) {
            Log.error { "J2V8EngineHelper: Failed to initialize: ${e.javaClass.name}: ${e.message}" }
            e.printStackTrace()
            initAttempted = true
            return false
        }
    }
    
    fun reset() {
        initAttempted = false
        j2v8Ready = false
        j2v8ClassLoader = null
    }
    
    fun isJ2V8PluginAvailable(): Boolean {
        if (PluginClassLoader.getClassLoader(J2V8_PLUGIN_ID) != null) return true
        val plugins = pluginManager.pluginsFlow.value
        val j2v8Plugin = plugins.find { it.id == J2V8_PLUGIN_ID }
        return j2v8Plugin != null && j2v8Plugin.status == PluginStatus.ENABLED
    }
    
    fun onJ2V8PluginAvailable() {
        if (!j2v8Ready) {
            Log.info { "J2V8EngineHelper: Plugin became available, resetting" }
            reset()
        }
    }
}

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
 * J2V8 engine implementation using reflection.
 * Mirrors the original AndroidJSEngine implementation.
 */
private class J2V8ReflectionEngine(
    private val bridgeService: JSBridgeService
) : JSEngine {
    
    private var v8Runtime: Any? = null
    private var isEngineLoaded = false
    private val mutex = Mutex()
    
    // V8 method references
    private var v8Class: Class<*>? = null
    private var createRuntimeMethod: Method? = null
    private var executeVoidScriptMethod: Method? = null
    private var executeStringScriptMethod: Method? = null
    private var executeObjectScriptMethod: Method? = null
    private var releaseMethod: Method? = null
    
    // V8 requires single-threaded access
    private val v8Executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "V8-Engine-${System.identityHashCode(this)}").apply { isDaemon = true }
    }
    private val v8Dispatcher = v8Executor.asCoroutineDispatcher()
    
    init {
        try {
            val classLoader = J2V8EngineHelper.getJ2V8ClassLoader()
                ?: throw IllegalStateException("J2V8 ClassLoader not available")
            
            v8Class = classLoader.loadClass("com.eclipsesource.v8.V8")
            createRuntimeMethod = v8Class?.getMethod("createV8Runtime")
            executeVoidScriptMethod = v8Class?.getMethod("executeVoidScript", String::class.java)
            executeStringScriptMethod = v8Class?.getMethod("executeStringScript", String::class.java)
            executeObjectScriptMethod = v8Class?.getMethod("executeObjectScript", String::class.java)
            releaseMethod = v8Class?.getMethod("release")
        } catch (e: Exception) {
            Log.error { "J2V8ReflectionEngine: Failed to initialize: ${e.message}" }
        }
    }
    
    override suspend fun loadPlugin(jsCode: String, pluginId: String): LNReaderPlugin = withContext(v8Dispatcher) {
        mutex.withLock {
            try {
                // Close existing engine if any
                v8Runtime?.let { 
                    try { releaseMethod?.invoke(it) } catch (e: Exception) {}
                }
                
                // Create new V8 runtime
                val runtime = createRuntimeMethod?.invoke(null)
                    ?: throw ireader.domain.js.engine.PluginLoadException("Failed to create V8 runtime")
                v8Runtime = runtime
                
                // Load adapter code with polyfills
                Log.debug { "J2V8ReflectionEngine: Loading adapter code" }
                executeVoidScriptMethod?.invoke(runtime, getAdapterCode())
                
                // Setup bridge for fetch
                setupBridge(runtime, bridgeService, pluginId)
                
                // Initialize module system
                executeVoidScriptMethod?.invoke(runtime, "if (typeof exports === 'undefined') { var exports = {}; }")
                executeVoidScriptMethod?.invoke(runtime, "if (typeof module === 'undefined') { var module = { exports: exports }; }")
                
                // Load the plugin code
                Log.debug { "J2V8ReflectionEngine: Loading plugin code (${jsCode.length} chars)" }
                executeVoidScriptMethod?.invoke(runtime, jsCode)
                
                // Wrap the plugin
                executeVoidScriptMethod?.invoke(runtime, "globalThis.__wrappedPlugin = wrapPlugin(exports.default || exports);")
                
                isEngineLoaded = true
                
                // Create wrapper
                J2V8PluginWrapper(this@J2V8ReflectionEngine, pluginId, bridgeService, v8Dispatcher)
                
            } catch (e: Exception) {
                Log.error { "J2V8ReflectionEngine: Failed to load plugin: ${e.message}" }
                close()
                val cause = e.cause ?: e
                throw ireader.domain.js.engine.PluginLoadException("Failed to load plugin: $pluginId - ${cause.message}", cause)
            }
        }
    }
    
    private fun setupBridge(runtime: Any, bridge: JSBridgeService, pluginId: String) {
        executeVoidScriptMethod?.invoke(runtime, """
            globalThis.fetch = function(url, options) {
                return new Promise((resolve, reject) => {
                    globalThis.__pendingFetch = {
                        url: String(url || ''),
                        method: (options && options.method) || 'GET',
                        headers: (options && options.headers) || {},
                        body: (options && options.body) || null,
                        resolve: resolve,
                        reject: reject
                    };
                    globalThis.__fetchReady = true;
                });
            };
            globalThis.fetchApi = globalThis.fetch;
        """.trimIndent())
    }
    
    fun evaluateScript(script: String): Any? {
        return try {
            // Use executeStringScript and parse the result
            // This is more reliable than executeObjectScript which returns V8Object
            val wrappedScript = "JSON.stringify($script)"
            val result = executeStringScriptMethod?.invoke(v8Runtime, wrappedScript) as? String
            if (result == null || result == "null" || result == "undefined") {
                null
            } else if (result.startsWith("\"") && result.endsWith("\"")) {
                // It's a JSON string, parse it
                result.substring(1, result.length - 1)
            } else if (result == "true") {
                true
            } else if (result == "false") {
                false
            } else {
                result
            }
        } catch (e: Exception) {
            Log.warn { "J2V8ReflectionEngine: evaluateScript error: ${e.cause?.message ?: e.message}" }
            null
        }
    }
    
    fun evaluateStringScript(script: String): String? {
        return try {
            executeStringScriptMethod?.invoke(v8Runtime, script) as? String
        } catch (e: Exception) {
            Log.warn { "J2V8ReflectionEngine: evaluateStringScript error: ${e.cause?.message ?: e.message}" }
            null
        }
    }
    
    fun evaluateVoidScript(script: String) {
        try {
            executeVoidScriptMethod?.invoke(v8Runtime, script)
        } catch (e: Exception) {
            Log.warn { "J2V8ReflectionEngine: evaluateVoidScript error: ${e.cause?.message ?: e.message}" }
        }
    }
    
    override fun close() {
        try {
            v8Executor.submit {
                try { v8Runtime?.let { releaseMethod?.invoke(it) } } catch (e: Exception) {}
                v8Runtime = null
            }.get()
        } catch (e: Exception) {}
        v8Executor.shutdown()
        isEngineLoaded = false
    }
    
    override fun isLoaded(): Boolean = isEngineLoaded

    
    private fun getAdapterCode(): String {
        return """
            // Console polyfill
            (function() {
                var console = {};
                console.log = function() { globalThis.__consoleLog = Array.prototype.slice.call(arguments).join(' '); };
                console.error = function() { globalThis.__consoleError = Array.prototype.slice.call(arguments).join(' '); };
                console.warn = function() { };
                console.info = function() { };
                console.debug = function() { };
                globalThis.console = console;
            })();

            // URL polyfill
            if (typeof URL === 'undefined') {
                globalThis.URL = function(url, base) {
                    if (url === null || url === undefined) throw new Error('Invalid URL');
                    url = String(url);
                    var fullUrl = url;
                    if (base && !url.match(/^https?:\/\//)) {
                        base = String(base);
                        if (url.indexOf('/') === 0) {
                            var baseMatch = base.match(/^(https?:\/\/[^\/]+)/);
                            fullUrl = baseMatch ? baseMatch[1] + url : url;
                        } else {
                            fullUrl = base.replace(/\/[^\/]*${'$'}/, '/') + url;
                        }
                    }
                    var match = fullUrl.match(/^(https?):\/\/([^\/\?#]+)(\/[^\?#]*)?(\?[^#]*)?(#.*)?${'$'}/);
                    if (!match) throw new Error('Invalid URL: ' + fullUrl);
                    this.protocol = (match[1] || 'http') + ':';
                    this.host = match[2] || '';
                    this.hostname = (match[2] || '').split(':')[0];
                    this.port = (match[2] || '').split(':')[1] || '';
                    this.pathname = match[3] || '/';
                    this.search = match[4] || '';
                    this.hash = match[5] || '';
                    this.href = fullUrl;
                    this.origin = this.protocol + '//' + this.host;
                    this.toString = function() { return this.href; };
                };
            }

            if (typeof URLSearchParams === 'undefined') {
                globalThis.URLSearchParams = function(init) {
                    this.params = {};
                    if (typeof init === 'string') {
                        var query = init.indexOf('?') === 0 ? init.substring(1) : init;
                        if (query) {
                            var self = this;
                            query.split('&').forEach(function(pair) {
                                var parts = pair.split('=');
                                var key = decodeURIComponent(parts[0]);
                                var value = parts[1] ? decodeURIComponent(parts[1]) : '';
                                if (!self.params[key]) self.params[key] = [];
                                self.params[key].push(value);
                            });
                        }
                    }
                    this.get = function(key) { return this.params[key] ? this.params[key][0] : null; };
                    this.toString = function() {
                        var parts = [];
                        for (var key in this.params) {
                            if (this.params.hasOwnProperty(key)) {
                                var self = this;
                                this.params[key].forEach(function(value) {
                                    parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(value));
                                });
                            }
                        }
                        return parts.join('&');
                    };
                };
            }

            // Headers polyfill
            if (typeof Headers === 'undefined') {
                globalThis.Headers = function(init) {
                    this.headers = {};
                    if (init && typeof init === 'object') {
                        for (var key in init) {
                            if (init.hasOwnProperty(key)) {
                                this.headers[key.toLowerCase()] = String(init[key]);
                            }
                        }
                    }
                    this.get = function(name) { return this.headers[name.toLowerCase()] || null; };
                    this.set = function(name, value) { this.headers[name.toLowerCase()] = String(value); };
                };
            }

            // TextEncoder/TextDecoder polyfills
            if (typeof TextEncoder === 'undefined') {
                globalThis.TextEncoder = function() {
                    this.encode = function(str) {
                        var utf8 = [];
                        for (var i = 0; i < str.length; i++) {
                            var c = str.charCodeAt(i);
                            if (c < 0x80) utf8.push(c);
                            else if (c < 0x800) utf8.push(0xc0 | (c >> 6), 0x80 | (c & 0x3f));
                            else utf8.push(0xe0 | (c >> 12), 0x80 | ((c >> 6) & 0x3f), 0x80 | (c & 0x3f));
                        }
                        return new Uint8Array(utf8);
                    };
                };
            }

            if (typeof TextDecoder === 'undefined') {
                globalThis.TextDecoder = function() {
                    this.decode = function(bytes) {
                        var str = '';
                        for (var i = 0; i < bytes.length; i++) {
                            str += String.fromCharCode(bytes[i]);
                        }
                        return str;
                    };
                };
            }

            // setTimeout/setInterval stubs
            globalThis.setTimeout = function(callback, delay) { callback(); return 0; };
            globalThis.setInterval = function(callback, delay) { return 0; };
            globalThis.clearTimeout = function(id) { };
            globalThis.clearInterval = function(id) { };

            // Wrapper function for plugins
            function wrapPlugin(plugin) {
                var wrapper = {};
                wrapper.getId = function() { return plugin.id || "unknown"; };
                wrapper.getName = function() { return plugin.name || "Unknown Plugin"; };
                wrapper.getSite = function() { return plugin.site || ""; };
                wrapper.getVersion = function() { return plugin.version || "1.0.0"; };
                wrapper.getLang = function() { return plugin.lang || "en"; };
                wrapper.getIcon = function() { return plugin.icon || ""; };

                wrapper.searchNovels = function(query, page) {
                    if (typeof plugin.searchNovels === 'function') {
                        return Promise.resolve(plugin.searchNovels(query, page)).then(function(results) {
                            if (!Array.isArray(results)) return [];
                            return results.map(function(r) {
                                return { name: r.name || r.title || "", url: r.url || r.path || "", cover: r.cover || r.image || "" };
                            });
                        });
                    }
                    return Promise.resolve([]);
                };

                wrapper.popularNovels = function(page) {
                    if (typeof plugin.popularNovels === 'function') {
                        var result;
                        if (plugin.popularNovels.length <= 1) {
                            result = plugin.popularNovels(page);
                        } else {
                            result = plugin.popularNovels(page, { showLatestNovels: false, filters: plugin.filters || {} });
                        }
                        return Promise.resolve(result).then(function(results) {
                            if (!Array.isArray(results)) return [];
                            return results.map(function(r) {
                                return { name: r.name || r.title || "", url: r.url || r.path || "", cover: r.cover || r.image || "" };
                            });
                        });
                    }
                    return Promise.resolve([]);
                };

                wrapper.latestNovels = function(page) {
                    if (typeof plugin.latestNovels === 'function') {
                        var result;
                        if (plugin.latestNovels.length <= 1) {
                            result = plugin.latestNovels(page);
                        } else {
                            result = plugin.latestNovels(page, { showLatestNovels: true, filters: plugin.filters || {} });
                        }
                        return Promise.resolve(result).then(function(results) {
                            if (!Array.isArray(results)) return [];
                            return results.map(function(r) {
                                return { name: r.name || r.title || "", url: r.url || r.path || "", cover: r.cover || r.image || "" };
                            });
                        });
                    }
                    return wrapper.popularNovels(page);
                };

                wrapper.getNovelDetails = function(url) {
                    if (typeof plugin.parseNovel === 'function') {
                        return Promise.resolve(plugin.parseNovel(url)).then(function(d) {
                            return {
                                name: d.name || d.title || "",
                                url: d.url || d.path || url,
                                cover: d.cover || d.image || "",
                                author: d.author || null,
                                description: d.description || d.summary || null,
                                genres: Array.isArray(d.genres) ? d.genres : [],
                                status: d.status || null
                            };
                        });
                    }
                    return Promise.resolve({ name: "", url: url, cover: "", author: null, description: null, genres: [], status: null });
                };

                wrapper.getChapters = function(url) {
                    if (typeof plugin.parseNovel === 'function') {
                        return Promise.resolve(plugin.parseNovel(url)).then(function(novel) {
                            if (novel && Array.isArray(novel.chapters)) {
                                return novel.chapters.map(function(c) {
                                    return { name: c.name || c.title || "", url: c.url || c.path || "", releaseTime: c.releaseTime || c.date || null };
                                });
                            }
                            return [];
                        });
                    }
                    return Promise.resolve([]);
                };

                wrapper.getChapterContent = function(url) {
                    if (typeof plugin.parseChapter === 'function') {
                        return Promise.resolve(plugin.parseChapter(url)).then(function(content) {
                            return typeof content === 'string' ? content : (content.text || "");
                        });
                    }
                    return Promise.resolve("");
                };

                return wrapper;
            }
            globalThis.wrapPlugin = wrapPlugin;
        """.trimIndent()
    }
}


/**
 * Wrapper that adapts J2V8 to LNReaderPlugin interface.
 */
private class J2V8PluginWrapper(
    private val engine: J2V8ReflectionEngine,
    private val pluginId: String,
    private val bridgeService: JSBridgeService,
    private val v8Dispatcher: kotlinx.coroutines.CoroutineDispatcher
) : LNReaderPlugin {

    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    override suspend fun getId(): String = withContext(v8Dispatcher) {
        mutex.withLock {
            engine.evaluateScript("__wrappedPlugin.getId()") as? String ?: pluginId
        }
    }

    override suspend fun getName(): String = withContext(v8Dispatcher) {
        mutex.withLock {
            engine.evaluateScript("__wrappedPlugin.getName()") as? String ?: "Unknown"
        }
    }

    override suspend fun getSite(): String = withContext(v8Dispatcher) {
        mutex.withLock {
            engine.evaluateScript("__wrappedPlugin.getSite()") as? String ?: ""
        }
    }

    override suspend fun getVersion(): String = withContext(v8Dispatcher) {
        mutex.withLock {
            engine.evaluateScript("__wrappedPlugin.getVersion()") as? String ?: "1.0.0"
        }
    }

    override suspend fun getLang(): String = withContext(v8Dispatcher) {
        mutex.withLock {
            engine.evaluateScript("__wrappedPlugin.getLang()") as? String ?: "en"
        }
    }

    override suspend fun getIcon(): String = withContext(v8Dispatcher) {
        mutex.withLock {
            engine.evaluateScript("__wrappedPlugin.getIcon()") as? String ?: ""
        }
    }

    override suspend fun searchNovels(query: String, page: Int): List<PluginNovel> = withContext(v8Dispatcher) {
        mutex.withLock {
            try {
                val resultJson = awaitPromise("__wrappedPlugin.searchNovels('${query.replace("'", "\\'")}', $page)")
                parseNovelList(resultJson)
            } catch (e: Exception) {
                Log.error { "J2V8PluginWrapper: Error in searchNovels: ${e.message}" }
                emptyList()
            }
        }
    }

    override suspend fun popularNovels(page: Int, filters: Map<String, Any>): List<PluginNovel> = withContext(v8Dispatcher) {
        mutex.withLock {
            try {
                val resultJson = awaitPromise("__wrappedPlugin.popularNovels($page)")
                parseNovelList(resultJson)
            } catch (e: Exception) {
                Log.error { "J2V8PluginWrapper: Error in popularNovels: ${e.message}" }
                emptyList()
            }
        }
    }

    override suspend fun latestNovels(page: Int): List<PluginNovel> = withContext(v8Dispatcher) {
        mutex.withLock {
            try {
                val resultJson = awaitPromise("__wrappedPlugin.latestNovels($page)")
                parseNovelList(resultJson)
            } catch (e: Exception) {
                Log.error { "J2V8PluginWrapper: Error in latestNovels: ${e.message}" }
                emptyList()
            }
        }
    }

    override suspend fun getNovelDetails(url: String): PluginNovelDetails = withContext(v8Dispatcher) {
        mutex.withLock {
            try {
                val resultJson = awaitPromise("__wrappedPlugin.getNovelDetails('${url.replace("'", "\\'")}')")
                parseNovelDetails(resultJson)
            } catch (e: Exception) {
                Log.error { "J2V8PluginWrapper: Error in getNovelDetails: ${e.message}" }
                PluginNovelDetails("", url, "", null, null, emptyList(), null)
            }
        }
    }

    override suspend fun getChapters(url: String): List<PluginChapter> = withContext(v8Dispatcher) {
        mutex.withLock {
            try {
                val resultJson = awaitPromise("__wrappedPlugin.getChapters('${url.replace("'", "\\'")}')")
                parseChapterList(resultJson)
            } catch (e: Exception) {
                Log.error { "J2V8PluginWrapper: Error in getChapters: ${e.message}" }
                emptyList()
            }
        }
    }

    override suspend fun getChapterContent(url: String): String = withContext(v8Dispatcher) {
        mutex.withLock {
            try {
                awaitPromise("__wrappedPlugin.getChapterContent('${url.replace("'", "\\'")}')")
            } catch (e: Exception) {
                Log.error { "J2V8PluginWrapper: Error in getChapterContent: ${e.message}" }
                ""
            }
        }
    }

    override fun getFilters(): Map<String, Any> = emptyMap()

    /**
     * Wait for a JavaScript Promise to resolve.
     */
    private suspend fun awaitPromise(jsExpression: String): String {
        val promiseId = System.currentTimeMillis()

        engine.evaluateVoidScript("""
            (async function() {
                try {
                    const result = await ($jsExpression);
                    globalThis.__promiseResult_$promiseId = JSON.stringify(result);
                    globalThis.__promiseStatus_$promiseId = 'resolved';
                } catch (e) {
                    globalThis.__promiseError_$promiseId = e.message || String(e);
                    globalThis.__promiseStatus_$promiseId = 'rejected';
                }
            })();
        """.trimIndent())

        val startTime = System.currentTimeMillis()
        val timeout = 30000L

        while (System.currentTimeMillis() - startTime < timeout) {
            // Process any pending fetch requests
            processPendingFetch()

            val status = engine.evaluateStringScript("globalThis.__promiseStatus_$promiseId")

            when (status) {
                "resolved" -> {
                    // Get the raw result without double-stringifying
                    val result = engine.evaluateStringScript("globalThis.__promiseResult_$promiseId") ?: ""
                    engine.evaluateVoidScript("delete globalThis.__promiseResult_$promiseId; delete globalThis.__promiseStatus_$promiseId;")
                    return result
                }
                "rejected" -> {
                    val error = engine.evaluateStringScript("globalThis.__promiseError_$promiseId") ?: "Unknown error"
                    engine.evaluateVoidScript("delete globalThis.__promiseError_$promiseId; delete globalThis.__promiseStatus_$promiseId;")
                    throw Exception("Promise rejected: $error")
                }
            }

            delay(10)
        }

        engine.evaluateVoidScript("delete globalThis.__promiseResult_$promiseId; delete globalThis.__promiseStatus_$promiseId; delete globalThis.__promiseError_$promiseId;")
        throw Exception("Promise timeout after ${timeout}ms")
    }

    /**
     * Process pending fetch requests.
     */
    private suspend fun processPendingFetch() {
        try {
            val fetchReadyStr = engine.evaluateStringScript("String(globalThis.__fetchReady)")
            if (fetchReadyStr != "true") return

            engine.evaluateVoidScript("globalThis.__fetchReady = false;")

            val fetchJson = engine.evaluateStringScript("JSON.stringify(globalThis.__pendingFetch)") ?: return
            val fetchData = json.parseToJsonElement(fetchJson).jsonObject

            val url = fetchData["url"]?.jsonPrimitive?.content ?: return
            val method = fetchData["method"]?.jsonPrimitive?.content ?: "GET"

            Log.debug { "J2V8PluginWrapper: Processing fetch: $method $url" }

            val response = withContext(Dispatchers.IO) {
                bridgeService.fetch(url, FetchOptions(method = method))
            }

            // Store response body
            val responseBodyVar = "__responseBody_${System.currentTimeMillis()}"
            engine.evaluateVoidScript("globalThis.$responseBodyVar = ${Json.encodeToString(JsonPrimitive.serializer(), JsonPrimitive(response.text))};")

            // Resolve the Promise
            engine.evaluateVoidScript("""
                if (globalThis.__pendingFetch && globalThis.__pendingFetch.resolve) {
                    const bodyText = globalThis.$responseBodyVar;
                    delete globalThis.$responseBodyVar;
                    const response = {
                        ok: ${response.status in 200..299},
                        status: ${response.status},
                        statusText: "${response.statusText.replace("\"", "\\\"")}",
                        url: "${url.replace("\"", "\\\"")}",
                        text: function() { return Promise.resolve(bodyText); },
                        json: function() { return Promise.resolve(JSON.parse(bodyText)); }
                    };
                    globalThis.__pendingFetch.resolve(response);
                    delete globalThis.__pendingFetch;
                }
            """.trimIndent())

        } catch (e: Exception) {
            Log.error { "J2V8PluginWrapper: Error processing fetch: ${e.message}" }
        }
    }

    private fun parseNovelList(jsonStr: String): List<PluginNovel> {
        return try {
            val array = json.parseToJsonElement(jsonStr).jsonArray
            array.map { element ->
                val obj = element.jsonObject
                PluginNovel(
                    name = obj["name"]?.jsonPrimitive?.content ?: "",
                    url = obj["url"]?.jsonPrimitive?.content ?: "",
                    cover = obj["cover"]?.jsonPrimitive?.content ?: ""
                )
            }
        } catch (e: Exception) {
            Log.error { "J2V8PluginWrapper: Error parsing novel list: ${e.message}" }
            emptyList()
        }
    }

    private fun parseNovelDetails(jsonStr: String): PluginNovelDetails {
        return try {
            val obj = json.parseToJsonElement(jsonStr).jsonObject
            PluginNovelDetails(
                name = obj["name"]?.jsonPrimitive?.content ?: "",
                url = obj["url"]?.jsonPrimitive?.content ?: "",
                cover = obj["cover"]?.jsonPrimitive?.content ?: "",
                author = obj["author"]?.jsonPrimitive?.content,
                description = obj["description"]?.jsonPrimitive?.content,
                genres = obj["genres"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                status = obj["status"]?.jsonPrimitive?.content
            )
        } catch (e: Exception) {
            Log.error { "J2V8PluginWrapper: Error parsing novel details: ${e.message}" }
            PluginNovelDetails("", "", "", null, null, emptyList(), null)
        }
    }

    private fun parseChapterList(jsonStr: String): List<PluginChapter> {
        return try {
            val array = json.parseToJsonElement(jsonStr).jsonArray
            array.map { element ->
                val obj = element.jsonObject
                PluginChapter(
                    name = obj["name"]?.jsonPrimitive?.content ?: "",
                    url = obj["url"]?.jsonPrimitive?.content ?: "",
                    releaseTime = obj["releaseTime"]?.jsonPrimitive?.content
                )
            }
        } catch (e: Exception) {
            Log.error { "J2V8PluginWrapper: Error parsing chapter list: ${e.message}" }
            emptyList()
        }
    }
}
