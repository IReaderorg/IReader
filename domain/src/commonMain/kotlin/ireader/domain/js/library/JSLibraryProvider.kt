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
            
            // Setup require() function
            val requireSetup = """
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
                    
                    // Now setup modules (after global APIs are ready)
                    const modules = {};
                    
                    // Cheerio library
                    modules['cheerio'] = ${getCheerioLibrary()};
                    
                    // Dayjs library
                    modules['dayjs'] = ${getDayjsLibrary()};
                    
                    // Urlencode library
                    modules['urlencode'] = ${getUrlencodeLibrary()};
                    
                    // LNReader-specific libraries
                    modules['@libs/filterInputs'] = ${getFilterInputsLibrary()};
                    modules['@libs/defaultCover'] = ${getDefaultCoverLibrary()};
                    modules['@libs/fetch'] = ${getFetchLibrary()};
                    modules['@libs/novelStatus'] = ${getNovelStatusLibrary()};
                    modules['@libs/storage'] = ${getStorageLibrary()};
                    modules['htmlparser2'] = ${getHtmlParser2Library()};
                    
                    // Define require function
                    globalThis.require = function(moduleName) {
                        if (modules[moduleName]) {
                            return modules[moduleName];
                        }
                        throw new Error('Module not found: ' + moduleName);
                    };
                })();
            """.trimIndent()
            
            engine.evaluateScript(requireSetup)
        } catch (e: Exception) {
            // Fallback to empty implementations if library loading fails
            setupFallbackImplementations()
        }
    }
    
    /**
     * Gets the Cheerio library code.
     * Returns a placeholder for now - actual library will be bundled in resources.
     */
    private fun getCheerioLibrary(): String {
        // TODO: Load from resources/js/cheerio.min.js
        return """
            (function() {
                // Cheerio placeholder - will be replaced with actual library
                return {
                    load: function(html) {
                        return {
                            find: function(selector) { return this; },
                            text: function() { return ''; },
                            attr: function(name) { return ''; },
                            html: function() { return html; }
                        };
                    }
                };
            })()
        """.trimIndent()
    }
    
    /**
     * Gets the Dayjs library code.
     * Returns a placeholder for now - actual library will be bundled in resources.
     */
    private fun getDayjsLibrary(): String {
        // TODO: Load from resources/js/dayjs.min.js
        return """
            (function() {
                // Dayjs placeholder - will be replaced with actual library
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
     * Gets the Urlencode library code.
     * Returns a placeholder for now - actual library will be bundled in resources.
     */
    private fun getUrlencodeLibrary(): String {
        // TODO: Load from resources/js/urlencode.min.js
        return """
            (function() {
                // Urlencode placeholder - will be replaced with actual library
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
                        Switch: 'Switch',
                        Checkbox: 'Checkbox',
                        ExcludableCheckbox: 'ExcludableCheckbox',
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
     * Returns the globalThis.storage object directly (lazy evaluation).
     */
    private fun getStorageLibrary(): String {
        return """globalThis.storage"""
    }
    
    /**
     * Gets the htmlparser2 library.
     * Provides HTML parsing functionality.
     */
    private fun getHtmlParser2Library(): String {
        // Minimal htmlparser2-compatible implementation
        return """
            (function() {
                return {
                    Parser: function(handlers) {
                        this.handlers = handlers || {};
                        this.write = function(html) {
                            // Simple HTML parser - just extract text and basic tags
                            // This is a minimal implementation for basic parsing
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
                                
                                if (isClosing) {
                                    if (this.handlers.onclosetag) {
                                        this.handlers.onclosetag(tagName);
                                    }
                                } else {
                                    // Parse attributes
                                    const attrs = {};
                                    const attrRegex = /([\w-]+)=["']([^"']*)["']/g;
                                    let attrMatch;
                                    while ((attrMatch = attrRegex.exec(attrsStr)) !== null) {
                                        attrs[attrMatch[1]] = attrMatch[2];
                                    }
                                    
                                    if (this.handlers.onopentag) {
                                        this.handlers.onopentag(tagName, attrs);
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
