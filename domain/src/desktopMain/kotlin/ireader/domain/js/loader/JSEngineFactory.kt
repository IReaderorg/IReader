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

/**
 * Desktop implementation using GraalVM Polyglot loaded from plugin.
 */
actual fun createEngine(bridgeService: JSBridgeService): JSEngine {
    if (GraalVMEngineHelper.isGraalVMReady()) {
        return GraalVMReflectionEngine(bridgeService)
    }
    
    if (GraalVMEngineHelper.tryInitializeGraalVM()) {
        return GraalVMReflectionEngine(bridgeService)
    }
    
    return StubJSEngine()
}

/**
 * Helper object to manage GraalVM plugin integration.
 */
object GraalVMEngineHelper : KoinComponent {
    private const val GRAALVM_PLUGIN_ID = "io.github.ireaderorg.plugins.graalvm-engine"

    
    private var graalvmReady = false
    private var initAttempted = false
    private var graalvmClassLoader: ClassLoader? = null
    
    // Cached class and method references for performance
    private var cachedContextClass: Class<*>? = null
    private var cachedContextBuilderClass: Class<*>? = null
    private var cachedValueClass: Class<*>? = null
    private var cachedCreateMethod: Method? = null
    private var cachedNewBuilderMethod: Method? = null
    private var cachedBuildMethod: Method? = null
    private var cachedEvalMethod: Method? = null
    private var cachedCloseMethod: Method? = null
    private var cachedAsStringMethod: Method? = null
    private var cachedAllowAllAccessMethod: Method? = null
    
    private val pluginManager: PluginManager by inject()
    
    fun isGraalVMReady(): Boolean = graalvmReady
    fun isGraalVMLoaded(): Boolean = graalvmReady
    fun getGraalVMClassLoader(): ClassLoader? = graalvmClassLoader
    
    // Provide cached references
    fun getContextClass(): Class<*>? = cachedContextClass
    fun getValueClass(): Class<*>? = cachedValueClass
    fun getNewBuilderMethod(): Method? = cachedNewBuilderMethod
    fun getAllowAllAccessMethod(): Method? = cachedAllowAllAccessMethod
    fun getBuildMethod(): Method? = cachedBuildMethod
    fun getEvalMethod(): Method? = cachedEvalMethod
    fun getCloseMethod(): Method? = cachedCloseMethod
    fun getAsStringMethod(): Method? = cachedAsStringMethod
    
