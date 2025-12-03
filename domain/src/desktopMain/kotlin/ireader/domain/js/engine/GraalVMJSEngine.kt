package ireader.domain.js.engine

import ireader.core.log.Log
import ireader.domain.js.bridge.FetchOptions
import ireader.domain.js.bridge.JSBridgeService
import ireader.domain.js.bridge.LNReaderPlugin
import ireader.domain.js.bridge.PluginChapter
import ireader.domain.js.bridge.PluginNovel
import ireader.domain.js.bridge.PluginNovelDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value
import org.graalvm.polyglot.proxy.ProxyExecutable
import org.graalvm.polyglot.proxy.ProxyObject


/**
 * GraalVM-based JavaScript engine for desktop.
 * Executes LNReader plugins using GraalVM's JavaScript engine.
 */
class GraalVMJSEngine(
    private val bridgeService: JSBridgeService? = null
) {
    
    private var context: Context? = null
    private val contextLock = Any()
    
    // Single-threaded dispatcher for GraalVM context access
    // GraalVM contexts are not thread-safe, so all operations must happen on the same thread
    private val graalvmThread = java.util.concurrent.Executors.newSingleThreadExecutor { r ->
        Thread(r, "GraalVM-Thread").apply {
            isDaemon = true
        }
    }.asCoroutineDispatcher()
    
    /**
     * Load a plugin from JavaScript code.
     */
    suspend fun loadPlugin(jsCode: String, pluginId: String): LNReaderPlugin = withContext(graalvmThread) {
        try {
            // Close existing context if any
            context?.close()
            
            // Create GraalVM context with JavaScript on the dedicated GraalVM thread
            // Create without explicit thread restrictions to allow coroutine access
            val newContext = Context.newBuilder("js")
                .allowAllAccess(true)
                .allowCreateThread(true)
                .allowExperimentalOptions(true)
                .allowPolyglotAccess(org.graalvm.polyglot.PolyglotAccess.ALL)
                .option("engine.WarnInterpreterOnly", "false")
                .option("js.shared-array-buffer", "true")
                .build()
            
            // Provide bridge service to JavaScript if available
            if (bridgeService != null) {
                val bridgeProxy = createBridgeProxy(bridgeService)
                newContext.getBindings("js").putMember("bridge", bridgeProxy)
            }
            
            // Provide cheerio API
            val cheerioApi = ireader.domain.js.library.JSCheerioApi(pluginId)
            val cheerioProxy = createCheerioProxy(cheerioApi)
            newContext.getBindings("js").putMember("__nativeCheerio", cheerioProxy)

            newContext.getBindings("js").putMember("cheerio", cheerioProxy)
            
            // Setup console that logs to Kotlin
            newContext.eval("js", """
                globalThis.console = {
                    log: function(...args) { 
                        java.lang.System.out.println('[JS] ' + args.join(' ')); 
                    },
                    error: function(...args) { 
                        java.lang.System.err.println('[JS ERROR] ' + args.join(' ')); 
                    },
                    warn: function(...args) { 
                        java.lang.System.out.println('[JS WARN] ' + args.join(' ')); 
                    },
                    info: function(...args) { 
                        java.lang.System.out.println('[JS INFO] ' + args.join(' ')); 
                    },
                    debug: function(...args) { 
                        java.lang.System.out.println('[JS DEBUG] ' + args.join(' ')); 
                    }
                };
            """.trimIndent())
            
            // Load the adapter code first
            val adapterCode = getAdapterCode()
            newContext.eval("js", adapterCode)
            
            // Initialize exports object if it doesn't exist
            newContext.eval("js", "if (typeof exports === 'undefined') { var exports = {}; }")
            newContext.eval("js", "if (typeof module === 'undefined') { var module = { exports: exports }; }")
            
            // Load the plugin code
            try {
                newContext.eval("js", jsCode)
            } catch (e: org.graalvm.polyglot.PolyglotException) {
                // Provide better error message for common issues
                if (e.message?.contains("404") == true || e.message?.contains("Not Found") == true) {
                    throw PluginLoadException("Plugin file contains an HTTP error response instead of JavaScript. The file may be corrupted or incorrectly downloaded. Please re-download the plugin.", e)
                }
                throw PluginLoadException("JavaScript syntax error in plugin: ${e.message}", e)
            }
            
            // Get the plugin object and wrap it
            val wrapPluginFunc = newContext.getBindings("js").getMember("wrapPlugin")
            if (wrapPluginFunc == null || wrapPluginFunc.isNull) {
                throw PluginLoadException("wrapPlugin function not found - adapter may have failed to load")
            }
            
            val pluginExports = newContext.getBindings("js").getMember("exports")
            val defaultExport = pluginExports?.getMember("default") ?: pluginExports
            
            if (defaultExport == null || defaultExport.isNull) {
                throw PluginLoadException("Plugin does not export a default object. exports=${pluginExports != null}")
            }
            
            val wrappedPlugin = wrapPluginFunc.execute(defaultExport)
            
            context = newContext
            
            // Extract filters during plugin loading (while on GraalVM thread)
            val filters = try {
                val filtersValue = newContext.eval("js", """
                    (function() {
                        var filters = null;
                        
                        if (typeof exports !== 'undefined' && exports.default && exports.default.filters) {
                            filters = exports.default.filters;
                        }
                        else if (typeof exports !== 'undefined' && exports.filters) {
                            filters = exports.filters;
                        }
                        else if (typeof module !== 'undefined' && module.exports && module.exports.filters) {
                            filters = module.exports.filters;
                        }
                        else if (typeof filters !== 'undefined' && filters !== null) {
                            filters = globalThis.filters;
                        }
                        
                        return filters;
                    })();
                """.trimIndent())
                
                if (filtersValue != null && !filtersValue.isNull) {
                    convertValueToMap(filtersValue)
                } else {
                    emptyMap()
                }
            } catch (e: Exception) {
                Log.warn("GraalVMJSEngine: Failed to extract filters: ${e.message}")
                emptyMap()
            }
            
            // Create Kotlin wrapper with context reference, dedicated thread, and pre-loaded filters
            GraalVMPluginWrapper(wrappedPlugin, newContext, graalvmThread, filters)
            
        } catch (e: Exception) {
            Log.error("GraalVMJSEngine: Failed to load plugin: ${e.message}", e)
            e.printStackTrace()
            throw PluginLoadException("Failed to load plugin: $pluginId - ${e.javaClass.simpleName}: ${e.message}", e)
        }
    }
    
    /**
     * Create a proxy object for the bridge service that JavaScript can call.
     */
    private fun createBridgeProxy(bridge: JSBridgeService): ProxyObject {
        return ProxyObject.fromMap(mapOf(
            "fetch" to ProxyExecutable { args ->
                val url = args[0].asString()
                val options = if (args.size > 1 && !args[1].isNull) {
                    val opts = args[1]
                    FetchOptions(
                        method = opts.getMember("method")?.asString() ?: "GET",
                        headers = opts.getMember("headers")?.let { h ->
                            val map = mutableMapOf<String, String>()
                            h.memberKeys.forEach { key ->
                                map[key] = h.getMember(key).asString()
                            }
                            map
                        } ?: emptyMap(),
                        body = opts.getMember("body")?.asString()
                    )
                } else {
                    FetchOptions()
                }
                
                // Call bridge service (this is a suspend function, need to handle)
                kotlinx.coroutines.runBlocking {
                    val response = bridge.fetch(url, options)
                    // Convert to JS object
                    mapOf(
                        "ok" to response.ok,
                        "status" to response.status,
                        "statusText" to response.statusText,
                        "headers" to response.headers,
                        "text" to response.text
                    )
                }
            }
        ))
    }
    
    /**
     * Create a proxy object for cheerio that JavaScript can call.
     */
    private fun createCheerioProxy(cheerio: ireader.domain.js.library.JSCheerioApi): ProxyObject {
        return ProxyObject.fromMap(mapOf(
            "load" to ProxyExecutable { args ->
                val html = args[0].asString()
                val cheerioObj = cheerio.load(html)
                createCheerioObjectProxy(cheerioObj)
            }
        ))
    }
    
    /**
     * Create a proxy for a CheerioObject that can be called as a function.
     */
    private fun createCheerioObjectProxy(obj: ireader.domain.js.library.JSCheerioApi.CheerioObject): ProxyObject {
        val proxy = object : ProxyObject, ProxyExecutable {
            // ProxyExecutable: allows calling as a function
            override fun execute(vararg arguments: Value?): Any {
                val selector = arguments.getOrNull(0)?.asString() ?: ""
                return createCheerioObjectProxy(obj(selector))
            }
            
            // ProxyObject: allows accessing as an object
            override fun getMember(key: String?): Any? {
                return when (key) {
                    "find" -> ProxyExecutable { args ->
                        createCheerioObjectProxy(obj.find(args[0].asString()))
                    }
                    "text" -> ProxyExecutable { obj.text() }
                    "html" -> ProxyExecutable { obj.html() }
                    "attr" -> ProxyExecutable { args -> obj.attr(args[0].asString()) }
                    "first" -> ProxyExecutable { createCheerioObjectProxy(obj.first()) }
                    "last" -> ProxyExecutable { createCheerioObjectProxy(obj.last()) }
                    "eq" -> ProxyExecutable { args -> createCheerioObjectProxy(obj.eq(args[0].asInt())) }
                    "length" -> obj.length
                    "each" -> ProxyExecutable { args ->
                        // Execute callback for each element
                        val callback = args.getOrNull(0)
                        if (callback != null && callback.canExecute()) {
                            for (i in 0 until obj.length) {
                                val el = obj.eq(i)
                                callback.execute(i, createCheerioObjectProxy(el))
                            }
                        }
                        createCheerioObjectProxy(obj)
                    }
                    "map" -> ProxyExecutable { args ->
                        val callback = args.getOrNull(0)
                        val results = mutableListOf<Any?>()
                        if (callback != null && callback.canExecute()) {
                            for (i in 0 until obj.length) {
                                val el = obj.eq(i)
                                val result = callback.execute(i, createCheerioObjectProxy(el))
                                results.add(result)
                            }
                        }
                        // Return an object with get() method
                        ProxyObject.fromMap(mapOf(
                            "get" to ProxyExecutable { results },
                            "toArray" to ProxyExecutable { results }
                        ))
                    }
                    "toArray" -> ProxyExecutable { obj.toArray() }
                    "get" -> ProxyExecutable { args ->
                        if (args.isEmpty()) {
                            obj.get()
                        } else {
                            val index = args[0].asInt()
                            obj.get(index)?.let { createCheerioObjectProxy(it) }
                        }
                    }
                    "next" -> ProxyExecutable { createCheerioObjectProxy(obj.next()) }
                    "prev" -> ProxyExecutable { createCheerioObjectProxy(obj.prev()) }
                    "parent" -> ProxyExecutable { createCheerioObjectProxy(obj.parent()) }
                    "children" -> ProxyExecutable { createCheerioObjectProxy(obj.children()) }
                    "siblings" -> ProxyExecutable { createCheerioObjectProxy(obj.siblings()) }
                    else -> null
                }
            }
            
            override fun getMemberKeys(): Any = setOf("find", "text", "html", "attr", "first", "last", "eq", "length", "each", "map", "toArray", "get", "next", "prev", "parent", "children", "siblings")
            override fun hasMember(key: String?): Boolean = key in getMemberKeys() as Set<*>
            override fun putMember(key: String?, value: Value?) {}
            override fun removeMember(key: String?): Boolean = false
        }
        return proxy
    }
    
    /**
     * Get the adapter JavaScript code.
     */
    private fun getAdapterCode(): String {
        return """
            // Setup URL API polyfill
            if (typeof URL === 'undefined') {
                globalThis.URL = function(url, base) {
                    if (url === null || url === undefined) {
                        throw new Error('Invalid URL: URL cannot be null or undefined');
                    }
                    
                    url = String(url);
                    let fullUrl = url;
                    
                    if (base && !url.match(/^https?:\/\//)) {
                        base = String(base);
                        if (url.startsWith('/')) {
                            const baseMatch = base.match(/^(https?:\/\/[^\/]+)/);
                            fullUrl = baseMatch ? baseMatch[1] + url : url;
                        } else {
                            const basePath = base.replace(/\/[^\/]*$/, '/');
                            fullUrl = basePath + url;
                        }
                    }
                    
                    const match = fullUrl.match(/^(https?):\/\/([^\/\?#]+)(\/[^\?#]*)?(\\?[^#]*)?(#.*)?$/);
                    if (!match) {
                        throw new Error('Invalid URL: ' + fullUrl);
                    }
                    
                    const protocol = match[1] || 'http';
                    const hostWithPort = match[2] || '';
                    const pathname = match[3] || '/';
                    const search = match[4] || '';
                    const hash = match[5] || '';
                    
                    const hostParts = (hostWithPort || '').split(':');
                    const hostname = hostParts[0] || '';
                    const port = hostParts[1] || '';
                    
                    this.protocol = String(protocol) + ':';
                    this.host = String(hostWithPort);
                    this.hostname = String(hostname);
                    this.port = String(port);
                    this.pathname = String(pathname);
                    this.search = String(search);
                    this.hash = String(hash);
                    this.href = String(fullUrl);
                    this.origin = String(protocol) + '://' + String(hostWithPort);
                    this.toString = function() { return this.href; };
                    this.toJSON = function() { return this.href; };
                };
            }
            
            // Setup URLSearchParams for query string manipulation
            if (typeof URLSearchParams === 'undefined') {
                globalThis.URLSearchParams = function(init) {
                    this.params = {};
                    
                    if (typeof init === 'string') {
                        const query = init.startsWith('?') ? init.substring(1) : init;
                        if (query) {
                            query.split('&').forEach(function(pair) {
                                const parts = pair.split('=');
                                const key = decodeURIComponent(parts[0]);
                                const value = parts[1] ? decodeURIComponent(parts[1]) : '';
                                if (!this.params[key]) {
                                    this.params[key] = [];
                                }
                                this.params[key].push(value);
                            }.bind(this));
                        }
                    } else if (init && typeof init === 'object') {
                        for (const key in init) {
                            if (init.hasOwnProperty(key)) {
                                this.params[key] = [String(init[key])];
                            }
                        }
                    }
                    
                    this.append = function(key, value) {
                        if (!this.params[key]) {
                            this.params[key] = [];
                        }
                        this.params[key].push(String(value));
                    };
                    
                    this.delete = function(key) {
                        delete this.params[key];
                    };
                    
                    this.get = function(key) {
                        return this.params[key] ? this.params[key][0] : null;
                    };
                    
                    this.getAll = function(key) {
                        return this.params[key] || [];
                    };
                    
                    this.has = function(key) {
                        return key in this.params;
                    };
                    
                    this.set = function(key, value) {
                        this.params[key] = [String(value)];
                    };
                    
                    this.toString = function() {
                        const parts = [];
                        for (const key in this.params) {
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
            
            // Setup fetch API using the bridge
            globalThis.fetch = async function(url, options) {
                const response = bridge.fetch(url, options || {});
                const responseText = response.text;
                return {
                    ok: response.ok,
                    status: response.status,
                    statusText: response.statusText,
                    headers: response.headers,
                    url: url,
                    redirected: false,
                    type: 'basic',
                    text: async () => responseText,
                    json: async () => {
                        try {
                            return JSON.parse(responseText);
                        } catch (e) {
                            const preview = responseText.substring(0, 100);
                            throw new Error('Failed to parse JSON from ' + url + ': ' + e.message + ' (content starts with: ' + preview + ')');
                        }
                    },
                    blob: async () => new Blob([responseText]),
                    arrayBuffer: async () => new ArrayBuffer(0),
                    clone: function() { return this; }
                };
            };
            
            // Also provide fetchApi alias for compatibility
            globalThis.fetchApi = globalThis.fetch;
            
            // Setup cheerio using native implementation
            if (typeof __nativeCheerio !== 'undefined') {
                globalThis.cheerio = __nativeCheerio;
            } else {
                console.warn('Native cheerio not available');
            }
            
            // Setup setTimeout/setInterval for promise polling
            globalThis.setTimeout = function(callback, delay) {
                // Immediate execution for now (GraalVM doesn't have native timers)
                callback();
                return 0;
            };
            
            globalThis.setInterval = function(callback, delay) {
                // Not implemented - return dummy ID
                return 0;
            };
            
            globalThis.clearTimeout = function(id) {
                // No-op
            };
            
            globalThis.clearInterval = function(id) {
                // No-op
            };
            
            // Setup Headers API for fetch compatibility
            if (typeof Headers === 'undefined') {
                globalThis.Headers = function(init) {
                    this.headers = {};
                    
                    if (init) {
                        if (typeof init === 'object') {
                            if (Array.isArray(init)) {
                                // Array of [key, value] pairs
                                init.forEach(function(pair) {
                                    if (Array.isArray(pair) && pair.length >= 2) {
                                        this.headers[pair[0].toLowerCase()] = String(pair[1]);
                                    }
                                }.bind(this));
                            } else {
                                // Object with key-value pairs
                                for (const key in init) {
                                    if (init.hasOwnProperty(key)) {
                                        this.headers[key.toLowerCase()] = String(init[key]);
                                    }
                                }
                            }
                        }
                    }
                    
                    this.append = function(name, value) {
                        this.headers[name.toLowerCase()] = String(value);
                    };
                    
                    this.delete = function(name) {
                        delete this.headers[name.toLowerCase()];
                    };
                    
                    this.get = function(name) {
                        return this.headers[name.toLowerCase()] || null;
                    };
                    
                    this.has = function(name) {
                        return name.toLowerCase() in this.headers;
                    };
                    
                    this.set = function(name, value) {
                        this.headers[name.toLowerCase()] = String(value);
                    };
                    
                    this.entries = function() {
                        const entries = [];
                        for (const key in this.headers) {
                            if (this.headers.hasOwnProperty(key)) {
                                entries.push([key, this.headers[key]]);
                            }
                        }
                        return entries;
                    };
                    
                    this.keys = function() {
                        return Object.keys(this.headers);
                    };
                    
                    this.values = function() {
                        return Object.values(this.headers);
                    };
                    
                    this.forEach = function(callback) {
                        for (const key in this.headers) {
                            if (this.headers.hasOwnProperty(key)) {
                                callback(this.headers[key], key, this);
                            }
                        }
                    };
                };
            }
            
            // Cheerio implementation using ksoup
            // Note: This is injected as a native object, not JavaScript
            // The actual implementation is in createCheerioProxy()
            
            // Wrapper function to adapt plugin to our interface
            function wrapPlugin(plugin) {
                return {
                    getId: () => plugin.id || "unknown",
                    getName: () => plugin.name || "Unknown Plugin",
                    getSite: () => plugin.site || "",
                    getVersion: () => plugin.version || "1.0.0",
                    getLang: () => plugin.lang || plugin.language || "en",
                    getIcon: () => plugin.icon || "",
                    
                    searchNovels: async (query, page) => {
                        try {
                            if (typeof plugin.searchNovels === 'function') {
                                const results = await plugin.searchNovels(query, page);
                                return Array.isArray(results) ? results.map(r => ({
                                    name: r.name || r.title || "",
                                    url: r.url || r.path || "",
                                    cover: r.cover || r.image || ""
                                })) : [];
                            }
                            return [];
                        } catch (e) {
                            console.error('searchNovels error:', e.message, e.stack);
                            throw e;
                        }
                    },
                    
                    popularNovels: async (page) => {
                        if (typeof plugin.popularNovels === 'function') {
                            // Check function arity to determine if it expects options parameter
                            let results;
                            if (plugin.popularNovels.length <= 1) {
                                // Only page parameter expected
                                results = await plugin.popularNovels(page);
                            } else {
                                // Multiple parameters - pass page and options object with filters
                                results = await plugin.popularNovels(page, {
                                    showLatestNovels: false,
                                    filters: plugin.filters || {}
                                });
                            }
                            return Array.isArray(results) ? results.map(r => ({
                                name: r.name || r.title || "",
                                url: r.url || r.path || "",
                                cover: r.cover || r.image || ""
                            })) : [];
                        }
                        return [];
                    },
                    
                    latestNovels: async (page) => {
                        if (typeof plugin.latestNovels === 'function') {
                            // Check function arity to determine if it expects options parameter
                            let results;
                            if (plugin.latestNovels.length <= 1) {
                                // Only page parameter expected
                                results = await plugin.latestNovels(page);
                            } else {
                                // Multiple parameters - pass page and options object with filters
                                results = await plugin.latestNovels(page, {
                                    showLatestNovels: true,
                                    filters: plugin.filters || {}
                                });
                            }
                            return Array.isArray(results) ? results.map(r => ({
                                name: r.name || r.title || "",
                                url: r.url || r.path || "",
                                cover: r.cover || r.image || ""
                            })) : [];
                        }
                        return this.popularNovels(page);
                    },
                    
                    getNovelDetails: async (url) => {
                        try {
                            // Try both getNovelDetails and parseNovel
                            const detailsFunc = plugin.getNovelDetails || plugin.parseNovel;
                            if (typeof detailsFunc === 'function') {
                                const d = await detailsFunc.call(plugin, url);
                                return {
                                    name: d.name || d.title || "",
                                    url: d.url || d.path || url,
                                    cover: d.cover || d.image || "",
                                    author: d.author || null,
                                    description: d.description || d.summary || null,
                                    genres: Array.isArray(d.genres) ? d.genres : [],
                                    status: d.status || null
                                };
                            }
                            return { name: "", url: url, cover: "", author: null, description: null, genres: [], status: null };
                        } catch (e) {
                            console.error('getNovelDetails error:', e.message, e.stack);
                            throw e;
                        }
                    },
                    
                    getChapters: async (url) => {
                        try {
                            console.log('getChapters called with url:', url);
                            console.log('Available plugin methods:', Object.getOwnPropertyNames(Object.getPrototypeOf(plugin)));
                            
                            // The plugin is a class instance - call parseNovel to get novel details which includes chapters
                            if (typeof plugin.parseNovel === 'function') {
                                console.log('Calling plugin.parseNovel to get chapters...');
                                const novel = await plugin.parseNovel(url);
                                console.log('parseNovel returned:', novel);
                                
                                // Check if novel has chapters property
                                if (novel && Array.isArray(novel.chapters)) {
                                    console.log('Found chapters in novel object:', novel.chapters.length);
                                    return novel.chapters.map(c => ({
                                        name: c.name || c.title || "",
                                        url: c.url || c.path || "",
                                        releaseTime: c.releaseTime || c.date || null
                                    }));
                                }
                            }
                            
                            console.log('No chapter data found');
                            return [];
                        } catch (e) {
                            console.error('getChapters error:', e.message, e.stack);
                            throw e;
                        }
                    },
                    
                    getChapterContent: async (url) => {
                        try {
                            // Try both getChapterContent and parseChapter
                            const contentFunc = plugin.getChapterContent || plugin.parseChapter;
                            if (typeof contentFunc === 'function') {
                                const content = await contentFunc.call(plugin, url);
                                return typeof content === 'string' ? content : (content.text || "");
                            }
                            return "";
                        } catch (e) {
                            console.error('getChapterContent error:', e.message, e.stack);
                            throw e;
                        }
                    }
                };
            }
            
            globalThis.wrapPlugin = wrapPlugin;
        """.trimIndent()
    }
    
    /**
     * Close the context and release resources.
     */
    fun close() {
        context?.close()
        context = null
    }
    
    /**
     * Check if a plugin is currently loaded.
     */
    fun isLoaded(): Boolean = context != null
}

