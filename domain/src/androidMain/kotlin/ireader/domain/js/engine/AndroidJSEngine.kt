package ireader.domain.js.engine

import ireader.core.log.Log
import ireader.domain.js.bridge.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.concurrent.Executors

/**
 * Android JavaScript engine implementation using J2V8 (V8).
 * 
 * IMPORTANT: V8 is single-threaded and must be accessed from the same thread.
 * We use a dedicated single-threaded executor to ensure thread safety.
 */
class AndroidJSEngine(
    private val bridgeService: JSBridgeService? = null
) {
    
    private var jsEngine: JSEngine? = null
    private val mutex = Mutex()
    private var bridgeServiceRef: JSBridgeService? = null
    
    // V8 requires single-threaded access - create dedicated thread
    private val v8Thread = Executors.newSingleThreadExecutor { r ->
        Thread(r, "V8-Thread").apply {
            isDaemon = true
        }
    }.asCoroutineDispatcher()
    
    /**
     * Load a plugin from JavaScript code.
     * All V8 operations must happen on the dedicated V8 thread.
     */
    suspend fun loadPlugin(jsCode: String, pluginId: String): LNReaderPlugin = withContext(v8Thread) {
        mutex.withLock {
            try {
                // Close existing engine if any
                jsEngine?.dispose()
                
                // Create new engine
                val newEngine = JSEngine()
                newEngine.initialize()
                
                Log.debug("AndroidJSEngine: Loading adapter code")
                val adapterCode = getAdapterCode()
                try {
                    newEngine.evaluateScript(adapterCode)
                    Log.debug("AndroidJSEngine: Adapter loaded")
                } catch (e: Exception) {
                    Log.error("AndroidJSEngine: Adapter failed to load", e)
                    // Log the first 500 chars of adapter code for debugging
                    Log.error("AndroidJSEngine: Adapter code start: ${adapterCode.take(500)}")
                    throw e
                }
                
                // Setup bridge if available
                if (bridgeService != null) {
                    setupBridge(newEngine, bridgeService, pluginId)
                }
                
                // Initialize module system
                newEngine.evaluateScript("if (typeof exports === 'undefined') { var exports = {}; }")
                newEngine.evaluateScript("if (typeof module === 'undefined') { var module = { exports: exports }; }")
                
                // Load the plugin code
                Log.debug("AndroidJSEngine: Loading plugin code (${jsCode.length} chars)")
                
                // QuickJS-NG supports ES6+ natively - no conversion needed!
                Log.debug("AndroidJSEngine: Loading plugin with native ES6+ support")
                
                try {
                    newEngine.evaluateScript(jsCode)
                    Log.debug("AndroidJSEngine: Plugin code loaded")
                } catch (e: Exception) {
                    Log.error("AndroidJSEngine: Plugin code failed to load", e)
                    throw e
                }
                
                // Wrap the plugin
                newEngine.evaluateScript("globalThis.__wrappedPlugin = wrapPlugin(exports.default || exports);")
                
                jsEngine = newEngine
                
                // Create Kotlin wrapper (pass v8Thread for thread-safe access)
                AndroidPluginWrapper(newEngine, pluginId, bridgeServiceRef, v8Thread)
                
            } catch (e: Exception) {
                Log.error("AndroidJSEngine: Failed to load plugin: ${e.message}", e)
                throw PluginLoadException("Failed to load plugin: $pluginId - ${e.message}", e)
            }
        }
    }

    /**
     * Setup bridge service for JavaScript to call Kotlin functions.
     * With V8, we can use native Promises without polling!
     */
    private fun setupBridge(engine: JSEngine, bridge: JSBridgeService, pluginId: String) {
        // Store bridge reference for callback
        bridgeServiceRef = bridge
        
        // Setup fetch with native Promise support
        // V8 handles the Promise resolution natively - no polling needed!
        engine.evaluateScript("""
            globalThis.fetch = function(url, options) {
                return new Promise((resolve, reject) => {
                    // Store request details for Kotlin to access
                    globalThis.__pendingFetch = {
                        url: String(url || ''),
                        method: (options && options.method) || 'GET',
                        headers: (options && options.headers) || {},
                        body: (options && options.body) || null,
                        resolve: resolve,
                        reject: reject
                    };
                    
                    // Signal that a fetch is pending
                    globalThis.__fetchReady = true;
                });
            };
            
            globalThis.fetchApi = globalThis.fetch;
        """.trimIndent())
    }


    /**
     * Get the adapter JavaScript code in ES5 for compatibility.
     * The plugins themselves can be ES2017, but our adapter must be ES5.
     */
    private fun getAdapterCode(): String {
        return """
            // Console polyfill with actual logging
            (function() {
                var console = {};
                console.log = function() { 
                    var msg = Array.prototype.slice.call(arguments).join(' ');
                    globalThis.__consoleLog = msg;
                };
                console.error = function() { 
                    var msg = Array.prototype.slice.call(arguments).join(' ');
                    globalThis.__consoleError = msg;
                };
                console.warn = function() { };
                console.info = function() { };
                console.debug = function() { };
                globalThis.console = console;
            })();
            
            // Wrap native BigInt to handle undefined/null gracefully
            ${getBigIntWrapper()}
            
            // URL polyfills
            ${getUrlPolyfills()}
            
            // setTimeout/setInterval
            globalThis.setTimeout = function(callback, delay) {
                callback();
                return 0;
            };
            
            globalThis.setInterval = function(callback, delay) {
                return 0;
            };
            
            globalThis.clearTimeout = function(id) { };
            globalThis.clearInterval = function(id) { };
            
            // Headers API
            ${getHeadersPolyfill()}
            
            // TextEncoder/TextDecoder polyfills
            ${getTextEncoderPolyfill()}
            
            // Blob polyfill
            ${getBlobPolyfill()}
            
            // Cheerio stub (can be enhanced later)
            (function() {
                var cheerio = {};
                cheerio.load = function(html) {
                    return function(selector) {
                        var result = {};
                        result.text = function() { return ''; };
                        result.html = function() { return ''; };
                        result.attr = function() { return null; };
                        result.find = function() { return result; };
                        result.eq = function() { return result; };
                        result.length = 0;
                        result.each = function() { return result; };
                        result.map = function() { 
                            var mapResult = {};
                            mapResult.get = function() { return []; };
                            return mapResult;
                        };
                        return result;
                    };
                };
                globalThis.cheerio = cheerio;
            })();
            
            // Wrapper function
            ${getWrapperFunction()}
        """.trimIndent()
    }
    
    private fun getBigIntWrapper(): String {
        return """
            // Wrap native BigInt to handle undefined/null gracefully
            if (typeof BigInt !== 'undefined') {
                const NativeBigInt = BigInt;
                globalThis.BigInt = function(value) {
                    // Handle undefined/null by returning 0 instead of throwing
                    if (value === undefined || value === null) {
                        console.warn('BigInt called with ' + value + ', returning 0');
                        return NativeBigInt(0);
                    }
                    return NativeBigInt(value);
                };
                
                // Copy static methods
                globalThis.BigInt.asIntN = NativeBigInt.asIntN;
                globalThis.BigInt.asUintN = NativeBigInt.asUintN;
            }
        """.trimIndent()
    }
    
    private fun getBigIntPolyfill(): String {
        return """
            if (typeof BigInt === 'undefined') {
                globalThis.BigInt = function(value) {
                    // Handle undefined/null
                    if (value === undefined || value === null) {
                        throw new TypeError('Cannot convert ' + value + ' to a BigInt');
                    }
                    
                    // Simple BigInt polyfill that converts to regular number
                    // This is a basic implementation - may lose precision for very large numbers
                    if (typeof value === 'string') {
                        var parsed = parseInt(value, 10);
                        if (isNaN(parsed)) {
                            throw new SyntaxError('Cannot convert ' + value + ' to a BigInt');
                        }
                        return parsed;
                    }
                    
                    if (typeof value === 'number') {
                        if (!isFinite(value) || Math.floor(value) !== value) {
                            throw new RangeError('The number ' + value + ' cannot be converted to a BigInt');
                        }
                        return value;
                    }
                    
                    if (typeof value === 'boolean') {
                        return value ? 1 : 0;
                    }
                    
                    throw new TypeError('Cannot convert ' + typeof value + ' to a BigInt');
                };
                
                // Add BigInt methods
                globalThis.BigInt.asIntN = function(bits, value) {
                    if (value === undefined || value === null) {
                        return 0;
                    }
                    return Number(value);
                };
                
                globalThis.BigInt.asUintN = function(bits, value) {
                    if (value === undefined || value === null) {
                        return 0;
                    }
                    return Math.abs(Number(value));
                };
            }
        """.trimIndent()
    }
    
    private fun getUrlPolyfills(): String {
        return """
            if (typeof URL === 'undefined') {
                globalThis.URL = function(url, base) {
                    if (url === null || url === undefined) {
                        throw new Error('Invalid URL');
                    }
                    url = String(url);
                    var fullUrl = url;
                    if (base && !url.match(/^https?:\/\//)) {
                        base = String(base);
                        if (url.indexOf('/') === 0) {
                            var baseMatch = base.match(/^(https?:\/\/[^\/]+)/);
                            fullUrl = baseMatch ? baseMatch[1] + url : url;
                        } else {
                            var basePath = base.replace(/\/[^\/]*$/, '/');
                            fullUrl = basePath + url;
                        }
                    }
                    var match = fullUrl.match(/^(https?):\/\/([^\/\?#]+)(\/[^\?#]*)?(\\?[^#]*)?(#.*)?$/);
                    if (!match) throw new Error('Invalid URL: ' + fullUrl);
                    var protocol = match[1] || 'http';
                    var hostWithPort = match[2] || '';
                    var pathname = match[3] || '/';
                    var search = match[4] || '';
                    var hash = match[5] || '';
                    var hostParts = (hostWithPort || '').split(':');
                    this.protocol = String(protocol) + ':';
                    this.host = String(hostWithPort);
                    this.hostname = String(hostParts[0] || '');
                    this.port = String(hostParts[1] || '');
                    this.pathname = String(pathname);
                    this.search = String(search);
                    this.hash = String(hash);
                    this.href = String(fullUrl);
                    this.origin = String(protocol) + '://' + String(hostWithPort);
                    this.toString = function() { return this.href; };
                };
            }
            
            if (typeof URLSearchParams === 'undefined') {
                globalThis.URLSearchParams = function(init) {
                    this.params = {};
                    if (typeof init === 'string') {
                        var query = init.indexOf('?') === 0 ? init.substring(1) : init;
                        if (query) {
                            query.split('&').forEach(function(pair) {
                                var parts = pair.split('=');
                                var key = decodeURIComponent(parts[0]);
                                var value = parts[1] ? decodeURIComponent(parts[1]) : '';
                                if (!this.params[key]) this.params[key] = [];
                                this.params[key].push(value);
                            }.bind(this));
                        }
                    } else if (init && typeof init === 'object') {
                        for (var key in init) {
                            if (init.hasOwnProperty(key)) {
                                this.params[key] = [String(init[key])];
                            }
                        }
                    }
                    this.get = function(key) {
                        return this.params[key] ? this.params[key][0] : null;
                    };
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
        """.trimIndent()
    }
    
    private fun getHeadersPolyfill(): String {
        return """
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
                    this.get = function(name) {
                        return this.headers[name.toLowerCase()] || null;
                    };
                    this.set = function(name, value) {
                        this.headers[name.toLowerCase()] = String(value);
                    };
                };
            }
        """.trimIndent()
    }
    
    private fun getTextEncoderPolyfill(): String {
        return """
            if (typeof TextEncoder === 'undefined') {
                globalThis.TextEncoder = function() {
                    this.encode = function(str) {
                        // Simple UTF-8 encoding
                        var utf8 = [];
                        for (var i = 0; i < str.length; i++) {
                            var charcode = str.charCodeAt(i);
                            if (charcode < 0x80) utf8.push(charcode);
                            else if (charcode < 0x800) {
                                utf8.push(0xc0 | (charcode >> 6), 
                                         0x80 | (charcode & 0x3f));
                            }
                            else if (charcode < 0xd800 || charcode >= 0xe000) {
                                utf8.push(0xe0 | (charcode >> 12), 
                                         0x80 | ((charcode >> 6) & 0x3f), 
                                         0x80 | (charcode & 0x3f));
                            }
                            else {
                                i++;
                                charcode = 0x10000 + (((charcode & 0x3ff) << 10)
                                          | (str.charCodeAt(i) & 0x3ff));
                                utf8.push(0xf0 | (charcode >> 18), 
                                         0x80 | ((charcode >> 12) & 0x3f), 
                                         0x80 | ((charcode >> 6) & 0x3f), 
                                         0x80 | (charcode & 0x3f));
                            }
                        }
                        return new Uint8Array(utf8);
                    };
                };
            }
            
            if (typeof TextDecoder === 'undefined') {
                globalThis.TextDecoder = function() {
                    this.decode = function(bytes) {
                        // Simple UTF-8 decoding
                        var str = '';
                        var i = 0;
                        while (i < bytes.length) {
                            var c = bytes[i++];
                            if (c < 128) {
                                str += String.fromCharCode(c);
                            } else if (c < 224) {
                                str += String.fromCharCode(((c & 31) << 6) | (bytes[i++] & 63));
                            } else if (c < 240) {
                                str += String.fromCharCode(((c & 15) << 12) | ((bytes[i++] & 63) << 6) | (bytes[i++] & 63));
                            } else {
                                var c2 = ((c & 7) << 18) | ((bytes[i++] & 63) << 12) | ((bytes[i++] & 63) << 6) | (bytes[i++] & 63);
                                c2 -= 0x10000;
                                str += String.fromCharCode((c2 >> 10) + 0xD800, (c2 & 0x3FF) + 0xDC00);
                            }
                        }
                        return str;
                    };
                };
            }
        """.trimIndent()
    }
    
    private fun getBlobPolyfill(): String {
        return """
            if (typeof Blob === 'undefined') {
                globalThis.Blob = function(parts, options) {
                    this.parts = parts || [];
                    this.options = options || {};
                    this.type = this.options.type || '';
                    this.size = 0;
                    
                    for (var i = 0; i < this.parts.length; i++) {
                        if (typeof this.parts[i] === 'string') {
                            this.size += this.parts[i].length;
                        }
                    }
                    
                    this.text = function() {
                        return Promise.resolve(this.parts.join(''));
                    };
                    
                    this.arrayBuffer = function() {
                        var text = this.parts.join('');
                        var encoder = new TextEncoder();
                        return Promise.resolve(encoder.encode(text).buffer);
                    };
                };
            }
        """.trimIndent()
    }

    private fun getWrapperFunction(): String {
        return """
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
                                var novel = {};
                                novel.name = r.name || r.title || "";
                                novel.url = r.url || r.path || "";
                                novel.cover = r.cover || r.image || "";
                                return novel;
                            });
                        });
                    }
                    return Promise.resolve([]);
                };
                
                wrapper.popularNovels = function(page) {
                    if (typeof plugin.popularNovels === 'function') {
                        // Check function arity (number of parameters)
                        var result;
                        if (plugin.popularNovels.length === 0) {
                            // No parameters expected
                            result = plugin.popularNovels();
                        } else if (plugin.popularNovels.length === 1) {
                            // Only page parameter
                            result = plugin.popularNovels(page);
                        } else {
                            // Multiple parameters - pass page and options object
                            // LNReader format: popularNovels(page, { showLatestNovels, filters })
                            // Filters structure: each filter has a 'value' property
                            result = plugin.popularNovels(page, { 
                                showLatestNovels: false,
                                filters: plugin.filters || {}  // Use plugin's default filters if available
                            });
                        }
                        
                        return Promise.resolve(result).then(function(results) {
                            if (!Array.isArray(results)) return [];
                            return results.map(function(r) {
                                var novel = {};
                                novel.name = r.name || r.title || "";
                                novel.url = r.url || r.path || "";
                                novel.cover = r.cover || r.image || "";
                                return novel;
                            });
                        });
                    }
                    return Promise.resolve([]);
                };
                
                wrapper.latestNovels = function(page) {
                    if (typeof plugin.latestNovels === 'function') {
                        // Check function arity (number of parameters)
                        var result;
                        if (plugin.latestNovels.length === 0) {
                            // No parameters expected
                            result = plugin.latestNovels();
                        } else if (plugin.latestNovels.length === 1) {
                            // Only page parameter
                            result = plugin.latestNovels(page);
                        } else {
                            // Multiple parameters - pass page and options object
                            // LNReader format: latestNovels(page, { showLatestNovels, filters })
                            // Filters structure: each filter has a 'value' property
                            result = plugin.latestNovels(page, { 
                                showLatestNovels: true,
                                filters: plugin.filters || {}  // Use plugin's default filters if available
                            });
                        }
                        
                        return Promise.resolve(result).then(function(results) {
                            if (!Array.isArray(results)) return [];
                            return results.map(function(r) {
                                var novel = {};
                                novel.name = r.name || r.title || "";
                                novel.url = r.url || r.path || "";
                                novel.cover = r.cover || r.image || "";
                                return novel;
                            });
                        });
                    }
                    // Fallback to popularNovels if latestNovels doesn't exist
                    return wrapper.popularNovels(page);
                };
                
                wrapper.getNovelDetails = function(url) {
                    if (typeof plugin.parseNovel === 'function') {
                        return Promise.resolve(plugin.parseNovel(url)).then(function(d) {
                            var details = {};
                            details.name = d.name || d.title || "";
                            details.url = d.url || d.path || url;
                            details.cover = d.cover || d.image || "";
                            details.author = d.author || null;
                            details.description = d.description || d.summary || null;
                            details.genres = Array.isArray(d.genres) ? d.genres : [];
                            details.status = d.status || null;
                            return details;
                        });
                    }
                    var empty = {};
                    empty.name = "";
                    empty.url = url;
                    empty.cover = "";
                    empty.author = null;
                    empty.description = null;
                    empty.genres = [];
                    empty.status = null;
                    return Promise.resolve(empty);
                };
                
                wrapper.getChapters = function(url) {
                    if (typeof plugin.parseNovel === 'function') {
                        return Promise.resolve(plugin.parseNovel(url)).then(function(novel) {
                            if (novel && Array.isArray(novel.chapters)) {
                                return novel.chapters.map(function(c) {
                                    var chapter = {};
                                    chapter.name = c.name || c.title || "";
                                    chapter.url = c.url || c.path || "";
                                    chapter.releaseTime = c.releaseTime || c.date || null;
                                    return chapter;
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
    
    /**
     * Close the engine and release resources.
     */
    fun close() {
        kotlinx.coroutines.runBlocking {
            withContext(v8Thread) {
                mutex.withLock {
                    jsEngine?.dispose()
                    jsEngine = null
                }
            }
        }
        v8Thread.close()
    }
    
    /**
     * Check if a plugin is currently loaded.
     */
    fun isLoaded(): Boolean = jsEngine != null
}

/**
 * Wrapper that adapts V8 to LNReaderPlugin interface.
 */
private class AndroidPluginWrapper(
    private val engine: JSEngine,
    private val pluginId: String,
    private val bridgeService: JSBridgeService?,
    private val v8Thread: kotlinx.coroutines.CoroutineDispatcher
) : LNReaderPlugin {
    
    private val mutex = Mutex()
    
    override suspend fun getId(): String = withContext(v8Thread) {
        mutex.withLock {
            engine.evaluateScript("__wrappedPlugin.getId()") as? String ?: pluginId
        }
    }
    
    override suspend fun getName(): String = withContext(v8Thread) {
        mutex.withLock {
            engine.evaluateScript("__wrappedPlugin.getName()") as? String ?: "Unknown"
        }
    }
    
    override suspend fun getSite(): String = withContext(v8Thread) {
        mutex.withLock {
            engine.evaluateScript("__wrappedPlugin.getSite()") as? String ?: ""
        }
    }
    
    override suspend fun getVersion(): String = withContext(v8Thread) {
        mutex.withLock {
            engine.evaluateScript("__wrappedPlugin.getVersion()") as? String ?: "1.0.0"
        }
    }
    
    override suspend fun getLang(): String = withContext(v8Thread) {
        mutex.withLock {
            engine.evaluateScript("__wrappedPlugin.getLang()") as? String ?: "en"
        }
    }
    
    override suspend fun getIcon(): String = withContext(v8Thread) {
        mutex.withLock {
            engine.evaluateScript("__wrappedPlugin.getIcon()") as? String ?: ""
        }
    }
    
    override suspend fun searchNovels(query: String, page: Int): List<PluginNovel> = withContext(v8Thread) {
        mutex.withLock {
            try {
                Log.debug("AndroidPluginWrapper: searchNovels called with query='$query', page=$page")
                
                val resultJson = awaitPromise("__wrappedPlugin.searchNovels('${query.replace("'", "\\'")}', $page)")
                parseNovelList(resultJson)
            } catch (e: Exception) {
                Log.error("AndroidPluginWrapper: Error in searchNovels: ${e.message}", e)
                emptyList()
            }
        }
    }
    
    override suspend fun popularNovels(page: Int): List<PluginNovel> = withContext(v8Thread) {
        mutex.withLock {
            try {
                val resultJson = awaitPromise("__wrappedPlugin.popularNovels($page)")
                parseNovelList(resultJson)
            } catch (e: Exception) {
                Log.error("AndroidPluginWrapper: Error in popularNovels: ${e.message}", e)
                emptyList()
            }
        }
    }
    
    override suspend fun latestNovels(page: Int): List<PluginNovel> = withContext(v8Thread) {
        mutex.withLock {
            try {
                val resultJson = awaitPromise("__wrappedPlugin.latestNovels($page)")
                parseNovelList(resultJson)
            } catch (e: Exception) {
                Log.error("AndroidPluginWrapper: Error in latestNovels: ${e.message}", e)
                emptyList()
            }
        }
    }
    
    override suspend fun getNovelDetails(url: String): PluginNovelDetails = withContext(v8Thread) {
        mutex.withLock {
            try {
                val resultJson = awaitPromise("__wrappedPlugin.getNovelDetails('${url.replace("'", "\\'")}')") 
                parseNovelDetails(resultJson)
            } catch (e: Exception) {
                Log.error("AndroidPluginWrapper: Error in getNovelDetails: ${e.message}", e)
                PluginNovelDetails("", url, "", null, null, emptyList(), null)
            }
        }
    }
    
    override suspend fun getChapters(url: String): List<PluginChapter> = withContext(v8Thread) {
        mutex.withLock {
            try {
                val resultJson = awaitPromise("__wrappedPlugin.getChapters('${url.replace("'", "\\'")}')") 
                parseChapterList(resultJson)
            } catch (e: Exception) {
                Log.error("AndroidPluginWrapper: Error in getChapters: ${e.message}", e)
                emptyList()
            }
        }
    }
    
    override suspend fun getChapterContent(url: String): String = withContext(v8Thread) {
        mutex.withLock {
            try {
                awaitPromise("__wrappedPlugin.getChapterContent('${url.replace("'", "\\'")}')") 
            } catch (e: Exception) {
                Log.error("AndroidPluginWrapper: Error in getChapterContent: ${e.message}", e)
                ""
            }
        }
    }
    
    override fun getFilters(): Map<String, Any> {
        return try {
            kotlinx.coroutines.runBlocking(v8Thread) {
                mutex.withLock {
                    try {
                        val filtersJson = engine.evaluateScript("""
                            (function() {
                                var filters = null;
                                
                                if (typeof exports !== 'undefined' && exports.filters) {
                                    filters = exports.filters;
                                }
                                else if (typeof module !== 'undefined' && module.exports && module.exports.filters) {
                                    filters = module.exports.filters;
                                }
                                else if (typeof filters !== 'undefined' && filters !== null) {
                                    filters = globalThis.filters;
                                }
                                else if (typeof __wrappedPlugin !== 'undefined' && __wrappedPlugin.filters) {
                                    filters = __wrappedPlugin.filters;
                                }
                                
                                return filters ? JSON.stringify(filters) : '{}';
                            })();
                        """.trimIndent()) as? String ?: "{}"
                        
                        if (filtersJson == "{}") {
                            return@runBlocking emptyMap()
                        }
                        
                        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        val jsonElement = json.parseToJsonElement(filtersJson)
                        
                        if (jsonElement is kotlinx.serialization.json.JsonObject) {
                            jsonElement.toMap()
                        } else {
                            emptyMap()
                        }
                    } catch (e: Exception) {
                        Log.warn("AndroidPluginWrapper: Failed to get filters: ${e.message}")
                        emptyMap()
                    }
                }
            }
        } catch (e: Exception) {
            Log.error("AndroidPluginWrapper: Error in getFilters: ${e.message}")
            emptyMap()
        }
    }
    
    override suspend fun popularNovelsWithFilters(page: Int, filters: Map<String, Any>): List<PluginNovel> = withContext(v8Thread) {
        mutex.withLock {
            try {
                // Convert filters map to JSON
                val filtersJson = kotlinx.serialization.json.Json.encodeToString(
                    kotlinx.serialization.json.JsonObject.serializer(),
                    filters.toJsonObject()
                )
                
                // Call plugin with filters
                val resultJson = awaitPromise("__wrappedPlugin.popularNovels($page, { filters: $filtersJson })")
                parseNovelList(resultJson)
            } catch (e: Exception) {
                Log.warn("AndroidPluginWrapper: Error in popularNovelsWithFilters, falling back to popularNovels: ${e.message}")
                // Fallback to regular popularNovels
                popularNovels(page)
            }
        }
    }
    
    /**
     * Wait for a JavaScript Promise to resolve.
     * With V8, Promises work natively - we just need to handle fetch requests.
     */
    private suspend fun awaitPromise(jsExpression: String): String {
        val promiseId = System.currentTimeMillis()
        
        Log.debug("AndroidPluginWrapper: awaitPromise starting for expression (promiseId=$promiseId)")
        
        // Setup promise handlers
        engine.evaluateScript("""
            (async function() {
                try {
                    const result = await ($jsExpression);
                    globalThis.__promiseResult_$promiseId = JSON.stringify(result);
                    globalThis.__promiseStatus_$promiseId = 'resolved';
                } catch (e) {
                    // Better error reporting
                    const errorMsg = e.message || String(e);
                    const errorStack = e.stack || '';
                    globalThis.__promiseError_$promiseId = errorMsg + (errorStack ? '\n' + errorStack : '');
                    globalThis.__promiseStatus_$promiseId = 'rejected';
                    console.error('Promise error:', errorMsg, errorStack);
                }
            })();
        """.trimIndent())
        
        val startTime = System.currentTimeMillis()
        val timeout = 30000L
        
        while (System.currentTimeMillis() - startTime < timeout) {
            // Process any pending fetch requests
            processPendingFetch()
            
            // Check promise status
            val status = engine.evaluateScript("globalThis.__promiseStatus_$promiseId") as? String
            
            when (status) {
                "resolved" -> {
                    val result = engine.evaluateScript("globalThis.__promiseResult_$promiseId") as? String ?: ""
                    engine.evaluateScript("""
                        delete globalThis.__promiseResult_$promiseId;
                        delete globalThis.__promiseStatus_$promiseId;
                    """.trimIndent())
                    return result
                }
                "rejected" -> {
                    val error = engine.evaluateScript("globalThis.__promiseError_$promiseId") as? String ?: "Unknown error"
                    engine.evaluateScript("""
                        delete globalThis.__promiseError_$promiseId;
                        delete globalThis.__promiseStatus_$promiseId;
                    """.trimIndent())
                    throw Exception("Promise rejected: $error")
                }
            }
            
            kotlinx.coroutines.delay(10) // Shorter delay since V8 is faster
        }
        
        // Cleanup on timeout
        engine.evaluateScript("""
            delete globalThis.__promiseResult_$promiseId;
            delete globalThis.__promiseStatus_$promiseId;
            delete globalThis.__promiseError_$promiseId;
        """.trimIndent())
        
        throw Exception("Promise timeout after ${timeout}ms")
    }
    
    /**
     * Process a single pending fetch request if any.
     * With V8, we can directly resolve/reject the Promise from Kotlin!
     */
    private suspend fun processPendingFetch() {
        val bridge = bridgeService ?: return
        
        try {
            // Check if there's a pending fetch
            val fetchReady = engine.evaluateScript("globalThis.__fetchReady") as? Boolean
            if (fetchReady != true) {
                return
            }
            
            // Clear the flag
            engine.evaluateScript("globalThis.__fetchReady = false;")
            
            // Get fetch details
            val fetchJson = engine.evaluateScript("JSON.stringify(globalThis.__pendingFetch)") as? String ?: return
            val fetchData = Json.parseToJsonElement(fetchJson).jsonObject
            
            val url = fetchData["url"]?.jsonPrimitive?.content ?: return
            val method = fetchData["method"]?.jsonPrimitive?.content ?: "GET"
            
            Log.debug("AndroidJSEngine: Processing fetch request: $method $url")
            
            // Make the actual HTTP request
            val response = withContext(Dispatchers.IO) {
                bridge.fetch(url, FetchOptions(method = method))
            }
            
            // Store response body in a global variable to avoid escaping issues
            val responseBodyVar = "__responseBody_${System.currentTimeMillis()}"
            engine.evaluateScript("globalThis.$responseBodyVar = ${Json.encodeToString(kotlinx.serialization.json.JsonPrimitive.serializer(), kotlinx.serialization.json.JsonPrimitive(response.text))};")
            
            // Resolve the Promise directly in JavaScript with full Response API
            engine.evaluateScript("""
                if (globalThis.__pendingFetch && globalThis.__pendingFetch.resolve) {
                    const bodyText = globalThis.$responseBodyVar;
                    delete globalThis.$responseBodyVar;
                    
                    const response = {
                        ok: ${response.status >= 200 && response.status < 300},
                        status: ${response.status},
                        statusText: "${response.statusText.replace("\"", "\\\"")}",
                        url: "${url.replace("\"", "\\\"")}",
                        headers: {},
                        bodyUsed: false,
                        
                        // Standard Response methods
                        text: function() {
                            this.bodyUsed = true;
                            return Promise.resolve(bodyText);
                        },
                        
                        json: function() {
                            this.bodyUsed = true;
                            try {
                                return Promise.resolve(JSON.parse(bodyText));
                            } catch (e) {
                                return Promise.reject(new Error('Invalid JSON: ' + e.message));
                            }
                        },
                        
                        arrayBuffer: function() {
                            this.bodyUsed = true;
                            // Convert string to ArrayBuffer
                            const encoder = new TextEncoder();
                            return Promise.resolve(encoder.encode(bodyText).buffer);
                        },
                        
                        blob: function() {
                            this.bodyUsed = true;
                            // Return a simple blob-like object
                            return Promise.resolve(new Blob([bodyText], { type: 'text/plain' }));
                        },
                        
                        formData: function() {
                            this.bodyUsed = true;
                            return Promise.reject(new Error('formData() not implemented'));
                        },
                        
                        clone: function() {
                            if (this.bodyUsed) {
                                throw new Error('Body already used');
                            }
                            return Object.assign({}, this);
                        }
                    };
                    
                    globalThis.__pendingFetch.resolve(response);
                    delete globalThis.__pendingFetch;
                }
            """.trimIndent())
            
            Log.debug("AndroidJSEngine: Fetch completed successfully")
        } catch (e: Exception) {
            Log.error("AndroidJSEngine: Error processing fetch: ${e.message}", e)
        }
    }
    
    private fun parseNovelList(json: String): List<PluginNovel> {
        return try {
            val array = Json.parseToJsonElement(json).jsonArray
            array.map { element ->
                val obj = element.jsonObject
                PluginNovel(
                    name = obj["name"]?.jsonPrimitive?.content ?: "",
                    url = obj["url"]?.jsonPrimitive?.content ?: "",
                    cover = obj["cover"]?.jsonPrimitive?.content ?: ""
                )
            }
        } catch (e: Exception) {
            Log.error("AndroidPluginWrapper: Error parsing novel list: ${e.message}", e)
            emptyList()
        }
    }
    
    private fun parseNovelDetails(json: String): PluginNovelDetails {
        return try {
            val obj = Json.parseToJsonElement(json).jsonObject
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
            Log.error("AndroidPluginWrapper: Error parsing novel details: ${e.message}", e)
            PluginNovelDetails("", "", "", null, null, emptyList(), null)
        }
    }
    
    private fun parseChapterList(json: String): List<PluginChapter> {
        return try {
            val array = Json.parseToJsonElement(json).jsonArray
            array.map { element ->
                val obj = element.jsonObject
                PluginChapter(
                    name = obj["name"]?.jsonPrimitive?.content ?: "",
                    url = obj["url"]?.jsonPrimitive?.content ?: "",
                    releaseTime = obj["releaseTime"]?.jsonPrimitive?.content
                )
            }
        } catch (e: Exception) {
            Log.error("AndroidPluginWrapper: Error parsing chapter list: ${e.message}", e)
            emptyList()
        }
    }
}

/**

 * Extension function to convert JsonObject to Map<String, Any>
 */
private fun kotlinx.serialization.json.JsonObject.toMap(): Map<String, Any> {
    val result = mutableMapOf<String, Any>()
    for ((key, value) in this) {
        result[key] = when (value) {
            is kotlinx.serialization.json.JsonPrimitive -> {
                when {
                    value.isString -> value.content
                    value.content == "true" || value.content == "false" -> value.content.toBoolean()
                    value.content.toIntOrNull() != null -> value.content.toInt()
                    value.content.toDoubleOrNull() != null -> value.content.toDouble()
                    else -> value.content
                }
            }
            is kotlinx.serialization.json.JsonObject -> value.toMap()
            is kotlinx.serialization.json.JsonArray -> value.map { element ->
                when (element) {
                    is kotlinx.serialization.json.JsonPrimitive -> element.content
                    is kotlinx.serialization.json.JsonObject -> element.toMap()
                    is kotlinx.serialization.json.JsonArray -> element.toString()
                    else -> element.toString()
                }
            }
            else -> value.toString()
        }
    }
    return result
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