    @Synchronized
    fun tryInitializeGraalVM(): Boolean {
        if (graalvmReady) return true
        
        val pluginClassLoader = PluginClassLoader.getClassLoader(GRAALVM_PLUGIN_ID)
        if (pluginClassLoader == null) {
            Log.info { "GraalVMEngineHelper: Plugin ClassLoader not available yet" }
            return false
        }
        
        // Allow retry if we have a new classloader (plugin was reinstalled)
        if (initAttempted && graalvmClassLoader == pluginClassLoader) {
            Log.debug { "GraalVMEngineHelper: Already attempted with this classloader" }
            return false
        }
        
        Log.info { "GraalVMEngineHelper: Initializing GraalVM..." }
        
        try {
            // Load GraalVM Polyglot classes via reflection
            val contextClass = pluginClassLoader.loadClass("org.graalvm.polyglot.Context")
            val valueClass = pluginClassLoader.loadClass("org.graalvm.polyglot.Value")
            
            cachedContextClass = contextClass
            cachedValueClass = valueClass
            
            // Get Context.newBuilder("js") method
            cachedNewBuilderMethod = contextClass.getMethod("newBuilder", Array<String>::class.java)
            
            // Get Builder class and methods
            val builderClass = pluginClassLoader.loadClass("org.graalvm.polyglot.Context\$Builder")
            cachedContextBuilderClass = builderClass
            cachedAllowAllAccessMethod = builderClass.getMethod("allowAllAccess", Boolean::class.java)
            cachedBuildMethod = builderClass.getMethod("build")
            
            // Get Context methods
            cachedEvalMethod = contextClass.getMethod("eval", String::class.java, CharSequence::class.java)
            cachedCloseMethod = contextClass.getMethod("close")
            
            // Get Value methods
            cachedAsStringMethod = valueClass.getMethod("asString")
            
            // Test creating a context with ICU disabled to avoid NoClassDefFoundError
            val builder = cachedNewBuilderMethod?.invoke(null, arrayOf("js"))
            var configuredBuilder = cachedAllowAllAccessMethod?.invoke(builder, true)
            
            // Try to disable ICU-based Intl and set UTC timezone
            try {
                val optionMethod = configuredBuilder?.javaClass?.getMethod("option", String::class.java, String::class.java)
                configuredBuilder = optionMethod?.invoke(configuredBuilder, "js.intl-402", "false")
                configuredBuilder = optionMethod?.invoke(configuredBuilder, "js.timezone", "UTC")
            } catch (e: Exception) {
                Log.debug { "GraalVMEngineHelper: Could not set js.intl-402 option: ${e.message}" }
            }
            
            val context = cachedBuildMethod?.invoke(configuredBuilder)
            cachedCloseMethod?.invoke(context)
            
            graalvmClassLoader = pluginClassLoader
            graalvmReady = true
            initAttempted = true
            Log.info { "GraalVMEngineHelper: GraalVM initialized successfully!" }
            return true
            
        } catch (e: Exception) {
            Log.error { "GraalVMEngineHelper: Failed to initialize: ${e.message}" }
            Log.error { "GraalVMEngineHelper: Exception type: ${e.javaClass.name}" }
            if (e.cause != null) {
                Log.error { "GraalVMEngineHelper: Cause: ${e.cause?.message}" }
            }
            e.printStackTrace()
            initAttempted = true
            graalvmClassLoader = pluginClassLoader // Remember which classloader failed
            return false
        }
    }
    
    fun reset() {
        initAttempted = false
        graalvmReady = false
        graalvmClassLoader = null
        cachedContextClass = null
        cachedContextBuilderClass = null
        cachedValueClass = null
        cachedCreateMethod = null
        cachedNewBuilderMethod = null
        cachedBuildMethod = null
        cachedEvalMethod = null
        cachedCloseMethod = null
        cachedAsStringMethod = null
        cachedAllowAllAccessMethod = null
    }
    
    fun isGraalVMPluginAvailable(): Boolean {
        if (PluginClassLoader.getClassLoader(GRAALVM_PLUGIN_ID) != null) return true
        val plugins = pluginManager.pluginsFlow.value
        val graalvmPlugin = plugins.find { it.id == GRAALVM_PLUGIN_ID }
        return graalvmPlugin != null && graalvmPlugin.status == PluginStatus.ENABLED
    }
    
    fun onGraalVMPluginAvailable() {
        if (!graalvmReady) {
            Log.info { "GraalVMEngineHelper: Plugin became available, resetting" }
            reset()
        }
    }
}

private class StubJSEngine : JSEngine {
    override suspend fun loadPlugin(jsCode: String, pluginId: String): LNReaderPlugin {
        throw NoJSEngineException(
            "JavaScript engine not available. Please install the 'GraalVM JavaScript Engine' " +
            "plugin from the Feature Store to use JavaScript-based sources."
        )
    }
    override fun close() {}
    override fun isLoaded(): Boolean = false
}