/**
 * Helper function to convert GraalVM Value to Map
 */
private fun convertValueToMap(value: Value): Map<String, Any> {
    val result = mutableMapOf<String, Any>()
    
    if (!value.hasMembers()) {
        return result
    }
    
    for (key in value.memberKeys) {
        val member = value.getMember(key)
        if (member != null && !member.isNull) {
            result[key] = convertValueToAny(member)
        }
    }
    
    return result
}

/**
 * Helper function to convert GraalVM Value to appropriate Kotlin type
 */
private fun convertValueToAny(value: Value): Any {
    return when {
        value.isString -> value.asString()
        value.isNumber -> {
            if (value.fitsInInt()) value.asInt()
            else if (value.fitsInLong()) value.asLong()
            else value.asDouble()
        }
        value.isBoolean -> value.asBoolean()
        value.hasArrayElements() -> {
            (0 until value.arraySize).map { i ->
                convertValueToAny(value.getArrayElement(i))
            }
        }
        value.hasMembers() -> convertValueToMap(value)
        else -> value.toString()
    }
}

/**
 * Wrapper that adapts GraalVM Value to LNReaderPlugin interface.
 * All methods use a dedicated single-threaded dispatcher to prevent multi-threaded access to GraalVM Context.
 * Filters are cached during plugin loading to avoid threading issues.
 */
