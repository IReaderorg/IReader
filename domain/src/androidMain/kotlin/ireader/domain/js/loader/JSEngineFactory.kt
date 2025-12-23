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
    
    // Cached method references for performance
    private var cachedV8Class: Class<*>? = null
    private var cachedCreateRuntimeMethod: Method? = null
    private var cachedExecuteVoidScriptMethod: Method? = null
    private var cachedExecuteStringScriptMethod: Method? = null
    private var cachedReleaseMethod: Method? = null
    
    private val pluginManager: PluginManager by inject()
    
    fun isJ2V8Ready(): Boolean = j2v8Ready
    fun isJ2V8Loaded(): Boolean = j2v8Ready
    fun getJ2V8ClassLoader(): ClassLoader? = j2v8ClassLoader
    
    // Provide cached method references
    fun getV8Class(): Class<*>? = cachedV8Class
    fun getCreateRuntimeMethod(): Method? = cachedCreateRuntimeMethod
    fun getExecuteVoidScriptMethod(): Method? = cachedExecuteVoidScriptMethod
    fun getExecuteStringScriptMethod(): Method? = cachedExecuteStringScriptMethod
    fun getReleaseMethod(): Method? = cachedReleaseMethod
    
    @Synchronized
    fun tryInitializeJ2V8(): Boolean {
        if (j2v8Ready) return true
        
        val pluginClassLoader = PluginClassLoader.getClassLoader(J2V8_PLUGIN_ID)
        if (pluginClassLoader == null) {
            Log.info { "J2V8EngineHelper: Plugin ClassLoader not available yet" }
            return false
        }
        
        if (initAttempted) return false
        
        Log.info { "J2V8EngineHelper: Initializing J2V8..." }
        
        try {
            // Load and cache V8 class and methods
            val v8Class = pluginClassLoader.loadClass("com.eclipsesource.v8.V8")
            cachedV8Class = v8Class
            cachedCreateRuntimeMethod = v8Class.getMethod("createV8Runtime")
            cachedExecuteVoidScriptMethod = v8Class.getMethod("executeVoidScript", String::class.java)
            cachedExecuteStringScriptMethod = v8Class.getMethod("executeStringScript", String::class.java)
            cachedReleaseMethod = v8Class.getMethod("release")
            
            // Test creating a runtime
            val runtime = cachedCreateRuntimeMethod?.invoke(null)
            cachedReleaseMethod?.invoke(runtime)
            
            j2v8ClassLoader = pluginClassLoader
            j2v8Ready = true
            initAttempted = true
            Log.info { "J2V8EngineHelper: J2V8 initialized successfully!" }
            return true
            
        } catch (e: Exception) {
            Log.error { "J2V8EngineHelper: Failed to initialize: ${e.message}" }
            initAttempted = true
            return false
        }
    }
    
    fun reset() {
        initAttempted = false
        j2v8Ready = false
        j2v8ClassLoader = null
        cachedV8Class = null
        cachedCreateRuntimeMethod = null
        cachedExecuteVoidScriptMethod = null
        cachedExecuteStringScriptMethod = null
        cachedReleaseMethod = null
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
 * Factory for creating single-thread executors for V8 engines.
 * Each V8 runtime MUST be accessed from the same thread that created it.
 * Using a shared pool causes "Invalid V8 thread access" errors.
 */
private object V8ThreadFactory {
    private var threadCounter = 0L
    
    /**
     * Create a new single-thread dispatcher for a V8 engine.
     * Each engine gets its own dedicated thread to ensure thread safety.
     */
    fun createDispatcher(): kotlinx.coroutines.CoroutineDispatcher {
        val threadId = threadCounter++
        val executor = Executors.newSingleThreadExecutor { r ->
            Thread(r, "V8-Engine-$threadId").apply { isDaemon = true }
        }
        return executor.asCoroutineDispatcher()
    }
}

/**
 * Cached adapter code - parsed once, reused for all plugins.
 */
private object AdapterCodeCache {
    val code: String by lazy { generateAdapterCode() }
    
    private fun generateAdapterCode(): String = """
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

        // Intl polyfill - provides basic date/number formatting without ICU
        // This is needed because some JS engines may not have ICU support
        if (typeof Intl === 'undefined' || typeof Intl.DateTimeFormat === 'undefined') {
            globalThis.Intl = globalThis.Intl || {};
            
            // Month names for formatting
            var monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
                              'July', 'August', 'September', 'October', 'November', 'December'];
            var monthNamesShort = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                                   'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
            var dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
            var dayNamesShort = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
            
            globalThis.Intl.DateTimeFormat = function(locale, options) {
                this.locale = locale || 'en';
                this.options = options || {};
            };
            
            globalThis.Intl.DateTimeFormat.prototype.format = function(date) {
                if (!(date instanceof Date)) {
                    date = new Date(date);
                }
                if (isNaN(date.getTime())) {
                    return 'Invalid Date';
                }
                
                var opts = this.options;
                var parts = [];
                
                // Handle dateStyle shortcuts
                if (opts.dateStyle === 'full') {
                    return dayNames[date.getDay()] + ', ' + monthNames[date.getMonth()] + ' ' + 
                           date.getDate() + ', ' + date.getFullYear();
                } else if (opts.dateStyle === 'long') {
                    return monthNames[date.getMonth()] + ' ' + date.getDate() + ', ' + date.getFullYear();
                } else if (opts.dateStyle === 'medium') {
                    return monthNamesShort[date.getMonth()] + ' ' + date.getDate() + ', ' + date.getFullYear();
                } else if (opts.dateStyle === 'short') {
                    return (date.getMonth() + 1) + '/' + date.getDate() + '/' + date.getFullYear();
                }
                
                // Handle individual options
                if (opts.weekday === 'long') parts.push(dayNames[date.getDay()]);
                else if (opts.weekday === 'short') parts.push(dayNamesShort[date.getDay()]);
                
                if (opts.month === 'long') parts.push(monthNames[date.getMonth()]);
                else if (opts.month === 'short') parts.push(monthNamesShort[date.getMonth()]);
                else if (opts.month === 'numeric') parts.push(String(date.getMonth() + 1));
                else if (opts.month === '2-digit') parts.push(String(date.getMonth() + 1).padStart(2, '0'));
                
                if (opts.day === 'numeric') parts.push(String(date.getDate()));
                else if (opts.day === '2-digit') parts.push(String(date.getDate()).padStart(2, '0'));
                
                if (opts.year === 'numeric') parts.push(String(date.getFullYear()));
                else if (opts.year === '2-digit') parts.push(String(date.getFullYear()).slice(-2));
                
                if (parts.length === 0) {
                    // Default format: Month Day, Year
                    return monthNames[date.getMonth()] + ' ' + date.getDate() + ', ' + date.getFullYear();
                }
                
                return parts.join(' ');
            };
            
            globalThis.Intl.DateTimeFormat.prototype.resolvedOptions = function() {
                return {
                    locale: this.locale,
                    calendar: 'gregory',
                    numberingSystem: 'latn',
                    timeZone: 'UTC'
                };
            };
            
            // NumberFormat polyfill
            globalThis.Intl.NumberFormat = function(locale, options) {
                this.locale = locale || 'en';
                this.options = options || {};
            };
            
            globalThis.Intl.NumberFormat.prototype.format = function(num) {
                return String(num);
            };
        }

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
                        var text = typeof content === 'string' ? content : (content.text || "");
                        return text.replace(/[\n\r\t]/g, '');
                    });
                }
                return Promise.resolve("");
            };

            return wrapper;
        }
        globalThis.wrapPlugin = wrapPlugin;
    """.trimIndent()
}


/**
 * Optimized J2V8 engine implementation.
 * 
 * Performance optimizations:
 * 1. Uses shared thread pool instead of per-engine threads
 * 2. Caches method references in J2V8EngineHelper
 * 3. Caches adapter code (parsed once)
 * 4. Extracts metadata during load to avoid repeated V8 calls
 * 5. Uses faster promise polling (1ms instead of 10ms)
 */
private class J2V8ReflectionEngine(
    private val bridgeService: JSBridgeService
) : JSEngine {
    
    private var v8Runtime: Any? = null
    private var isEngineLoaded = false
    private val mutex = Mutex()
    
    // Each engine gets its own dedicated thread - V8 requires single-thread access
    private val v8Dispatcher = V8ThreadFactory.createDispatcher()
    
    // Use cached method references from helper
    private val executeVoidScriptMethod = J2V8EngineHelper.getExecuteVoidScriptMethod()
    private val executeStringScriptMethod = J2V8EngineHelper.getExecuteStringScriptMethod()
    private val createRuntimeMethod = J2V8EngineHelper.getCreateRuntimeMethod()
    private val releaseMethod = J2V8EngineHelper.getReleaseMethod()
    
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
                
                // Load cached adapter code
                executeVoidScriptMethod?.invoke(runtime, AdapterCodeCache.code)
                
                // Setup bridge for fetch
                setupBridge(runtime)
                
                // Initialize module system
                executeVoidScriptMethod?.invoke(runtime, "var exports = {}; var module = { exports: exports };")
                
                // Load the plugin code
                executeVoidScriptMethod?.invoke(runtime, jsCode)
                
                // Wrap the plugin and extract metadata in one call
                val metadataScript = """
                    globalThis.__wrappedPlugin = wrapPlugin(exports.default || exports);
                    JSON.stringify({
                        id: __wrappedPlugin.getId(),
                        name: __wrappedPlugin.getName(),
                        site: __wrappedPlugin.getSite(),
                        version: __wrappedPlugin.getVersion(),
                        lang: __wrappedPlugin.getLang(),
                        icon: __wrappedPlugin.getIcon()
                    });
                """.trimIndent()
                
                val metadataJson = executeStringScriptMethod?.invoke(runtime, metadataScript) as? String
                
                isEngineLoaded = true
                
                // Create wrapper with pre-extracted metadata and same dispatcher
                J2V8PluginWrapper(this@J2V8ReflectionEngine, pluginId, bridgeService, metadataJson, v8Dispatcher)
                
            } catch (e: Exception) {
                Log.error { "J2V8ReflectionEngine: Failed to load plugin: ${e.message}" }
                close()
                val cause = e.cause ?: e
                throw ireader.domain.js.engine.PluginLoadException("Failed to load plugin: $pluginId - ${cause.message}", cause)
            }
        }
    }
    
    private fun setupBridge(runtime: Any) {
        executeVoidScriptMethod?.invoke(runtime, """
            globalThis.fetch = function(url, options) {
                var requestUrl = String(url || '');
                return new Promise(function(resolve, reject) {
                    // Convert Headers object to plain object if needed
                    var headersObj = {};
                    if (options && options.headers) {
                        if (options.headers instanceof Headers) {
                            // Headers polyfill stores in .headers property
                            headersObj = options.headers.headers || {};
                        } else if (typeof options.headers === 'object') {
                            headersObj = options.headers;
                        }
                    }
                    
                    // Convert body to string if needed
                    var bodyStr = null;
                    if (options && options.body) {
                        if (typeof options.body === 'string') {
                            bodyStr = options.body;
                        } else if (options.body instanceof FormData) {
                            // FormData not fully supported, convert to URL encoded
                            bodyStr = '';
                        } else {
                            bodyStr = String(options.body);
                        }
                    }
                    
                    globalThis.__pendingFetch = {
                        url: requestUrl,
                        method: (options && options.method) || 'GET',
                        headers: headersObj,
                        body: bodyStr,
                        resolve: resolve,
                        reject: reject,
                        requestUrl: requestUrl
                    };
                    globalThis.__fetchReady = true;
                });
            };
            globalThis.fetchApi = globalThis.fetch;
        """.trimIndent())
    }
    
    fun evaluateStringScript(script: String): String? {
        return try {
            executeStringScriptMethod?.invoke(v8Runtime, script) as? String
        } catch (e: Exception) {
            null
        }
    }
    
    fun evaluateVoidScript(script: String) {
        try {
            executeVoidScriptMethod?.invoke(v8Runtime, script)
        } catch (e: Exception) {
            Log.warn { "J2V8: evaluateVoidScript error: ${e.cause?.message ?: e.message}" }
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
 * Optimized plugin wrapper with cached metadata.
 */
private class J2V8PluginWrapper(
    private val engine: J2V8ReflectionEngine,
    private val pluginId: String,
    private val bridgeService: JSBridgeService,
    metadataJson: String?,
    private val v8Dispatcher: kotlinx.coroutines.CoroutineDispatcher
) : LNReaderPlugin {

    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    
    // Cache metadata extracted during load
    private val cachedId: String
    private val cachedName: String
    private val cachedSite: String
    private val cachedVersion: String
    private val cachedLang: String
    private val cachedIcon: String
    
    init {
        // Parse metadata from JSON extracted during load
        val metadata = metadataJson?.let {
            try {
                json.parseToJsonElement(it).jsonObject
            } catch (e: Exception) { null }
        }
        
        cachedId = metadata?.get("id")?.jsonPrimitive?.content ?: pluginId
        cachedName = metadata?.get("name")?.jsonPrimitive?.content ?: "Unknown"
        cachedSite = metadata?.get("site")?.jsonPrimitive?.content ?: ""
        cachedVersion = metadata?.get("version")?.jsonPrimitive?.content ?: "1.0.0"
        cachedLang = metadata?.get("lang")?.jsonPrimitive?.content ?: "en"
        cachedIcon = metadata?.get("icon")?.jsonPrimitive?.content ?: ""
    }

    // Return cached values - no V8 calls needed
    override suspend fun getId(): String = cachedId
    override suspend fun getName(): String = cachedName
    override suspend fun getSite(): String = cachedSite
    override suspend fun getVersion(): String = cachedVersion
    override suspend fun getLang(): String = cachedLang
    override suspend fun getIcon(): String = cachedIcon

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
     * Optimized promise awaiting with faster polling.
     */
    private suspend fun awaitPromise(jsExpression: String): String {
        val promiseId = System.nanoTime()

        engine.evaluateVoidScript("""
            (async function() {
                try {
                    const result = await ($jsExpression);
                    globalThis.__pr_$promiseId = JSON.stringify(result);
                    globalThis.__ps_$promiseId = 1;
                } catch (e) {
                    globalThis.__pe_$promiseId = e.message || String(e);
                    globalThis.__ps_$promiseId = 2;
                }
            })();
        """.trimIndent())

        val startTime = System.currentTimeMillis()
        val timeout = 30000L

        while (System.currentTimeMillis() - startTime < timeout) {
            // Process any pending fetch requests
            processPendingFetch()

            val status = engine.evaluateStringScript("String(globalThis.__ps_$promiseId || 0)")

            when (status) {
                "1" -> {
                    val result = engine.evaluateStringScript("globalThis.__pr_$promiseId") ?: ""
                    engine.evaluateVoidScript("delete globalThis.__pr_$promiseId; delete globalThis.__ps_$promiseId;")
                    return result
                }
                "2" -> {
                    val error = engine.evaluateStringScript("globalThis.__pe_$promiseId") ?: "Unknown error"
                    engine.evaluateVoidScript("delete globalThis.__pe_$promiseId; delete globalThis.__ps_$promiseId;")
                    throw Exception("Promise rejected: $error")
                }
            }

            delay(1) // Faster polling
        }

        engine.evaluateVoidScript("delete globalThis.__pr_$promiseId; delete globalThis.__ps_$promiseId; delete globalThis.__pe_$promiseId;")
        throw Exception("Promise timeout after ${timeout}ms")
    }

    private suspend fun processPendingFetch() {
        try {
            val fetchReadyStr = engine.evaluateStringScript("String(globalThis.__fetchReady || false)")
            if (fetchReadyStr != "true") return

            engine.evaluateVoidScript("globalThis.__fetchReady = false;")

            val fetchJson = engine.evaluateStringScript("JSON.stringify(globalThis.__pendingFetch)") ?: return
            val fetchData = json.parseToJsonElement(fetchJson).jsonObject

            val url = fetchData["url"]?.jsonPrimitive?.content ?: return
            val requestUrl = fetchData["requestUrl"]?.jsonPrimitive?.content ?: url
            val method = fetchData["method"]?.jsonPrimitive?.content ?: "GET"
            
            // Extract headers
            val headersObj = fetchData["headers"]?.jsonObject
            val headers = headersObj?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap()
            
            // Extract body for POST requests
            val body = fetchData["body"]?.jsonPrimitive?.content

            val response = withContext(Dispatchers.IO) {
                bridgeService.fetch(url, FetchOptions(method = method, headers = headers, body = body))
            }

            // Store response and resolve promise
            val responseBodyVar = "__rb_${System.nanoTime()}"
            engine.evaluateVoidScript("globalThis.$responseBodyVar = ${Json.encodeToString(JsonPrimitive.serializer(), JsonPrimitive(response.text))};")

            // Include url property in response for redirect detection
            val responseUrl = response.url ?: url
            engine.evaluateVoidScript("""
                if (globalThis.__pendingFetch && globalThis.__pendingFetch.resolve) {
                    var bodyText = globalThis.$responseBodyVar;
                    delete globalThis.$responseBodyVar;
                    globalThis.__pendingFetch.resolve({
                        ok: ${response.status in 200..299},
                        status: ${response.status},
                        url: '${responseUrl.replace("'", "\\'")}',
                        text: function() { return Promise.resolve(bodyText); },
                        json: function() { return Promise.resolve(JSON.parse(bodyText)); }
                    });
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
            emptyList()
        }
    }
}
