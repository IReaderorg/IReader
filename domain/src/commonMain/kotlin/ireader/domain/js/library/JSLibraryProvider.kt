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
    
    private val fetchApi = JSFetchApi(httpClient, pluginId)
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
                    
                    // Setup fetch API
                    globalThis.fetch = function(url, init) {
                        const result = __nativeFetch.fetch(url, init || {});
                        return Promise.resolve({
                            ok: result.ok,
                            status: result.status,
                            statusText: result.statusText,
                            text: function() { return Promise.resolve(result.text); },
                            json: function() { return Promise.resolve(JSON.parse(result.text)); },
                            headers: result.headers
                        });
                    };
                    
                    // Setup window and location objects for browser compatibility
                    globalThis.window = globalThis;
                    globalThis.location = {
                        href: 'about:blank',
                        protocol: 'about:',
                        host: '',
                        hostname: '',
                        port: '',
                        pathname: 'blank',
                        search: '',
                        hash: ''
                    };
                    
                    // Setup URL API polyfill
                    globalThis.URL = function(url, base) {
                        // Parse URL
                        let fullUrl = url;
                        if (base && !url.match(/^https?:\/\//)) {
                            // Relative URL - combine with base
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
                        
                        // Parse the URL
                        const match = fullUrl.match(/^(https?):\/\/([^\/]+)(\/.*)?$/);
                        if (!match) {
                            throw new Error('Invalid URL: ' + fullUrl);
                        }
                        
                        this.protocol = match[1] + ':';
                        this.host = match[2];
                        this.hostname = match[2].split(':')[0];
                        this.port = match[2].includes(':') ? match[2].split(':')[1] : '';
                        this.pathname = match[3] || '/';
                        this.href = fullUrl;
                        this.origin = match[1] + '://' + match[2];
                        
                        // Parse search params
                        const searchIndex = this.pathname.indexOf('?');
                        if (searchIndex !== -1) {
                            this.search = this.pathname.substring(searchIndex);
                            this.pathname = this.pathname.substring(0, searchIndex);
                        } else {
                            this.search = '';
                        }
                        
                        // Parse hash
                        const hashIndex = this.pathname.indexOf('#');
                        if (hashIndex !== -1) {
                            this.hash = this.pathname.substring(hashIndex);
                            this.pathname = this.pathname.substring(0, hashIndex);
                        } else {
                            this.hash = '';
                        }
                        
                        this.toString = function() { return this.href; };
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
                                        const tagInfo = {
                                            name: tagName,
                                            isVoidElement: function() { return voidElements.has(tagName); }
                                        };
                                        this.handlers.onclosetag(tagName, tagInfo);
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
                                            const tagInfo = {
                                                name: tagName,
                                                isVoidElement: function() { return true; }
                                            };
                                            this.handlers.onclosetag(tagName, tagInfo);
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
