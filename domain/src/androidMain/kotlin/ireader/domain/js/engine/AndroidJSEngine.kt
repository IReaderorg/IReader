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
            
            // Cheerio stub with DOM manipulation support
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
                        result.append = function() { return result; };
                        result.prepend = function() { return result; };
                        result.after = function() { return result; };
                        result.before = function() { return result; };
                        result.remove = function() { return result; };
                        result.empty = function() { return result; };
                        return result;
                    };
                };
                globalThis.cheerio = cheerio;
            })();
            
            // Document polyfill for DOM manipulation
            ${getDocumentPolyfill()}
            
            // Add DOM methods to Object prototype as fallback for bundled code
            // This ensures any object can use append/prepend methods
            (function() {
                if (typeof Object.prototype.append === 'undefined') {
                    Object.defineProperty(Object.prototype, 'append', {
                        value: function() {
                            // If this object has children array, use it
                            if (Array.isArray(this.children)) {
                                for (var i = 0; i < arguments.length; i++) {
                                    var child = arguments[i];
                                    if (typeof child === 'string') {
                                        if (typeof this.textContent === 'string') {
                                            this.textContent += child;
                                        }
                                    } else {
                                        this.children.push(child);
                                    }
                                }
                            }
                            // Otherwise, initialize children array
                            else {
                                if (!this.children) this.children = [];
                                if (!this.textContent) this.textContent = '';
                                for (var i = 0; i < arguments.length; i++) {
                                    var child = arguments[i];
                                    if (typeof child === 'string') {
                                        this.textContent += child;
                                    } else {
                                        this.children.push(child);
                                    }
                                }
                            }
                            return this;
                        },
                        writable: true,
                        configurable: true,
                        enumerable: false
                    });
                }
                
                if (typeof Object.prototype.prepend === 'undefined') {
                    Object.defineProperty(Object.prototype, 'prepend', {
                        value: function() {
                            if (!this.children) this.children = [];
                            if (!this.textContent) this.textContent = '';
                            for (var i = arguments.length - 1; i >= 0; i--) {
                                var child = arguments[i];
                                if (typeof child === 'string') {
                                    this.textContent = child + this.textContent;
                                } else {
                                    this.children.unshift(child);
                                }
                            }
                            return this;
                        },
                        writable: true,
                        configurable: true,
                        enumerable: false
                    });
                }
                
                if (typeof Object.prototype.appendChild === 'undefined') {
                    Object.defineProperty(Object.prototype, 'appendChild', {
                        value: function(child) {
                            if (!this.children) this.children = [];
                            this.children.push(child);
                            return child;
                        },
                        writable: true,
                        configurable: true,
                        enumerable: false
                    });
                }
            })();
            
            // Promise polyfill that works with J2V8's limitations
            ${getPromisePolyfill()}
            
            // Async generator runtime support for compiled async/await
            ${getAsyncGeneratorSupport()}
            
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
                    
                    this.entries = function() {
                        var entries = [];
                        for (var key in this.params) {
                            if (this.params.hasOwnProperty(key)) {
                                this.params[key].forEach(function(value) {
                                    entries.push([key, value]);
                                });
                            }
                        }
                        return entries;
                    };
                    
                    this.keys = function() {
                        return Object.keys(this.params);
                    };
                    
                    this.values = function() {
                        var values = [];
                        for (var key in this.params) {
                            if (this.params.hasOwnProperty(key)) {
                                values.push.apply(values, this.params[key]);
                            }
                        }
                        return values;
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
                        var entries = [];
                        for (var key in this.headers) {
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
                        var values = [];
                        for (var key in this.headers) {
                            if (this.headers.hasOwnProperty(key)) {
                                values.push(this.headers[key]);
                            }
                        }
                        return values;
                    };
                    
                    this.forEach = function(callback, thisArg) {
                        for (var key in this.headers) {
                            if (this.headers.hasOwnProperty(key)) {
                                callback.call(thisArg, this.headers[key], key, this);
                            }
                        }
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
    
    private fun getPromisePolyfill(): String {
        return """
            // Override native Promise to work with J2V8's limitations
            (function() {
                var NativePromise = globalThis.Promise;
                var pendingPromises = [];
                
                // Microtask queue simulation for async behavior
                var microtaskQueue = [];
                var isProcessingQueue = false;
                
                function scheduleMicrotask(fn) {
                    microtaskQueue.push(fn);
                }
                
                function processMicrotasks() {
                    if (isProcessingQueue) return 0;
                    if (microtaskQueue.length === 0) return 0;
                    
                    isProcessingQueue = true;
                    var tasksToProcess = microtaskQueue.slice();
                    var count = tasksToProcess.length;
                    microtaskQueue = [];
                    
                    for (var i = 0; i < tasksToProcess.length; i++) {
                        try {
                            tasksToProcess[i]();
                        } catch (e) {
                            console.error('Microtask error:', e);
                        }
                    }
                    
                    isProcessingQueue = false;
                    
                    // Process any new tasks that were added
                    if (microtaskQueue.length > 0) {
                        count += processMicrotasks();
                    }
                    
                    return count;
                }
                
                globalThis.__processMicrotasks = processMicrotasks;
                
                globalThis.Promise = function(executor) {
                    var self = this;
                    self._state = 'pending';
                    self._value = undefined;
                    self._handlers = [];
                    self._id = Math.random().toString(36).substr(2, 9);
                    
                    function resolve(value) {
                        if (self._state !== 'pending') return;
                        
                        // Prevent resolving with self
                        if (value === self) {
                            reject(new TypeError('Cannot resolve promise with itself'));
                            return;
                        }
                        
                        // If resolving with a thenable, adopt its state
                        if (value && typeof value.then === 'function') {
                            // Mark as resolving to prevent re-entrance
                            self._state = 'resolving';
                            try {
                                var called = false;
                                value.then(
                                    function(v) {
                                        if (called) return;
                                        called = true;
                                        self._state = 'pending'; // Reset to allow resolve to work
                                        resolve(v);
                                    },
                                    function(r) {
                                        if (called) return;
                                        called = true;
                                        self._state = 'pending'; // Reset to allow reject to work
                                        reject(r);
                                    }
                                );
                            } catch (e) {
                                if (!called) {
                                    self._state = 'pending';
                                    reject(e);
                                }
                            }
                            return;
                        }
                        
                        // Fulfill with the value
                        self._state = 'fulfilled';
                        self._value = value;
                        
                        // Debug: log resolution
                        if (globalThis.__debugPromiseResolution) {
                            globalThis.__consoleLog = 'Promise ' + self._id + ' resolved with ' + typeof value + ', ' + self._handlers.length + ' handlers';
                        }
                        
                        // Process all pending handlers as microtasks
                        var handlers = self._handlers.slice();
                        self._handlers = [];
                        
                        handlers.forEach(function(handler) {
                            scheduleMicrotask(function() {
                                try {
                                    if (handler.onFulfilled) {
                                        var result = handler.onFulfilled(value);
                                        // If handler returns a thenable, chain to it
                                        if (result && typeof result.then === 'function') {
                                            result.then(handler.resolve, handler.reject);
                                        } else {
                                            handler.resolve(result);
                                        }
                                    } else {
                                        handler.resolve(value);
                                    }
                                } catch (e) {
                                    handler.reject(e);
                                }
                            });
                        });
                    }
                    
                    function reject(reason) {
                        if (self._state !== 'pending') return;
                        self._state = 'rejected';
                        self._value = reason;
                        self._handlers.forEach(function(handler) {
                            if (handler.onRejected) {
                                try {
                                    var result = handler.onRejected(reason);
                                    handler.resolve(result);
                                } catch (e) {
                                    handler.reject(e);
                                }
                            } else {
                                handler.reject(reason);
                            }
                        });
                        self._handlers = [];
                    }
                    
                    try {
                        executor(resolve, reject);
                    } catch (e) {
                        reject(e);
                    }
                };
                
                globalThis.Promise.prototype.then = function(onFulfilled, onRejected) {
                    var self = this;
                    return new Promise(function(resolve, reject) {
                        function handle() {
                            var state = self._state;
                            
                            // Handle 'resolving' state as pending
                            if (state === 'resolving') {
                                state = 'pending';
                            }
                            
                            if (state === 'fulfilled') {
                                // Execute immediately if already fulfilled
                                if (onFulfilled) {
                                    try {
                                        var result = onFulfilled(self._value);
                                        // If result is a thenable, chain to it
                                        if (result && typeof result.then === 'function') {
                                            result.then(resolve, reject);
                                        } else {
                                            resolve(result);
                                        }
                                    } catch (e) {
                                        reject(e);
                                    }
                                } else {
                                    resolve(self._value);
                                }
                            } else if (state === 'rejected') {
                                // Execute immediately if already rejected
                                if (onRejected) {
                                    try {
                                        var result = onRejected(self._value);
                                        // If result is a thenable, chain to it
                                        if (result && typeof result.then === 'function') {
                                            result.then(resolve, reject);
                                        } else {
                                            resolve(result);
                                        }
                                    } catch (e) {
                                        reject(e);
                                    }
                                } else {
                                    reject(self._value);
                                }
                            } else {
                                // Still pending or resolving, add to handlers
                                self._handlers.push({
                                    onFulfilled: onFulfilled,
                                    onRejected: onRejected,
                                    resolve: resolve,
                                    reject: reject
                                });
                            }
                        }
                        // Execute handle immediately (synchronously)
                        handle();
                    });
                };
                
                globalThis.Promise.prototype.catch = function(onRejected) {
                    return this.then(null, onRejected);
                };
                
                globalThis.Promise.resolve = function(value) {
                    return new Promise(function(resolve) {
                        resolve(value);
                    });
                };
                
                globalThis.Promise.reject = function(reason) {
                    return new Promise(function(resolve, reject) {
                        reject(reason);
                    });
                };
                
                globalThis.Promise.all = function(promises) {
                    return new Promise(function(resolve, reject) {
                        if (!Array.isArray(promises)) {
                            reject(new TypeError('Promise.all requires an array'));
                            return;
                        }
                        var results = new Array(promises.length);
                        var remaining = promises.length;
                        if (remaining === 0) {
                            resolve(results);
                            return;
                        }
                        var hasRejected = false;
                        promises.forEach(function(promise, index) {
                            Promise.resolve(promise).then(function(value) {
                                if (hasRejected) return;
                                results[index] = value;
                                remaining--;
                                if (remaining === 0) {
                                    resolve(results);
                                }
                            }).catch(function(error) {
                                if (hasRejected) return;
                                hasRejected = true;
                                reject(error);
                            });
                        });
                    });
                };
                
                globalThis.Promise.race = function(promises) {
                    return new Promise(function(resolve, reject) {
                        if (!Array.isArray(promises)) {
                            reject(new TypeError('Promise.race requires an array'));
                            return;
                        }
                        promises.forEach(function(promise) {
                            Promise.resolve(promise).then(resolve).catch(reject);
                        });
                    });
                };
            })();
        """.trimIndent()
    }
    
    private fun getAsyncGeneratorSupport(): String {
        return """
            // Patch for TypeScript/Babel compiled async/await generator runtime
            // This makes the generator state machine work with our Promise polyfill
            (function() {
                // The compiled async/await uses a generator-based state machine
                // Pattern: a(this, void 0, void 0, function() { return l(this, function(l2) { ... }) })
                // Where 'a' is the async wrapper and 'l' is the generator stepper
                
                // We need to ensure that when a generator yields a promise,
                // the stepper continues after the promise resolves
                
                // Patch: Wrap the generator stepper to ensure proper continuation
                var originalSetTimeout = globalThis.setTimeout;
                var originalSetImmediate = globalThis.setImmediate;
                
                // Create a simple task scheduler for generator continuation
                var scheduledTasks = [];
                var isProcessingTasks = false;
                
                function scheduleTask(fn) {
                    scheduledTasks.push(fn);
                    if (!isProcessingTasks) {
                        processScheduledTasks();
                    }
                }
                
                function processScheduledTasks() {
                    if (isProcessingTasks) return;
                    if (scheduledTasks.length === 0) return;
                    
                    isProcessingTasks = true;
                    while (scheduledTasks.length > 0) {
                        var task = scheduledTasks.shift();
                        try {
                            task();
                        } catch (e) {
                            console.error('Scheduled task error:', e);
                        }
                    }
                    isProcessingTasks = false;
                }
                
                globalThis.__processScheduledTasks = processScheduledTasks;
                
                // Override setImmediate to use our scheduler
                globalThis.setImmediate = function(fn) {
                    scheduleTask(fn);
                    return 0;
                };
                
                // Ensure setTimeout with 0 delay uses our scheduler
                globalThis.setTimeout = function(fn, delay) {
                    if (delay === 0 || delay === undefined) {
                        scheduleTask(fn);
                        return 0;
                    }
                    return originalSetTimeout ? originalSetTimeout(fn, delay) : 0;
                };
                
                // Patch Promise.prototype.then to ensure generator continuation
                var OriginalPromiseThen = globalThis.Promise.prototype.then;
                globalThis.Promise.prototype.then = function(onFulfilled, onRejected) {
                    var self = this;
                    
                    // Wrap handlers to ensure they execute via scheduler
                    var wrappedOnFulfilled = onFulfilled ? function(value) {
                        var result;
                        try {
                            result = onFulfilled(value);
                        } catch (e) {
                            throw e;
                        }
                        return result;
                    } : undefined;
                    
                    var wrappedOnRejected = onRejected ? function(reason) {
                        var result;
                        try {
                            result = onRejected(reason);
                        } catch (e) {
                            throw e;
                        }
                        return result;
                    } : undefined;
                    
                    return OriginalPromiseThen.call(self, wrappedOnFulfilled, wrappedOnRejected);
                };
                
                // Helper to detect if something is a thenable
                globalThis.__isThenable = function(obj) {
                    return obj && typeof obj.then === 'function';
                };
                
                // Ensure Promise.resolve works correctly
                if (!globalThis.Promise.resolve) {
                    globalThis.Promise.resolve = function(value) {
                        if (value instanceof globalThis.Promise) {
                            return value;
                        }
                        return new globalThis.Promise(function(resolve) {
                            resolve(value);
                        });
                    };
                }
                
                // Ensure Promise.reject works correctly
                if (!globalThis.Promise.reject) {
                    globalThis.Promise.reject = function(reason) {
                        return new globalThis.Promise(function(resolve, reject) {
                            reject(reason);
                        });
                    };
                }
            })();
        """.trimIndent()
    }
    
    private fun getDocumentPolyfill(): String {
        return """
            // Basic DOM Element polyfill
            if (typeof Element === 'undefined') {
                globalThis.Element = function() {
                    this.children = [];
                    this.innerHTML = '';
                    this.textContent = '';
                    this.attributes = {};
                    
                    this.appendChild = function(child) {
                        if (!this.children) this.children = [];
                        this.children.push(child);
                        return child;
                    };
                    
                    this.append = function() {
                        if (!this.children) this.children = [];
                        if (!this.textContent) this.textContent = '';
                        for (var i = 0; i < arguments.length; i++) {
                            var child = arguments[i];
                            if (typeof child === 'string') {
                                this.textContent += child;
                            } else {
                                this.children.push(child);
                            }
                        }
                    };
                    
                    this.prepend = function() {
                        if (!this.children) this.children = [];
                        if (!this.textContent) this.textContent = '';
                        for (var i = arguments.length - 1; i >= 0; i--) {
                            var child = arguments[i];
                            if (typeof child === 'string') {
                                this.textContent = child + this.textContent;
                            } else {
                                this.children.unshift(child);
                            }
                        }
                    };
                    
                    this.removeChild = function(child) {
                        if (!this.children) this.children = [];
                        var index = this.children.indexOf(child);
                        if (index > -1) {
                            this.children.splice(index, 1);
                        }
                        return child;
                    };
                    
                    this.remove = function() {
                        if (this.parentNode) {
                            this.parentNode.removeChild(this);
                        }
                    };
                    
                    this.getAttribute = function(name) {
                        if (!this.attributes) this.attributes = {};
                        return this.attributes[name] || null;
                    };
                    
                    this.setAttribute = function(name, value) {
                        if (!this.attributes) this.attributes = {};
                        this.attributes[name] = String(value);
                    };
                    
                    this.querySelector = function() {
                        return null;
                    };
                    
                    this.querySelectorAll = function() {
                        return [];
                    };
                };
            }
            
            // Basic Document polyfill
            if (typeof document === 'undefined') {
                globalThis.document = {
                    createElement: function(tagName) {
                        var el = new Element();
                        el.tagName = String(tagName).toUpperCase();
                        el.nodeName = el.tagName;
                        return el;
                    },
                    createTextNode: function(text) {
                        return { nodeValue: String(text), textContent: String(text) };
                    },
                    querySelector: function() {
                        return null;
                    },
                    querySelectorAll: function() {
                        return [];
                    },
                    getElementById: function() {
                        return null;
                    },
                    getElementsByTagName: function() {
                        return [];
                    },
                    getElementsByClassName: function() {
                        return [];
                    }
                };
            }
            
            // Polyfill for DOMParser (used by some plugins)
            if (typeof DOMParser === 'undefined') {
                globalThis.DOMParser = function() {
                    this.parseFromString = function(str, type) {
                        // Return a minimal document-like object
                        var doc = {
                            documentElement: new Element(),
                            createElement: globalThis.document.createElement,
                            createTextNode: globalThis.document.createTextNode,
                            querySelector: function() { return null; },
                            querySelectorAll: function() { return []; },
                            getElementById: function() { return null; },
                            getElementsByTagName: function() { return []; },
                            getElementsByClassName: function() { return []; }
                        };
                        doc.documentElement.innerHTML = str;
                        return doc;
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
                wrapper.getLang = function() { return plugin.lang || plugin.language || "en"; };
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
                            console.log('getNovelDetails: Got novel data, keys=' + Object.keys(d || {}).join(','));
                            var details = {};
                            details.name = d.name || d.title || "";
                            details.url = d.url || d.path || url;
                            details.cover = d.cover || d.image || "";
                            details.author = d.author || null;
                            details.description = d.description || d.summary || null;
                            details.genres = Array.isArray(d.genres) ? d.genres : [];
                            details.status = d.status || null;
                            console.log('getNovelDetails: Mapped to details, name=' + details.name);
                            return details;
                        }).catch(function(e) {
                            console.error('getNovelDetails error: ' + (e.message || e));
                            throw e;
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
                        globalThis.__consoleLog = 'getChapters: Calling parseNovel for ' + url;
                        return Promise.resolve(plugin.parseNovel(url)).then(function(novel) {
                            globalThis.__consoleLog = 'getChapters: Got novel, type=' + typeof novel + ', keys=' + (novel ? Object.keys(novel).join(',') : 'null');
                            if (novel && Array.isArray(novel.chapters)) {
                                globalThis.__consoleLog = 'getChapters: Found ' + novel.chapters.length + ' chapters';
                                return novel.chapters.map(function(c) {
                                    var chapter = {};
                                    chapter.name = c.name || c.title || "";
                                    chapter.url = c.url || c.path || "";
                                    chapter.releaseTime = c.releaseTime || c.date || null;
                                    return chapter;
                                });
                            }
                            globalThis.__consoleLog = 'getChapters: No chapters found, novel=' + JSON.stringify(novel).substring(0, 200);
                            return [];
                        }).catch(function(e) {
                            globalThis.__consoleError = 'getChapters error: ' + (e.message || e);
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
    
    override suspend fun popularNovels(page: Int, filters: Map<String, Any>): List<PluginNovel> = withContext(v8Thread) {
        mutex.withLock {
            try {
                // If filters are provided, use them
                if (filters.isNotEmpty()) {
                    val filtersJson = kotlinx.serialization.json.Json.encodeToString(
                        kotlinx.serialization.json.JsonObject.serializer(),
                        filters.toJsonObject()
                    )
                    val resultJson = awaitPromise("__wrappedPlugin.popularNovels($page, { filters: $filtersJson })")
                    parseNovelList(resultJson)
                } else {
                    val resultJson = awaitPromise("__wrappedPlugin.popularNovels($page)")
                    parseNovelList(resultJson)
                }
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
    

    
    /**
     * Wait for a JavaScript Promise to resolve.
     * With V8, Promises work natively - we just need to handle fetch requests.
     */
    private suspend fun awaitPromise(jsExpression: String): String {
        val promiseId = System.currentTimeMillis()
        
        Log.debug("AndroidPluginWrapper: awaitPromise starting for expression (promiseId=$promiseId)")
        
        // Setup promise handlers using .then() instead of async/await
        // J2V8 doesn't automatically execute microtasks, so we use a different approach
        try {
            // Initialize debug log array for this promise
            engine.evaluateScript("if (!globalThis.__promiseDebugLogs_$promiseId) globalThis.__promiseDebugLogs_$promiseId = [];")
            
            engine.evaluateScript("""
                (function() {
                    var debugLog = function(msg) {
                        if (!globalThis.__promiseDebugLogs_$promiseId) globalThis.__promiseDebugLogs_$promiseId = [];
                        globalThis.__promiseDebugLogs_$promiseId.push(msg);
                    };
                    
                    try {
                        globalThis.__debugPromiseResolution = true;
                        debugLog('Starting execution');
                        var promise = ($jsExpression);
                        debugLog('Got promise object, type=' + typeof promise + ', isThenable=' + (promise && typeof promise.then === 'function'));
                        
                        if (promise && typeof promise.then === 'function') {
                            debugLog('Promise state: ' + (promise._state || 'unknown') + ', id: ' + (promise._id || 'unknown') + ', handlers: ' + (promise._handlers ? promise._handlers.length : 'unknown'));
                            var thenCalled = false;
                            debugLog('About to call .then()');
                            var thenResult = promise.then(function(result) {
                                debugLog('INSIDE then() callback - this should execute!');

                                if (thenCalled) {
                                    debugLog('ERROR: then() called multiple times!');
                                    return;
                                }
                                thenCalled = true;
                                debugLog('then() callback executing, result type=' + typeof result);
                                try {
                                    globalThis.__promiseResult_$promiseId = JSON.stringify(result);
                                    globalThis.__promiseStatus_$promiseId = 'resolved';
                                    debugLog('Resolved successfully');
                                } catch (stringifyError) {
                                    debugLog('JSON.stringify error: ' + stringifyError.message);
                                    globalThis.__promiseError_$promiseId = 'Failed to stringify result: ' + stringifyError.message;
                                    globalThis.__promiseStatus_$promiseId = 'rejected';
                                }
                            }, function(e) {
                                if (thenCalled) {
                                    debugLog('ERROR: catch() called after then()!');
                                    return;
                                }
                                thenCalled = true;
                                var errorMsg = e.message || String(e);
                                var errorStack = e.stack || '';
                                debugLog('Promise error: ' + errorMsg);
                                globalThis.__promiseError_$promiseId = errorMsg + (errorStack ? '\n' + errorStack : '');
                                globalThis.__promiseStatus_$promiseId = 'rejected';
                            });
                            debugLog('then() registered, returned promise state: ' + (thenResult._state || 'unknown'));
                        } else {
                            // Not a promise, return immediately
                            globalThis.__promiseResult_$promiseId = JSON.stringify(promise);
                            globalThis.__promiseStatus_$promiseId = 'resolved';
                            debugLog('Not a promise, resolved immediately');
                        }
                    } catch (e) {
                        var errorMsg = e.message || String(e);
                        var errorStack = e.stack || '';
                        debugLog('Sync error: ' + errorMsg);
                        globalThis.__promiseError_$promiseId = errorMsg + (errorStack ? '\n' + errorStack : '');
                        globalThis.__promiseStatus_$promiseId = 'rejected';
                    }
                })();
            """.trimIndent())
            
            Log.debug("AndroidPluginWrapper: Promise $promiseId setup complete, starting polling")
        } catch (e: Exception) {
            Log.error("AndroidPluginWrapper: Failed to setup promise: ${e.message}", e)
            throw e
        }
        
        val startTime = System.currentTimeMillis()
        val timeout = 30000L
        var lastLogTime = startTime
        
        while (System.currentTimeMillis() - startTime < timeout) {
            // Process any pending fetch requests
            processPendingFetch()
            
            // Process microtask queue (critical for Promise resolution)
            try {
                val processed = engine.evaluateScript("globalThis.__processMicrotasks()") as? Number
                if (processed != null && processed.toInt() > 0) {
                    Log.debug("AndroidPluginWrapper: Processed ${processed.toInt()} microtasks")
                }
            } catch (e: Exception) {
                Log.warn("AndroidPluginWrapper: Error processing microtasks: ${e.message}")
            }
            
            // Process scheduled tasks (for generator continuation)
            try {
                engine.evaluateScript("if (globalThis.__processScheduledTasks) globalThis.__processScheduledTasks();")
            } catch (e: Exception) {
                // Ignore
            }
            
            // Pump the event loop by executing a dummy script
            // This forces V8 to process pending microtasks (promises)
            try {
                engine.evaluateScript("void 0;")
            } catch (e: Exception) {
                // Ignore
            }
            
            // Log console messages
            val consoleLog = engine.evaluateScript("globalThis.__consoleLog") as? String
            if (consoleLog != null) {
                Log.debug("JS Console: $consoleLog")
                engine.evaluateScript("globalThis.__consoleLog = null;")
            }
            
            val consoleError = engine.evaluateScript("globalThis.__consoleError") as? String
            if (consoleError != null) {
                Log.error("JS Console Error: $consoleError")
                engine.evaluateScript("globalThis.__consoleError = null;")
            }
            
            // Check promise status
            val status = engine.evaluateScript("globalThis.__promiseStatus_$promiseId") as? String
            
            // Log progress every 5 seconds
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastLogTime > 5000) {
                // Get debug logs
                try {
                    val debugLogs = engine.evaluateScript("JSON.stringify(globalThis.__promiseDebugLogs_$promiseId || [])") as? String
                    if (debugLogs != null && debugLogs != "[]") {
                        Log.debug("AndroidPluginWrapper: Promise $promiseId debug logs: $debugLogs")
                    }
                } catch (e: Exception) {
                    // Ignore
                }
                Log.debug("AndroidPluginWrapper: Still waiting for promise $promiseId (${(currentTime - startTime) / 1000}s elapsed, status=$status)")
                lastLogTime = currentTime
            }
            
            when (status) {
                "resolved" -> {
                    val result = engine.evaluateScript("globalThis.__promiseResult_$promiseId") as? String ?: ""
                    engine.evaluateScript("""
                        delete globalThis.__promiseResult_$promiseId;
                        delete globalThis.__promiseStatus_$promiseId;
                        delete globalThis.__promiseDebugLogs_$promiseId;
                    """.trimIndent())
                    Log.debug("AndroidPluginWrapper: Promise $promiseId resolved successfully")
                    return result
                }
                "rejected" -> {
                    val error = engine.evaluateScript("globalThis.__promiseError_$promiseId") as? String ?: "Unknown error"
                    engine.evaluateScript("""
                        delete globalThis.__promiseError_$promiseId;
                        delete globalThis.__promiseStatus_$promiseId;
                        delete globalThis.__promiseDebugLogs_$promiseId;
                    """.trimIndent())
                    throw Exception("Promise rejected: $error")
                }
            }
            
            kotlinx.coroutines.delay(10) // Shorter delay since V8 is faster
        }
        
        // Cleanup on timeout
        try {
            val debugLogs = engine.evaluateScript("JSON.stringify(globalThis.__promiseDebugLogs_$promiseId || [])") as? String
            if (debugLogs != null && debugLogs != "[]") {
                Log.error("AndroidPluginWrapper: Promise $promiseId debug logs at timeout: $debugLogs")
            }
        } catch (e: Exception) {
            // Ignore
        }
        
        engine.evaluateScript("""
            delete globalThis.__promiseResult_$promiseId;
            delete globalThis.__promiseStatus_$promiseId;
            delete globalThis.__promiseError_$promiseId;
            delete globalThis.__promiseDebugLogs_$promiseId;
        """.trimIndent())
        
        Log.error("AndroidPluginWrapper: Promise $promiseId timed out after ${timeout}ms")
        throw Exception("Promise timeout after ${timeout}ms")
    }
    
    /**
     * Process a single pending fetch request if any.
     * With V8, we can directly resolve/reject the Promise from Kotlin!
     */
    private suspend fun processPendingFetch() {
        val bridge = bridgeService
        if (bridge == null) {
            Log.warn("AndroidJSEngine: No bridge service available for fetch")
            return
        }
        
        try {
            // Check if there's a pending fetch
            val fetchReady = engine.evaluateScript("globalThis.__fetchReady") as? Boolean
            if (fetchReady != true) {
                return
            }
            
            Log.debug("AndroidJSEngine: Fetch request detected, processing...")
            
            // Clear the flag
            engine.evaluateScript("globalThis.__fetchReady = false;")
            
            // Get fetch details
            val fetchJson = engine.evaluateScript("JSON.stringify(globalThis.__pendingFetch)") as? String
            if (fetchJson == null) {
                Log.error("AndroidJSEngine: Failed to get fetch details")
                return
            }
            
            val fetchData = Json.parseToJsonElement(fetchJson).jsonObject
            
            val url = fetchData["url"]?.jsonPrimitive?.content
            if (url == null) {
                Log.error("AndroidJSEngine: No URL in fetch request")
                return
            }
            
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
            
            // Reject the promise in JavaScript
            try {
                engine.evaluateScript("""
                    if (globalThis.__pendingFetch && globalThis.__pendingFetch.reject) {
                        globalThis.__pendingFetch.reject(new Error('Fetch failed: ${e.message?.replace("'", "\\'")}'));
                        delete globalThis.__pendingFetch;
                    }
                """.trimIndent())
            } catch (rejectError: Exception) {
                Log.error("AndroidJSEngine: Failed to reject promise: ${rejectError.message}")
            }
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
            Log.debug("AndroidPluginWrapper: Parsing novel details JSON (${json.length} chars)")
            val obj = Json.parseToJsonElement(json).jsonObject
            val details = PluginNovelDetails(
                name = obj["name"]?.jsonPrimitive?.content ?: "",
                url = obj["url"]?.jsonPrimitive?.content ?: "",
                cover = obj["cover"]?.jsonPrimitive?.content ?: "",
                author = obj["author"]?.jsonPrimitive?.content,
                description = obj["description"]?.jsonPrimitive?.content,
                genres = obj["genres"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                status = obj["status"]?.jsonPrimitive?.content
            )
            Log.debug("AndroidPluginWrapper: Parsed novel: ${details.name}, chapters in JSON: ${obj.containsKey("chapters")}")
            details
        } catch (e: Exception) {
            Log.error("AndroidPluginWrapper: Error parsing novel details: ${e.message}", e)
            Log.error("AndroidPluginWrapper: JSON was: ${json.take(500)}")
            PluginNovelDetails("", "", "", null, null, emptyList(), null)
        }
    }
    
    private fun parseChapterList(json: String): List<PluginChapter> {
        return try {
            Log.debug("AndroidPluginWrapper: Parsing chapter list JSON (${json.length} chars)")
            if (json.length < 500) {
                Log.debug("AndroidPluginWrapper: Chapter JSON: $json")
            }
            val array = Json.parseToJsonElement(json).jsonArray
            val chapters = array.map { element ->
                val obj = element.jsonObject
                PluginChapter(
                    name = obj["name"]?.jsonPrimitive?.content ?: "",
                    url = obj["url"]?.jsonPrimitive?.content ?: "",
                    releaseTime = obj["releaseTime"]?.jsonPrimitive?.content
                )
            }
            Log.debug("AndroidPluginWrapper: Parsed ${chapters.size} chapters")
            chapters
        } catch (e: Exception) {
            Log.error("AndroidPluginWrapper: Error parsing chapter list: ${e.message}", e)
            Log.error("AndroidPluginWrapper: JSON was: ${json.take(500)}")
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