/**
 * Cached adapter code - parsed once, reused for all plugins.
 * NOTE: Avoid regex literals - use string methods or new RegExp() to avoid TRegex dependency.
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

        // CRITICAL: Force-override Intl FIRST before any other code runs
        // This prevents ICU NoClassDefFoundError when dayjs or other libs use Intl
        (function() {
            var monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
                              'July', 'August', 'September', 'October', 'November', 'December'];
            var monthNamesShort = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                                   'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
            var dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
            var dayNamesShort = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
            
            function pad(n) { return n < 10 ? '0' + n : String(n); }
            
            // Force-override Intl completely to avoid ICU dependency
            // This MUST happen before any library (like dayjs) tries to use Intl
            var IntlPolyfill = {
                DateTimeFormat: function(locale, options) {
                    this.locale = locale || 'en';
                    this.options = options || {};
                },
                NumberFormat: function(locale, options) {
                    this.locale = locale || 'en';
                    this.options = options || {};
                },
                Collator: function(locale, options) {
                    this.locale = locale || 'en';
                    this.options = options || {};
                },
                PluralRules: function(locale, options) {
                    this.locale = locale || 'en';
                },
                RelativeTimeFormat: function(locale, options) {
                    this.locale = locale || 'en';
                    this.options = options || {};
                },
                ListFormat: function(locale, options) {
                    this.locale = locale || 'en';
                    this.options = options || {};
                },
                getCanonicalLocales: function(locales) {
                    if (!locales) return [];
                    if (typeof locales === 'string') return [locales];
                    return Array.prototype.slice.call(locales);
                }
            };
            
            IntlPolyfill.DateTimeFormat.prototype.format = function(date) {
                if (!(date instanceof Date)) date = new Date(date);
                if (isNaN(date.getTime())) return 'Invalid Date';
                var opts = this.options;
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
                return monthNames[date.getMonth()] + ' ' + date.getDate() + ', ' + date.getFullYear();
            };
            IntlPolyfill.DateTimeFormat.prototype.formatToParts = function(date) {
                if (!(date instanceof Date)) date = new Date(date);
                return [
                    { type: 'month', value: monthNames[date.getMonth()] },
                    { type: 'literal', value: ' ' },
                    { type: 'day', value: String(date.getDate()) },
                    { type: 'literal', value: ', ' },
                    { type: 'year', value: String(date.getFullYear()) }
                ];
            };
            IntlPolyfill.DateTimeFormat.prototype.resolvedOptions = function() {
                return { locale: this.locale, calendar: 'gregory', numberingSystem: 'latn', timeZone: 'UTC' };
            };
            IntlPolyfill.DateTimeFormat.supportedLocalesOf = function() { return ['en']; };
            
            IntlPolyfill.NumberFormat.prototype.format = function(num) { return String(num); };
            IntlPolyfill.NumberFormat.prototype.formatToParts = function(num) {
                return [{ type: 'integer', value: String(Math.floor(num)) }];
            };
            IntlPolyfill.NumberFormat.prototype.resolvedOptions = function() {
                return { locale: this.locale, numberingSystem: 'latn' };
            };
            IntlPolyfill.NumberFormat.supportedLocalesOf = function() { return ['en']; };
            
            IntlPolyfill.Collator.prototype.compare = function(a, b) {
                return String(a).localeCompare(String(b));
            };
            IntlPolyfill.Collator.prototype.resolvedOptions = function() {
                return { locale: this.locale };
            };
            IntlPolyfill.Collator.supportedLocalesOf = function() { return ['en']; };
            
            IntlPolyfill.PluralRules.prototype.select = function(n) {
                return n === 1 ? 'one' : 'other';
            };
            IntlPolyfill.PluralRules.prototype.resolvedOptions = function() {
                return { locale: this.locale };
            };
            IntlPolyfill.PluralRules.supportedLocalesOf = function() { return ['en']; };
            
            IntlPolyfill.RelativeTimeFormat.prototype.format = function(value, unit) {
                var absVal = Math.abs(value);
                var suffix = value < 0 ? ' ago' : ' from now';
                return absVal + ' ' + unit + (absVal !== 1 ? 's' : '') + suffix;
            };
            IntlPolyfill.RelativeTimeFormat.prototype.resolvedOptions = function() {
                return { locale: this.locale };
            };
            IntlPolyfill.RelativeTimeFormat.supportedLocalesOf = function() { return ['en']; };
            
            IntlPolyfill.ListFormat.prototype.format = function(list) {
                return Array.prototype.slice.call(list).join(', ');
            };
            IntlPolyfill.ListFormat.prototype.resolvedOptions = function() {
                return { locale: this.locale };
            };
            IntlPolyfill.ListFormat.supportedLocalesOf = function() { return ['en']; };
            
            // FORCE override - don't check if exists, always replace
            globalThis.Intl = IntlPolyfill;
            
            // Override Date prototype methods to avoid ICU
            Date.prototype.toLocaleString = function(locale, options) {
                return monthNames[this.getMonth()] + ' ' + this.getDate() + ', ' + this.getFullYear() + 
                       ' ' + pad(this.getHours()) + ':' + pad(this.getMinutes()) + ':' + pad(this.getSeconds());
            };
            
            Date.prototype.toLocaleDateString = function(locale, options) {
                return monthNames[this.getMonth()] + ' ' + this.getDate() + ', ' + this.getFullYear();
            };
            
            Date.prototype.toLocaleTimeString = function(locale, options) {
                return pad(this.getHours()) + ':' + pad(this.getMinutes()) + ':' + pad(this.getSeconds());
            };
        })();

        // Helper function to check if URL is absolute (has protocol)
        function isAbsoluteUrl(url) {
            return url.indexOf('http://') === 0 || url.indexOf('https://') === 0;
        }
        
        // Helper function to parse URL without regex
        function parseUrl(fullUrl) {
            var result = { protocol: '', host: '', hostname: '', port: '', pathname: '/', search: '', hash: '' };
            var url = fullUrl;
            
            // Extract hash
            var hashIdx = url.indexOf('#');
            if (hashIdx !== -1) {
                result.hash = url.substring(hashIdx);
                url = url.substring(0, hashIdx);
            }
            
            // Extract search/query
            var queryIdx = url.indexOf('?');
            if (queryIdx !== -1) {
                result.search = url.substring(queryIdx);
                url = url.substring(0, queryIdx);
            }
            
            // Extract protocol
            var protoIdx = url.indexOf('://');
            if (protoIdx !== -1) {
                result.protocol = url.substring(0, protoIdx) + ':';
                url = url.substring(protoIdx + 3);
            }
            
            // Extract pathname
            var pathIdx = url.indexOf('/');
            if (pathIdx !== -1) {
                result.pathname = url.substring(pathIdx);
                url = url.substring(0, pathIdx);
            }
            
            // What remains is host (possibly with port)
            result.host = url;
            var portIdx = url.lastIndexOf(':');
            if (portIdx !== -1 && portIdx > url.lastIndexOf(']')) {
                result.hostname = url.substring(0, portIdx);
                result.port = url.substring(portIdx + 1);
            } else {
                result.hostname = url;
            }
            
            return result;
        }
        
        // Helper to get base URL (protocol + host)
        function getBaseUrl(url) {
            var protoIdx = url.indexOf('://');
            if (protoIdx === -1) return '';
            var rest = url.substring(protoIdx + 3);
            var pathIdx = rest.indexOf('/');
            if (pathIdx === -1) return url;
            return url.substring(0, protoIdx + 3 + pathIdx);
        }
        
        // Helper to get directory part of URL
        function getUrlDirectory(url) {
            var lastSlash = url.lastIndexOf('/');
            if (lastSlash === -1) return url;
            return url.substring(0, lastSlash + 1);
        }

        // URL polyfill
        if (typeof URL === 'undefined') {
            globalThis.URL = function(url, base) {
                if (url === null || url === undefined) throw new Error('Invalid URL');
                url = String(url);
                var fullUrl = url;
                if (base && !isAbsoluteUrl(url)) {
                    base = String(base);
                    if (url.indexOf('/') === 0) {
                        fullUrl = getBaseUrl(base) + url;
                    } else {
                        fullUrl = getUrlDirectory(base) + url;
                    }
                }
                if (!isAbsoluteUrl(fullUrl)) throw new Error('Invalid URL: ' + fullUrl);
                var parsed = parseUrl(fullUrl);
                this.protocol = parsed.protocol;
                this.host = parsed.host;
                this.hostname = parsed.hostname;
                this.port = parsed.port;
                this.pathname = parsed.pathname || '/';
                this.search = parsed.search;
                this.hash = parsed.hash;
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

        // FormData polyfill - needed for POST requests with form data
        if (typeof FormData === 'undefined') {
            globalThis.FormData = function() {
                this._data = {};
            };
            globalThis.FormData.prototype.append = function(key, value) {
                if (!this._data[key]) {
                    this._data[key] = [];
                }
                this._data[key].push(String(value));
            };
            globalThis.FormData.prototype.set = function(key, value) {
                this._data[key] = [String(value)];
            };
            globalThis.FormData.prototype.get = function(key) {
                return this._data[key] ? this._data[key][0] : null;
            };
            globalThis.FormData.prototype.getAll = function(key) {
                return this._data[key] || [];
            };
            globalThis.FormData.prototype.has = function(key) {
                return key in this._data;
            };
            globalThis.FormData.prototype.delete = function(key) {
                delete this._data[key];
            };
            globalThis.FormData.prototype.keys = function() {
                return Object.keys(this._data);
            };
            globalThis.FormData.prototype.entries = function() {
                var result = [];
                for (var key in this._data) {
                    if (this._data.hasOwnProperty(key)) {
                        for (var i = 0; i < this._data[key].length; i++) {
                            result.push([key, this._data[key][i]]);
                        }
                    }
                }
                return result;
            };
            globalThis.FormData.prototype.toString = function() {
                var parts = [];
                for (var key in this._data) {
                    if (this._data.hasOwnProperty(key)) {
                        for (var i = 0; i < this._data[key].length; i++) {
                            parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(this._data[key][i]));
                        }
                    }
                }
                return parts.join('&');
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
        // This is needed because GraalVM JS may not have ICU support
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
                        // Remove newlines, carriage returns, and tabs without regex
                        return text.split('\n').join('').split('\r').join('').split('\t').join('');
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
 * GraalVM Polyglot engine implementation using reflection.
 * 
 * Uses GraalVM's Polyglot API loaded from the plugin's classloader.
 */