private class GraalVMPluginWrapper(
    private val jsPlugin: Value,
    private val context: Context,
    private val graalvmThread: kotlinx.coroutines.CoroutineDispatcher,
    private val cachedFilters: Map<String, Any>
) : LNReaderPlugin {
    
    private val lock = Any()
    
    /**
     * Wait for a JavaScript Promise to resolve.
     * This uses a simple polling approach since GraalVM doesn't have built-in async/await.
     */
    private fun awaitPromise(promise: Value): Value {
        if (!promise.hasMember("then")) {
            // Not a promise, return as-is
            return promise
        }
        
        var resolved = false
        var result: Value? = null
        var error: Throwable? = null
        
        // Attach then/catch handlers
        promise.invokeMember("then", 
            ProxyExecutable { args ->
                result = args[0]
                resolved = true
                null
            }
        ).invokeMember("catch",
            ProxyExecutable { args ->
                error = Exception("Promise rejected: ${args[0]}")
                resolved = true
                null
            }
        )
        
        // Poll until resolved (with timeout)
        val startTime = System.currentTimeMillis()
        val timeout = 30000L // 30 seconds
        while (!resolved) {
            if (System.currentTimeMillis() - startTime > timeout) {
                throw Exception("Promise timeout after ${timeout}ms")
            }
            Thread.sleep(10)
        }
        
        if (error != null) {
            throw error!!
        }
        
        return result ?: throw Exception("Promise resolved with null")
    }
    
    override suspend fun getId(): String = withContext(Dispatchers.IO) {
        jsPlugin.getMember("getId").execute().asString()
    }
    
    override suspend fun getName(): String = withContext(Dispatchers.IO) {
        jsPlugin.getMember("getName").execute().asString()
    }
    
    override suspend fun getSite(): String = withContext(Dispatchers.IO) {
        jsPlugin.getMember("getSite").execute().asString()
    }
    
    override suspend fun getVersion(): String = withContext(Dispatchers.IO) {
        jsPlugin.getMember("getVersion").execute().asString()
    }
    
    override suspend fun getLang(): String = withContext(Dispatchers.IO) {
        jsPlugin.getMember("getLang").execute().asString()
    }
    
    override suspend fun getIcon(): String = withContext(Dispatchers.IO) {
        jsPlugin.getMember("getIcon").execute().asString()
    }
    
    override suspend fun searchNovels(query: String, page: Int): List<PluginNovel> = withContext(Dispatchers.IO) {
        synchronized(lock) {
            context.enter()
            try {
                val searchFunc = jsPlugin.getMember("searchNovels")
                if (searchFunc == null || searchFunc.isNull) {
                    return@withContext emptyList()
                }
                var result = searchFunc.execute(query, page)
        
        // Check if result is a Promise and wait for it
        if (result.hasMember("then") && result.getMember("then").canExecute()) {
            result = awaitPromise(result)
        }
        
                convertToNovelList(result)
            } finally {
                context.leave()
            }
        }
    }
    
    override suspend fun popularNovels(page: Int, filters: Map<String, Any>): List<PluginNovel> = withContext(graalvmThread) {
        try {
            val popularFunc = jsPlugin.getMember("popularNovels")
            if (popularFunc == null || popularFunc.isNull) {
                return@withContext emptyList()
            }
            
            // If filters are provided, pass them to the plugin
            val result = if (filters.isNotEmpty()) {
                // Convert filters map to JavaScript object
                val filtersJson = kotlinx.serialization.json.Json.encodeToString(
                    kotlinx.serialization.json.JsonObject.serializer(),
                    filters.toJsonObject()
                )
                
                // Create options object with filters
                val optionsObj = context.eval("js", "({ filters: $filtersJson })")
                
                // Call with page and options
                popularFunc.execute(page, optionsObj)
            } else {
                // Call with just page
                popularFunc.execute(page)
            }
            
            // Handle promise if returned
            val finalResult = if (result.hasMember("then")) {
                awaitPromise(result)
            } else {
                result
            }
            
            convertToNovelList(finalResult)
        } catch (e: Exception) {
            Log.error("GraalVMPluginWrapper: Error in popularNovels: ${e.message}", e)
            emptyList()
        }
    }
    
    override suspend fun latestNovels(page: Int): List<PluginNovel> = withContext(Dispatchers.IO) {
        var result = jsPlugin.getMember("latestNovels").execute(page)
        if (result.hasMember("then")) {
            result = awaitPromise(result)
        }
        convertToNovelList(result)
    }
    
    override suspend fun getNovelDetails(url: String): PluginNovelDetails = withContext(Dispatchers.IO) {
        synchronized(lock) {
            context.enter()
            try {
                var result = jsPlugin.getMember("getNovelDetails").execute(url)
                if (result.hasMember("then")) {
                    result = awaitPromise(result)
                }
                PluginNovelDetails(
                    name = result.getMember("name").asString(),
                    url = result.getMember("url").asString(),
                    cover = result.getMember("cover")?.asString() ?: "",
                    author = result.getMember("author")?.asString(),
                    description = result.getMember("description")?.asString(),
                    genres = result.getMember("genres")?.let { convertToStringList(it) } ?: emptyList(),
                    status = result.getMember("status")?.asString()
                )
            } catch (e: Exception) {
                Log.error("GraalVMPluginWrapper: Error in getNovelDetails: ${e.message}", e)
                throw e
            } finally {
                context.leave()
            }
        }
    }
    
    override suspend fun getChapters(url: String): List<PluginChapter> = withContext(Dispatchers.IO) {
        synchronized(lock) {
            context.enter()
            try {
                var result = jsPlugin.getMember("getChapters").execute(url)
                if (result.hasMember("then")) {
                    result = awaitPromise(result)
                }
                convertToChapterList(result)
            } catch (e: Exception) {
                Log.error("GraalVMPluginWrapper: Error in getChapters: ${e.message}", e)
                throw e
            } finally {
                context.leave()
            }
        }
    }
    
    override suspend fun getChapterContent(url: String): String = withContext(Dispatchers.IO) {
        var result = jsPlugin.getMember("getChapterContent").execute(url)
        if (result.hasMember("then")) {
            result = awaitPromise(result)
        }
        result.asString()
    }
    
    override fun getFilters(): Map<String, Any> {
        return cachedFilters
    }
    
    private fun convertValueToMap(value: Value): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        if (!value.hasMembers()) {
            return result
        }
        
        for (key in value.memberKeys) {
            val member = value.getMember(key)
            if (member != null && !member.isNull) {
                result[key] = convertValueToAny(member)
            }
        }
        
        return result
    }
    
    private fun convertValueToAny(value: Value): Any {
        return when {
            value.isString -> value.asString()
            value.isNumber -> {
                if (value.fitsInInt()) value.asInt()
                else if (value.fitsInLong()) value.asLong()
                else value.asDouble()
            }
            value.isBoolean -> value.asBoolean()
            value.hasArrayElements() -> {
                (0 until value.arraySize).map { i ->
                    convertValueToAny(value.getArrayElement(i))
                }
            }
            value.hasMembers() -> convertValueToMap(value)
            else -> value.toString()
        }
    }
    
    private fun convertToNovelList(value: Value): List<PluginNovel> {
        if (!value.hasArrayElements()) {
            return emptyList()
        }
        return (0 until value.arraySize).map { i ->
            val item = value.getArrayElement(i)
            PluginNovel(
                name = item.getMember("name")?.asString() ?: "",
                url = item.getMember("url")?.asString() ?: "",
                cover = item.getMember("cover")?.asString() ?: ""
            )
        }
    }
    
    private fun convertToChapterList(value: Value): List<PluginChapter> {
        if (!value.hasArrayElements()) {
            return emptyList()
        }
        return (0 until value.arraySize).map { i ->
            val item = value.getArrayElement(i)
            PluginChapter(
                name = item.getMember("name")?.asString() ?: "",
                url = item.getMember("url")?.asString() ?: "",
                releaseTime = item.getMember("releaseTime")?.asString()
            )
        }
    }
    
    private fun convertToStringList(value: Value): List<String> {
        if (!value.hasArrayElements()) return emptyList()
        return (0 until value.arraySize).map { i ->
            value.getArrayElement(i).asString()
        }
    }
}


/**
 * Extension function to convert Map<String, Any> to JsonObject
 */
private fun Map<String, Any>.toJsonObject(): kotlinx.serialization.json.JsonObject {
    val map = mutableMapOf<String, kotlinx.serialization.json.JsonElement>()
    for ((key, value) in this) {
        map[key] = when (value) {
            is String -> kotlinx.serialization.json.JsonPrimitive(value)
            is Number -> kotlinx.serialization.json.JsonPrimitive(value)
            is Boolean -> kotlinx.serialization.json.JsonPrimitive(value)
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                (value as Map<String, Any>).toJsonObject()
            }
            is List<*> -> kotlinx.serialization.json.JsonArray(value.map { item ->
                when (item) {
                    is String -> kotlinx.serialization.json.JsonPrimitive(item)
                    is Number -> kotlinx.serialization.json.JsonPrimitive(item)
                    is Boolean -> kotlinx.serialization.json.JsonPrimitive(item)
                    else -> kotlinx.serialization.json.JsonPrimitive(item.toString())
                }
            })
            else -> kotlinx.serialization.json.JsonPrimitive(value.toString())
        }
    }
    return kotlinx.serialization.json.JsonObject(map)
}
