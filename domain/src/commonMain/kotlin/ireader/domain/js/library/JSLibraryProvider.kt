package ireader.domain.js.library

import io.ktor.client.HttpClient
import ireader.core.prefs.PreferenceStore
import ireader.domain.js.engine.JSEngine

/**
 * Provides JavaScript libraries and APIs to plugins.
 * Sets up the require() function and injects global APIs like fetch, storage, etc.
 */
class JSLibraryProvider(
    private val engine: JSEngine,
    private val pluginId: String,
    private val httpClient: HttpClient,
    private val preferenceStore: PreferenceStore
) {
    
    // Create FlareSolverr client for Cloudflare bypass with extended timeout
    private val flareSolverrClient = ireader.domain.http.FlareSolverrClient(
        httpClient = httpClient,
        maxTimeout = 120000 // 2 minutes for complex challenges like wuxiaworld.site
    )
    
    private val fetchApi = JSFetchApi(httpClient, pluginId, flareSolverrClient = flareSolverrClient)
    private val storage = JSStorage(preferenceStore, pluginId)
    private val localStorage = JSLocalStorage()
    private val sessionStorage = JSSessionStorage()
    private val cheerioApi = JSCheerioApi(pluginId)
    
    /**
     * Sets up the require() function and injects global APIs.
     */
    fun setupRequireFunction() {
        try {
            // Inject native fetch function
            engine.setGlobalObject("__nativeFetch", fetchApi)
            
            // Inject storage objects
            engine.setGlobalObject("__nativeStorage", storage)
            engine.setGlobalObject("__nativeLocalStorage", localStorage)
            engine.setGlobalObject("__nativeSessionStorage", sessionStorage)
            
            // Inject cheerio API
            engine.setGlobalObject("__nativeCheerio", cheerioApi)
            
            // First, setup global APIs
            val globalSetup = """
                (function() {
                    // Wrap native cheerio to add JavaScript-side .each() and .map() support
                    if (typeof __nativeCheerio !== 'undefined') {
                        const originalLoad = __nativeCheerio.load.bind(__nativeCheerio);
                        __nativeCheerio.load = function(html) {
                            const $ = originalLoad(html);
                            
                            // Wrap the selector function to add .each() and .map() support
                            const originalSelector = $;
                            const wrappedSelector = function(selector) {
                                const selection = originalSelector(selector);
                                
                                // Add .each() method that works with JavaScript callbacks
                                const originalEach = selection.each;
                                selection.each = function(callback) {
                                    // Iterate over elements
                                    for (let i = 0; i < selection.length; i++) {
                                        const element = selection.eq ? selection.eq(i) : selection.get(i);
                                        callback.call(element, i, element);
                                    }
                                    return selection;
                                };
                                
                                // Add .map() method that works with JavaScript callbacks
                                const originalMap = selection.map;
                                selection.map = function(callback) {
                                    const results = [];
                                    // Iterate over elements and collect callback results
                                    for (let i = 0; i < selection.length; i++) {
                                        const element = selection.eq ? selection.eq(i) : selection.get(i);
                                        const result = callback.call(element, i, element);
                                        if (result !== null && result !== undefined) {
                                            results.push(result);
                                        }
                                    }
                                    // Return an object with get() method like Cheerio does
                                    return {
                                        get: function() { return results; },
                                        toArray: function() { return results; },
                                        length: results.length
                                    };
                                };
                                
                                return selection;
                            };
                            
                            // Copy properties from original selector
                            for (const key in originalSelector) {
                                if (originalSelector.hasOwnProperty(key)) {
                                    wrappedSelector[key] = originalSelector[key];
                                }
                            }
                            
                            return wrappedSelector;
                        };
                    }
                    
                    // Verify native storage is available
                    if (typeof __nativeStorage === 'undefined') {
                        throw new Error('__nativeStorage is not available');
                    }
                    
                    // Setup Storage API first (before modules that depend on it)
                    globalThis.storage = {
                        set: function(key, value, expires) {
                            __nativeStorage.set(key, JSON.stringify(value), expires);
                        },
                        get: function(key) {
                            const value = __nativeStorage.get(key);
                            return value ? JSON.parse(value) : null;
                        },
                        delete: function(key) {
                            __nativeStorage.delete(key);
                        },
                        clearAll: function() {
                            __nativeStorage.clearAll();
                        },
                        getAllKeys: function() {
                            return __nativeStorage.getAllKeys();
                        }
                    };
                    
                    // Setup LocalStorage API
                    globalThis.localStorage = {
                        setItem: function(key, value) {
                            __nativeLocalStorage.set(key, value);
                        },
                        getItem: function(key) {
                            return __nativeLocalStorage.get(key);
                        },
                        removeItem: function(key) {
                            __nativeLocalStorage.delete(key);
                        },
                        clear: function() {
                            __nativeLocalStorage.clearAll();
                        }
                    };
                    
                    // Setup SessionStorage API
                    globalThis.sessionStorage = {
                        setItem: function(key, value) {
                            __nativeSessionStorage.set(key, value);
                        },
                        getItem: function(key) {
                            return __nativeSessionStorage.get(key);
                        },
                        removeItem: function(key) {
                            __nativeSessionStorage.delete(key);
                        },
                        clear: function() {
                            __nativeSessionStorage.clearAll();
                        }
                    };
                    
                    // Setup fetch API with comprehensive response object
                    globalThis.fetch = function(url, init) {
                        const requestUrl = String(url || '');
                        const result = __nativeFetch.fetch(requestUrl, init || {});
                        
                        // Create a proper Response-like object with all properties
                        const response = {
                            ok: Boolean(result.ok),
                            status: Number(result.status) || 0,
                            statusText: String(result.statusText || ''),
                            url: String(result.url || requestUrl),  // Ensure URL is always a string
                            headers: result.headers || {},
                            redirected: false,
                            type: 'basic',
                            text: function() { 
                                return Promise.resolve(String(result.text || '')); 
                            },
                            json: function() { 
                                try {
                                    return Promise.resolve(JSON.parse(result.text || '{}')); 
                                } catch (e) {
                                    return Promise.reject(new Error('Invalid JSON: ' + e.message));
                                }
                            },
                            blob: function() { 
                                return Promise.resolve(new Blob([result.text || ''])); 
                            },
                            arrayBuffer: function() { 
                                return Promise.resolve(new ArrayBuffer(0)); 
                            },
                            clone: function() { 
                                return response; 
                            }
                        };
                        
                        return Promise.resolve(response);
                    };
                    
                    // Setup window and location objects for browser compatibility
                    globalThis.window = globalThis;
                    
                    // Create a proper location object with all properties as strings
                    const createLocation = function(url) {
                        return {
                            href: String(url || 'about:blank'),
                            protocol: 'about:',
                            host: 'blank',
                            hostname: 'blank',
                            port: '',
                            pathname: '/blank',
                            search: '',
                            hash: '',
                            origin: 'about:blank',
                            toString: function() { return this.href; }
                        };
                    };
                    
                    globalThis.location = createLocation('about:blank');
                    
                    // Setup document object with location reference
                    globalThis.document = {
                        location: globalThis.location,
                        URL: 'about:blank',
                        domain: 'blank',
                        referrer: '',
                        title: '',
                        cookie: '',
                        documentURI: 'about:blank',
                        baseURI: 'about:blank'
                    };
                    
                    // Setup URL API polyfill with comprehensive error handling
                    globalThis.URL = function(url, base) {
                        // Handle null/undefined inputs
                        if (url === null || url === undefined) {
                            throw new Error('Invalid URL: URL cannot be null or undefined');
                        }
                        
                        // Convert to string
                        url = String(url);
                        
                        // Parse URL
                        let fullUrl = url;
                        if (base && !url.match(/^https?:\/\//)) {
                            // Relative URL - combine with base
                            base = String(base);
                            if (url.startsWith('/')) {
                                // Absolute path
                                const baseMatch = base.match(/^(https?:\/\/[^\/]+)/);
                                fullUrl = baseMatch ? baseMatch[1] + url : url;
                            } else {
                                // Relative path
                                const basePath = base.replace(/\/[^\/]*$/, '/');
                                fullUrl = basePath + url;
                            }
                        }
                        
                        // Parse the URL - more flexible regex to handle edge cases
                        const match = fullUrl.match(/^(https?):\/\/([^\/\?#]+)(\/[^\?#]*)?(\\?[^#]*)?(#.*)?$/);
                        if (!match) {
                            throw new Error('Invalid URL: ' + fullUrl);
                        }
                        
                        const protocol = match[1] || 'http';
                        const hostWithPort = match[2] || '';
                        const pathname = match[3] || '/';
                        const search = match[4] || '';
                        const hash = match[5] || '';
                        
                        // Parse host and port - ensure we always have strings
                        const hostParts = (hostWithPort || '').split(':');
                        const hostname = hostParts[0] || '';
                        const port = hostParts[1] || '';
                        
                        // Set all properties with safe defaults - ensure all are strings
                        this.protocol = String(protocol) + ':';
                        this.host = String(hostWithPort);
                        this.hostname = String(hostname);
                        this.port = String(port);
                        this.pathname = String(pathname);
                        this.search = String(search);
                        this.hash = String(hash);
                        this.href = String(fullUrl);
                        this.origin = String(protocol) + '://' + String(hostWithPort);
                        
                        // Add methods that might be called
                        this.toString = function() { return this.href; };
                        this.toJSON = function() { return this.href; };
                    };
                    
                    // Setup URLSearchParams for query string manipulation
                    globalThis.URLSearchParams = function(init) {
                        this.params = {};
                        
                        // Parse initialization
                        if (typeof init === 'string') {
                            // Parse query string
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
                            // Parse object
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
                        
                        this.entries = function() {
                            const entries = [];
                            for (const key in this.params) {
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
                            const values = [];
                            for (const key in this.params) {
                                if (this.params.hasOwnProperty(key)) {
                                    values.push(...this.params[key]);
                                }
                            }
                            return values;
                        };
                    };
                    
                    // Setup Blob constructor for fetch API compatibility
                    globalThis.Blob = function(parts, options) {
                        this.parts = parts || [];
                        this.options = options || {};
                        this.size = this.parts.reduce(function(acc, part) {
                            return acc + (part.length || 0);
                        }, 0);
                        this.type = this.options.type || '';
                    };
                    
                    // Setup ArrayBuffer for fetch API compatibility
                    if (typeof globalThis.ArrayBuffer === 'undefined') {
                        globalThis.ArrayBuffer = function(length) {
                            this.byteLength = length || 0;
                        };
                    }
                    
                    // Setup FormData for POST requests
                    globalThis.FormData = function() {
                        this.data = {};
                        this.append = function(key, value) {
                            if (!this.data[key]) {
                                this.data[key] = [];
                            }
                            this.data[key].push(value);
                        };
                        this.get = function(key) {
                            return this.data[key] ? this.data[key][0] : null;
                        };
                        this.getAll = function(key) {
                            return this.data[key] || [];
                        };
                        this.has = function(key) {
                            return key in this.data;
                        };
                        this.delete = function(key) {
                            delete this.data[key];
                        };
                        this.set = function(key, value) {
                            this.data[key] = [value];
                        };
                        this.entries = function() {
                            const entries = [];
                            for (const key in this.data) {
                                for (const value of this.data[key]) {
                                    entries.push([key, value]);
                                }
                            }
                            return entries;
                        };
                    };
                    
                    // Setup atob and btoa for base64 encoding/decoding (pure JavaScript implementation)
                    if (typeof globalThis.atob === 'undefined') {
                        globalThis.atob = function(str) {
                            // Base64 decode - pure JavaScript implementation
                            const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
                            let output = '';
                            str = String(str).replace(/=+$/, '');
                            
                            if (str.length % 4 === 1) {
                                throw new Error('Invalid base64 string');
                            }
                            
                            for (let i = 0; i < str.length;) {
                                const enc1 = chars.indexOf(str.charAt(i++));
                                const enc2 = chars.indexOf(str.charAt(i++));
                                const enc3 = chars.indexOf(str.charAt(i++));
                                const enc4 = chars.indexOf(str.charAt(i++));
                                
                                if (enc1 === -1 || enc2 === -1) {
                                    throw new Error('Invalid base64 string');
                                }
                                
                                const chr1 = (enc1 << 2) | (enc2 >> 4);
                                const chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
                                const chr3 = ((enc3 & 3) << 6) | enc4;
                                
                                output += String.fromCharCode(chr1);
                                if (enc3 !== 64 && enc3 !== -1) output += String.fromCharCode(chr2);
                                if (enc4 !== 64 && enc4 !== -1) output += String.fromCharCode(chr3);
                            }
                            return output;
                        };
                    }
                    
                    if (typeof globalThis.btoa === 'undefined') {
                        globalThis.btoa = function(str) {
                            // Base64 encode - pure JavaScript implementation
                            const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
                            let output = '';
                            str = String(str);
                            
                            for (let i = 0; i < str.length;) {
                                const chr1 = str.charCodeAt(i++);
                                const chr2 = str.charCodeAt(i++);
                                const chr3 = str.charCodeAt(i++);
                                
                                const enc1 = chr1 >> 2;
                                const enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
                                let enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
                                let enc4 = chr3 & 63;
                                
                                if (isNaN(chr2)) {
                                    enc3 = enc4 = 64;
                                } else if (isNaN(chr3)) {
                                    enc4 = 64;
                                }
                                
                                output += chars.charAt(enc1) + chars.charAt(enc2) + chars.charAt(enc3) + chars.charAt(enc4);
                            }
                            return output;
                        };
                    }
                    
                    // Setup TextEncoder and TextDecoder
                    if (typeof globalThis.TextEncoder === 'undefined') {
                        globalThis.TextEncoder = function() {
                            this.encode = function(str) {
                                const utf8 = unescape(encodeURIComponent(str));
                                const result = new Uint8Array(utf8.length);
                                for (let i = 0; i < utf8.length; i++) {
                                    result[i] = utf8.charCodeAt(i);
                                }
                                return result;
                            };
                        };
                    }
                    
                    if (typeof globalThis.TextDecoder === 'undefined') {
                        globalThis.TextDecoder = function() {
                            this.decode = function(buffer) {
                                const bytes = new Uint8Array(buffer);
                                let str = '';
                                for (let i = 0; i < bytes.length; i++) {
                                    str += String.fromCharCode(bytes[i]);
                                }
                                return decodeURIComponent(escape(str));
                            };
                        };
                    }
                    
                    // Setup Uint8Array if not available
                    if (typeof globalThis.Uint8Array === 'undefined') {
                        globalThis.Uint8Array = function(length) {
                            const arr = new Array(length);
                            for (let i = 0; i < length; i++) {
                                arr[i] = 0;
                            }
                            arr.buffer = new ArrayBuffer(length);
                            arr.byteLength = length;
                            return arr;
                        };
                    }
                    
                    // Add utility functions for URL parsing
                    globalThis.getHostname = function(url) {
                        if (!url) return '';
                        try {
                            const urlObj = new URL(url);
                            return urlObj.hostname || '';
                        } catch (e) {
                            // Fallback: try to extract hostname manually
                            const match = String(url).match(/^(?:https?:\/\/)?([^\/\?#:]+)/);
                            return match ? match[1] : '';
                        }
                    };
                    
                    // Add safe property access helper
                    globalThis.safeGet = function(obj, path, defaultValue) {
                        if (!obj || !path) return defaultValue;
                        const keys = path.split('.');
                        let result = obj;
                        for (const key of keys) {
                            if (result === null || result === undefined) {
                                return defaultValue;
                            }
                            result = result[key];
                        }
                        return result !== undefined ? result : defaultValue;
                    };
                    
                    // Patch Array.prototype to handle undefined gracefully
                    const originalArrayAt = Array.prototype.at;
                    if (originalArrayAt) {
                        Array.prototype.at = function(index) {
                            return originalArrayAt.call(this, index);
                        };
                    }
                    
                    // Add safe array access that returns empty string instead of undefined
                    Array.prototype.safeAt = function(index) {
                        const item = this[index];
                        return item !== undefined && item !== null ? item : '';
                    };
                    
                    // Initialize module registry
                    globalThis.__modules = {};
                })();
            """.trimIndent()
            
            engine.evaluateScript(globalSetup)
            
            // Load third-party libraries
            loadCheerioLibrary()
            loadDayjsLibrary()
            loadUrlencodeLibrary()
            loadHtmlParser2Library()
            
            // Setup LNReader-specific libraries and require function
            val moduleSetup = """
                (function() {
                    const modules = globalThis.__modules;
                    
                    // LNReader-specific libraries
                    modules['@libs/filterInputs'] = ${getFilterInputsLibrary()};
                    modules['@libs/defaultCover'] = ${getDefaultCoverLibrary()};
                    modules['@libs/fetch'] = ${getFetchLibrary()};
                    modules['@libs/novelStatus'] = ${getNovelStatusLibrary()};
                    modules['@libs/storage'] = ${getStorageLibrary()};
                    
                    // Define require function
                    globalThis.require = function(moduleName) {
                        if (modules[moduleName]) {
                            return modules[moduleName];
                        }
                        throw new Error('Module not found: ' + moduleName);
                    };
                })();
            """.trimIndent()
            
            engine.evaluateScript(moduleSetup)
        } catch (e: Exception) {
            // Fallback to empty implementations if library loading fails
            setupFallbackImplementations()
        }
    }
    
    /**
     * Loads the Cheerio library and registers it in the module system.
     */
    private fun loadCheerioLibrary() {
        try {
            val cheerioCode = loadLibraryFromResources("/js/cheerio.min.js")
            if (cheerioCode != null) {
                // Wrap cheerio in a module loader that captures exports
                val wrapper = """
                    (function() {
                        var exports = {};
                        var module = { exports: exports };
                        $cheerioCode
                        // Cheerio typically exports as module.exports or has a load function
                        globalThis.__modules['cheerio'] = module.exports.default || module.exports;
                    })();
                """.trimIndent()
                engine.evaluateScript(wrapper)
            } else {
                // Use fallback
                engine.evaluateScript("globalThis.__modules['cheerio'] = ${getCheerioFallback()};")
            }
        } catch (e: Exception) {
            // Use fallback on error
            engine.evaluateScript("globalThis.__modules['cheerio'] = ${getCheerioFallback()};")
        }
    }
    
    /**
     * Loads the Dayjs library and registers it in the module system.
     */
    private fun loadDayjsLibrary() {
        try {
            val dayjsCode = loadLibraryFromResources("/js/dayjs.min.js")
            if (dayjsCode != null) {
                val wrapper = """
                    (function() {
                        var exports = {};
                        var module = { exports: exports };
                        $dayjsCode
                        globalThis.__modules['dayjs'] = module.exports.default || module.exports;
                    })();
                """.trimIndent()
                engine.evaluateScript(wrapper)
            } else {
                engine.evaluateScript("globalThis.__modules['dayjs'] = ${getDayjsFallback()};")
            }
        } catch (e: Exception) {
            engine.evaluateScript("globalThis.__modules['dayjs'] = ${getDayjsFallback()};")
        }
    }
    
    /**
     * Loads the Urlencode library and registers it in the module system.
     */
    private fun loadUrlencodeLibrary() {
        try {
            val urlencodeCode = loadLibraryFromResources("/js/urlencode.min.js")
            if (urlencodeCode != null) {
                val wrapper = """
                    (function() {
                        var exports = {};
                        var module = { exports: exports };
                        $urlencodeCode
                        globalThis.__modules['urlencode'] = module.exports.default || module.exports;
                    })();
                """.trimIndent()
                engine.evaluateScript(wrapper)
            } else {
                engine.evaluateScript("globalThis.__modules['urlencode'] = ${getUrlencodeFallback()};")
            }
        } catch (e: Exception) {
            engine.evaluateScript("globalThis.__modules['urlencode'] = ${getUrlencodeFallback()};")
        }
    }
    
    /**
     * Loads a JavaScript library from resources.
     */
    private fun loadLibraryFromResources(path: String): String? {
        return try {
            // Try to load from resources
            val resourceStream = javaClass.getResourceAsStream(path)
            resourceStream?.bufferedReader()?.use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Fallback Cheerio implementation using jsoup bridge.
     * Returns an object with a load function that provides cheerio-like API using native jsoup.
     */
    private fun getCheerioFallback(): String {
        return """
            {
                load: function(html) {
                    // Use native jsoup through the bridge
                    if (typeof __nativeCheerio !== 'undefined') {
                        return __nativeCheerio.load(html);
                    }
                    
                    // Fallback: minimal cheerio-like API
                    var doc = { html: html };
                    var createCheerio = function(elements, html) {
                        return {
                            find: function(selector) { return createCheerio([], html); },
                            text: function() { return ''; },
                            attr: function(name, value) { 
                                if (arguments.length === 1) return '';
                                return this;
                            },
                            html: function() { return html || ''; },
                            first: function() { return this; },
                            last: function() { return this; },
                            eq: function(i) { return this; },
                            map: function(fn) { return { get: function() { return []; }, toArray: function() { return []; } }; },
                            toArray: function() { return []; },
                            get: function(i) { return []; },
                            length: 0
                        };
                    };
                    return function(selector) {
                        return createCheerio([], html);
                    };
                }
            }
        """.trimIndent()
    }
    
    /**
     * Fallback Dayjs implementation if library cannot be loaded.
     */
    private fun getDayjsFallback(): String {
        return """
            (function() {
                // Dayjs fallback - minimal implementation
                return function(date) {
                    return {
                        format: function(fmt) { return new Date(date).toISOString(); },
                        unix: function() { return Math.floor(new Date(date).getTime() / 1000); }
                    };
                };
            })()
        """.trimIndent()
    }
    
    /**
     * Fallback Urlencode implementation if library cannot be loaded.
     */
    private fun getUrlencodeFallback(): String {
        return """
            (function() {
                // Urlencode fallback - minimal implementation
                return {
                    encode: function(str) { return encodeURIComponent(str); },
                    decode: function(str) { return decodeURIComponent(str); }
                };
            })()
        """.trimIndent()
    }
    
    /**
     * Gets the LNReader filterInputs library.
     * Provides filter type definitions for plugin filters.
     */
    private fun getFilterInputsLibrary(): String {
        return """
            (function() {
                return {
                    FilterTypes: {
                        Picker: 'Picker',
                        Text: 'Text',
                        TextInput: 'Text',
                        Switch: 'Switch',
                        Checkbox: 'Checkbox',
                        CheckboxGroup: 'Checkbox',
                        ExcludableCheckbox: 'ExcludableCheckbox',
                        ExcludableCheckboxGroup: 'XCheckbox',
                        TriState: 'TriState',
                        Sort: 'Sort',
                        Title: 'Title'
                    }
                };
            })()
        """.trimIndent()
    }
    
    /**
     * Gets the LNReader defaultCover library.
     * Provides a default cover image URL.
     */
    private fun getDefaultCoverLibrary(): String {
        return """
            (function() {
                return {
                    defaultCover: 'https://via.placeholder.com/300x400?text=No+Cover'
                };
            })()
        """.trimIndent()
    }
    
    /**
     * Gets the LNReader fetch library.
     * Wraps the global fetch API with LNReader-specific functionality.
     */
    private fun getFetchLibrary(): String {
        return """
            (function() {
                return {
                    fetchApi: function(url, options) {
                        return fetch(url, options);
                    },
                    fetchFile: function(url) {
                        return fetch(url).then(function(res) { return res.text(); });
                    }
                };
            })()
        """.trimIndent()
    }
    
    /**
     * Gets the LNReader novelStatus library.
     * Provides novel status constants.
     */
    private fun getNovelStatusLibrary(): String {
        return """
            (function() {
                return {
                    NovelStatus: {
                        Unknown: 0,
                        Ongoing: 1,
                        Completed: 2,
                        Licensed: 3,
                        PublishingFinished: 4,
                        Cancelled: 5,
                        OnHiatus: 6
                    }
                };
            })()
        """.trimIndent()
    }
    
    /**
     * Gets the LNReader storage library.
     * Provides access to the plugin storage API.
     * Returns an object with storage, localStorage, and sessionStorage properties.
     * This matches LNReader's pluginManager behavior where @libs/storage returns
     * { storage: Storage, localStorage: LocalStorage, sessionStorage: SessionStorage }
     */
    private fun getStorageLibrary(): String {
        return """
            (function() {
                // Return an object with storage, localStorage, and sessionStorage
                // Each property forwards to the corresponding globalThis object
                return {
                    storage: {
                        set: function(key, value, expires) {
                            return globalThis.storage.set(key, value, expires);
                        },
                        get: function(key) {
                            return globalThis.storage.get(key);
                        },
                        delete: function(key) {
                            return globalThis.storage.delete(key);
                        },
                        clearAll: function() {
                            return globalThis.storage.clearAll();
                        },
                        getAllKeys: function() {
                            return globalThis.storage.getAllKeys();
                        }
                    },
                    localStorage: {
                        setItem: function(key, value) {
                            return globalThis.localStorage.setItem(key, value);
                        },
                        getItem: function(key) {
                            return globalThis.localStorage.getItem(key);
                        },
                        removeItem: function(key) {
                            return globalThis.localStorage.removeItem(key);
                        },
                        clear: function() {
                            return globalThis.localStorage.clear();
                        }
                    },
                    sessionStorage: {
                        setItem: function(key, value) {
                            return globalThis.sessionStorage.setItem(key, value);
                        },
                        getItem: function(key) {
                            return globalThis.sessionStorage.getItem(key);
                        },
                        removeItem: function(key) {
                            return globalThis.sessionStorage.removeItem(key);
                        },
                        clear: function() {
                            return globalThis.sessionStorage.clear();
                        }
                    }
                };
            })()
        """.trimIndent()
    }
    
    /**
     * Loads the htmlparser2 library and registers it in the module system.
     */
    private fun loadHtmlParser2Library() {
        engine.evaluateScript("globalThis.__modules['htmlparser2'] = ${getHtmlParser2Library()};")
    }
    
    /**
     * Gets the htmlparser2 library.
     * Provides HTML parsing functionality.
     */
    private fun getHtmlParser2Library(): String {
        // Improved htmlparser2-compatible implementation
        return """
            (function() {
                // List of void/self-closing elements
                const voidElements = new Set([
                    'area', 'base', 'br', 'col', 'embed', 'hr', 'img', 'input',
                    'link', 'meta', 'param', 'source', 'track', 'wbr'
                ]);
                
                return {
                    Parser: function(handlers, options) {
                        this.handlers = handlers || {};
                        this.options = options || {};
                        this.tagStack = [];
                        
                        // Add isVoidElement method to Parser instance
                        this.isVoidElement = function(tagName) {
                            return voidElements.has(tagName.toLowerCase());
                        };
                        
                        this.write = function(html) {
                            // Simple HTML parser - extract text and basic tags
                            const tagRegex = /<(\/?)([\w-]+)([^>]*)>/g;
                            let match;
                            let lastIndex = 0;
                            
                            while ((match = tagRegex.exec(html)) !== null) {
                                // Handle text before tag
                                if (match.index > lastIndex) {
                                    const text = html.substring(lastIndex, match.index);
                                    if (text && this.handlers.ontext) {
                                        this.handlers.ontext(text);
                                    }
                                }
                                
                                const isClosing = match[1] === '/';
                                const tagName = match[2].toLowerCase();
                                const attrsStr = match[3];
                                const isSelfClosing = attrsStr.trim().endsWith('/');
                                
                                if (isClosing) {
                                    if (this.handlers.onclosetag) {
                                        this.handlers.onclosetag(tagName);
                                    }
                                } else {
                                    // Parse attributes
                                    const attrs = {};
                                    const attrRegex = /([\w-]+)(?:=["']([^"']*)["'])?/g;
                                    let attrMatch;
                                    while ((attrMatch = attrRegex.exec(attrsStr)) !== null) {
                                        if (attrMatch[1] && attrMatch[1] !== '/') {
                                            attrs[attrMatch[1]] = attrMatch[2] || '';
                                        }
                                    }
                                    
                                    if (this.handlers.onopentag) {
                                        this.handlers.onopentag(tagName, attrs);
                                    }
                                    
                                    // Auto-close void elements
                                    if (voidElements.has(tagName) || isSelfClosing) {
                                        if (this.handlers.onclosetag) {
                                            this.handlers.onclosetag(tagName);
                                        }
                                    }
                                }
                                
                                lastIndex = tagRegex.lastIndex;
                            }
                            
                            // Handle remaining text
                            if (lastIndex < html.length) {
                                const text = html.substring(lastIndex);
                                if (text && this.handlers.ontext) {
                                    this.handlers.ontext(text);
                                }
                            }
                        };
                        
                        this.end = function() {
                            if (this.handlers.onend) {
                                this.handlers.onend();
                            }
                        };
                    }
                };
            })()
        """.trimIndent()
    }
    
    /**
     * Sets up fallback implementations if library loading fails.
     */
    private fun setupFallbackImplementations() {
        try {
            val fallback = """
                globalThis.require = function(moduleName) {
                    throw new Error('Module system not available: ' + moduleName);
                };
                globalThis.fetch = function() {
                    return Promise.reject(new Error('Fetch API not available'));
                };
                globalThis.storage = {
                    set: function() {},
                    get: function() { return null; },
                    delete: function() {},
                    clearAll: function() {},
                    getAllKeys: function() { return []; }
                };
                globalThis.localStorage = {
                    setItem: function() {},
                    getItem: function() { return null; },
                    removeItem: function() {},
                    clear: function() {}
                };
                globalThis.sessionStorage = {
                    setItem: function() {},
                    getItem: function() { return null; },
                    removeItem: function() {},
                    clear: function() {}
                };
            """.trimIndent()
            
            engine.evaluateScript(fallback)
        } catch (e: Exception) {
            // If even fallback fails, we can't do much
        }
    }
}