private class GraalVMReflectionEngine(
    private val bridgeService: JSBridgeService
) : JSEngine {
    
    private var context: Any? = null
    private var isEngineLoaded = false
    private val mutex = Mutex()
    
    // Use cached method references from helper
    private val newBuilderMethod = GraalVMEngineHelper.getNewBuilderMethod()
    private val allowAllAccessMethod = GraalVMEngineHelper.getAllowAllAccessMethod()
    private val buildMethod = GraalVMEngineHelper.getBuildMethod()
    private val evalMethod = GraalVMEngineHelper.getEvalMethod()
    private val closeMethod = GraalVMEngineHelper.getCloseMethod()
    private val asStringMethod = GraalVMEngineHelper.getAsStringMethod()
    
    override suspend fun loadPlugin(jsCode: String, pluginId: String): LNReaderPlugin = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                // Close existing context if any
                context?.let { 
                    try { closeMethod?.invoke(it) } catch (e: Exception) {}
                }
                
                Log.info { "GraalVMReflectionEngine: Creating context for plugin: $pluginId" }
                
                // Create new GraalVM Context with JS language
                val builder = newBuilderMethod?.invoke(null, arrayOf("js"))
                    ?: throw ireader.domain.js.engine.PluginLoadException("Failed to create Context builder")
                
                // Configure builder to suppress warnings and allow all access
                var configuredBuilder = allowAllAccessMethod?.invoke(builder, true)
                
                // Try to set options
                try {
                    val optionMethod = configuredBuilder?.javaClass?.getMethod("option", String::class.java, String::class.java)
                    // Suppress interpreter-only warning
                    configuredBuilder = optionMethod?.invoke(configuredBuilder, "engine.WarnInterpreterOnly", "false")
                    // Disable ICU-based Intl to avoid NoClassDefFoundError for ICU classes
                    // Note: js.intl-402 is the correct option name (not js.ecma-402)
                    configuredBuilder = optionMethod?.invoke(configuredBuilder, "js.intl-402", "false")
                    // Use UTC timezone to avoid ICU TimeZone dependency
                    configuredBuilder = optionMethod?.invoke(configuredBuilder, "js.timezone", "UTC")
                } catch (e: Exception) {
                    Log.debug { "GraalVM: Could not set options: ${e.message}" }
                }
                
                val builderWithAccess = configuredBuilder
                
                val ctx = buildMethod?.invoke(builderWithAccess)
                    ?: throw ireader.domain.js.engine.PluginLoadException("Failed to build Context")
                context = ctx
                
                Log.info { "GraalVMReflectionEngine: Context created, loading adapter code..." }
                
                // Load cached adapter code
                try {
                    evalMethod?.invoke(ctx, "js", AdapterCodeCache.code)
                } catch (e: Exception) {
                    val cause = if (e is java.lang.reflect.InvocationTargetException) e.targetException else e
                    Log.error { "GraalVM: Failed to load adapter code: ${cause.message}" }
                    throw cause
                }
                
                // Setup bridge for fetch
                setupBridge(ctx)
                
                // Initialize module system
                evalMethod?.invoke(ctx, "js", "var exports = {}; var module = { exports: exports };")
                
                Log.info { "GraalVMReflectionEngine: Loading plugin code (${jsCode.length} chars)..." }
                
                // Load the plugin code
                try {
                    evalMethod?.invoke(ctx, "js", jsCode)
                } catch (e: Exception) {
                    val cause = if (e is java.lang.reflect.InvocationTargetException) e.targetException else e
                    Log.error { "GraalVM: Failed to load plugin code: ${cause.message}" }
                    cause.printStackTrace()
                    throw cause
                }
                
                Log.info { "GraalVMReflectionEngine: Plugin code loaded, extracting metadata..." }
                
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
                
                val metadataValue = try {
                    evalMethod?.invoke(ctx, "js", metadataScript)
                } catch (e: Exception) {
                    val cause = if (e is java.lang.reflect.InvocationTargetException) e.targetException else e
                    Log.error { "GraalVM: Failed to extract metadata: ${cause.message}" }
                    cause.printStackTrace()
                    throw cause
                }
                
                val metadataJson = try {
                    asStringMethod?.invoke(metadataValue) as? String
                } catch (e: Exception) {
                    Log.warn { "GraalVM: Could not convert metadata to string: ${e.message}" }
                    null
                }
                
                Log.info { "GraalVMReflectionEngine: Plugin loaded successfully, metadata: $metadataJson" }
                
                isEngineLoaded = true
                
                // Create wrapper with pre-extracted metadata
                GraalVMPluginWrapper(this@GraalVMReflectionEngine, pluginId, bridgeService, metadataJson)
                
            } catch (e: Exception) {
                val actualError = if (e is java.lang.reflect.InvocationTargetException) e.targetException else e
                Log.error { "GraalVMReflectionEngine: Failed to load plugin $pluginId: ${actualError.javaClass.name}: ${actualError.message}" }
                actualError.printStackTrace()
                close()
                throw ireader.domain.js.engine.PluginLoadException(
                    "Failed to load plugin: $pluginId - ${actualError.message ?: actualError.javaClass.name}", 
                    actualError
                )
            }
        }
    }
    
    private fun setupBridge(ctx: Any) {
        evalMethod?.invoke(ctx, "js", """
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
                            // Convert FormData to URL-encoded string
                            var parts = [];
                            var data = options.body._data || {};
                            for (var key in data) {
                                if (data.hasOwnProperty(key)) {
                                    var values = data[key];
                                    for (var i = 0; i < values.length; i++) {
                                        parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(values[i]));
                                    }
                                }
                            }
                            bodyStr = parts.join('&');
                            // Set content-type header for form data
                            if (!headersObj['content-type'] && !headersObj['Content-Type']) {
                                headersObj['Content-Type'] = 'application/x-www-form-urlencoded';
                            }
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
            val value = evalMethod?.invoke(context, "js", script)
            asStringMethod?.invoke(value) as? String
        } catch (e: Exception) {
            val cause = if (e is java.lang.reflect.InvocationTargetException) e.targetException else e
            Log.warn { "GraalVM: evaluateStringScript error: ${cause.message}" }
            null
        }
    }
    
    fun evaluateVoidScript(script: String) {
        try {
            evalMethod?.invoke(context, "js", script)
        } catch (e: Exception) {
            val cause = if (e is java.lang.reflect.InvocationTargetException) e.targetException else e
            val errorMsg = cause.message ?: cause.toString()
            
            // Check if this is an ICU-related error
            if (errorMsg.contains("icu", ignoreCase = true) || 
                errorMsg.contains("TimeZone", ignoreCase = true) ||
                errorMsg.contains("NoClassDefFoundError", ignoreCase = true)) {
                Log.warn { "GraalVM: ICU-related error in evaluateVoidScript, script may have used Date/Intl features: $errorMsg" }
                // Don't rethrow - the polyfills should handle this gracefully
            } else {
                Log.warn { "GraalVM: evaluateVoidScript error: $errorMsg" }
            }
        }
    }
    
    override fun close() {
        try {
            context?.let { closeMethod?.invoke(it) }
        } catch (e: Exception) {}
        context = null
        isEngineLoaded = false
    }
    
    override fun isLoaded(): Boolean = isEngineLoaded
}


/**
 * GraalVM plugin wrapper with cached metadata.
 */
private class GraalVMPluginWrapper(
    private val engine: GraalVMReflectionEngine,
    private val pluginId: String,
    private val bridgeService: JSBridgeService,
    metadataJson: String?
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

    // Return cached values - no GraalVM calls needed
    override suspend fun getId(): String = cachedId
    override suspend fun getName(): String = cachedName
    override suspend fun getSite(): String = cachedSite
    override suspend fun getVersion(): String = cachedVersion
    override suspend fun getLang(): String = cachedLang
    override suspend fun getIcon(): String = cachedIcon

    override suspend fun searchNovels(query: String, page: Int): List<PluginNovel> = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val resultJson = awaitPromise("__wrappedPlugin.searchNovels('${query.replace("'", "\\'")}', $page)")
                parseNovelList(resultJson)
            } catch (e: Exception) {
                Log.error { "GraalVMPluginWrapper: Error in searchNovels: ${e.message}" }
                emptyList()
            }
        }
    }

    override suspend fun popularNovels(page: Int, filters: Map<String, Any>): List<PluginNovel> = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val resultJson = awaitPromise("__wrappedPlugin.popularNovels($page)")
                parseNovelList(resultJson)
            } catch (e: Exception) {
                Log.error { "GraalVMPluginWrapper: Error in popularNovels: ${e.message}" }
                emptyList()
            }
        }
    }

    override suspend fun latestNovels(page: Int): List<PluginNovel> = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val resultJson = awaitPromise("__wrappedPlugin.latestNovels($page)")
                parseNovelList(resultJson)
            } catch (e: Exception) {
                Log.error { "GraalVMPluginWrapper: Error in latestNovels: ${e.message}" }
                emptyList()
            }
        }
    }

    override suspend fun getNovelDetails(url: String): PluginNovelDetails = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val resultJson = awaitPromise("__wrappedPlugin.getNovelDetails('${url.replace("'", "\\'")}')")
                parseNovelDetails(resultJson)
            } catch (e: Exception) {
                Log.error { "GraalVMPluginWrapper: Error in getNovelDetails: ${e.message}" }
                PluginNovelDetails("", url, "", null, null, emptyList(), null)
            }
        }
    }

    override suspend fun getChapters(url: String): List<PluginChapter> = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val resultJson = awaitPromise("__wrappedPlugin.getChapters('${url.replace("'", "\\'")}')")
                parseChapterList(resultJson)
            } catch (e: Exception) {
                Log.error { "GraalVMPluginWrapper: Error in getChapters: ${e.message}" }
                emptyList()
            }
        }
    }

    override suspend fun getChapterContent(url: String): String = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                awaitPromise("__wrappedPlugin.getChapterContent('${url.replace("'", "\\'")}')")
            } catch (e: Exception) {
                Log.error { "GraalVMPluginWrapper: Error in getChapterContent: ${e.message}" }
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
            Log.error { "GraalVMPluginWrapper: Error processing fetch: ${e.message}" }
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
